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
package valless.type.data

import valless.type._1
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass
import valless.type.control.applicative.Applicative
import valless.type.control.monad.Monad
import valless.type.data.functor.Functor
import valless.util.function.id
import valless.util.function.times

/**
 * Minimal Definition is [traverse] or [sequenceA]
 */
@TypeClass
interface Traversable<T> : Functor<T>, Foldable<T> {

    interface _1_<T> {
        val traversable: Traversable<T>
    }

    /**
     * <code>Data.Traversable.traverse</code>
     * <code><pre>
     *     Applicative f => (a -> f b) -> t a -> f (t b)
     * </pre></code>
     */
    @MinimumDefinition(Implementation.SELECTION)
    fun <P, R, F> traverse(m: Applicative<F>, ta: _1<T, P>, f: (P) -> _1<F, R>): _1<F, _1<T, R>> =
            sequenceA(m, map(ta, f))

    /**
     * function type of [traverse]
     */
    fun <P, R, F> traverse(m: Applicative<F>): (_1<T, P>) -> (((P) -> _1<F, R>) -> _1<F, _1<T, R>>) =
            { ta -> { f -> traverse(m, ta, f) } }

    /**
     * <code>Data.Traversable.sequenceA</code>
     * <code><pre>
     *     Applicative f => t (f a) -> f (t a)
     * </pre></code>
     */
    @MinimumDefinition(Implementation.SELECTION)
    fun <P, F> sequenceA(m: Applicative<F>, ta: _1<T, _1<F, P>>): _1<F, _1<T, P>> =
            traverse(m, ta, id())

    /**
     * function type of [sequenceA]
     */
    fun <P, F> sequenceA(m: Applicative<F>): (_1<T, _1<F, P>>) -> _1<F, _1<T, P>> =
            { ta -> sequenceA(m, ta) }

    /**
     * <code>Data.Traversable.for</code>
     * <code><pre>
     *     (Traversable t, Applicative f) => t a -> (a -> f b) -> f (t b)
     * </pre></code>
     */
    fun <P, R, F> forAp(m: Applicative<F>, ta: _1<T, P>, f: (P) -> _1<F, R>): _1<F, _1<T, R>> =
            traverse<P, R, F>(m) * ta * f

    /**
     * Mapping each element to monadic action, and collect results.
     * <code>Data.Traversable.mapM</code>
     */
    fun <P, R, M> mapM(m: Monad<M>, ta: _1<T, P>, f: (P) -> _1<M, R>): _1<M, _1<T, R>> =
            traverse(m, ta, f)

    /**
     * Evaluate each monadic action, then collect results.
     * <code>Data.Traversable.sequence</code>
     */
    fun <P, M> sequence(m: Monad<M>, ta: _1<T, _1<M, P>>): _1<M, _1<T, P>> = sequenceA(m, ta)
}
