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
package valless.util

/**
 * Mapping function for nullable object of [T].
 * 
 * This function maps receiver [T] to another object [R] if it is not null.
 * If the receiver is null, `null` will be returned.
 * 
 * @receiver a nullable object [T].
 * @param [f] mapping function.
 * @return [R] if the receiver is not null. `null` if the receiver is null.
 */
infix fun <T, R> T?.make(f: (T) -> R?): R? = if (this == null) null else f(this)

/** shortcut of [make] */
infix fun <T, R> T?.`^`(f: (T) -> R?): R? = this.make(f)

/**
 * Avoiding compiler warning.
 * 
 * This function returns [Unit] after evaluating a given function.
 * 
 * @receiver a nullable object [T].
 * @param f a function to be evaluated.
 * @return [Unit]
 */
infix fun <T> T?.unit(f: (T) -> Unit): Unit { if (this != null) f(this) }

/**
 * Avoiding compiler warning.
 * 
 * This property returns [Unit] after evaluating the receiver.
 */
val Any?.unit: Unit get() { @Suppress("UNUSED_EXPRESSION")this; Unit }

/**
 * Initializer.
 * 
 * This function initializes the receiver by a given function, then returns the receiver.
 * 
 * @receiver [T] - not null object to be initialized.
 * @param c - a function which initializes the receiver.
 * @return [T] - The receiver itself initialized by the given function [c].
 */
infix fun <T> T.initBy(c: (T) -> Unit): T {
    c(this)
    return this
}

/**
 * Initializer.
 * 
 * This function creates instance of [R] by the first parameter function,
 * then initializes it with the receiver and the second parameter function.
 * 
 * @receiver [T] - not null object.
 * @param g - a function creates the instance of [R].
 * @param f - a function initializes the instance of [R] with the receiver [T].
 * @return [R]
 */
fun <T,R> T.initialize(g: () -> R, f: (T,R) -> Unit): R = g() initBy { f(this, it) }

/**
 * Avoiding compiler warning.
 * 
 * This function enables two function execution which doesn't return [Unit] in [Unit] function.
 * 
 * @receiver [T] - a execution result of the first function.
 * @param [R] - a execution result of the second function.
 * @return [Unit]
 */
infix fun <T, R> T.and(r: R): Unit = this.let { r }.unit

fun <T, R> Pair<T, T>.both(f: (T) -> R): Pair<R, R> = f(this.first) to f(this.second)

fun <T> T.toPair(): Pair<T, T> = this to this

val <L, R> Pair<L, R>.swap: Pair<R, L> get() = Pair(this.second, this.first)

fun <L, R> swap(): (Pair<L, R>) -> Pair<R, L> = { it.swap }

infix operator fun <P, Q, R> Pair<P, Q>.times(f: (Q) -> R): Pair<P, R> = this.first to f(this.second)

infix operator fun <P, Q, R, S> Pair<P, Q>.times(p: Pair<(P) -> R, (Q) -> S>): Pair<R, S> =
        p.first(this.first) to p.second(this.second)

infix operator fun <P, Q, R> Pair<P, Q>.div(f: (P) -> R): Pair<R, Q> = f(this.first) to this.second
