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
package util.function

/* composition on supplier */
infix operator fun <P, R, F : () -> P> F.plus(f: (P) -> R): () -> R = { f(this()) }

/* composition on function */
infix operator fun <P, Q, R, F : (P) -> Q> F.plus(f: (Q) -> R): (P) -> R = { f(this(it)) }

/* flip arguments */
fun <P, Q, R, F : (P) -> ((Q) -> R)> F.flip(): (Q) -> ((P) -> R) = { q -> { p -> this(p)(q) } }

/** Applying function. this is equivalent to the function [let] */
infix fun <P, R> P.`$`(f: (P) -> R): R = f(this)

infix operator fun <P, Q, F : (P) -> Q> F.times(p: P): Q = this(p)

/**
 * Identity function
 */
fun <T> id(): (T) -> T = { it }
