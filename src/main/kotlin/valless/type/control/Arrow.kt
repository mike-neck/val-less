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
package valless.type.control

import valless.type._1
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass
import valless.type.data.function.Fun1
import valless.type.data.function.fun1
import valless.util.function.`$`

@TypeClass
interface Arrow<A> {

    val cat: Category<A>

    fun <T> id(): _1<_1<A, T>, T> = cat.id<T>()

    fun <P, Q, R> plus(pq: _1<_1<A, P>, Q>, qr: _1<_1<A, Q>, R>): _1<_1<A, P>, R> = cat.plus(pq, qr)

    infix fun <P, Q, R> _1<_1<A, P>, Q>.then(next: _1<_1<A, Q>, R>): _1<_1<A, P>, R> = cat.plus(this, next)

    /**
     * Lift a function to an arrow.
     */
    @MinimumDefinition(Implementation.MUST)
    fun <P, R> arr(f: (P) -> R): _1<_1<A, P>, R>

    @MinimumDefinition(Implementation.SELECTION)
    fun <P, R, Q> first(ar: _1<_1<A, P>, R>): _1<_1<A, Pair<P, Q>>, Pair<R, Q>> = defaultFirst(ar)

    fun <P, R, Q> defaultFirst(ar: _1<_1<A, P>, R>): _1<_1<A, Pair<P, Q>>, Pair<R, Q>> = ar `***` id<Q>()

    fun <P, R, Q> second(ar: _1<_1<A, P>, R>): _1<_1<A, Pair<Q, P>>, Pair<Q, R>> = id<Q>() `***` ar

    @MinimumDefinition(Implementation.SELECTION)
    fun <P, Q, T, U> split(f: _1<_1<A, P>, T>, g: _1<_1<A, Q>, U>): _1<_1<A, Pair<P, Q>>, Pair<T, U>>

    fun <P, Q, T, U> defaultSplit(f: _1<_1<A, P>, T>, g: _1<_1<A, Q>, U>): _1<_1<A, Pair<P, Q>>, Pair<T, U>> =
            first<P, T, Q>(f) then arr(swap<T, Q>()) then first<Q, U, T>(g) then arr(swap<U, T>())

    private fun <P, Q> swap(): (Pair<P, Q>) -> Pair<Q, P> = { it.second to it.first }

    /**
     * infix version of [split]
     */
    infix fun <P, Q, T, U> (_1<_1<A, P>, T>).`***`(g: _1<_1<A, Q>, U>): _1<_1<A, Pair<P, Q>>, Pair<T, U>> =
            split(this, g)

    fun <P, Q, R> fanout(f: _1<_1<A, P>, Q>, g: _1<_1<A, P>, R>): _1<_1<A, P>, Pair<Q, R>> =
            arr { p: P -> p to p } then (f `***` g)

    /**
     * infix version of [fanout]
     */
    infix fun <P, Q, R> _1<_1<A, P>, Q>.`&&&`(g: _1<_1<A, P>, R>): _1<_1<A, P>, Pair<Q, R>> = fanout(this, g)

    /**
     * pre-composition with pure function
     */
    fun <P, Q, R> ((P) -> Q).`~}}`(ar: _1<_1<A, Q>, R>): _1<_1<A, P>, R> = arr(this) then ar

    /**
     * post-composition with pure function
     */
    fun <P, Q, R> _1<_1<A, P>, Q>.`}}~`(f: (Q) -> R): _1<_1<A, P>, R> = this then arr(f)

    fun <P> pureA(): _1<_1<A, P>, P> = id()

    fun <R> exec(action: Arrow<A>.() -> R): R = this.action()

    companion object {

        val fun1: Arrow<Fun1> = object : Arrow<Fun1> {

            fun <P, Q> toArrow(): ((P) -> Q) -> _1<_1<Fun1, P>, Q> = { arr(it) }

            override val cat: Category<Fun1> = Category.fun1

            override fun <P, R> arr(f: (P) -> R): _1<_1<Fun1, P>, R> = f.fun1

            override fun <P, Q, T, U> split(f: _1<_1<Fun1, P>, T>, g: _1<_1<Fun1, Q>, U>): _1<_1<Fun1, Pair<P, Q>>, Pair<T, U>> =
                    { p: Pair<P, Q> -> f.fun1.invoke(p.first) to g.fun1.invoke(p.second) } `$` toArrow()
        }
    }
}
