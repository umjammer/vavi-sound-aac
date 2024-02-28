package net.sourceforge.jaad.spi.javasound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.MP4Input;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;


public class AACAudioFileReader extends AudioFileReader {

    private static Logger logger = Logger.getLogger(AACAudioFileReader.class.getName());

    public static final AudioFileFormat.Type AAC = new AudioFileFormat.Type("AAC", "aac");
    public static final AudioFileFormat.Type MP4 = new AudioFileFormat.Type("MP4", "mp4");
    public static final AudioFormat.Encoding AAC_ENCODING = new AudioFormat.Encoding("AAC");

    private static class LimitedInputStream extends FilterInputStream {

        static final String ERROR_MESSAGE_REACHED_TO_LIMIT = "stop reading, prevent form eof";

        protected LimitedInputStream(InputStream in) throws IOException {
            super(in);
            logger.fine("limit: " + in.available());
        }

        private void check(int r) throws IOException {
            if (in.available() < r) {
                logger.fine("reached to limit");
                throw new IOException(ERROR_MESSAGE_REACHED_TO_LIMIT);
            }
        }

        @Override
        public int read() throws IOException {
            check(1);
            return super.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            check(b.length);
            return super.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            check(len);
            return super.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            check((int) n);
            return super.skip(n);
        }
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream in) throws UnsupportedAudioFileException, IOException {
        return getAudioFileFormat(in, AudioSystem.NOT_SPECIFIED);
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        try (InputStream in = url.openStream()) {
            return getAudioFileFormat(in instanceof BufferedInputStream ? in : new BufferedInputStream(in, Integer.MAX_VALUE - 8));
        }
    }

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return getAudioFileFormat(new BufferedInputStream(in, Integer.MAX_VALUE - 8), (int) file.length());
        }
    }

    private AudioFileFormat getAudioFileFormat(InputStream in, int mediaLength) throws UnsupportedAudioFileException, IOException {
        try {
            if (!in.markSupported()) throw new IllegalArgumentException("mark not supported");

            byte[] head = new byte[12];
            synchronized (this) {
                in.mark(12);
                in.read(head);
                in.reset(); // (*a)
            }

            int whole = in.available();
            in.mark(whole);
logger.fine("mark: " + whole);
            in = new LimitedInputStream(in);

            boolean canHandle;
            AudioFileFormat.Type type = AAC;
            if (new String(head, 4, 4).equals("ftyp")) {

                // ⚠⚠⚠ in position must be zero ⚠⚠⚠
                MP4Input is = MP4Input.open(in);
                MP4Container cont = new MP4Container(is);
                Movie movie = cont.getMovie();
                List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
                if (tracks.isEmpty()) throw new UnsupportedAudioFileException("movie does not contain any AAC track");
                Track track = tracks.get(0);
                Decoder.create(track.getDecoderSpecificInfo().getData());

                canHandle = true;
                type = MP4;
                // This code is pulled directly from MP3-SPI.
            } else if ((head[0] == 'R') && (head[1] == 'I') && (head[2] == 'F') && (head[3] == 'F') && (head[8] == 'W') && (head[9] == 'A') && (head[10] == 'V') && (head[11] == 'E')) {
                canHandle = false;    // RIFF/WAV stream found
            } else if ((head[0] == '.') && (head[1] == 's') && (head[2] == 'n') && (head[3] == 'd')) {
                canHandle = false;    // AU stream found
            } else if ((head[0] == 'F') && (head[1] == 'O') && (head[2] == 'R') && (head[3] == 'M') && (head[8] == 'A') && (head[9] == 'I') && (head[10] == 'F') && (head[11] == 'F')) {
                canHandle = false;    // AIFF stream found
            } else if (((head[0] == 'M') | (head[0] == 'm')) && ((head[1] == 'A') | (head[1] == 'a')) && ((head[2] == 'C') | (head[2] == 'c'))) {
                canHandle = false;    // APE stream found
            } else if (((head[0] == 'F') | (head[0] == 'f')) && ((head[1] == 'L') | (head[1] == 'l')) && ((head[2] == 'A') | (head[2] == 'a')) && ((head[3] == 'C') | (head[3] == 'c'))) {
                canHandle = false;    // FLAC stream found
            } else if (((head[0] == 'I') | (head[0] == 'i')) && ((head[1] == 'C') | (head[1] == 'c')) && ((head[2] == 'Y') | (head[2] == 'y'))) {
                canHandle = false;    // Shoutcast / ICE stream ?
            } else if (((head[0] == 'O') | (head[0] == 'o')) && ((head[1] == 'G') | (head[1] == 'g')) && ((head[2] == 'G') | (head[2] == 'g'))) {
                canHandle = false;    // Ogg stream ?
            } else {
                ADTSDemultiplexer adts = new ADTSDemultiplexer(in);
                Decoder.create(adts.getDecoderInfo());

                canHandle = true;
            }

            if (canHandle) {
                AudioFileFormat.Type afft = type;
                AudioFormat format = new AudioFormat(AAC_ENCODING, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, true, new HashMap<>() {{
                    put("type", afft);
                }});
                logger.fine("DEFINED: " + type);
                return new AudioFileFormat(type, format, mediaLength);
            } else {
                throw new UnsupportedAudioFileException("no match sequence");
            }

        } catch (IOException e) {
            if (e.getMessage().equals(LimitedInputStream.ERROR_MESSAGE_REACHED_TO_LIMIT)) {
                logger.fine(LimitedInputStream.ERROR_MESSAGE_REACHED_TO_LIMIT);
                throw new UnsupportedAudioFileException(e.getMessage());
            } else if (e.getMessage().equals("no ADTS header found")) {
                logger.fine("no ADTS header found");
                throw new UnsupportedAudioFileException(e.getMessage());
            } else if (e instanceof net.sourceforge.jaad.mp4.MP4Exception) {
                logger.fine(e.toString());
                throw new UnsupportedAudioFileException(e.getMessage());
            } else {
                logger.info(e.toString());
                throw e;
            }
        } finally {
            try {
                in.reset();
logger.fine("reset");
            } catch (IOException e) {
                logger.info("FAIL TO RESET: " + e);
            } finally {
                logger.fine("finally available: " + in.available());
            }
        }
    }

    // ----

    @Override
    public AudioInputStream getAudioInputStream(InputStream in) throws UnsupportedAudioFileException, IOException {
        try {
            AudioFileFormat aff = getAudioFileFormat(in, AudioSystem.NOT_SPECIFIED);
logger.fine("format: " + aff);

            int whole = in.available();
            in.mark(whole);
logger.fine("mark: " + whole);
            in = new LimitedInputStream(in);

            // in position should be zero
            return new AudioInputStream(in, aff.getFormat(), aff.getFrameLength());

        } catch (IOException e) {
            if (e.getMessage().equals(LimitedInputStream.ERROR_MESSAGE_REACHED_TO_LIMIT)) {
                logger.fine(LimitedInputStream.ERROR_MESSAGE_REACHED_TO_LIMIT);
                throw new UnsupportedAudioFileException(e.getMessage());
            } else if (e instanceof net.sourceforge.jaad.mp4.MP4Exception) {
                logger.fine(e.toString());
                throw new UnsupportedAudioFileException(e.getMessage());
            } else {
                logger.info(e.toString());
                throw e;
            }
        } finally {
            try {
                in.reset();
logger.fine("reset");
            } catch (IOException e) {
                logger.info("FAIL TO RESET: " + e);
            } finally {
                logger.fine("finally available: " + in.available());
            }
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = url.openStream();
        try {
            return getAudioInputStream(inputStream instanceof BufferedInputStream ? inputStream : new BufferedInputStream(inputStream, Integer.MAX_VALUE - 8));
        } catch (UnsupportedAudioFileException | IOException e) {
            inputStream.close();
            throw e;
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = Files.newInputStream(file.toPath());
        try {
            return getAudioInputStream(new BufferedInputStream(inputStream, Integer.MAX_VALUE - 8));
        } catch (UnsupportedAudioFileException | IOException e) {
            inputStream.close();
            throw e;
        }
    }
}
