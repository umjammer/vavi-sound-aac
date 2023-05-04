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
        try {
            if (!in.markSupported()) in = new BufferedInputStream(in);
            in.mark(1000);
            return getAudioFileFormat(new LimitedInputStream(in), AudioSystem.NOT_SPECIFIED);
        } finally {
            in.reset();
        }
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        try (InputStream in = url.openStream()) {
            return getAudioFileFormat(in);
        }
    }

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        InputStream in = null;
        try {
            in = new BufferedInputStream(Files.newInputStream(file.toPath()));
            in.mark(1000);
            AudioFileFormat aff = getAudioFileFormat(new LimitedInputStream(in), (int) file.length());
            return aff;
        } finally {
            if (in != null) {
                in.reset();
                in.close();
            }
        }
    }

    private AudioFileFormat getAudioFileFormat(InputStream in, int mediaLength) throws UnsupportedAudioFileException, IOException {
        try {
            byte[] head = new byte[12];
            in.read(head);
            boolean canHandle = false;
            AudioFileFormat.Type type = AAC;
            if (new String(head, 4, 4).equals("ftyp")) {
                in.reset();
                in.mark(1000);

                // in position should be zero
                MP4Input is = MP4Input.open(in);
                MP4Container cont = new MP4Container(is);
                Movie movie = cont.getMovie();
                List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
                if (tracks.isEmpty()) throw new UnsupportedAudioFileException("movie does not contain any AAC track");
                Track track = tracks.get(0);
                Decoder.create(track.getDecoderSpecificInfo().getData());

                canHandle = true;
                type = MP4;
                //This code is pulled directly from MP3-SPI.
            } else if ((head[0] == 'R') && (head[1] == 'I') && (head[2] == 'F') && (head[3] == 'F') && (head[8] == 'W') && (head[9] == 'A') && (head[10] == 'V') && (head[11] == 'E')) {
                canHandle = false;    //RIFF/WAV stream found
            } else if ((head[0] == '.') && (head[1] == 's') && (head[2] == 'n') && (head[3] == 'd')) {
                canHandle = false;    //AU stream found
            } else if ((head[0] == 'F') && (head[1] == 'O') && (head[2] == 'R') && (head[3] == 'M') && (head[8] == 'A') && (head[9] == 'I') && (head[10] == 'F') && (head[11] == 'F')) {
                canHandle = false;    //AIFF stream found
            } else if (((head[0] == 'M') | (head[0] == 'm')) && ((head[1] == 'A') | (head[1] == 'a')) && ((head[2] == 'C') | (head[2] == 'c'))) {
                canHandle = false;    //APE stream found
            } else if (((head[0] == 'F') | (head[0] == 'f')) && ((head[1] == 'L') | (head[1] == 'l')) && ((head[2] == 'A') | (head[2] == 'a')) && ((head[3] == 'C') | (head[3] == 'c'))) {
                canHandle = false;    //FLAC stream found
            } else if (((head[0] == 'I') | (head[0] == 'i')) && ((head[1] == 'C') | (head[1] == 'c')) && ((head[2] == 'Y') | (head[2] == 'y'))) {
                canHandle = false;    //Shoutcast / ICE stream ?
            } else if (((head[0] == 'O') | (head[0] == 'o')) && ((head[1] == 'G') | (head[1] == 'g')) && ((head[2] == 'G') | (head[2] == 'g'))) {
                canHandle = false;    //Ogg stream ?
            } else {
                ADTSDemultiplexer adts = new ADTSDemultiplexer(in);
                Decoder.create(adts.getDecoderInfo());

                canHandle = true;
            }

            if (canHandle) {
                AudioFileFormat.Type afft = type;
                AudioFormat format = new AudioFormat(AAC_ENCODING, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, true, new HashMap<String, Object>() {{
                    put("type", afft);
                }});
                logger.fine("DEFINED: " + type);
                return new AudioFileFormat(type, format, mediaLength);
            } else {
                throw new UnsupportedAudioFileException("no match sequence");
            }
        } catch (Exception e) {
            logger.fine(e.getClass().getSimpleName() + ": " + e.getMessage());
            throw (UnsupportedAudioFileException) new UnsupportedAudioFileException().initCause(e);
        }
    }

    //================================================
    @Override
    public AudioInputStream getAudioInputStream(InputStream in) throws UnsupportedAudioFileException, IOException {
        boolean needReset = false;
        try {
            if (!in.markSupported()) in = new BufferedInputStream(in);
            synchronized (this) {
                in.mark(1000);
                logger.finer("mark: " + in.available());
                needReset = true;
            }
            AudioFileFormat aff = getAudioFileFormat(new LimitedInputStream(in), AudioSystem.NOT_SPECIFIED);
            synchronized (this) {
                logger.finer("before reset: " + in.available());
                in.reset();
                logger.finer("after reset: " + in.available());
                needReset = false;
            }
            synchronized (this) {
                in.mark(in.available());
                logger.finer("mark2: " + in.available());
                needReset = true;
            }
            // in position should be zero
            logger.fine("format: " + aff.getFormat());
            return new AudioInputStream(in, aff.getFormat(), aff.getFrameLength());
        } catch (UnsupportedAudioFileException e) {
            throw e;
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
        } catch (Exception e) {
            logger.info(e.toString());
            throw e;
        } finally {
            logger.fine("reset?: " + needReset + ", available: " + in.available());
            try {
                if (needReset) {
                    in.reset();
                }
                logger.fine("finally available: " + in.available());
            } catch (IOException e) {
                logger.info(e.toString());
            }
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream in = url.openStream();
        try {
            return getAudioInputStream(in);
        } catch (UnsupportedAudioFileException | IOException e) {
            if (in != null) in.close();
            throw e;
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        InputStream in = Files.newInputStream(file.toPath());
        try {
            return getAudioInputStream(in);
        } catch (UnsupportedAudioFileException | IOException e) {
            in.close();
            throw e;
        }
    }
}
