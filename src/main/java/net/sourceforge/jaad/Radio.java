package net.sourceforge.jaad;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URI;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;


/**
 * Command line example, that can decode an AAC stream from a Shoutcast/Icecast
 * server.
 *
 * @author in-somnia
 */
public class Radio {

    private static final String USAGE = "usage:\nnet.sourceforge.jaad.Radio <url>";

    public static void main(String[] args) {
        try {
            if (args.length < 1)
                printUsage();
            else
                decode(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("error while decoding: " + e);
        }
    }

    private static void printUsage() {
        System.out.println(USAGE);
        System.exit(1);
    }

    private static void decode(String arg) throws Exception {
        SampleBuffer buf = new SampleBuffer();

        SourceDataLine line = null;
        byte[] b;
        try {
            URI uri = new URI(arg);
            Socket sock = new Socket(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 80);

            // send HTTP request
            PrintStream out = new PrintStream(sock.getOutputStream());
            String path = uri.getPath();
            if (path == null || path.isEmpty())
                path = "/";
            if (uri.getQuery() != null)
                path += "?" + uri.getQuery();
            out.println("GET " + path + " HTTP/1.1");
            out.println("Host: " + uri.getHost());
            out.println();

            // read response (skip header)
            DataInputStream in = new DataInputStream(sock.getInputStream());
            String x;
            do {
                x = in.readLine();
            } while (x != null && !x.trim().isEmpty());

            ADTSDemultiplexer adts = new ADTSDemultiplexer(in);
            AudioFormat aufmt = new AudioFormat(adts.getSampleFrequency(), 16, adts.getChannelCount(), true, true);
            Decoder dec = Decoder.create(adts.getDecoderInfo());

            while (true) {
                b = adts.readNextFrame();
                dec.decodeFrame(b, buf);

                if (line != null && formatChanged(line.getFormat(), buf)) {
                    // format has changed (e.g. SBR has started)
                    line.stop();
                    line.close();
                    line = null;
                    aufmt = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(), buf.getChannels(), true, true);
                }
                if (line == null) {
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

    private static boolean formatChanged(AudioFormat af, SampleBuffer buf) {
        return af.getSampleRate() != buf.getSampleRate()
                || af.getChannels() != buf.getChannels()
                || af.getSampleSizeInBits() != buf.getBitsPerSample()
                || af.isBigEndian() != buf.isBigEndian();
    }
}
