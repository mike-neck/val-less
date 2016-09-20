package valless.type.data.list

import valless.type.data.*
import valless.type.data.List
import valless.util.function.`$`
import valless.util.function.times
import valless.util.initBy
import valless.util.times

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

    fun <E> sort(list: List<E>, by: (E) -> (E) -> Ordering): List<E> =
            partition(PartD(Dual.fromList(list)).toPartition(Dual.empty()), by) `$` merge(by)

    internal tailrec fun <E> partition(part: PartitionD<E, Dual<Dual<E>>>, by: (E) -> (E) -> Ordering): Dual<Dual<E>> =
            when (part) {
                is PartitionD.Finished -> part.result.reverse()
                is PartitionD.Soon -> (part.result.append(Dual.of(part.head))).reverse()
                is PartitionD.Building ->
                    if ((part.first `$` by) * part.second == Ordering.GT) partition(desc(part.second, part.list, Dual.of(part.first), part.result, by), by)
                    else partition(asc(part.second, part.list, Dual.of(part.first), part.result, by), by)
            }

    internal tailrec fun <E> desc(first: E, list: Dual<E>, mid: Dual<E>, parted: Dual<Dual<E>>, by: (E) -> (E) -> Ordering): PartitionD<E, Dual<Dual<E>>> =
            when (list) {
                is EmptyDual -> PartitionD.Finished(list, parted.append(mid.append(first)))
                is DualImpl ->
                    if ((first `$` by) * list.first.value != Ordering.GT) PartD(list).toPartition(parted.append(mid.append(first)))
                    else desc(list.first.value, list.drop(1), mid.append(first), parted, by)
                else -> throw IllegalStateException()
            }

    internal tailrec fun <E> asc(first: E, list: Dual<E>, mid: Dual<E>, parted: Dual<Dual<E>>, by: (E) -> (E) -> Ordering): PartitionD<E, Dual<Dual<E>>> =
            when (list) {
                is EmptyDual -> PartitionD.Finished(list, parted.append(mid.append(first).reverse()))
                is DualImpl ->
                    if ((first `$` by) * list.first.value == Ordering.GT) PartD(list).toPartition(parted.append(mid.append(first).reverse()))
                    else asc(list.first.value, list.drop(1), mid.append(first), parted, by)
                else -> throw IllegalStateException()
            }

    internal fun <E> merge(by: (E) -> (E) -> Ordering): (Dual<Dual<E>>) -> List<E> = { mergeAll(PartD(it), by) }

    internal tailrec fun <E> mergeAll(part: PartD<Dual<E>>, by: (E) -> (E) -> Ordering): List<E> = when (part) {
        is PartD.Empty -> List.Nil()
        is PartD.Single -> part.head.toList()
        is PartD.Multi -> mergeAll(PartD(merging(part, by, Dual.empty())), by)
    }

    internal tailrec fun <E> merging(part: PartD<Dual<E>>, by: (E) -> (E) -> Ordering, merged: Dual<Dual<E>>): Dual<Dual<E>> =
            when (part) {
                is PartD.Empty -> merged
                is PartD.Single -> merged + part.list
                is PartD.Multi -> merging(PartD(part.list), by, merged.append(merge(MergeArg(part.first, part.second, Dual.empty(), by))))
            }

    internal tailrec fun <E> merge(arg: MergeArg<E>): Dual<E> =
            when (arg.left) {
                is EmptyDual -> when (arg.right) {
                    is EmptyDual -> arg.merged
                    is DualImpl -> arg.merged + arg.right
                    else -> throw IllegalStateException()
                }
                is DualImpl -> when (arg.right) {
                    is EmptyDual -> arg.merged + arg.left
                    is DualImpl -> merge(arg.next())
                    else -> throw IllegalStateException()
                }
                else -> throw IllegalStateException()
            }

    internal class MergeArg<E>(val left: Dual<E>, val right: Dual<E>, val merged: Dual<E>, val by: (E) -> (E) -> Ordering) {
        val leftValue: E get() = (left as DualImpl).first.value
        val rightValue: E get() = (right as DualImpl).first.value

        fun mergeRightNext(): MergeArg<E> = ((right as DualImpl<E>).first to right.drop(1)) `$` { MergeArg(left, it.second, merged.append(it.first.value), by) }
        fun mergeLeftNext(): MergeArg<E> = ((left as DualImpl<E>).first to left.drop(1)) `$` { MergeArg(it.second, right, merged.append(it.first.value), by) }

        fun next(): MergeArg<E> = if ((leftValue `$` by) * rightValue == Ordering.GT) mergeRightNext() else mergeLeftNext()

        override fun toString(): String =
                "Arg left: $left right: $right merged: $merged"
    }
}

internal sealed class PartD<E> {
    abstract val list: Dual<E>
    fun <R> toPartition(result: R): PartitionD<E, R> = when (this) {
        is Empty -> PartitionD.Finished(this.list, result)
        is Single -> PartitionD.Soon(this.head, this.list, result)
        is Multi -> PartitionD.Building(this.first, this.second, this.list, result)
    }

    class Empty<E>(override val list: EmptyDual<E> = EmptyDual()) : PartD<E>()
    class Single<E>(val head: E, override val list: Dual<E>) : PartD<E>()
    class Multi<E>(val first: E, val second: E, override val list: Dual<E>) : PartD<E>()

    companion object {
        operator fun <E> invoke(dual: Dual<E>): PartD<E> = when (dual) {
            is EmptyDual -> Empty(dual)
            is DualImpl ->
                if (dual.size == 1) Single(dual.first.value, dual.drop(1))
                else dual.first.value.let { it to (dual.drop(1) as DualImpl<E>) }
                        .let { Triple(it.first, it.second.first.value, it.second.drop(1)) }
                        .let { Multi(it.first, it.second, it.third) }
            else -> throw UnsupportedOperationException()
        }
    }
}

internal sealed class PartitionD<E, out R> {
    abstract val list: Dual<E>
    abstract val result: R

    class Finished<E, out R>(override val list: Dual<E>, override val result: R) : PartitionD<E, R>()
    class Soon<E, out R>(val head: E, override val list: Dual<E>, override val result: R) : PartitionD<E, R>()
    class Building<E, out R>(val first: E, val second: E, override val list: Dual<E>, override val result: R) : PartitionD<E, R>()
}

internal interface Dual<E> : Iterable<E> {
    var size: Int
    fun toList(): List<E>
    fun append(value: E): Dual<E>
    fun reverse(): Dual<E>
    infix operator fun plus(other: Dual<E>): Dual<E>
    fun drop(count: Int): Dual<E>

    companion object {

        fun <E> empty(): Dual<E> = EmptyDual()

        fun <E> of(value: E): Dual<E> = DualImpl.Link.Item(value, DualImpl.Link.term(), DualImpl.Link.term())
                .let { DualImpl(it, it, DualImpl.Direction.ASC) }

        fun <E> fromList(list: List<E>): Dual<E> = when (list) {
            is List.Nil -> EmptyDual()
            is List.Cons -> fromList(list.tail, DualImpl.Link.Item(list.head, DualImpl.Link.term(), DualImpl.Link.term()))
        }

        fun <E> fromList(list: List<E>, item: DualImpl.Link.Item<E>): DualImpl<E> = fromList(list, item, item, 1)

        tailrec fun <E> fromList(list: List<E>, pre: DualImpl.Link.Item<E>, head: DualImpl.Link.Item<E>, size: Int): DualImpl<E> = when (list) {
            is List.Nil -> DualImpl(head, pre, DualImpl.Direction.ASC, size)
            is List.Cons -> fromList(list.tail, DualImpl.Link.Item(list.head, pre, DualImpl.Link.term()).initBy { pre.post = it }, head, size + 1)
        }
    }
}

internal class EmptyDual<E> : Dual<E> {
    override fun drop(count: Int): Dual<E> = this
    override fun plus(other: Dual<E>): Dual<E> = other
    override fun reverse(): Dual<E> = this
    override fun toList(): List<E> = List.Nil()
    override fun append(value: E): Dual<E> =
            DualImpl.Link.Item(value, DualImpl.Link.term(), DualImpl.Link.term()) `$`
                    { DualImpl(it, it, DualImpl.Direction.ASC) }

    override var size: Int
        get() = 0
        set(value) {
        }

    override fun iterator(): Iterator<E> = object : Iterator<E> {
        override fun hasNext(): Boolean = false
        override fun next(): E = throw UnsupportedOperationException("no element")
    }

    override fun toString(): String = this.joinToString(", ", "[", "]")
}

internal class DualImpl<E>(var head: Link.Item<E>, var tail: Link.Item<E>, var direction: Direction, override var size: Int = 1) : Dual<E> {

    val first: Link.Item<E> get() = if (direction == Direction.ASC) head else tail

    override fun reverse(): DualImpl<E> = this.initBy { it.direction = direction.reverse }

    override fun append(value: E): DualImpl<E> = when (direction) {
        Direction.ASC -> toHead(value)
        Direction.DESC -> toTail(value)
    }

    fun toHead(value: E): DualImpl<E> = Link.Item(value, Link.term(), head)
            .initBy { head.pre = it }
            .initBy { head = it }
            .initBy { size += 1 }
            .let { this }

    fun toTail(value: E): DualImpl<E> = Link.Item(value, tail, Link.term())
            .initBy { tail.post = it }
            .initBy { tail = it }
            .initBy { size += 1 }
            .let { this }

    override fun plus(other: Dual<E>): Dual<E> = when (other) {
        is EmptyDual -> this
        is DualImpl -> plusInternal(other)
        else -> throw UnsupportedOperationException()
    }

    infix fun plusInternal(other: DualImpl<E>): DualImpl<E> = when (this.direction == other.direction) {
        true -> add(other)
        false -> smaller(this, other) * { it.swap() } `$` { it.first.add(it.second) }
    }

    fun add(other: DualImpl<E>): DualImpl<E> = when (this.direction) {
        Direction.ASC -> this.initBy { it.tail.post = other.head }
                .initBy { other.head.pre = it.tail }
                .initBy { it.tail = other.tail }
                .initBy { it.size += other.size }
        Direction.DESC -> this.initBy { it.head.pre = other.tail }
                .initBy { other.tail.post = it.head }
                .initBy { it.head = other.head }
                .initBy { it.size += other.size }
    }

    fun swap(): DualImpl<E> = swap(if (direction == Direction.ASC) head else tail, this)

    tailrec fun swap(current: Link<E>, dual: DualImpl<E>): DualImpl<E> = when (current) {
        is Link.Term -> dual
        is Link.Item -> swap(current.swap(direction), dual)
    }

    override fun toList(): List<E> =
            (if (direction == Direction.ASC) tail else head) `$`
                    { Companion.toList(it, List.empty(), direction.reverse()) }

    override fun drop(count: Int): Dual<E> = Companion.drop(this, count, direction)

    override fun iterator(): Iterator<E> = object : Iterator<E> {
        val d: (Link.Item<E>) -> Link<E> = direction.asFun()
        var current: Link<E> = if (direction == Direction.ASC) head else tail
        override fun hasNext(): Boolean = current is Link.Item
        override fun next(): E =
                if (current is Link.Term) throw UnsupportedOperationException("finished")
                else current
                        .initBy { current = d(it as Link.Item<E>) } `$`
                        { (it as Link.Item).value }
    }

    override fun toString(): String = this.joinToString(", ", "[", "]")

    companion object {
        fun <E> smaller(left: DualImpl<E>, right: DualImpl<E>): Pair<DualImpl<E>, DualImpl<E>> =
                if (left.size < right.size) right to left else left to right

        tailrec fun <E> toList(current: Link<E>, built: List<E>, next: (Link.Item<E>) -> Link<E>): List<E> =
                when (current) {
                    is Link.Term -> built
                    is Link.Item -> toList(next(current), current.value + built, next)
                }

        tailrec fun <E> drop(dual: Dual<E>, count: Int, direction: Direction): Dual<E> =
                if (count == 0) dual
                else if (dual is EmptyDual) dual
                else if (dual is DualImpl) drop(drop(dual, direction), count - 1, direction)
                else throw IllegalStateException()

        fun <E> drop(dual: DualImpl<E>, direction: Direction): Dual<E> =
                if (direction == Direction.ASC) dual.head.post
                        .initBy { if (it is Link.Item) it.pre = Link.term() }
                        .initBy { if (it is Link.Item) dual.head = it }
                        .initBy { dual.size -= 1 }
                        .let { if (it is Link.Item) dual else EmptyDual<E>() }
                else dual.tail.pre.initBy { if (it is Link.Item) it.post = Link.term() }
                        .initBy { if (it is Link.Item) dual.tail = it }
                        .initBy { dual.size -= 1 }
                        .let { if (it is Link.Item) dual else EmptyDual<E>() }
    }

    sealed class Link<E> {
        abstract val term: Bool

        object Term : Link<Any>() {
            override val term: Bool = Bool.True
            override fun toString(): String = ""
        }

        class Item<E>(val value: E, var pre: Link<E>, var post: Link<E>) : Link<E>() {
            override val term: Bool = Bool.False
            fun swap(direction: Direction): Link<E> =
                    ((if (direction == Direction.ASC) post else pre) to pre)
                            .initBy { pre = post }
                            .initBy { post = it.second }
                            .let { it.first }

            override fun toString(): String = value.toString()
        }

        companion object {
            @Suppress("UNCHECKED_CAST") fun <E> term(): Link<E> = Term as Link<E>
        }
    }

    enum class Direction {
        ASC {
            override fun <E> asFun(): (Link.Item<E>) -> Link<E> = { it.post }
            override fun <E> reverse(): (Link.Item<E>) -> Link<E> = { it.pre }
            override val reverse: Direction get() = DESC
        },
        DESC {
            override fun <E> asFun(): (Link.Item<E>) -> Link<E> = { it.pre }
            override fun <E> reverse(): (Link.Item<E>) -> Link<E> = { it.post }
            override val reverse: Direction get() = ASC
        };

        abstract fun <E> asFun(): (Link.Item<E>) -> Link<E>
        abstract fun <E> reverse(): (Link.Item<E>) -> Link<E>
        abstract val reverse: Direction
    }
}
