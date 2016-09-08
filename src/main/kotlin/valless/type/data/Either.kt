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
import valless.type._2
import valless.type.control.applicative.Applicative
import valless.type.control.monad.Monad
import valless.type.data.monoid.Monoid
import valless.type.up
import valless.util.function.`$`

sealed class Either<L, R> : _2<Either.Companion, L, R> {

    class Left<L, R>(val left: L) : Either<L, R>()

    class Right<L, R>(val right: R) : Either<L, R>()

    fun adjust(f: (L) -> R): R = when (this) {
        is Left -> f(this.left)
        is Right -> this.right
    }

    fun <V> onLeft(lf: (L) -> V): AdjustingValue<R, V> = object : AdjustingValue<R, V> {
        override fun onRight(rf: (R) -> V): V = when (this@Either) {
            is Left -> this@Either.left `$` lf
            is Right -> this@Either.right `$` rf
        }
    }

    interface AdjustingValue<out R, V> {
        fun onRight(rf: (R) -> V): V
    }

    fun isLeft(): Bool = if (this is Left) Bool.True else Bool.False

    fun isRight(): Bool = if (this is Right) Bool.True else Bool.False

    companion object :
            Eq.Deriving2<Companion>
            , Ord.Deriving2<Companion>
            , Traversable._2_<Companion>
            , Monad._2_<Companion> {

        override fun <F, S> eq(f: Eq<F>, s: Eq<S>): Eq<_1<_1<Companion, F>, S>> = object : Eq<_1<_1<Companion, F>, S>> {
            override fun eq(x: _1<_1<Companion, F>, S>, y: _1<_1<Companion, F>, S>): Bool =
                    comparing(x.up.narrow, y.up.narrow)
                            .both_left { xl, yl -> f.eq(xl, yl) }
                            .left_right { xl, yr -> Bool.False }
                            .right_left { xr, yl -> Bool.False }
                            .both_right { xr, yr -> s.eq(xr, yr) }
        }

        override fun <F, S> ord(f: Ord<F>, s: Ord<S>): Ord<_1<_1<Companion, F>, S>> = object : Ord<_1<_1<Companion, F>, S>> {
            override fun compare(x: _1<_1<Companion, F>, S>, y: _1<_1<Companion, F>, S>): Ordering =
                    comparing(x.up.narrow, y.up.narrow)
                            .both_left { xl, yl -> f.compare(xl, yl) }
                            .left_right { xl, yr -> Ordering.LT }
                            .right_left { xr, yl -> Ordering.GT }
                            .both_right { xr, yr -> s.compare(xr, yr) }
        }

        fun <L, R> toRight(): (R) -> Either<L, R> = { Right(it) }

        fun <L, R> toLeft(): (L) -> Either<L, R> = { Left(it) }

        fun <L, R, F> map(obj: _1<_1<Companion, L>, R>, f: (R) -> F): _1<_1<Companion, L>, F> =
                obj.up.narrow
                        .onLeft<_1<_1<Companion, L>, F>> { Left(it) }
                        .onRight { f(it) `$` toRight() }

        override fun <L> traversable(): Traversable<_1<Companion, L>> = object : Traversable<_1<Companion, L>> {

            override fun <T, R> map(obj: _1<_1<Companion, L>, T>, f: (T) -> R): _1<_1<Companion, L>, R> =
                    this@Companion.map(obj, f)

            override fun <T, R> foldr(ta: _1<_1<Companion, L>, T>, init: R, f: (T) -> (R) -> R): R =
                    ta.up.narrow.onLeft { init }.onRight { f(it)(init) }

            override fun <T, R> foldMap(m: Monoid<R>, ta: _1<_1<Companion, L>, T>, f: (T) -> R): R =
                    ta.up.narrow.onLeft { m.mempty }.onRight { f(it) }

            override fun <T> isNull(ta: _1<_1<Companion, L>, T>): Bool = ta.up.narrow.isLeft()

            override fun <T> size(ta: _1<_1<Companion, L>, T>): Int =
                    ta.up.narrow.onLeft { 0 }.onRight { 1 }

            override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<_1<Companion, L>, P>, f: (P) -> _1<F, R>): _1<F, _1<_1<Companion, L>, R>> =
                    ta.up.narrow
                            .onLeft<_1<F, _1<_1<Companion, L>, R>>> { m.pure(Left(it)) }
                            .onRight { m.map(f(it), toRight()) }
        }

        override fun <F> monad(): Monad<_1<Companion, F>> = object : Monad<_1<Companion, F>> {

            override fun <T> pure(value: T): _1<_1<Companion, F>, T> = Right(value)

            override fun <T, R> map(obj: _1<_1<Companion, F>, T>, f: (T) -> R): _1<_1<Companion, F>, R> =
                    this@Companion.map(obj, f)

            override fun <T, R, G : (T) -> R> _1<_1<Companion, F>, G>.`(_)`(obj: _1<_1<Companion, F>, T>): _1<_1<Companion, F>, R> =
                    this.up.narrow `$` { f ->
                        when (f) {
                            is Left -> f.left `$` toLeft()
                            is Right -> obj.up.narrow `$` { o ->
                                when (o) {
                                    is Left -> o.left `$` toLeft<F, R>()
                                    is Right -> o.right `$` f.right `$` toRight()
                                }
                            }
                        }
                    }

            override fun <T, R> bind(obj: _1<_1<Companion, F>, T>, f: (T) -> _1<_1<Companion, F>, R>): _1<_1<Companion, F>, R> =
                    obj.up.narrow
                            .onLeft<_1<_1<Companion, F>, R>> { Left(it) }
                            .onRight { f(it) }
        }
    }
}

val <L, R> _2<Either.Companion, L, R>.narrow: Either<L, R> get() = this as Either<L, R>

private fun <L, R> comparing(x: Either<L, R>, y: Either<L, R>): WhenBothLeft<L, R> = object : WhenBothLeft<L, R> {
    override fun <F> both_left(bl: (L, L) -> F): WhenLeftRight<L, R, F> = object : WhenLeftRight<L, R, F> {
        override fun left_right(lr: (L, R) -> F): WhenRightLeft<L, R, F> = object : WhenRightLeft<L, R, F> {
            override fun right_left(rl: (R, L) -> F): WhenBothRight<L, R, F> = object : WhenBothRight<L, R, F> {
                override fun both_right(br: (R, R) -> F): F =
                        when (x) {
                            is Either.Left -> when (y) {
                                is Either.Left -> bl(x.left, y.left)
                                is Either.Right -> lr(x.left, y.right)
                            }
                            is Either.Right -> when (y) {
                                is Either.Left -> rl(x.right, y.left)
                                is Either.Right -> br(x.right, y.right)
                            }
                        }
            }
        }
    }
}

private interface WhenBothLeft<L, out R> {
    fun <F> both_left(bl: (L, L) -> F): WhenLeftRight<L, R, F>
}

private interface WhenLeftRight<L, out R, F> {
    fun left_right(lr: (L, R) -> F): WhenRightLeft<L, R, F>
}

private interface WhenRightLeft<L, out R, F> {
    fun right_left(rl: (R, L) -> F): WhenBothRight<L, R, F>
}

private interface WhenBothRight<L, out R, F> {
    fun both_right(br: (R, R) -> F): F
}
