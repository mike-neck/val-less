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
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass
import valless.util.function.`$`

@TypeClass
interface Eq<T> : _1<Eq.Å, T> {

    object Å

    interface _1_<T> {
        val eq: Eq<T>
    }

    interface Deriving<D> {
        fun <T> eq(e: Eq<T>): Eq<_1<D, T>>
    }

    interface Deriving2<D> {
        fun <F, S> eq(f: Eq<F>, s: Eq<S>): Eq<_1<_1<D, F>, S>>
    }

    /**
     * Object equality.
     *
     * @param x - 1st parameter of [T].
     * @param y - 2nd parameter of [T].
     * @return [Bool.True] - if [x] equals to [y]. [Bool.False] - if [x] does not equal to [y].
     */
    @MinimumDefinition(Implementation.MUST)
    fun eq(x: T, y: T): Bool

    val eq: (T) -> (T) -> Bool get() = { x -> { y -> eq(x, y) } }

    fun neq(x: T, y: T): Bool = -eq(x, y)

    companion object {

        fun <C, T> deriveFrom(e: Eq<T>, f: (_1<C, T>) -> T): Eq<_1<C, T>> = object : Eq<_1<C, T>> {
            override fun eq(x: _1<C, T>, y: _1<C, T>): Bool = e.eq(f(x), f(y))
        }

        fun <T> fromEquals(): Eq<T> = object : Eq<T> {
            override fun eq(x: T, y: T): Bool = (x == y) `$` booleanToBool
        }

        val boolToBoolean: (Bool) -> Boolean = Bool::raw
        val booleanToBool: (Boolean) -> Bool = { if (it) Bool.True else Bool.False }
    }
}
