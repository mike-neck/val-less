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
package valless.type.control

import valless.type._1
import valless.type._3
import valless.type.control.monad.Monad
import valless.type.up
import valless.util.function.`$`
import valless.util.function.plus

class Kleisli<M, T, R>(val mn: Monad<M>, val runKleisli: (T) -> _1<M, R>) : _3<Kleisli.Companion, M, T, R> {

    companion object {

        fun <M, P, Q> narrow(obj: _1<_1<_1<Companion, M>, P>, Q>): Kleisli<M, P, Q> = obj.up.up.narrow

        fun <M, P, Q> toKleisli(mn: Monad<M>): ((P) -> _1<M, Q>) -> Kleisli<M, P, Q> = { Kleisli(mn, it) }

        fun <M> cat(mn: Monad<M>): Category<_1<Companion, M>> = object : Category<_1<Companion, M>> {
            override fun <T> id(): _1<_1<_1<Companion, M>, T>, T> = Kleisli(mn) { mn.pure(it) }

            override fun <P, Q, R> plus(pq: _1<_1<_1<Companion, M>, P>, Q>, qr: _1<_1<_1<Companion, M>, Q>, R>): _1<_1<_1<Companion, M>, P>, R> =
                    Companion.narrow(pq) to Companion.narrow(qr) `$`
                            { p: Pair<Kleisli<M, P, Q>, Kleisli<M, Q, R>> ->
                                { a: P -> mn.bind(p.first.runKleisli(a), p.second.runKleisli) }
                            } `$`
                            toKleisli(mn)
        }

        fun <M> arrow(mn: Monad<M>): Arrow<_1<Companion, M>> = object : Arrow<_1<Companion, M>> {

            override val cat: Category<_1<Companion, M>> = Companion.cat(mn)

            override fun <P, R> arr(f: (P) -> R): _1<_1<_1<Companion, M>, P>, R> = Kleisli(mn, f + { mn.pure(it) })

            override fun <P, Q, T, U> split(f: _1<_1<_1<Companion, M>, P>, T>, g: _1<_1<_1<Companion, M>, Q>, U>): _1<_1<_1<Companion, M>, Pair<P, Q>>, Pair<T, U>> =
                    defaultSplit(f, g)

            override fun <P, R, Q> first(ar: _1<_1<_1<Companion, M>, P>, R>): _1<_1<_1<Companion, M>, Pair<P, Q>>, Pair<R, Q>> =
                    Companion.narrow(ar).runKleisli `$`
                            { f -> { p: Pair<P, Q> -> mn.bind(f(p.first)) { mn.pure(it to p.second) } } } `$`
                            toKleisli(mn)

            override fun <P, R, Q> second(ar: _1<_1<_1<Companion, M>, P>, R>): _1<_1<_1<Companion, M>, Pair<Q, P>>, Pair<Q, R>> =
                    Companion.narrow(ar).runKleisli `$`
                            { f -> { p: Pair<Q, P> -> mn.bind(f(p.second)) { mn.pure(p.first to it) } } } `$`
                            toKleisli(mn)
        }
    }
}

val <M, T, R> _3<Kleisli.Companion, M, T, R>.narrow: Kleisli<M, T, R> get() = this as Kleisli<M, T, R>
