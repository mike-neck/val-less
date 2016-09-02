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
package valless.util.basic

import java.util.*

sealed class Choice<F, out S> {

    abstract val first: F
    abstract val second: S

    class First<F, out S>(override val first: F) : Choice<F, S>() {
        override val second: S get() = throw NoSuchElementException("This is first.")
    }

    class Second<F, out S>(override val second: S) : Choice<F, S>() {
        override val first: F get() = throw NoSuchElementException("This is second.")
    }

    interface ResultBuilder<F, out S, R> {
        fun onSecond(sh: (S) -> R): R
    }

    fun <R> onFirst(fh: (F) -> R): ResultBuilder<F, S, R> = object : ResultBuilder<F, S, R> {
        override fun onSecond(sh: (S) -> R): R = when (this@Choice) {
            is Choice.First -> fh(this@Choice.first)
            is Choice.Second -> sh(this@Choice.second)
        }
    }
}
