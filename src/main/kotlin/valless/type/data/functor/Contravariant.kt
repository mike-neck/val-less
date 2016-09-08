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
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass

@TypeClass
interface Contravariant<F> : Invariant<F> {
    /**
     * Contravariant laws.
     *
     * * <code>cmap(id()) == id()</code>
     * * <code>cmap(f) + cmap(g) == cmap(g + f)</code>
     */
    @MinimumDefinition(Implementation.MUST)
    fun <T, R> cmap(obj: _1<F, T>, f: (R) -> T): _1<F, R>

    @MinimumDefinition(Implementation.MUST)
    fun <T, R> phantom(obj: _1<F, T>): _1<F, R>

    override fun <T, R> imap(obj: _1<F, T>, f: (T) -> R, g: (R) -> T): _1<F, R> = cmap(obj, g)
}
