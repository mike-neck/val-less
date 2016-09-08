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
import valless.type.data.functor.Functor
import valless.util.both
import valless.util.function.`$`
import valless.util.function.times

data class Dual<T>(val dual: T) : _1<Dual.Companion, T> {

    companion object :
            Eq.Deriving<Companion>
            , Ord.Deriving<Companion>
            , Monoid.Deriving<Companion>
            , Functor._1_<Companion>
            , Foldable._1_<Companion>
            , Traversable._1_<Companion>
            , Monad._1_<Companion> {
        override fun <T> eq(e: Eq<T>): Eq<_1<Companion, T>> = object : Eq<_1<Companion, T>> {
            override fun eq(x: _1<Companion, T>, y: _1<Companion, T>): Bool =
                    (x.narrow to y.narrow).both { it.dual } `$` { e.eq(it.first, it.second) }
        }

        override fun <T> ord(o: Ord<T>): Ord<_1<Companion, T>> = object : Ord<_1<Companion, T>> {
            override fun compare(x: _1<Companion, T>, y: _1<Companion, T>): Ordering =
                    (x.narrow to y.narrow).both { it.dual } `$` { o.compare(it.first, it.second) }
        }

        override val functor: Functor<Companion> get() = object : Functor<Companion> {
            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    obj.narrow.dual `$` f `$` toDual()
        }

        override val foldable: Foldable<Companion> get() = traversable

        override val traversable: Traversable<Companion> get() = object : Traversable<Companion> {
            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> = functor.map(obj, f)

            override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<Companion, P>, f: (P) -> _1<F, R>): _1<F, _1<Companion, R>> =
                    ta.narrow.dual `$` f `$` m.map { Dual(it) }

            override fun <T, R> foldr(ta: _1<Companion, T>, init: R, f: (T) -> (R) -> R): R = (ta.narrow.dual `$` f) * init

            override fun <T, R> foldl(ta: _1<Companion, T>, init: R, f: (R) -> (T) -> R): R = ta.narrow.dual `$` f(init)

            override fun <T, R> foldMap(m: Monoid<R>, ta: _1<Companion, T>, f: (T) -> R): R = ta.narrow.dual `$` f

            override fun <T> fold(m: Monoid<T>, tm: _1<Companion, T>): T = tm.narrow.dual

            override fun <T> elem(e: Eq<T>, sbj: T, xs: _1<Companion, T>): Bool = e.eq(sbj, xs.narrow.dual)

            override fun sum(xs: _1<Companion, Int>): Int = xs.narrow.dual

            override fun product(xs: _1<Companion, Long>): Long = xs.narrow.dual
        }

        fun <T> toDual(): (T) -> Dual<T> = ::Dual

        override fun <T> monoid(m: Monoid<T>): Monoid<_1<Companion, T>> = object : Monoid<_1<Companion, T>> {
            override fun empty(): _1<Companion, T> = Dual(m.mempty)

            override fun append(
                    x: _1<Companion, T>,
                    y: _1<Companion, T>
            ): _1<Companion, T> =
                    (x.narrow to y.narrow).both { it.dual }
                            .let { m.append(it.first, it.second) }
                            .let { Dual(it) }
        }

        override val monad: Monad<Companion> get() = object : Monad<Companion> {
            override fun <T> pure(value: T): _1<Companion, T> = Dual(value)

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> = functor.map(obj, f)

            override fun <T, R, G : (T) -> R> _1<Companion, G>.`(_)`(obj: _1<Companion, T>): _1<Companion, R> =
                    obj.narrow.dual `$` this.narrow.dual `$` toDual()

            override fun <T, R> bind(obj: _1<Companion, T>, f: (T) -> _1<Companion, R>): _1<Companion, R> =
                    obj.narrow.dual `$` f
        }
    }
}

val <T> _1<Dual.Companion, T>.narrow: Dual<T> get() = this as Dual<T>
