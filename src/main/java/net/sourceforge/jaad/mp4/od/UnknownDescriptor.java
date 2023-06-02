package net.sourceforge.jaad.mp4.od;

import java.io.IOException;

import net.sourceforge.jaad.mp4.MP4Input;


/**
 * This class is used if any unknown Descriptor is found in a stream. All
 * contents of the Descriptor will be skipped.
 *
 * @author in-somnia
 */
public class UnknownDescriptor extends Descriptor {

    @Override
    void decode(MP4Input in) throws IOException {
        // content will be skipped
    }
}
