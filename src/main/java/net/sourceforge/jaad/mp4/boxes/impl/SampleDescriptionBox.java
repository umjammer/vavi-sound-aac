package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;

import net.sourceforge.jaad.mp4.MP4Input;
import net.sourceforge.jaad.mp4.boxes.FullBox;


/**
 * The sample description table gives detailed information about the coding type
 * used, and any initialization information needed for that coding.
 *
 * @author in-somnia
 */
public class SampleDescriptionBox extends FullBox {

    public SampleDescriptionBox() {
        super("Sample Description Box");
    }

    @Override
    public void decode(MP4Input in) throws IOException {
        super.decode(in);

        int entryCount = (int) in.readBytes(4);

        readChildren(in, entryCount);
    }
}
