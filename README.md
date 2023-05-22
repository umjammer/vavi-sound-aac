[![Release](https://jitpack.io/v/umjammer/JAADec.svg)](https://jitpack.io/#umjammer/JAADec)
[![Java CI with Maven](https://github.com/umjammer/JAADec/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/JAADec/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/JAADec/actions/workflows/codeql.yml/badge.svg)](https://github.com/umjammer/JAADec/actions/workflows/codeql.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--sound--sandbox-pink)](https://github.com/umjammer/vavi-sound-sandbox)

# JAADec

**This is a fork of https://sourceforge.net/projects/jaadec/ 
containing fixes to make it play nice with other Java Sound Providers.**

The original project was licensed under Public Domain
and as such this fork is also licensed under Public Domain. Use as you like!

JAAD is an AAC decoder and MP4 demultiplexer library written completely in Java.
It uses no native libraries, is platform-independent and portable.
It can read MP4 container from almost every input-stream (files, network sockets etc.)
and decode AAC-LC (Low Complexity) and HE-AAC (High Efficiency/AAC+).

## Install

 * https://jitpack.io/#/umjammer/JAADec

## Applied Patches

 * https://github.com/Tianscar/jaac/commit/cf9b24f55fcd8f77ae76c42cac87602fdb0382a8
 * https://github.com/Tianscar/jaac/commit/bbaaec277d6620e0233561d02185f2e901970480
