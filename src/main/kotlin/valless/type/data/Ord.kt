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
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass
import valless.util.flow.When
import valless.util.flow.ifSo

@TypeClass
interface Ord<T> : _1<Ord.Å, T> {

    object Å

    interface _1_<T> {
        val ord: Ord<T>
    }

    interface Deriving<D> {
        fun <T> ord(o: Ord<T>): Ord<_1<D, T>>
    }

    @MinimumDefinition(Implementation.MUST)
    fun compare(x: T, y: T): Ordering

    val asEq: Eq<T> get() = object : Eq<T> {
        override fun eq(x: T, y: T): Bool =
                (compare(x, y) == Ordering.EQ)
                        .ifSo { Bool.True }
                        .els { Bool.False }
    }

    companion object {

        fun <C, T> deriveFrom(o: Ord<T>, f: (_1<C, T>) -> T): Ord<_1<C, T>> = object : Ord<_1<C, T>> {
            override fun compare(x: _1<C, T>, y: _1<C, T>): Ordering = o.compare(f(x), f(y))
        }

        fun <T : Comparable<T>> fromComparable(): Ord<T> = object : Ord<T> {
            override fun compare(x: T, y: T): Ordering =
                    When(x.compareTo(y))
                            .case { it < 0 }.then { Ordering.LT }
                            .case { it == 0 }.then { Ordering.EQ }
                            .els { Ordering.GT }
        }
    }
}

