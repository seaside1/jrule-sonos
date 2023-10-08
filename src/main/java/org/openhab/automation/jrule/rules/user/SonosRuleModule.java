package org.openhab.automation.jrule.rules.user;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import org.openhab.automation.jrule.internal.engine.JRuleEngine;
import org.openhab.automation.jrule.internal.handler.JRuleItemHandler;
import org.openhab.automation.jrule.internal.handler.JRuleThingHandler;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.core.thing.Thing;

public class SonosRuleModule extends JRule {

    private static final String RULE_METHOD_NAME = "fireAudioClip";
    private static final int DEFAULT_VOLUME = 35;
  
    
    public SonosRuleModule() {
        fetchSonosThingAttributes();
        registerItems();
        registerRules();
    }

    private void registerRules() {
        logInfo("Registering Sonos Dynamic JRules");
        SonosAudioClipRules jRule = new SonosAudioClipRules();
        Method ruleMethod = null;
        Optional<Method> methodOpt = Arrays.stream(jRule.getClass().getDeclaredMethods()).filter(m->m.getName().equals(RULE_METHOD_NAME)).findAny();
        if (methodOpt.isEmpty()) {
            logError("Failed to find method: {}", RULE_METHOD_NAME);
            return;
        }
        ruleMethod = methodOpt.get();
        JRuleEngine.get().addDynamicWhenReceivedCommand(ruleMethod, jRule, "SonosFireAudioClip", SonosCoordinator.get().getUriItemNames());
    }
    
    private void registerItems() {
        SonosCoordinator.get().getDeviceInfos().forEach(deviceInfo->registerItem(deviceInfo));
    }
    
    private void registerItem(SonosDeviceInfo deviceInfo) {
        JRuleItemHandler jRuleItemHandler = JRuleItemHandler.get();
        if (!jRuleItemHandler.itemRegistryContainsItem(deviceInfo.getUriItemName())) {
            jRuleItemHandler.addStringItem(deviceInfo.getUriItemName(), "");
        }
         if (!jRuleItemHandler.itemRegistryContainsItem(deviceInfo.getVolumeItemName())) {
            jRuleItemHandler.addNumberItem(deviceInfo.getVolumeItemName(), DEFAULT_VOLUME);
        }
    }
    
    @Override
    public String getRuleLogName() {
        return "SonosLan";
    }
    public void fetchSonosThingAttributes() {
        JRuleThingHandler thingHandler = JRuleThingHandler.get();
      thingHandler.getThings().stream().filter(t->t.getUID() != null && t.getThingTypeUID().toString().startsWith("sonos:")).forEach(t->fetchThingAttributes(t));
    }
    
    private void fetchThingAttributes(Thing t) {
        final String ip = getIp(t);
        final String udn = getUdn(t);
        if (ip == null || udn == null) {
            logWarn("Failed to get IP for Thing: {} may attempt later to get ip", t.getLabel());
            return;
        }
        SonosDeviceInfo deviceInfo = new SonosDeviceInfo(ip, udn);
        SonosCoordinator.get().addDeviInfo(deviceInfo);
    }

    private String getUdn(Thing thing) {
        return thing.getConfiguration().get("udn").toString();
    }

    private String getIp(Thing thing) {
      return thing.getProperties().get("ipAddress");
    }
}
