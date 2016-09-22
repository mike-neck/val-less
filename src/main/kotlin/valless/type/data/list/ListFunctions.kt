package valless.type.data.list

import valless.type.data.*
import valless.type.data.List
import valless.util.function.`$`
import valless.util.function.times

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
        is List.Cons -> filter(list.tail, pred, filtered(list.head, pred, filtered))
    }

    internal fun <E> filtered(item: E, pred: (E) -> Bool, filtered: List<E>): List<E> = when (pred(item)) {
        Bool.True -> item + filtered
        Bool.False -> filtered
    }

    fun <E> sort(list: List<E>, by: (E) -> (E) -> Ordering): List<E> = partition(Partition(list), by) `$` merge(by)

    internal fun <E> merge(by: (E) -> (E) -> Ordering): (MutableList<MutableList<E>>) -> List<E> = { mergeAll(it, by) }

    internal tailrec fun <E> partition(partition: Partition<E>, by: (E) -> (E) -> Ordering): MutableList<MutableList<E>> =
            when (partition) {
                is Partition.Finished -> partition.result
                is Partition.Almost -> partition.result.append(MutableList.singleton(partition.head))
                is Partition.Building ->
                    if ((partition.first `$` by) * partition.second == Ordering.GT) partition(desc(partition.second, partition.list, MutableList.singleton(partition.first), partition.result, by), by)
                    else partition(asc(partition.second, partition.list, MutableList.singleton(partition.first), partition.result, by), by)
            }


    internal tailrec fun <E> desc(first: E, list: List<E>, mid: MutableList<E>, result: MutableList<MutableList<E>>, by: (E) -> (E) -> Ordering): Partition<E> =
            when (list) {
                is List.Nil -> Partition(list, result.append(mid.append(first).reverse()))
                is List.Cons ->
                    if ((first `$` by) * list.head != Ordering.GT) Partition(list, result.append(mid.append(first).reverse()))
                    else desc(list.head, list.tail, mid.append(first), result, by)
            }

    internal tailrec fun <E> asc(first: E, list: List<E>, mid: MutableList<E>, result: MutableList<MutableList<E>>, by: (E) -> (E) -> Ordering): Partition<E> =
            when (list) {
                is List.Nil -> Partition(list, result.append(mid.append(first)))
                is List.Cons ->
                    if ((first `$` by) * list.head == Ordering.GT) Partition(list, result.append(mid.append(first)))
                    else asc(list.head, list.tail, mid.append(first), result, by)
            }

    internal tailrec fun <E> mergeAll(list: MutableList<MutableList<E>>, by: (E) -> (E) -> Ordering): List<E> =
            when (list) {
                is MutableList.Empty -> List.Nil()
                is MutableList.Boxed ->
                    if (list.size == 1) list.first.toList()
                    else mergeAll(merging(list, by), by)
            }

    internal fun <E> merging(list: MutableList<MutableList<E>>, by: (E) -> (E) -> Ordering, merged: MutableList<MutableList<E>> = MutableList.empty()): MutableList<MutableList<E>> =
            merging(merged, list, by)

    internal tailrec fun <E> merging(merged: MutableList<MutableList<E>>, list: MutableList<MutableList<E>>, by: (E) -> (E) -> Ordering): MutableList<MutableList<E>> =
            when (list) {
                is MutableList.Empty -> merged
                is MutableList.Boxed ->
                    if (list.size == 1) merged.append(list.first)
                    else merging(merged.append(merge(list.first, list.second, by)), list.drop(2), by)
            }

    internal fun <E> merge(left: MutableList<E>, right: MutableList<E>, by: (E) -> (E) -> Ordering, merged: MutableList<E> = MutableList.empty()): MutableList<E> =
            merge(MergeList(left, right, merged, by))

    internal tailrec fun <E> merge(arg: MergeList<E>): MutableList<E> =
            when (arg.left) {
                is MutableList.Empty -> when (arg.right) {
                    is MutableList.Empty -> arg.merged
                    is MutableList.Boxed -> arg.merged + arg.right
                }
                is MutableList.Boxed -> when (arg.right) {
                    is MutableList.Empty -> arg.merged + arg.left
                    is MutableList.Boxed ->
                        if ((arg.left.first `$` arg.by) * arg.right.first == Ordering.GT) merge(arg.rightMerge())
                        else merge(arg.leftMerge())
                }
            }

    internal class MergeList<E>(val left: MutableList<E>, val right: MutableList<E>, val merged: MutableList<E>, val by: (E) -> (E) -> Ordering) {
        fun rightMerge(): MergeList<E> = (right as MutableList.Boxed).first `$` { MergeList(left, right.drop(1), merged.append(it), by) }
        fun leftMerge(): MergeList<E> = (left as MutableList.Boxed).first `$` { MergeList(left.drop(1), right, merged.append(it), by) }
    }

    internal tailrec fun <E> nub(list: List<E>, result: List<E> = List.empty(), cond: (E) -> (E) -> Bool): List<E> = when (list) {
        is List.Nil -> reverse(result)
        is List.Cons ->
            if (elemBy(list.head, result, cond).raw) nub(list.tail, result, cond)
            else nub(list.tail, list.head + result, cond)
    }

    internal tailrec fun <E> elemBy(elem: E, result: List<E>, cond: (E) -> (E) -> Bool): Bool = when (result) {
        is List.Nil -> Bool.False
        is List.Cons ->
            if (cond(elem)(result.head).raw) Bool.True
            else elemBy(elem, result.tail, cond)
    }

    internal tailrec fun <E> drop(num: Int, list: List<E>): List<E> = when (list) {
        is List.Nil -> list
        is List.Cons ->
            if (num == 0) list
            else drop(num - 1, list.tail)
    }
}
