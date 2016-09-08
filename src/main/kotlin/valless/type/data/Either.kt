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
import valless.type.up

sealed class Either<L, R> : _2<Either.Companion, L, R> {

    class Left<L, R>(val left: L) : Either<L, R>()

    class Right<L, R>(val right: R) : Either<L, R>()

    companion object :
            Eq.Deriving2<Companion>
            , Ord.Deriving2<Companion> {
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
