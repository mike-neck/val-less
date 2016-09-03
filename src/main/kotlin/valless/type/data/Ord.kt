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

import valless.type._0
import valless.type._1
import valless.util.flow.When
import valless.util.flow.ifSo

interface Ord<T> : _1<Ord.Å, T> {

    object Å

    fun compare(x: T, y: T): Ordering

    val asEq: Eq<T> get() = object : Eq<T> {
        override fun eq(x: T, y: T): Bool =
                (compare(x, y) == Ordering.EQ)
                        .ifSo { Bool.True }
                        .els { Bool.False }
    }

    companion object {
        fun <T : Comparable<T>> fromComparable(): Ord<T> = object : Ord<T> {
            override fun compare(x: T, y: T): Ordering =
                    When<Int, Ordering>(x.compareTo(y))
                            .case { it < 0 }.then { Ordering.LT }
                            .case { it == 0 }.then { Ordering.EQ }
                            .els { Ordering.GT }
        }
    }
}

enum class Ordering : _0<Ordering.Companion>, Comparable<Ordering> {
    LT,
    EQ,
    GT;

    companion object {
        val eqInstance: Eq<Ordering> = Ord.fromComparable<Ordering>().asEq
        val ordInstance: Ord<Ordering> = Ord.fromComparable()
        val enumInstance: Enum<Ordering> = object : Enum<Ordering> {
            override fun toEnum(i: Int): Ordering =
                    When<Int, Ordering>(i)
                            .case { it == 0 }.then { LT }
                            .case { it == 1 }.then { EQ }
                            .case { it == 2 }.then { GT }
                            .els { throw IllegalArgumentException("Illegal Argument for Enum.Ordering.toEnum") }

            override fun fromEnum(e: Ordering): Int =
                    When<Ordering, Int>(e)
                            .case { it == LT }.then { 0 }
                            .case { it == EQ }.then { 1 }
                            .case { it == GT }.then { 2 }
                            .els { throw IllegalArgumentException("Illegal Argument for Enum.Ordering.fromEnum") }

            override fun succ(e: Ordering): Ordering =
                    When<Ordering, Ordering>(e)
                            .case { it == LT }.then { EQ }
                            .case { it == EQ }.then { GT }
                            .els { throw IllegalArgumentException("Illegal Argument for Enum.Ordering.succ") }

            override fun pred(e: Ordering): Ordering =
                    When<Ordering, Ordering>(e)
                            .case { it == GT }.then { EQ }
                            .case { it == EQ }.then { LT }
                            .els { throw IllegalArgumentException("Illegal Argument for Enum.Ordering.pred") }
        }
    }
}
