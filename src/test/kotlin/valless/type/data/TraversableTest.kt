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
package valless.type.data

import valless.type._1
import valless.type.data.functor.Identity
import valless.type.data.functor.narrow
import valless.util.function.flip
import valless.util.function.plus

interface TraversableTest<F> {

    val tr: Traversable<F>

    fun <T> `traverse Identity`(): (_1<F, T>) -> Identity<_1<F, T>> =
            tr.traverse<T, T, Identity.Companion>(Identity.monad).flip()(Identity.toIdentity<T>()).narrow

    fun <T> identity(): (_1<F, T>) -> Identity<_1<F, T>> = Identity.toIdentity<_1<F, T>>()

    /**
     * Identity
     */
    fun `traverse Identity = Identity`(): Unit


    fun <T> eta(): (_1<Identity.Companion, T>) -> _1<Maybe.Companion, T> = { Maybe.monad.pure(it.narrow.identity) }

    fun <T> `eta _ traverse f`(): (_1<F, T>) -> _1<Maybe.Companion, _1<F, T>> =
            tr.traverse<T, T, Identity.Companion>(Identity.monad).flip()(Identity.toIdentity<T>()) + eta()

    fun <T> `traverse (eta _ f)`(): (_1<F, T>) -> _1<Maybe.Companion, _1<F, T>> =
            tr.traverse<T, T, Maybe.Companion>(Maybe.monad).flip()(Identity.toIdentity<T>() + eta())

    /**
     * Naturality
     */
    fun `eta _ traverse f = traverse (eta _ f)`()
}
