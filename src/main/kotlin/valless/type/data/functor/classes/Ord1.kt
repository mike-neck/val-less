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
package valless.type.data.functor.classes

import valless.type._1
import valless.type.data.*
import valless.type.data.List
import valless.type.data.functor.Identity
import valless.type.data.functor.narrow
import valless.type.up
import valless.util.flow.ifItIs
import valless.util.function.`$`
import valless.util.function.uncurry

interface Ord1<F> {

    fun <T> compare(o: Ord<T>, l: _1<F, T>, r: _1<F, T>): Ordering = liftCompare(l, r) { x, y -> o.compare(x, y) }

    fun <T> compare(o: Ord<T>): (_1<F, T>) -> (_1<F, T>) -> Ordering = liftCompare { x, y -> o.compare(x, y) }

    fun <L, R> liftCompare(l: _1<F, L>, r: _1<F, R>, f: (L, R) -> Ordering): Ordering

    fun <L, R> liftCompare(f: (L, R) -> Ordering): (_1<F, L>) -> (_1<F, R>) -> Ordering = { x -> { y -> liftCompare(x, y, f) } }

    val asEq1: Eq1<F> get() = object : Eq1<F> {
        override fun <L, R> liftEq(l: _1<F, L>, r: _1<F, R>, f: (L, R) -> Bool): Bool =
                liftCompare(l, r) { x, y -> if (f(x, y).raw) Ordering.EQ else Ordering.LT } `$`
                        ifItIs<Ordering> { it == Ordering.EQ }.then { Bool.True }.els { Bool.False }
    }

    companion object {

        val identity: Ord1<Identity.Companion> = object : Ord1<Identity.Companion> {
            override fun <L, R> liftCompare(l: _1<Identity.Companion, L>, r: _1<Identity.Companion, R>, f: (L, R) -> Ordering): Ordering =
                    (l.narrow.identity to r.narrow.identity) `$` f.uncurry
        }

        val maybe: Ord1<Maybe.Companion> = object : Ord1<Maybe.Companion> {
            override fun <L, R> liftCompare(l: _1<Maybe.Companion, L>, r: _1<Maybe.Companion, R>, f: (L, R) -> Ordering): Ordering =
                    maybeCompare(l, r)
                            .nothing_nothing { Ordering.EQ }
                            .nothing_just { Ordering.LT }
                            .just_nothing { Ordering.GT }
                            .just_just(f)
        }

        val list: Ord1<List.Companion> = object : Ord1<List.Companion> {
            override fun <L, R> liftCompare(l: _1<List.Companion, L>, r: _1<List.Companion, R>, f: (L, R) -> Ordering): Ordering =
                    tailrecListComp(l.narrow, r.narrow, f)

            tailrec fun <L, R> tailrecListComp(l: List<L>, r: List<R>,
                                               f: (L, R) -> Ordering,
                                               prev: Ordering = Ordering.EQ): Ordering =
                    if (prev != Ordering.EQ) prev else when (l) {
                        is List.Nil -> when (r) {
                            is List.Nil -> Ordering.EQ
                            is List.Cons -> Ordering.LT
                        }
                        is List.Cons -> when (r) {
                            is List.Nil -> Ordering.GT
                            is List.Cons -> tailrecListComp(l.tail, r.tail, f, f(l.head, r.head))
                        }
                    }
        }

        fun <T> either(lo: Ord<T>): Ord1<_1<Either.Companion, T>> = object : Ord1<_1<Either.Companion, T>> {
            override fun <L, R> liftCompare(l: _1<_1<Either.Companion, T>, L>, r: _1<_1<Either.Companion, T>, R>, f: (L, R) -> Ordering): Ordering =
                    eitherCompare(l.up.narrow, r.up.narrow)
                            .left_left { x, y -> lo.compare(x, y) }
                            .left_right { x, y -> Ordering.LT }
                            .right_left { x, y -> Ordering.GT }
                            .right_right(f)
        }
    }
}
