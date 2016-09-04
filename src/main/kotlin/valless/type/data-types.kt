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
package valless.type

/**
 * Represents a higher kinded type of arity 0.
 * @param [M] - a 'witness' (cited at highj) type of the type to be lifted as a type constructor.
 */
interface _0<M>

/**
 * Represents a higher kinded type of arity 1.
 * @param [M] - a 'witness' (cited at highj) type of the type to be lifted as a type constructor.
 * @param [A] - the 1st parameter of the type constructor.
 */
interface _1<M, A>


/**
 * Represents a higher kinded type of arity 2.
 * @param [M] - a 'witness' (cited at highj) type of the type to be lifted as a type constructor.
 * @param [B] - the 1st parameter of the type constructor.
 * @param [A] - the 2nd parameter of the type constructor.
 */
interface _2<M, B, A> : _1<_1<M, B>, A>


/**
 * Represents a higher kinded type of arity 3.
 * @param [M] - a 'witness' (cited at highj) type of the type to be lifted as a type constructor.
 * @param [C] - the 1st parameter of the type constructor.
 * @param [B] - the 2nd parameter of the type constructor.
 * @param [A] - the 3rd parameter of the type constructor.
 */
interface _3<M, C, B, A> : _2<_1<M, C>, B, A>


/**
 * Represents a higher kinded type of arity 4.
 * @param [M] - a 'witness' (cited at highj) type of the type to be lifted as a type constructor.
 * @param [D] - the 1st parameter of the type constructor.
 * @param [C] - the 2nd parameter of the type constructor.
 * @param [B] - the 3rd parameter of the type constructor.
 * @param [A] - the 4th parameter of the type constructor.
 */
interface _4<M, D, C, B, A> : _3<_1<M, D>, C, B, A>

object Cast {

    fun <M, B, A> _1<_1<M, B>, A>.cast(): _2<M, B, A> = this as _2<M, B, A>

    fun <M, C, B, A> _1<_1<_1<M, C>, B>, A>.cast(): _3<M, C, B, A> = this as _3<M, C, B, A>

    fun <M, D, C, B, A> _1<_1<_1<_1<M, D>, C>, B>, A>.cast(): _4<M, D, C, B, A> = this as _4<M, D, C, B, A>
}
