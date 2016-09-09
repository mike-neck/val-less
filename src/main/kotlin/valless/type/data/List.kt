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
import valless.type.data.monoid.Monoid
import valless.util.function.`$`
import valless.util.function.times

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

            override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<Companion, P>, f: (P) -> _1<F, R>): _1<F, _1<Companion, R>> {
                return super.traverse(m, ta, f)
            }

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

object ListFunctions {

    tailrec fun <E> create(value: kotlin.collections.List<E>, gen: (List<E>) -> List<E> = { it }): List<E> = when (value.size) {
        0 -> gen(List.Nil())
        else -> create(value.drop(1)) { l -> List.Cons(value[0], l) `$` gen }
    }

    tailrec fun <E> eq(e: Eq<E>, xs: List<E>, ys: List<E>): Bool = when (xs) {
        is List.Nil -> when (ys) {
            is List.Nil -> Bool.True
            is List.Cons -> Bool.False
        }
        is List.Cons -> when (ys) {
            is List.Nil -> Bool.False
            is List.Cons -> if (e.eq(xs.head, ys.head).raw == false) Bool.False else eq(e, xs.tail, ys.tail)
        }
    }

    tailrec fun <E> compare(o: Ord<E>, xs: List<E>, ys: List<E>, prev: Ordering = Ordering.EQ): Ordering =
            if (prev != Ordering.EQ) prev else when (xs) {
                is List.Nil -> when (ys) {
                    is List.Nil -> Ordering.EQ
                    is List.Cons -> Ordering.LT
                }
                is List.Cons -> when (ys) {
                    is List.Nil -> Ordering.GT
                    is List.Cons -> compare(o, xs.tail, ys.tail, o.compare(xs.head, ys.head))
                }
            }

    tailrec fun <E> plus(left: List<E>, right: List<E>): List<E> = when (left) {
        is List.Nil -> right
        is List.Cons -> plus(left.tail, left.head + right)
    }

    tailrec fun <T, R> map(obj: List<T>, f: (T) -> R, building: () -> List<R> = { List.Nil() }): List<R> = when (obj) {
        is List.Nil -> building()
        is List.Cons -> map(obj.tail, f) { f(obj.head) + building() }
    }

    tailrec fun <T> reverse(list: List<T>, building: List<T> = List.Nil()): List<T> = when (list) {
        is List.Nil -> building
        is List.Cons -> reverse(list.tail, list.head + building)
    }

    tailrec fun <T, R> foldr(list: List<T>, f: (T) -> (R) -> R, gen: () -> R): R = when (list) {
        is List.Nil -> gen()
        is List.Cons -> foldr(list.tail, f) { (list.head `$` f) * gen() }
    }

    tailrec fun <T> any(list: List<T>, pred: (T) -> Bool): Bool = when (list) {
        is List.Nil -> Bool.False
        is List.Cons -> if (pred(list.head).raw) Bool.True else any(list.tail, pred)
    }

    tailrec fun <T> all(list: List<T>, pred: (T) -> Bool): Bool = when (list) {
        is List.Nil -> Bool.True
        is List.Cons -> if (pred(list.head).raw == false) Bool.False else all(list.tail, pred)
    }

    tailrec fun <T> sum(n: Num<T>, list: List<T>, sum: () -> T = { n.zero }): T = when (list) {
        is List.Nil -> sum()
        is List.Cons -> sum(n, list.tail) { n.calc { sum() + list.head } }
    }

    tailrec fun <T> product(n: Num<T>, list: List<T>, prd: () -> T = { n.plusOne }): T = when (list) {
        is List.Nil -> prd()
        is List.Cons -> product(n, list.tail) { n.calc { prd() * list.head } }
    }

    tailrec fun <T, R> bind(xs: List<T>, f: (T) -> List<R>, rs: List<R> = List.Nil()): List<R> = when (xs) {
        is List.Nil -> rs
        is List.Cons -> bind(xs.tail, f, plus(rs, f(xs.head)))
    }
}

infix operator fun <E> E.plus(list: List<E>): List<E> = List.Cons(this, list)

val <T> _1<List.Companion, T>.narrow: List<T> get() = this as List<T>
