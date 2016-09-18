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
package valless.type.data

import valless.type._1

sealed class Set<T> : _1<Set.Companion, T> {

    abstract val size: Int

    class Tip<T> : Set<T>() {
        override val size: Int = 0
    }

    class Bin<T>(override val size: Int, val value: T, left: Set<T>, right: Set<T>) : Set<T>()

    companion object {

        fun <T> empty(): Set<T> = Tip()

        fun <T> singleton(value: T): Set<T> = Bin(1, value, Tip(), Tip())
    }
}

