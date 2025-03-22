package com.example.base.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;

import static com.example.base.utils.Preconditions.check;
import static com.example.base.utils.Preconditions.checkArgument;

public class ByteUtils {
    public static final byte[] EMPTY_ARRAY = new byte[0];

    public static final byte[] EMPTY_BYTES = new byte[32];

    public static final String ZERO_HASH = Hex.encodeHexString(EMPTY_BYTES);

    /** Maximum unsigned value that can be expressed by 16 bits. */
    public static final int MAX_UNSIGNED_SHORT = Short.toUnsignedInt((short) -1);
    /** Maximum unsigned value that can be expressed by 32 bits. */
    public static final long MAX_UNSIGNED_INTEGER = Integer.toUnsignedLong(-1);

    // 00000001, 00000010, 00000100, 00001000, ...
    private static final int[] bitMask = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};


    /**
     * 将多个字节数组合并成一个字节数组
     *
     * @param bytes
     * @return
     */
    public static byte[] merge(byte[]... bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("merge 传入 null");
        }
        Stream<Byte> stream = Stream.of();
        for (byte[] b: bytes) {
            stream = Stream.concat(stream, Arrays.stream(ArrayUtils.toObject(b)));
        }
        return ArrayUtils.toPrimitive(stream.toArray(Byte[]::new));
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b)); // %02x 代表2位十六进制
        }
        return hexString.toString();
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64ToByteArray(String base64) {
        return Base64.getDecoder().decode(base64);
    }


    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        checkArgument(b.signum() >= 0, () -> "b must be positive or zero: " + b);
        checkArgument(numBytes > 0, () -> "numBytes must be positive: " + numBytes);
        byte[] src = b.toByteArray();
        byte[] dest = new byte[numBytes];
        boolean isFirstByteOnlyForSign = src[0] == 0;
        int length = isFirstByteOnlyForSign ? src.length - 1 : src.length;
        checkArgument(length <= numBytes, () -> "The given number does not fit in " + numBytes);
        int srcPos = isFirstByteOnlyForSign ? 1 : 0;
        int destPos = numBytes - length;
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    /**
     * Converts an array of bytes into a positive BigInteger. This is the inverse of
     * {@link #bigIntegerToBytes(BigInteger, int)}.
     *
     * @param bytes to convert into a BigInteger
     * @return the converted BigInteger
     */
    public static BigInteger bytesToBigInteger(byte[] bytes) {
        return new BigInteger(1, bytes);
    }

    /**
     * Write a 16-bit integer to a given buffer in little-endian format.
     * <p>
     * The value is expected as an unsigned {@code int} as per the Java Unsigned Integer API.
     *
     * @param val value to be written
     * @param buf buffer to be written into
     * @return the buffer
     * @throws BufferOverflowException if the value doesn't fit the remaining buffer
     */
    public static ByteBuffer writeInt16LE(int val, ByteBuffer buf) throws BufferOverflowException {
        checkArgument(val >= 0 && val <= MAX_UNSIGNED_SHORT, () ->
                "value out of range: " + val);
        return buf.order(ByteOrder.LITTLE_ENDIAN).putShort((short) val);
    }

    /**
     * Write a 16-bit integer to a given buffer in big-endian format.
     * <p>
     * The value is expected as an unsigned {@code int} as per the Java Unsigned Integer API.
     *
     * @param val value to be written
     * @param buf buffer to be written into
     * @return the buffer
     * @throws BufferOverflowException if the value doesn't fit the remaining buffer
     */
    public static ByteBuffer writeInt16BE(int val, ByteBuffer buf) throws BufferOverflowException {
        checkArgument(val >= 0 && val <= MAX_UNSIGNED_SHORT, () ->
                "value out of range: " + val);
        return buf.order(ByteOrder.BIG_ENDIAN).putShort((short) val);
    }

    /**
     * Write a 32-bit integer to a given buffer in little-endian format.
     * <p>
     * The value is expected as a signed or unsigned {@code int}. If you've got an unsigned {@code long} as per the
     * Java Unsigned Integer API, use {@link #writeInt32LE(long, ByteBuffer)}.
     *
     * @param val value to be written
     * @param buf buffer to be written into
     * @return the buffer
     * @throws BufferOverflowException if the value doesn't fit the remaining buffer
     */
    public static ByteBuffer writeInt32LE(int val, ByteBuffer buf) throws BufferOverflowException {
        return buf.order(ByteOrder.LITTLE_ENDIAN).putInt(val);
    }

    /**
     * Write a 32-bit integer to a given buffer in little-endian format.
     * <p>
     * The value is expected as an unsigned {@code long} as per the Java Unsigned Integer API.
     *
     * @param val value to be written
     * @param buf buffer to be written into
     * @return the buffer
     * @throws BufferOverflowException if the value doesn't fit the remaining buffer
     */
    public static ByteBuffer writeInt32LE(long val, ByteBuffer buf) throws BufferOverflowException {
        checkArgument(val >= 0 && val <= MAX_UNSIGNED_INTEGER, () ->
                "value out of range: " + val);
        return buf.order(ByteOrder.LITTLE_ENDIAN).putInt((int) val);
    }

    /**
     * Write a 32-bit integer to a given byte array in little-endian format, starting at a given offset.
     * <p>
     * The value is expected as an unsigned {@code long} as per the Java Unsigned Integer API.
     *
     * @param val    value to be written
     * @param out    buffer to be written into
     * @param offset offset into the buffer
     * @throws ArrayIndexOutOfBoundsException if offset points outside of the buffer, or
     *                                        if the value doesn't fit the remaining buffer
     */
    public static void writeInt32LE(long val, byte[] out, int offset) throws ArrayIndexOutOfBoundsException {
        check(offset >= 0 && offset <= out.length - 4, () ->
                new ArrayIndexOutOfBoundsException(offset));
        writeInt32LE(val, ByteBuffer.wrap(out, offset, out.length - offset));
    }

    /**
     * Write a 32-bit integer to a given buffer in big-endian format.
     * <p>
     * The value is expected as a signed or unsigned {@code int}.
     *
     * @param val value to be written
     * @param buf buffer to be written into
     * @return the buffer
     * @throws BufferOverflowException if the value doesn't fit the remaining buffer
     */
    public static ByteBuffer writeInt32BE(int val, ByteBuffer buf) throws BufferOverflowException {
        return buf.order(ByteOrder.BIG_ENDIAN).putInt((int) val);
    }

    /**
     * Write a 32-bit integer to a given byte array in big-endian format, starting at a given offset.
     * <p>
     * The value is expected as a signed or unsigned {@code int}.
     *
     * @param val    value to be written
     * @param out    buffer to be written into
     * @param offset offset into the buffer
     * @throws ArrayIndexOutOfBoundsException if offset points outside of the buffer, or
     *                                        if the value doesn't fit the remaining buffer
     */
    public static void writeInt32BE(int val, byte[] out, int offset) throws ArrayIndexOutOfBoundsException {
        writeInt32BE(val, ByteBuffer.wrap(out, offset, out.length - offset));
    }

    /**
     * Write a 64-bit integer to a given buffer in little-endian format.
     * <p>
     * The value is expected as a signed or unsigned {@code long}.
     *
     * @param val value to be written
     * @param buf buffer to be written into
     * @return the buffer
     * @throws BufferOverflowException if the value doesn't fit the remaining buffer
     */
    public static ByteBuffer writeInt64LE(long val, ByteBuffer buf) throws BufferOverflowException {
        return buf.order(ByteOrder.LITTLE_ENDIAN).putLong(val);
    }

    /**
     * Write a 64-bit integer to a given byte array in little-endian format, starting at a given offset.
     * <p>
     * The value is expected as a signed or unsigned {@code long}.
     *
     * @param val    value to be written
     * @param out    buffer to be written into
     * @param offset offset into the buffer
     * @throws ArrayIndexOutOfBoundsException if offset points outside of the buffer, or
     *                                        if the value doesn't fit the remaining buffer
     */
    public static void writeInt64LE(long val, byte[] out, int offset) throws ArrayIndexOutOfBoundsException {
        check(offset >= 0 && offset <= out.length + 8, () ->
                new ArrayIndexOutOfBoundsException(offset));
        writeInt64LE(val, ByteBuffer.wrap(out, offset, out.length - offset));
    }

    /**
     * Write a 16-bit integer to a given output stream in little-endian format.
     * <p>
     * The value is expected as an unsigned {@code int} as per the Java Unsigned Integer API.
     *
     * @param val    value to be written
     * @param stream stream to be written into
     * @throws IOException if an I/O error occurs
     */
    public static void writeInt16LE(int val, OutputStream stream) throws IOException {
        byte[] buf = new byte[2];
        writeInt16LE(val, ByteBuffer.wrap(buf));
        stream.write(buf);
    }

    /**
     * Write a 16-bit integer to a given output stream in big-endian format.
     * <p>
     * The value is expected as an unsigned {@code int} as per the Java Unsigned Integer API.
     *
     * @param val    value to be written
     * @param stream stream to be written into
     * @throws IOException if an I/O error occurs
     */
    public static void writeInt16BE(int val, OutputStream stream) throws IOException {
        byte[] buf = new byte[2];
        writeInt16BE(val, ByteBuffer.wrap(buf));
        stream.write(buf);
    }

    /**
     * Write a 32-bit integer to a given output stream in little-endian format.
     * <p>
     * The value is expected as a signed or unsigned {@code int}. If you've got an unsigned {@code long} as per the
     * Java Unsigned Integer API, use {@link #writeInt32LE(long, OutputStream)}.
     *
     * @param val    value to be written
     * @param stream stream to be written into
     * @throws IOException if an I/O error occurs
     */
    public static void writeInt32LE(int val, OutputStream stream) throws IOException {
        byte[] buf = new byte[4];
        writeInt32LE(val, ByteBuffer.wrap(buf));
        stream.write(buf);
    }

    /**
     * Write a 32-bit integer to a given output stream in little-endian format.
     * <p>
     * The value is expected as an unsigned {@code long} as per the Java Unsigned Integer API.
     *
     * @param val    value to be written
     * @param stream stream to be written into
     * @throws IOException if an I/O error occurs
     */
    public static void writeInt32LE(long val, OutputStream stream) throws IOException {
        byte[] buf = new byte[4];
        writeInt32LE(val, ByteBuffer.wrap(buf));
        stream.write(buf);
    }

    /**
     * Write a 32-bit integer to a given output stream in big-endian format.
     * <p>
     * The value is expected as a signed or unsigned {@code int}.
     *
     * @param val    value to be written
     * @param stream stream to be written into
     * @throws IOException if an I/O error occurs
     */
    public static void writeInt32BE(int val, OutputStream stream) throws IOException {
        byte[] buf = new byte[4];
        writeInt32BE(val, ByteBuffer.wrap(buf));
        stream.write(buf);
    }

    /**
     * Write a 64-bit integer to a given output stream in little-endian format.
     * <p>
     * The value is expected as a signed or unsigned {@code long}.
     *
     * @param val    value to be written
     * @param stream stream to be written into
     * @throws IOException if an I/O error occurs
     */
    public static void writeInt64LE(long val, OutputStream stream) throws IOException {
        byte[] buf = new byte[8];
        writeInt64LE(val, ByteBuffer.wrap(buf));
        stream.write(buf);
    }

    /**
     * Write a 64-bit integer to a given output stream in little-endian format.
     * <p>
     * The value is expected as an unsigned {@link BigInteger}.
     *
     * @param val    value to be written
     * @param stream stream to be written into
     * @throws IOException if an I/O error occurs
     */
    public static void writeInt64LE(BigInteger val, OutputStream stream) throws IOException {
        byte[] bytes = val.toByteArray();
        if (bytes.length > 8) {
            throw new RuntimeException("Input too large to encode into a uint64");
        }
        bytes = reverseBytes(bytes);
        stream.write(bytes);
        if (bytes.length < 8) {
            for (int i = 0; i < 8 - bytes.length; i++)
                stream.write(0);
        }
    }

    /**
     * Read 2 bytes from the buffer as unsigned 16-bit integer in little endian format.
     * @param buf buffer to be read from
     * @throws BufferUnderflowException if the read value extends beyond the remaining bytes of the buffer
     */
    public static int readUint16(ByteBuffer buf) throws BufferUnderflowException {
        return Short.toUnsignedInt(buf.order(ByteOrder.LITTLE_ENDIAN).getShort());
    }

    /**
     * Read 2 bytes from the byte array (starting at the offset) as unsigned 16-bit integer in little endian format.
     * @param bytes buffer to be read from
     * @param offset offset into the buffer
     * @throws ArrayIndexOutOfBoundsException if offset points outside of the buffer, or
     *                                        if the read value extends beyond the remaining bytes of the buffer
     */
    public static int readUint16(byte[] bytes, int offset) throws ArrayIndexOutOfBoundsException {
        check(offset >= 0 && offset <= bytes.length - 2, () ->
                new ArrayIndexOutOfBoundsException(offset));
        return readUint16(ByteBuffer.wrap(bytes, offset, bytes.length - offset));
    }

    /**
     * Read 2 bytes from the buffer as unsigned 16-bit integer in big endian format.
     * @param buf buffer to be read from
     * @throws BufferUnderflowException if the read value extends beyond the remaining bytes of the buffer
     */
    public static int readUint16BE(ByteBuffer buf) throws BufferUnderflowException {
        return Short.toUnsignedInt(buf.order(ByteOrder.BIG_ENDIAN).getShort());
    }

    /**
     * Read 2 bytes from the byte array (starting at the offset) as unsigned 16-bit integer in big endian format.
     * @param bytes buffer to be read from
     * @param offset offset into the buffer
     * @throws ArrayIndexOutOfBoundsException if offset points outside of the buffer, or
     *                                        if the read value extends beyond the remaining bytes of the buffer
     */
    public static int readUint16BE(byte[] bytes, int offset) throws ArrayIndexOutOfBoundsException {
        check(offset >= 0 && offset <= bytes.length - 2, () ->
                new ArrayIndexOutOfBoundsException(offset));
        return readUint16BE(ByteBuffer.wrap(bytes, offset, bytes.length - offset));
    }

    /**
     * Read 4 bytes from the buffer as unsigned 32-bit integer in little endian format.
     * @param buf buffer to be read from
     * @throws BufferUnderflowException if the read value extends beyond the remaining bytes of the buffer
     */
    public static long readUint32(ByteBuffer buf) throws BufferUnderflowException {
        return Integer.toUnsignedLong(buf.order(ByteOrder.LITTLE_ENDIAN).getInt());
    }

    /**
     * Read 4 bytes from the byte array (starting at the offset) as signed 32-bit integer in little endian format.
     * @param buf buffer to be read from
     * @return read integer
     * @throws BufferUnderflowException if the read value extends beyond the remaining bytes of the buffer
     */
    public static int readInt32(ByteBuffer buf) throws BufferUnderflowException {
        return buf.order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * Read 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in little endian format.
     * @param bytes buffer to be read from
     * @param offset offset into the buffer
     * @throws ArrayIndexOutOfBoundsException if offset points outside of the buffer, or
     *                                        if the read value extends beyond the remaining bytes of the buffer
     */
    public static long readUint32(byte[] bytes, int offset) throws ArrayIndexOutOfBoundsException {
        check(offset >= 0 && offset <= bytes.length - 4, () ->
                new ArrayIndexOutOfBoundsException(offset));
        return readUint32(ByteBuffer.wrap(bytes, offset, bytes.length - offset));
    }

    /**
     * Read 4 bytes from the buffer as unsigned 32-bit integer in big endian format.
     * @param buf buffer to be read from
     * @throws BufferUnderflowException if the read value extends beyond the remaining bytes of the buffer
     */
    public static long readUint32BE(ByteBuffer buf) throws BufferUnderflowException {
        return Integer.toUnsignedLong(buf.order(ByteOrder.BIG_ENDIAN).getInt());
    }

    /**
     * Read 4 bytes from the byte array (starting at the offset) as unsigned 32-bit integer in big endian format.
     * @param bytes buffer to be read from
     * @param offset offset into the buffer
     * @throws ArrayIndexOutOfBoundsException if offset points outside of the buffer, or
     *                                        if the read value extends beyond the remaining bytes of the buffer
     */
    public static long readUint32BE(byte[] bytes, int offset) throws ArrayIndexOutOfBoundsException {
        check(offset >= 0 && offset <= bytes.length - 4, () ->
                new ArrayIndexOutOfBoundsException(offset));
        return readUint32BE(ByteBuffer.wrap(bytes, offset, bytes.length - offset));
    }

    /**
     * Read 8 bytes from the buffer as signed 64-bit integer in little endian format.
     * @param buf buffer to be read from
     * @throws BufferUnderflowException if the read value extends beyond the remaining bytes of the buffer
     */
    public static long readInt64(ByteBuffer buf) throws BufferUnderflowException {
        return buf.order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    /**
     * Read 8 bytes from the byte array (starting at the offset) as signed 64-bit integer in little endian format.
     * @param bytes buffer to be read from
     * @param offset offset into the buffer
     * @throws ArrayIndexOutOfBoundsException if offset points outside of the buffer, or
     *                                        if the read value extends beyond the remaining bytes of the buffer
     */
    public static long readInt64(byte[] bytes, int offset) throws ArrayIndexOutOfBoundsException {
        check(offset >= 0 && offset <= bytes.length - 8, () ->
                new ArrayIndexOutOfBoundsException(offset));
        return readInt64(ByteBuffer.wrap(bytes, offset, bytes.length - offset));
    }

    /**
     * Read 2 bytes from the stream as unsigned 16-bit integer in little endian format.
     * @param is stream to be read from
     */
    public static int readUint16(InputStream is) {
        byte[] buf = new byte[2];
        try {
            is.read(buf);
            return readUint16(ByteBuffer.wrap(buf));
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Read 4 bytes from the stream as unsigned 32-bit integer in little endian format.
     * @param is stream to be read from
     */
    public static long readUint32(InputStream is) {
        byte[] buf = new byte[4];
        try {
            is.read(buf);
            return readUint32(ByteBuffer.wrap(buf));
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Returns a copy of the given byte array in reverse order.
     */
    public static byte[] reverseBytes(byte[] bytes) {
        // We could use the XOR trick here but it's easier to understand if we don't. If we find this is really a
        // performance issue the matter can be revisited.
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = bytes[bytes.length - 1 - i];
        return buf;
    }

    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format (with a sign bit).
     * @param hasLength can be set to false if the given array is missing the 4 byte length field
     */
    public static BigInteger decodeMPI(byte[] mpi, boolean hasLength) {
        byte[] buf;
        if (hasLength) {
            int length = (int) readUint32BE(mpi, 0);
            buf = new byte[length];
            System.arraycopy(mpi, 4, buf, 0, length);
        } else
            buf = mpi;
        if (buf.length == 0)
            return BigInteger.ZERO;
        boolean isNegative = (buf[0] & 0x80) == 0x80;
        if (isNegative)
            buf[0] &= 0x7f;
        BigInteger result = new BigInteger(buf);
        return isNegative ? result.negate() : result;
    }

    /**
     * MPI encoded numbers are produced by the OpenSSL BN_bn2mpi function. They consist of
     * a 4 byte big endian length field, followed by the stated number of bytes representing
     * the number in big endian format (with a sign bit).
     * @param includeLength indicates whether the 4 byte length field should be included
     */
    public static byte[] encodeMPI(BigInteger value, boolean includeLength) {
        if (value.equals(BigInteger.ZERO)) {
            if (!includeLength)
                return new byte[] {};
            else
                return new byte[] {0x00, 0x00, 0x00, 0x00};
        }
        boolean isNegative = value.signum() < 0;
        if (isNegative)
            value = value.negate();
        byte[] array = value.toByteArray();
        int length = array.length;
        if ((array[0] & 0x80) == 0x80)
            length++;
        if (includeLength) {
            byte[] result = new byte[length + 4];
            System.arraycopy(array, 0, result, length - array.length + 3, array.length);
            writeInt32BE(length, result, 0);
            if (isNegative)
                result[4] |= 0x80;
            return result;
        } else {
            byte[] result;
            if (length != array.length) {
                result = new byte[length];
                System.arraycopy(array, 0, result, 1, array.length);
            }else
                result = array;
            if (isNegative)
                result[0] |= 0x80;
            return result;
        }
    }

    /**
     * <p>The "compact" format is a representation of a whole number N using an unsigned 32 bit number similar to a
     * floating point format. The most significant 8 bits are the unsigned exponent of base 256. This exponent can
     * be thought of as "number of bytes of N". The lower 23 bits are the mantissa. Bit number 24 (0x800000) represents
     * the sign of N. Therefore, N = (-1^sign) * mantissa * 256^(exponent-3).</p>
     *
     * <p>Satoshi's original implementation used BN_bn2mpi() and BN_mpi2bn(). MPI uses the most significant bit of the
     * first byte as sign. Thus 0x1234560000 is compact 0x05123456 and 0xc0de000000 is compact 0x0600c0de. Compact
     * 0x05c0de00 would be -0x40de000000.</p>
     *
     * <p>Bitcoin only uses this "compact" format for encoding difficulty targets, which are unsigned 256bit quantities.
     * Thus, all the complexities of the sign bit and using base 256 are probably an implementation accident.</p>
     */
    public static BigInteger decodeCompactBits(long compact) {
        int size = ((int) (compact >> 24)) & 0xFF;
        byte[] bytes = new byte[4 + size];
        bytes[3] = (byte) size;
        if (size >= 1) bytes[4] = (byte) ((compact >> 16) & 0xFF);
        if (size >= 2) bytes[5] = (byte) ((compact >> 8) & 0xFF);
        if (size >= 3) bytes[6] = (byte) (compact & 0xFF);
        return decodeMPI(bytes, true);
    }

    /**
     * @see #decodeCompactBits(long)
     */
    public static long encodeCompactBits(BigInteger value) {
        long result;
        int size = value.toByteArray().length;
        if (size <= 3)
            result = value.longValue() << 8 * (3 - size);
        else
            result = value.shiftRight(8 * (size - 3)).longValue();
        // The 0x00800000 bit denotes the sign.
        // Thus, if it is already set, divide the mantissa by 256 and increase the exponent.
        if ((result & 0x00800000L) != 0) {
            result >>= 8;
            size++;
        }
        result |= (long) size << 24;
        result |= value.signum() == -1 ? 0x00800000 : 0;
        return result;
    }

    /** Checks if the given bit is set in data, using little endian (not the same as Java native big endian) */
    public static boolean checkBitLE(byte[] data, int index) {
        return (data[index >>> 3] & bitMask[7 & index]) != 0;
    }

    /** Sets the given bit in data to one, using little endian (not the same as Java native big endian) */
    public static void setBitLE(byte[] data, int index) {
        data[index >>> 3] |= bitMask[7 & index];
    }

    /**
     * Provides a byte array comparator.
     * @return A comparator for byte[]
     */
    public static Comparator<byte[]> arrayUnsignedComparator() {
        return ARRAY_UNSIGNED_COMPARATOR;
    }

    // In Java 9, this can be replaced with Arrays.compareUnsigned()
    private static final Comparator<byte[]> ARRAY_UNSIGNED_COMPARATOR = (a, b) -> {
        int minLength = Math.min(a.length, b.length);
        for (int i = 0; i < minLength; i++) {
            int result = compareUnsigned(a[i], b[i]);
            if (result != 0) {
                return result;
            }
        }
        return a.length - b.length;
    };

    private static int compareUnsigned(byte a, byte b) {
        return Byte.toUnsignedInt(a) - Byte.toUnsignedInt(b);
    }

    /**
     * Concatenate two byte arrays
     * @param b1 first byte array
     * @param b2 second byte array
     * @return new concatenated byte array
     */
    public static byte[] concat(byte[] b1, byte[] b2) {
        byte[] result = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, result, 0, b1.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);
        return result;
    }




}
