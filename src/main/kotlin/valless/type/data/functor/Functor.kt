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
import valless.type.data.tuple.`()`

interface Functor<F> : Invariant<F> {
    /**
     * fmap
     */
    fun <T, R> map(obj: _1<F, T>, f: (T) -> R): _1<F, R>

    /**
     * <code>Data.Functor.&lt;$&gt;</code>
     */
    infix fun <T, R> _1<F, T>.`$$`(f: (T) -> R): _1<F, R> = map(this, f)

    /**
     * <code>Data.Functor.&lt;$</code>
     */
    fun <T, R> `-$`(v: R, obj: _1<F, T>): _1<F, R> = map(obj) { v }

    /**
     * <code>Data.Functor.$&gt;</code>
     */
    fun <T, R> `$-`(obj: _1<F, T>, v: R): _1<F, R> = map(obj) { v }

    /**
     * <code>Data.Functor.void</code>
     */
    fun <T> void(obj: _1<F, T>): _1<F, `()`> = `-$`(`()`, obj)

    override fun <T, R> imap(obj: _1<F, T>, f: (T) -> R, g: (R) -> T): _1<F, R> = map(obj, f)
}
