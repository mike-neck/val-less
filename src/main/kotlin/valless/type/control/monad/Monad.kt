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
package valless.type.control.monad

import valless.type._1
import valless.type.control.applicative.Applicative
import valless.util.function.id

interface Monad<M> : Applicative<M> {

    /**
     * Sequential action composer.
     *
     * <code>Control.Monad.>>=</code>
     * <code><pre>
     *     >>= :: (Monad m) => (a -> m b) -> m a -> m b
     * </pre></code>
     */
    fun <T, R> bind(obj: _1<M, T>, f: (T) -> _1<M, R>): _1<M, R>

    /**
     * Conventional [Monad] join operator.
     *
     * <code>Control.Monad.join</code>
     */
    fun <T> join(obj: _1<M, _1<M, T>>): _1<M, T> = bind(obj, id())

    /**
     * Discarding a value produced by the first action.
     */
    fun <T, R> discardFirst(fst: _1<M, T>, snd: _1<M, R>): _1<M, R> = bind(fst) { snd }

    /**
     * Function version of [discardFirst]
     */
    fun <T, R> disc(): (_1<M, T>) -> ((_1<M, R>) -> _1<M, R>) = { f -> { s -> discardFirst(f, s) } }
}
