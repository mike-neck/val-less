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
package util

import org.junit.Test

interface TpCls {
    interface Level0<C> {
        fun offer(reg: Tp.Reg0<C>): Tp.Reg0<C>
    }
    interface Level1<C> {
        fun <C2, T: Tp.Reg0<C2>> offer(reg: Tp.Reg1<C, T>, cv: TpCls.Level0<C2>): Tp.Reg1<C, T>
    }
}

object Tp {
    interface Reg0<C>
    interface Reg1<C, T>
}

object Ref

data class Refer(val v: Int): Tp.Reg0<Ref>

object TpcRef: TpCls.Level0<Ref> {
    override fun offer(reg: Tp.Reg0<Ref>): Tp.Reg0<Ref> = reg
}

object Ch

data class Check<T>(val v: T): Tp.Reg1<Ch, T>

object TpcCheck: TpCls.Level1<Ch> {
    @Suppress("UNCHECKED_CAST")
    override fun <C2, T : Tp.Reg0<C2>> offer(reg: Tp.Reg1<Ch, T>, cv: TpCls.Level0<C2>): Tp.Reg1<Ch, T> =
            if (reg is Check<T>) cv.offer(reg.v).let { Check(it) as Tp.Reg1<Ch, T> }
            else throw IllegalArgumentException("")
}

class CheckTest {

    @Test fun check() =
            TpcCheck.offer(Check(Refer(100)), TpcRef)
                    .unit(::println)
}
