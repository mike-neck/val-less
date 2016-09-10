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
package valless.type.data.functor

import valless.type._1
import valless.type.control.applicative.Applicative
import valless.type.control.monad.Monad
import valless.type.data.*
import valless.type.data.monoid.Monoid
import valless.util.both
import valless.util.function.`$`
import valless.util.function.times

data class Identity<E>(val identity: E) : _1<Identity.Companion, E> {

    companion object :
            Eq.Deriving<Companion>
            , Ord.Deriving<Companion>
            , Monoid.Deriving<Companion>
            , Traversable._1_<Companion>
            , Monad._1_<Companion> {
        fun <T> runIdentityN(): (_1<Companion, T>) -> T = { it.narrow.identity }

        fun <T> toIdentity(): (T) -> Identity<T> = { Identity(it) }

        fun <T, R> map(ta: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                ta.narrow.identity `$` f `$` toIdentity()

        override fun <T> eq(e: Eq<T>): Eq<_1<Companion, T>> = Eq.deriveFrom(e, runIdentityN())

        override fun <T> ord(o: Ord<T>): Ord<_1<Companion, T>> = Ord.deriveFrom(o, runIdentityN())

        override fun <T> monoid(m: Monoid<T>): Monoid<_1<Companion, T>> = object : Monoid<_1<Companion, T>> {

            override fun empty(): _1<Companion, T> = Identity(m.mempty)

            override fun append(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                    (x.narrow to y.narrow).both { it.identity } `$`
                            { m.append(it.first, it.second) } `$` toIdentity()
        }

        override val monad: Monad<Companion> get() = object : Monad<Companion> {

            override fun <T> pure(value: T): _1<Companion, T> = Identity(value)

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    this@Companion.map(obj, f)

            override fun <T, R, G : (T) -> R> _1<Companion, G>.`(_)`(obj: _1<Companion, T>): _1<Companion, R> =
                    this.narrow.identity * obj.narrow.identity `$` toIdentity()

            override fun <T, R> bind(obj: _1<Companion, T>, f: (T) -> _1<Companion, R>): _1<Companion, R> =
                    obj.narrow.identity `$` f
        }

        override val traversable: Traversable<Companion> get() = object : Traversable<Companion> {

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    this@Companion.map(obj, f)

            override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<Companion, P>, f: (P) -> _1<F, R>): _1<F, _1<Companion, R>> =
                    ta.narrow.identity `$` f `$` m.map(toIdentity())

            override fun <T, R> foldr(ta: _1<Companion, T>, init: R, f: (T) -> (R) -> R): R =
                    (ta.narrow.identity `$` f) * init

            override fun <T, R> foldMap(m: Monoid<R>, ta: _1<Companion, T>, f: (T) -> R): R =
                    ta.narrow.identity `$` f

            override fun <T> isNull(ta: _1<Companion, T>): Bool = Bool.False

            override fun <T> size(ta: _1<Companion, T>): Int = 1

            override fun <T> sum(n: Num<T>, xs: _1<Companion, T>): T = xs.narrow.identity

            override fun <T> product(n: Num<T>, xs: _1<Companion, T>): T = xs.narrow.identity

            override fun <T> elem(e: Eq<T>, sbj: T, xs: _1<Companion, T>): Bool = e.eq(xs.narrow.identity, sbj)

            override fun <T> any(ta: _1<Companion, T>, pred: (T) -> Bool): Bool = pred(ta.narrow.identity)

            override fun <T> all(ta: _1<Companion, T>, pred: (T) -> Bool): Bool = pred(ta.narrow.identity)

            override fun and(bs: _1<Companion, Bool>): Bool = bs.narrow.identity

            override fun or(bs: _1<Companion, Bool>): Bool = bs.narrow.identity
        }
    }
}

val <E> _1<Identity.Companion, E>.narrow: Identity<E> get() = this as Identity<E>
