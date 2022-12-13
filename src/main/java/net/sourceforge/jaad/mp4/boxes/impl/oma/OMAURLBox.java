package net.sourceforge.jaad.mp4.boxes.impl.oma;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * This box is used for several sub-boxes of the user-data box in an OMA DRM 
 * file. These boxes have in common, that they only contain one String.
 * 
 * @author in-somnia
 */
public class OMAURLBox extends FullBox {

	private String content;

	public OMAURLBox(String name) {
		super(name);
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		byte[] b = new byte[(int) getLeft(in)];
		in.readBytes(b);
		content = new String(b, StandardCharsets.UTF_8);
	}

	/**
	 * Returns the String that this box contains. Its meaning depends on the 
	 * type of this box.
	 * 
	 * @return the content of this box
	 */
	public String getContent() {
		return content;
	}
}
