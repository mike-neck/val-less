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
package valless.type.data.monoid

import valless.type._1
import valless.type.control.applicative.Applicative
import valless.type.control.monad.Monad
import valless.type.data.*
import valless.util.function.`$`
import valless.util.function.times

/**
 * [Monoid] under addition.
 */
data class Sum<T>(val sum: T) : _1<Sum.Companion, T> {

    companion object :
            Monoid.NumConstraint<Companion>
            , Eq.Deriving<Companion>
            , Ord.Deriving<Companion>
            , Traversable._1_<Companion>
            , Monad._1_<Companion> {

        override fun <T> eq(e: Eq<T>): Eq<_1<Companion, T>> = Eq.deriveFrom(e) { it.sum }

        override fun <T> ord(o: Ord<T>): Ord<_1<Companion, T>> = Ord.deriveFrom(o) { it.sum }

        fun <T> toSum(): (T) -> Sum<T> = { Sum(it) }

        override fun <T> monoid(n: Num<T>): Monoid<_1<Companion, T>> = object : Monoid<_1<Companion, T>> {

            override fun empty(): _1<Companion, T> = Sum(n.zero)

            override fun append(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                    n.calc { x.sum + y.sum } `$` toSum()
        }

        fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> = obj.sum `$` f `$` toSum()

        override val traversable: Traversable<Companion> get() = object : Traversable<Companion> {
            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> = this@Companion.map(obj, f)

            override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<Companion, P>, f: (P) -> _1<F, R>): _1<F, _1<Companion, R>> =
                    m.map(ta.sum `$` f, toSum())

            override fun <T, R> foldr(ta: _1<Companion, T>, init: R, f: (T) -> (R) -> R): R =
                    (ta.sum `$` f) * init

            override fun <T, R> foldMap(m: Monoid<R>, ta: _1<Companion, T>, f: (T) -> R): R =
                    ta.sum `$` f

            override fun <T> isNull(ta: _1<Companion, T>): Bool = Bool.False

            override fun <T> size(ta: _1<Companion, T>): Int = 1

            override fun <T> sum(n: Num<T>, xs: _1<Companion, T>): T = xs.sum

            override fun <T> product(n: Num<T>, xs: _1<Companion, T>): T = xs.sum

            override fun <T> elem(e: Eq<T>, sbj: T, xs: _1<Companion, T>): Bool = e.eq(sbj, xs.sum)
        }

        override val monad: Monad<Companion> get() = object : Monad<Companion> {

            override fun <T> pure(value: T): _1<Companion, T> = value `$` toSum()

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> =
                    this@Companion.map(obj, f)

            override fun <T, R, G : (T) -> R> _1<Companion, G>.`(_)`(obj: _1<Companion, T>): _1<Companion, R> =
                    obj.sum `$` this.sum `$` toSum()

            override fun <T, R> bind(obj: _1<Companion, T>, f: (T) -> _1<Companion, R>): _1<Companion, R> =
                    obj.sum `$` f
        }
    }
}

val <T> _1<Sum.Companion, T>.narrow: Sum<T> get() = this as Sum<T>

val <T> _1<Sum.Companion, T>.sum: T get() = this.narrow.sum

/**
 * [Monoid] under multiplication.
 */
data class Product<T>(val product: T) : _1<Product.Companion, T> {

    companion object : Monoid.NumConstraint<Companion>
            , Eq.Deriving<Companion>
            , Ord.Deriving<Companion>
            , Traversable._1_<Companion>
            , Monad._1_<Companion> {

        fun <T> product(): (_1<Companion, T>) -> T = { it.product }

        fun <T> toProduct(): (T) -> Product<T> = { Product(it) }

        fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> = obj.product `$` f `$` toProduct()

        override fun <T> eq(e: Eq<T>): Eq<_1<Companion, T>> = Eq.deriveFrom(e, product())

        override fun <T> ord(o: Ord<T>): Ord<_1<Companion, T>> = Ord.deriveFrom(o, product())

        override fun <T> monoid(n: Num<T>): Monoid<_1<Companion, T>> = object : Monoid<_1<Companion, T>> {

            override fun empty(): _1<Companion, T> = n.plusOne `$` toProduct()

            override fun append(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                    n.calc { x.product * y.product } `$` toProduct()
        }

        override val traversable: Traversable<Companion> get() = object : Traversable<Companion> {

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> = this@Companion.map(obj, f)

            override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<Companion, P>, f: (P) -> _1<F, R>): _1<F, _1<Companion, R>> =
                    m.map(ta.product `$` f, toProduct())

            override fun <T, R> foldr(ta: _1<Companion, T>, init: R, f: (T) -> (R) -> R): R =
                    (ta.product `$` f) * init

            override fun <T, R> foldMap(m: Monoid<R>, ta: _1<Companion, T>, f: (T) -> R): R =
                    ta.product `$` f

            override fun <T> isNull(ta: _1<Companion, T>): Bool = Bool.False

            override fun <T> size(ta: _1<Companion, T>): Int = 1

            override fun <T> sum(n: Num<T>, xs: _1<Companion, T>): T = xs.product

            override fun <T> product(n: Num<T>, xs: _1<Companion, T>): T = xs.product
        }

        override val monad: Monad<Companion> get() = object : Monad<Companion> {

            override fun <T> pure(value: T): _1<Companion, T> = Product(value)

            override fun <T, R> map(obj: _1<Companion, T>, f: (T) -> R): _1<Companion, R> = this@Companion.map(obj, f)

            override fun <T, R, G : (T) -> R> _1<Companion, G>.`(_)`(obj: _1<Companion, T>): _1<Companion, R> =
                    obj.product `$` this.product `$` toProduct()

            override fun <T, R> bind(obj: _1<Companion, T>, f: (T) -> _1<Companion, R>): _1<Companion, R> =
                    obj.product `$` f
        }
    }
}

val <T> _1<Product.Companion, T>.narrow: Product<T> get() = this as Product<T>

val <T> _1<Product.Companion, T>.product: T get() = this.narrow.product
