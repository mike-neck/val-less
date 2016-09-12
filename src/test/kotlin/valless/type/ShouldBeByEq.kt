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
package valless.type

import valless.type.data.Eq
import valless.util.function.`$`

class ShouldBeByEq<T>(val e: Eq<T>) {

    fun <P> test(obj: P, assertion: ShouldBeByEq<T>.(P) -> Unit): Unit = assertion(this, obj)

    infix fun T.shouldEqualTo(other: T): Unit = """
Expected : $other
Actual   : $this
""" to e.eq(this, other) `$` { if (it.second.raw == false) throw AssertionError(it.first) }
}

fun <P, T> P.testWith(e: Eq<T>, test: ShouldBeByEq<T>.(P) -> Unit): Unit =
        ShouldBeByEq(e).test(this, test)

fun <P : Pair<T, T>, T> Eq<T>.test(test: ShouldBeByEq<T>.(P) -> Unit): (P) -> Unit = { p -> p.testWith(this, test) }
