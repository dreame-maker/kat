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

import java.io.IOException;

/**
 * @author kraity
 * @since 0.0.3
 */
public class StringBufferSpare extends Property<StringBuffer> {

    public static final StringBufferSpare
        INSTANCE = new StringBufferSpare();

    public StringBufferSpare() {
        super(StringBuffer.class);
    }

    @Override
    public StringBuffer apply() {
        return new StringBuffer();
    }

    @Override
    public String getSpace() {
        return "s";
    }

    @Override
    public StringBuffer read(
        @NotNull Flag flag,
        @NotNull Alias alias
    ) {
        return new StringBuffer(
            alias.toString()
        );
    }

    @Override
    public StringBuffer read(
        @NotNull Flag flag,
        @NotNull Value value
    ) {
        return new StringBuffer(
            value.toString()
        );
    }

    @Override
    public void write(
        @NotNull Flow flow,
        @NotNull Object value
    ) throws IOException {
        flow.emit(
            (CharSequence) value
        );
    }

    @Override
    public StringBuffer cast(
        @Nullable Object data,
        @NotNull Supplier supplier
    ) {
        if (data == null) {
            return apply();
        }

        if (data instanceof CharSequence) {
            return new StringBuffer(
                (CharSequence) data
            );
        }

        return new StringBuffer(data.toString());
    }
}
