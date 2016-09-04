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
package valless.type.data.monoid

import valless.type._1
import valless.util.function.id
import valless.util.function.plus

class Endo<T>(val appEndo: (T) -> T) : _1<Endo.Companion, T> {

    companion object : Monoid._2_<Companion> {

        fun <T> monoid(): Monoid<_1<Companion, T>> = monoidInstance<T>().monoidInstance

        val <T> _1<Companion, T>.`%`: Endo<T> get() = this as Endo<T>

        override fun <T> monoidInstance(): Monoid._1_<_1<Companion, T>> =
                object : Monoid._1_<_1<Companion, T>> {
                    override val monoidInstance: Monoid<_1<Companion, T>>
                        get() = object : Monoid<_1<Companion, T>> {
                            override fun empty(): _1<Companion, T> = Endo(id())

                            override fun append(x: _1<Companion, T>, y: _1<Companion, T>): _1<Companion, T> =
                                    Endo(x.`%`.appEndo + y.`%`.appEndo)
                        }
                }
    }
}

val <T> _1<Endo.Companion, T>.narrow: Endo<T> get() = this as Endo<T>

@Suppress("UNCHECKED_CAST")
val <T> Monoid<_1<Endo.Companion, T>>.narrow: Monoid<Endo<T>> get() = this as Monoid<Endo<T>>
