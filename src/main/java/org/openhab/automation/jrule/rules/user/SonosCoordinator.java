package org.openhab.automation.jrule.rules.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.automation.jrule.items.JRuleNumberItem;
import org.openhab.automation.jrule.sonos.SonosWsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SonosCoordinator {
    private static final String UNDERSCORE="_";

    private static final String SONOS = "Sonos_";

    private final Logger logger = LoggerFactory.getLogger(SonosCoordinator.class);

    private static SonosCoordinator instance = null;
   
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
        final int start = itemName.indexOf(SONOS) + SONOS.length();
        final int end = itemName.lastIndexOf(UNDERSCORE);
        return (start >= 0 && end >= 0 && end > start) ? itemName.substring(start, end) : null;
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
        final SonosDeviceInfo sonosDeviceInfo = udnToDeviceInfo.get(extractSonosId(itemName));
        if (sonosDeviceInfo == null) {       
            logger.error("Failed to get sonosDeviceInfo for itemName: {} extracedId: {}", itemName, extractSonosId(itemName));
        }
        if (sonosDeviceInfo.getIp() == null || sonosDeviceInfo.getIp().length() < 2) {
           logger.error("Failed to get Ip from sonosDevce Info itemName: {} deviceInfo: {}", itemName, sonosDeviceInfo);
        }
        return sonosDeviceInfo != null ? sonosDeviceInfo.getIp() : null; 
                
    }

    public void addDeviInfo(SonosDeviceInfo deviceInfo) {
        logger.debug("Adding deviceInfo: {}", deviceInfo);
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
