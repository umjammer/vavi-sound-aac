package net.sourceforge.jaad.aac.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.20
 * Time: 18:59
 */
public interface Utils {

    boolean isDebug = Boolean.parseBoolean(System.getProperty("net.sourceforge.jaad.debug", "false").toLowerCase(Locale.ROOT));

    static boolean[] copyOf(boolean[] array) {
        return array == null ? null : java.util.Arrays.copyOf(array, array.length);
    }

    static int[] copyOf(int[] array) {
        return array == null ? null : java.util.Arrays.copyOf(array, array.length);
    }

    static float[] copyOf(float[] array) {
        return array == null ? null : java.util.Arrays.copyOf(array, array.length);
    }

    static void copyRange(int[] array, int srcPos, int destPos, int length) {
        System.arraycopy(array, srcPos, array, destPos, length);
    }

    static int clip(int idx, int min, int max) {
        idx = Math.max(idx, min);
        idx = Math.min(idx, max);
        return idx;
    }

    @SafeVarargs
    static <E> List<E> listOf(E... elements) {
        if (elements.length == 0) return Collections.emptyList();
        else return List.of(elements);
    }

    @SuppressWarnings("unchecked")
    static <E> List<E> listCopyOf(Collection<? extends E> coll) {
        if (coll == Collections.emptyList()) return (List<E>) coll;
        else return (List<E>) listOf(coll.toArray());
    }

}
