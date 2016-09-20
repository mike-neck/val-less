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
import valless.type.data.IntInstance
import valless.type.data.List
import valless.type.data.Ord
import valless.type.data.list.ListFunctions.merge
import valless.util.collection.comb
import valless.util.times
import valless.util.toPair

class ListFunctionsTest {

    val io: Ord<Int> = IntInstance.ord

    val array = arrayOf(50, 30, 20, 70, 90, 60, 10, 0, 80, 40, 100)

    private val partition: (List<Int>) -> (Dual<Dual<Int>>) =
            { ListFunctions.partition(PartD(Dual.fromList(it)).toPartition(Dual.empty()), io.compare) }

    val kotlinSort: (Array<Int>) -> List<Int> = { a: Array<Int> -> List.of(*a.sortedArray()) }

    val sort: (List<Int>) -> List<Int> = List.sort(io)

    @Test fun partition() = (1..array.size).map { IntRange(0, it - 1) }
            .map { array.sliceArray(it) }
            .map { List.of(*it).toPair() }
            .map { it * partition * (List<Int>::toString to Dual<Dual<Int>>::toString) }
            .map { "${it.first} -> ${it.second}" }
            .forEach(::println)

    @Test fun sortCasePrinting() = (1..array.size).map { IntRange(0, it - 1) }
            .map { array.sliceArray(it) }
            .map { it to List.of(*it) }
            .map { it * (kotlinSort to sort) }
            .map { "${it.first} -> ${it.second}" }
            .forEach(::println)

    @Test fun merge() =
            (listOf({ Dual.empty<Int>() }, { Dual.of(10) }, { Dual.fromList(List.of(10, 30, 70)) })
                    comb
                    listOf({ Dual.empty<Int>() }, { Dual.of(20) }, { Dual.fromList(List.of(0, 20, 40, 60)) }))
                    .map { ListFunctions.MergeArg(it.first(), it.second(), Dual.empty(), io.compare) }
                    .map { merge(it) }
                    .forEach(::println)
}
