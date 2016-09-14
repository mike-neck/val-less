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
package valless.type.control.monad.trans

import valless.type._1
import valless.type._3
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass
import valless.type.control.monad.Monad
import valless.type.control.monad.MonadPlus
import valless.type.data.functor.Identity
import valless.type.up
import valless.util.both
import valless.util.function.`$`
import valless.util.function.flip
import valless.util.function.plus
import valless.util.function.times

interface ReaderT<R, M, T> : _3<ReaderT.Companion, R, M, T> {

    val runReaderT: (R) -> _1<M, T>

    val mn: Monad<M>

    private class Impl<R, M, T>(override val mn: Monad<M>, override val runReaderT: (R) -> _1<M, T>) : ReaderT<R, M, T>

    companion object {

        fun <R, M, T> narrow(obj: _1<_1<_1<Companion, R>, M>, T>): ReaderT<R, M, T> = obj.up.up.narrow

        fun <R, M, T, P> toReaderT(mn: Monad<M>, f: (P) -> (R) -> _1<M, T>): (P) -> ReaderT<R, M, T> = { p -> Impl(mn, f(p)) }

        fun <R, M> monad(mn: Monad<M>): Monad<_1<_1<Companion, R>, M>> = object : Monad<_1<_1<Companion, R>, M>> {
            override fun <P, Q> map(obj: _1<_1<_1<Companion, R>, M>, P>, f: (P) -> Q): _1<_1<_1<Companion, R>, M>, Q> =
                    Companion.narrow(obj) `$` toReaderT(mn) { p -> { r -> mn.map(f)(p.runReaderT(r)) } }

            override fun <T> pure(value: T): _1<_1<_1<Companion, R>, M>, T> = Impl(mn) { mn.pure(value) }

            override fun <P, Q, G : (P) -> Q> _1<_1<_1<Companion, R>, M>, G>.`(_)`(obj: _1<_1<_1<Companion, R>, M>, P>): _1<_1<_1<Companion, R>, M>, Q> =
                    (Companion.narrow(this).runReaderT to Companion.narrow(obj).runReaderT) `$`
                            toReaderT(mn) { p -> { r: R -> mn.ap(p.first(r), p.second(r)) } }

            override fun <P, Q> bind(obj: _1<_1<_1<Companion, R>, M>, P>, f: (P) -> _1<_1<_1<Companion, R>, M>, Q>): _1<_1<_1<Companion, R>, M>, Q> =
                    Impl(mn) { r ->
                        Companion.narrow(obj).runReaderT(r) `$`
                                mn.map(f) `$`
                                mn.bind<_1<_1<_1<Companion, R>, M>, Q>, Q>().flip() *
                                        { o: _1<_1<_1<Companion, R>, M>, Q> -> Companion.narrow(o).runReaderT(r) }
                    }
        }

        fun <R, M> monadPlus(mp: MonadPlus<M>): MonadPlus<_1<_1<Companion, R>, M>> = object : MonadPlus<_1<_1<Companion, R>, M>> {

            val monad: Monad<_1<_1<Companion, R>, M>> = Companion.monad(mp)

            override fun <T> empty(): _1<_1<_1<Companion, R>, M>, T> = Impl(mp) { mp.empty() }

            override fun <T, Q> map(obj: _1<_1<_1<Companion, R>, M>, T>, f: (T) -> Q): _1<_1<_1<Companion, R>, M>, Q> =
                    monad.map(obj, f)

            override fun <T> mzero(): _1<_1<_1<Companion, R>, M>, T> = empty()

            override fun <T> pure(value: T): _1<_1<_1<Companion, R>, M>, T> = monad.pure(value)

            override fun <T> mplus(x: _1<_1<_1<Companion, R>, M>, T>, y: _1<_1<_1<Companion, R>, M>, T>): _1<_1<_1<Companion, R>, M>, T> =
                    (x to y).both { Companion.narrow(it) }.both { it.runReaderT } `$`
                            toReaderT(mp) { p -> { r -> mp.mplus(p.first(r), p.second(r)) } }

            override fun <T> `_+_`(): (_1<_1<_1<Companion, R>, M>, T>) -> (_1<_1<_1<Companion, R>, M>, T>) -> _1<_1<_1<Companion, R>, M>, T> =
                    { x -> { y -> mplus(x, y) } }

            override fun <T, Q, G : (T) -> Q> _1<_1<_1<Companion, R>, M>, G>.`(_)`(obj: _1<_1<_1<Companion, R>, M>, T>): _1<_1<_1<Companion, R>, M>, Q> =
                    monad.ap(this, obj)

            override fun <T, Q> bind(obj: _1<_1<_1<Companion, R>, M>, T>, f: (T) -> _1<_1<_1<Companion, R>, M>, Q>): _1<_1<_1<Companion, R>, M>, Q> =
                    monad.bind(obj, f)
        }

        fun <R, M> monadReader(mn: Monad<M>): MonadReader<Companion, M, R> = object : MonadReader<Companion, M, R> {

            override val mn: Monad<M> = mn

            override fun <T> reader(f: (R) -> _1<M, T>): _1<_1<_1<Companion, R>, M>, T> = Impl(mn, f)

            override fun ask(): _1<_1<_1<Companion, R>, M>, R> = Impl(mn) { mn.pure(it) }

            override fun <T> local(obj: _1<_1<_1<Companion, R>, M>, T>, f: (R) -> R): _1<_1<_1<Companion, R>, M>, T> =
                    reader(f + Companion.narrow(obj).runReaderT)
        }
    }
}

val <R, T> _3<ReaderT.Companion, R, Identity.Companion, T>.narrow: Reader<R, T> get() = this as Reader<R, T>
val <R, M, T>  _3<ReaderT.Companion, R, M, T>.narrow: ReaderT<R, M, T> get() = this as ReaderT<R, M, T>

class Reader<R, T>(override val runReaderT: (R) -> _1<Identity.Companion, T>) : ReaderT<R, Identity.Companion, T> {

    override val mn: Monad<Identity.Companion> = Identity.monad

    val runReader: (R) -> T = runReaderT + Identity.runIdentityN()

    companion object {

        fun <R, T> narrow(obj: _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, T>): Reader<R, T> = obj.up.up.narrow

        fun <R, T> toReader(): ((R) -> T) -> Reader<R, T> = { f -> reader(f) }

        fun <P, R, T> toReader(f: (P) -> (R) -> T): (P) -> Reader<R, T> = { reader(f(it)) }

        fun <R, T> reader(f: (R) -> T): Reader<R, T> = Reader(f + Identity.toIdentity())

        fun <R> monad(): Monad<_1<_1<ReaderT.Companion, R>, Identity.Companion>> = object : Monad<_1<_1<ReaderT.Companion, R>, Identity.Companion>> {

            override fun <T, Q> map(obj: _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, T>, f: (T) -> Q): _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, Q> =
                    Companion.narrow(obj).runReader + f `$` toReader()

            override fun <T> pure(value: T): _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, T> =
                    Companion.reader { value }

            override fun <T, Q, G : (T) -> Q> _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, G>.`(_)`(obj: _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, T>): _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, Q> =
                    (Companion.narrow(this).runReader to Companion.narrow(obj).runReader) `$`
                            { p -> { r: R -> (p.second + p.first(r))(r) } } `$`
                            toReader()

            override fun <T, Q> bind(obj: _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, T>, f: (T) -> _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, Q>): _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, Q> =
                    Companion.narrow(obj).runReader `$` toReader { g -> { r -> narrow((g + f)(r)).runReader(r) } }
        }

        fun <R> monadReader(): MonadReader<ReaderT.Companion, Identity.Companion, R> = object : MonadReader<ReaderT.Companion, Identity.Companion, R> {

            override val mn: Monad<Identity.Companion> = Identity.monad

            override fun <T> reader(f: (R) -> _1<Identity.Companion, T>): _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, T> =
                    Reader(f)

            override fun ask(): _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, R> = reader { mn.pure(it) }

            override fun <T> local(obj: _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, T>, f: (R) -> R): _1<_1<_1<ReaderT.Companion, R>, Identity.Companion>, T> =
                    f + Companion.narrow(obj).runReader `$` toReader()
        }
    }
}

@TypeClass
interface MonadReader<N, M, R> {

    val mn: Monad<M>

    @MinimumDefinition(Implementation.SELECTION)
    fun <T> reader(f: (R) -> _1<M, T>): _1<_1<_1<N, R>, M>, T>

    @MinimumDefinition(Implementation.SELECTION)
    fun ask(): _1<_1<_1<N, R>, M>, R>

    @MinimumDefinition(Implementation.MUST)
    fun <T> local(obj: _1<_1<_1<N, R>, M>, T>, f: (R) -> R): _1<_1<_1<N, R>, M>, T>
}
