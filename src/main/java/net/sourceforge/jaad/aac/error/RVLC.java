package net.sourceforge.jaad.aac.error;

import net.sourceforge.jaad.aac.huffman.HCB;
import net.sourceforge.jaad.aac.syntax.BitStream;
import net.sourceforge.jaad.aac.syntax.ICSInfo;
import net.sourceforge.jaad.aac.syntax.ICStream;


/**
 * Reversible variable length coding
 * Decodes scalefactors if error resilience is used.
 */
public class RVLC implements RVLCTables {

    private static final int ESCAPE_FLAG = 7;

    public void decode(BitStream in, ICStream ics, int[][] scaleFactors) {
        int bits = (ics.getInfo().isEightShortFrame()) ? 11 : 9;
        boolean sfConcealment = in.readBool();
        int revGlobalGain = in.readBits(8);
        int rvlcSFLen = in.readBits(bits);

        ICSInfo info = ics.getInfo();
        int windowGroupCount = info.getWindowGroupCount();
        int maxSFB = info.getMaxSFB();
        int[][] sfbCB = null; // ics.getSectionData().getSfbCB();

        int sf = ics.getGlobalGain();
        int intensityPosition = 0;
        int noiseEnergy = sf - 90 - 256;
        boolean intensityUsed = false, noiseUsed = false;

        int sfb;
        for (int g = 0; g < windowGroupCount; g++) {
            for (sfb = 0; sfb < maxSFB; sfb++) {
                switch (sfbCB[g][sfb]) {
                case HCB.ZERO_HCB:
                    scaleFactors[g][sfb] = 0;
                    break;
                case HCB.INTENSITY_HCB:
                case HCB.INTENSITY_HCB2:
                    if (!intensityUsed) intensityUsed = true;
                    intensityPosition += decodeHuffman(in);
                    scaleFactors[g][sfb] = intensityPosition;
                    break;
                case HCB.NOISE_HCB:
                    if (noiseUsed) {
                        noiseEnergy += decodeHuffman(in);
                        scaleFactors[g][sfb] = noiseEnergy;
                    } else {
                        noiseUsed = true;
                        noiseEnergy = decodeHuffman(in);
                    }
                    break;
                default:
                    sf += decodeHuffman(in);
                    scaleFactors[g][sfb] = sf;
                    break;
                }
            }
        }

        int lastIntensityPosition = 0;
        if (intensityUsed) lastIntensityPosition = decodeHuffman(in);
        noiseUsed = false;
        if (in.readBool()) decodeEscapes(in, ics, scaleFactors);
    }

    private static void decodeEscapes(BitStream in, ICStream ics, int[][] scaleFactors) {
        ICSInfo info = ics.getInfo();
        int windowGroupCount = info.getWindowGroupCount();
        int maxSFB = info.getMaxSFB();
        int[][] sfbCB = null; // ics.getSectionData().getSfbCB();

        int escapesLen = in.readBits(8);

        boolean noiseUsed = false;

        int sfb, val;
        for (int g = 0; g < windowGroupCount; g++) {
            for (sfb = 0; sfb < maxSFB; sfb++) {
                if (sfbCB[g][sfb] == HCB.NOISE_HCB && !noiseUsed) noiseUsed = true;
                else if (Math.abs(sfbCB[g][sfb]) == ESCAPE_FLAG) {
                    val = decodeHuffmanEscape(in);
                    if (sfbCB[g][sfb] == -ESCAPE_FLAG) scaleFactors[g][sfb] -= val;
                    else scaleFactors[g][sfb] += val;
                }
            }
        }
    }

    private static int decodeHuffman(BitStream in) {
        int off = 0;
        int i = RVLC_BOOK[off][1];
        int cw = in.readBits(i);

        int j;
        while ((cw != RVLC_BOOK[off][2]) && (i < 10)) {
            off++;
            j = RVLC_BOOK[off][1] - i;
            i += j;
            cw <<= j;
            cw |= in.readBits(j);
        }

        return RVLC_BOOK[off][0];
    }

    private static int decodeHuffmanEscape(BitStream in) {
        int off = 0;
        int i = ESCAPE_BOOK[off][1];
        int cw = in.readBits(i);

        int j;
        while ((cw != ESCAPE_BOOK[off][2]) && (i < 21)) {
            off++;
            j = ESCAPE_BOOK[off][1] - i;
            i += j;
            cw <<= j;
            cw |= in.readBits(j);
        }

        return ESCAPE_BOOK[off][0];
    }
}
