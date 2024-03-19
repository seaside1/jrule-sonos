package org.openhab.automation.jrule.rules.user;

import org.openhab.automation.jrule.internal.engine.JRuleInvocationCallback;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;

public class SonosAudioClipRule extends JRule implements JRuleInvocationCallback {
    
    @Override
    public void accept(JRuleEvent rawEvent) {
        final JRuleItemEvent event = (JRuleItemEvent) rawEvent;
        final String stateValue = event.getState() == null ? "" : event.getState().toString();
        logInfo("Executing fireAudioClip event item {} value: {}", event.getItem().getName(), stateValue);
        final String ip = SonosCoordinator.get().getIpFromItem(event.getItem().getName());
        if (ip == null) {
            logInfo("Failed to find ip for item, cannot play clip. Item: {}", event.getItem().getName());
            return;
        }
        final SonosDeviceInfo deviceInfo = SonosCoordinator.get().getDeviceInfoFromItem(event.getItem().getName());
        String uri = event.getState().stringValue();
        String udn = deviceInfo.getUdn();
        final String volume = SonosCoordinator.get().getVolume(deviceInfo);
        final Boolean led = SonosCoordinator.get().getLed(deviceInfo);
        logInfo("Sending Play Clip ip: {} uri: {} udn: {} volume: {} led: {}", ip, uri, udn, volume, led);
        SonosCoordinator.get().playAudioClip(deviceInfo, uri, volume, led);
    }

    @Override
    public String getRuleLogName() {
        return "SonosPlayAudioClip";
    }
}
