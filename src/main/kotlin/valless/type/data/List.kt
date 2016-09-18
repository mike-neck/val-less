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

        fun <E> filter(list: List<E>, pred: (E) -> Bool): List<E> = ListFunctions.filter(list, pred)

        fun <E> sort(ord: Ord<E>): (List<E>) -> List<E> = { sort(ord, it) }

        fun <E> sortBy(by: (E) -> (E) -> Ordering): (List<E>) -> List<E> = { sort(it, by) }

        fun <E> sort(ord: Ord<E>, list: List<E>): List<E> = sort(list, ord.compare)

        fun <E> sort(list: List<E>, by: (E) -> (E) -> Ordering): List<E> = when (list) {
            is Nil -> list
            is Cons -> ListFunctions.sort(list, by)
        }

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

object ListFunctions {

    fun <E> create(values: kotlin.collections.List<E>): List<E> = createRecursive(values)

    tailrec fun <E> createRecursive(values: kotlin.collections.List<E>, index: Int = 0, size: Int = values.size, gen: List<E> = List.Nil()): List<E> = when (index == size) {
        true -> reverse(gen)
        false -> createRecursive(values, index + 1, size, values[index] + gen)
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

    fun <E> plus(left: List<E>, right: List<E>): List<E> = when (left) {
        is List.Nil -> right
        is List.Cons -> when (right) {
            is List.Nil -> left
            is List.Cons -> plusInternal(reverse(left), right)
        }
    }

    tailrec fun <E> plusInternal(left: List<E>, right: List<E>): List<E> = when (left) {
        is List.Nil -> right
        is List.Cons -> plusInternal(left.tail, left.head + right)
    }

    tailrec fun <T, R> map(obj: List<T>, f: (T) -> R, building: () -> List<R> = { List.Nil() }): List<R> = when (obj) {
        is List.Nil -> building()
        is List.Cons -> map(obj.tail, f) { f(obj.head) + building() }
    }

    tailrec fun <T> reverse(list: List<T>, building: List<T> = List.Nil()): List<T> = when (list) {
        is List.Nil -> building
        is List.Cons -> reverse(list.tail, list.head + building)
    }

    fun <T, R> foldr(list: List<T>, f: (T) -> (R) -> R, gen: () -> R): R = when (list) {
        is List.Nil -> gen()
        is List.Cons -> foldrInternal(reverse(list), f, gen)
    }

    tailrec fun <T, R> foldrInternal(list: List<T>, f: (T) -> (R) -> R, gen: () -> R): R = when (list) {
        is List.Nil -> gen()
        is List.Cons -> foldrInternal(list.tail, f) { (list.head `$` f) * gen() }
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

    tailrec fun <E> filter(list: List<E>, pred: (E) -> Bool, filtered: List<E> = List.Nil()): List<E> = when (list) {
        is List.Nil -> reverse(filtered)
        is List.Cons -> filter(list.tail, pred, ListFunctions.filtered(list.head, pred, filtered))
    }

    internal fun <E> filtered(item: E, pred: (E) -> Bool, filtered: List<E>): List<E> = when (pred(item)) {
        Bool.True -> item + filtered
        Bool.False -> filtered
    }

    fun <E> sort(list: List<E>, by: (E) -> (E) -> Ordering): List<E> =
            recursivePartition(Partition.Builder(Part(list)).build(List.empty()), by) `$` merge(by)

    internal tailrec fun <E> recursivePartition(part: Partition<E, List<List<E>>>, by: (E) -> (E) -> Ordering): List<List<E>> =
            when (part) {
                is Partition.Finished -> reverse(part.result)
                is Partition.Soon -> reverse(part.list + part.result)
                is Partition.Building ->
                    if ((part.first `$` by) * part.second == Ordering.GT) recursivePartition(recursiveDesc(part.second, part.list, List.of(part.first), part.result, by), by)
                    else recursivePartition(recursiveAsc(part.second, part.list, List.of(part.first), part.result, by), by)
            }

    internal tailrec fun <E> recursiveDesc(first: E, list: List<E>, mid: List<E>, parted: List<List<E>>, by: (E) -> (E) -> Ordering): Partition<E, List<List<E>>> =
            when (list) {
                is List.Nil -> Partition.Finished(list, reverse(first + mid + parted))
                is List.Cons ->
                    if ((first `$` by) * list.head != Ordering.GT) Partition.Builder(Part(list)).build(first + mid + parted)
                    else recursiveDesc(list.head, list.tail, first + mid, parted, by)
            }

    internal tailrec fun <E> recursiveAsc(first: E, list: List<E>, mid: List<E>, parted: List<List<E>>, by: (E) -> (E) -> Ordering): Partition<E, List<List<E>>> =
            when (list) {
                is List.Nil -> Partition.Finished(list, reverse(reverse(first + mid) + parted))
                is List.Cons ->
                    if ((first `$` by) * list.head == Ordering.GT) Partition.Builder(Part(list)).build(reverse(first + mid) + parted)
                    else recursiveAsc(list.head, list.tail, first + mid, parted, by)
            }

    internal fun <E> partition(part: Part<E>, by: (E) -> (E) -> Ordering, parted: List<List<E>> = List.empty()): List<List<E>> =
            when (part) {
                is Part.Empty -> reverse(parted)
                is Part.Single -> reverse(part.list + parted)
                is Part.Multi ->
                    if ((part.first `$` by) * part.second == Ordering.GT) desc(part.second, part.list, List.of(part.first), parted, by)
                    else asc(part.second, part.list, List.of(part.first), parted, by)
            }

    internal tailrec fun <E> desc(first: E, list: List<E>, mid: List<E>, parted: List<List<E>>, by: (E) -> (E) -> Ordering): List<List<E>> = when (list) {
        is List.Nil -> reverse((first + mid) + parted)
        is List.Cons ->
            if ((first `$` by) * list.head != Ordering.GT) partition(Part(list), by, (first + mid) + parted)
            else desc(list.head, list.tail, first + mid, parted, by)
    }

    internal tailrec fun <E> asc(first: E, list: List<E>, mid: List<E>, parted: List<List<E>>, by: (E) -> (E) -> Ordering): List<List<E>> = when (list) {
        is List.Nil -> reverse(reverse(first + mid) + parted)
        is List.Cons ->
            if ((first `$` by) * list.head == Ordering.GT) partition(Part(list), by, reverse(first + mid) + parted)
            else asc(list.head, list.tail, first + mid, parted, by)
    }

    internal fun <E> merge(by: (E) -> (E) -> Ordering): (List<List<E>>) -> List<E> = { mergeAll(Part(it), by) }

    internal tailrec fun <E> mergeAll(part: Part<List<E>>, by: (E) -> (E) -> Ordering): List<E> = when (part) {
        is Part.Empty -> List.Nil()
        is Part.Single -> part.head
        is Part.Multi -> mergeAll(merging(part, by, List.Nil()) `$` Part.toPart(), by)
    }

    internal tailrec fun <E> merging(part: Part<List<E>>, by: (E) -> (E) -> Ordering, merged: List<List<E>>): List<List<E>> =
            when (part) {
                is Part.Empty -> merged
                is Part.Single -> plus(merged, part.list)
                is Part.Multi -> merging(Part(part.list), by,
                        merge(part.first, part.second, by, List.empty()) `$` { plus(merged, List.of(it)) })
            }

    internal tailrec fun <E> merge(left: List<E>, right: List<E>, by: (E) -> (E) -> Ordering, merged: List<E>): List<E> =
            when (left) {
                is List.Nil -> when (right) {
                    is List.Nil -> merged
                    is List.Cons -> plus(merged, right)
                }
                is List.Cons -> when (right) {
                    is List.Nil -> plus(merged, left)
                    is List.Cons -> merge(
                            if ((left.head `$` by) * right.head == Ordering.GT) left else left.tail,
                            if ((left.head `$` by) * right.head == Ordering.GT) right.tail else right,
                            by,
                            if ((left.head `$` by) * right.head == Ordering.GT) plus(merged, List.of(right.head))
                            else plus(merged, List.of(left.head))
                    )
                }
            }
}

internal sealed class Part<E> {

    abstract val list: List<E>

    class Empty<E>(override val list: List.Nil<E>) : Part<E>()
    class Single<E>(val head: E, val tail: List<E>, override val list: List<E>) : Part<E>()
    class Multi<E>(val first: E, val second: E, override val list: List<E>) : Part<E>()

    companion object {
        operator fun <E> invoke(list: List<E>): Part<E> = when (list) {
            is List.Nil -> Empty(list)
            is List.Cons -> list.tail `$` {
                when (it) {
                    is List.Nil -> Single(list.head, list.tail, list)
                    is List.Cons -> Multi(list.head, it.head, it.tail)
                }
            }
        }

        fun <E> toPart(): (List<E>) -> Part<E> = this::invoke
    }
}

internal sealed class Partition<E, out R> {

    abstract val list: List<E>

    abstract val result: R

    class Finished<E, out R>(override val list: List<E>, override val result: R) : Partition<E, R>()
    class Soon<E, out R>(val head: E, override val list: List<E>, override val result: R) : Partition<E, R>()
    class Building<E, out R>(val first: E, val second: E, override val list: List<E>, override val result: R) : Partition<E, R>()

    class Builder<E>(val part: Part<E>) {
        fun <R> build(result: R): Partition<E, R> = when (part) {
            is Part.Empty -> Finished(part.list, result)
            is Part.Single -> Soon(part.head, part.list, result)
            is Part.Multi -> Building(part.first, part.second, part.list, result)
        }
    }
}

infix operator fun <E> E.plus(list: List<E>): List<E> = List.Cons(this, list)

val <T> _1<List.Companion, T>.narrow: List<T> get() = this as List<T>

@Suppress("UNCHECKED_CAST")
val <M, T> _1<M, _1<List.Companion, T>>.dn: _1<M, List<T>> get() = this as _1<M, List<T>>

@Suppress("UNCHECKED_CAST")
val <M, T> _1<M, List<T>>.up: _1<M, _1<List.Companion, T>> get() = this as _1<M, _1<List.Companion, T>>
