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

import valless.type.data.List
import valless.util.function.`$`

internal sealed class Partition<T> {

    abstract val result: MutableList<MutableList<T>>

    class Finished<T>(override val result: MutableList<MutableList<T>>) : Partition<T>()

    class Almost<T>(val head: T, override val result: MutableList<MutableList<T>>) : Partition<T>()

    class Building<T>(val first: T, val second: T, val list: List<T>, override val result: MutableList<MutableList<T>>) : Partition<T>()

    companion object {
        operator fun <T> invoke(list: List<T>, result: MutableList<MutableList<T>> = MutableList.empty()): Partition<T> = when (list) {
            is List.Nil -> Finished(result)
            is List.Cons -> list.tail `$` {
                when (it) {
                    is List.Nil -> Almost(list.head, result)
                    is List.Cons -> Building(list.head, it.head, it.tail, result)
                }
            }
        }
    }
}
