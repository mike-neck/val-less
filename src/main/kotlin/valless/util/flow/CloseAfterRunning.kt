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
package valless.util.flow

import valless.util.basic.Choice
import valless.util.function.`$`
import valless.util.function.id
import valless.util.initialize

fun <C : AutoCloseable, R> C.run(f: (C) -> R): TryWithResource<R> = CloseAfterRunning(this, f)

interface TryWithResource<R> {
    fun exec(): Choice<R, Exception>
    fun handle(h: (Exception) -> R): R
}

private class CloseAfterRunning<C : AutoCloseable, R>(
        private val closeable: C,
        private val runner: (C) -> R) : TryWithResource<R> {

    private fun run(): Choice<R, Exception> =
            try {
                Choice.First(runner(closeable))
            } catch (e: Exception) {
                try {
                    run { closeable.close() }.let { Choice.Second(e) }
                } catch (i: Exception) {
                    i.initialize({ e }) { f, s -> s.addSuppressed(f) }
                            .let { Choice.Second(it) }
                }
            }

    private fun closeAction(): Choice<Unit, Exception> =
            try {
                closeable.close() `$` { Choice.First(it) }
            } catch (e: Exception) {
                Choice.Second(e)
            }

    private fun close(c: Choice<R, Exception>): Choice<R, Exception> =
            closeAction() `$` {
                when (it) {
                    is Choice.First -> c
                    is Choice.Second -> Choice.Second(it.second)
                }
            }

    private fun classifyResult(c: Choice<R, Exception>): Choice<R, Exception> =
            when (c) {
                is Choice.First -> close(c)
                is Choice.Second -> c
            }

    private fun getResult(): Choice<R, Exception> = run() `$` { classifyResult(it) }

    override fun exec(): Choice<R, Exception> = getResult()

    override fun handle(h: (Exception) -> R): R =
            exec() `$` { it.onFirst(id()).onSecond(h) }
}
