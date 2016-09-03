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

import valless.type._1
import valless.util.function.`$`

interface Enum<T> : _1<Enum.Å, T> {

    interface __0<T> {
        val enumInstance: Enum<T>
    }

    object Å

    fun toEnum(i: Int): T

    val toEnum: (Int) -> T get() = { toEnum(it) }

    fun fromEnum(e: T): Int

    val fromEnum: (T) -> Int get() = { fromEnum(it) }

    fun succ(e: T): T = e `$` fromEnum `$` { it + 1 } `$` toEnum

    val succ: (T) -> T get() = { succ(it) }

    fun pred(e: T): T = e `$` fromEnum `$` { it - 1 } `$` toEnum

    val pred: (T) -> T get() = { pred(it) }
}
