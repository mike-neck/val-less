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

object Bounded {

    interface Type0<C, I: Type0<C, I>>: Type.Class0<C, I> {
        @MinimalDefinition
        fun minBound(): Type.Kind0<C>
        @MinimalDefinition
        fun maxBound(): Type.Kind0<C>
    }

    interface Type1<C, I: Type1<C, I>>: Type.Class1<C, I> {
        @MinimalDefinition
        fun <T> minBound(): Type.Kind1<C, T>
        @MinimalDefinition
        fun <T> maxBound(): Type.Kind1<C, T>
    }

    interface Type2<C, I: Type2<C, I>>: Type.Class2<C, I> {
        @MinimalDefinition
        fun <S, T> minBound(): Type.Kind2<C, S, T>
        @MinimalDefinition
        fun <S, T> maxBound(): Type.Kind2<C, S, T>
    }

    interface Type3<C, I: Type3<C, I>>: Type.Class3<C, I> {
        @MinimalDefinition
        fun <S1, S2, T> minBound(): Type.Kind3<C, S1, S2, T>
        @MinimalDefinition
        fun <S1, S2, T> maxBound(): Type.Kind3<C, S1, S2, T>
    }
}
