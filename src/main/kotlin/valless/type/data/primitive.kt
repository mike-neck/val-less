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

object IntInstance :
        Eq._1_<Int>
        , Ord._1_<Int>
        , Enum<Int>
        , Num<Int> {

    override val eq: Eq<Int> get() = Eq.fromEquals()

    override val ord: Ord<Int> get() = Ord.fromComparable()

    override fun toEnum(i: Int): Int = i

    override fun fromEnum(e: Int): Int = e

    override fun `{+}`(x: Int, y: Int): Int = x + y

    override fun `{-}`(x: Int, y: Int): Int = x - y

    override fun `{*}`(x: Int, y: Int): Int = x * y

    override val zero: Int get() = 0

    override val plusOne: Int get() = 1

    override fun negate(x: Int): Int = -x

    override fun signum(x: Int): Int = when (ord.compare(x, 0)) {
        Ordering.LT -> -1
        Ordering.EQ -> 0
        Ordering.GT -> 1
    }

    override fun abs(x: Int): Int = if (x < 0) -x else x

    override fun fromIntegral(x: Integral): Int = x.value().toInt()
}

object LongInstance :
        Eq._1_<Long>
        , Ord._1_<Long>
        , Enum<Long>
        , Num<Long> {

    override val eq: Eq<Long> get() = Eq.fromEquals()

    override val ord: Ord<Long> get() = Ord.fromComparable()

    override fun toEnum(i: Int): Long = i.toLong()

    override fun fromEnum(e: Long): Int =
            if (e <= Int.MAX_VALUE && e >= Int.MIN_VALUE) e.toInt()
            else throw IllegalArgumentException("Enum.fromEnum.Long : the value is out of bound.")

    override fun succ(e: Long): Long = e + 1

    override fun pred(e: Long): Long = e - 1

    override fun `{+}`(x: Long, y: Long): Long = x + y

    override fun `{-}`(x: Long, y: Long): Long = x - y

    override fun `{*}`(x: Long, y: Long): Long = x * y

    override val zero: Long get() = 0

    override val plusOne: Long get() = 1

    override fun negate(x: Long): Long = -x

    override fun signum(x: Long): Long = when (ord.compare(x, 0)) {
        Ordering.LT -> -1
        Ordering.EQ -> 0
        Ordering.GT -> 1
    }

    override fun abs(x: Long): Long = if (x < 0) -x else x

    override fun fromIntegral(x: Integral): Long = x.value()
}
