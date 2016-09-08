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

import org.junit.Test
import valless.util.function.`$`

class TypeTest {

    data class Kind<F, S>(val f: F, val s: S) : _2<Companion, F, S>, _1<_1<Companion, F>, S> {
        fun as2(): _2<Companion, F, S> = this
        fun as1(): _1<_1<Companion, F>, S> = this
    }

    companion object

    fun <F, S> toK1(): (_2<Companion, F, S>) -> _1<_1<Companion, F>, S> = { it }

    fun <F, S> toK2(): (_1<_1<Companion, F>, S>) -> _2<Companion, F, S> = { it as _2<Companion, F, S> }

    @Test fun canCastToK1() = Kind("", 0).as2() `$` toK1() `$` ::println

    @Test fun canCastToK2() = Kind("", 0).as1() `$` toK2() `$` ::println

    @Suppress("UNCHECKED_CAST")
    fun <F, S> pcas1(): (Holder<_2<Companion, F, S>>) -> Holder<_1<_1<Companion, F>, S>> = { it as Holder<_1<_1<Companion, F>, S>> }

    @Suppress("UNCHECKED_CAST")
    fun <F, S> pcas2(): (Holder<_1<_1<Companion, F>, S>>) -> Holder<_2<Companion, F, S>> = { it as Holder<_2<Companion, F, S>> }

    fun <T> toHolder(): (T) -> Holder<T> = { Holder(it) }

    @Test fun canCastParam1() = Kind("", 0).as2() `$` toHolder() `$` pcas1() `$` ::println

    @Test fun canCastParam2() = Kind("", 0).as1() `$` toHolder() `$` pcas2() `$` ::println
}

data class Holder<T>(val item: T) {
    fun <R> map(f: (T) -> R): Holder<R> = Holder(f(item))
    fun <R> eq(r: R, f: (R) -> T): Boolean = item == f(r)
}
