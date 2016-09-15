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
package valless.type.control

import valless.type._1
import valless.type.annotation.Implementation
import valless.type.annotation.MinimumDefinition
import valless.type.annotation.TypeClass
import valless.type.data.function.Fun1
import valless.type.data.function.fun1
import valless.util.function.plus

@TypeClass
interface Category<C> {

    @MinimumDefinition(Implementation.MUST)
    fun <T> id(): _1<_1<C, T>, T>

    @MinimumDefinition(Implementation.MUST)
    fun <P, Q, R> plus(pq: _1<_1<C, P>, Q>, qr: _1<_1<C, Q>, R>): _1<_1<C, P>, R>

    infix fun <P, Q, R> _1<_1<C, P>, Q>.then(next: _1<_1<C, Q>, R>): _1<_1<C, P>, R> = this@Category.plus(this, next)

    fun <P> exec(action: Category<C>.() -> P): P = this.action()

    companion object {

        val fun1: Category<Fun1> get() = object : Category<Fun1> {

            override fun <T> id(): _1<_1<Fun1, T>, T> = { t: T -> t }.fun1

            override fun <P, Q, R> plus(pq: _1<_1<Fun1, P>, Q>, qr: _1<_1<Fun1, Q>, R>): _1<_1<Fun1, P>, R> =
                    (pq.fun1 + qr.fun1).fun1
        }
    }
}
