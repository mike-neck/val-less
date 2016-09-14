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
package valless.type.control.monad

import valless.type._1
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass
import valless.type.control.applicative.Applicative
import valless.util.function.id

@TypeClass
interface Monad<M> : Applicative<M> {

    interface _1_<M> {
        val monad: Monad<M>
    }

    interface _2_<M> {
        fun <T> monad(): Monad<_1<M, T>>
    }

    /**
     * Sequential action composer.
     *
     * <code>Control.Monad.>>=</code>
     * <code><pre>
     *     >>= :: (Monad m) => (a -> m b) -> m a -> m b
     * </pre></code>
     */
    @MinimumDefinition(Implementation.MUST)
    fun <T, R> bind(obj: _1<M, T>, f: (T) -> _1<M, R>): _1<M, R>

    /**
     * Function version of [bind].
     */
    fun <T, R> bind(): (_1<M, T>) -> ((T) -> _1<M, R>) -> _1<M, R> = { o -> { f -> bind(o, f) } }

    /**
     * Conventional [Monad] join operator.
     *
     * <code>Control.Monad.join</code>
     */
    fun <T> join(obj: _1<M, _1<M, T>>): _1<M, T> = bind(obj, id())

    /**
     * Discarding a value produced by the first action.
     */
    fun <T, R> discardFirst(fst: _1<M, T>, snd: _1<M, R>): _1<M, R> = bind(fst) { snd }

    /**
     * Function version of [discardFirst]
     */
    fun <T, R> disc(): (_1<M, T>) -> ((_1<M, R>) -> _1<M, R>) = { f -> { s -> discardFirst(f, s) } }

    /**
     * Left to right Kleisli composition.
     */
    fun <P, Q, R> lrKComposition(f: (P) -> _1<M, Q>, g: (Q) -> _1<M, R>): (P) -> _1<M, R> =
            { p -> bind(f(p), g) }

    /**
     * Function version of [lrKComposition]
     */
    fun <P, Q, R> lrKComp(): ((P) -> _1<M, Q>) -> ((Q) -> _1<M, R>) -> ((P) -> _1<M, R>) =
            { f -> { g -> lrKComposition(f, g) } }

    /**
     * Right to left Kleisli composition.
     */
    fun <P, Q, R> rlKComposition(f: (Q) -> _1<M, R>, g: (P) -> _1<M, Q>): (P) -> _1<M, R> =
            { p -> bind(g(p), f) }

    /**
     * Function version of [rlKComposition]
     */
    fun <P, Q, R> rlKComp(): ((Q) -> _1<M, R>) -> ((P) -> _1<M, Q>) -> ((P) -> _1<M, R>) =
            { f -> { g -> rlKComposition(f, g) } }
}
