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
package valless.type.data.list

import org.junit.Test
import valless.type.data.List
import valless.util.function.`$`
import valless.util.function.id
import valless.util.initBy
import valless.util.shouldBe
import valless.util.times

class MutableListTest {

    val smallArray: Array<Int> = arrayOf(10, 20, 30)

    internal fun small(): MutableList<Int> = MutableList.fromList(List.of(*smallArray))

    val largeArray: Array<Int> = arrayOf(5, 15, 25, 35)

    internal fun large(): MutableList<Int> = MutableList.fromList(List.of(*largeArray))

    internal fun small_large(): Pair<MutableList<Int>, MutableList<Int>> = small() to large()

    internal fun large_small(): Pair<MutableList<Int>, MutableList<Int>> = large() to small()

    internal val asc_asc: Pair<(MutableList<Int>) -> MutableList<Int>, (MutableList<Int>) -> MutableList<Int>> =
            id<MutableList<Int>>() to id<MutableList<Int>>()

    internal val asc_desc: Pair<(MutableList<Int>) -> MutableList<Int>, (MutableList<Int>) -> MutableList<Int>> =
            id<MutableList<Int>>() to MutableList<Int>::reverse

    internal val desc_asc: Pair<(MutableList<Int>) -> MutableList<Int>, (MutableList<Int>) -> MutableList<Int>> =
            MutableList<Int>::reverse to id()

    internal val desc_desc: Pair<(MutableList<Int>) -> MutableList<Int>, (MutableList<Int>) -> MutableList<Int>> =
            MutableList<Int>::reverse to MutableList<Int>::reverse

    internal val plus: (Pair<MutableList<Int>, MutableList<Int>>) -> MutableList<Int> = { it.first + it.second }

    internal val drop7: (MutableList<Int>) -> MutableList<Int> = { it.drop(7) }

    internal val itsSize: (MutableList<Int>) -> Int = { it.size }

    internal val showItsState: (MutableList<Int>) -> MutableList<Int> = { it.initBy(printIt) }

    internal val printIt: (MutableList<Int>) -> Unit = { println("$it(size: ${it.size}, direction: ${(it as MutableList.Boxed).direction}, head: ${it.head.item}, tail: ${it.last.item})") }

    @Test fun smallASC_largeASC(): Unit = small_large() * asc_asc `$` plus `$` showItsState `$` drop7 `$` itsSize shouldBe 0

    @Test fun smallDESC_largeASC(): Unit = small_large() * desc_asc `$` plus `$` showItsState `$` drop7 `$` itsSize shouldBe 0

    @Test fun smallASC_largeDESC(): Unit = small_large() * asc_desc `$` plus `$` showItsState `$` drop7 `$` itsSize shouldBe 0

    @Test fun smallDESC_largeDESC(): Unit = small_large() * desc_desc `$` plus `$` showItsState `$` drop7 `$` itsSize shouldBe 0

    @Test fun largeASC_smallASC(): Unit = large_small() * asc_asc `$` plus `$` showItsState `$` drop7 `$` itsSize shouldBe 0

    @Test fun largeASC_smallDESC(): Unit = large_small() * asc_desc `$` plus `$` showItsState `$` drop7 `$` itsSize shouldBe 0

    @Test fun largeDESC_smallASC(): Unit = large_small() * desc_asc `$` plus `$` showItsState `$` drop7 `$` itsSize shouldBe 0

    @Test fun largeDESC_smallDESC(): Unit = large_small() * desc_desc `$` plus `$` showItsState `$` drop7 `$` itsSize shouldBe 0
}

