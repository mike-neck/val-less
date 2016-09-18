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
import valless.type._1
import valless.type.control.monad.Monad
import valless.type.control.monad.trans.State
import valless.type.control.monad.trans.StateT
import valless.type.control.monad.trans.narrow
import valless.type.data.*
import valless.type.data.List
import valless.type.data.functor.Identity
import valless.type.up
import valless.util.asUnit
import valless.util.function.`$`
import valless.util.function.times
import java.util.*

class ListSort {

    val r: Random = Random(System.currentTimeMillis())

    fun randomInts(size: Int): Array<Int> =
            if (size <= 0) emptyArray()
            else (1..size).map { r.nextInt() }.toTypedArray()

    fun <T> toList(): (Array<T>) -> List<T> = { List.of(*it) }

    val sm: Monad<_1<_1<StateT.Companion, Long>, Identity.Companion>> = State.monad()

    tailrec fun iterateSort(listSize: Int = 10000, times: Int = 5, results: () -> List<Long> = { List.empty() }): List<Long> =
            when (times == 0) {
                true -> results()
                false -> iterateSort(listSize, times - 1) {
                    sm.bind(State.get<Long>()) { sm.pure(runSort(listSize)) } `$`
                            { sm.bind(it) { State.modify<Long> { System.nanoTime() - it } } } `$`
                            { it.up.up.narrow.execState(System.nanoTime()) } `$`
                            { ListFunctions.plus(results(), List.of(it)) }
                }
            }

    fun runSort(size: Int): Unit = randomInts(size) `$` toList() `$` List.sort(IntInstance.ord) `$` asUnit

    val lt: Traversable<List.Companion> = List.traversable

    @Test fun benchmarkOfSortingList() =
            (iterateSort() `$` lt.for_<Long, Unit, Maybe.Companion>(Maybe.monad)) *
                    { Maybe.Just(println(it)) } `$` asUnit
}
