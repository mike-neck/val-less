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
package valless.type.data.functor

import valless.type._1
import valless.type._3
import valless.type.control.applicative.Applicative
import valless.type.data.*
import valless.type.data.functor.classes.Eq1
import valless.type.data.functor.classes.Ord1
import valless.type.data.monoid.Monoid
import valless.type.up
import valless.util.function.`$`
import valless.util.function.times
import valless.util.function.uncurry

/**
 * Containered container type.
 *
 * The kind of [F] is 2 or more.
 * The kind of [G] is 2 or more.
 * The kind of [T] is 1.
 *
 * Example 1: Simple example.
 * <code><pre>
 *     val a: Maybe&lt;Identity&lt;Int&gt;&gt; = ...
 *     val compose: Compose&lt;Maybe.Companion, Identity.Companion, Int&gt; = Companion(a)
 * </pre></code>
 *
 * Example 2: Complex example.
 * <code><pre>
 *     val a: Maybe&lt;Either&lt;String, Int&gt;&gt; = ...
 *     // Either&lt;String, Int&gt; = _2&lt;Either.Companion, String, Int&gt;
 *     val b: Maybe&lt;_2&lt;Either.Companion, String, Int&gt;&gt; = a
 *     // _2&lt;Either.Companion, String, Int&gt; = _1&lt;_1&lt;Either.Companion, String&gt;, Int&gt;
 *     val c: Maybe&lt;_1&lt;_1&lt;Either.Companion, String&gt;, Int&gt;&gt; = b
 *     val com: Compose&lt;Maybe.Companion, _1&lt;Either.Companion, String&gt;, Int&gt;
 * </pre></code>
 */
class Compose<F, G, T>(val compose: _1<F, _1<G, T>>) : _3<Compose.Companion, F, G, T> {

    companion object :
            Applicative.Deriving2<Companion>
            , Traversable.Deriving2<Companion> {

        fun <F, G, T> toCompose(): (_1<F, _1<G, T>>) -> Compose<F, G, T> = { Compose(it) }

        fun <F, G, T, R> map(af: Functor<F>, ag: Functor<G>, obj: Compose<F, G, T>, f: (T) -> R): Compose<F, G, R> =
                ((ag.map(f) `$` { af.map(it) }) * obj.compose) `$` toCompose()

        fun <F, G, T> getCompose(c: _1<_1<_1<Companion, F>, G>, T>): _1<F, _1<G, T>> = c.up.up.narrow.compose

        fun <F, G, T> eq(ef: Eq1<F>, eg: Eq1<G>, et: Eq<T>): Eq<_1<_1<_1<Companion, F>, G>, T>> = object : Eq<_1<_1<_1<Companion, F>, G>, T>> {
            override fun eq(x: _1<_1<_1<Companion, F>, G>, T>, y: _1<_1<_1<Companion, F>, G>, T>): Bool =
                    ef.liftEq(eg.eq(et).uncurry)(getCompose(x))(getCompose(y))
        }

        fun <F, G, T> ord(of: Ord1<F>, og: Ord1<G>, ot: Ord<T>): Ord<_1<_1<_1<Companion, F>, G>, T>> = object : Ord<_1<_1<_1<Companion, F>, G>, T>> {
            override fun compare(x: _1<_1<_1<Companion, F>, G>, T>, y: _1<_1<_1<Companion, F>, G>, T>): Ordering =
                    of.liftCompare(og.compare(ot).uncurry)(getCompose(x))(getCompose(y))
        }

        override fun <F, G> traversable(af: Traversable<F>, ag: Traversable<G>): Traversable<_1<_1<Companion, F>, G>> = object : Traversable<_1<_1<Companion, F>, G>> {
            override fun <T, R> map(obj: _1<_1<_1<Companion, F>, G>, T>, f: (T) -> R): _1<_1<_1<Companion, F>, G>, R> =
                    this@Companion.map(af, ag, obj.up.up.narrow, f)

            override fun <P, R, M> traverse(m: Applicative<M>, ta: _1<_1<_1<Companion, F>, G>, P>, f: (P) -> _1<M, R>): _1<M, _1<_1<_1<Companion, F>, G>, R>> =
                    af.traverse(m, getCompose(ta)) { ag.traverse(m, it, f) } `$` m.map(toCompose())

            override fun <T, R> foldMap(m: Monoid<R>, ta: _1<_1<_1<Companion, F>, G>, T>, f: (T) -> R): R =
                    af.foldMap(m, getCompose(ta)) { ag.foldMap(m, it, f) }
        }

        override fun <F, G> applicative(af: Applicative<F>, ag: Applicative<G>): Applicative<_1<_1<Companion, F>, G>> =
                object : Applicative<_1<_1<Companion, F>, G>> {

                    override fun <T, R> map(obj: _1<_1<_1<Companion, F>, G>, T>, f: (T) -> R): _1<_1<_1<Companion, F>, G>, R> =
                            this@Companion.map(af, ag, obj.up.up.narrow, f)

                    override fun <T> pure(value: T): _1<_1<_1<Companion, F>, G>, T> =
                            value `$` ag.pure() `$` af.pure() `$` toCompose()

                    @Suppress("UNCHECKED_CAST")
                    fun <T, R, C : (T) -> R> inferFun(f: _1<_1<_1<Companion, F>, G>, C>): _1<_1<_1<Companion, F>, G>, (T) -> R> =
                            f as _1<_1<_1<Companion, F>, G>, (T) -> R>

                    override fun <T, R, C : (T) -> R> _1<_1<_1<Companion, F>, G>, C>.`(_)`(obj: _1<_1<_1<Companion, F>, G>, T>): _1<_1<_1<Companion, F>, G>, R> =
                            (af.map<_1<G, (T) -> R>, (_1<G, T>) -> _1<G, R>>(ag.ap()) * getCompose(inferFun(this))) `$`
                                    { af.ap(it, getCompose(obj)) } `$` toCompose()
                }
    }
}

val <F, G, T> _3<Compose.Companion, F, G, T>.narrow: Compose<F, G, T> get() = this as Compose<F, G, T>
