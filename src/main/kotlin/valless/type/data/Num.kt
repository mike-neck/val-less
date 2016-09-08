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
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass
import valless.util.basic.Choice
import valless.util.both
import valless.util.flow.When
import valless.util.function.`$`

@TypeClass
interface Num<T> {

    interface _1_<T> {
        val num: Num<T>
    }

    @MinimumDefinition(Implementation.MUST)
    fun `{+}`(x: T, y: T): T

    infix operator fun T.plus(o: T): T = this@Num.`{+}`(this, o)

    @MinimumDefinition(Implementation.MUST)
    fun `{-}`(x: T, y: T): T

    infix operator fun T.minus(o: T): T = this@Num.`{-}`(this, o)

    @MinimumDefinition(Implementation.MUST)
    fun `{*}`(x: T, y: T): T

    infix operator fun T.times(o: T): T = this@Num.`{*}`(this, o)

    @MinimumDefinition(Implementation.MUST)
    fun negate(x: T): T

    operator fun T.unaryMinus(): T = negate(this)

    @MinimumDefinition(Implementation.MUST)
    fun signum(x: T): T

    @MinimumDefinition(Implementation.MUST)
    fun abs(x: T): T

    @MinimumDefinition(Implementation.MUST)
    fun fromIntegral(x: Integral): T

    fun <R> calc(f: Num<T>.() -> R): R = this.f()
}

data class Integral(val value: Choice<Int, Long>) : _0<Integral.Companion>, Comparable<Integral> {

    constructor(v: Int) : this(Choice.First(v))

    constructor(v: Long) : this(Choice.Second(v))

    fun value(): Long = value.arrange(Int::toLong)

    override fun compareTo(other: Integral): Int =
            (this to other).both { it.value }
                    .both { it.arrange(Int::toLong) }
                    .let { it.first.compareTo(it.second) }

    companion object :
            Eq._1_<Integral>
            , Ord._1_<Integral>
            , Enum._1_<Integral>
            , Num._1_<Integral> {
        override val eq: Eq<Integral> get() = Eq.fromEquals()

        override val ord: Ord<Integral> get() = Ord.fromComparable()

        override val enm: Enum<Integral> get() = object : Enum<Integral> {
            override fun toEnum(i: Int): Integral = Integral(i)

            override fun fromEnum(e: Integral): Int = e.value().toInt()
        }

        val toIntegral: (Long) -> Integral =
                { if (it > Int.MAX_VALUE || it < Int.MIN_VALUE) Integral(it) else Integral(it.toInt()) }

        override val num: Num<Integral> get() = object : Num<Integral> {
            override fun `{+}`(x: Integral, y: Integral): Integral =
                    (x.value() + y.value()) `$` toIntegral

            override fun `{-}`(x: Integral, y: Integral): Integral =
                    (x.value() - y.value()) `$` toIntegral

            override fun `{*}`(x: Integral, y: Integral): Integral =
                    (x.value() * y.value()) `$` toIntegral

            override fun signum(x: Integral): Integral =
                    x.value() `$` { if (it < 0) minusOne else if (it == 0.toLong()) zero else plusOne }

            override fun negate(x: Integral): Integral = x.value
                    .onFirst<Choice<Int, Long>> { Choice.First<Int, Long>(-it) }
                    .onSecond { Choice.Second<Int, Long>(-it) } `$` ::Integral

            private val minusOne: Integral = Integral(-1)
            private val zero: Integral = Integral(0)
            private val plusOne: Integral = Integral(1)

            override fun abs(x: Integral): Integral =
                    When(x.value())
                            .case { it < 0 }.then { -x }
                            .els { x }

            override fun fromIntegral(x: Integral): Integral = x
        }
    }
}
