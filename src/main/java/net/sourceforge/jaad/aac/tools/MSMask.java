package net.sourceforge.jaad.aac.tools;

import net.sourceforge.jaad.aac.AACException;


/**
 * The MSMask indicates, if MS is applied to a specific ICStream.
 *
 * @author in-somnia
 */
public enum MSMask {

    TYPE_ALL_0(0),
    TYPE_USED(1),
    TYPE_ALL_1(2),
    TYPE_RESERVED(3);

    public static MSMask forInt(int i) {
        MSMask m = switch (i) {
            case 0 -> TYPE_ALL_0;
            case 1 -> TYPE_USED;
            case 2 -> TYPE_ALL_1;
            case 3 -> TYPE_RESERVED;
            default -> throw new AACException("unknown MS mask type");
        };
        return m;
    }

    private final int num;

    MSMask(int num) {
        this.num = num;
    }
}
