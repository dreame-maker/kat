/*
 * Copyright 2022 Kat+ Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package plus.kat.kernel;

import plus.kat.anno.NotNull;
import plus.kat.anno.Nullable;

import javax.crypto.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.nio.charset.Charset;

import plus.kat.crash.*;
import plus.kat.stream.*;

import static plus.kat.stream.Binary.*;
import static java.nio.charset.StandardCharsets.*;

/**
 * @author kraity
 * @since 0.0.1
 */
public abstract class Chain implements CharSequence, Comparable<CharSequence> {

    protected int count;
    protected byte[] value;

    protected int hash;
    protected Bucket bucket;

    protected int asset;
    protected String backup;

    /**
     * empty bytes
     */
    public static final byte[]
        EMPTY_BYTES = {};

    /**
     * empty chars
     */
    public static final char[]
        EMPTY_CHARS = {};

    /**
     * Constructs an empty chain
     */
    public Chain() {
        value = EMPTY_BYTES;
    }

    /**
     * Constructs a chain with the specified size
     *
     * @param size the initial capacity
     */
    public Chain(
        int size
    ) {
        value = size > 0 ? new byte[size] : EMPTY_BYTES;
    }

    /**
     * Constructs an empty chain
     *
     * @param fixed the specified state
     */
    public Chain(
        boolean fixed
    ) {
        value = EMPTY_BYTES;
        if (fixed) {
            asset |= Integer.MIN_VALUE;
        }
    }

    /**
     * Constructs a chain with the specified data
     *
     * @param data the initial byte array
     */
    public Chain(
        @Nullable byte[] data
    ) {
        value = data == null ? EMPTY_BYTES : data;
    }

    /**
     * Constructs a chain with the specified chain
     *
     * @param chain the specified {@link Chain} to be used
     */
    public Chain(
        @Nullable Chain chain
    ) {
        if (chain == null) {
            value = EMPTY_BYTES;
        } else {
            value = chain.toBytes();
            count = value.length;
        }
    }

    /**
     * Constructs an empty chain with the specified bucket
     *
     * @param bucket the specified {@link Bucket} to be used
     */
    public Chain(
        @Nullable Bucket bucket
    ) {
        value = EMPTY_BYTES;
        this.bucket = bucket;
    }

    /**
     * Returns the hash code of this {@link Chain}
     * <p>
     * Similar to {@link String#hashCode()} when the chain is the {@code Latin1}
     *
     * @return a hash code value for this {@link Chain}
     * @see String#hashCode()
     */
    @Override
    public int hashCode() {
        int h = hash;
        if ((asset & 1) == 0) {
            h = 0;
            int size = count;
            if (size != 0) {
                byte[] v = value;
                for (int i = 0; i < size; i++) {
                    h = 31 * h + v[i];
                }
            }
            hash = h;
            asset |= 1;
        }
        return h;
    }

    /**
     * Compares a {@link Chain} or {@link CharSequence} to this chain
     * as the {@code Latin1} to determine if their contents are the same
     *
     * @param o the {@link Object} to compare this {@link Chain} against
     */
    @Override
    public boolean equals(
        @Nullable Object o
    ) {
        if (this == o) {
            return true;
        }

        if (o instanceof Chain) {
            Chain c = (Chain) o;
            int range = c.count;
            if (count == range) {
                byte[] it = value;
                byte[] dest = c.value;
                for (int i = 0; i < range; i++) {
                    if (it[i] != dest[i]) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        if (o instanceof CharSequence) {
            CharSequence c = (CharSequence) o;
            int range = c.length();
            if (count == range) {
                byte[] it = value;
                for (int i = 0; i < range; i++) {
                    if (c.charAt(i) !=
                        (char) (it[i] & 0xFF)) {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if and only if
     * the chain is describing class
     *
     * @since 0.0.5
     */
    public boolean isSpace() {
        return false;
    }

    /**
     * Returns true if and only if
     * this chain is finally unchanged
     *
     * @since 0.0.5
     */
    public final boolean isFixed() {
        // asset & 0x80000000
        return asset < 0;
    }

    /**
     * Returns a {@link CharSequence} that
     * is a subsequence of this {@link Chain}
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     */
    @Override
    public CharSequence subSequence(
        int start, int end
    ) {
        if (end <= count) {
            return new String(
                value, start, end - start, UTF_8
            );
        } else {
            throw new StringIndexOutOfBoundsException(end);
        }
    }

    /**
     * Compares this chain as the {@code Latin1}
     * with the specified {@link CharSequence} for order
     *
     * @param o the specified chars to be compared
     * @throws NullPointerException If the specified {@code chars} is null
     * @see String#compareTo(String)
     */
    @Override
    public int compareTo(
        @NotNull CharSequence o
    ) {
        if (this == o) {
            return 0;
        }

        int len1, len2, res,
            limit = Math.min(
                len1 = count,
                len2 = o.length()
            );

        byte[] it = value;
        for (int i = 0; i < limit; i++) {
            res = (it[i] & 0xFF) - (
                o.charAt(i) & 0xFFFF
            );
            if (res != 0) {
                return res;
            }
        }

        return len1 - len2;
    }

    /**
     * Compares this chain as the {@code Latin1} with the specified {@code byte} value
     *
     * <pre>{@code
     *   byte b = 'k';
     *   new Value("k").is(b); // true
     *   new Value("kat").is(b); // false
     * }</pre>
     *
     * @param b the specified value to be compared
     */
    public boolean is(
        byte b
    ) {
        return count == 1 && value[0] == b;
    }

    /**
     * Compares this chain as the {@code UTF8} with the specified {@code char} value
     *
     * <pre>{@code
     *   char c = 'k';
     *   new Value("k").is(c); // true
     *   new Value("kat").is(c); // false
     * }</pre>
     *
     * @param c the specified value to be compared
     */
    public boolean is(
        char c
    ) {
        int l = count;
        byte[] it = value;

        // U+0000 ~ U+007F
        if (c < 0x80) {
            if (l != 1) {
                return false;
            }

            return it[0] == (byte) c;
        }

        // U+0080 ~ U+07FF
        else if (c < 0x800) {
            if (l != 2) {
                return false;
            }

            return it[0] == (byte) ((c >> 6) | 0xC0)
                && it[1] == (byte) ((c & 0x3F) | 0x80);
        }

        // U+10000 ~ U+10FFFF
        // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
        else if (c >= 0xD800 && c <= 0xDFFF) {
            return false;
        }

        // U+0800 ~ U+FFFF
        else {
            if (l != 3) {
                return false;
            }

            return it[0] == (byte) ((c >> 12) | 0xE0)
                && it[1] == (byte) (((c >> 6) & 0x3F) | 0x80)
                && it[2] == (byte) ((c & 0x3F) | 0x80);
        }
    }

    /**
     * Compares the specified index value of this chain
     * as the {@code Latin1} with the specified {@code byte} value
     *
     * <pre>{@code
     *   byte b = 'k';
     *   Chain c0 = new Value("k");
     *
     *   c0.is(0, b); // true
     *   c0.is(1, b); // false
     *
     *   Chain c1 = new Value("kat");
     *   c1.is(0, b); // true
     *   c1.is(1, b); // false
     *
     *   byte c = 't';
     *   c1.is(2, c); // true
     *   c1.is(1, c); // false
     * }</pre>
     *
     * @param i the specified index
     * @param b the specified value to be compared
     */
    public boolean is(
        int i, byte b
    ) {
        return 0 <= i && i < count && value[i] == b;
    }

    /**
     * Compares the specified index value of the chain
     * as the {@code UTF8} with the specified {@code char} value
     *
     * <pre>{@code
     *   char b = 'k';
     *   Chain c0 = new Value("k");
     *
     *   c0.is(0, b); // true
     *   c0.is(1, b); // false
     *
     *   Chain c1 = new Value("kat");
     *   c1.is(0, b); // true
     *   c1.is(1, b); // false
     *
     *   char c = 't';
     *   c1.is(2, c); // true
     *   c1.is(1, c); // false
     * }</pre>
     *
     * @param i the specified index
     * @param c the specified value to be compared
     */
    public boolean is(
        int i, char c
    ) {
        int l = count;
        if (l <= i || i < 0) {
            return false;
        }

        int m = 0;
        byte[] it = value;

        for (int k = 0; k < l; m++) {
            if (i == m) {
                // U+0000 ~ U+007F
                if (c < 0x80) {
                    return it[k] == (byte) c;
                }

                // U+0080 ~ U+07FF
                else if (c < 0x800) {
                    if (k + 2 > l) {
                        return false;
                    }

                    return it[k] == (byte) ((c >> 6) | 0xC0)
                        && it[k + 1] == (byte) ((c & 0x3F) | 0x80);
                }

                // U+10000 ~ U+10FFFF
                // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
                else if (c >= 0xD800 && c <= 0xDFFF) {
                    if (k + 2 >= l ||
                        c > 0xDBFF) {
                        return false;
                    }

                    byte b2 = it[k + 1];
                    byte b3 = it[k + 2];
                    return c == (char) (
                        ((0xD8 | (it[k] & 0x03)) << 8) |
                            ((((b2 - 0x10 >> 2)) & 0x0F) << 4) |
                            (((b2 & 0x03) << 2) | ((b3 >> 4) & 0x03))
                    );
                }

                // U+0800 ~ U+FFFF
                else {
                    if (k + 3 > l) {
                        return false;
                    }

                    return it[k] == (byte) ((c >> 12) | 0xE0)
                        && it[k + 1] == (byte) (((c >> 6) & 0x3F) | 0x80)
                        && it[k + 2] == (byte) ((c & 0x3F) | 0x80);
                }
            }

            // next byte
            byte b = it[k];

            // U+0000 ~ U+007F
            // 0xxxxxxx
            if (b >= 0) {
                k++;
            }

            // U+0080 ~ U+07FF
            // 110xxxxx 10xxxxxx
            else if ((b >> 5) == -2) {
                k += 2;
            }

            // U+0800 ~ U+FFFF
            // 1110xxxx 10xxxxxx 10xxxxxx
            else if ((b >> 4) == -2) {
                k += 3;
            }

            // U+10000 ~ U+10FFFF
            // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
            // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            else if ((b >> 3) == -2) {
                if (i != ++m) {
                    k += 4;
                    continue;
                }

                if (k + 3 >= l ||
                    c < 0xDC00 ||
                    c > 0xDFFF) {
                    return false;
                }

                byte b3 = it[k + 2];
                byte b4 = it[k + 3];
                return c == (char) (
                    ((0xDC | ((b3 >> 2) & 0x03)) << 8) |
                        ((((b3 & 0x3) << 2) | ((b4 >> 4) & 0x03)) << 4) | (b4 & 0x0F)
                );
            }

            // beyond the current range
            else {
                return false;
            }
        }

        return false;
    }

    /**
     * Compares this chain as the {@code Latin1} with the specified {@code byte} array
     *
     * <pre>{@code
     *   byte[] b = new byte[]{'k'};
     *   new Value("k").is(b); // true
     *   new Value("kat").is(b); // false
     *
     *   byte[] c = new byte[]{'k', 'a', 't'};
     *   new Value("k").is(c); // false
     *   new Value("kat").is(c); // true
     * }</pre>
     *
     * @param bs the specified bytes to compared
     */
    public boolean is(
        @Nullable byte[] bs
    ) {
        if (bs != null) {
            int range = bs.length;
            if (count == range) {
                byte[] it = value;
                for (int i = 0; i < range; i++) {
                    if (it[i] != bs[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Compares this chain as the {@code UTF8} with the specified {@link CharSequence}
     *
     * <pre>{@code
     *   new Value("k").is("k"); // true
     *   new Value("kat").is("k"); // false
     *
     *   new Value("k").is("kat"); // false
     *   new Value("kat").is("kat"); // true
     * }</pre>
     *
     * @param ch the specified chars to be compared
     */
    public boolean is(
        @Nullable CharSequence ch
    ) {
        if (ch == null) {
            return false;
        }

        int l = count;
        int r = ch.length();

        // ensure: r <= l <= 3r
        if (r > l || l > 3 * r) {
            return false;
        }

        int i = 0, j = 0;
        byte[] it = value;

        for (; i < l && j < r; j++) {
            // next char
            char c = ch.charAt(j);

            // U+0000 ~ U+007F
            if (c < 0x80) {
                if (it[i++] != (byte) c) {
                    return false;
                }
            }

            // U+0080 ~ U+07FF
            else if (c < 0x800) {
                if (i + 2 > l) {
                    return false;
                }

                if (it[i++] != (byte) ((c >> 6) | 0xC0) ||
                    it[i++] != (byte) ((c & 0x3F) | 0x80)) {
                    return false;
                }
            }

            // U+10000 ~ U+10FFFF
            // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
            else if (c >= 0xD800 && c <= 0xDFFF) {
                if (i + 4 > l) {
                    return false;
                }

                if (++j >= r) {
                    return false;
                }

                char d = ch.charAt(j);
                if (d < 0xDC00 || d > 0xDFFF) {
                    return false;
                }

                int u = (c << 10) + d - 0x35F_DC00;
                if (it[i++] != (byte) ((u >> 18) | 0xF0) ||
                    it[i++] != (byte) (((u >> 12) & 0x3F) | 0x80) ||
                    it[i++] != (byte) (((u >> 6) & 0x3F) | 0x80) ||
                    it[i++] != (byte) ((u & 0x3F) | 0x80)) {
                    return false;
                }
            }

            // U+0800 ~ U+FFFF
            else {
                if (i + 3 > l) {
                    return false;
                }

                if (it[i++] != (byte) ((c >> 12) | 0xE0) ||
                    it[i++] != (byte) (((c >> 6) & 0x3F) | 0x80) ||
                    it[i++] != (byte) ((c & 0x3F) | 0x80)) {
                    return false;
                }
            }
        }

        return i == l && j == r;
    }

    /**
     * Returns the specified index value of the {@code Latin1} chain
     *
     * <pre>{@code
     *   Chain c = new Value("kat");
     *   byte b0 = c.get(0); // 'k'
     *   byte b1 = c.get(1); // 'a'
     *   byte b2 = c.get(2); // 't'
     *   byte b3 = c.get(3); // ERROR
     *   byte b4 = c.get(-1); // 't'
     *   byte b5 = c.get(-3); // 'k'
     *   byte b6 = c.get(-4); // ERROR
     * }</pre>
     *
     * @param i the specified index
     * @throws ArrayIndexOutOfBoundsException If the specified index is out of range
     */
    public byte get(int i) {
        if (i < 0) {
            i += count;
            if (0 <= i) {
                return value[i];
            }
        } else {
            if (i < count) {
                return value[i];
            }
        }

        throw new ArrayIndexOutOfBoundsException(
            "Index " + i + " out of bounds for length " + count
        );
    }

    /**
     * Returns the specified index value of the {@code Latin1} chain
     *
     * <pre>{@code
     *   Chain c = new Value("kat");
     *   byte def = '$';
     *   byte b0 = c.get(0, def); // 'k'
     *   byte b1 = c.get(1, def); // 'a'
     *   byte b2 = c.get(2, def); // 't'
     *   byte b3 = c.get(3, def); // '$'
     *   byte b4 = c.get(-1, def); // 't'
     *   byte b5 = c.get(-3, def); // 'k'
     *   byte b6 = c.get(-4, def); // '$'
     * }</pre>
     *
     * @param i   the specified index
     * @param def the specified default value
     */
    public byte get(int i, byte def) {
        if (i < 0) {
            i += count;
            return i < 0 ? def : value[i];
        } else {
            return i < count ? value[i] : def;
        }
    }

    /**
     * Returns the specified index value of the {@code Latin1} chain
     *
     * <pre>{@code
     *   Chain c = new Value("kat");
     *   byte b0 = c.at(0); // 'k'
     *   byte b1 = c.at(1); // 'a'
     *   byte b2 = c.at(2); // 't'
     *   byte b3 = c.at(3); // ERROR
     *   byte b4 = c.at(-1); // ERROR
     *   byte b5 = c.at(-3); // ERROR
     *   byte b6 = c.at(-4); // ERROR
     * }</pre>
     *
     * @param i the specified index
     * @throws ArrayIndexOutOfBoundsException If the specified index is negative or out of range
     */
    public byte at(int i) {
        if (i < count) {
            return value[i];
        }

        throw new ArrayIndexOutOfBoundsException(
            "Index " + i + " out of bounds for length " + count
        );
    }

    /**
     * Returns the specified index value of the {@code Latin1} chain
     *
     * <pre>{@code
     *   Chain c = new Value("kat");
     *   char c0 = c.charAt(0); // 'k'
     *   char c1 = c.charAt(1); // 'a'
     *   char c2 = c.charAt(2); // 't'
     *   char c3 = c.charAt(3); // ERROR
     *   char c4 = c.charAt(-1); // ERROR
     *   char c5 = c.charAt(-3); // ERROR
     *   char c6 = c.charAt(-4); // ERROR
     * }</pre>
     *
     * @param i the specified index
     * @throws ArrayIndexOutOfBoundsException If the specified index is negative or out of range
     */
    @Override
    public char charAt(int i) {
        if (i < count) {
            return (char) (
                value[i] & 0xFF
            );
        }

        throw new ArrayIndexOutOfBoundsException(
            "Index " + i + " out of bounds for length " + count
        );
    }

    /**
     * Returns the charset of this {@link Chain}
     *
     * @see Charset
     * @since 0.0.5
     */
    @NotNull
    public Charset charset() {
        return UTF_8;
    }

    /**
     * Returns the length of this {@code Latin1} chain
     */
    @Override
    public int length() {
        return count;
    }

    /**
     * Returns the length of internal {@code byte} array
     *
     * @see Chain#length()
     */
    public int capacity() {
        return value.length;
    }

    /**
     * Returns true if and only if
     * the count of this chain is {@code 0}
     *
     * <pre>{@code
     *   new Value().isEmpty()          = true
     *   new Value("").isEmpty()        = true
     *   new Value(" ").isEmpty()       = false
     *   new Value("kat").isEmpty()     = false
     *   new Value("  kat  ").isEmpty() = false
     * }</pre>
     *
     * @see Chain#isNotEmpty()
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns true if this chain is empty
     * or contains only white space codepoints
     * <p>
     * White space: {@code 9,10,11,12,13,28,29,30,31,32}
     *
     * <pre>{@code
     *   new Value().isBlank()          = true
     *   new Value("").isBlank()        = true
     *   new Value(" ").isBlank()       = true
     *   new Value("  ").isBlank()      = true
     *   new Value("kat").isBlank()     = false
     *   new Value("  kat  ").isBlank() = false
     * }</pre>
     *
     * @see Chain#isNotBlank()
     * @see Character#isWhitespace(char)
     */
    public boolean isBlank() {
        int l = count;
        if (l != 0) {
            byte[] it = value;
            for (int i = 0; i < l; i++) {
                byte b = it[i];
                if (b > 32 || b < 9) {
                    return false;
                }
                if (13 < b && b < 28) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns true if the chain is
     * non-empty and contains only numbers
     * <p>
     * Digit code: {@code 48,49,50,51,52,53,54,55,56,57}
     *
     * <pre>{@code
     *   new Value("0").isDigit()    = true
     *   new Value("01").isDigit()   = true
     *   new Value("123").isDigit()  = true
     *   new Value("").isDigit()     = false
     *   new Value("  ").isDigit()   = false
     *   new Value("12 3").isDigit() = false
     *   new Value("abc4").isDigit() = false
     *   new Value("12-3").isDigit() = false
     *   new Value("12.3").isDigit() = false
     *   new Value("-1.2").isDigit() = false
     *   new Value("-123").isDigit() = false
     * }</pre>
     *
     * @see Chain#isNotDigit()
     * @see Character#isDigit(char)
     * @since 0.0.5
     */
    public boolean isDigit() {
        int l = count;
        if (l == 0) {
            return false;
        }

        byte[] it = value;
        for (int i = 0; i < l; i++) {
            byte b = it[i];
            if (b < 48 || b > 57) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if and only if
     * the count of this chain is not {@code 0}
     *
     * @see Chain#isEmpty()
     */
    public boolean isNotEmpty() {
        return count != 0;
    }

    /**
     * Returns false if this chain is empty
     * or contains only white space codepoints
     * <p>
     * White space: {@code 9,10,11,12,13,28,29,30,31,32}
     *
     * @see Chain#isBlank()
     */
    public boolean isNotBlank() {
        int l = count;
        if (l != 0) {
            byte[] it = value;
            for (int i = 0; i < l; i++) {
                byte b = it[i];
                if (b > 32 || b < 9) {
                    return true;
                }
                if (13 < b && b < 28) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns false if the chain is
     * non-empty and contains only numbers
     * <p>
     * Digit code: {@code 48,49,50,51,52,53,54,55,56,57}
     *
     * @see Chain#isDigit()
     * @since 0.0.5
     */
    public boolean isNotDigit() {
        int l = count;
        if (l == 0) {
            return true;
        }

        byte[] it = value;
        for (int i = 0; i < l; i++) {
            byte b = it[i];
            if (b < 48 || b > 57) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if and only if this {@code Latin1}
     * chain contains the specified {@code byte} value
     *
     * @param b the byte value to search for
     * @see Chain#indexOf(byte)
     */
    public boolean contains(byte b) {
        return indexOf(b) != -1;
    }

    /**
     * Returns true if and only if this {@code Latin1}
     * chain contains the specified {@code Latin1} sequence
     *
     * @param c the {@link CharSequence} to search for
     * @see Chain#indexOf(CharSequence)
     * @see String#contains(CharSequence)
     */
    public boolean contains(
        @Nullable CharSequence c
    ) {
        return indexOf(c) != -1;
    }

    /**
     * Tests if this {@code Latin1} chain starts with the specified {@code prefix}
     *
     * <pre>{@code
     *   Chain c = new Value("kat");
     *   boolean b = c.startWith("ka"); // true
     *   boolean b = c.startWith("kat.plus"); // false
     * }</pre>
     *
     * @param c the specified prefix to search for
     * @see String#startsWith(String)
     */
    public boolean startWith(
        @Nullable CharSequence c
    ) {
        if (c == null) {
            return false;
        }

        int l = c.length();
        if (count < l) {
            return false;
        }

        char ch;
        byte[] it = value;

        for (int i = 0; i < l; i++) {
            ch = (char) (
                it[i] & 0xFF
            );
            if (ch != c.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests if this {@code Latin1} chain ends with the specified {@code suffix}
     *
     * <pre>{@code
     *   Chain c = new Value("kat");
     *   boolean b = c.endsWith("at"); // true
     *   boolean b = c.endsWith("plus.kat"); // false
     * }</pre>
     *
     * @param c the specified suffix to search for
     * @see String#endsWith(String)
     */
    public boolean endsWith(
        @Nullable CharSequence c
    ) {
        if (c == null) {
            return false;
        }

        int l = c.length();
        int k = count - l;
        if (k < 0) {
            return false;
        }

        char ch;
        byte[] it = value;

        for (int i = 0; i < l; i++, k++) {
            ch = (char) (
                it[k] & 0xFF
            );
            if (ch != c.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the index within this {@code Latin1} chain
     * of the first occurrence of the specified {@code byte} value
     *
     * @param b the specified value to search for
     * @see String#indexOf(int)
     */
    public int indexOf(byte b) {
        int l = count;
        if (l != 0) {
            int i = 0;
            byte[] it = value;
            do {
                if (it[i] == b) {
                    return i;
                }
            } while (++i < l);
        }
        return -1;
    }

    /**
     * Returns the index within this {@code Latin1} chain of the first occurrence
     * of the specified {@code byte} value, starting the search at the specified index
     *
     * @param b the specified value to search for
     * @param i the index to start the search from
     * @see String#indexOf(int, int)
     */
    public int indexOf(
        byte b, int i
    ) {
        int l = count;
        if (l != 0) {
            if (i < 0) {
                i = 0;
            } else if (l <= i) {
                return -1;
            }

            byte[] it = value;
            do {
                if (it[i] == b) {
                    return i;
                }
            } while (++i < l);
        }
        return -1;
    }

    /**
     * Returns the index within this {@code Latin1} chain of
     * the first occurrence of the specified {@code Latin1} subsequence
     *
     * @param c the specified {@code Latin1} sequence
     * @see String#indexOf(String)
     */
    public int indexOf(
        @Nullable CharSequence c
    ) {
        return indexOf(c, 0);
    }

    /**
     * Returns the index within this {@code Latin1} chain of the first occurrence
     * of the specified {@code Latin1} subsequence, starting at the specified index
     *
     * @param c the specified {@code Latin1} sequence
     * @param i the index from which to start the search
     * @see String#indexOf(String, int)
     */
    public int indexOf(
        @Nullable CharSequence c, int i
    ) {
        if (c == null) {
            return -1;
        }

        int m = count;
        int l = c.length();

        if (m <= i) {
            return l == 0 ? m : -1;
        }

        if (i < 0) {
            i = 0;
        }

        if (l == 0) {
            return i;
        }

        int k = m - l;
        if (k < 0) {
            return -1;
        }

        char ch = c.charAt(0);
        if (ch > 0xFF) {
            return -1;
        }

        byte h = (byte) ch;
        byte[] it = value;

        for (; i <= k; i++) {
            if (it[i] != h) {
                continue;
            }

            int i1 = i, i2 = 0;
            while (++i2 < l) {
                ch = (char) (it[++i1] & 0xFF);
                if (ch != c.charAt(i2)) break;
            }
            if (i2 == l) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the index within this {@code Latin1} chain
     * of the last occurrence of the specified {@code byte} value
     *
     * @param b the specified value to search for
     * @see String#lastIndexOf(int)
     */
    public int lastIndexOf(byte b) {
        int i = count - 1;
        if (0 <= i) {
            byte[] it = value;
            do {
                if (it[i] == b) {
                    return i;
                }
            } while (0 <= --i);
        }
        return -1;
    }

    /**
     * Returns the index within this {@code Latin1} chain of the last occurrence of
     * the specified {@code byte} value, searching backward starting at the specified index
     *
     * @param b the specified value to search for
     * @param i the index from which to start the search
     * @see String#lastIndexOf(int, int)
     */
    public int lastIndexOf(
        byte b, int i
    ) {
        int l = count;
        if (l <= i) {
            i = l - 1;
        }
        if (0 <= i) {
            byte[] it = value;
            do {
                if (it[i] == b) {
                    return i;
                }
            } while (0 <= --i);
        }
        return -1;
    }

    /**
     * Returns the index within this {@code Latin1} chain of
     * the last occurrence of the specified {@code Latin1} subsequence
     *
     * @param c the specified {@code Latin1} sequence
     * @see String#lastIndexOf(String)
     */
    public int lastIndexOf(
        @Nullable CharSequence c
    ) {
        return lastIndexOf(c, count);
    }

    /**
     * Returns the index within this {@code Latin1} chain of the last occurrence of
     * the specified {@code Latin1} {@code sequence}, searching backward starting at the specified index
     *
     * @param c the specified {@code Latin1} sequence
     * @param i the index from which to start the search
     * @see String#lastIndexOf(String, int)
     */
    public int lastIndexOf(
        @Nullable CharSequence c, int i
    ) {
        if (c == null) {
            return -1;
        }

        int l = c.length();
        int r = count - l;

        if (i > r) {
            i = r;
        }

        if (i < 0) {
            return -1;
        }

        if (l == 0) {
            return i;
        }

        char ch = c.charAt(0);
        if (ch > 0xFF) {
            return -1;
        }

        byte h = (byte) ch;
        byte[] it = value;

        for (; 0 <= i; --i) {
            if (it[i] != h) {
                continue;
            }

            int i1 = i, i2 = 0;
            while (++i2 < l) {
                ch = (char) (
                    it[++i1] & 0xFF
                );
                if (ch != c.charAt(i2)) break;
            }
            if (i2 == l) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Copy this {@code Latin1} chain into a new {@code byte} array
     *
     * @since 0.0.4
     */
    @NotNull
    public byte[] toBytes() {
        int size = count;
        if (size != 0) {
            byte[] copy = new byte[size];
            System.arraycopy(
                value, 0, copy, 0, size
            );
            return copy;
        }
        return EMPTY_BYTES;
    }

    /**
     * Copy this {@code Latin1} chain into a new {@code byte} array
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @throws IndexOutOfBoundsException If the start is negative or the end out of range
     * @since 0.0.4
     */
    @NotNull
    public byte[] toBytes(
        int start, int end
    ) {
        int length = end - start;
        if (0 <= start && 0 <= length && end <= count) {
            if (length != 0) {
                byte[] copy = new byte[length];
                System.arraycopy(
                    value, start, copy, 0, length
                );
                return copy;
            }
            return EMPTY_BYTES;
        } else {
            throw new IndexOutOfBoundsException(
                "Unexpectedly, start: " + start
                    + " end: " + end + " size: " + count
            );
        }
    }

    /**
     * Copy this {@code UTF8} chain into a new {@code char} array
     *
     * @since 0.0.4
     */
    @NotNull
    public char[] toChars() {
        int size = count;
        if (size == 0) {
            return EMPTY_CHARS;
        }
        return Convert.toChars(
            value, 0, size
        );
    }

    /**
     * Copy this {@code UTF8} chain into a new {@code char} array
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @throws IndexOutOfBoundsException If the start is negative or the end out of range
     * @since 0.0.4
     */
    @NotNull
    public char[] toChars(
        int start, int end
    ) {
        int length = end - start;
        if (0 <= start && 0 <= length && end <= count) {
            if (length == 0) {
                return EMPTY_CHARS;
            }
            return Convert.toChars(
                value, start, end
            );
        } else {
            throw new IndexOutOfBoundsException(
                "Unexpectedly, start: " + start
                    + " end: " + end + " size: " + count
            );
        }
    }

    /**
     * Writes to the {@link OutputStream} using the internal value of this {@link Chain}
     *
     * @throws NullPointerException If the specified {@code stream} is null
     * @see Chain#update(OutputStream, int, int)
     * @since 0.0.2
     */
    public void update(
        @NotNull OutputStream s
    ) throws IOException {
        update(
            s, 0, count
        );
    }

    /**
     * Writes to the {@link OutputStream} using the internal value of this {@link Chain}
     *
     * @param i the specified offset
     * @param l the specified length
     * @throws NullPointerException      If the specified {@code stream} is null
     * @throws IndexOutOfBoundsException If the offset is negative or the length out of range
     * @see OutputStream#write(int)
     * @see OutputStream#write(byte[], int, int)
     * @since 0.0.2
     */
    public void update(
        @NotNull OutputStream s, int i, int l
    ) throws IOException {
        if (i <= count - l) {
            if (0 <= asset) {
                s.write(
                    value, i, l
                );
            } else {
                if (0 <= i && 0 <= l) {
                    byte[] it = value;
                    while (i < l) {
                        s.write(it[i++]);
                    }
                } else {
                    throw new IndexOutOfBoundsException(
                        "offset:" + i + ", length:" + l
                    );
                }
            }
        } else {
            throw new IndexOutOfBoundsException(
                "Specified offset(" + i + ")/length("
                    + l + ") index is out of bounds: " + count
            );
        }
    }

    /**
     * Updates the {@link Mac} using the internal value of this {@link Chain}
     *
     * @throws NullPointerException If the specified {@code mac} is null
     * @see Mac#update(byte[], int, int)
     */
    public void update(
        @NotNull Mac m
    ) {
        m.update(
            value, 0, count
        );
    }

    /**
     * Updates the {@link Mac} using the internal value of this {@link Chain}
     *
     * @param i the specified offset
     * @param l the specified length
     * @throws NullPointerException     If the specified {@code mac} is null
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see Mac#update(byte[], int, int)
     */
    public void update(
        @NotNull Mac m, int i, int l
    ) {
        if (i <= count - l) {
            m.update(
                value, i, l
            );
        } else {
            throw new IllegalArgumentException(
                "Specified offset(" + i + ")/length("
                    + l + ") index is out of bounds: " + count
            );
        }
    }

    /**
     * Updates the {@link Signature} using the internal value of this {@link Chain}
     *
     * @throws NullPointerException If the specified {@code signature} is null
     * @see Signature#update(byte[], int, int)
     */
    public void update(
        @NotNull Signature s
    ) throws SignatureException {
        s.update(
            value, 0, count
        );
    }

    /**
     * Updates the {@link Signature} using the internal value of this {@link Chain}
     *
     * @param i the specified offset
     * @param l the specified length
     * @throws NullPointerException     If the specified {@code signature} is null
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see Signature#update(byte[], int, int)
     */
    public void update(
        @NotNull Signature s, int i, int l
    ) throws SignatureException {
        if (i <= count - l) {
            s.update(
                value, i, l
            );
        } else {
            throw new IllegalArgumentException(
                "Specified offset(" + i + ")/length("
                    + l + ") index is out of bounds: " + count
            );
        }
    }

    /**
     * Updates the {@link MessageDigest} using the internal value of this {@link Chain}
     *
     * @throws NullPointerException If the specified {@code digest} is null
     * @see MessageDigest#update(byte[], int, int)
     */
    public void update(
        @NotNull MessageDigest m
    ) {
        m.update(
            value, 0, count
        );
    }

    /**
     * Updates the {@link MessageDigest} using the internal value of this {@link Chain}
     *
     * @param i the specified offset
     * @param l the specified length
     * @throws NullPointerException     If the specified {@code digest} is null
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see MessageDigest#update(byte[], int, int)
     */
    public void update(
        @NotNull MessageDigest m, int i, int l
    ) {
        if (i <= count - l) {
            m.update(
                value, i, l
            );
        } else {
            throw new IllegalArgumentException(
                "Specified offset(" + i + ")/length("
                    + l + ") index is out of bounds: " + count
            );
        }
    }

    /**
     * Updates the {@link Cipher} using the internal value of this {@link Chain}
     *
     * @throws NullPointerException If the specified {@code cipher} is null
     * @see Cipher#update(byte[], int, int)
     */
    @Nullable
    public byte[] update(
        @NotNull Cipher c
    ) {
        return c.update(
            value, 0, count
        );
    }

    /**
     * Updates the {@link Cipher} using the internal value of this {@link Chain}
     *
     * @param i the specified offset
     * @param l the specified length
     * @throws NullPointerException     If the specified {@code cipher} is null
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see Cipher#update(byte[], int, int)
     */
    @Nullable
    public byte[] update(
        @NotNull Cipher c, int i, int l
    ) {
        if (i <= count - l) {
            return c.update(
                value, i, l
            );
        } else {
            throw new IllegalArgumentException(
                "Specified offset(" + i + ")/length("
                    + l + ") index is out of bounds: " + count
            );
        }
    }

    /**
     * Completes the {@link Cipher} using the internal value of this {@link Chain}
     *
     * @throws NullPointerException If the specified {@code cipher} is null
     * @see Cipher#doFinal(byte[], int, int)
     */
    @Nullable
    public byte[] doFinal(
        @NotNull Cipher c
    ) throws IllegalBlockSizeException, BadPaddingException {
        return c.doFinal(
            value, 0, count
        );
    }

    /**
     * Completes the {@link Cipher} using the internal value of this {@link Chain}
     *
     * @param i the specified offset
     * @param l the specified length
     * @throws NullPointerException     If the specified {@code cipher} is null
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see Cipher#doFinal(byte[], int, int)
     */
    @Nullable
    public byte[] doFinal(
        @NotNull Cipher c, int i, int l
    ) throws IllegalBlockSizeException, BadPaddingException {
        if (i <= count - l) {
            return c.doFinal(
                value, i, l
            );
        } else {
            throw new IllegalArgumentException(
                "Specified offset(" + i + ")/length("
                    + l + ") index is out of bounds: " + count
            );
        }
    }

    /**
     * Completes the {@link Base64} using the internal value of this {@link Chain}
     *
     * @throws NullPointerException     If the specified {@code base64} is null
     * @throws IllegalArgumentException If the offset is negative or the length out of range
     * @see Base64#encode(byte[], int, int)
     * @since 0.0.5
     */
    @NotNull
    public byte[] encrypt(
        @NotNull Base64 base64
    ) {
        return base64.encode(
            value, 0, count
        );
    }

    /**
     * Completes the {@link Base64} using the internal value of this {@link Chain}
     *
     * @param i the specified offset
     * @param l the specified length
     * @throws NullPointerException      If the specified {@code base64} is null
     * @throws IndexOutOfBoundsException If the offset is negative or the length out of range
     * @see Base64#encode(byte[], int, int)
     * @since 0.0.5
     */
    @NotNull
    public byte[] encrypt(
        @NotNull Base64 base64, int i, int l
    ) {
        if (i <= count - l && 0 <= i && 0 <= l) {
            return base64.encode(
                value, i, l
            );
        } else {
            throw new IndexOutOfBoundsException(
                "Specified offset(" + i + ")/length("
                    + l + ") index is out of bounds: " + count
            );
        }
    }

    /**
     * Completes the {@link Base64} using the internal value of this {@link Chain}
     *
     * @throws NullPointerException If the specified {@code base64} is null
     * @see Base64#decode(byte[], int, int)
     * @since 0.0.5
     */
    @NotNull
    public byte[] decrypt(
        @NotNull Base64 base64
    ) {
        return base64.decode(
            value, 0, count
        );
    }

    /**
     * Completes the {@link Base64} using the internal value of this {@link Chain}
     *
     * @param i the specified offset
     * @param l the specified length
     * @throws NullPointerException      If the specified {@code base64} is null
     * @throws IndexOutOfBoundsException If the offset is negative or the length out of range
     * @see Base64#decode(byte[], int, int)
     * @since 0.0.5
     */
    @NotNull
    public byte[] decrypt(
        @NotNull Base64 base64, int i, int l
    ) {
        if (i <= count - l && 0 <= i && 0 <= l) {
            return base64.decode(
                value, i, l
            );
        } else {
            throw new IndexOutOfBoundsException(
                "Specified offset(" + i + ")/length("
                    + l + ") index is out of bounds: " + count
            );
        }
    }

    /**
     * Returns a lowercase {@code MD5} of this {@link Chain}
     *
     * @throws FatalCrash If the MD5 algo is not supported
     */
    @NotNull
    public String digest() {
        return digest(
            "MD5", 0, count
        );
    }

    /**
     * Returns a lowercase message digest of this {@link Chain}
     *
     * @param algo the name of the algorithm requested
     * @throws FatalCrash If the specified algo is not supported
     * @see MessageDigest
     * @see Binary#toLower(byte[])
     * @see Chain#digest(String, int, int)
     */
    @NotNull
    public String digest(
        @NotNull String algo
    ) {
        return digest(
            algo, 0, count
        );
    }

    /**
     * Returns a lowercase message digest of this {@link Chain}
     *
     * @param algo the name of the algorithm requested
     * @param i    the specified offset
     * @param l    the specified length
     * @throws FatalCrash               If the specified algo is not supported
     * @throws IllegalArgumentException If the length out of range
     * @see MessageDigest
     * @see Binary#toLower(byte[])
     */
    @NotNull
    public String digest(
        @NotNull String algo, int i, int l
    ) {
        if (i <= count - l && 0 <= i && 0 <= l) {
            try {
                MessageDigest md =
                    MessageDigest
                        .getInstance(algo);

                md.update(
                    value, i, l
                );

                return toLower(
                    md.digest()
                );
            } catch (NoSuchAlgorithmException e) {
                throw new FatalCrash(
                    algo + " is not supported", e
                );
            }
        } else {
            throw new IllegalArgumentException(
                "Specified offset(" + i + ")/length("
                    + l + ") index is out of bounds: " + count
            );
        }
    }

    /**
     * Returns a {@link Reader} of this {@link Chain}
     *
     * @see Reader
     */
    @NotNull
    public Reader reader() {
        return new Reader(
            this, 0, count
        );
    }

    /**
     * Returns a {@link Reader} of this {@link Chain}
     *
     * @param i the specified index
     * @param l the specified length
     * @throws IndexOutOfBoundsException If the index is negative or the length out of range
     * @see Reader
     */
    @NotNull
    public Reader reader(
        int i, int l
    ) {
        if (i < 0) {
            throw new IndexOutOfBoundsException(
                "The 'index' argument is negative"
            );
        }

        int o = i + l;
        if (o <= count) {
            return new Reader(
                this, i, o
            );
        } else {
            throw new IndexOutOfBoundsException(
                "The 'length' argument is ouf of range"
            );
        }
    }

    /**
     * Returns the value of this {@link Chain} as a {@link String}
     */
    @Override
    @SuppressWarnings("deprecation")
    public String toString() {
        if (count == 0) {
            return "";
        }

        if ((asset & 2) == 0) {
            asset |= 2;
        } else {
            String data = backup;
            if (data != null) {
                return data;
            }
        }

        Charset c = charset();
        if (c != ISO_8859_1) {
            return backup = new String(
                value, 0, count, c
            );
        } else {
            return backup = new String(
                value, 0, 0, count
            );
        }
    }

    /**
     * Returns the value of this {@link Chain} as a {@link String}
     *
     * @param b the specified begin index, inclusive
     * @param e the specified end index, exclusive
     * @throws ArrayIndexOutOfBoundsException If the specified begin/end index is out of range
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public String toString(
        int b, int e
    ) {
        int l = e - b;
        if (0 <= b && 0 <= l &&
            e <= count) {
            if (l == 0) {
                return "";
            }
            if (l != count) {
                Charset c = charset();
                if (c != ISO_8859_1) {
                    return new String(
                        value, b, l, c
                    );
                } else {
                    return new String(
                        value, 0, 0, l
                    );
                }
            }
            return toString();
        } else {
            throw new ArrayIndexOutOfBoundsException(
                "Specified begin(" + b + ")/end(" + e +
                    ") index is out of range: " + count
            );
        }
    }

    /**
     * Returns the value of this {@link Chain} as a {@link String}
     *
     * @param c the specified charset
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public String toString(
        @NotNull Charset c
    ) {
        if (c != charset()) {
            if (count == 0) {
                return "";
            }
            if (c != ISO_8859_1) {
                return new String(
                    value, 0, count, c
                );
            } else {
                return new String(
                    value, 0, 0, count
                );
            }
        }
        return toString();
    }

    /**
     * Returns the value of this {@link Chain} as a {@link String}
     *
     * @param c the specified charset
     * @param b the specified begin index, inclusive
     * @param e the specified end index, exclusive
     * @throws ArrayIndexOutOfBoundsException If the specified begin/end index is out of range
     */
    @NotNull
    @SuppressWarnings("deprecation")
    public String toString(
        @NotNull Charset c, int b, int e
    ) {
        if (c != charset()) {
            int l = e - b;
            if (0 <= b &&
                0 <= l && e <= count) {
                if (l == 0) {
                    return "";
                }
                if (c != ISO_8859_1) {
                    return new String(
                        value, b, l, c
                    );
                } else {
                    return new String(
                        value, 0, b, l
                    );
                }
            } else {
                throw new ArrayIndexOutOfBoundsException(
                    "Specified begin(" + b + ")/end(" + e +
                        ") index is out of range: " + count
                );
            }
        }
        return toString(b, e);
    }

    /**
     * Parses this {@code UTF8} {@link Chain} as a {@code char}
     *
     * @return the specified {@code char}, {@code '\0'} on error
     * @see Convert#toChar(byte[], int, char)
     * @since 0.0.3
     */
    public char toChar() {
        return Convert.toChar(
            value, count, '\0'
        );
    }

    /**
     * Parses this {@code UTF8} {@link Chain} as a {@code char}
     *
     * @return the specified {@code char}, {@code def} value on error
     * @see Convert#toChar(byte[], int, char)
     * @since 0.0.3
     */
    public char toChar(
        char def
    ) {
        return Convert.toChar(
            value, count, def
        );
    }

    /**
     * Parses this {@link Chain} as a signed decimal {@code int}
     *
     * @return the specified {@code int}, {@code '0'} on error
     * @see Convert#toInt(byte[], int, int, int)
     * @since 0.0.3
     */
    public int toInt() {
        return Convert.toInt(
            value, count, 10, 0
        );
    }

    /**
     * Parses this {@link Chain} as a signed decimal {@code int}
     *
     * @return the specified {@code int}, {@code def} value on error
     * @see Convert#toInt(byte[], int, int, int)
     * @since 0.0.3
     */
    public int toInt(
        int def
    ) {
        return Convert.toInt(
            value, count, 10, def
        );
    }

    /**
     * Parses this {@link Chain} as a signed decimal {@code int}
     *
     * @param radix the radix to be used while parsing {@link Chain}
     * @return the specified {@code int}, {@code def} value on error
     * @see Convert#toInt(byte[], int, int, int)
     * @since 0.0.3
     */
    public int toInt(
        int def, int radix
    ) {
        if (radix < 2 || radix > 36) {
            return def;
        }
        return Convert.toInt(
            value, count, radix, def
        );
    }

    /**
     * Parses this {@link Chain} as a signed decimal {@code long}
     *
     * @return the specified {@code long}, {@code '0L'} on error
     * @see Convert#toLong(byte[], int, long, long)
     * @since 0.0.3
     */
    public long toLong() {
        return Convert.toLong(
            value, count, 10L, 0L
        );
    }

    /**
     * Parses this {@link Chain} as a signed decimal {@code long}
     *
     * @return the specified {@code long}, {@code def} value on error
     * @see Convert#toLong(byte[], int, long, long)
     * @since 0.0.3
     */
    public long toLong(
        long def
    ) {
        return Convert.toLong(
            value, count, 10L, def
        );
    }

    /**
     * Parses this {@link Chain} as a signed decimal {@code long}
     *
     * @param radix the radix to be used while parsing {@link Chain}
     * @return the specified {@code long}, {@code def} value on error
     * @see Convert#toLong(byte[], int, long, long)
     * @since 0.0.3
     */
    public long toLong(
        long def, long radix
    ) {
        if (radix < 2L || radix > 36L) {
            return def;
        }
        return Convert.toLong(
            value, count, radix, def
        );
    }

    /**
     * Parses this {@link Chain} as a {@code float}
     *
     * @return the specified {@code float}, {@code '0F'} on error
     * @see Convert#toFloat(byte[], int, float)
     * @since 0.0.3
     */
    public float toFloat() {
        return Convert.toFloat(
            value, count, 0F
        );
    }

    /**
     * Parses this {@link Chain} as a {@code float}
     *
     * @return the specified {@code float}, {@code def} value on error
     * @see Convert#toFloat(byte[], int, float)
     * @since 0.0.3
     */
    public float toFloat(
        float def
    ) {
        return Convert.toFloat(
            value, count, def
        );
    }

    /**
     * Parses this {@link Chain} as a {@code double}
     *
     * @return the specified {@code double}, {@code '0D'} on error
     * @see Convert#toDouble(byte[], int, double)
     * @since 0.0.3
     */
    public double toDouble() {
        return Convert.toDouble(
            value, count, 0D
        );
    }

    /**
     * Parses this {@link Chain} as a {@code double}
     *
     * @return the specified {@code double}, {@code def} value on error
     * @see Convert#toDouble(byte[], int, double)
     * @since 0.0.3
     */
    public double toDouble(
        double def
    ) {
        return Convert.toDouble(
            value, count, def
        );
    }

    /**
     * Parses this {@link Chain} as a {@code int},
     * {@code long}, {@code double}, or {@code null}
     *
     * @return the specified {@link Number}, {@code 'null'} on error
     * @see Convert#toNumber(byte[], int, Number)
     * @since 0.0.3
     */
    @Nullable
    public Number toNumber() {
        return Convert.toNumber(
            value, count, null
        );
    }

    /**
     * Parses this {@link Chain} as a {@code int},
     * {@code long}, {@code double}, or {@code def} value
     *
     * @return the specified {@link Number}, {@code def} value on error
     * @see Convert#toNumber(byte[], int, Number)
     * @since 0.0.3
     */
    @Nullable
    public Number toNumber(
        @Nullable Number def
    ) {
        return Convert.toNumber(
            value, count, def
        );
    }

    /**
     * Parses this {@link Chain} as a {@code boolean}
     *
     * @return the specified {@code boolean}, {@code 'false'} on error
     * @see Convert#toBoolean(byte[], int, boolean)
     * @since 0.0.3
     */
    public boolean toBoolean() {
        return Convert.toBoolean(
            value, count, false
        );
    }

    /**
     * Parses this {@link Chain} as a {@code boolean}
     *
     * @return the specified {@code boolean}, {@code def} value on error
     * @see Convert#toBoolean(byte[], int, boolean)
     * @since 0.0.3
     */
    public boolean toBoolean(
        boolean def
    ) {
        return Convert.toBoolean(
            value, count, def
        );
    }

    /**
     * Returns a {@code REC4648|BASE} encoded byte array of {@link Chain}
     *
     * @see Base64
     * @since 0.0.5
     */
    @NotNull
    public String toBase64() {
        return toBase64(
            Base64.base()
        );
    }

    /**
     * Returns a specified {@code base64} encoded byte array of {@link Chain}
     *
     * @see Base64#encode(byte[], int, int)
     * @since 0.0.5
     */
    @NotNull
    public String toBase64(
        @NotNull Base64 base64
    ) {
        return Binary.latin(
            base64.encode(
                value, 0, count
            )
        );
    }

    /**
     * Parses this {@link Chain} as a {@link BigInteger}
     *
     * @return the specified {@link BigInteger}, {@code 'ZERO'} on error
     * @since 0.0.5
     */
    @NotNull
    public BigInteger toBigInteger() {
        return toBigInteger(
            BigInteger.ZERO
        );
    }

    /**
     * Parses this {@link Chain} as a {@link BigInteger}
     *
     * @return the specified {@link BigInteger}, {@code def} value on error
     * @since 0.0.5
     */
    @Nullable
    public BigInteger toBigInteger(
        @Nullable BigInteger def
    ) {
        int size = count;
        if (size != 0) {
            long num = toLong();
            if (num != 0) {
                return BigInteger.valueOf(num);
            }
            try {
                return new BigInteger(
                    toString(ISO_8859_1)
                );
            } catch (Exception e) {
                // Nothing
            }
        }
        return def;
    }

    /**
     * Parses this {@link Chain} as a {@link BigDecimal}
     *
     * @return the specified {@link BigDecimal}, {@code 'ZERO'} on error
     * @since 0.0.5
     */
    @NotNull
    public BigDecimal toBigDecimal() {
        return toBigDecimal(
            BigDecimal.ZERO
        );
    }

    /**
     * Parses this {@link Chain} as a {@link BigDecimal}
     *
     * @return the specified {@link BigDecimal}, {@code def} value on error
     * @since 0.0.5
     */
    @NotNull
    public BigDecimal toBigDecimal(
        @NotNull BigDecimal def
    ) {
        int size = count;
        if (size != 0) {
            byte[] it = value;
            char[] ch = new char[size];
            while (--size != -1) {
                ch[size] = (char) (
                    it[size] & 0xFF
                );
            }
            try {
                return new BigDecimal(ch);
            } catch (Exception e) {
                // Nothing
            }
        }
        return def;
    }

    /**
     * Returns this {@link Chain} as an {@link InputStream}
     *
     * @since 0.0.5
     */
    @NotNull
    public InputStream toInputStream() {
        return new ByteArrayInputStream(
            value, 0, count
        );
    }

    /**
     * Returns this {@link Chain} as an {@link InputStream}
     *
     * @since 0.0.5
     */
    @NotNull
    public InputStream toInputStream(
        int offset, int length
    ) {
        return new ByteArrayInputStream(
            value, offset, length
        );
    }

    /**
     * Copy bytes from this {@link Chain} into the destination byte array
     *
     * @param index the specified start index
     * @param dest  the specified destination array
     * @return the length of bytes read to the buffer, or {@code -1} if no more
     * @throws NullPointerException      If the specified {@code dest} array is null
     * @throws IndexOutOfBoundsException If access to buffer array exceeds array bounds
     * @see Chain#getBytes(int, byte[], int, int)
     * @since 0.0.3
     */
    public int getBytes(
        int index, byte[] dest
    ) {
        return getBytes(
            index, dest, 0, count - index
        );
    }

    /**
     * Copy bytes from this {@link Chain} into the destination byte array
     *
     * @param index  the specified start index
     * @param dest   the specified destination array
     * @param from   the specified start index in dest
     * @param length the specified number of array elms to copy
     * @return the length of bytes read to the buffer, or {@code -1} if no more
     * @throws NullPointerException      If the specified {@code dest} array is null
     * @throws IndexOutOfBoundsException If access to buffer array exceeds array bounds
     * @since 0.0.3
     */
    public int getBytes(
        int index, byte[] dest, int from, int length
    ) {
        if (length == 0) {
            return 0;
        }

        if ((index | from | length) > 0) {
            int more = count - index;
            if (more == 0) {
                return -1;
            }

            if (more > 0) {
                int size = dest.length - from;
                if (size == 0) {
                    return 0;
                }

                if (size > 0) {
                    if (more < length) {
                        length = more;
                    }

                    if (size < length) {
                        length = size;
                    }

                    System.arraycopy(
                        value, index, dest, from, length
                    );
                    return length;
                }
            }
        }

        throw new IndexOutOfBoundsException(
            "Index: " + index + ", from: " + from
                + ", length: " + length + ", count: " + count
        );
    }

    /**
     * Concatenates the byte to this {@link Chain}, copy it directly
     *
     * @param b the specified byte value to be appended
     * @since 0.0.5
     */
    protected void concat(byte b) {
        byte[] it = value;
        if (count != it.length) {
            asset = 0;
            it[count++] = b;
        } else {
            grow(count + 1);
            asset = 0;
            value[count++] = b;
        }
    }

    /**
     * Concatenates the byte array to this {@link Chain}, copy it directly
     *
     * @param b the specified source to be appended
     * @param i the specified start index for array
     * @param l the specified length of bytes to concat
     * @since 0.0.5
     */
    protected void concat(
        @NotNull byte[] b, int i, int l
    ) {
        if (l != 0) {
            int d = count;
            grow(d + l);
            asset = 0;
            count += l;
            System.arraycopy(
                b, i, value, d, l
            );
        }
    }

    /**
     * Concatenates the chain to this {@link Chain}, copy it directly
     *
     * @param c the specified chain to be appended
     * @param i the specified start index for chain
     * @param l the specified length of chain to concat
     * @since 0.0.5
     */
    protected void concat(
        @NotNull Chain c, int i, int l
    ) {
        int d = count;
        if (l == 1) {
            grow(d + 1);
            asset = 0;
            value[count++] = c.value[i];
        } else {
            grow(d + l);
            asset = 0;
            count += l;
            System.arraycopy(
                c.value, i, value, d, l
            );
        }
    }

    /**
     * Concatenates the stream to this {@link Chain}, copy it directly
     *
     * @param in the specified {@link InputStream} to be appended
     * @throws IOException If an I/O error occurs
     * @since 0.0.5
     */
    protected void concat(
        @NotNull InputStream in, int range
    ) throws IOException {
        int m, n, size;
        while (true) {
            m = in.available();
            if (m != 0) {
                m = Math.min(m, range);
                n = value.length - count;

                if (n < m) {
                    n = m;
                    grow(
                        count + m
                    );
                }

                size = in.read(
                    value, count, n
                );
                if (size == -1) {
                    break;
                } else {
                    asset = 0;
                    count += size;
                }
            } else {
                m = in.read();
                if (m == -1) {
                    break;
                } else {
                    byte[] it = value;
                    if (count != it.length) {
                        asset = 0;
                        it[count++] = (byte) m;
                    } else {
                        grow(count + 1);
                        asset = 0;
                        value[count++] = (byte) m;
                    }
                }
            }
        }
    }

    /**
     * Concatenates the string representation
     * of the integer value to this {@link Chain}
     *
     * @param num the specified number to be appended
     * @since 0.0.5
     */
    protected void concat(int num) {
        if (num < 0) {
            concat(
                (byte) '-'
            );
            if (num > -10) {
                concat((byte) (
                    0x3A + num
                ));
            } else {
                int mark = count;
                do {
                    concat((byte) (
                        0x3A + (num % 10)
                    ));
                    num /= 10;
                } while (num != 0);
                swop(mark, count);
            }
        } else {
            if (num < 10) {
                concat((byte) (
                    0x30 + num
                ));
            } else {
                int mark = count;
                do {
                    concat((byte) (
                        0x30 + (num % 10)
                    ));
                    num /= 10;
                } while (num != 0);
                swop(mark, count);
            }
        }
    }

    /**
     * Concatenates the string representation
     * of the long value to this {@link Chain}
     *
     * @param num the specified number to be appended
     * @since 0.0.5
     */
    protected void concat(long num) {
        if (num < 0) {
            concat(
                (byte) '-'
            );
            if (num > -10) {
                concat((byte) (
                    0x3A + num
                ));
            } else {
                int mark = count;
                do {
                    concat((byte) (
                        0x3A + (num % 10)
                    ));
                    num /= 10;
                } while (num != 0);
                swop(mark, count);
            }
        } else {
            if (num < 10) {
                concat((byte) (
                    0x30 + num
                ));
            } else {
                int mark = count;
                do {
                    concat((byte) (
                        0x30 + (num % 10)
                    ));
                    num /= 10;
                } while (num != 0);
                swop(mark, count);
            }
        }
    }

    /**
     * Concatenates the string representation
     * of the float value to this {@link Chain}
     *
     * @param num the specified number to be appended
     * @since 0.0.5
     */
    protected void concat(float num) {
        String data =
            Float.toString(num);
        concat(data, 0, data.length());
    }

    /**
     * Concatenates the string representation
     * of the double value to this {@link Chain}
     *
     * @param num the specified number to be appended
     * @since 0.0.5
     */
    protected void concat(double num) {
        String data =
            Double.toString(num);
        concat(data, 0, data.length());
    }

    /**
     * Concatenates the string representation
     * of the boolean value to this {@link Chain}
     *
     * @param b the specified boolean to be appended
     * @since 0.0.5
     */
    protected void concat(boolean b) {
        asset = 0;
        byte[] it = value;
        if (b) {
            int size = count + 4;
            if (size > it.length) {
                grow(size);
                it = value;
            }
            it[count++] = 't';
            it[count++] = 'r';
            it[count++] = 'u';
        } else {
            int size = count + 5;
            if (size > it.length) {
                grow(size);
                it = value;
            }
            it[count++] = 'f';
            it[count++] = 'a';
            it[count++] = 'l';
            it[count++] = 's';
        }
        it[count++] = 'e';
    }

    /**
     * Concatenates the character to this
     * {@link Chain}, converting it to UTF-8 first
     *
     * @param c the specified character to be appended
     * @since 0.0.5
     */
    protected void concat(char c) {
        asset = 0;
        byte[] it = value;

        // U+0000 ~ U+007F
        if (c < 0x80) {
            if (count != it.length) {
                it[count++] = (byte) c;
            } else {
                grow(count + 1);
                value[count++] = (byte) c;
            }
        }

        // U+0080 ~ U+07FF
        else if (c < 0x800) {
            int size = count + 2;
            if (size > it.length) {
                grow(size);
                it = value;
            }
            it[count++] = (byte) ((c >> 6) | 0xC0);
            it[count++] = (byte) ((c & 0x3F) | 0x80);
        }

        // U+10000 ~ U+10FFFF
        // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
        else if (0xD800 <= c && c <= 0xDFFF) {
            // crippled surrogate pair
            if (count != it.length) {
                it[count++] = (byte) '?';
            } else {
                grow(count + 1);
                value[count++] = (byte) '?';
            }
        }

        // U+0800 ~ U+FFFF
        else {
            int size = count + 3;
            if (size > it.length) {
                grow(size);
                it = value;
            }
            it[count++] = (byte) ((c >> 12) | 0xE0);
            it[count++] = (byte) (((c >> 6) & 0x3F) | 0x80);
            it[count++] = (byte) ((c & 0x3F) | 0x80);
        }
    }

    /**
     * Concatenates the character to this
     * {@link Chain}, converting it to unicode first
     *
     * @param e the specified escape character
     * @param c the specified character to be appended
     * @since 0.0.5
     */
    protected void concat(char c, byte e) {
        byte[] it = value;
        int size = count + 6;
        if (size > it.length) {
            grow(size);
            it = value;
        }
        it[count++] = e;
        it[count++] = 'u';
        it[count++] = upper((c >> 12) & 0x0F);
        it[count++] = upper((c >> 8) & 0x0F);
        it[count++] = upper((c >> 4) & 0x0F);
        it[count++] = upper(c & 0x0F);
    }

    /**
     * Concatenates the char array to this
     * {@link Chain}, converting it to UTF-8 first
     *
     * @param ch the specified array to be appended
     * @param i  the specified start index for array
     * @param l  the specified length of array to concat
     * @since 0.0.5
     */
    protected void concat(
        @NotNull char[] ch, int i, int l
    ) {
        int k = i + l;
        grow(count + l);

        asset = 0;
        byte[] it = value;

        while (i < k) {
            // next char
            char code = ch[i++];

            // U+0000 ~ U+007F
            if (code < 0x80) {
                if (count == it.length) {
                    grow(count + 1);
                    it = value;
                }
                it[count++] = (byte) code;
            }

            // U+0080 ~ U+07FF
            else if (code < 0x800) {
                int size = count + 2;
                if (size > it.length) {
                    grow(size);
                    it = value;
                }
                it[count++] = (byte) ((code >> 6) | 0xC0);
                it[count++] = (byte) ((code & 0x3F) | 0x80);
            }

            // U+10000 ~ U+10FFFF
            // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
            else if (0xD800 <= code && code <= 0xDFFF) {
                if (k <= i) {
                    if (count == it.length) {
                        grow(count + 1);
                        it = value;
                    }
                    it[count++] = (byte) '?';
                    break;
                }

                char next = ch[i++];
                if (next < 0xDC00 ||
                    next > 0xDFFF) {
                    if (count == it.length) {
                        grow(count + 1);
                        it = value;
                    }
                    it[count++] = (byte) '?';
                    continue;
                }

                int size = count + 4;
                if (size > it.length) {
                    grow(size);
                    it = value;
                }
                int unit = (code << 10) + next - 0x35FDC00;
                it[count++] = (byte) ((unit >> 18) | 0xF0);
                it[count++] = (byte) (((unit >> 12) & 0x3F) | 0x80);
                it[count++] = (byte) (((unit >> 6) & 0x3F) | 0x80);
                it[count++] = (byte) ((unit & 0x3F) | 0x80);
            }

            // U+0800 ~ U+FFFF
            else {
                int size = count + 3;
                if (size > it.length) {
                    grow(size);
                    it = value;
                }
                it[count++] = (byte) ((code >> 12) | 0xE0);
                it[count++] = (byte) (((code >> 6) & 0x3F) | 0x80);
                it[count++] = (byte) ((code & 0x3F) | 0x80);
            }
        }
    }

    /**
     * Concatenates the char sequence to this
     * {@link Chain}, converting it to UTF-8 first
     *
     * @param ch the specified sequence to be appended
     * @param i  the specified start index for sequence
     * @param l  the specified length of sequence to concat
     * @since 0.0.5
     */
    protected void concat(
        @NotNull CharSequence ch, int i, int l
    ) {
        int k = i + l;
        grow(count + l);

        asset = 0;
        byte[] it = value;

        while (i < k) {
            // next char
            char code = ch.charAt(i++);

            // U+0000 ~ U+007F
            if (code < 0x80) {
                if (count == it.length) {
                    grow(count + 1);
                    it = value;
                }
                it[count++] = (byte) code;
            }

            // U+0080 ~ U+07FF
            else if (code < 0x800) {
                int size = count + 2;
                if (size > it.length) {
                    grow(size);
                    it = value;
                }
                it[count++] = (byte) ((code >> 6) | 0xC0);
                it[count++] = (byte) ((code & 0x3F) | 0x80);
            }

            // U+10000 ~ U+10FFFF
            // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
            else if (0xD800 <= code && code <= 0xDFFF) {
                if (k <= i) {
                    if (count == it.length) {
                        grow(count + 1);
                        it = value;
                    }
                    it[count++] = (byte) '?';
                    break;
                }

                char next = ch.charAt(i++);
                if (next < 0xDC00 ||
                    next > 0xDFFF) {
                    if (count == it.length) {
                        grow(count + 1);
                        it = value;
                    }
                    it[count++] = (byte) '?';
                    continue;
                }

                int size = count + 4;
                if (size > it.length) {
                    grow(size);
                    it = value;
                }
                int unit = (code << 10) + next - 0x35FDC00;
                it[count++] = (byte) ((unit >> 18) | 0xF0);
                it[count++] = (byte) (((unit >> 12) & 0x3F) | 0x80);
                it[count++] = (byte) (((unit >> 6) & 0x3F) | 0x80);
                it[count++] = (byte) ((unit & 0x3F) | 0x80);
            }

            // U+0800 ~ U+FFFF
            else {
                int size = count + 3;
                if (size > it.length) {
                    grow(size);
                    it = value;
                }
                it[count++] = (byte) ((code >> 12) | 0xE0);
                it[count++] = (byte) (((code >> 6) & 0x3F) | 0x80);
                it[count++] = (byte) ((code & 0x3F) | 0x80);
            }
        }
    }

    /**
     * Unsafe method
     *
     * <pre>{@code
     *  move(1, 1);
     *  value[1] = 'k';
     *  move(1, -1);
     *  value[0] // is 'k'
     * }</pre>
     *
     * @param i the specified offset
     * @param s the specified shift length
     */
    protected void move(
        int i, int s
    ) {
        System.arraycopy(
            value, i, value, i + s, count - i
        );
    }

    /**
     * Unsafe method
     *
     * <pre>{@code
     *  value // kat.plus
     *  swop(1, 6);
     *  value // klp.taus
     * }</pre>
     *
     * @param i the start index, inclusive
     * @param e the end index, exclusive
     */
    protected void swop(
        int i, int e
    ) {
        byte item;
        asset = 0;
        byte[] it = value;
        while (i < --e) {
            item = it[e];
            it[e] = it[i];
            it[i++] = item;
        }
    }

    /**
     * Unsafe method
     *
     * <pre>{@code
     *  grow(count + 3);
     *  value[count++] = 'k';
     *  value[count++] = 'a';
     *  value[count++] = 't';
     * }</pre>
     *
     * @param capacity the specified minimum capacity
     */
    protected void grow(
        int capacity
    ) {
        byte[] it = value;
        if (capacity > it.length) {
            if (bucket == null) {
                int cap = it.length +
                    (it.length >> 1);
                if (cap < capacity) {
                    cap = capacity;
                }

                byte[] result = new byte[cap];
                System.arraycopy(
                    it, 0, result, 0, count
                );
                value = result;
            } else {
                value = bucket.apply(
                    it, count, capacity
                );
            }
        }
    }

    /**
     * @author Kraity
     * @since 0.0.1
     */
    public static class Reader implements plus.kat.stream.Reader {

        private int i, e;
        private byte[] b;

        /**
         * @param c the specified chain
         * @param i the specified start index
         * @param e the specified end index, exclude
         */
        private Reader(
            @NotNull Chain c, int i, int e
        ) {
            this.i = i;
            this.e = e;
            this.b = c.value;
        }

        /**
         * Check {@link Reader} for readable bytes
         *
         * @throws NullPointerException If this has been closed
         */
        @Override
        public boolean also() {
            return i < e;
        }

        /**
         * Read a byte and cursor switch to next
         *
         * @throws NullPointerException If this has been closed
         */
        @Override
        public byte read() {
            return b[i++];
        }

        /**
         * Reads a byte if {@link Reader} has readable bytes, otherwise raise IOException
         *
         * @throws IOException If this has been closed
         */
        @Override
        public byte next() throws IOException {
            if (i < e) {
                return b[i++];
            }

            throw new ReaderCrash(
                "Unexpectedly, no readable byte"
            );
        }

        /**
         * Sets the index of this reader
         *
         * @throws IllegalStateException if the index argument is negative
         */
        public void slip(
            int index
        ) {
            if (index >= 0) {
                i = index;
            } else {
                throw new IllegalStateException(
                    "Unexpectedly, the index is negative"
                );
            }
        }

        /**
         * Close this {@link Reader}
         */
        @Override
        public void close() {
            e = 0;
            b = null;
        }
    }
}
