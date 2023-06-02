package net.sourceforge.jaad.aac.syntax;

import java.util.List;

import net.sourceforge.jaad.aac.DecoderConfig;
import net.sourceforge.jaad.aac.sbr.SBR;


/**
 * lfe_channel_element: Abbreviation LFE.
 * <p>
 * Syntactic element that contains a low sampling frequency enhancement channel.
 * The rules for the number of lfe_channel_element()’s and instance tags are
 * as for single_channel_element’s.
 */

class LFE extends SCE {

    public static final Type TYPE = Type.LFE;

    static class Tag extends SCE.Tag {

        protected Tag(int id) {
            super(id);
        }

        @Override
        public boolean isChannelPair() {
            return false;
        }

        @Override
        public Type getType() {
            return TYPE;
        }

        @Override
        public ChannelElement newElement(DecoderConfig config) {
            return new LFE(config, this);
        }
    }

    public static final List<Tag> TAGS = Element.createTagList(16, Tag::new);

    LFE(DecoderConfig config, Tag tag) {
        super(config, tag);
    }

    @Override
    protected SBR openSBR() {
        return null;
    }

    @Override
    public boolean isChannelPair() {
        return false;
    }

    public boolean isLFE() {
        return true;
    }
}
