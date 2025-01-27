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
import plus.kat.kernel.*;
import plus.kat.stream.*;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author kraity
 * @since 0.0.1
 */
public class ShortSpare extends Property<Short> {

    public static final ShortSpare
        INSTANCE = new ShortSpare();

    public ShortSpare() {
        super(Short.class);
    }

    @Override
    public Short apply() {
        return (short) 0;
    }

    @Override
    public Short apply(
        @NotNull Type type
    ) {
        if (type == short.class ||
            type == Short.class) {
            return (short) 0;
        }

        throw new Collapse(
            "Unable to create an instance of " + type
        );
    }

    @Override
    public String getSpace() {
        return "u";
    }

    @Override
    public boolean accept(
        @NotNull Class<?> clazz
    ) {
        return clazz == short.class
            || clazz == Short.class
            || clazz == Number.class
            || clazz == Object.class;
    }

    @Override
    public Boolean getBorder(
        @NotNull Flag flag
    ) {
        return Boolean.FALSE;
    }

    @Override
    public Short read(
        @NotNull Flag flag,
        @NotNull Alias alias
    ) {
        return (short) alias.toInt();
    }

    @Override
    public Short read(
        @NotNull Flag flag,
        @NotNull Value value
    ) {
        return (short) value.toInt();
    }

    @Override
    public void write(
        @NotNull Flow flow,
        @NotNull Object value
    ) throws IOException {
        flow.emit(
            (short) value
        );
    }

    @Override
    public Short cast(
        @Nullable Object data,
        @NotNull Supplier supplier
    ) {
        if (data != null) {
            if (data instanceof Short) {
                return (Short) data;
            }

            if (data instanceof Number) {
                return ((Number) data).shortValue();
            }

            if (data instanceof Boolean) {
                return ((boolean) data) ? (short) 1 : (short) 0;
            }

            int i = 0;
            if (data instanceof Chain) {
                i = ((Chain) data).toInt();
            } else if (data instanceof CharSequence) {
                CharSequence num = (CharSequence) data;
                i = Convert.toInt(
                    num, num.length(), 10, 0
                );
            }

            return i < Short.MIN_VALUE
                || i > Short.MAX_VALUE ? (short) 0 : (short) i;
        }
        return (short) 0;
    }
}
