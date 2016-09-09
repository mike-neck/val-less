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

import org.junit.Test
import valless.util.function.`$`
import valless.util.shouldBe
import valless.util.times
import valless.util.toPair
import java.util.*

interface OrdEqTest<N> {

    val e: Eq<N>

    val o: Ord<N>

    @Test fun eqTrueTest()

    @Test fun eqFalseTest()

    @Test fun ordLtTest()

    @Test fun ordEqTest()

    @Test fun ordGtTest()
}

interface EnumTest<E> {

    val en: Enum<E>

    fun `fromEnum then toEnum becomes same`()

    fun `succ 2 times is increment 2 times`()

    fun `pred 2 times is decrement 2 times`()
}

class IntTest : OrdEqTest<Int>, EnumTest<Int> {

    override val e: Eq<Int> = IntInstance.eq

    override val o: Ord<Int> = IntInstance.ord

    override val en: Enum<Int> = IntInstance

    val i: IntInstance = IntInstance

    val r: Random = Random(Date().time)

    fun random(): Int = r.nextInt()

    private fun IntRange.random(): Int = this.start + r.nextInt(this.endInclusive - this.start + 1)

    @Test override fun eqTrueTest() = random() `$` { e.eq(it, it) } shouldBe Bool.True

    @Test override fun eqFalseTest() = ((1..1024).random() to (1025..4096).random()) `$`
            { e.eq(it.first, it.second) } shouldBe Bool.False

    @Test override fun ordLtTest() = o.compare(-1, 0) shouldBe Ordering.LT

    @Test override fun ordEqTest() = random() `$` { o.compare(it, it) } shouldBe Ordering.EQ

    @Test override fun ordGtTest() = random() `$` { o.compare(it, it - 1) } shouldBe Ordering.GT

    @Test override fun `fromEnum then toEnum becomes same`() = random().toPair() *
            i.fromEnum * i.toEnum `$` { it.first shouldBe it.second }

    @Test override fun `succ 2 times is increment 2 times`() = random().toPair() *
            i.succ * i.succ `$` { it.first + 2 shouldBe it.second }

    @Test override fun `pred 2 times is decrement 2 times`() = random().toPair() *
            i.pred * i.pred `$` { it.first - 2 shouldBe it.second }

    @Test fun `calculation should be same`() = random().toPair() *
            (random() `$` { rn -> { v: Int -> rn + v } to { v: Int -> i.calc { rn + v } } }) *
            (r.nextInt(20) `$` { rn -> { v: Int -> rn * v } to { v: Int -> i.calc { rn * v } } }) `$`
            { it.first shouldBe it.second }
}

class LongTest : OrdEqTest<Long>, EnumTest<Long> {

    override val e: Eq<Long>
        get() = LongInstance.eq

    override val o: Ord<Long>
        get() = LongInstance.ord

    override val en: Enum<Long>
        get() = LongInstance

    val r: Random = Random(Date().time)

    fun random(): Long = r.nextLong()

    fun bound(max: Long): Pair<Long, Long> = r.nextLong().shl(1).shr(1).toPair() * { it % max }

    tailrec fun random(max: Long, prev: Pair<Long, Long> = 0L to Long.MAX_VALUE): Long =
            if (prev.first - prev.second + (max - 1) >= 0) prev.second
            else random(max, bound(max))

    @Test override fun eqTrueTest() = random().toPair() `$`
            { e.eq(it.first, it.second) } shouldBe Bool.True

    @Test override fun eqFalseTest() = (random() to random()) `$`
            { e.eq(it.first, it.second) } shouldBe Bool.False

    @Test override fun ordLtTest() = random() `$`
            { o.compare(it, it + 1) } shouldBe Ordering.LT

    @Test override fun ordEqTest() = random().toPair() `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.EQ

    @Test override fun ordGtTest() = random() `$`
            { o.compare(it, it - 1) } shouldBe Ordering.GT

    @Test override fun `fromEnum then toEnum becomes same`() = random(Int.MAX_VALUE.toLong()).toPair() *
            en.fromEnum *
            en.toEnum `$`
            { it.first shouldBe it.second }

    @Test override fun `succ 2 times is increment 2 times`() = random().toPair() *
            en.succ *
            en.succ `$`
            { it.first + 2 shouldBe it.second }

    @Test override fun `pred 2 times is decrement 2 times`() = random().toPair() *
            en.pred *
            en.pred `$`
            { it.first - 2 shouldBe it.second }
}
