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
import valless.type._1
import valless.type.data.functor.Identity
import valless.type.data.functor.Identity.Companion.narrow
import valless.type.data.functor.narrow
import valless.type.data.monoid.Monoid
import valless.type.test
import valless.util.function.`$`
import valless.util.function.flip
import valless.util.shouldBe
import valless.util.times
import valless.util.toPair
import java.util.*

class ListTest :
        OrdEqTest<_1<List.Companion, Int>>
        , MonoidTest<_1<List.Companion, Int>> {

    override val e: Eq<_1<List.Companion, Int>>
        get() = List.eq(IntInstance.eq)

    override val o: Ord<_1<List.Companion, Int>>
        get() = List.ord(IntInstance.ord)

    val t: Traversable<List.Companion> = List.Companion.traversable

    val r: Random = Random(Date().time)

    fun randomInts(): Array<Int> = randomInts(1 + r.nextInt(100))

    fun randomIntsMini(): Array<Int> = randomInts(5)

    fun <T> tripleOf(g: () -> T): Triple<T, T, T> = Triple(g(), g(), g())

    tailrec fun randomInts(size: Int, array: Array<Int> = emptyArray()): Array<Int> =
            if (size == 0) array
            else randomInts(size - 1, array + r.nextInt())

    @Test override fun eqTrueTest() = List.of(*randomInts()).toPair() `$`
            { e.eq(it.first, it.second) } shouldBe Bool.True

    @Test override fun eqFalseTest() = (List.of(*randomInts()) to List.of(*randomInts())) `$`
            { e.eq(it.first, it.second) } shouldBe Bool.False

    @Test override fun ordLtTest() = (List.of(1, 2, 3, 4, 5) to List.of(1, 2, 3, 4, 5, 6)) `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.LT

    @Test fun ordLtTest2() = (List.empty<Int>() to List.of(1)) `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.LT

    @Test override fun ordEqTest() = List.of(*randomInts()).toPair() `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.EQ

    @Test fun ordEqTest2() = List.empty<Int>() `$`
            { o.compare(it, it) } shouldBe Ordering.EQ

    @Test override fun ordGtTest() = (List.of(1, 2, 3, 4, 5) to List.of(1, 2, 3, 4)) `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.GT

    @Test fun ordGtTest2() = (List.of(1) to List.empty<Int>()) `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.GT

    override val mn: Monoid<_1<List.Companion, Int>> get() = List.monoid()

    @Test override fun `mappend mempty x = x`() = List.of(*randomInts()).toPair() *
            `mappend mempty x` `$` e.test { it.first shouldEqualTo it.second }

    @Test override fun `mappend x mempty = x`() = List.of(*randomInts()).toPair() *
            `mappend x mempty` `$` e.test { it.first shouldEqualTo it.second }

    @Test override fun `mappend x {mappend y z} = mappend {mappend x y} z`() =
            tripleOf { List.of(*randomInts()) }.toPair() *
                    (`__ mappend x {mappend y z}` to `__ mappend {mappend x y} z`) `$`
                    e.test { it.first shouldEqualTo it.second }

    private fun <T> T.toTriple(): Triple<T, T, T> = Triple(this, this, this)

    val `traverse Identity`: (_1<List.Companion, Int>) -> Identity<_1<List.Companion, Int>> =
            t.traverse<Int, Int, Identity.Companion>(Identity.monad).flip()(Identity.toIdentity<Int>()).narrow

    @Test fun identity() = List.of(*randomInts()).toPair() *
            (`traverse Identity` to Identity.toIdentity<List<Int>>()) `$`
            Identity.eq(e).narrow.test { it.first shouldEqualTo it.second.up.narrow }
}
