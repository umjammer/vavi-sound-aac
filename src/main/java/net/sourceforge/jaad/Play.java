package net.sourceforge.jaad;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.mp4.MP4Container;
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
        SourceDataLine line = null;
        byte[] b;
        try {
            //create container
            MP4Container cont = new MP4Container(new RandomAccessFile(in, "r"));
            Movie movie = cont.getMovie();
            //find AAC track
            List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
            if (tracks.isEmpty()) throw new Exception("movie does not contain any AAC track");
            AudioTrack track = (AudioTrack) tracks.get(0);

            //create audio format
            AudioFormat aufmt = new AudioFormat(track.getSampleRate(), track.getSampleSize(), track.getChannelCount(), true, true);
            line = AudioSystem.getSourceDataLine(aufmt);
            line.open();
            line.start();

            //create AAC decoder
            Decoder dec = new Decoder(track.getDecoderSpecificInfo());

            //decode
            Frame frame;
            SampleBuffer buf = new SampleBuffer();
            while (track.hasMoreFrames()) {
                frame = track.readNextFrame();
                try {
                    dec.decodeFrame(frame.getData(), buf);
                    b = buf.getData();
                    line.write(b, 0, b.length);
                } catch (AACException e) {
                    e.printStackTrace();
                    //since the frames are separate, decoding can continue if one fails
                }
            }
        } finally {
            if (line != null) {
                line.stop();
                line.close();
            }
        }
    }

    private static void decodeAAC(String in) throws Exception {
        SourceDataLine line = null;
        byte[] b;
        try {
            ADTSDemultiplexer adts = new ADTSDemultiplexer(Files.newInputStream(Paths.get(in)));
            Decoder dec = new Decoder(adts.getDecoderSpecificInfo());
            SampleBuffer buf = new SampleBuffer();
            while (true) {
                b = adts.readNextFrame();
                dec.decodeFrame(b, buf);

                if (line == null) {
                    AudioFormat aufmt = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(), buf.getChannels(), true, true);
                    line = AudioSystem.getSourceDataLine(aufmt);
                    line.open();
                    line.start();
                }
                b = buf.getData();
                line.write(b, 0, b.length);
            }
        } finally {
            if (line != null) {
                line.stop();
                line.close();
            }
        }
    }
}
