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
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass
import valless.type.control.applicative.Alternative

@TypeClass
interface MonadPlus<M> : Monad<M>, Alternative<M> {

    interface _1_<M> {
        val monadPlus: MonadPlus<M>
    }

    /**
     * The identity.
     */
    @MinimumDefinition(Implementation.MUST)
    fun <T> mzero(): _1<M, T>

    /**
     * An associative operation.
     */
    @MinimumDefinition(Implementation.MUST)
    fun <T> mplus(x: _1<M, T>, y: _1<M, T>): _1<M, T>
}
