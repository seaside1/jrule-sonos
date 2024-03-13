package org.openhab.automation.jrule.sonos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.automation.jrule.rules.user.SonosCoordinator;
import org.openhab.automation.jrule.rules.user.SonosDeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.google.gson.Gson;

/**
 * The {@link }
 *
 * @author Joseph (Seaside) Hagberg - Initial contribution
 */
public class SonosWsClient implements PropertyChangeListener {
    private static final String SONOS_WEBSOCKET_API = ":1443/websocket/api";
    private static final String WSS = "wss://";
    private HttpClient httpClient;
    private static final String SONOS_API_HEADER = "X-Sonos-Api-Key";
    private static final String SONOS_API_VALUE = "123e4567-e89b-12d3-a456-426655440000";
    private static final String SONOS_PROTOCOL_VALUE = "v1.api.smartspeaker.audio";

    private static final String JSON_X = "[{}, {}]";
    private static final String JSON_AUDIO = "[{\"namespace\": \"audioClip:1\", \"command\": \"loadAudioClip\", \"playerId\": \"{}\"}, {\"name\": \"Sonos Websocket\", \"appId\": \"org.openhab.jrule.sonos\", \"streamUrl\": \"{}\", \"volume\": {}}]";
    private static final String JSON_CANCEL = "[{\"namespace\": \"audioClip:1\", \"command\": \"cancelAudioClip\", \"playerId\": \"{}\"}, {\"name\": \"Sonos Websocket\", \"appId\": \"org.openhab.jrule.sonos\", \"id\": \"{}\"}]";
    private static final int SUCCESS_STATUS_SWITCHING_PROTOCOL = 101;
    private static final long SLEEP_TIME = 10;
    private static final int MIN_ID_LENGTH = 3;

    private final Logger logger = LoggerFactory.getLogger(SonosWsClient.class);
    private WebSocketClient client;
    private SonosWebSocket socket;
    private Future<Session> futureSession;
    private volatile boolean socketResponse = false;
    private final Gson gson = new Gson();

    public SonosWsClient() {
        SslContextFactory factory = new SslContextFactory.Client(true);
        httpClient = new HttpClient(factory);
    }

    public synchronized void sendEmptyRequest() {
        socket.sendMessage(JSON_X);
    }

    public void disconnect() {

        if (socket != null) {
            socket.removePropertyChangeListener(this);
            socket.dispose();
            socket = null;
        }
    }

    public synchronized SonosConnectStatus connect(String host) {
        disconnect();
        String uri = WSS.concat(host).concat(SONOS_WEBSOCKET_API);
        client = new WebSocketClient(httpClient);
        socket = new SonosWebSocket();
        socket.addPropertyChangeListener(this);
        try {
            client.start();
        } catch (Exception x) {
            logger.debug("Failed to start client", x);
            return SonosConnectStatus.START_FAILURE;
        }
        URI destUri = null;
        try {
            destUri = new URI(uri);
        } catch (URISyntaxException x) {
            logger.debug("Failed to create uri from: " + uri, x);
            return SonosConnectStatus.INVALID_URI;
        }
        final ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setSubProtocols(SONOS_PROTOCOL_VALUE);
        request.setHeader(SONOS_API_HEADER, SONOS_API_VALUE);
        logger.debug("Connecting to: {}", destUri);
        try {
            futureSession = client.connect(socket, destUri, request);
            Session session = futureSession.get(); // Waiting for connection
            if (session.getUpgradeResponse().getStatusCode() != SUCCESS_STATUS_SWITCHING_PROTOCOL) {
                logger.error("Failed to connect socket: {}", session.toString());
                return SonosConnectStatus.CONNECT_FAILED;
            }
        } catch (IOException x) {
            logger.debug("Failed to connect", x);
            return SonosConnectStatus.CONNECT_FAILED;
        } catch (InterruptedException x) {
            logger.debug("Failed to connect", x);
            return SonosConnectStatus.CONNECT_FAILED;
        } catch (ExecutionException x) {
            logger.debug("Failed to connect", x);
            return SonosConnectStatus.CONNECT_FAILED;
        }
        return SonosConnectStatus.SUCCESS;
    }

    public void stop() throws Exception {
        if (client != null) {
            socket.removePropertyChangeListener(this);
            socket.dispose();
            client.stop();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent changeEvent) {
        if (changeEvent.getPropertyName().equals(SonosWebSocket.PROPERTY_SOCKET_AUDIOCLIP_RESPONSE)) {
            logger.debug("Socket Audio Response");
            this.socketResponse = true;
            final String onFrameRespone = (String) changeEvent.getNewValue();
            logger.debug("PropChangeSupport Resp: {}", onFrameRespone);
            final SonosAudioClipResponse[] response = gson.fromJson(onFrameRespone, SonosAudioClipResponse[].class);
            final Optional<SonosAudioClipResponse> findAnyAudioClipId = Arrays.stream(response)
                    .filter(r -> r.getId() != null && r.getId().length() > MIN_ID_LENGTH).findAny();
            final Optional<SonosAudioClipResponse> findAnyPlayerId = Arrays.stream(response)
                    .filter(r -> r.getPlayerId() != null && r.getPlayerId().length() > MIN_ID_LENGTH).findAny();

            final String lastId = findAnyAudioClipId.isPresent() ? findAnyAudioClipId.get().getId() : null;
            final String playerId = findAnyPlayerId.isPresent() ? findAnyPlayerId.get().getPlayerId() : null;
            if (lastId != null && playerId != null) {
                final SonosDeviceInfo deviceInfo = SonosCoordinator.get().getDeviceInfoFromUdn(playerId);
                if (deviceInfo != null) {
                    deviceInfo.setLastAudioClipId(lastId);
                }
            }
        }
    }

    public synchronized void sendAudioClip(String udn, String uri, String volume) {
        socket.sendMessage(createJson(JSON_AUDIO, udn, uri, volume));
        int sleepTime = 0;
        int maxSleepTime = 1000;
        while (!socketResponse && sleepTime < maxSleepTime) {
            try {
                Thread.sleep(SLEEP_TIME);
                sleepTime += SLEEP_TIME;
            } catch (InterruptedException x) {
                logger.error("Failed to wait for socket audio clip response", x);
            }
        }
    }

    private String createJson(String json, String udn, String uri, String volume) {
        return MessageFormatter.arrayFormat(json, new Object[] { udn, uri, volume }).getMessage();

    }

    private String createJson(String json, String udn, String lastAudioClipId) {
        return MessageFormatter.arrayFormat(json, new Object[] { udn, lastAudioClipId}).getMessage();

    }

    
    public void cancelAudioClip(String udn, String lastAudioClipId) {
        if (lastAudioClipId == null) {
            lastAudioClipId = SonosCoordinator.get().getDeviceInfoFromUdn(udn).getLastAudioClipId();
        }
        logger.debug("Cancelling audio clip for udn: {} clipId: {}", udn, lastAudioClipId);
        socket.sendMessage(createJson(JSON_CANCEL, udn, lastAudioClipId));
        int sleepTime = 0;
        int maxSleepTime = 1000;
        while (!socketResponse && sleepTime < maxSleepTime) {
            try {
                Thread.sleep(SLEEP_TIME);
                sleepTime += SLEEP_TIME;
            } catch (InterruptedException x) {
                logger.error("Failed to wait for socket audio clip response", x);
            }
        }
        
    }
}
