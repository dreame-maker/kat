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
package plus.kat.spare;

import plus.kat.anno.NotNull;
import plus.kat.anno.Nullable;

import plus.kat.*;
import plus.kat.chain.*;
import plus.kat.crash.*;

import java.io.*;
import java.net.*;

/**
 * @author kraity
 * @since 0.0.2
 */
public class URISpare extends Property<URI> {

    public static final URISpare
        INSTANCE = new URISpare();

    public URISpare() {
        super(URI.class);
    }

    @Override
    public String getSpace() {
        return "URI";
    }

    @Override
    public boolean accept(
        @NotNull Class<?> clazz
    ) {
        return clazz == URI.class
            || clazz == Object.class;
    }

    @Override
    public URI read(
        @NotNull Flag flag,
        @NotNull Value value
    ) throws IOException {
        if (value.isEmpty()) {
            return null;
        }
        try {
            return new URI(
                value.toString()
            );
        } catch (URISyntaxException e) {
            throw new ProxyCrash(e);
        }
    }

    @Override
    public void write(
        @NotNull Flow flow,
        @NotNull Object value
    ) throws IOException {
        flow.emit(
            ((URI) value).toASCIIString()
        );
    }

    @Override
    public URI cast(
        @Nullable Object data,
        @NotNull Supplier supplier
    ) {
        if (data != null) {
            if (data instanceof URI) {
                return (URI) data;
            }

            if (data instanceof URL) {
                try {
                    return ((URL) data).toURI();
                } catch (Exception e) {
                    return null;
                }
            }

            if (data instanceof CharSequence) {
                String d = data.toString();
                if (d.isEmpty()) {
                    return null;
                }
                try {
                    return new URI(d);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }
}
