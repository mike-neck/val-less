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
import valless.util.function.plus
import valless.util.swap
import valless.util.times

sealed class StateT<S, M, T> : _3<StateT.Companion, S, M, T> {

    abstract val stateT: (S) -> _1<M, Pair<T, S>>

    fun execStateT(m: Monad<M>): (S) -> _1<M, S> = { s -> m.map(stateT(s)) { it.second } }

    fun evalStateT(m: Monad<M>): (S) -> _1<M, T> = { s -> m.map(stateT(s)) { it.first } }

    private class StateTImpl<S, M, T>(val mon: Monad<M>, override val stateT: (S) -> _1<M, Pair<T, S>>) : StateT<S, M, T>()

    class State<S, T>(override val stateT: (S) -> _1<Identity.Companion, Pair<T, S>>) : StateT<S, Identity.Companion, T>() {

        val state: (S) -> Pair<T, S> get() = stateT + Identity.runIdentityN()

        val execState: (S) -> S = execStateT(Identity.monad) + Identity.runIdentityN()

        val evalState: (S) -> T = evalStateT(Identity.monad) + Identity.runIdentityN()
    }

    companion object {

        fun <S, T> state(f: (S) -> Pair<T, S>): State<S, T> = State(f + Identity.toIdentity())

        fun <S, M, T> stateT(m: Monad<M>, f: (S) -> _1<M, Pair<T, S>>): StateT<S, M, T> = StateTImpl(m, f)

        fun <S, M, T, R> map(obj: StateT<S, M, T>, f: (T) -> R) = when (obj) {
            is StateTImpl -> stateT(obj.mon) { s: S -> obj.mon.map(obj.stateT(s)) { it.swap.times(f).swap } }
            is State -> state { s: S -> obj.state(s).swap.times(f).swap }
        }
    }
}

