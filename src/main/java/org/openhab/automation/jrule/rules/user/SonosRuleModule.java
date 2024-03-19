package org.openhab.automation.jrule.rules.user;

import java.util.Hashtable;

import org.openhab.automation.jrule.internal.engine.JRuleBuilder;
import org.openhab.automation.jrule.internal.engine.JRuleEngine;
import org.openhab.automation.jrule.internal.handler.JRuleItemHandler;
import org.openhab.automation.jrule.internal.handler.JRuleThingHandler;
import org.openhab.automation.jrule.internal.handler.JRuleTransformationHandler;
import org.openhab.automation.jrule.internal.handler.JRuleVoiceHandler;
import org.openhab.automation.jrule.rules.JRule;
import org.openhab.automation.jrule.rules.JRuleMemberOf;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.thing.Thing;

public class SonosRuleModule extends JRule {

    private static final Double DEFAULT_VOLUME = 35.0;
    private final SonosAudioClipRule sonosAudioClipRule;
    
    private final SonosCancelAudioClipRule sonosCancelAudioClipRule;

    public SonosRuleModule() {
        sonosAudioClipRule = new SonosAudioClipRule();   
        sonosCancelAudioClipRule = new SonosCancelAudioClipRule();
        fetchSonosThingAttributes();
        registerItems();
        registerRules();
        registerAudioSinks();
    }

    private void registerAudioSinks() {
        SonosCoordinator.get().getDeviceInfos().forEach(deviceInfo -> registerAudioSink(deviceInfo));
    }

    private void registerAudioSink(SonosDeviceInfo deviceInfo) {
        JRuleTransformationHandler.get().getBundleContext().registerService(AudioSink.class.getName(),
                new SonosAudioClipAudioSink(deviceInfo, JRuleVoiceHandler.get().getNetworkAddressService()), new Hashtable<>());
        logInfo("Registering Sonos Audio sink for overlay: {} {}", deviceInfo.getAudioSinkName(), deviceInfo.getLabel());
    }

    private void registerRules() {
        logInfo("Registering Sonos Dynamic JRules");
        final JRuleBuilder builderAudioClip = JRuleEngine.get().createJRuleBuilder("SonosFireAudioClip", sonosAudioClipRule);
        SonosCoordinator.get().getDeviceInfos().forEach(deviceInfo -> builderAudioClip
                .whenItemReceivedCommand(deviceInfo.getUriItemName(), JRuleMemberOf.None, null, null));
        builderAudioClip.build();
        
        final JRuleBuilder builderCancelAudioClip = JRuleEngine.get().createJRuleBuilder("SonosCancelAudioClip",
                sonosCancelAudioClipRule);
        SonosCoordinator.get().getDeviceInfos().forEach(deviceInfo -> builderCancelAudioClip
                .whenItemReceivedCommand(deviceInfo.getCancelAudioClipName(), JRuleMemberOf.None, null, null));
        builderCancelAudioClip.build();

    }

    private void registerItems() {
        SonosCoordinator.get().getDeviceInfos().forEach(deviceInfo -> registerItem(deviceInfo));
    }

    private void registerItem(SonosDeviceInfo deviceInfo) {
        JRuleItemHandler jRuleItemHandler = JRuleItemHandler.get();
        if (!jRuleItemHandler.itemRegistryContainsItem(deviceInfo.getUriItemName())) {
            jRuleItemHandler.addStringItem(deviceInfo.getUriItemName(), "");
        }
        if (!jRuleItemHandler.itemRegistryContainsItem(deviceInfo.getledItemName())) {
            jRuleItemHandler.addSwitchItem(deviceInfo.getledItemName(), Boolean.FALSE);
        }
        if (!jRuleItemHandler.itemRegistryContainsItem(deviceInfo.getVolumeItemName())) {
            jRuleItemHandler.addNumberItem(deviceInfo.getVolumeItemName(), DEFAULT_VOLUME);
        }       
        if (!jRuleItemHandler.itemRegistryContainsItem(deviceInfo.getCancelAudioClipName())) {
            jRuleItemHandler.addSwitchItem(deviceInfo.getCancelAudioClipName());
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
