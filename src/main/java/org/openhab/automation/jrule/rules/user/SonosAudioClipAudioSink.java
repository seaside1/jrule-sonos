package org.openhab.automation.jrule.rules.user;

/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.jrule.internal.handler.JRuleTransformationHandler;
import org.openhab.automation.jrule.internal.handler.JRuleVoiceHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSinkSync;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FileAudioStream;
import org.openhab.core.audio.StreamServed;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.audio.utils.AudioStreamUtils;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SonosAudioClipAudioSink extends AudioSinkSync {

    private final Logger logger = LoggerFactory.getLogger(SonosAudioClipAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = Set.of(AudioFormat.MP3, AudioFormat.WAV);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = Set.of(AudioStream.class);

    private String volume = null;
    private final String baseUrl;
    private final SonosDeviceInfo deviceInfo;
   
    public SonosAudioClipAudioSink(SonosDeviceInfo deviceInfo, NetworkAddressService networkAddressService) {
        this.deviceInfo = deviceInfo;
        baseUrl = createBaseUrl(networkAddressService.getPrimaryIpv4HostAddress());
    }

    private String createBaseUrl(String ipAddress) {
        if (ipAddress == null) {
            logger.warn("No network interface could be found.");
            return null;
        }

        // we do not use SSL as it can cause certificate validation issues.
        final int port = HttpServiceUtil.getHttpServicePort(JRuleTransformationHandler.get().getBundleContext());
        if (port == -1) {
            logger.warn("Cannot find port of the http service.");
            return null;
        }
        return "http://" + ipAddress + ":" + port;
    }

    @Override
    public String getId() {
        return deviceInfo.getAudioSinkName();
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        return deviceInfo.getLabel();
    }

    @Override
    public CompletableFuture<@Nullable Void> processAndComplete(@Nullable AudioStream audioStream) {
        if (audioStream instanceof URLAudioStream) {
            // Asynchronous handling for URLAudioStream
            CompletableFuture<@Nullable Void> completableFuture = new CompletableFuture<@Nullable Void>();
            try {
                processAsynchronously(audioStream);
            } catch (UnsupportedAudioFormatException | UnsupportedAudioStreamException e) {
                completableFuture.completeExceptionally(e);
            }
            return completableFuture;
        } else {
            return super.processAndComplete(audioStream);
        }
    }

    @Override
    public void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream instanceof URLAudioStream) {
            processAsynchronously(audioStream);
        } else {
            processSynchronously(audioStream);
        }
    }

    private void processAsynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream instanceof URLAudioStream urlAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            SonosCoordinator.get().playAudioClip(deviceInfo, urlAudioStream.getURL(), volume, null);
            try {
                audioStream.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void processSynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream instanceof URLAudioStream) {
            return;
        }

        if (audioStream == null) {
            // in case the audioStream is null, this should be interpreted as a request to
            // end any currently playing
            // stream.
            logger.trace("Stop currently playing stream.");
            // handler.stopPlaying(OnOffType.ON);
            SonosCoordinator.get().cancelLastAudioClip(deviceInfo);
            return;
        }

        // we serve it on our own HTTP server and treat it as a notification
        // Note that Sonos does multiple concurrent requests to the AudioServlet,
        // so a one time serving won't work.
        StreamServed streamServed;
        try {
            streamServed = JRuleVoiceHandler.get().getAudioHTTPServer().serve(audioStream, 10, true);
        } catch (IOException e) {
            try {
                audioStream.close();
            } catch (IOException ex) {
            }
            throw new UnsupportedAudioStreamException("Sonos was not able to handle the audio stream (cache on disk failed).",
                    audioStream.getClass(), e);
        }
        final String url = baseUrl + streamServed.url();
        AudioFormat format = audioStream.getFormat();
        if (AudioFormat.WAV.isCompatible(format)) {
            SonosCoordinator.get().playAudioClip(deviceInfo,
                    url + AudioStreamUtils.EXTENSION_SEPARATOR + FileAudioStream.WAV_EXTENSION, volume, null);
        } else if (AudioFormat.MP3.isCompatible(format)) {
            SonosCoordinator.get().playAudioClip(deviceInfo,
                    url + AudioStreamUtils.EXTENSION_SEPARATOR + FileAudioStream.MP3_EXTENSION, volume, null);
        } else {
            throw new UnsupportedAudioFormatException("Sonos only supports MP3 or WAV.", format);
        }

    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_AUDIO_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_AUDIO_STREAMS;
    }

    @Override
    public PercentType getVolume() {
        return volume == null ? new PercentType(SonosCoordinator.get().getVolume(deviceInfo)) : new PercentType(volume);
    }

    @Override
    public void setVolume(PercentType volume) {
        this.volume = "" + volume.longValue();
    }

//    public OnOffType isLed() {
//        return led == Boolean.TRUE ? OnOffType.ON : OnOffType.OFF;
//    }
//
//    public void setLed(OnOffType led) {
//        this.led = led == OnOffType.ON;
//    }
}
