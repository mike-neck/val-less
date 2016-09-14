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
import valless.type.control.monad.Monad
import valless.type.data.functor.Identity
import valless.util.function.`$`

interface WriterT<W, M, T> : _3<WriterT.Companion, W, M, T> {

    val mn: Monad<M>

    val runWriterT: _1<M, Pair<T, W>>

    private class Impl<W, M, T>(override val mn: Monad<M>, override val runWriterT: _1<M, Pair<T, W>>) : WriterT<W, M, T>

    companion object {

        fun <W, M, T> writer(mn: Monad<M>, obj: Pair<T, W>): WriterT<W, M, T> = Impl(mn, mn.pure(obj))

        fun <W, M, T> toWriter(mn: Monad<M>): (Pair<T, W>) -> WriterT<W, M, T> = { p -> writer(mn, p) }

        fun <W, M, T> toWriterImpl(mn: Monad<M>): (_1<M, Pair<T, W>>) -> WriterT<W, M, T> = { p -> Impl(mn, p) }

        fun <W, M> tell(mn: Monad<M>, value: W): WriterT<W, M, Unit> = writer(mn, Unit to value)

        fun <W, M, T> listen(wr: WriterT<W, M, T>): WriterT<W, M, Pair<T, W>> =
                (wr.runWriterT to { p: Pair<T, W> -> (p.first to p.second) to p.second }) `$`
                        { wr.mn.map(it.first, it.second) } `$` toWriterImpl(wr.mn)

        fun <W, M, T, R> listens(wr: WriterT<W, M, T>, f: (W) -> R): WriterT<W, M, Pair<T, R>> =
                (wr.runWriterT to { p: Pair<T, W> -> (p.first to f(p.second)) to p.second }) `$`
                        { wr.mn.map(it.first, it.second) } `$` toWriterImpl(wr.mn)

        fun <W, M, T> pass(wr: WriterT<W, M, Pair<T, (W) -> W>>): WriterT<W, M, T> =
                (wr.runWriterT to { pp: Pair<Pair<T, (W) -> W>, W> -> pp.first.first to pp.first.second(pp.second) }) `$`
                        { wr.mn.map(it.first, it.second) } `$` toWriterImpl(wr.mn)

        fun <W, M, T> censor(wr: WriterT<W, M, T>, f: (W) -> W): WriterT<W, M, T> =
                (wr.runWriterT to { p: Pair<T, W> -> p.first to f(p.second) }) `$`
                        { wr.mn.map(it.first, it.second) } `$` toWriterImpl(wr.mn)
    }
}

val <W, M, T> _3<WriterT.Companion, W, M, T>.narrow: WriterT<W, M, T> get() = this as WriterT<W, M, T>

class Writer<W, T>(val runWriter: Pair<T, W>) : WriterT<W, Identity.Companion, T> {

    override val mn: Monad<Identity.Companion> get() = Identity.monad

    override val runWriterT: _1<Identity.Companion, Pair<T, W>>
        get() = runWriter `$` Identity.toIdentity()

    companion object {

        fun <W, T> writer(obj: Pair<T, W>): Writer<W, T> = Writer(obj)

        fun <W, T> toWriter(): (Pair<T, W>) -> Writer<W, T> = { writer(it) }

        fun <P, W, T> toWriter(f: (P) -> (Pair<T, W>)): (P) -> Writer<W, T> = { writer(f(it)) }

        fun <W> tell(value: W): Writer<W, Unit> = writer(Unit to value)

        fun <W, T> listen(wr: Writer<W, T>): Writer<W, Pair<T, W>> =
                wr.runWriter `$` toWriter { (it.first to it.second) to it.second }

        fun <W, T, R> listens(wr: Writer<W, T>, f: (W) -> R): Writer<W, Pair<T, R>> =
                wr.runWriter `$` toWriter { (it.first to f(it.second)) to it.second }

        fun <W, T> pass(wr: Writer<W, Pair<T, (W) -> W>>): Writer<W, T> =
                wr.runWriter `$` toWriter { it.first.first to it.first.second(it.second) }

        fun <W, T> censor(wr: Writer<W, T>, f: (W) -> W): Writer<W, T> =
                wr.runWriter `$` toWriter { it.first to f(it.second) }
    }
}
