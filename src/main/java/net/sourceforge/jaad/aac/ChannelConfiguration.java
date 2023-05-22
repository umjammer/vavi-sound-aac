package net.sourceforge.jaad.aac;

import java.util.List;

import net.sourceforge.jaad.aac.tools.Utils;

import static net.sourceforge.jaad.aac.Speaker.BC;
import static net.sourceforge.jaad.aac.Speaker.BL;
import static net.sourceforge.jaad.aac.Speaker.BR;
import static net.sourceforge.jaad.aac.Speaker.FC;
import static net.sourceforge.jaad.aac.Speaker.FL;
import static net.sourceforge.jaad.aac.Speaker.FLC;
import static net.sourceforge.jaad.aac.Speaker.FR;
import static net.sourceforge.jaad.aac.Speaker.FRC;
import static net.sourceforge.jaad.aac.Speaker.LFE;


/**
 * All possible channel configurations for AAC.
 *
 * @author in-somnia
 */
public enum ChannelConfiguration {

    NONE("No channel"),
    MONO("Mono", FC),
    STEREO("Stereo", FL, FR),
    STEREO_PLUS_CENTER("Stereo+Center", FC, FL, FR),
    STEREO_PLUS_CENTER_PLUS_REAR_MONO("Stereo+Center+Rear", FC, FL, FR, BC),
    FIVE("Five channels", FC, FL, FR, BL, BR),
    FIVE_PLUS_ONE("Five channels+LF", FC, FL, FR, BL, BR, LFE),
    INVALID_SEVEN("Seven channels (invalid)", (Speaker) null),
    SEVEN_PLUS_ONE("Seven channels+LF", FC, FL, FR, BL, BR, FLC, FRC, LFE);

    static final ChannelConfiguration CHANNEL_CONFIG_UNSUPPORTED = null;

    static final List<ChannelConfiguration> CONFIGURATIONS = Utils.listOf(values());

    public static ChannelConfiguration forInt(int i) {
        // 7 -> 8
        // 8 -> 9 -> error
        if (i >= 7)
            ++i;

        return CONFIGURATIONS.get(i);
    }

    public static ChannelConfiguration forChannelCount(int n) {
        if (n == 7)
            throw new AACException("invalid channel configuration: 8");

        return CONFIGURATIONS.get(n);
    }

    private final String descr;

    private final List<Speaker> speakers;

    ChannelConfiguration(String descr) {
        this.descr = descr;
        this.speakers = Utils.listOf();
    }

    ChannelConfiguration(String descr, Speaker... speakes) {
        this.descr = descr;
        this.speakers = Utils.listOf(speakes);
        if (speakers.size() != ordinal())
            throw new IllegalArgumentException("invalid speaker count");
    }

    ChannelConfiguration(String descr, Speaker speaker) {
        this.descr = descr;
        this.speakers = speaker == null ? null : Utils.listOf(speaker);
    }

    /**
     * Returns the number of channels in this configuration.
     */
    public int getChannelCount() {
        return ordinal();
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    /**
     * Returns a short description of this configuration.
     *
     * @return the channel configuration's description
     */
    public String getDescription() {
        return descr;
    }

    /**
     * Returns a string representation of this channel configuration.
     * The method is identical to <code>getDescription()</code>.
     *
     * @return the channel configuration's description
     */
    @Override
    public String toString() {
        return descr;
    }
}
