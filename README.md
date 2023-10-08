# jrule-sonos
JRule Sonos Integration. These rules are intented to do generate items and rules for 
running Sonos Audio Clip via a local Sonos websocket. Doing this will take advantage of overlay functionality, not interrupting what is currently
being played. Whatever is being played will be volume downed and the uri provided will be played on top

## Why
Automatically generate items and rules to provide easy access for the local sonos websocket api for playing audio clips.
Utilizing JRule for generating items and rules automatically.

## Pre-requisits
- Sonos Speakers with the Sonos S2 firmware (S1 is not compatible with the Sonos AudioClip websocket api)
- Sonos openHAB binding installed and set up https://www.openhab.org/addons/bindings/sonos/
- JRule with Dynamic rules support (Sharp version will be released later see PR: https://github.com/seaside1/jrule/pull/177

## Installation
- Copy pre-released Jrule-jar https://github.com/seaside1/jrule/releases/tag/jrule-4.x.x-BETA19-pre-release to your openHAB addons folder https://www.openhab.org/docs/configuration/addons.html#through-manually-provided-add-ons
- Copy jrule-sonos.jar to $OPENHAB_BASE_DIR/conf/automation/jrule/rules-jar/

## Details
This addon will search of all sonos things in openHAB. Once the things have been located the ip-number and udn (Unique Device Number) for that thing will be read.
JRule will then create Two items per IP/UDN combo: An UriItem and a volumeItem.
JRule will generate rules, which will trigger when an uri item is changed it will attempt to play that uri as a AudioClip on the local websocket using the volume item.

## Example
These are generated from my system with speakers with Ip 10.0.40.xx

![Screenshot from 2023-10-08 21-34-35](https://github.com/seaside1/jrule-sonos/assets/24649305/42e29e7f-5c7f-4d83-8255-2eab249dc0f7)

![Screenshot from 2023-10-08 21-33-29](https://github.com/seaside1/jrule-sonos/assets/24649305/161afbd7-d4e1-4f89-80cf-e17eaee12348)



```
openhab> openhab:send Sonos_1004068_audioClipUri https://freetestdata.com/wp-content/uploads/2021/09/Free_Test_Data_100KB_MP3.mp3                                                                                                             
Command has been sent successfully.
```

```
2023-10-08 21:29:42.247 [INFO ] [openhab.automation.jrule.rules.JRule] - [SonosFireAudioClip] Executing fireAudioClip event item Sonos_1004068_audioClipUri value: https://freetestdata.com/wp-content/uploads/2021/09/Free_Test_Data_100KB_MP3.mp3
2023-10-08 21:29:42.248 [INFO ] [openhab.automation.jrule.rules.JRule] - [SonosFireAudioClip] Sending Play Clip ip: 10.0.40.68 uri: https://freetestdata.com/wp-content/uploads/2021/09/Free_Test_Data_100KB_MP3.mp3 udn: RINCON_000E58F93C7401400 volume: 35
2023-10-08 21:29:42.274 [INFO ] [utomation.jrule.sonos.SonosWebSocket] - Got connect: WebSocketSession[websocket=JettyAnnotatedEventDriver[org.openhab.automation.jrule.sonos.SonosWebSocket@2ddf1f8],behavior=CLIENT,connection=WebSocketClientConnection@195aeb7c::DecryptedEndPoint@126b11e2{l=/10.0.40.30:43584,r=/10.0.40.68:1443,OPEN,fill=-,flush=-,to=6/300000},remote=WebSocketRemoteEndpoint@34613f0c[batching=true],incoming=JettyAnnotatedEventDriver[org.openhab.automation.jrule.sonos.SonosWebSocket@2ddf1f8],outgoing=ExtensionStack[queueSize=0,extensions=[],incoming=org.eclipse.jetty.websocket.common.WebSocketSession,outgoing=org.eclipse.jetty.websocket.client.io.WebSocketClientConnection]]
2023-10-08 21:29:42.274 [INFO ] [utomation.jrule.sonos.SonosWebSocket] - Connection status code: 101 reason: Switching Protocols
2023-10-08 21:29:42.297 [INFO ] [utomation.jrule.sonos.SonosWebSocket] - Connection closed: 1000 - null
```
