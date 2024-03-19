package org.openhab.automation.jrule.rules.user;

public class SonosDeviceInfo {

    private static final String JSAS = "Jsas ";
    private final String ip;
    private final String uriItemName;
    private final String ledItemName;
    private final String volumeItemName;
    private final String cancelAudioClipName;
    private final String udn;
    private final String label;
    private volatile String lastAudioClipId = null;

    public SonosDeviceInfo(String ip, String udn, String label) {
        this.ip = ip;
        this.udn = udn;
        this.label = JSAS.concat(label);
        uriItemName = getUriItemNameFromUdn(udn);
        ledItemName = getLedItemNameFromUdn(udn);
        volumeItemName = getVolumeItemNameFromUdn(udn);
        cancelAudioClipName = getCanelAudioClipItemNameFromUdn(udn);
    }
     
    private String getCanelAudioClipItemNameFromUdn(String udn) {
        return "Sonos_" + udn + "_cancelAudioClip";
    }

    private String getUriItemNameFromUdn(String udn) {
        return "Sonos_" + udn + "_audioClipUri";
    }

    private String getLedItemNameFromUdn(String udn) {
        return "Sonos_" + udn + "_Led";
    }

    
    private String getVolumeItemNameFromUdn(String udn) {
        return "Sonos_" + udn + "_volume";
    }
    
    public String getIp() {
        return ip;
    }
    public String getUriItemName() {
        return uriItemName;
    }
    
    public String getAudioSinkName() {
        return "jsas:" + udn;
    }
    
    public String getUdn() {
        return udn;
    }

    public String getVolumeItemName() {
        return volumeItemName;
    }

    public String getLabel() {
        return label;
    }

    public String getLastAudioClipId() {
        return lastAudioClipId;
    }

    public void setLastAudioClipId(String lastAudioClipId) {
        this.lastAudioClipId = lastAudioClipId;
    }

    public String getledItemName() {
        return ledItemName;
    }

    public String getCancelAudioClipName() {
        return cancelAudioClipName;
    }
    
    @Override
    public String toString() {
        return "SonosDeviceInfo [ip=" + ip + ", uriItemName=" + uriItemName + ", ledItemName=" + ledItemName
                + ", volumeItemName=" + volumeItemName + ", cancelAudioClipName=" + cancelAudioClipName + ", udn=" + udn
                + ", label=" + label + ", lastAudioClipId=" + lastAudioClipId + "]";
    }
}
