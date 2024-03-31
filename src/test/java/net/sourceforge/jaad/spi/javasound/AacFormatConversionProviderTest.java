/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.sourceforge.jaad.spi.javasound;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.sound.SoundUtil;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static vavi.sound.SoundUtil.volume;
import static vavix.util.DelayedWorker.later;


/**
 * AacFormatConversionProviderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2023/05/23 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class AacFormatConversionProviderTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    static long time;

    static {
        System.setProperty("vavi.util.logging.VaviFormatter.extraClassMethod", "org\\.tritonus\\.share\\.TDebug#out");

        time = System.getProperty("vavi.test", "").equals("ide") ? 1000 * 1000 : 9 * 1000;
    }

    @Property
    String mp4 = "src/test/resources/test.m4a";

    static final String aac = "/test.aac";
    static final String mid = "/test.mid";
    static final String alac = "/alac.m4a";
    static final String caf = "/test.caf";

    @Test
    @DisplayName("unsupported exception is able to detect in 3 ways")
    public void test1() throws Exception {

        Path path = Paths.get("src/test/resources", mid);

        assertThrows(UnsupportedAudioFileException.class, () -> {
            // don't replace with Files#newInputStream(Path)
            new AACAudioFileReader().getAudioInputStream(new BufferedInputStream(Files.newInputStream(path.toFile().toPath())));
        });

        assertThrows(UnsupportedAudioFileException.class, () -> new AACAudioFileReader().getAudioInputStream(path.toFile()));

        assertThrows(UnsupportedAudioFileException.class, () -> new AACAudioFileReader().getAudioInputStream(path.toUri().toURL()));
    }

    @Test
    @DisplayName("not aac")
    public void test11() throws Exception {
        Path path = Paths.get(AacFormatConversionProviderTest.class.getResource(mid).toURI());
Debug.println(path);
        assertThrows(UnsupportedAudioFileException.class, () -> new AACAudioFileReader().getAudioInputStream(new BufferedInputStream(Files.newInputStream(path))));
    }

    @Test
    @DisplayName("movie does not contain any AAC track")
    public void test12() throws Exception {
        Path path = Paths.get(AacFormatConversionProviderTest.class.getResource(alac).toURI());
Debug.println(path);
        assertThrows(UnsupportedAudioFileException.class, () -> new AACAudioFileReader().getAudioInputStream(new BufferedInputStream(Files.newInputStream(path))));
    }

    @Test
    @DisplayName("a file consumes input stream all")
    public void test13() throws Exception {
        Path path = Paths.get(AacFormatConversionProviderTest.class.getResource(caf).toURI());
Debug.println(path);
        InputStream is = new BufferedInputStream(Files.newInputStream(path));
        int l = is.available();
        assertThrows(UnsupportedAudioFileException.class, () -> new AACAudioFileReader().getAudioInputStream(is));
        assertEquals(l, is.available());
    }

    @Test
    @DisplayName("aac -> pcm")
    public void test2() throws Exception {

        //
        Path path = Paths.get(AacFormatConversionProviderTest.class.getResource(aac).toURI());
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

        byte[] buf = new byte[8192];
        while (!later(time).come()) {
            int r = pcmAis.read(buf, 0, buf.length);
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
        Path path = Paths.get(mp4);
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

    @Test
    @DisplayName("another input type 2")
    void test62() throws Exception {
        URL url = Paths.get(mp4).toUri().toURL();
Debug.println(url);
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        assertEquals(AACAudioFileReader.AAC_ENCODING, ais.getFormat().getEncoding());
    }

    @Test
    @DisplayName("another input type 3")
    void test63() throws Exception {
        File file = Paths.get(mp4).toFile();
Debug.println(file);
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        assertEquals(AACAudioFileReader.AAC_ENCODING, ais.getFormat().getEncoding());
    }

    @Test
    @DisplayName("clip")
    void test4() throws Exception {

        AudioInputStream ais = AudioSystem.getAudioInputStream(Paths.get(mp4).toFile());
Debug.println(ais.getFormat());

        Clip clip = AudioSystem.getClip();
CountDownLatch cdl = new CountDownLatch(1);
clip.addLineListener(ev -> {
 Debug.println(ev.getType());
 if (ev.getType() == LineEvent.Type.STOP)
  cdl.countDown();
});
        clip.open(AudioSystem.getAudioInputStream(new AudioFormat(44100, 16, 2, true, false), ais));
SoundUtil.volume(clip, 0.1f);
        clip.start();
if (!System.getProperty("vavi.test", "").equals("ide")) {
 Thread.sleep(10 * 1000);
 clip.stop();
 Debug.println("Interrupt");
} else {
 cdl.await();
}
        clip.drain();
        clip.stop();
        clip.close();
    }
}

/* */
