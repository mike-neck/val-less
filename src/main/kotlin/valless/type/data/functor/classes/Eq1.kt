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
import valless.util.function.`$`
import valless.util.function.uncurry

interface Eq1<F> {

    fun <T> eq1(e: Eq<T>, l: (_1<F, T>), r: (_1<F, T>)): Bool = liftEq(l, r) { p, q -> e.eq(p, q) }

    fun <T> eq1(e: Eq<T>): (_1<F, T>) -> (_1<F, T>) -> Bool = { l -> { r -> liftEq(l, r) { p, q -> e.eq(p, q) } } }

    fun <L, R> liftEq(l: _1<F, L>, r: _1<F, R>, f: (L, R) -> Bool): Bool

    companion object {

        fun <F, T> fromEqInstance(e: Eq<_1<F, T>>): (_1<F, T>) -> (_1<F, T>) -> Bool = { l -> { r -> e.eq(l, r) } }

        object _Maybe : Eq1<Maybe.Companion> {
            override fun <L, R> liftEq(l: _1<Maybe.Companion, L>, r: _1<Maybe.Companion, R>, f: (L, R) -> Bool): Bool =
                    maybeCompare(l, r)
                            .nothing_nothing { Bool.True }
                            .nothing_just { Bool.False }
                            .just_nothing { Bool.False }
                            .just_just(f)
        }

        object _Identity : Eq1<Identity.Companion> {
            override fun <L, R> liftEq(l: _1<Identity.Companion, L>, r: _1<Identity.Companion, R>, f: (L, R) -> Bool): Bool =
                    (l.narrow to r.narrow) `$` { it.first.identity to it.second.identity } `$` f.uncurry
        }

        object _List : Eq1<List.Companion> {
            override fun <L, R> liftEq(l: _1<List.Companion, L>, r: _1<List.Companion, R>, f: (L, R) -> Bool): Bool =
                    tailLiftEq(l.narrow, r.narrow, f)

            tailrec fun <L, R> tailLiftEq(l: List<L>, r: List<R>, f: (L, R) -> Bool): Bool = when (l) {
                is List.Nil -> when (r) {
                    is List.Nil -> Bool.True
                    is List.Cons -> Bool.False
                }
                is List.Cons -> when (r) {
                    is List.Nil -> Bool.False
                    is List.Cons -> if (f(l.head, r.head) == Bool.False) Bool.False else tailLiftEq(l, r, f)
                }
            }
        }
    }
}

internal fun <P, Q> maybeCompare(l: _1<Maybe.Companion, P>, r: _1<Maybe.Companion, Q>): MaybeBothNothing<P, Q> =
        object : MaybeBothNothing<P, Q> {
            override fun <R> nothing_nothing(bn: () -> R): MaybeNothingJust<P, Q, R> = object : MaybeNothingJust<P, Q, R> {
                override fun nothing_just(nj: (Q) -> R): MaybeJustNothing<P, Q, R> = object : MaybeJustNothing<P, Q, R> {
                    override fun just_nothing(jn: (P) -> R): MaybeJustJust<P, Q, R> = object : MaybeJustJust<P, Q, R> {
                        override fun just_just(bj: (P, Q) -> R): R = l.narrow `$` { o ->
                            when (o) {
                                is Maybe.Nothing -> r.narrow `$` {
                                    when (it) {
                                        is Maybe.Nothing -> bn()
                                        is Maybe.Just -> nj(it.value)
                                    }
                                }
                                is Maybe.Just -> r.narrow `$` {
                                    when (it) {
                                        is Maybe.Nothing -> jn(o.value)
                                        is Maybe.Just -> bj(o.value, it.value)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun <R> nothing(nn: () -> R): MaybeJustNothing<P, Q, R> =
                    nothing_nothing(nn)
                            .nothing_just { throw IllegalStateException("There is no condition for this state.") }
        }

internal interface MaybeBothNothing<P, Q> {
    fun <R> nothing_nothing(bn: () -> R): MaybeNothingJust<P, Q, R>
    fun <R> nothing(nn: () -> R): MaybeJustNothing<P, Q, R>
}

internal interface MaybeNothingJust<P, Q, R> {
    fun nothing_just(nj: (Q) -> R): MaybeJustNothing<P, Q, R>
}

internal interface MaybeJustNothing<P, Q, R> {
    fun just_nothing(jn: (P) -> R): MaybeJustJust<P, Q, R>
}

internal interface MaybeJustJust<P, Q, R> {
    fun just_just(bj: (P, Q) -> R): R
}
