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
import valless.util.make
import kotlin.reflect.KClass

data class Dual<T : _0<O>, O>(val dual: T) : _1<Dual.Companion, T> {

    companion object {

        inline fun <T : _0<O>, reified O> instance(kc: KClass<O> = O::class): Instance<T, O>
                where O : Eq.Instance<T>, O : Ord.Instance<T> = kc.objectInstance.make { Instance(it) } ?: throw IllegalStateException("No instance found.")
    }

    class Instance<T : _0<O>, O>(val obj: O) :
            Eq.Instance<Dual<T, O>>
            , Ord.Instance<Dual<T, O>>
    where O : Eq.Instance<T>
    , O : Ord.Instance<T> {

        override val eqInstance: Eq<Dual<T, O>> get() = object : Eq<Dual<T, O>> {
            override fun eq(x: Dual<T, O>, y: Dual<T, O>): Bool = obj.eqInstance.eq(x.dual, y.dual)
        }

        override val ordInstance: Ord<Dual<T, O>> get() = object : Ord<Dual<T, O>> {
            override fun compare(x: Dual<T, O>, y: Dual<T, O>): Ordering = obj.ordInstance.compare(x.dual, y.dual)
        }
    }
}
