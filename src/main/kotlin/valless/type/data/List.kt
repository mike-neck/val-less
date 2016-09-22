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
import valless.type.control.monad.MonadPlus
import valless.type.data.list.ListFunctions
import valless.type.data.monoid.Monoid
import valless.util.function.`$`

sealed class List<E> : Iterable<E>, _1<List.Companion, E> {

    class Nil<E> : List<E>()

    class Cons<E>(val head: E, val tail: List<E>) : List<E>()

    override fun iterator(): Iterator<E> = when (this) {
        is Nil -> object : Iterator<E> {
            override fun hasNext(): Boolean = false
            override fun next(): E = throw IllegalStateException("Nil does not have any item.")
        }
        is Cons -> object : Iterator<E> {

            var list: List<E> = this@List

            override fun hasNext(): Boolean = list is Cons

            override fun next(): E = when (list) {
                is Nil -> throw IllegalStateException("Nil does not have any item.")
                is Cons -> (list as Cons<E>)
                        .let { it to it.head }
                        .apply { list = this.first.tail }
                        .let { it.second }
            }
        }
    }

    override fun toString(): String = joinToString(",", "[", "]")

    companion object :
            Eq.Deriving<Companion>
            , Ord.Deriving<Companion>
            , Monoid._2_<Companion>
            , Traversable._1_<Companion>
            , MonadPlus._1_<Companion> {
        fun <E> of(value: E): List<E> = Cons(value, Nil())

        fun <E> of(vararg value: E): List<E> = ListFunctions.create(kotlin.collections.listOf(*value))

        fun <E> empty(): List<E> = Nil()

        fun <E> filter(list: List<E>, pred: (E) -> Bool): List<E> = ListFunctions.filter(list, pred)

        fun <E> sort(ord: Ord<E>): (List<E>) -> List<E> = { sort(ord, it) }

        fun <E> sortBy(by: (E) -> (E) -> Ordering): (List<E>) -> List<E> = { sort(it, by) }

        fun <E> sort(ord: Ord<E>, list: List<E>): List<E> = sort(list, ord.compare)

        fun <E> sort(list: List<E>, by: (E) -> (E) -> Ordering): List<E> = when (list) {
            is Nil -> list
            is Cons -> ListFunctions.sort(list, by)
        }

        fun <E> nub(eq: Eq<E>, list: List<E>): List<E> = nubBy(list, eq.eq)

        fun <E> nubBy(list: List<E>, cond: (E) -> (E) -> Bool): List<E> = ListFunctions.nub(list = list, cond = cond)

        fun <E> drop(num: Int, list: List<E>): List<E> = ListFunctions.drop(num, list)

        fun <E> dropWhile(list: List<E>, cond: (E) -> Bool): List<E> = ListFunctions.dropWhile(list, cond)

        fun <P, Q> zip(left: List<P>, right: List<Q>): List<Pair<P, Q>> = ListFunctions.zip(left, right)

        override fun <T> eq(e: Eq<T>): Eq<_1<Companion, T>> = object : Eq<_1<Companion, T>> {
            override fun eq(x: _1<Companion, T>, y: _1<Companion, T>): Bool =
                    ListFunctions.eq(e, x.narrow, y.narrow)
        }

        override fun <T> ord(o: Ord<T>): Ord<_1<Companion, T>> = object : Ord<_1<Companion, T>> {
            override fun compare(x: _1<Companion, T>, y: _1<Companion, T>): Ordering =
                    ListFunctions.compare(o, x.narrow, y.narrow)
        }

        override fun <T> monoid(): Monoid<_1<Companion, T>> = object : Monoid<_1<Companion, T>> {
            override fun empty(): _1<Companion, T> = Nil()

            override fun append(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                    ListFunctions.plus(x.narrow, y.narrow)
        }

        override val traversable: Traversable<Companion> get() = object : Traversable<Companion> {
            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    ListFunctions.map(obj.narrow, f)

            override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<Companion, P>, f: (P) -> _1<F, R>): _1<F, _1<Companion, R>> =
                    ListFunctions.foldr(ta.narrow, consf(m, f)) { m.pure(Nil()) }

            fun <F, T, R> consf(m: Applicative<F>, f: (T) -> _1<F, R>): (T) -> (_1<F, _1<Companion, R>>) -> (_1<F, _1<Companion, R>>) =
                    { x -> { fl -> m.ap(liftConcat<F, R>(m)(f(x)), fl) } }

            fun <E> concatList(): (E) -> (_1<Companion, E>) -> _1<Companion, E> = { l -> { r -> l + r.narrow } }

            fun <F, E> liftConcat(f: Applicative<F>): (_1<F, E>) -> _1<F, (_1<Companion, E>) -> _1<Companion, E>> =
                    f.map(concatList())

            override fun <T, R> foldr(ta: _1<Companion, T>, init: R, f: (T) -> (R) -> R): R =
                    ListFunctions.foldr(ta.narrow, f) { init }

            override fun <T, R> foldMap(m: Monoid<R>, ta: _1<Companion, T>, f: (T) -> R): R =
                    ListFunctions.foldr(ta.narrow, { f(it) `$` m.mappend }) { m.mempty }

            override fun <T> any(ta: _1<Companion, T>, pred: (T) -> Bool): Bool = ListFunctions.any(ta.narrow, pred)

            override fun <T> all(ta: _1<Companion, T>, pred: (T) -> Bool): Bool = ListFunctions.all(ta.narrow, pred)

            override fun <T> sum(n: Num<T>, xs: _1<Companion, T>): T = ListFunctions.sum(n, xs.narrow)

            override fun <T> product(n: Num<T>, xs: _1<Companion, T>): T = ListFunctions.product(n, xs.narrow)

            override fun <T> isNull(ta: _1<Companion, T>): Bool = ta.narrow `$` {
                when (it) {
                    is Nil -> Bool.True
                    is Cons -> Bool.False
                }
            }

            override fun <T> elem(e: Eq<T>, sbj: T, xs: _1<Companion, T>): Bool {
                return super.elem(e, sbj, xs)
            }
        }

        override val monadPlus: MonadPlus<Companion> get() = object : MonadPlus<Companion> {

            override fun <T> pure(value: T): _1<Companion, T> = Cons(value, Nil())

            override fun <T> empty(): _1<Companion, T> = Nil()

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    ListFunctions.map(obj.narrow, f)

            override fun <T> mzero(): _1<Companion, T> = Nil()

            override fun <T> mplus(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                    ListFunctions.plus(x.narrow, y.narrow)

            override fun <T> `_+_`(): (_1<Companion, T>) -> (_1<Companion, T>) -> _1<Companion, T> =
                    { x -> { y -> ListFunctions.plus(x.narrow, y.narrow) } }

            override fun <T, R, G : (T) -> R> _1<Companion, G>.`(_)`(obj: _1<Companion, T>): _1<Companion, R> = TODO("not implemented")

            override fun <T, R> bind(obj: _1<Companion, T>, f: (T) -> _1<Companion, R>): _1<Companion, R> =
                    ({ t: T -> f(t).narrow }) `$` { ListFunctions.bind(obj.narrow, it) }
        }
    }
}

infix operator fun <E> E.plus(list: List<E>): List<E> = List.Cons(this, list)

val <T> _1<List.Companion, T>.narrow: List<T> get() = this as List<T>

@Suppress("UNCHECKED_CAST")
val <M, T> _1<M, _1<List.Companion, T>>.dn: _1<M, List<T>> get() = this as _1<M, List<T>>

@Suppress("UNCHECKED_CAST")
val <M, T> _1<M, List<T>>.up: _1<M, _1<List.Companion, T>> get() = this as _1<M, _1<List.Companion, T>>
