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
import valless.type.data.functor.Functor
import valless.type.data.monoid.Monoid
import valless.type.up
import valless.util.flow.ifItIs
import valless.util.function.`$`
import valless.util.function.times
import valless.util.pair
import valless.util.times

object IntInstance :
        Eq._1_<Int>
        , Ord._1_<Int>
        , Enum<Int>
        , Num<Int> {

    override val eq: Eq<Int> get() = Eq.fromEquals()

    override val ord: Ord<Int> get() = Ord.fromComparable()

    override fun toEnum(i: Int): Int = i

    override fun fromEnum(e: Int): Int = e

    override fun `{+}`(x: Int, y: Int): Int = x + y

    override fun `{-}`(x: Int, y: Int): Int = x - y

    override fun `{*}`(x: Int, y: Int): Int = x * y

    override val zero: Int get() = 0

    override val plusOne: Int get() = 1

    override fun negate(x: Int): Int = -x

    override fun signum(x: Int): Int = when (ord.compare(x, 0)) {
        Ordering.LT -> -1
        Ordering.EQ -> 0
        Ordering.GT -> 1
    }

    override fun abs(x: Int): Int = if (x < 0) -x else x

    override fun fromIntegral(x: Integral): Int = x.value().toInt()
}

object LongInstance :
        Eq._1_<Long>
        , Ord._1_<Long>
        , Enum<Long>
        , Num<Long> {

    override val eq: Eq<Long> get() = Eq.fromEquals()

    override val ord: Ord<Long> get() = Ord.fromComparable()

    override fun toEnum(i: Int): Long = i.toLong()

    override fun fromEnum(e: Long): Int =
            if (e <= Int.MAX_VALUE && e >= Int.MIN_VALUE) e.toInt()
            else throw IllegalArgumentException("Enum.fromEnum.Long : the value is out of bound.")

    override fun succ(e: Long): Long = e + 1

    override fun pred(e: Long): Long = e - 1

    override fun `{+}`(x: Long, y: Long): Long = x + y

    override fun `{-}`(x: Long, y: Long): Long = x - y

    override fun `{*}`(x: Long, y: Long): Long = x * y

    override val zero: Long get() = 0

    override val plusOne: Long get() = 1

    override fun negate(x: Long): Long = -x

    override fun signum(x: Long): Long = when (ord.compare(x, 0)) {
        Ordering.LT -> -1
        Ordering.EQ -> 0
        Ordering.GT -> 1
    }

    override fun abs(x: Long): Long = if (x < 0) -x else x

    override fun fromIntegral(x: Integral): Long = x.value()
}

private class PairHk<P, Q>(val p: Pair<P, Q>) : _2<PairInstance, P, Q>

val <Q, P> _2<PairInstance, P, Q>.narrow: Pair<P, Q> get() = (this as PairHk<P, Q>).p

val <Q, P> Pair<P, Q>.hkt: _2<PairInstance, P, Q> get() = PairHk(this)

object PairInstance :
        Eq.Deriving2<PairInstance>
        , Ord.Deriving2<PairInstance>
        , Traversable._2_<PairInstance>
        , Functor._2_<PairInstance> {
    fun <P, Q> eq(): Eq<Pair<P, Q>> = Eq.fromEquals()

    fun <P, Q, R> map(p: Pair<P, Q>, f: (Q) -> R): Pair<P, R> = p.first to f(p.second)

    fun <S, T> narrow(obj: _1<_1<PairInstance, S>, T>): Pair<S, T> = obj.up.narrow

    override fun <F, S> eq(f: Eq<F>, s: Eq<S>): Eq<_1<_1<PairInstance, F>, S>> = object : Eq<_1<_1<PairInstance, F>, S>> {
        override fun eq(x: _1<_1<PairInstance, F>, S>, y: _1<_1<PairInstance, F>, S>): Bool =
                PairInstance.eq<F, S>().eq(x.up.narrow, y.up.narrow)
    }

    override fun <F, S> ord(f: Ord<F>, s: Ord<S>): Ord<_1<_1<PairInstance, F>, S>> = object : Ord<_1<_1<PairInstance, F>, S>> {
        override fun compare(x: _1<_1<PairInstance, F>, S>, y: _1<_1<PairInstance, F>, S>): Ordering =
                (x.up.narrow to y.up.narrow) `$`
                        { it to f.compare(it.first.first, it.second.first) } `$`
                        ifItIs<Pair<Pair<Pair<F, S>, Pair<F, S>>, Ordering>> { it.second == Ordering.EQ }
                                .then { s.compare(it.first.first.second, it.first.second.second) }
                                .els { it.second }
    }

    override fun <S> functor(): Functor<_1<PairInstance, S>> = object : Functor<_1<PairInstance, S>> {
        override fun <T, R> map(obj: _1<_1<PairInstance, S>, T>, f: (T) -> R): _1<_1<PairInstance, S>, R> =
                this@PairInstance.map(obj.up.narrow, f).hkt
    }

    override fun <S> traversable(): Traversable<_1<PairInstance, S>> = object : Traversable<_1<PairInstance, S>> {
        override fun <T, R> map(obj: _1<_1<PairInstance, S>, T>, f: (T) -> R): _1<_1<PairInstance, S>, R> =
                this@PairInstance.map(obj.up.narrow, f).hkt

        override fun <T, R> foldr(ta: _1<_1<PairInstance, S>, T>, init: R, f: (T) -> (R) -> R): R =
                (ta.up.narrow.second `$` f) * init

        override fun <T, R> foldMap(m: Monoid<R>, ta: _1<_1<PairInstance, S>, T>, f: (T) -> R): R =
                f(ta.up.narrow.second)

        override fun <P, R, F> traverse(m: Applicative<F>, ta: _1<_1<PairInstance, S>, P>, f: (P) -> _1<F, R>): _1<F, _1<_1<PairInstance, S>, R>> =
                ta.up.narrow `$` { o -> m.map(o.second `$` f) { (o.first to it).hkt } }

        override fun <T> isNull(ta: _1<_1<PairInstance, S>, T>): Bool = Bool.False

        override fun <T> size(ta: _1<_1<PairInstance, S>, T>): Int = 1

        override fun <T> any(ta: _1<_1<PairInstance, S>, T>, pred: (T) -> Bool): Bool =
                ta.up.narrow.second `$` pred

        override fun <T> all(ta: _1<_1<PairInstance, S>, T>, pred: (T) -> Bool): Bool =
                ta.up.narrow.second `$` pred

        override fun and(bs: _1<_1<PairInstance, S>, Bool>): Bool = bs.up.narrow.second

        override fun or(bs: _1<_1<PairInstance, S>, Bool>): Bool = bs.up.narrow.second

        override fun <T> elem(e: Eq<T>, sbj: T, xs: _1<_1<PairInstance, S>, T>): Bool =
                e.eq(sbj, xs.up.narrow.second)

        override fun <T> sum(n: Num<T>, xs: _1<_1<PairInstance, S>, T>): T =
                xs.up.narrow.second

        override fun <T> product(n: Num<T>, xs: _1<_1<PairInstance, S>, T>): T =
                xs.up.narrow.second
    }

    fun <S> monad(mn: Monoid<S>): Monad<_1<PairInstance, S>> = object : Monad<_1<PairInstance, S>> {

        override fun <T, R> map(obj: _1<_1<PairInstance, S>, T>, f: (T) -> R): _1<_1<PairInstance, S>, R> =
                this@PairInstance.map(PairInstance.narrow(obj), f).hkt

        override fun <T> pure(value: T): _1<_1<PairInstance, S>, T> =
                (mn.mempty to value).hkt

        override fun <T, R, G : (T) -> R> _1<_1<PairInstance, S>, G>.`(_)`(obj: _1<_1<PairInstance, S>, T>): _1<_1<PairInstance, S>, R> =
                (PairInstance.narrow(this) to PairInstance.narrow(obj)) `$`
                        { mn.append(it.first.first, it.second.first) to it.first.second(it.second.second) } `$`
                        { it.hkt }

        override fun <T, R> bind(obj: _1<_1<PairInstance, S>, T>, f: (T) -> _1<_1<PairInstance, S>, R>): _1<_1<PairInstance, S>, R> =
                PairInstance.narrow(obj) `$`
                        { it.first to narrow(f(it.second)) } `$`
                        { mn.append(it.first, it.second.first) to it.second.second } `$`
                        { it.hkt }
    }

    fun <F, S> monoid(mf: Monoid<F>, ms: Monoid<S>): Monoid<_1<_1<PairInstance, F>, S>> =
            object : Monoid<_1<_1<PairInstance, F>, S>> {

                override fun empty(): _1<_1<PairInstance, F>, S> = (mf.mempty to ms.mempty).hkt

                override fun append(x: _1<_1<PairInstance, F>, S>, y: _1<_1<PairInstance, F>, S>): _1<_1<PairInstance, F>, S> =
                        ((PairInstance.narrow(x) to PairInstance.narrow(y)) `$`
                                { (it.first.first to it.second.first) to (it.first.second to it.second.second) }) *
                                (mf.mappend.pair to ms.mappend.pair) `$` { it.hkt }
            }
}
