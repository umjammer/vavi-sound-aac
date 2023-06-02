[![Release](https://jitpack.io/v/umjammer/vavi-sound-aac.svg)](https://jitpack.io/#umjammer/vavi-sound-aac)
[![Java CI with Maven](https://github.com/umjammer/vavi-sound-aac/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-sound-aac/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-sound-aac/actions/workflows/codeql.yml/badge.svg)](https://github.com/umjammer/vavi-sound-aac/actions/workflows/codeql.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--sound--sandbox-pink)](https://github.com/umjammer/vavi-sound-sandbox)

# vavi-sound-aac

Pure Java AAC decoder (Java Sound SPI)

<img src="https://github.com/umjammer/vavi-image-avif/assets/493908/58a132fd-ba3d-4309-9481-2b86fc885f14" width="160" alt="AAC logo"/><sub><a href="https://www.dolby.com/">© Dolby Laboratories, Inc.</a></sub>

## Install

* https://jitpack.io/#/umjammer/vavi-sound-aac

## Usage

```java
    AudioInputStream ais = AudioSystem.getAudioInputStream(Files.newInputStream(Paths.get(m4a)));
    Clip clip = AudioSystem.getClip();
    clip.open(AudioSystem.getAudioInputStream(new AudioFormat(44100, 16, 2, true, false), ais));
    clip.loop(Clip.LOOP_CONTINUOUSLY);
```

## TODO

 * ~~rename project into vavi-sound-aac~~
 * patches
   * ~~https://github.com/Tianscar/jaac/commit/cf9b24f55fcd8f77ae76c42cac87602fdb0382a8~~
   * ~~https://github.com/Tianscar/jaac/commit/bbaaec277d6620e0233561d02185f2e901970480~~

---

## Original

# JAADec

**This is a fork of https://sourceforge.net/projects/vavi-sound-aac/ 
containing fixes to make it play nice with other Java Sound Providers.**

The original project was licensed under Public Domain
and as such this fork is also licensed under Public Domain. Use as you like!

JAAD is an AAC decoder and MP4 demultiplexer library written completely in Java.
It uses no native libraries, is platform-independent and portable.
It can read MP4 container from almost every input-stream (files, network sockets etc.)
and decode AAC-LC (Low Complexity) and HE-AAC (High Efficiency/AAC+).
