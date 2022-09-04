package plus.kat.caller;

import plus.kat.anno.NotNull;
import plus.kat.anno.Nullable;

import plus.kat.chain.*;
import plus.kat.kernel.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static plus.kat.stream.Binary.upper;

/**
 * @author kraity
 * @since 0.0.4
 */
@SuppressWarnings("deprecation")
public class Query extends Chain {

    protected int offset;

    public Query() {
        super(16);
    }

    /**
     * @param src the specified src
     */
    public Query(
        @NotNull byte[] src
    ) {
        super(src);
        count = value.length;
        if (offset() == 0) offset = -1;
    }

    /**
     * @param url the specified url
     */
    public Query(
        @NotNull CharSequence url
    ) {
        super();
        super.chain(
            url, 0, url.length()
        );
        if (offset() == 0) offset = -1;
    }

    /**
     * Returns a {@link Query} of this {@link Query}
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     */
    @Override
    public Query subSequence(
        int start, int end
    ) {
        return new Query(
            copyBytes(start, end)
        );
    }

    /**
     * Returns the starting index of the parameter
     */
    public int offset() {
        int o = offset;
        if (o > 0) {
            return o;
        }
        int max = count;
        byte[] it = value;
        while (o < max) {
            if (it[o++] == '?') {
                return offset = o;
            }
        }
        return 0;
    }

    /**
     * @param key   the specified key
     * @param value the specified value
     */
    public Query set(
        @NotNull String key,
        @Nullable int value
    ) {
        return set(key).add(value);
    }

    /**
     * @param key   the specified key
     * @param value the specified value
     */
    public Query set(
        @NotNull String key,
        @Nullable long value
    ) {
        return set(key).add(value);
    }

    /**
     * @param key   the specified key
     * @param value the specified value
     */
    public Query set(
        @NotNull String key,
        @Nullable String value
    ) {
        return set(key).add(value);
    }

    /**
     * @param key the specified key
     * @param val the specified value
     */
    public Query set(
        @NotNull String key,
        @Nullable Object val
    ) {
        return set(key).add(
            val.toString()
        );
    }

    /**
     * @param b the specified data
     */
    public Query set(
        byte b
    ) {
        grow(count + 3);
        hash = 0;
        value[count++] = '%';
        value[count++] = upper((b & 0xF0) >> 4);
        value[count++] = upper(b & 0x0F);
        return this;
    }

    /**
     * @param key the specified key
     */
    public Query set(
        @NotNull String key
    ) {
        if (count > 0) {
            if (offset != -1) {
                super.chain(
                    (byte) '&'
                );
            } else {
                super.chain(
                    (byte) '?'
                );
                offset = count;
            }
        }

        this.add(key);
        byte[] it = value;

        if (count != it.length) {
            hash = 0;
            it[count++] = '=';
        } else {
            grow(count + 1);
            hash = 0;
            value[count++] = '=';
        }
        return this;
    }

    /**
     * @param b the specified data
     */
    public Query add(
        byte b
    ) {
        if ((0x60 < b && b < 0x7b) ||
            (0x40 < b && b < 0x5b) ||
            (0x2F < b && b < 0x3A)) {
            chain(b);
        } else {
            if (b == ' ') {
                chain(
                    (byte) '+'
                );
            } else if (b == '.' ||
                b == '_' ||
                b == '-' ||
                b == '*') {
                chain(b);
            } else {
                return set(b);
            }
        }
        return this;
    }

    /**
     * @param data the specified data
     */
    public Query add(
        @NotNull byte[] data
    ) {
        grow(count + data.length);
        for (byte b : data) add(b);
        return this;
    }

    /**
     * @param data the specified data
     * @param i    the start index
     * @param l    the specified length
     */
    public Query add(
        @NotNull byte[] data, int i, int l
    ) {
        grow(count + l);
        int k = i + l;
        while (i < k) {
            add(data[i++]);
        }
        return this;
    }

    /**
     * @param num the specified data
     */
    public Query add(
        int num
    ) {
        chain(num);
        return this;
    }

    /**
     * @param num the specified data
     */
    public Query add(
        long num
    ) {
        chain(num);
        return this;
    }

    /**
     * @param c the specified data
     */
    public Query add(
        @NotNull CharSequence c
    ) {
        return add(
            c, 0, c.length()
        );
    }

    /**
     * @param c the specified data
     * @param i the start index
     * @param l the specified length
     */
    public Query add(
        @NotNull CharSequence c, int i, int l
    ) {
        int k = i + l;
        grow(count + l);

        while (i < k) {
            // get char
            char d = c.charAt(i++);

            // U+0000 ~ U+007F
            if (d < 0x80) {
                add((byte) d);
            }

            // U+0080 ~ U+07FF
            else if (d < 0x800) {
                set((byte) ((d >> 6) | 0xC0));
                set((byte) ((d & 0x3F) | 0x80));
            }

            // U+10000 ~ U+10FFFF
            // U+D800 ~ U+DBFF & U+DC00 ~ U+DFFF
            else if (d >= 0xD800 && d <= 0xDFFF) {
                if (i >= k) {
                    set((byte) '?');
                    break;
                }

                char f = c.charAt(i++);
                if (f < 0xDC00 || f > 0xDFFF) {
                    set((byte) '?');
                    continue;
                }

                int u = (d << 10) + f - 0x35F_DC00;
                set((byte) ((u >> 18) | 0xF0));
                set((byte) (((u >> 12) & 0x3F) | 0x80));
                set((byte) (((u >> 6) & 0x3F) | 0x80));
                set((byte) ((u & 0x3F) | 0x80));
            }

            // U+0800 ~ U+FFFF
            else {
                set((byte) ((d >> 12) | 0xE0));
                set((byte) (((d >> 6) & 0x3F) | 0x80));
                set((byte) ((d & 0x3F) | 0x80));
            }
        }

        return this;
    }

    /**
     * @throws IOException If an I/O error occurs
     */
    @NotNull
    public Client get()
        throws IOException {
        return client().get();
    }

    /**
     * Returns this {@link Query} as a {@link Client}
     *
     * @throws IOException If an I/O error occurs
     */
    @NotNull
    public Client client()
        throws IOException {
        return new Client(
            new URL(
                toString()
            )
        );
    }

    /**
     * Returns this {@link Query} as a {@link HashMap}
     */
    @NotNull
    public Map<String, String> toMap() {
        return toMap(
            new HashMap<>()
        );
    }

    /**
     * Returns this {@link Query} as a {@link HashMap}
     */
    @NotNull
    public Map<String, String> toMap(
        @NotNull Map<String, String> map
    ) {
        int i = offset();
        Value val = new Value();

        while (true) {
            int k = indexOf(
                (byte) '=', i
            );
            if (k == -1) {
                break;
            }

            val.slip(0);
            val.uniform(
                value, i, k
            );
            String key = val.toString();

            int v = indexOf(
                (byte) '&', ++k
            );
            if (v == -1) {
                v = count;
            }
            val.slip(0);
            val.uniform(
                value, k, v
            );

            i = v + 1;
            map.put(
                key, val.toString()
            );
        }
        return map;
    }

    /**
     * Returns this {@link Query} as a {@link URL}
     *
     * @throws MalformedURLException If no protocol is specified, or unknown protocol
     */
    @NotNull
    public URL toUrl()
        throws MalformedURLException {
        return new URL(
            toString()
        );
    }

    /**
     * Returns the {@code byte[]} of this {@link Query} as a {@link String}
     */
    @NotNull
    @Override
    public String toString() {
        if (count == 0) {
            return "";
        }

        return new String(
            value, 0, 0, count
        );
    }

    /**
     * Returns the {@code byte[]} of this {@link Query} as a {@link String}
     *
     * @param b the beginning index, inclusive
     * @param e the ending index, exclusive
     * @throws IndexOutOfBoundsException if the beginIndex is negative
     */
    @NotNull
    @Override
    public String toString(
        int b, int e
    ) {
        int l = e - b;
        if (l <= 0 || e > count) {
            return "";
        }

        return new String(
            value, 0, b, l
        );
    }
}