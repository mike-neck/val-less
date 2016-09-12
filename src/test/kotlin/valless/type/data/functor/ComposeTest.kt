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
package valless.type.data.functor

import org.junit.Test
import valless.type._1
import valless.type.data.*
import valless.type.data.functor.classes.Eq1
import valless.type.data.functor.classes.Ord1
import valless.type.test
import valless.util.*
import valless.util.function.`$`
import java.util.*

class ComposeTest :
        OrdEqTest<_1<_1<_1<Compose.Companion, Maybe.Companion>, Identity.Companion>, Int>>
        , TraversableTest<_1<_1<Compose.Companion, Maybe.Companion>, Identity.Companion>> {
    override val e: Eq<_1<_1<_1<Compose.Companion, Maybe.Companion>, Identity.Companion>, Int>>
        get() = Compose.eq(Eq1.maybe, Eq1.identity, IntInstance.eq)

    override val o: Ord<_1<_1<_1<Compose.Companion, Maybe.Companion>, Identity.Companion>, Int>>
        get() = Compose.ord(Ord1.maybe, Ord1.identity, IntInstance.ord)

    override val tr: Traversable<_1<_1<Compose.Companion, Maybe.Companion>, Identity.Companion>>
        get() = Compose.traversable(Maybe.traversable, Identity.traversable)

    val r: Random = Random(Date().time)

    fun <F, G, T> toCompose(): (_1<F, _1<G, T>>) -> Compose<F, G, T> = { Compose(it) }

    fun compose(v: Int): Compose<Maybe.Companion, Identity.Companion, Int> =
            Maybe.Just(Identity(v).wide).wide `$` toCompose()

    val toCompose: (Int) -> Compose<Maybe.Companion, Identity.Companion, Int> = { compose(it) }

    tailrec fun runTest(leftGen: () -> _1<_1<_1<Compose.Companion, Maybe.Companion>, Identity.Companion>, Int>,
                        rightGen: () -> _1<_1<_1<Compose.Companion, Maybe.Companion>, Identity.Companion>, Int>,
                        times: Int = 100,
                        @Suppress("UNUSED_PARAMETER") prev: Unit = Unit,
                        check: (Pair<_1<_1<_1<Compose.Companion, Maybe.Companion>, Identity.Companion>, Int>,
                                _1<_1<_1<Compose.Companion, Maybe.Companion>, Identity.Companion>, Int>>)
                        -> Unit = { e.test { it.first shouldEqualTo it.second } }): Unit = when (times) {
        0 -> Unit
        else -> runTest(leftGen, rightGen, times - 1, (leftGen() to rightGen()) `$` check, check)
    }

    @Test override fun eqTrueTest() = r.nextInt().toPair().both(toCompose) `$`
            { e.eq(it.first, it.second) } shouldBe Bool.True

    @Test override fun eqFalseTest() = (r.nextInt(25) to (26 + r.nextInt(25))).both(toCompose) `$`
            { e.eq(it.first, it.second) } shouldBe Bool.False

    @Test override fun ordLtTest() = (r.nextInt(100) to (100 + r.nextInt(100))).both(toCompose) `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.LT

    @Test fun nothingIsLtTest() = (Maybe.Nothing<_1<Identity.Companion, Int>>() to r.nextInt()) *
            (toCompose<Maybe.Companion, Identity.Companion, Int>() to toCompose) `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.LT

    @Test override fun ordEqTest() = r.nextInt().toPair().both(toCompose) `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.EQ

    @Test fun nothingToNothingIsEq() = Maybe.Nothing<_1<Identity.Companion, Int>>().toPair().both(toCompose()) `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.EQ

    @Test override fun ordGtTest() = (r.nextInt(100) to (r.nextInt(100) - 100)).both(toCompose) `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.GT

    @Test fun justIsGtToNothing() = (Maybe.Nothing<_1<Identity.Companion, Int>>() to r.nextInt()) *
            (toCompose<Maybe.Companion, Identity.Companion, Int>() to toCompose) `$`
            swap() `$`
            { o.compare(it.first, it.second) } shouldBe Ordering.GT

    @Test override fun `traverse Identity = Identity`() = r.nextInt().toPair().both(toCompose) *
            (`traverse Identity`<Int>() to identity<Int>()) `$`
            Identity.eq(e).test { it.first shouldEqualTo it.second }

    @Test override fun `eta _ traverse f = traverse (eta _ f)`() = r.nextInt().toPair().both(toCompose) *
            (`eta _ traverse f`<Int>() to `traverse (eta _ f)`<Int>()) `$`
            Maybe.eq(e).test { it.first shouldEqualTo it.second }
}

