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
import valless.util.function.times

class First<T>(val getFirst: Maybe<T>) : _1<First.Companion, T> {

    companion object :
            Eq.Deriving<Companion>
            , Ord.Deriving<Companion>
            , Monad._1_<Companion>
            , Traversable._1_<Companion>
            , Monoid._2_<Companion> {

        fun <T> toFirst(): (Maybe<T>) -> First<T> = ::First

        override fun <T> eq(e: Eq<T>): Eq<_1<Companion, T>> = object : Eq<_1<Companion, T>> {
            override fun eq(x: _1<Companion, T>, y: _1<Companion, T>): Bool =
                    Maybe.eq(e).eq(x.narrow.getFirst, y.narrow.getFirst)
        }

        override fun <T> ord(o: Ord<T>): Ord<_1<Companion, T>> = object : Ord<_1<Companion, T>> {
            override fun compare(x: _1<Companion, T>, y: _1<Companion, T>): Ordering =
                    Maybe.ord(o).compare(x.narrow.getFirst, y.narrow.getFirst)
        }

        override fun <T> monoid(): Monoid<_1<Companion, T>> = object : Monoid<_1<Companion, T>> {

            override fun empty(): _1<Companion, T> = First(Maybe.Nothing())

            override fun append(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                    maybeCompare(x.narrow.getFirst, y.narrow.getFirst)
                            .nothing { y }
                            .just_nothing { x }
                            .just_just { l, r -> x }
        }

        fun <T, R> map(obj: First<T>, f: (T) -> R): First<R> =
                obj.getFirst `$` Maybe.monad.map(f) `$` Maybe.narrow() `$` toFirst()

        override val monad: Monad<Companion> = object : Monad<Companion> {

            val mm: Monad<Maybe.Companion> = Maybe.monad

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    this@Companion.map(obj.narrow, f)

            override fun <T> pure(value: T): _1<Companion, T> = First(Maybe.Just(value))

            override fun <T, R, G : (T) -> R> _1<Companion, G>.`(_)`(obj: _1<Companion, T>): _1<Companion, R> =
                    mm.ap(this.narrow.getFirst, obj.narrow.getFirst) `$` Maybe.narrow() `$` toFirst()

            override fun <T, R> bind(obj: _1<Companion, T>, f: (T) -> _1<Companion, R>): _1<Companion, R> =
                    obj.narrow.getFirst `$` { v ->
                        when (v) {
                            is Maybe.Nothing -> First(Maybe.Nothing())
                            is Maybe.Just -> f(v.value)
                        }
                    }
        }

        override val traversable: Traversable<Companion> = object : Traversable<Companion> {

            val mt: Traversable<Maybe.Companion> = Maybe.traversable

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    this@Companion.map(obj.narrow, f)

            override fun <T, R> foldr(ta: _1<Companion, T>, init: R, f: (T) -> (R) -> R): R =
                    ta.narrow.getFirst `$` { mt.foldr(it, init, f) }

            override fun <T, R> foldMap(m: Monoid<R>, ta: _1<Companion, T>, f: (T) -> R): R =
                    ta.narrow.getFirst `$` { mt.foldMap(m, it, f) }

            override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<Companion, P>, f: (P) -> _1<F, R>): _1<F, _1<Companion, R>> =
                    (ta.narrow.getFirst `$` mt.traverse<P, R, F>(m)) * f `$`
                            m.map(Maybe.narrow<R>()) `$` m.map(toFirst())
        }

    }
}

val <T> _1<First.Companion, T>.narrow: First<T> get() = this as First<T>

class Last<T>(val getLast: Maybe<T>) : _1<Last.Companion, T> {

    companion object :
            Eq.Deriving<Companion>
            , Ord.Deriving<Companion>
            , Monad._1_<Companion>
            , Traversable._1_<Companion>
            , Monoid._2_<Companion> {

        fun <T> toLast(): (Maybe<T>) -> Last<T> = ::Last

        override fun <T> eq(e: Eq<T>): Eq<_1<Companion, T>> = object : Eq<_1<Companion, T>> {
            override fun eq(x: _1<Companion, T>, y: _1<Companion, T>): Bool =
                    Maybe.eq(e).eq(x.narrow.getLast, y.narrow.getLast)
        }

        override fun <T> ord(o: Ord<T>): Ord<_1<Companion, T>> = object : Ord<_1<Companion, T>> {
            override fun compare(x: _1<Companion, T>, y: _1<Companion, T>): Ordering =
                    Maybe.ord(o).compare(x.narrow.getLast, y.narrow.getLast)
        }

        override fun <T> monoid(): Monoid<_1<Companion, T>> = object : Monoid<_1<Companion, T>> {

            override fun empty(): _1<Companion, T> = Last(Maybe.Nothing())

            override fun append(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                    maybeCompare(x.narrow.getLast, y.narrow.getLast)
                            .nothing { y }
                            .just_nothing { x }
                            .just_just { l, r -> y }
        }

        fun <T, R> map(obj: Last<T>, f: (T) -> R): Last<R> =
                obj.getLast `$` Maybe.monad.map(f) `$` Maybe.narrow() `$` toLast<R>()

        override val monad: Monad<Companion> = object : Monad<Companion> {

            val mm: Monad<Maybe.Companion> = Maybe.monad

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    this@Companion.map(obj.narrow, f)

            override fun <T> pure(value: T): _1<Companion, T> = Last(Maybe.Just(value))

            override fun <T, R, G : (T) -> R> _1<Companion, G>.`(_)`(obj: _1<Companion, T>): _1<Companion, R> =
                    mm.ap(this.narrow.getLast, obj.narrow.getLast).narrow `$` toLast<R>()

            override fun <T, R> bind(obj: _1<Companion, T>, f: (T) -> _1<Companion, R>): _1<Companion, R> =
                    obj.narrow.getLast `$` {
                        when (it) {
                            is Maybe.Nothing -> Last(Maybe.Nothing())
                            is Maybe.Just -> f(it.value)
                        }
                    }
        }

        override val traversable: Traversable<Companion> = object : Traversable<Companion> {

            val mt: Traversable<Maybe.Companion> = Maybe.traversable

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    this@Companion.map(obj.narrow, f)

            override fun <T, R> foldr(ta: _1<Companion, T>, init: R, f: (T) -> (R) -> R): R =
                    ta.narrow.getLast `$` { mt.foldr(it, init, f) }

            override fun <T, R> foldMap(m: Monoid<R>, ta: _1<Companion, T>, f: (T) -> R): R =
                    ta.narrow.getLast `$` { mt.foldMap(m, it, f) }

            override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<Companion, P>, f: (P) -> _1<F, R>): _1<F, _1<Companion, R>> =
                    (ta.narrow.getLast `$` mt.traverse<P, R, F>(m)) * f `$`
                            m.map(Maybe.narrow<R>()) `$` m.map(toLast())
        }
    }
}

val <T> _1<Last.Companion, T>.narrow: Last<T> get() = this as Last<T>
