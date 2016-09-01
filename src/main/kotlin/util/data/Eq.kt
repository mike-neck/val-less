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

object Eq {

    interface Type0<C, I: Type0<C, I>>: Type.Class0<C, I> {
        @MinimalDefinition
        infix fun Type.Kind0<C>.eqTo(y: Type.Kind0<C>): Bool

        infix fun Type.Kind0<C>.isNot(y: Type.Kind0<C>): Bool = if (this.eqTo(y) == Bool.True) Bool.False else Bool.True
        fun eq(x: Type.Kind0<C>, y: Type.Kind0<C>): Bool = x.eqTo(y)
        fun notEq(x: Type.Kind0<C>, y: Type.Kind0<C>): Bool = x.isNot(y)
    }

    interface Type1<C, I: Type1<C, I>>: Type.Class1<C, I> {
        @MinimalDefinition
        fun <C2, I2: Type0<C2, I2>, T: Type.Kind0<C2>> Type.Kind1<C, T>.eqTo(y: Type.Kind1<C, T>, cv: Type0<C2, I2>? = null): Bool

        fun <C2, I2: Type0<C2, I2>, T: Type.Kind0<C2>> Type.Kind1<C, T>.isNot(y: Type.Kind1<C, T>, cv: Type0<C2, I2>? = null): Bool = if (this.eqTo(y, cv) == Bool.True) Bool.False else Bool.True
        fun <C2, I2: Type0<C2, I2>, T: Type.Kind0<C2>> eq(x: Type.Kind1<C, T>, y: Type.Kind1<C, T>, cv: Type0<C2, I2>? = null): Bool = x.eqTo(y, cv)
        fun <C2, I2: Type0<C2, I2>, T: Type.Kind0<C2>> notEq(x: Type.Kind1<C, T>, y: Type.Kind1<C, T>, cv: Type0<C2, I2>? = null): Bool = x.isNot(y, cv)
    }

    interface Type2<C, I: Type2<C, I>>: Type.Class2<C, I> {
        @MinimalDefinition
        fun <S, T> Type.Kind2<C, S, T>.eqTo(y: Type.Kind2<C, S, T>): Bool

        fun <S, T> Type.Kind2<C, S, T>.isNot(y: Type.Kind2<C, S, T>): Bool = if (this.eqTo(y) == Bool.True) Bool.False else Bool.True
        fun <S, T> eq(x: Type.Kind2<C, S, T>, y: Type.Kind2<C, S, T>): Bool = x.eqTo(y)
        fun <S, T> notEq(x: Type.Kind2<C, S, T>, y: Type.Kind2<C, S, T>): Bool = x.isNot(y)
    }

    interface Type3<C, I: Type3<C, I>>: Type.Class3<C, I> {
        @MinimalDefinition
        fun <S1, S2, T> Type.Kind3<C, S1, S2, T>.eqTo(y: Type.Kind3<C, S1, S2, T>): Bool

        fun <S1, S2, T> Type.Kind3<C, S1, S2, T>.isNot(y: Type.Kind3<C, S1, S2, T>): Bool = if (this.eqTo(y) == Bool.True) Bool.False else Bool.True
        fun <S1, S2, T> eq(x: Type.Kind3<C, S1, S2, T>, y: Type.Kind3<C, S1, S2, T>): Bool = x.eqTo(y)
        fun <S1, S2, T> notEq(x: Type.Kind3<C, S1, S2, T>, y: Type.Kind3<C, S1, S2, T>): Bool = x.isNot(y)
    }
}
