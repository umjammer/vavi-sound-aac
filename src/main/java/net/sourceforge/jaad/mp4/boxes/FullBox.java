package net.sourceforge.jaad.mp4.boxes;

import java.io.IOException;

import net.sourceforge.jaad.mp4.MP4Input;


public class FullBox extends BoxImpl {

    protected int version, flags;

    public FullBox(String name) {
        super(name);
    }

    @Override
    public void decode(MP4Input in) throws IOException {
        version = in.readByte();
        flags = (int) in.readBytes(3);
    }
}
