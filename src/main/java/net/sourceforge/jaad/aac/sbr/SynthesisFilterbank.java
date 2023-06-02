package net.sourceforge.jaad.aac.sbr;


abstract class SynthesisFilterbank extends Filterbank {

    SynthesisFilterbank(int channels) {
        super(channels);
    }

    abstract void synthesis(int numTimeSlotsRate, float[][][] X, float[] output);
}
