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
package util.type

interface Type<C> {

    val kind: String

    interface Kind0<C>: Type<C> {
        override val kind: String get() = "*"
    }

    interface Kind1<C, T>: Type<C> {
        override val kind: String get() = "* -> *"
    }

    interface Kind2<C, S, T>: Type<C> {
        override val kind: String get() = "* -> * -> *"
    }

    interface Kind3<C, S1, S2, T>: Type<C> {
        override val kind: String get() = "* -> * -> * -> *"
    }

    interface Class<C>

    interface Class0<C, I: Class0<C, I>>: Class<C>

    interface Class1<C, I: Class1<C, I>>: Class<C>

    interface Class2<C, I: Class2<C, I>>: Class<C>

    interface Class3<C, I: Class3<C, I>>: Class<C>
}
