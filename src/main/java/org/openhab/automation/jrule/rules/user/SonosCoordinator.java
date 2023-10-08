package org.openhab.automation.jrule.rules.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.automation.jrule.sonos.SonosWsClient;

public class SonosCoordinator {

    private static SonosCoordinator instance = null;

    private Map<String, SonosDeviceInfo> uriItemNameToDeviceInfo = new HashMap<>();
    private final SonosWsClient sonosWsClient;

    private SonosCoordinator() {
        sonosWsClient = new SonosWsClient();
    }

    public Collection<SonosDeviceInfo> getDeviceInfos() {
        return uriItemNameToDeviceInfo.values();
    }

    public static SonosCoordinator get() {
        if (instance == null) {
            instance = new SonosCoordinator();
        }
        return instance;
    }

    public String getIpFromUriItem(String itemName) {
        return uriItemNameToDeviceInfo.get(itemName).getIp();
    }

    public void addDeviInfo(SonosDeviceInfo deviceInfo) {
        uriItemNameToDeviceInfo.put(deviceInfo.getUriItemName(), deviceInfo);
    }

    public List<String> getUriItemNames() {
        return uriItemNameToDeviceInfo.values().stream().map(d -> d.getUriItemName()).toList();
    }

    public synchronized void playAudioClip(String ip, String uri, String udn, String volume) {
        sonosWsClient.connect(ip);
        sonosWsClient.sendAudioClip(udn, uri, volume);
        sonosWsClient.disconnect();
    }

    public SonosDeviceInfo getDeviceInfo(String uriItemName) {
        return uriItemNameToDeviceInfo.get(uriItemName);
    }
}
