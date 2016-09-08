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
package valless.type.control.applicative

import valless.type._1
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass

@TypeClass
interface Alternative<F> : Applicative<F> {

    /**
     * <code>Control.Applicative.empty</code>
     *
     * The identity of [(+)].
     */
    @MinimumDefinition(Implementation.MUST)
    fun <T> empty(): _1<F, T>

    /**
     * <code>Control.Applicative.(&lt;|&gt;)</code>
     *
     * An associative binary operation.
     */
    @MinimumDefinition(Implementation.MUST)
    fun <T> `_+_`(): (_1<F, T>) -> (_1<F, T>) -> _1<F, T>
}
