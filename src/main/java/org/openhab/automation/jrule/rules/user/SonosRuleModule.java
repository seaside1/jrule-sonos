package org.openhab.automation.jrule.rules.user;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.internal.engine.JRuleEngine;
import org.openhab.automation.jrule.internal.handler.JRuleItemHandler;
import org.openhab.automation.jrule.internal.handler.JRuleThingHandler;
import org.openhab.automation.jrule.rules.event.JRuleItemEvent;
import org.openhab.automation.jrule.rules.value.JRuleValue;
import org.openhab.core.thing.Thing;

public class SonosRuleModule extends JRule {

    private static final String RULE_METHOD_NAME = "fireAudioClip";
    private static final int DEFAULT_VOLUME = 35;
    private List<String> sonosIp = new ArrayList<>();
    private List<String> sonosUriItemNames = new ArrayList<>();
    
    public SonosRuleModule() {
        fetchSonosDeviceIps();
        registerItems();
        registerRules();
    }
    
    private void registerRules() {
        logInfo("1Register rules");
        //JRuleCondition jRuleCondition = new JRuleWhenCojRuleWhen.condition();
        SonosAudioClipRules jRule = new SonosAudioClipRules();
        logInfo("2Register rules");
        Method ruleMethod = null;
        try {
            logInfo("2.1Register rules" + jRule.getClass());
            Optional<Method> methodOpt = Arrays.stream(jRule.getClass().getDeclaredMethods()).filter(m->m.getName().equals(RULE_METHOD_NAME)).findAny();
            if (methodOpt.isEmpty()) {
                logError("Failed to find method: {}", RULE_METHOD_NAME);
                return;
            }
            ruleMethod = methodOpt.get();
            logInfo("3Register rules");
            if (ruleMethod == null) {
                Method[] declaredMethods = jRule.getClass().getDeclaredMethods();
                if (declaredMethods == null) {
                    
                    logError("Declared Methods all null");
                }
                Arrays.stream(declaredMethods).forEach(m->logInfo("M Name: {}", m.getName()));
            }
        } catch (Exception x) {
            // TODO Auto-generated catch block
            x.printStackTrace();
        }
        JRuleEngine.get().addDynamicWhenReceivedCommand(ruleMethod, jRule, "SonosFireAudioClip", sonosUriItemNames);

        
        //).filter(method -> method.getName().startsWith("myRule"));
        //SonosLanRules.get
//        
//        JRuleItemReceivedUpdateExecutionContext context = new JRuleItemReceivedUpdateExecutionContext(jRule,
//                "CoolRule", loggingTags, method, jRuleWhen.item(), jRuleWhen.memberOf(),
//                Optional.of(new JRuleItemExecutionContext.JRuleConditionContext(jRuleCondition)),
//                jRulePreconditionContexts, Optional.of(jRuleWhen.state()).filter(StringUtils::isNotEmpty),
//                timedLock, delayed);
//        addToContext(context, enableRule);
//        ruleLoadingStatistics.addItemStateTrigger();
//        ruleModuleEntry.addJRuleWhenItemReceivedUpdate(context);
//        addedToContext.set(true);
//        
        // TODO Auto-generated method stub
    
        
    }
    

    public void myMethod(JRuleItemEvent event) {
        JRuleValue state = event.getItem().getState();
        logInfo("Executing myMethod");
    }
    
    private void registerItems() {
        sonosIp.forEach(ip->registerItem(ip));
    }
    
    private void registerItem(String ip) {
        String uriName = getUriNameFromIp(ip);
        JRuleItemHandler jRuleItemHandler = JRuleItemHandler.get();
        if (!jRuleItemHandler.itemRegistryContainsItem(uriName)) {
            jRuleItemHandler.addStringItem(uriName, "");
        }
        String volumeName = getVolumeNameFromIp(ip);
        if (!jRuleItemHandler.itemRegistryContainsItem(volumeName)) {
            jRuleItemHandler.addNumberItem(volumeName, DEFAULT_VOLUME);
        }
    }
    
    private String getVolumeNameFromIp(String ip) {
        return "Sonos_" + ip.replaceAll("\\.", "") + "_volume";
    }

    private String getUriNameFromIp(String ip) {
        return "Sonos_" + ip.replaceAll("\\.", "") + "_audioClipUri";
    }
    
    @Override
    public String getRuleLogName() {
        return "SonosLan";
    }
    public void fetchSonosDeviceIps() {
        JRuleThingHandler thingHandler = JRuleThingHandler.get();
      thingHandler.getThings().stream().filter(t->t.getUID() != null && t.getThingTypeUID().toString().startsWith("sonos:")).forEach(t->addIp(t));
    }
    
    private void addIp(Thing thing) {
      String ip = thing.getProperties().get("ipAddress");
      sonosIp.add(ip);
      sonosUriItemNames.add(getUriNameFromIp(ip));
      
    }
}
