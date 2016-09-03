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
import valless.type.data.function.Function.const
import valless.util.function.flip

/**
 * A strong lax semimonoidal endofunctor.
 *
 * This is equivalent to an [Applicative] without [pure].
 */
interface Apply<F> : Functor<F> {

    infix fun <T, R, G : (T) -> R> _1<F, G>.`(_)`(obj: _1<F, T>): _1<F, R>

    fun <T, R> _1<F, T>.`(_`(other: _1<F, R>): _1<F, T> = (this `$$` const<T, R>()) `(_)` other

    fun <T, R> _1<F, T>.`_)`(other: _1<F, R>): _1<F, R> = (this `$$` const<R, T>().flip()) `(_)` other

    val <P, Q, R, G : (P) -> ((Q) -> R)> G.liftF2: (_1<F, P>) -> ((_1<F, Q>) -> _1<F, R>)
        get() = { p -> { q -> (p `$$` this) `(_)` q } }

    val <P, Q, T, R, G : (P) -> ((Q) -> ((T) -> R))> G.liftF3:
            (_1<F, P>) -> ((_1<F, Q>) -> ((_1<F, T>) -> _1<F, R>))
        get() = { p -> { q -> { t -> (p `$$` this) `(_)` q `(_)` t } } }
}
