package net.sourceforge.jaad.aac;

import java.util.List;
import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import javax.sound.sampled.AudioFormat;

import net.sourceforge.jaad.SampleBuffer;
import net.sourceforge.jaad.aac.syntax.BitStream;
import net.sourceforge.jaad.aac.syntax.PCE;
import net.sourceforge.jaad.aac.syntax.SyntacticElements;
import net.sourceforge.jaad.aac.transport.ADIFHeader;


/**
 * Main AAC decoder class
 *
 * @author in-somnia
 */
public class Decoder {

    static final Logger logger = System.getLogger(Decoder.class.getName());

    private final DecoderConfig config;
    private final SyntacticElements syntacticElements;
    public int frames = 0;
    private ADIFHeader adifHeader;

    /**
     * The methods returns true, if a profile is supported by the decoder.
     *
     * @param profile an AAC profile
     * @return true if the specified profile can be decoded
     * @see Profile#isDecodingSupported()
     */
    public static boolean canDecode(Profile profile) {
        return profile.isDecodingSupported();
    }

    public static Decoder create(byte[] data) {
        return create(BitStream.open(data));
    }

    public static Decoder create(BitStream in) {
        DecoderConfig config = new DecoderConfig().decode(in);
        return create(config);
    }

    public static Decoder create(AudioDecoderInfo info) {
        DecoderConfig config = DecoderConfig.create(info);
        return create(config);
    }

    public static Decoder create(DecoderConfig config) {
        if (config == null)
            throw new IllegalArgumentException("illegal MP4 decoder specific info");
        return new Decoder(config);
    }

    /**
     * Initializes the decoder with a MP4 decoder specific info.
     * <p>
     * After this the MP4 frames can be passed to the
     * <code>decodeFrame(byte[], SampleBuffer)</code> method to decode them.
     *
     * @param config decoder specific info from an MP4 container
     * @throws AACException if the specified profile is not supported
     */
    public Decoder(DecoderConfig config) {
//        config = DecoderConfig.parseMP4DecoderSpecificInfo(decoderSpecificInfo);

        this.config = config;

        syntacticElements = new SyntacticElements(config);

        logger.log(Level.TRACE, "profile: {0}", config.getProfile());
        logger.log(Level.TRACE, "sf: {0}", config.getSampleFrequency() != null ? config.getSampleFrequency().getFrequency() : null);
        logger.log(Level.TRACE, "channels: {0}", config.getChannelConfiguration().getDescription());
    }

    public DecoderConfig getConfig() {
        return config;
    }

    /**
     * Decodes one frame of AAC data in frame mode and returns the raw PCM
     * data.
     *
     * @param frame  the AAC frame
     * @param buffer a buffer to hold the decoded PCM data
     * @throws AACException if decoding fails
     */
    public void decodeFrame(byte[] frame, SampleBuffer buffer) throws AACException {

        BitStream in = BitStream.open(frame);

        try {
            logger.log(Level.TRACE, () -> "frame %d @%d".formatted(frames, 8 * frame.length));
            decode(in, buffer);
            logger.log(Level.TRACE, () -> "left %d".formatted(in.getBitsLeft()));
        } catch (EOSException e) {
            logger.log(Level.WARNING, "unexpected end of frame", e);
        } finally {
            ++frames;
        }
    }

    private void decode(BitStream in, SampleBuffer buffer) throws AACException {
        if (ADIFHeader.isPresent(in)) {
            adifHeader = ADIFHeader.readHeader(in);
            PCE pce = adifHeader.getFirstPCE();
            config.setProfile(pce.getProfile());
        }

        if (!canDecode(config.getProfile()))
            throw new AACException("unsupported profile: " + config.getProfile().getDescription());

        syntacticElements.startNewFrame();

        // 1: bitstream parsing and noiseless coding
        syntacticElements.decode(in);
        // 2: spectral processing
        List<float[]> channels = syntacticElements.process();
        // 3: send to output buffer
        buffer.accept(channels, config.getSampleLength(), config.getOutputFrequency().getFrequency());
    }

    public AudioFormat getAudioFormat() {

        int freq = config.getSampleFrequency().getFrequency();

        // assume SBR/PS
        if (!config.getProfile().isErrorResilientProfile()
                && config.getChannelConfiguration() == ChannelConfiguration.MONO
                && freq < 24000)
            freq *= 2;

        return new AudioFormat(freq, 16, config.getChannelCount(), true, false);
    }
}
