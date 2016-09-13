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
package valless.type.control.monad.trans

import valless.type._1
import valless.type._3
import valless.type.control.monad.Monad
import valless.type.data.functor.Identity
import valless.type.up
import valless.util.function.`$`
import valless.util.function.id
import valless.util.function.plus
import valless.util.swap
import valless.util.times

sealed class StateT<S, M, T> : _3<StateT.Companion, S, M, T> {

    abstract val stateT: (S) -> _1<M, Pair<T, S>>

    abstract internal val mn: Monad<M>

    fun execStateT(): (S) -> _1<M, S> = { s -> mn.map(stateT(s)) { it.second } }

    fun evalStateT(): (S) -> _1<M, T> = { s -> mn.map(stateT(s)) { it.first } }

    private class StateTImpl<S, M, T>(override val mn: Monad<M>, override val stateT: (S) -> _1<M, Pair<T, S>>) : StateT<S, M, T>()

    internal class State<S, T>(override val stateT: (S) -> _1<Identity.Companion, Pair<T, S>>) : StateT<S, Identity.Companion, T>() {

        override val mn: Monad<Identity.Companion> = Identity.monad

        val state: (S) -> Pair<T, S> get() = stateT + Identity.runIdentityN()
    }

    companion object {

        fun <F, S, R> firstMap(f: (F) -> R): Pair<(F) -> R, (S) -> S> = f to id<S>()

        fun <S, M, T> stateT(m: Monad<M>, f: (S) -> _1<M, Pair<T, S>>): StateT<S, M, T> = StateTImpl(m, f)

        fun <S, M, T, R> map(obj: StateT<S, M, T>, f: (T) -> R): StateT<S, M, R> =
                stateT(obj.mn) { obj.mn.map(obj.stateT(it)) { it * firstMap<T, S, R>(f) } }

        fun <P, S, M, T> toStateT(m: Monad<M>, f: (P) -> ((S) -> _1<M, Pair<T, S>>)): (P) -> StateT<S, M, T> =
                { p: P -> stateT(m, f(p)) }

        fun <S, M, T> narrow(): (_1<_1<_1<Companion, S>, M>, T>) -> StateT<S, M, T> = { it.up.up.narrow }

        private fun <S, T, R> stateMap(obj: State<S, T>, f: (T) -> R): State<S, R> =
                state { obj.state(it) * firstMap<T, S, R>(f) }

        fun <S, M> monad(m: Monad<M>): Monad<_1<_1<Companion, S>, M>> = object : Monad<_1<_1<Companion, S>, M>> {

            override fun <T, R> map(obj: _1<_1<_1<Companion, S>, M>, T>, f: (T) -> R): _1<_1<_1<Companion, S>, M>, R> =
                    this@Companion.map(obj.up.up.narrow, f)

            override fun <T> pure(value: T): _1<_1<_1<Companion, S>, M>, T> = StateTImpl(m) { m.pure(value to it) }

            override fun <T, R, G : (T) -> R> _1<_1<_1<Companion, S>, M>, G>.`(_)`(obj: _1<_1<_1<Companion, S>, M>, T>): _1<_1<_1<Companion, S>, M>, R> =
                    this.up.up.narrow.evalStateT() to obj.up.up.narrow.stateT `$`
                            { p -> stateT(m) { s: S -> m.ap(m.map(p.first(s), firstMapping()), p.second(s)) } }

            private fun <T, R> firstMapping(): ((T) -> R) -> (Pair<T, S>) -> Pair<R, S> = { f -> { p: Pair<T, S> -> p.swap.times(f).swap } }

            override fun <T, R> bind(obj: _1<_1<_1<Companion, S>, M>, T>, f: (T) -> _1<_1<_1<Companion, S>, M>, R>): _1<_1<_1<Companion, S>, M>, R> =
                    obj.up.up.narrow.evalStateT() `$`
                            toStateT(m) { g -> { s: S -> m.bind(g(s), f + narrow() + { it.stateT(s) }) } }
        }
    }
}

val <S, M, T> _3<StateT.Companion, S, M, T>.narrow: StateT<S, M, T> get() = this as StateT<S, M, T>

object State {

    operator fun <S, T> invoke(f: (S) -> Pair<T, S>): StateT<S, Identity.Companion, T> = StateT.State(f + Identity.toIdentity())

    fun <S, T> state(f: (S) -> Pair<T, S>): StateT.State<S, T> = StateT.State(f + Identity.toIdentity())

    fun <S> monad(): Monad<_1<_1<StateT.Companion, S>, Identity.Companion>> = object : Monad<_1<_1<StateT.Companion, S>, Identity.Companion>> {

        override fun <T, R> map(obj: _1<_1<_1<StateT.Companion, S>, Identity.Companion>, T>, f: (T) -> R): _1<_1<_1<StateT.Companion, S>, Identity.Companion>, R> = TODO("not implemented")

        override fun <T> pure(value: T): _1<_1<_1<StateT.Companion, S>, Identity.Companion>, T> = TODO("not implemented")

        override fun <T, R, G : (T) -> R> _1<_1<_1<StateT.Companion, S>, Identity.Companion>, G>.`(_)`(obj: _1<_1<_1<StateT.Companion, S>, Identity.Companion>, T>): _1<_1<_1<StateT.Companion, S>, Identity.Companion>, R> = TODO("not implemented")

        override fun <T, R> bind(obj: _1<_1<_1<StateT.Companion, S>, Identity.Companion>, T>, f: (T) -> _1<_1<_1<StateT.Companion, S>, Identity.Companion>, R>): _1<_1<_1<StateT.Companion, S>, Identity.Companion>, R> = TODO("not implemented")
    }
}

val <S, T> StateT<S, Identity.Companion, T>.execState: (S) -> S get() = this.execStateT() + Identity.runIdentityN()

val <S, T> StateT<S, Identity.Companion, T>.evalState: (S) -> T get() = this.evalStateT() + Identity.runIdentityN()
