package org.openhab.automation.jrule.rules.user;

import org.openhab.automation.jrule.items.JRuleNumberItem;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;

public class SonosAudioClipRules extends JRule {

    private static final String DEFAULT_VOLUME = "35";

    public SonosAudioClipRules() {
        super(false);
    }
    
    public void fireAudioClip(JRuleItemEvent event) {
        final String stateValue = event.getState() == null ? "" : event.getState().toString();
        logInfo("Executing fireAudioClip event item {} value: {}", event.getItem().getName(), stateValue);
        final String ip = SonosCoordinator.get().getIpFromUriItem(event.getItem().getName());
        if (ip == null) {
            logInfo("Failed to find ip for item, cannot play clip. Item: {}", event.getItem().getName());
            return;
        }
        final SonosDeviceInfo deviceInfo = SonosCoordinator.get().getDeviceInfo(event.getItem().getName());
        final JRuleNumberItem volumeItem = JRuleNumberItem.forName(deviceInfo.getVolumeItemName());
        String volumeStateString = volumeItem != null && volumeItem.getState() != null ? volumeItem.getState().stringValue() : null;
        String volume = volumeStateString == null || volumeStateString.isEmpty() ? DEFAULT_VOLUME : volumeStateString;
        String uri = event.getState().stringValue();
        String udn = deviceInfo.getUdn();
        logInfo("Sending Play Clip ip: {} uri: {} udn: {} volume: {}", ip, uri, udn, volume);
        SonosCoordinator.get().playAudioClip(deviceInfo.getIp(), uri, udn, volume);
    }
    
}
