package net.sourceforge.jaad.mp4.boxes;

import java.io.IOException;

import net.sourceforge.jaad.mp4.MP4Input;


/**
 * Box implementation that is used for unknown types.
 *
 * @author in-somnia
 */
class UnknownBox extends BoxImpl {

    UnknownBox() {
        super("unknown");
    }

    @Override
    public void decode(MP4Input in) throws IOException {
        // no need to read, box will be skipped
    }
}
