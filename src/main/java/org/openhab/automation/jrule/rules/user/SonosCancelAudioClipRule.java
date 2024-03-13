package org.openhab.automation.jrule.rules.user;

import org.openhab.automation.jrule.internal.engine.JRuleInvocationCallback;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.openhab.automation.jrule.rules.value.JRuleOnOffValue;

public class SonosCancelAudioClipRule extends JRule implements JRuleInvocationCallback {
    @Override
    public void accept(JRuleEvent rawEvent) {
        final JRuleItemEvent event = (JRuleItemEvent) rawEvent;
        if (event.getState() == JRuleOnOffValue.ON) {
            logInfo("Executing cancelAudioClip event item: {}", event.getItem().getName());
        }
        final SonosDeviceInfo deviceInfo = SonosCoordinator.get().getDeviceInfoFromItem(event.getItem().getName());
        final String ip = deviceInfo.getIp();
        if (ip == null) {
            logInfo("Failed to find ip for item, cannot play clip. Item: {}", event.getItem().getName());
            return;
        }
        String udn = deviceInfo.getUdn();
        String lastAudioClipId = deviceInfo.getLastAudioClipId();
        logInfo("Sending Cancel Audio Clip ip: {} udn: {} id: {}", ip, udn, lastAudioClipId);
        SonosCoordinator.get().cancelLastAudioClip(ip, udn, lastAudioClipId);
    }
    @Override
    public String getRuleLogName() {
        return "SonosCancelAudioClip";
    }
}