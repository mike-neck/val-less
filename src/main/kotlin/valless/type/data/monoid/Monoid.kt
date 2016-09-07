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
package valless.type.data.monoid

import valless.type._1

interface Monoid<T> {

    interface _1_<T> {
        val monoid: Monoid<T>
    }

    interface _2_<M> {
        fun <T> monoid(): _1_<_1<M, T>>
    }

    fun empty(): T

    val mempty: T get() = empty()

    fun append(x: T, y: T): T

    val mappend: (T) -> ((T) -> T) get() = { x -> { y -> append(x, y) } }

    interface Builder<T> {
        fun append(appender: (T, T) -> T): Monoid<T>
    }

    companion object {
        fun <T> empty(empty: () -> T): Builder<T> = object : Builder<T> {
            override fun append(appender: (T, T) -> T): Monoid<T> = object : Monoid<T> {
                override fun empty(): T = empty()
                override fun append(x: T, y: T): T = appender(x, y)
            }
        }
    }
}
