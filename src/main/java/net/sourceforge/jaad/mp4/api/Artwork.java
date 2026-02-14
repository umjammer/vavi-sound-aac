package net.sourceforge.jaad.mp4.api;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.lang.System.Logger;
import javax.imageio.ImageIO;

import net.sourceforge.jaad.mp4.boxes.impl.meta.ITunesMetadataBox.DataType;


public class Artwork {

    private static final Logger logger = System.getLogger(Artwork.class.getName());

    // TODO: need this enum? it just copies the DataType
    public enum Type {

        GIF, JPEG, PNG, BMP;

        static Type forDataType(DataType dataType) {
            Type type = switch (dataType) {
                case GIF -> GIF;
                case JPEG -> JPEG;
                case PNG -> PNG;
                case BMP -> BMP;
                default -> null;
            };
            return type;
        }
    }

    private final Type type;
    private final byte[] data;
    private Image image;

    Artwork(Type type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Returns the type of data in this artwork.
     *
     * @return the data's type
     * @see Type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the encoded data of this artwork.
     *
     * @return the encoded data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the decoded image, that can be painted.
     *
     * @return the decoded image
     * @throws IOException if decoding fails
     */
    public Image getImage() throws IOException {
        try {
            if (image == null) image = ImageIO.read(new ByteArrayInputStream(data));
            return image;
        } catch (IOException e) {
            logger.log(Level.ERROR, "Artwork.getImage failed: " + e.getMessage(), e);
            throw e;
        }
    }
}
