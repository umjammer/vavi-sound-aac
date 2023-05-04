package net.sourceforge.jaad.aac.syntax;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.jaad.aac.EOSException;


public class ByteArrayBitStream implements BitStream {

    static final Logger LOGGER = Logger.getLogger(ByteArrayBitStream.class.getName());

    private static final int WORD_BITS = 32;
    private static final int WORD_BYTES = 4;
    private static final int BYTE_MASK = 0xff;

    private byte[] buffer;
    /** # of bits in buffer */
    private int length;
    /** offset in the buffer array */
    private int pos;
    /** current 4 bytes, that are read from the buffer */
    private int cache;
    /** remaining bits in current cache */
    protected int bitsCached;
    /** number of total bits read */
    protected int position;

    public String toString() {
        if (buffer == null)
            return "[]";
        else
            return String.format("[%d;%d]", getPosition(), length);
    }

    ByteArrayBitStream() {
    }

    /**
     * Clone a subset of another BitStream
     *
     * @param other BitStream
     * @param size  of this sub stream
     */
    ByteArrayBitStream(ByteArrayBitStream other, int size) {

        if (other.getBitsLeft() < size) throw new EOSException("stream overrun");

        this.length = other.position + size;
        this.buffer = other.buffer;
        this.pos = other.pos;
        this.cache = other.cache;
        this.bitsCached = other.bitsCached;
        this.position = other.position;

        other.skipBits(size);
    }

    ByteArrayBitStream(byte[] data) {
        setData(data);
    }

    public void destroy() {
        reset();
        buffer = null;
    }

    public final void setData(byte[] data) {
        reset();

        int size = data.length;
        this.length = 8 * size;

        // reduce the buffer size to an integer number of words
        int shift = size % WORD_BYTES;

        // push leading bytes to cache
        bitsCached = 8 * shift;

        for (int i = 0; i < shift; ++i) {
            byte c = data[i];
            cache <<= 8;
            cache |= 0xff & c;
        }

        size -= shift;

        // only reallocate if needed
        if (buffer == null || buffer.length != size)
            buffer = new byte[size];

        System.arraycopy(data, shift, buffer, 0, buffer.length);
    }

    @Override
    public BitStream readSubStream(int n) {
        if (getBitsLeft() < n)
            throw new EOSException("stream overrun");

        return new ByteArrayBitStream(this, n);
    }

    public void byteAlign() {
        LOGGER.log(Level.FINER, "@%d byteAlign: %d", position);
        int toFlush = bitsCached & 7;
        if (toFlush > 0)
            skipBits(toFlush);
    }

    public final void reset() {
        pos = 0;
        length = 0;
        bitsCached = 0;
        cache = 0;
        position = 0;
    }

    public int getPosition() {
        return position;
    }

    public int getBitsLeft() {
        return length - position;
    }

    /**
     * Reads the next four bytes.
     *
     * @param peek if true, the stream pointer will not be increased
     */
    protected int readCache(boolean peek) {
        int i;
        if (pos > buffer.length - WORD_BYTES)
            throw new EOSException("end of stream");

        else i = ((buffer[pos] & BYTE_MASK) << 24)
                | ((buffer[pos + 1] & BYTE_MASK) << 16)
                | ((buffer[pos + 2] & BYTE_MASK) << 8)
                | (buffer[pos + 3] & BYTE_MASK);
        if (!peek)
            pos += WORD_BYTES;
        return i;
    }

    public int readBits(int n) {
        LOGGER.log(n == 0 ? Level.FINEST : Level.FINER, "@%d readBits: %d", n);

        if (getBitsLeft() < n)
            throw new EOSException("stream overrun");

        int result;
        if (bitsCached >= n) {
            bitsCached -= n;
            result = (cache >> bitsCached) & maskBits(n);
            position += n;
        } else {
            position += n;
            int c = cache & maskBits(bitsCached);
            int left = n - bitsCached;
            cache = readCache(false);
            bitsCached = WORD_BITS - left;
            result = ((cache >> bitsCached) & maskBits(left)) | (c << left);
        }
        return result;
    }

    public int readBit() {
        LOGGER.log(Level.FINER, "@%d readBit: %d", 1);

        if (getBitsLeft() < 1)
            throw new EOSException("stream overrun");

        int i;
        if (bitsCached > 0) {
            bitsCached--;
            i = (cache >> (bitsCached)) & 1;
        } else {
            cache = readCache(false);
            bitsCached = WORD_BITS - 1;
            i = (cache >> bitsCached) & 1;
        }

        position++;
        return i;
    }

    public boolean readBool() {
        return (readBit() & 0x1) != 0;
    }

    public int peekBits(int n) {
        LOGGER.log(Level.FINER, "@%d peekBits: %d", n);

        if (getBitsLeft() < n)
            throw new EOSException("stream overrun");

        int ret;
        if (bitsCached >= n) {
            ret = (cache >> (bitsCached - n)) & maskBits(n);
        } else {
            //old cache
            int c = cache & maskBits(bitsCached);
            n -= bitsCached;
            //read next & combine
            ret = ((readCache(true) >> WORD_BITS - n) & maskBits(n)) | (c << n);
        }
        return ret;
    }

    public int peekBit() {
        LOGGER.log(Level.FINER, "@%d peekBit: %d", 1);

        if (getBitsLeft() < 1)
            throw new EOSException("stream overrun");

        int ret;
        if (bitsCached > 0) {
            ret = (cache >> (bitsCached - 1)) & 1;
        } else {
            int word = readCache(true);
            ret = (word >> WORD_BITS - 1) & 1;
        }
        return ret;
    }

    public void skipBits(int n) {
        LOGGER.log(Level.FINER, "@%d skipBits: %d", n);

        if (getBitsLeft() < n)
            throw new EOSException("stream overrun");

        position += n;
        if (n <= bitsCached) {
            bitsCached -= n;
        } else {
            n -= bitsCached;
            while (n >= WORD_BITS) {
                n -= WORD_BITS;
                readCache(false);
            }
            if (n > 0) {
                cache = readCache(false);
                bitsCached = WORD_BITS - n;
            } else {
                cache = 0;
                bitsCached = 0;
            }
        }
    }

    public void skipBit() {
        LOGGER.log(Level.FINER, "@%d skipBit: %d", 1);

        if (getBitsLeft() < 1)
            throw new EOSException("end of stream");

        position++;
        if (bitsCached > 0) {
            bitsCached--;
        } else {
            cache = readCache(false);
            bitsCached = WORD_BITS - 1;
        }
    }

    public static int maskBits(int n) {
        int i;
        if (n == 32)
            i = -1;
        else
            i = (1 << n) - 1;
        return i;
    }
}
