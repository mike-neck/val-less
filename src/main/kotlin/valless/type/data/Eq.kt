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

interface Eq<T> : _1<Eq.Å, T> {

    object Å

    interface _1_<T> {
        val eqInstance: Eq<T>
    }

    interface Deriving<D> {
        fun <T> eq(e: Eq<T>): Eq<_1<D, T>>
    }

    /**
     * Object equality.
     *
     * @param x - 1st parameter of [T].
     * @param y - 2nd parameter of [T].
     * @return [Bool.True] - if [x] equals to [y]. [Bool.False] - if [x] does not equal to [y].
     */
    fun eq(x: T, y: T): Bool

    fun neq(x: T, y: T): Bool = -eq(x, y)

    companion object {

        fun <T> fromEquals(): Eq<T> = object : Eq<T> {
            override fun eq(x: T, y: T): Bool = (x == y) `$` booleanToBool
        }

        val boolToBoolean: (Bool) -> Boolean = Bool::raw
        val booleanToBool: (Boolean) -> Bool = { if (it) Bool.True else Bool.False }
    }
}
