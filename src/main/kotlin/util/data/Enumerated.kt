/*
 * Copyright 2016 Shinya Mochida
 * 
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package util.data

import util.annotation.MinimalDefinition
import util.plus
import util.type.Type

object Enumerated {

    interface Type0<C, I: Type0<C, I>>: Type.Class0<C, I> {
        @MinimalDefinition
        fun toEnum(i: Int): Type.Kind0<C>
        val toEnum: (Int) -> Type.Kind0<C> get() = { toEnum(it) }

        @MinimalDefinition
        fun fromEnum(e: Type.Kind0<C>): Int
        val fromEnum: (Type.Kind0<C>) -> Int get() = { fromEnum(it) }

        fun succ(e: Type.Kind0<C>): Type.Kind0<C> = (fromEnum + { it + 1 } + toEnum)(e)
        val succ: (Type.Kind0<C>) -> Type.Kind0<C> get() = { succ(it) }

        fun pred(e: Type.Kind0<C>): Type.Kind0<C> = (fromEnum + { it - 1} + toEnum)(e)
        val pred: (Type.Kind0<C>) -> Type.Kind0<C> get() = { pred(it) }
    }

    interface Type1<C, I: Type1<C, I>>: Type.Class1<C, I> {
        @MinimalDefinition
        fun <T> toEnum(i: Int): Type.Kind1<C, T>

        @MinimalDefinition
        fun <T> fromEnum(e: Type.Kind1<C, T>): Int

        fun <T> succ(e: Type.Kind1<C, T>): Type.Kind1<C, T> = fromEnum(e).let { it + 1 }.let { toEnum(it) }
        fun <T> pred(e: Type.Kind1<C, T>): Type.Kind1<C, T> = fromEnum(e).let { it - 1 }.let { toEnum(it) }
    }

    interface Type2<C, I: Type2<C, I>>: Type.Class2<C, I> {
        @MinimalDefinition
        fun <S, T> toEnum(i: Int): Type.Kind2<C, S, T>

        @MinimalDefinition
        fun <S, T> fromEnum(e: Type.Kind2<C, S, T>): Int

        fun <S, T> succ(e: Type.Kind2<C, S, T>): Type.Kind2<C, S, T> = fromEnum(e).let { it + 1 }.let { toEnum(it) }
        fun <S, T> pred(e: Type.Kind2<C, S, T>): Type.Kind2<C, S, T> = fromEnum(e).let { it - 1 }.let { toEnum(it) }
    }

    interface Type3<C, I: Type3<C, I>>: Type.Class3<C, I> {
        @MinimalDefinition
        fun <S1, S2, T> toEnum(i: Int): Type.Kind3<C, S1, S2, T>

        @MinimalDefinition
        fun <S1, S2, T> fromEnum(e: Type.Kind3<C, S1, S2, T>): Int

        fun <S1, S2, T> succ(e: Type.Kind3<C, S1, S2, T>): Type.Kind3<C, S1, S2, T> = fromEnum(e).let { it + 1 }.let { toEnum(it) }
        fun <S1, S2, T> pred(e: Type.Kind3<C, S1, S2, T>): Type.Kind3<C, S1, S2, T> = fromEnum(e).let { it - 1 }.let { toEnum(it) }
    }
}
