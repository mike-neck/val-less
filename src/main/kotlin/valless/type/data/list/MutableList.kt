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
package valless.type.data.list

import valless.type.data.List
import valless.util.function.`$`
import valless.util.initBy

internal sealed class MutableList<E> : Iterable<E> {

    abstract val size: Int
    abstract fun toList(): List<E>
    abstract fun reverse(): MutableList<E>
    abstract fun append(item: E): MutableList<E>
    abstract infix operator fun plus(other: MutableList<E>): MutableList<E>
    fun drop(count: Int): MutableList<E> = if (count <= 0) this else dropInternal(count)
    abstract fun dropInternal(count: Int): MutableList<E>

    class Empty<E> : MutableList<E>() {
        override val size: Int = 0

        override fun toList(): List<E> = List.empty()

        override fun reverse(): MutableList<E> = this

        override fun append(item: E): MutableList<E> = MutableList.singleton(item)

        override infix operator fun plus(other: MutableList<E>): MutableList<E> = other

        override fun dropInternal(count: Int): MutableList<E> = this

        override fun iterator(): Iterator<E> = object : Iterator<E> {
            override fun hasNext(): Boolean = false
            override fun next(): E = throw UnsupportedOperationException()
        }

        override fun toString(): String = joinToString(", ", "[", "]")
    }

    class Boxed<E>(var head: Link.Item<E>, var last: Link.Item<E>,
                   override var size: Int = 1, var direction: Direction = Direction.ASC) : MutableList<E>() {

        val firstItem: Link.Item<E> get() = if (direction == Direction.ASC) head else last

        val first: E get() = firstItem.item

        val secondItem: Link.Item<E> get() =
        if (size <= 1) throw IllegalStateException("The size of this list is $size.[$this]")
        else (if (direction == Direction.ASC) head.post else last.pre) as Link.Item<E>

        val second: E get() = secondItem.item

        val lastItem: Link.Item<E> get() = if (direction == Direction.ASC) last else head

        val end: E get() = lastItem.item

        override fun toList(): List<E> = constructList(List.empty(), lastItem, direction.reverse.next())

        private tailrec fun constructList(list: List<E>, current: Link<E>, next: (Link.Item<E>) -> Link<E>): List<E> = when (current) {
            is Link.Term -> list
            is Link.Item -> constructList(List.Cons(current.item, list), next(current), next)
        }

        override fun reverse(): MutableList<E> = this.initBy { direction = direction.reverse }

        override fun append(item: E): MutableList<E> = when (direction) {
            Direction.ASC -> createNewLink(item, last)
                    .initBy { this.last = it }
                    .initBy { size += 1 }
                    .let { this }
            Direction.DESC -> insertNewLink(item, head)
                    .initBy { this.head = it }
                    .initBy { size += 1 }
                    .let { this }
        }

        override infix operator fun plus(other: MutableList<E>): MutableList<E> = when (other) {
            is Empty -> this
            is Boxed -> direction.concatenate(this, other)
        }

        internal tailrec fun swap(link: Link<E> = head, currentHead: Link.Item<E> = head, currentLast: Link.Item<E> = last): Unit = when (link) {
            is Link.Term -> Unit.initBy { head = currentLast }.initBy { last = currentHead }
            is Link.Item -> swap(link.swap())
        }

        override fun dropInternal(count: Int): MutableList<E> = when (count <= size) {
            false -> throw IllegalArgumentException()
            true ->
                if (count == size) Empty()
                else when (direction) {
                    Direction.ASC -> dropping(count, head)
                            .initBy { this.head = it.first }
                            .initBy { this.size = it.second }
                            .initBy { it.first.pre = Link.term() }
                            .let { this }
                    Direction.DESC -> dropping(count, last)
                            .initBy { this.last = it.first }
                            .initBy { this.size = it.second }
                            .initBy { it.first.post = Link.term() }
                            .let { this }
                }
        }

        private tailrec fun dropping(count: Int, current: Link.Item<E>, size: Int = this.size, next: (Link.Item<E>) -> Link<E> = this.direction.next()): Pair<Link.Item<E>, Int> = when (count) {
            0 -> current to size
            else -> dropping(count - 1, getNext(current, next, count, size), size - 1, next)
        }

        private fun getNext(current: Link.Item<E>, next: (Link.Item<E>) -> Link<E>, count: Int, size: Int): Link.Item<E> =
                next(current) `$` {
                    when (it) {
                        is Link.Term -> throw IllegalArgumentException("count exceeds size.[count: $count, size: $size][$this]")
                        is Link.Item -> it
                    }
                }

        override fun iterator(): Iterator<E> = object : Iterator<E> {
            var current: Link<E> = if (direction == Direction.ASC) head else last
            val next: (Link.Item<E>) -> Link<E> = direction.next()
            override fun hasNext(): Boolean = current is Link.Term == false
            override fun next(): E = current `$` { c ->
                when (c) {
                    is Link.Term -> throw UnsupportedOperationException()
                    is Link.Item -> c.item.initBy { current = next(c) }
                }
            }
        }

        override fun toString(): String = joinToString(", ", "[", "]")
    }

    companion object {
        fun <E> empty(): MutableList<E> = Empty()
        fun <E> singleton(item: E): MutableList<E> = head(item) `$` { Boxed(it, it) }
        fun <E> fromList(list: List<E>): MutableList<E> = when (list) {
            is List.Nil -> empty()
            is List.Cons -> head(list.head) `$` { fromList(list.tail, it, it) }
        }

        tailrec fun <E> fromList(list: List<E>, pre: Link.Item<E>, head: Link.Item<E>, size: Int = 1): MutableList<E> = when (list) {
            is List.Nil -> Boxed(head, pre, size)
            is List.Cons -> fromList(list.tail, createNewLink(list.head, pre), head, size + 1)
        }

        fun <E> head(item: E): Link.Item<E> = Link.Item(item, Link.term(), Link.term())
        fun <E> createNewLink(next: E, pre: Link.Item<E>): Link.Item<E> = Link.Item(next, pre, Link.term()).initBy { pre.post = it }
        fun <E> insertNewLink(item: E, post: Link.Item<E>): Link.Item<E> = Link.Item(item, Link.term(), post).initBy { post.pre = it }
    }

    sealed class Link<E> {
        object Term : Link<Any>()
        class Item<E>(val item: E, var pre: Link<E>, var post: Link<E>) : Link<E>() {
            fun swap(): Link<E> = pre.initBy { pre = post }.initBy { post = it }
        }

        companion object {
            @Suppress("UNCHECKED_CAST") fun <E> term(): Link<E> = Term as Link<E>
        }
    }

    enum class Direction {
        ASC {
            override val reverse: Direction get() = DESC
            override fun <E> next(): (Link.Item<E>) -> Link<E> = { it.post }
            override fun <E> concatenate(self: Boxed<E>, other: Boxed<E>): Boxed<E> = when (other.direction) {
                ASC ->
                    if (self.size < other.size) other
                            .initBy { self.last.post = other.head }
                            .initBy { other.head.pre = self.last }
                            .initBy { other.size += self.size }
                            .initBy { other.head = self.head }
                    else self
                            .initBy { self.last.post = other.head }
                            .initBy { other.head.pre = self.last }
                            .initBy { self.size += other.size }
                            .initBy { self.last = other.last }
                DESC ->
                    if (self.size < other.size) other
                            .initBy { self.swap() }
                            .initBy { self.head.pre = other.last }
                            .initBy { other.last.post = self.head }
                            .initBy { other.size += self.size }
                            .initBy { other.last = self.last }
                    else self
                            .initBy { other.swap() }
                            .initBy { self.last.post = other.head }
                            .initBy { other.head.pre = self.last }
                            .initBy { self.size += other.size }
                            .initBy { self.last = other.last }
            }
        },
        DESC {
            override val reverse: Direction get() = ASC
            override fun <E> next(): (Link.Item<E>) -> Link<E> = { it.pre }
            override fun <E> concatenate(self: Boxed<E>, other: Boxed<E>): Boxed<E> = when (other.direction) {
                ASC ->
                    if (self.size < other.size) other
                            .initBy { self.swap() }
                            .initBy { self.last.post = other.head }
                            .initBy { other.head.pre = self.last }
                            .initBy { other.size += self.size }
                            .initBy { other.head = self.head }
                    else self
                            .initBy { other.swap() }
                            .initBy { self.head.pre = other.last }
                            .initBy { other.last.post = self.head }
                            .initBy { self.size += other.size }
                            .initBy { self.head = other.head }
                DESC ->
                    if (self.size < other.size) other
                            .initBy { self.head.pre = other.last }
                            .initBy { other.last.post = self.head }
                            .initBy { other.size += self.size }
                            .initBy { other.last = self.last }
                    else self
                            .initBy { self.head.pre = other.last }
                            .initBy { other.last.post = self.head }
                            .initBy { self.size += other.size }
                            .initBy { self.head = other.head }
            }
        };

        abstract val reverse: Direction
        abstract fun <E> next(): (Link.Item<E>) -> Link<E>
        abstract fun <E> concatenate(self: Boxed<E>, other: Boxed<E>): Boxed<E>
    }
}
