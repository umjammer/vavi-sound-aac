package net.sourceforge.jaad.mp4.boxes.impl.sampleentries.codec;

import java.io.IOException;

import net.sourceforge.jaad.mp4.MP4Input;


public class EVRCSpecificBox extends CodecSpecificBox {

    private int framesPerSample;

    public EVRCSpecificBox() {
        super("EVCR Specific Box");
    }

    @Override
    public void decode(MP4Input in) throws IOException {
        decodeCommon(in);

        framesPerSample = in.readByte();
    }

    public int getFramesPerSample() {
        return framesPerSample;
    }
}
