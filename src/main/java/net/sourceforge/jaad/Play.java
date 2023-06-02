package net.sourceforge.jaad;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.DecoderConfig;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.MP4Input;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;


/**
 * Command line example, that can decode an AAC file and play it.
 *
 * @author in-somnia
 */
public class Play {

    private static final String USAGE = "usage:\nnet.sourceforge.jaad.Play [-mp4] <infile>\n\n\t-mp4\tinput file is in MP4 container format";

    public static void main(String[] args) {
        try {
            if (args.length < 1) printUsage();
            if (args[0].equals("-mp4")) {
                if (args.length < 2) printUsage();
                else decodeMP4(args[1]);
            } else decodeAAC(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("error while decoding: " + e);
        }
    }

    private static void printUsage() {
        System.out.println(USAGE);
        System.exit(1);
    }

    private static void decodeMP4(String in) throws Exception {
        if (in.startsWith("http:"))
            decodeMP4(new URL(in).openStream());
        else
            decodeMP4(new RandomAccessFile(in, "r"));
    }

    private static void decodeMP4(InputStream in) throws Exception {
        decodeMP4(MP4Input.open(in));
    }

    private static void decodeMP4(RandomAccessFile in) throws Exception {
        decodeMP4(MP4Input.open(in));
    }

    private static void decodeMP4(MP4Input in) throws Exception {

        // create container
        MP4Container cont = new MP4Container(in);
        Movie movie = cont.getMovie();
        // find AAC track
        List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
        if (tracks.isEmpty()) throw new Exception("movie does not contain any AAC track");
        AudioTrack track = (AudioTrack) tracks.get(0);

        // create AAC decoder
        Decoder dec = Decoder.create(track.getDecoderSpecificInfo().getData());

        // create audio format
        DecoderConfig conf = dec.getConfig();
        AudioFormat aufmt = new AudioFormat(conf.getOutputFrequency().getFrequency(), 16, conf.getChannelCount(), true, true);

        try (SourceDataLine line = AudioSystem.getSourceDataLine(aufmt)) {
            line.open();
            line.start();

            // decode
            SampleBuffer buf = new SampleBuffer(aufmt);
            while (track.hasMoreFrames()) {
                Frame frame = track.readNextFrame();

                try {
                    dec.decodeFrame(frame.getData(), buf);
                    byte[] b = buf.getData();
                    line.write(b, 0, b.length);
                } catch (AACException e) {
                    e.printStackTrace();
                    // since the frames are separate, decoding can continue if one fails
                }
            }
            line.drain();
        }
    }

    private static void decodeAAC(String in) throws Exception {
        if (in.startsWith("http:"))
            decodeAAC(new URL(in).openStream());
        else
            decodeAAC(Files.newInputStream(Paths.get(in)));
    }

    private static void decodeAAC(InputStream in) throws Exception {

        ADTSDemultiplexer adts = new ADTSDemultiplexer(in);
        Decoder dec = Decoder.create(adts.getDecoderInfo());
        AudioFormat aufmt = dec.getAudioFormat();
        SampleBuffer buf = new SampleBuffer(aufmt);

        try (SourceDataLine line = AudioSystem.getSourceDataLine(aufmt)) {
            line.open();
            line.start();

            while (true) {
                byte[] b = adts.readNextFrame();
                dec.decodeFrame(b, buf);
                b = buf.getData();
                line.write(b, 0, b.length);
            }
        }
    }
}
