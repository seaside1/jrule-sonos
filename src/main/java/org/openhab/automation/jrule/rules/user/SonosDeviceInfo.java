package org.openhab.automation.jrule.rules.user;

public class SonosDeviceInfo {

    private static final String JSAS = "Jsas ";
    private final String ip;
    private final String uriItemName;
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
        volumeItemName = getVolumeItemNameFromUdn(udn);
        cancelAudioClipName = getCanelAudioClipItemNameFromUdn(udn);
    }
    
  
     
    private String getCanelAudioClipItemNameFromUdn(String udn) {
        return "Sonos_" + udn + "_cancelAudioClip";
    }

    public String getUriItemNameFromUdn(String udn) {
        return "Sonos_" + udn + "_audioClipUri";
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

    public String getCancelAudioClipName() {
        return cancelAudioClipName;
    }
    
    @Override
    public String toString() {
        return "SonosDeviceInfo [ip=" + ip + ", uriItemName=" + uriItemName + ", volumeItemName=" + volumeItemName
                + ", cancelAudioClipName=" + cancelAudioClipName + ", udn=" + udn + ", label=" + label + ", lastAudioClipId="
                + lastAudioClipId + "]";
    }
}
