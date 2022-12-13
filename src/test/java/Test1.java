/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.EOFException;
import java.io.InputStream;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import org.junit.jupiter.api.Test;

import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static vavi.sound.SoundUtil.volume;
import static vavix.util.DelayedWorker.later;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;


/**
 * test basic functions.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022/02/19 umjammer initial version <br>
 */
public class Test1 {

    static long time;

    static {
        time = System.getProperty("vavi.test", "").equals("ide") ? 10 * 1000 : 1000 * 1000;
    }

    @Test
    void decodeMP4() throws Exception {
        InputStream in = Test1.class.getResourceAsStream("/test.m4a");
        SourceDataLine line = null;
        // create container
        MP4Container cont = new MP4Container(in);
        Movie movie = cont.getMovie();
        // find AAC track
        List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
        AudioTrack track = (AudioTrack) tracks.get(0);

        // create audio format
        AudioFormat aufmt = new AudioFormat(
            track.getSampleRate(), track.getSampleSize(), track.getChannelCount(), true, true);
        line = AudioSystem.getSourceDataLine(aufmt);
        line.open();
        volume(line, .2f);
        line.start();

        // create AAC decoder
        Decoder dec = new Decoder(track.getDecoderSpecificInfo());

        // decode
        Frame frame;
        SampleBuffer buf = new SampleBuffer();
        while (track.hasMoreFrames() && !later(time).come()) {
            frame = track.readNextFrame();
            dec.decodeFrame(frame.getData(), buf);
            byte[] b = buf.getData();
            line.write(b, 0, b.length);
        }
    }

    @Test
    void decodeAAC() throws Exception {
        InputStream in = Test1.class.getResourceAsStream("/test.aac");
        SourceDataLine line = null;
        ADTSDemultiplexer adts = new ADTSDemultiplexer(in);
        Decoder dec = new Decoder(adts.getDecoderSpecificInfo());
        SampleBuffer buf = new SampleBuffer();
        while (!later(time).come()) {
            try {
            byte[] b = adts.readNextFrame();
                dec.decodeFrame(b, buf);

                if (line == null) {
                    // need to read the first frame for determine rate, ch etc.
                    AudioFormat aufmt = new AudioFormat(
                    buf.getSampleRate(), buf.getBitsPerSample(), buf.getChannels(), true, true);
Debug.println("IN: " + aufmt);
                    line = AudioSystem.getSourceDataLine(aufmt);
                    line.open();
                    volume(line, .2f);
                    line.start();
                }
                b = buf.getData();
                line.write(b, 0, b.length);
            } catch (EOFException e) {
                break;
            }
        }
    }

    @Test
    void testX() {
        assertArrayEquals(new byte[4], "\0\0\0\0".getBytes());
    }
}

/* */
