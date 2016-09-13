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
package valless.type.data.tuple

typealias `()` = Unit

data class Tp2<out A, out B>(val _1: A, val _2: B) {

    val swap: Tp2<B, A> get() = Tp2(_2, _1)

    val p: Pair<A, B> get() = Pair(_1, _2)

    infix operator fun <P, R> times(f: Tp2<(A) -> P, (B) -> R>): Tp2<P, R> = Tp2(f._1(this._1), f._2(this._2))

    infix operator fun <P> times(f: (B) -> P): Tp2<A, P> = Tp2(_1, f(_2))
}

val <A, B> Pair<A, B>.tp: Tp2<A, B> get() = Tp2(this.first, this.second)

data class Tp3<out A, out B, out C>(val _1: A, val _2: B, val _3: C)

val <A, B, C> Triple<A, B, C>.tp: Tp3<A, B, C> get() = Tp3(this.first, this.second, this.third)
