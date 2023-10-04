package org.openhab.automation.jrule.rules.user;

import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.openhab.automation.jrule.rules.value.JRuleValue;

public class SonosAudioClipRules extends JRule {

    public SonosAudioClipRules() {
        super(false);
    }
    
    public void fireAudioClip(JRuleItemEvent event) {
        JRuleValue state = event.getItem().getState();
        logInfo("Executing myMethod event item {} value: {}", event.getItem().getName(), state.toString());
    }
}
