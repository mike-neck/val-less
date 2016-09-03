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

import valless.type._0
import valless.type.data.Eq.Companion.fromEquals
import valless.util.flow.If

enum class Bool(val raw: Boolean) : _0<Bool.Companion> {
    False(false) {
        override fun unaryMinus(): Bool = True
    },
    True(true) {
        override fun unaryMinus(): Bool = False
    };

    abstract operator fun unaryMinus(): Bool

    companion object {

        val eqInstance: Eq<Bool> = fromEquals()

        val ordInstance: Ord<Bool> = object : Ord<Bool> {
            override fun compare(x: Bool, y: Bool): Ordering =
                    If(x == True && y == True) { Ordering.EQ }
                            .elIf(x == True && y == False) { Ordering.GT }
                            .elIf(x == False && y == True) { Ordering.LT }
                            .els { Ordering.EQ }
        }
    }
}
