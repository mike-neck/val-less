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
import valless.type.control.applicative.Applicative
import valless.type.control.monad.Monad
import valless.type.control.monad.MonadPlus
import valless.type.data.monoid.Monoid
import valless.util.function.`$`

sealed class Maybe<T> : _1<Maybe.Companion, T> {

    class Nothing<T> : Maybe<T>()

    class Just<T>(val value: T) : Maybe<T>()

    companion object : MonadPlus._1_<Companion>
            , Monad._1_<Companion>
            , Foldable._1_<Companion>
            , Eq.Deriving<Companion>
            , Traversable._1_<Companion> {

        override fun <T> eq(e: Eq<T>): Eq<_1<Companion, T>> = object : Eq<_1<Companion, T>> {

            override fun eq(x: _1<Companion, T>, y: _1<Companion, T>): Bool =
                    x.narrow `$` { xn ->
                        when (xn) {
                            is Nothing -> Eq.booleanToBool(y.narrow is Nothing)
                            is Just -> y.narrow `$` {
                                when (it) {
                                    is Nothing -> Bool.False
                                    is Just -> e.eq(xn.value, it.value)
                                }
                            }
                        }
                    }
        }

        fun <T> monoid(m: Monoid<T>): Monoid<_1<Companion, T>> = object : Monoid<_1<Companion, T>> {

            override fun empty(): _1<Companion, T> = Nothing()

            override fun append(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                    x.narrow `$` { xn ->
                        when (xn) {
                            is Nothing -> y
                            is Just -> y.narrow `$` {
                                when (it) {
                                    is Nothing -> xn
                                    is Just -> m.append(xn.value, it.value) `$` { Just(it) }
                                }
                            }
                        }
                    }
        }

        override val foldable: Foldable<Companion> get() = traversable

        override val traversable: Traversable<Companion>
            get() = object : Traversable<Companion> {

                override fun <P, R, M> traverse(m: Applicative<M>, ta: _1<Companion, P>, f: (P) -> _1<M, R>): _1<M, _1<Companion, R>> =
                        ta.narrow `$` {
                            when (it) {
                                is Nothing -> m.pure(Nothing())
                                is Just -> m.map(f(it.value)) { Just(it) }
                            }
                        }

                override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> = monadPlus.map(obj, f)

                override fun <T, R> foldr(ta: _1<Companion, T>, init: R, f: (T) -> (R) -> R): R =
                        ta.narrow `$` {
                            when (it) {
                                is Nothing -> init
                                is Just -> f(it.value)(init)
                            }
                        }

                override fun <T, R> foldl(ta: _1<Companion, T>, init: R, f: (R) -> (T) -> R): R =
                        ta.narrow `$` {
                            when (it) {
                                is Nothing -> init
                                is Just -> f(init)(it.value)
                            }
                        }
            }

        override val monad: Monad<Companion> get() = monadPlus

        override val monadPlus: MonadPlus<Companion>
            get() = object : MonadPlus<Companion> {
                override fun <T> empty(): _1<Companion, T> = Nothing()

                override fun <T> mzero(): _1<Companion, T> = empty()

                override fun <T> mplus(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                        x.narrow `$` {
                            when (it) {
                                is Just -> it
                                is Nothing -> y
                            }
                        }

                override fun <T> `(+)`(): (_1<Companion, T>) -> (_1<Companion, T>) -> _1<Companion, T> =
                        { x -> { y -> mplus(x, y) } }

                override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                        obj.narrow `$` {
                            when (it) {
                                is Nothing -> Nothing()
                                is Just -> Just(f(it.value))
                            }
                        }

                override fun <T> pure(value: T): _1<Companion, T> = Just(value)

                override fun <T, R, G : (T) -> R> _1<Companion, G>.`(_)`(obj: _1<Companion, T>): _1<Companion, R> =
                        obj.narrow `$` { o ->
                            when (o) {
                                is Nothing -> Nothing()
                                is Just -> this.narrow `$` {
                                    when (it) {
                                        is Nothing -> Nothing<R>()
                                        is Just -> Just(it.value(o.value))
                                    }
                                }
                            }
                        }

                override fun <T, R> bind(obj: _1<Companion, T>, f: (T) -> _1<Companion, R>): _1<Companion, R> =
                        obj.narrow `$` {
                            when (it) {
                                is Nothing -> Nothing()
                                is Just -> f(it.value)
                            }
                        }
            }
    }
}

val <T> _1<Maybe.Companion, T>.narrow: Maybe<T> get() = this as Maybe<T>
