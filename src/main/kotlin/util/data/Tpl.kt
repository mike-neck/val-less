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
package util.data

import util.type.Type

interface Tuple

data class Tpl<F, S>(val fst: F, val snd: S): Type.Kind2<Tuple, F, S> {

    fun swap(): Tpl<S, F> = Tpl(snd, fst)

    fun pair(): Pair<F, S> = fst to snd

    companion object {
        fun <F, S> fst(tpl: Tpl<F, S>): F = tpl.fst
        fun <F, S> snd(tpl: Tpl<F, S>): S = tpl.snd

        fun <F, S, R> curry(func: (Tpl<F, S>) -> R): (F) -> ((S) -> R) = { f -> { s -> func(Tpl(f, s)) } }
        fun <F, S, R> uncurry(func: (F) -> ((S) -> R)): (Tpl<F, S>) -> R = { func(it.fst)(it.snd) }

        fun <F, S, R> normal(func: (Tpl<F, S>) -> R): (F, S) -> R = { f, s -> func(Tpl(f, s)) }
        fun <F, S, R> tuple(func: (F, S) -> R): (Tpl<F, S>) -> R = { func(it.fst, it.snd) }

        fun <F, S> swap(tpl: Tpl<F, S>): Tpl<S, F> = tpl.swap()
    }
}

object EqTpl: Eq.Type2<Tuple, EqTpl> {
    override fun <S, T> Type.Kind2<Tuple, S, T>.eqTo(y: Type.Kind2<Tuple, S, T>): Bool =
            if (this is Tpl<S, T> && y is Tpl<S, T>)
                if (this.fst == y.fst && this.snd == y.snd) Bool.True else Bool.False
            else Bool.False
}

