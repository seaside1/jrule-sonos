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
- JRule with Dynamic rules support BETA21 or later (https://github.com/seaside1/jrule/)
- Proper network setup: Static ip numbers for Sonos speakers or DHCP ip reservations. Able to connect to the Sonos Speakers on port 1443/tcp from the openHAB instance.

## Installation
- Install JRule by using openHAB Marketplace or by copying Jrule-jar https://github.com/seaside1/jrule/releases/download/jrule-4.0.0-BETA21/org.openhab.automation.jrule-4.0.0-BETA21.jar
 to your openHAB addons folder https://www.openhab.org/docs/configuration/addons.html#through-manually-provided-add-ons
- Copy jrule-sonos.jar to $OPENHAB_BASE_DIR/conf/automation/jrule/rules-jar/

## Details
This addon will search of all sonos things in openHAB. Once the things have been located the ip-number and udn (Unique Device Number) for that thing will be read.
JRule will then create three items per IP/UDN combo: An UriItem, a cancelItem, a volumeItem and a LedItem
JRule will generate rules, which will trigger when an uri item is changed it will attempt to play that uri as a AudioClip on the local websocket using the volume item and LedItem (If led is on the Sonos speaker will start flashing when playing the audioClip)
JRule will also register audio sinks for all speakers. Enabling you to use openHAB "say()" and using the overlay functionality with the sonos speaker. The audioSinks are also looking at the ledItem to determine if they should flash when playing the audioClip.

## Example
These are generated from my system with speakers with Ip 10.0.40.xx
![Screenshot from 2024-04-08 21-49-53](https://github.com/seaside1/jrule-sonos/assets/24649305/48a157e3-c183-4f1f-aa83-2ea4035ee5d4)

![Screenshot from 2024-04-08 21-52-11](https://github.com/seaside1/jrule-sonos/assets/24649305/3496dcf2-b0d7-4118-b4dd-5deec6b03683)



```
openhab> openhab:send Items

Sonos_RINCON_000E5877C45801400_audioClipUri https://freetestdata.com/wp-content/uploads/2021/09/Free_Test_Data_100KB_MP3.mp3                                                                                                             
Command has been sent successfully.
```

```
2024-04-08 21:54:26.695 [INFO ] [openhab.automation.jrule.rules.JRule] - [SonosPlayAudioClip] Executing fireAudioClip event item Sonos_RINCON_949FXXXXXXX_audioClipUri value: https://freetestdata.com/wp-content/uploads/2021/09/Free_Test_Data_100KB_MP3.mp3
2024-04-08 21:54:26.696 [INFO ] [openhab.automation.jrule.rules.JRule] - [SonosPlayAudioClip] Sending Play Clip ip: 10.0.40.69 uri: https://freetestdata.com/wp-content/uploads/2021/09/Free_Test_Data_100KB_MP3.mp3 udn: RINCON_949F3EC0234F01400 volume: 35 led: false
2024-04-08 21:54:26.851 [INFO ] [utomation.jrule.sonos.SonosWebSocket] - Got connect: WebSocketSession[websocket=JettyAnnotatedEventDriver[org.openhab.automation.jrule.sonos.SonosWebSocket@6c3396d2],behavior=CLIENT,connection=WebSocketClientConnection@5e7dd118::DecryptedEndPoint@68a00add{l=/10.0.40.30:43344,r=/10.0.40.69:1443,OPEN,fill=-,flush=-,to=3/300000},remote=WebSocketRemoteEndpoint@69b7bd52[batching=true],incoming=JettyAnnotatedEventDriver[org.openhab.automation.jrule.sonos.SonosWebSocket@6c3396d2],outgoing=ExtensionStack[queueSize=0,extensions=[],incoming=org.eclipse.jetty.websocket.common.WebSocketSession,outgoing=org.eclipse.jetty.websocket.client.io.WebSocketClientConnection]]
2024-04-08 21:54:26.852 [INFO ] [utomation.jrule.sonos.SonosWebSocket] - Connection status code: 101 reason: Switching Protocols
2024-04-08 21:54:27.154 [INFO ] [utomation.jrule.sonos.SonosWebSocket] - Connection closed: 1000 - null
2024-0
```

## Audio Sink
```
2024-03-26 16:44:08.159 [INFO ] [openhab.automation.jrule.rules.JRule] - [SonosLan] Registering Sonos Audio sink for overlay: jsas:RINCON_000XXXXXXXXX01400 Jsas Sonos Play:3 (MyRoom)
```

```
  say(message, "myvoiceId", "jsas:RINCON_C438XXXXXXXXXXXXXX", 60);
```


# Karaf
```
openhab> openhab:audio sinks
  Sonos Move 2 (Move 2) (jsas:RINCON_XXXXXXXXXXXXXXXXXX)
  Sonos Move 2 (Move 2) (sonos:zoneplayer:RINCON_XXXXXXXXXXXXXXXXXXXX)
  System Speaker (enhancedjavasound)
  Web Audio (webaudio)
openhab>
```
