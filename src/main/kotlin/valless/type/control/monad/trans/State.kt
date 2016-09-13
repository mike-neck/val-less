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
import valless.type.control.monad.MonadPlus
import valless.type.data.functor.Identity
import valless.type.up
import valless.util.both
import valless.util.div
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

    companion object {

        fun <S, M> get(m: Monad<M>): StateT<S, M, S> = stateT(m) { m.pure(it to it) }

        fun <S, M, T> gets(m: Monad<M>, f: (S) -> T): StateT<S, M, T> = stateT(m) { m.pure(f(it) to it) }

        fun <S, M> put(m: Monad<M>, s: S): StateT<S, M, Unit> = stateT(m) { m.pure(Unit to s) }

        fun <S, M, T, N, R> mapStateT(n: Monad<N>, obj: StateT<S, M, T>, f: (_1<M, Pair<T, S>>) -> _1<N, Pair<R, S>>): StateT<S, N, R> =
                stateT(n, obj.stateT + f)

        fun <S, M, T> withState(obj: StateT<S, M, T>, f: (S) -> S): StateT<S, M, T> =
                stateT(obj.mn, f + obj.stateT)

        fun <S, M> modify(m: Monad<M>, f: (S) -> S): StateT<S, M, Unit> = stateT(m) { m.pure(Unit to f(it)) }

        fun <F, S, R> firstMap(f: (F) -> R): Pair<(F) -> R, (S) -> S> = f to id<S>()

        fun <S, M, T> stateT(m: Monad<M>, f: (S) -> _1<M, Pair<T, S>>): StateT<S, M, T> = StateTImpl(m, f)

        fun <S, M, T, R> map(obj: StateT<S, M, T>, f: (T) -> R): StateT<S, M, R> =
                stateT(obj.mn) { obj.mn.map(obj.stateT(it)) { it * firstMap<T, S, R>(f) } }

        fun <P, S, M, T> toStateT(m: Monad<M>, f: (P) -> ((S) -> _1<M, Pair<T, S>>)): (P) -> StateT<S, M, T> =
                { p: P -> stateT(m, f(p)) }

        fun <S, M, T> narrow(obj: _1<_1<_1<Companion, S>, M>, T>): StateT<S, M, T> = obj.up.up.narrow

        fun <S, M, T> narrow(): (_1<_1<_1<Companion, S>, M>, T>) -> StateT<S, M, T> = { it.up.up.narrow }

        fun <S, M> monad(m: Monad<M>): Monad<_1<_1<Companion, S>, M>> = object : Monad<_1<_1<Companion, S>, M>> {

            override fun <T, R> map(obj: _1<_1<_1<Companion, S>, M>, T>, f: (T) -> R): _1<_1<_1<Companion, S>, M>, R> =
                    this@Companion.map(obj.up.up.narrow, f)

            override fun <T> pure(value: T): _1<_1<_1<Companion, S>, M>, T> = StateTImpl(m) { m.pure(value to it) }

            override fun <T, R, G : (T) -> R> _1<_1<_1<Companion, S>, M>, G>.`(_)`(obj: _1<_1<_1<Companion, S>, M>, T>): _1<_1<_1<Companion, S>, M>, R> =
                    this.up.up.narrow.evalStateT() to obj.up.up.narrow.stateT `$`
                            toStateT(m) { p -> { s: S -> m.ap(m.map(p.first(s), firstMapping()), p.second(s)) } }

            private fun <T, R> firstMapping(): ((T) -> R) -> (Pair<T, S>) -> Pair<R, S> = { f -> { p: Pair<T, S> -> p.swap.times(f).swap } }

            override fun <T, R> bind(obj: _1<_1<_1<Companion, S>, M>, T>, f: (T) -> _1<_1<_1<Companion, S>, M>, R>): _1<_1<_1<Companion, S>, M>, R> =
                    obj.up.up.narrow.evalStateT() `$`
                            toStateT(m) { g -> { s: S -> m.bind(g(s), f + narrow() + { it.stateT(s) }) } }
        }

        fun <S, M> monadPlus(mp: MonadPlus<M>): MonadPlus<_1<_1<Companion, S>, M>> = object : MonadPlus<_1<_1<Companion, S>, M>> {

            private val monad: Monad<_1<_1<Companion, S>, M>> = Companion.monad(mp)

            override fun <T> empty(): _1<_1<_1<Companion, S>, M>, T> =
                    stateT(mp) { s -> mp.map(mp.mzero<T>()) { t -> t to s } }

            override fun <T, R> map(obj: _1<_1<_1<Companion, S>, M>, T>, f: (T) -> R): _1<_1<_1<Companion, S>, M>, R> =
                    this@Companion.map(Companion.narrow(obj), f)

            override fun <T> mzero(): _1<_1<_1<Companion, S>, M>, T> = empty()

            override fun <T> pure(value: T): _1<_1<_1<Companion, S>, M>, T> = stateT(mp) { s -> mp.pure(value to s) }

            override fun <T> mplus(x: _1<_1<_1<Companion, S>, M>, T>, y: _1<_1<_1<Companion, S>, M>, T>): _1<_1<_1<Companion, S>, M>, T> =
                    (x to y).both(Companion.narrow()).both { it.stateT } `$`
                            toStateT(mp) { p -> { s -> mp.mplus(p.first(s), p.second(s)) } }

            override fun <T> `_+_`(): (_1<_1<_1<Companion, S>, M>, T>) -> (_1<_1<_1<Companion, S>, M>, T>) -> _1<_1<_1<Companion, S>, M>, T> =
                    { x -> { y -> mplus(x, y) } }

            override fun <T, R, G : (T) -> R> _1<_1<_1<Companion, S>, M>, G>.`(_)`(obj: _1<_1<_1<Companion, S>, M>, T>): _1<_1<_1<Companion, S>, M>, R> =
                    monad.ap(this, obj)

            override fun <T, R> bind(obj: _1<_1<_1<Companion, S>, M>, T>, f: (T) -> _1<_1<_1<Companion, S>, M>, R>): _1<_1<_1<Companion, S>, M>, R> =
                    monad.bind(obj, f)
        }
    }
}

val <S, T> _3<StateT.Companion, S, Identity.Companion, T>.narrow: State<S, T> get() = this as State<S, T>
val <S, M, T> _3<StateT.Companion, S, M, T>.narrow: StateT<S, M, T> get() = this as StateT<S, M, T>

class State<S, T>(val state: (S) -> Pair<T, S>) : StateT<S, Identity.Companion, T>() {

    override val stateT: (S) -> _1<Identity.Companion, Pair<T, S>>
        get() = state + Identity.toIdentity()

    override val mn: Monad<Identity.Companion>
        get() = Identity.monad

    val execState: (S) -> S get() = { s: S -> state(s).second }

    val evalState: (S) -> T get() = { s: S -> state(s).first }

    companion object {

        fun <S> get(): State<S, S> = state { it to it }

        fun <S, T> gets(f: (S) -> T): State<S, T> = state { f(it) to it }

        fun <S> put(s: S): State<S, Unit> = state { Unit to s }

        fun <S, T, R> mapState(obj: State<S, T>, f: (Pair<T, S>) -> Pair<R, S>): State<S, R> = state(obj.state + f)

        fun <S, T> withState(obj: State<S, T>, f: (S) -> S): State<S, T> = state(f + obj.state)

        fun <S> modify(f: (S) -> S): State<S, Unit> = state { Unit to f(it) }

        fun <S, T> narrow(obj: _1<_1<_1<StateT.Companion, S>, Identity.Companion>, T>): State<S, T> = obj.up.up.narrow

        fun <S, T> narrow(): (_1<_1<_1<StateT.Companion, S>, Identity.Companion>, T>) -> State<S, T> = { it.up.up.narrow }

        fun <S, T> state(f: (S) -> Pair<T, S>): State<S, T> = State(f)

        fun <P, S, T> toState(f: (P) -> ((S) -> Pair<T, S>)): (P) -> State<S, T> = { p: P -> State(f(p)) }

        fun <S, T, R> map(obj: State<S, T>, f: (T) -> R): State<S, R> = State { s -> obj.state(s) / f }

        fun <S> monad(): Monad<_1<_1<StateT.Companion, S>, Identity.Companion>> = object : Monad<_1<_1<StateT.Companion, S>, Identity.Companion>> {

            override fun <T, R> map(obj: _1<_1<_1<StateT.Companion, S>, Identity.Companion>, T>, f: (T) -> R): _1<_1<_1<StateT.Companion, S>, Identity.Companion>, R> =
                    this@Companion.map(Companion.narrow(obj), f)

            override fun <T> pure(value: T): _1<_1<_1<StateT.Companion, S>, Identity.Companion>, T> = State { s -> Pair(value, s) }

            override fun <T, R, G : (T) -> R> _1<_1<_1<StateT.Companion, S>, Identity.Companion>, G>.`(_)`(obj: _1<_1<_1<StateT.Companion, S>, Identity.Companion>, T>): _1<_1<_1<StateT.Companion, S>, Identity.Companion>, R> =
                    Companion.narrow(this).evalState to Companion.narrow(obj).state `$`
                            toState { p -> { s -> p.second(s) / p.first(s) } }

            override fun <T, R> bind(obj: _1<_1<_1<StateT.Companion, S>, Identity.Companion>, T>, f: (T) -> _1<_1<_1<StateT.Companion, S>, Identity.Companion>, R>): _1<_1<_1<StateT.Companion, S>, Identity.Companion>, R> =
                    Companion.narrow(obj).evalState `$`
                            toState { es -> { s -> narrow(f(es(s))).state(s) } }
        }
    }
}
