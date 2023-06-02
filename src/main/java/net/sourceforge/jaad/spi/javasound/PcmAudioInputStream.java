package net.sourceforge.jaad.spi.javasound;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;

import net.sourceforge.jaad.SampleBuffer;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;


class PcmAudioInputStream extends AsynchronousAudioInputStream {

    private static Logger logger = Logger.getLogger(PcmAudioInputStream.class.getName());

    private final ADTSDemultiplexer adts;
    private final Decoder decoder;
    private final SampleBuffer sampleBuffer;
    private AudioFormat audioFormat = null;
    private byte[] saved;

    PcmAudioInputStream(InputStream in, AudioFormat format, long length) throws IOException {
        super(in, format, length);
        adts = new ADTSDemultiplexer(in);
        decoder = Decoder.create(adts.getDecoderInfo());
        sampleBuffer = new SampleBuffer();
    }

    @Override
    public AudioFormat getFormat() {
        if (audioFormat == null) {
            // read first frame
            try {
                decoder.decodeFrame(adts.readNextFrame(), sampleBuffer);
                audioFormat = new AudioFormat(sampleBuffer.getSampleRate(), sampleBuffer.getBitsPerSample(), sampleBuffer.getChannels(), true, true);
                logger.fine("format: " + audioFormat);
                saved = sampleBuffer.getData();
            } catch (IOException e) {
                return null;
            }
        }
        return audioFormat;
    }

    @Override
    public void execute() {
        try {
            if (saved == null) {
                decoder.decodeFrame(adts.readNextFrame(), sampleBuffer);
                buffer.write(sampleBuffer.getData());
            } else {
                buffer.write(saved);
                saved = null;
            }
        } catch (IOException e) {
            buffer.close();
        }
    }
}
