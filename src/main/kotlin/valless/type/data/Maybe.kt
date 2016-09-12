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
            , Ord.Deriving<Companion>
            , Monoid.Deriving<Companion>
            , Traversable._1_<Companion> {
        override fun <T> eq(e: Eq<T>): Eq<_1<Companion, T>> = object : Eq<_1<Companion, T>> {

            override fun eq(x: _1<Companion, T>, y: _1<Companion, T>): Bool =
                    comparing(x, y)
                            .nothing_nothing { Bool.True }
                            .nothing_just { Bool.False }
                            .just_nothing { Bool.False }
                            .just_just { xn, yn -> e.eq(xn, yn) }
        }

        override fun <T> ord(o: Ord<T>): Ord<_1<Companion, T>> = object : Ord<_1<Companion, T>> {

            override fun compare(x: _1<Companion, T>, y: _1<Companion, T>): Ordering =
                    comparing(x, y)
                            .nothing_nothing { Ordering.EQ }
                            .nothing_just { Ordering.LT }
                            .just_nothing { Ordering.GT }
                            .just_just { xn, yn -> o.compare(xn, yn) }
        }

        override fun <T> monoid(m: Monoid<T>): Monoid<_1<Companion, T>> = object : Monoid<_1<Companion, T>> {

            override fun empty(): _1<Companion, T> = Nothing()

            override fun append(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                    comparing(x, y)
                            .nothing_nothing { y }
                            .nothing_just { y }
                            .just_nothing { x }
                            .just_just { xn, yn -> m.append(xn, yn) `$` { Just(it) } }
        }

        override val foldable: Foldable<Companion> get() = traversable

        override val traversable: Traversable<Companion>
            get() = object : Traversable<Companion> {

                override fun <P, R, M> traverse(m: Applicative<M>, ta: _1<Companion, P>, f: (P) -> _1<M, R>): _1<M, _1<Companion, R>> =
                        inspect<P, _1<M, _1<Companion, R>>>(ta)
                                .nothing { m.pure(Nothing()) }
                                .just { m.map(f(it)) { Just(it) } }

                override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> = monadPlus.map(obj, f)

                override fun <T, R> foldr(ta: _1<Companion, T>, init: R, f: (T) -> (R) -> R): R =
                        inspect<T, R>(ta)
                                .nothing { init }
                                .just { f(it)(init) }

                override fun <T, R> foldl(ta: _1<Companion, T>, init: R, f: (R) -> (T) -> R): R =
                        inspect<T, R>(ta)
                                .nothing { init }
                                .just { f(init)(it) }
            }

        override val monad: Monad<Companion> get() = monadPlus

        override val monadPlus: MonadPlus<Companion>
            get() = object : MonadPlus<Companion> {
                override fun <T> empty(): _1<Companion, T> = Nothing()

                override fun <T> mzero(): _1<Companion, T> = empty()

                override fun <T> mplus(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                        inspect<T, _1<Companion, T>>(x)
                                .nothing { y }
                                .just { x }

                override fun <T> `_+_`(): (_1<Companion, T>) -> (_1<Companion, T>) -> _1<Companion, T> =
                        { x -> { y -> mplus(x, y) } }

                override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                        inspect<T, _1<Companion, R>>(obj)
                                .nothing { Nothing() }
                                .just { Just(f(it)) }

                override fun <T> pure(value: T): _1<Companion, T> = Just(value)

                override fun <T, R, G : (T) -> R> _1<Companion, G>.`(_)`(obj: _1<Companion, T>): _1<Companion, R> =
                        inspect<T, _1<Companion, R>>(obj)
                                .nothing { Nothing() }
                                .just { o ->
                                    inspect<G, _1<Companion, R>>(this)
                                            .nothing { Nothing() }
                                            .just { Just(it(o)) }
                                }

                override fun <T, R> bind(obj: _1<Companion, T>, f: (T) -> _1<Companion, R>): _1<Companion, R> =
                        inspect<T, _1<Companion, R>>(obj)
                                .nothing { Nothing() }
                                .just { f(it).narrow }
            }
    }
}

val <T> _1<Maybe.Companion, T>.narrow: Maybe<T> get() = this as Maybe<T>

val <T> Maybe<T>.wide: _1<Maybe.Companion, T> get() = this

private fun <T, R> inspect(x: _1<Maybe.Companion, T>): WhenNothing<T, R> = object : WhenNothing<T, R> {
    override fun nothing(nf: () -> R): WhenJust<T, R> = object : WhenJust<T, R> {
        override fun just(jf: (T) -> R): R = x.narrow `$` {
            when (it) {
                is Maybe.Nothing -> nf()
                is Maybe.Just -> jf(it.value)
            }
        }
    }
}

private interface WhenNothing<out T, R> {
    fun nothing(nf: () -> R): WhenJust<T, R>
}

private interface WhenJust<out T, R> {
    fun just(jf: (T) -> R): R
}

private fun <T> comparing(x: _1<Maybe.Companion, T>, y: _1<Maybe.Companion, T>): WhenBothNothing<T> = object : WhenBothNothing<T> {
    override fun <R> nothing(fn: () -> R): WhenLeftJustRightNothing<T, R> =
            nothing_nothing { fn() }
                    .nothing_just { fn() }

    override fun <R> nothing_nothing(f1: () -> R): WhenLeftNothingRightJust<T, R> = object : WhenLeftNothingRightJust<T, R> {
        override fun nothing_just(f2: (T) -> R): WhenLeftJustRightNothing<T, R> = object : WhenLeftJustRightNothing<T, R> {
            override fun just_nothing(f3: (T) -> R): WhenBothJust<T, R> = object : WhenBothJust<T, R> {
                override fun just_just(f4: (T, T) -> R): R = x.narrow `$` { xn ->
                    y.narrow `$` { yn ->
                        when (xn) {
                            is Maybe.Nothing -> when (yn) {
                                is Maybe.Nothing -> f1()
                                is Maybe.Just -> f2(yn.value)
                            }
                            is Maybe.Just -> when (yn) {
                                is Maybe.Nothing -> f3(xn.value)
                                is Maybe.Just -> f4(xn.value, yn.value)
                            }
                        }
                    }
                }
            }
        }
    }
}

private interface WhenBothNothing<out T> {
    fun <R> nothing_nothing(f1: () -> R): WhenLeftNothingRightJust<T, R>
    fun <R> nothing(fn: () -> R): WhenLeftJustRightNothing<T, R>
}

private interface WhenLeftNothingRightJust<out T, R> {
    fun nothing_just(f2: (T) -> R): WhenLeftJustRightNothing<T, R>
}

private interface WhenLeftJustRightNothing<out T, R> {
    fun just_nothing(f3: (T) -> R): WhenBothJust<T, R>
}

private interface WhenBothJust<out T, R> {
    fun just_just(f4: (T, T) -> R): R
}


