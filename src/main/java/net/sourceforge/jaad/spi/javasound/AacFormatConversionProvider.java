/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package net.sourceforge.jaad.spi.javasound;

import java.io.IOException;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.FormatConversionProvider;


/**
 * AACFormatConversionProvider.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 220220 nsano initial version <br>
 */
public class AacFormatConversionProvider extends FormatConversionProvider {

    private static Logger logger = Logger.getLogger(AacFormatConversionProvider.class.getName());

    @Override
    public AudioFormat.Encoding[] getSourceEncodings() {
        return new AudioFormat.Encoding[] {AACAudioFileReader.AAC_ENCODING};
    }

    @Override
    public AudioFormat.Encoding[] getTargetEncodings() {
        return new AudioFormat.Encoding[] {AudioFormat.Encoding.PCM_SIGNED};
    }

    @Override
    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat) {
        if (sourceFormat.getEncoding() == AACAudioFileReader.AAC_ENCODING) {
            return new AudioFormat.Encoding[] {AudioFormat.Encoding.PCM_SIGNED};
        } else {
            return new AudioFormat.Encoding[0];
        }
    }

    @Override
    public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
        if (sourceFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
            return new AudioFormat[0];
        } else if (sourceFormat.getEncoding() == AACAudioFileReader.AAC_ENCODING && targetEncoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
            return new AudioFormat[] {
                    // TODO signed, endian should be free (means add more 3 patterns)
                    new AudioFormat(sourceFormat.getSampleRate(),
                            16,         // sample size in bits
                            sourceFormat.getChannels(),
                            true,       // signed
                            false)      // little endian (for PCM wav)
            };
        } else {
            return new AudioFormat[0];
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream) {
        try {
            if (isConversionSupported(targetEncoding, sourceStream.getFormat())) {
                AudioFormat[] formats = getTargetFormats(targetEncoding, sourceStream.getFormat());
                if (formats != null && formats.length > 0) {
                    AudioFormat sourceFormat = sourceStream.getFormat();
                    AudioFormat targetFormat = formats[0];
                    if (sourceFormat.equals(targetFormat)) {
                        logger.info("same1: " + sourceFormat);
                        return sourceStream;
                    } else if (sourceFormat.getEncoding() == AACAudioFileReader.AAC_ENCODING && targetFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
                        Object type = sourceFormat.getProperty("type");
                        logger.info("convert1: " + type);
                        if (type != null && type == AACAudioFileReader.MP4) {
                            return new MP4AudioInputStream(sourceStream, targetFormat, AudioSystem.NOT_SPECIFIED);
                        } else {
                            return new AACAudioInputStream(sourceStream, targetFormat, AudioSystem.NOT_SPECIFIED);
                        }
                    } else if (sourceFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && targetFormat.getEncoding() == AACAudioFileReader.AAC_ENCODING) {
                        throw new IllegalArgumentException("unable to convert " + sourceFormat + " to " + targetFormat);
                    } else {
                        throw new IllegalArgumentException("unable to convert " + sourceFormat + " to " + targetFormat.toString());
                    }
                } else {
                    throw new IllegalArgumentException("target format not found");
                }
            } else {
                throw new IllegalArgumentException("conversion not supported");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {
        try {
            if (isConversionSupported(targetFormat, sourceStream.getFormat())) {
                AudioFormat[] formats = getTargetFormats(targetFormat.getEncoding(), sourceStream.getFormat());
                if (formats != null && formats.length > 0) {
                    AudioFormat sourceFormat = sourceStream.getFormat();
                    if (sourceFormat.equals(targetFormat)) {
                        logger.info("same2: " + sourceFormat);
                        return sourceStream;
                    } else if (sourceFormat.getEncoding() == AACAudioFileReader.AAC_ENCODING &&
                            targetFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
                        Object type = sourceFormat.getProperty("type");
                        logger.info("convert2: " + type);
                        if (type != null && type == AACAudioFileReader.MP4) {
                            return new MP4AudioInputStream(sourceStream, targetFormat, AudioSystem.NOT_SPECIFIED);
                        } else {
                            return new AACAudioInputStream(sourceStream, targetFormat, AudioSystem.NOT_SPECIFIED);
                        }
                    } else if (sourceFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && targetFormat.getEncoding() == AACAudioFileReader.AAC_ENCODING) {
                        throw new IllegalArgumentException("unable to convert " + sourceFormat + " to " + targetFormat);
                    } else {
                        throw new IllegalArgumentException("unable to convert " + sourceFormat + " to " + targetFormat);
                    }
                } else {
                    throw new IllegalArgumentException("target format not found");
                }
            } else {
                throw new IllegalArgumentException("conversion not supported");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

/* */
