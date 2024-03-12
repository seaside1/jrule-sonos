package org.openhab.automation.jrule.sonos;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
public class SonosWebSocket {

    private static final String UTF_8 = "UTF-8";
    private static final int FRAME_MIN_SIZE = 1;
    public static final String PROPERTY_SOCKET_CLOSED = "SOCKET_CLOSED";
    public static final String PROPERTY_SOCKET_CONNECTED = "SOCKET_CONNECTED";
    public static final String PROPERTY_SOCKET_AUDIOCLIP_RESPONSE = "SOCKET_AUDIO_CLIP_RESPONSE";
    private final CountDownLatch closeLatch;
    private Session session;
    private final Logger logger = LoggerFactory.getLogger(SonosWebSocket.class);
    private final PropertyChangeSupport propertyChangeSupport;
     
    public SonosWebSocket() {
         this.closeLatch = new CountDownLatch(1);
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.info("Connection closed: {} - {}", statusCode, reason);
        this.session = null;
        this.closeLatch.countDown(); // trigger latch
        propertyChangeSupport.firePropertyChange(SonosWebSocket.PROPERTY_SOCKET_CLOSED, null,
                "statusCode: " + statusCode + " reason: " + reason);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info("Got connect: {}", session);
        this.session = session;
        logger.info("Connection status code: {} reason: {}", session.getUpgradeResponse().getStatusCode(),
                session.getUpgradeResponse().getStatusReason());
        propertyChangeSupport.firePropertyChange(SonosWebSocket.PROPERTY_SOCKET_CONNECTED, null, session);
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        logger.debug("Got msg: {}", msg);
    }

    public void sendMessage(String msg) {
        try {
            logger.debug("Send msg: {}", msg);
            this.session.getRemote().sendString(msg);
        } catch (IOException e) {

        }
    }

    @OnWebSocketFrame
    public synchronized void onFrame(Frame frame) throws UnsupportedEncodingException {
        if (session == null) {
            return;
        }
        if (frame.getPayloadLength() <= FRAME_MIN_SIZE) {
            logger.debug("Failed to decode frame, not enough byte: {}", frame.getPayloadLength());
            return;
        }

        final byte[] bytes = new byte[frame.getPayloadLength()];
        frame.getPayload().get(bytes);
        String frameString = new String(bytes, UTF_8);
        logger.debug("Got Frame: {}", frameString);
        if (frameString.contains("audioClip:1")) {
            propertyChangeSupport.firePropertyChange(SonosWebSocket.PROPERTY_SOCKET_AUDIOCLIP_RESPONSE, null, frameString);
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.info("Error in websocket: {}", cause.getMessage(), cause);
        
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.addPropertyChangeListener(pcl);
    }

    public synchronized void dispose() {
        if (session == null) {
            return;
        }
        try {
            session.close();
            session = null;

        } catch (Exception x) {
            try {
                session.disconnect();
                session = null;
            } catch (IOException e) {
            }
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(pcl);
    }

    class HouseholdId {
        private String householdId;

        public String getHouseholdId() {
            return householdId;
        }

        public void setHouseholdId(String householdId) {
            this.householdId = householdId;
        }
        
    }
}
