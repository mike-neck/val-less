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
package valless.util.flow

import valless.util.function.`$`
import java.util.*

/**
 * Represents <code>if</code> statement.
 *
 * @param R - a type of this statement's result.
 * @param condition - a condition for the statement.
 * @param onTrue - a supplier for result
 */
class If<R>(private val condition: Boolean, private val onTrue: () -> R) {

    fun els(onFalse: () -> R): R = if (condition) onTrue() else onFalse()

    fun elIf(newCondition: Boolean, another: () -> R): If<R> =
            if (condition) this else If(newCondition, another)
}

/**
 * Making it simple to start if statement.
 */
infix fun <R> Boolean.ifSo(onTrue: () -> R): If<R> = If(this, onTrue)

fun <C> ifItIs(condition: (C) -> Boolean): Then<C> = object : Then<C> {
    override fun <R> then(onTrue: (C) -> R): Else<C, R> = object : Else<C, R> {
        override fun els(onFalse: (C) -> R): (C) -> R = { c -> if (condition(c)) onTrue(c) else onFalse(c) }
    }
}

interface Then<C> {
    fun <R> then(onTrue: (C) -> R): Else<C, R>
}

interface Else<C, R> {
    fun els(onFalse: (C) -> R): (C) -> R
}

/**
 * Represents <code>when</code> statement.
 *
 * @param C - an object to be source of condition.
 * @param R - a type of this statement's result.
 * @property value - an object to be source of condition.
 * @property cases - list of conditions and results.
 */
interface When<C, R> {
    val value: C
    val cases: List<Case<C, R>>

    fun case(condition: (C) -> Boolean): Matched<C, R>

    class Case<in C, out R>(val condition: (C) -> Boolean, val onTrue: (C) -> R) : (C) -> Boolean {
        override operator fun invoke(v: C): Boolean = condition(v)
        operator fun get(v: C): R = onTrue(v)
    }

    interface Matched<C, R> {
        fun then(onTrue: (C) -> R): When<C, R>
    }

    fun els(def: (C) -> R): R = match(value, def, Elements(cases))

    private tailrec fun match(v: C, def: (C) -> R, xs: Elements<When.Case<C, R>>): R =
            when (xs) {
                is Elements.Empty -> def(v)
                is Elements.HasElements -> if (xs.head(v)) xs.head[v] else match(v, def, xs.tailElements())
            }

    companion object {
        operator fun <C> invoke(value: C) = WhenCondition(value)
    }
}

class WhenCondition<C>(val value: C) {
    fun case(condition: (C) -> Boolean): Matched<C> = object : Matched<C> {
        override fun <R> then(onTrue: (C) -> R): When<C, R> = WhenBody(value, listOf(When.Case(condition, onTrue)))
    }

    interface Matched<C> {
        fun <R> then(onTrue: (C) -> R): When<C, R>
    }
}

private class WhenBody<C, R>(override val value: C, override val cases: List<When.Case<C, R>> = emptyList()) : When<C, R> {

    override fun case(condition: (C) -> Boolean): When.Matched<C, R> = object : When.Matched<C, R> {
        override fun then(onTrue: (C) -> R): When<C, R> =
                When.Case(condition, onTrue) `$` { cases + it } `$` { WhenBody(value, it) }
    }
}

private sealed class Elements<out T> {
    abstract val head: T
    abstract val tail: List<T>
    abstract val empty: Boolean
    abstract fun tailElements(): Elements<T>

    class Empty<out T> : Elements<T>() {
        override val empty: Boolean get() = true
        override val head: T get() = throw NoSuchElementException()
        override val tail: List<T> get() = throw NoSuchElementException()
        override fun tailElements(): Elements<T> = throw NoSuchElementException()
    }

    class HasElements<out T>(override val head: T, override val tail: List<T>) : Elements<T>() {
        override val empty: Boolean get() = false
        override fun tailElements(): Elements<T> = Elements(tail)
    }

    companion object {
        operator fun <T> invoke(list: List<T>): Elements<T> =
                list.isEmpty().ifSo<Elements<T>> { Empty() }.els { HasElements(list.first(), list.drop(1)) }
    }
}
