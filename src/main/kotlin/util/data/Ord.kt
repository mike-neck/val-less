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
import util.type.Type

object Ord {

    interface Type0<C, I: Type0<C, I>>: Eq.Type0<C, I> {
        @MinimalDefinition
        infix fun Type.Kind0<C>.comp(y: Type.Kind0<C>): Ordering

        fun compare(x: Type.Kind0<C>, y: Type.Kind0<C>): Ordering = x.comp(y)
        fun <O> comparing(f: (O) -> Type.Kind0<C>, l: O, r: O): Ordering = f(l) comp f(r)
    }

    interface Type1<C, I: Type1<C, I>>: Eq.Type1<C, I> {
        @MinimalDefinition
        infix fun <T> Type.Kind1<C, T>.comp(y: Type.Kind1<C, T>): Ordering

        fun <T> compare(x: Type.Kind1<C, T>, y: Type.Kind1<C, T>): Ordering = x.comp(y)
        fun <O, T> comparing(f: (O) -> Type.Kind1<C, T>, l: O, r: O): Ordering = f(l) comp f(r)
    }

    interface Type2<C, I: Type2<C, I>>: Eq.Type2<C, I> {
        @MinimalDefinition
        infix fun <S, T> Type.Kind2<C, S, T>.comp(y: Type.Kind2<C, S, T>): Ordering

        fun <S, T> compare(x: Type.Kind2<C, S, T>, y: Type.Kind2<C, S, T>): Ordering = x.comp(y)
        fun <O, S, T> comparing(f: (O) -> Type.Kind2<C, S, T>, l: O, r: O): Ordering = f(l) comp f(r)
    }

    interface Type3<C, I: Type3<C, I>>: Eq.Type3<C, I> {
        @MinimalDefinition
        infix fun <S1, S2, T> Type.Kind3<C, S1, S2, T>.comp(y: Type.Kind3<C, S1, S2, T>): Ordering

        fun <S1, S2, T> compare(x: Type.Kind3<C, S1, S2, T>, y: Type.Kind3<C, S1, S2, T>): Ordering = x.comp(y)
        fun <O, S1, S2, T> comparing(f: (O) -> Type.Kind3<C, S1, S2, T>, l: O, r: O): Ordering = f(l) comp f(r)
    }
}
