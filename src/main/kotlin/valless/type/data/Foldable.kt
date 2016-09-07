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
import valless.type.control.applicative.Alternative
import valless.type.control.applicative.Applicative
import valless.type.control.monad.Monad
import valless.type.control.monad.MonadPlus
import valless.type.data.monoid.*
import valless.type.data.monoid.Dual.Companion.toDual
import valless.type.data.monoid.Endo.Companion.toEndo
import valless.util.function.`$`
import valless.util.function.flip
import valless.util.function.id
import valless.util.function.plus

interface Foldable<F> {

    interface _1_<F> {
        val foldable: Foldable<F>
    }

    /**
     * <code>Data.Foldable.foldr</code>
     * <code>(a -> b -> b) -> b -> t a -> b</code>
     *
     * default implementation is
     * <code>foldr f z t = appEndo (foldMap (Endo . f) t) z</code>
     */
    fun <T, R> foldr(ta: _1<F, T>, init: R, f: (T) -> ((R) -> R)): R =
            foldMap(Endo.monoid<R>().monoid, ta, f + toEndo())
                    .narrow.appEndo(init)

    /**
     * <code>Data.Foldable.foldMap</code>
     * <code>Monoid m => (a -> m) -> t a -> m</code>
     *
     * default implementation is
     * <code>foldMap f = foldr (mappend . f) mempty</code>
     */
    fun <T, R> foldMap(m: Monoid<R>, ta: _1<F, T>, f: (T) -> R): R = foldr(ta, m.mempty, f + m.mappend)

    /**
     * <code>Data.Foldable.fold</code>
     * <code>Monoid m => t m -> m</code>
     */
    fun <T> fold(m: Monoid<T>, tm: _1<F, T>): T = foldMap(m, tm, id())

    /**
     * <code>Data.Foldable.foldl</code>
     * <code>(b -> a -> b) -> b -> t a -> b</code>
     *
     * default implementation is
     * <code>foldl f z t = appEndo (getDual (foldMap (Dual . Endo . flip f) t)) z</code>
     */
    fun <T, R> foldl(ta: _1<F, T>, init: R, f: (R) -> ((T) -> R)): R =
            (f.flip() + toEndo() + toDual())
                    .let { foldMap(Dual.monoid(Endo.monoid<R>().monoid.narrow), ta, it) }
                    .narrow.dual.appEndo(init)

    /**
     * <code>Data.Foldable.foldl'</code>
     * <code>(b -> a -> b) -> b -> t a -> b</code>
     *
     * default implementation is
     * <code><pre>
     *     foldl' f z0 xs = foldr f' id xs z0
     *       where f' x k z = k $! f z x
     * </pre></code>
     */
    fun <T, R> foldld(ta: _1<F, T>, init: R, f: (R) -> ((T) -> R)): R =
            ff<T, R, R>(f) `$` { foldr(ta, init, it.flip()(id())) }

    /**
     * <code><pre>
     *     f' :: (a -> b -> a) -> b -> (b -> c) -> b -> c
     *     f' f k x z = k $! f z x
     * </pre></code>
     */
    private fun <P, Q, R> ff(f: (R) -> ((P) -> R)): (P) -> (((R) -> Q) -> ((R) -> Q)) =
            { p -> { g -> { r -> f(r)(p) `$` g } } }

    /**
     * <code>Data.Foldable.null</code>
     */
    fun <T> isNull(ta: _1<F, T>): Bool = foldr(ta, Bool.True) { { Bool.False } }

    /**
     * <code>Data.Foldable.length</code>
     */
    fun <T> size(ta: _1<F, T>): Int = foldld(ta, 0) { c -> { c + 1 } }

    /**
     * <code>Data.Foldable.any</code>
     *
     * default implementation is
     * <code><pre>
     *     any :: Foldable t => (a -> Bool) -> t a -> Bool
     *     any p = getAny . foldMap (Any . p)
     * </pre></code>
     */
    fun <T> any(ta: _1<F, T>, pred: (T) -> Bool): Bool =
            foldMap(OrInstances.monoid, ta, pred + ::Or).or

    /**
     * <code>Data.Foldable.all</code>
     *
     * default implementation is
     * <code><pre>
     *     all :: Foldable t => (a -> Bool) -> t a -> Bool
     *     all p = getAll . foldMap (All . p)
     * </pre></code>
     */
    fun <T> all(ta: _1<F, T>, pred: (T) -> Bool): Bool =
            foldMap(AndInstances.monoid, ta, pred + ::And).and

    /**
     * <code>Data.Foldable.and</code>
     */
    fun and(bs: _1<F, Bool>): Bool = foldMap(AndInstances.monoid, bs, ::And).and

    /**
     * <code>Data.Foldable.or</code>
     */
    fun or(bs: _1<F, Bool>): Bool = foldMap(OrInstances.monoid, bs, ::Or).or

    /**
     * <code>Data.Foldable.elem</code>
     */
    fun <T> elem(e: Eq<T>, sbj: T, xs: _1<F, T>): Bool = any(xs) { e.eq(it, sbj) }

    /**
     * <code>Data.Foldable.sum</code>
     */
    fun sum(xs: _1<F, Int>): Int = foldMap(SumInstances.monoid, xs, ::Sum).sum

    /**
     * <code>Data.Foldable.product</code>
     */
    fun product(xs: _1<F, Long>): Long =
            foldMap(ProductInstance.monoid, xs, ::Product).product

    /**
     * <code>Data.Foldable.traverse_</code>
     */
    fun <M, T, R> traverse_(m: Applicative<M>, ta: _1<F, T>, f: (T) -> _1<M, R>): _1<M, Unit> =
            foldr(ta, m.pure(Unit), f + m.rgt<R, Unit>())

    /**
     * Mapping each element to monadic action, and ignore results.
     * <code>Data.Foldable.mapM</code>
     */
    fun <P, R, M> mapM_(m: Monad<M>, ta: _1<F, P>, f: (P) -> _1<M, R>): _1<M, Unit> =
            foldr(ta, m.pure(Unit), f + m.disc<R, Unit>())

    /**
     * Evaluate each monadic action, and ignore results.
     * <code>Data.Foldable.sequence_</code>
     */
    fun <M, T> sequence_(m: Monad<M>, ta: _1<F, _1<M, T>>): _1<M, Unit> =
            foldr(ta, m.pure(Unit), m.disc<T, Unit>())

    /**
     * <code>Data.Foldable.foldrM</code>
     *
     * Monadic fold over the elements of a structure, from right to left.
     */
    fun <P, R, M> foldrM(m: Monad<M>, ta: _1<F, P>, init: R, f: (P) -> ((R) -> _1<M, R>)): _1<M, R> =
            foldl(ta, { m.pure(it) }, ff(m, f))(init)

    fun <P, R, M> ff(m: Monad<M>, f: (P) -> ((R) -> _1<M, R>)): ((R) -> _1<M, R>) -> ((P) -> ((R) -> _1<M, R>)) =
            { g -> { x -> { z -> m.bind(f(x)(z), g) } } }

    /**
     * <code>Data.Foldable.foldM</code>
     *
     * Monadic fold over the elements of a structure from left to right.
     */
    fun <P, R, M> foldlM(m: Monad<M>, ta: _1<F, P>, init: R, f: (R) -> ((P) -> _1<M, R>)): _1<M, R> =
            { p: P -> { g: Function1<R, _1<M, R>> -> { r: R -> m.bind(f(r)(p), g) } } }
                    .let { foldr(ta, { m.pure(it) }, it) }(init)

    /**
     * <code>Data.Foldable.sequenceA_</code>
     *
     * Evaluate each action in the structure from left to right, ignoring results.
     * For the versions that doesn't ignore the results see [Traversable.sequenceA].
     */
    fun <P, M> sequenceA_(a: Applicative<M>, ta: _1<F, _1<M, P>>): _1<M, Unit> =
            foldr(ta, a.pure(Unit), a.rgt())

    fun <P, A> asum(a: Alternative<A>, ta: _1<F, _1<A, P>>): _1<A, P> =
            foldr(ta, a.empty(), a.`(+)`())

    fun <P, M> msum(m: MonadPlus<M>, ta: _1<F, _1<M, P>>): _1<M, P> = asum(m, ta)
}
