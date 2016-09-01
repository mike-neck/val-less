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
package util.flow

fun <T> T.doing(calc: (T) -> T): Until<T> = object : Until<T> {
    override fun until(condition: (T) -> Boolean): T = go(this@doing, condition)
    tailrec fun go(t: T, condition: (T) -> Boolean): T =
            if (condition(t)) t else go(calc(t), condition)
}

interface Until<out T> {
    fun until(condition: (T) -> Boolean): T
}


