package net.sourceforge.jaad.mp4.boxes.impl.sampleentries.codec;

import java.io.IOException;

import net.sourceforge.jaad.mp4.MP4Input;


public class SMVSpecificBox extends CodecSpecificBox {

    private int framesPerSample;

    public SMVSpecificBox() {
        super("SMV Specific Structure");
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
