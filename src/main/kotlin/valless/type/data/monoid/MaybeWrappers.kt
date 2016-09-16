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
package valless.type.data.monoid

import valless.type._1
import valless.type.control.applicative.Applicative
import valless.type.control.monad.Monad
import valless.type.data.*
import valless.type.data.functor.classes.maybeCompare
import valless.util.function.`$`

class First<T>(val getFirst: Maybe<T>) : _1<First.Companion, T> {

    companion object :
            Eq.Deriving<Companion>
            , Ord.Deriving<Companion>
            , Monad._1_<Companion>
            , Traversable._1_<Companion>
            , Monoid._2_<Companion> {

        override fun <T> eq(e: Eq<T>): Eq<_1<Companion, T>> = object : Eq<_1<Companion, T>> {
            override fun eq(x: _1<Companion, T>, y: _1<Companion, T>): Bool =
                    maybeCompare(x.narrow.getFirst, y.narrow.getFirst)
                            .nothing_nothing { Bool.True }
                            .nothing_just { Bool.False }
                            .just_nothing { Bool.False }
                            .just_just { l, r -> e.eq(l, r) }
        }

        override fun <T> ord(o: Ord<T>): Ord<_1<Companion, T>> = object : Ord<_1<Companion, T>> {
            override fun compare(x: _1<Companion, T>, y: _1<Companion, T>): Ordering =
                    maybeCompare(x.narrow.getFirst, y.narrow.getFirst)
                            .nothing_nothing { Ordering.EQ }
                            .nothing_just { Ordering.LT }
                            .just_nothing { Ordering.GT }
                            .just_just { l, r -> o.compare(l, r) }
        }

        override fun <T> monoid(): Monoid<_1<Companion, T>> = object : Monoid<_1<Companion, T>> {

            override fun empty(): _1<Companion, T> = First(Maybe.Nothing())

            override fun append(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                    maybeCompare(x.narrow.getFirst, y.narrow.getFirst)
                            .nothing_nothing<_1<Companion, T>> { First<T>(Maybe.Nothing()) }
                            .nothing_just { y }
                            .just_nothing { x }
                            .just_just { l, r -> x }
        }

        fun <T, R> map(obj: First<T>, f: (T) -> R): First<R> =
                obj.getFirst `$` {
                    when (it) {
                        is Maybe.Nothing -> First(Maybe.Nothing())
                        is Maybe.Just -> First(Maybe.Just(f(it.value)))
                    }
                }

        override val monad: Monad<Companion> = object : Monad<Companion> {

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    this@Companion.map(obj.narrow, f)

            override fun <T> pure(value: T): _1<Companion, T> = First(Maybe.Just(value))

            override fun <T, R, G : (T) -> R> _1<Companion, G>.`(_)`(obj: _1<Companion, T>): _1<Companion, R> =
                    maybeCompare(this.narrow.getFirst, obj.narrow.getFirst)
                            .nothing { First<R>(Maybe.Nothing()) }
                            .just_nothing { First(Maybe.Nothing()) }
                            .just_just { f, v -> First(Maybe.Just(f(v))) }

            override fun <T, R> bind(obj: _1<Companion, T>, f: (T) -> _1<Companion, R>): _1<Companion, R> =
                    obj.narrow.getFirst `$` { v ->
                        when (v) {
                            is Maybe.Nothing -> First(Maybe.Nothing())
                            is Maybe.Just -> f(v.value)
                        }
                    }
        }

        override val traversable: Traversable<Companion> = object : Traversable<Companion> {

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    this@Companion.map(obj.narrow, f)

            override fun <T, R> foldr(ta: _1<Companion, T>, init: R, f: (T) -> (R) -> R): R =
                    ta.narrow.getFirst `$` {
                        when (it) {
                            is Maybe.Nothing -> init
                            is Maybe.Just -> f(it.value)(init)
                        }
                    }

            override fun <T, R> foldMap(m: Monoid<R>, ta: _1<Companion, T>, f: (T) -> R): R =
                    ta.narrow.getFirst `$` {
                        when (it) {
                            is Maybe.Nothing -> m.mempty
                            is Maybe.Just -> f(it.value)
                        }
                    }

            override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<Companion, P>, f: (P) -> _1<F, R>): _1<F, _1<Companion, R>> =
                    ta.narrow.getFirst `$` {
                        when (it) {
                            is Maybe.Nothing -> m.pure(First(Maybe.Nothing()))
                            is Maybe.Just -> m.map<R, _1<Companion, R>> { r: R -> First(Maybe.Just(r)) }(f(it.value))
                        }
                    }
        }

    }
}

val <T> _1<First.Companion, T>.narrow: First<T> get() = this as First<T>

