package org.openhab.automation.jrule.rules.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.automation.jrule.items.JRuleNumberItem;
import org.openhab.automation.jrule.sonos.SonosWsClient;

public class SonosCoordinator {

    private static SonosCoordinator instance = null;
    private static final String SONOS_ID_REGEX = "\\bSonos_([A-Z0-9_]+)\\b";
    private static final Pattern SONOS_ID_PATTERN = Pattern.compile(SONOS_ID_REGEX);
   
    private Map<String, SonosDeviceInfo> udnToDeviceInfo = new HashMap<>();
    private final SonosWsClient sonosWsClient;
    private static final String DEFAULT_VOLUME = "35";

    private SonosCoordinator() {
        sonosWsClient = new SonosWsClient();
    }

    public Collection<SonosDeviceInfo> getDeviceInfos() {
        return udnToDeviceInfo.values();
    }

    public  String extractSonosId(String itemName) {
        Matcher matcher = SONOS_ID_PATTERN.matcher(itemName);    
        return matcher.find() ? matcher.group(1) : null;
    }
    
    public static SonosCoordinator get() {
        if (instance == null) {
            instance = new SonosCoordinator();
        }
        return instance;
    }
 
    
    public SonosDeviceInfo getDeviceInfoFromItem(String itemName) {
        return udnToDeviceInfo.get(extractSonosId(itemName));
    }
    
    public String getIpFromItem(String itemName) {
        return udnToDeviceInfo.get(extractSonosId(itemName)).getIp();
    }

    public void addDeviInfo(SonosDeviceInfo deviceInfo) {
        udnToDeviceInfo.put(deviceInfo.getUdn(), deviceInfo);
    }

    public List<String> getUriItemNames() {
        return udnToDeviceInfo.values().stream().map(d -> d.getUriItemName()).toList();
    }

    public synchronized void playAudioClip(String ip, String uri, String udn, String volume) {
        sonosWsClient.connect(ip);
        sonosWsClient.sendAudioClip(udn, uri, volume);
        sonosWsClient.disconnect();
    }
    
    public String getVolume(SonosDeviceInfo deviceInfo) {
        final JRuleNumberItem volumeItem = JRuleNumberItem.forName(deviceInfo.getVolumeItemName());
        final String volumeStateString = volumeItem != null && volumeItem.getState() != null ? volumeItem.getState().stringValue() : null;
        return  volumeStateString == null || volumeStateString.isEmpty() ? DEFAULT_VOLUME : volumeStateString;
    }

    public synchronized void playAudioClip(SonosDeviceInfo info, String uri, String volume) {
        playAudioClip(info.getIp(), uri, info.getUdn(), volume == null ? getVolume(info) : volume);
    }

    
    public void cancelLastAudioClip(String sonosDeviceIp, String udn, String lastAudioClipId) {
        sonosWsClient.connect(sonosDeviceIp);
        sonosWsClient.cancelAudioClip(udn, lastAudioClipId);
        sonosWsClient.disconnect(); 
    }

    public SonosDeviceInfo getDeviceInfoFromUdn(String udn) {
        return udnToDeviceInfo.get(udn);
    }
}
