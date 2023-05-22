package net.sourceforge.jaad.mp4.boxes.impl.samplegroupentries;

import java.io.IOException;

import net.sourceforge.jaad.mp4.MP4Input;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;


public abstract class SampleGroupDescriptionEntry extends BoxImpl {

    protected SampleGroupDescriptionEntry(String name) {
        super(name);
    }

    @Override
    public abstract void decode(MP4Input in) throws IOException;
}
