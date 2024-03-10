package org.openhab.automation.jrule.rules.user;

import org.openhab.automation.jrule.internal.engine.JRuleInvocationCallback;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.event.JRuleEvent;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;

public class SonosAudioClipRules extends JRule implements JRuleInvocationCallback {

  
    
    public void fireAudioClip(JRuleItemEvent event) {
    }

    @Override
    public void accept(JRuleEvent event) {
        final String stateValue = ((JRuleItemEvent)event).getState() == null ? "" : ((JRuleItemEvent)event).getState().toString();
        logInfo("Executing fireAudioClip event item {} value: {}", ((JRuleItemEvent)event).getItem().getName(), stateValue);
        final String ip = SonosCoordinator.get().getIpFromUriItem(((JRuleItemEvent)event).getItem().getName());
        if (ip == null) {
            logInfo("Failed to find ip for item, cannot play clip. Item: {}", ((JRuleItemEvent)event).getItem().getName());
            return;
        }
        final SonosDeviceInfo deviceInfo = SonosCoordinator.get().getDeviceInfo(((JRuleItemEvent)event).getItem().getName());
        String uri = ((JRuleItemEvent)event).getState().stringValue();
        String udn = deviceInfo.getUdn();
        final String volume =  SonosCoordinator.get().getVolume(deviceInfo);
        logInfo("Sending Play Clip ip: {} uri: {} udn: {} volume: {}", ip, uri, udn, volume);
        SonosCoordinator.get().playAudioClip(deviceInfo, uri, volume);

    }
    
    @Override
    public String getRuleLogName() {
        return "fireAudioClip";
    }
    
}
