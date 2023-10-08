package org.openhab.automation.jrule.rules.user;

public class SonosDeviceInfo {

    private final String ip;
    private final String uriItemName;
    private final String volumeItemName;
    private final String udn;

    public SonosDeviceInfo(String ip, String udn) {
        this.ip = ip;
        this.udn = udn;
        uriItemName = getUriItemNameFromIp(ip);
        volumeItemName = getVolumeItemNameFromIp(ip);
    }
    
    public String getUriItemNameFromIp(String ip) {
        return "Sonos_" + ip.replaceAll("\\.", "") + "_audioClipUri";
    }
    
    private String getVolumeItemNameFromIp(String ip) {
        return "Sonos_" + ip.replaceAll("\\.", "") + "_volume";
    }
    
    public String getIp() {
        return ip;
    }
    public String getUriItemName() {
        return uriItemName;
    }
    public String getUdn() {
        return udn;
    }

    public String getVolumeItemName() {
        return volumeItemName;
    }
}
