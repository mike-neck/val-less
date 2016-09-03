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
package valless.type.data.function

object Function {

    fun <T> id(): (T) -> T = { it }

    fun <T, Q> const(): (T) -> ((Q) -> T) = { t -> { t } }

    /**
     * Usage
     * <code><pre>
     *     (compare.on { it.length })("foo", "quux")
     * </pre></code>
     */
    fun <T, R, A, F : (T, T) -> R> F.on(conv: (A) -> T): (A, A) -> R = { f, s -> this(conv(f), conv(s)) }
}
