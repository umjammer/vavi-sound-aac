/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.sourceforge.jaad.spi.javasound;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vavi.sound.SoundUtil.volume;
import static vavix.util.DelayedWorker.later;

class AacFormatConversionProviderTest {

    static final String inFile1 = "/test.aac";
    static final String inFile = "/test.mid";
    static final String inFile2 = "/test.m4a";
    static final String inFile4 = "/alac.m4a";
    static final String inFile3 = "/test.caf";

    static long time;

    @BeforeAll
    static void setup() throws Exception {
        time = System.getProperty("vavi.test", "").equals("ide") ? 10 * 1000 : 1000 * 1000;
    }

    @Test
    @DisplayName("unsupported exception is able to detect in 3 ways")
    public void test1() throws Exception {

        Path path = Paths.get("src/test/resources", inFile);

        assertThrows(UnsupportedAudioFileException.class, () -> {
            // don't replace with Files#newInputStream(Path)
            new AACAudioFileReader().getAudioInputStream(new BufferedInputStream(Files.newInputStream(path.toFile().toPath())));
        });

        assertThrows(UnsupportedAudioFileException.class, () -> new AACAudioFileReader().getAudioInputStream(path.toFile()));

        assertThrows(UnsupportedAudioFileException.class, () -> new AACAudioFileReader().getAudioInputStream(path.toUri().toURL()));
    }

    @Test
    public void test11() throws Exception {

        Path path = Paths.get(AacFormatConversionProviderTest.class.getResource(inFile).toURI());

        assertThrows(UnsupportedAudioFileException.class, () -> new AACAudioFileReader().getAudioInputStream(new BufferedInputStream(Files.newInputStream(path))));
    }

    @Test
    @DisplayName("movie does not contain any AAC track")
    public void test12() throws Exception {

        Path path = Paths.get(AacFormatConversionProviderTest.class.getResource(inFile4).toURI());

        assertThrows(UnsupportedAudioFileException.class, () -> new AACAudioFileReader().getAudioInputStream(new BufferedInputStream(Files.newInputStream(path))));
    }

    @Test
    @DisplayName("a file consumes input stram all")
    public void test13() throws Exception {

        Path path = Paths.get(AacFormatConversionProviderTest.class.getResource(inFile3).toURI());

        assertThrows(UnsupportedAudioFileException.class, () -> new AACAudioFileReader().getAudioInputStream(new BufferedInputStream(Files.newInputStream(path))));
    }

    @Test
    @DisplayName("aac -> pcm")
    public void test2() throws Exception {

        //
        Path path = Paths.get(AacFormatConversionProviderTest.class.getResource(inFile1).toURI());
Debug.println("file: " + path);
        AudioInputStream aacAis = AudioSystem.getAudioInputStream(path.toFile());
Debug.println("INS: " + aacAis);
        AudioFormat inAudioFormat = aacAis.getFormat();
Debug.println("INF: " + inAudioFormat);
        AudioFormat outAudioFormat = new AudioFormat(
            AudioSystem.NOT_SPECIFIED,
            16,
            AudioSystem.NOT_SPECIFIED,
            true,
            false);

        assertTrue(AudioSystem.isConversionSupported(outAudioFormat, inAudioFormat));

        AudioInputStream pcmAis = AudioSystem.getAudioInputStream(outAudioFormat, aacAis);
Debug.println("OUTS: " + pcmAis);
Debug.println("OUT: " + pcmAis.getFormat());
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcmAis.getFormat());
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(pcmAis.getFormat());
        volume(line, .05d);
        line.start();


        byte[] buf = new byte[1024];
        while (!later(time).come()) {
            int r = pcmAis.read(buf, 0, 1024);
            if (r < 0) {
                break;
            }
            line.write(buf, 0, r);
        }
        line.drain();
        line.stop();
        line.close();
    }

    @Test
    @DisplayName("mp4 -> pcm")
    public void test3() throws Exception {

        //
        Path path = Paths.get(AacFormatConversionProviderTest.class.getResource(inFile2).toURI());
Debug.println("file: " + path);
        AudioInputStream aacAis = AudioSystem.getAudioInputStream(path.toFile());
Debug.println("INS: " + aacAis);
        AudioFormat inAudioFormat = aacAis.getFormat();
Debug.println("INF: " + inAudioFormat);
        AudioFormat outAudioFormat = new AudioFormat(
            inAudioFormat.getSampleRate(),
            16,
            inAudioFormat.getChannels(),
            true,
            false);

        assertTrue(AudioSystem.isConversionSupported(outAudioFormat, inAudioFormat));

        AudioInputStream pcmAis = AudioSystem.getAudioInputStream(outAudioFormat, aacAis);
Debug.println("OUTS: " + pcmAis);
Debug.println("OUT: " + pcmAis.getFormat());
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcmAis.getFormat());
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(pcmAis.getFormat());
        volume(line, .05d);
        line.start();


        byte[] buf = new byte[1024];
        while (!later(time).come()) {
            int r = pcmAis.read(buf, 0, 1024);
            if (r < 0) {
                break;
            }
            line.write(buf, 0, r);
        }
        line.drain();
        line.stop();
        line.close();
    }
}

/* */
