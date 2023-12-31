package org.openhab.automation.jrule.rules.user;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.automation.jrule.internal.engine.JRuleEngine;
import org.openhab.automation.jrule.internal.handler.JRuleHandler;
import org.openhab.automation.jrule.internal.handler.JRuleItemHandler;
import org.openhab.automation.jrule.internal.handler.JRuleThingHandler;
import org.openhab.automation.jrule.internal.handler.JRuleTransformationHandler;
import org.openhab.automation.jrule.internal.handler.JRuleVoiceHandler;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.thing.Thing;
import org.osgi.framework.ServiceRegistration;

public class SonosRuleModule extends JRule {

    private static final String RULE_METHOD_NAME = "fireAudioClip";
    private static final int DEFAULT_VOLUME = 35;
    private final Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();

    public SonosRuleModule() {
        fetchSonosThingAttributes();
        registerItems();
        registerRules();
        registerAudioSinks();
    }

    private void registerAudioSinks() {
        SonosCoordinator.get().getDeviceInfos().forEach(deviceInfo -> registerAudioSink(deviceInfo));
   }

    private void registerAudioSink(SonosDeviceInfo deviceInfo) {
        JRuleTransformationHandler.get().getBundleContext()
        .registerService(AudioSink.class.getName(), new SonosAudioClipAudioSink(deviceInfo, JRuleVoiceHandler.get().getNetworkAddressService()), new Hashtable<>());
        logInfo("Registering Sonos Audio sink for overlay: {} {}", deviceInfo.getAudioSinkName(), deviceInfo.getLabel());
    }

    private void registerRules() {
        logInfo("Registering Sonos Dynamic JRules");
        SonosAudioClipRules jRule = new SonosAudioClipRules();
        Method ruleMethod = null;
        Optional<Method> methodOpt = Arrays.stream(jRule.getClass().getDeclaredMethods())
                .filter(m -> m.getName().equals(RULE_METHOD_NAME)).findAny();
        if (methodOpt.isEmpty()) {
            logError("Failed to find method: {}", RULE_METHOD_NAME);
            return;
        }
        ruleMethod = methodOpt.get();
        JRuleEngine.get().addDynamicWhenReceivedCommand(ruleMethod, jRule, "SonosFireAudioClip",
                SonosCoordinator.get().getUriItemNames());
    }

    private void registerItems() {
        SonosCoordinator.get().getDeviceInfos().forEach(deviceInfo -> registerItem(deviceInfo));
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
        thingHandler.getThings().stream().filter(t -> t.getUID() != null && t.getThingTypeUID().toString().startsWith("sonos:"))
                .forEach(t -> fetchThingAttributes(t));
    }

    private void fetchThingAttributes(Thing t) {
        final String ip = getIp(t);
        final String udn = getUdn(t);
        if (ip == null || udn == null) {
            logWarn("Failed to get IP for Thing: {} may attempt later to get ip", t.getLabel());
            return;
        }
        SonosDeviceInfo deviceInfo = new SonosDeviceInfo(ip, udn, t.getLabel());
        SonosCoordinator.get().addDeviInfo(deviceInfo);
    }

    private String getUdn(Thing thing) {
        return thing.getConfiguration().get("udn").toString();
    }

    private String getIp(Thing thing) {
        return thing.getProperties().get("ipAddress");
    }
}
