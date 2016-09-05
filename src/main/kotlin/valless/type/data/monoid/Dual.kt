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

import valless.type._0
import valless.type._1
import valless.type.data.Bool
import valless.type.data.Eq
import valless.type.data.Ord
import valless.type.data.Ordering
import valless.util.both
import valless.util.make
import kotlin.reflect.KClass

data class Dual<T>(val dual: T) : _1<Dual.Companion, T> {

    companion object {

        fun <T> toDual(): (T) -> Dual<T> = ::Dual

        inline fun <T : _0<O>, reified O> instance(kc: KClass<O> = O::class): Instance<T, O>
                where O : Eq._1_<T>, O : Ord._1_<T> = kc.objectInstance.make { Instance(it) } ?: throw IllegalStateException("No instance found.")

        fun <T> monoid(m: Monoid<T>): Monoid<_1<Companion, T>> = monoidInstance(m).monoidInstance

        fun <T> monoidInstance(m: Monoid<T>): Monoid._1_<_1<Companion, T>> =
                object : Monoid._1_<_1<Companion, T>> {
                    override val monoidInstance: Monoid<_1<Companion, T>>
                        get() = object : Monoid<_1<Companion, T>> {
                            override fun empty(): _1<Companion, T> = Dual(m.mempty)

                            override fun append(
                                    x: _1<Companion, T>,
                                    y: _1<Companion, T>
                            ): _1<Companion, T> =
                                    (x.narrow to y.narrow).both { it.dual }
                                            .let { m.append(it.first, it.second) }
                                            .let { Dual(it) }
                        }
                }
    }

    class Instance<T : _0<O>, O>(val obj: O) :
            Eq._1_<Dual<T>>
            , Ord._1_<Dual<T>>
    where O : Eq._1_<T>
    , O : Ord._1_<T> {

        override val eqInstance: Eq<Dual<T>> get() = object : Eq<Dual<T>> {
            override fun eq(x: Dual<T>, y: Dual<T>): Bool = obj.eqInstance.eq(x.dual, y.dual)
        }

        override val ordInstance: Ord<Dual<T>> get() = object : Ord<Dual<T>> {
            override fun compare(x: Dual<T>, y: Dual<T>): Ordering = obj.ordInstance.compare(x.dual, y.dual)
        }
    }
}

val <T> _1<Dual.Companion, T>.narrow: Dual<T> get() = this as Dual<T>
