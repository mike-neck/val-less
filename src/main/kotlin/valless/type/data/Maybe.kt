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
import valless.type.control.monad.MonadPlus
import valless.util.function.`$`

sealed class Maybe<T> : _1<Maybe.Companion, T> {

    class Nothing<T> : Maybe<T>()

    class Just<T>(val value: T) : Maybe<T>()

    companion object : MonadPlus._1_<Companion> {
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
