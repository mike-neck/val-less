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
import valless.type.data.monoid.Dual
import valless.type.data.monoid.Endo
import valless.type.data.monoid.Monoid
import valless.type.data.monoid.narrow
import valless.util.function.`$`
import valless.util.function.flip
import valless.util.function.id
import valless.util.function.plus

interface Foldable<F> {

    /**
     * <code>Data.Foldable.foldr</code>
     * <code>(a -> b -> b) -> b -> t a -> b</code>
     *
     * default implementation is
     * <code>foldr f z t = appEndo (foldMap (Endo . f) t) z</code>
     */
    fun <T, R> foldr(ta: _1<F, T>, init: R, f: (T) -> ((R) -> R)): R =
            foldMap(Endo.monoid<R>(), ta, f + toEndo())
                    .narrow.appEndo(init)

    private fun <T> toEndo(): ((T) -> T) -> Endo<T> = { f -> Endo(f) }

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
                    .let { foldMap(Dual.monoid(Endo.monoid<R>().narrow), ta, it) }
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

    fun <T> isNull(ta: _1<F, T>): Bool = foldr(ta, Bool.True) { { Bool.False } }

    fun <T> size(ta: _1<F, T>): Int = foldld(ta, 0) { c -> { c + 1 } }

    fun <T> toDual(): (T) -> Dual<T> = { Dual(it) }
}
