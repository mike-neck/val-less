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
import valless.util.flow.When

enum class Bool(val raw: Boolean) : _0<Bool.Companion> {
    False(false) {
        override fun unaryMinus(): Bool = True
    },
    True(true) {
        override fun unaryMinus(): Bool = False
    };

    abstract operator fun unaryMinus(): Bool

    companion object : Eq._1_<Bool>, Ord._1_<Bool>, Enum._1_<Bool> {

        override val eqInstance: Eq<Bool> = fromEquals()

        override val ordInstance: Ord<Bool> = object : Ord<Bool> {
            override fun compare(x: Bool, y: Bool): Ordering =
                    If(x == True && y == True) { Ordering.EQ }
                            .elIf(x == True && y == False) { Ordering.GT }
                            .elIf(x == False && y == True) { Ordering.LT }
                            .els { Ordering.EQ }

            override val asEq: Eq<Bool> get() = eqInstance
        }

        override val enumInstance: Enum<Bool> = object : Enum<Bool> {
            override fun toEnum(i: Int): Bool =
                    When<Int, Bool>(i)
                            .case { it == 0 }.then { False }
                            .case { it == 1 }.then { True }
                            .els { throw IllegalArgumentException("Illegal Argument for Enum.Bool.fromInt") }

            override fun fromEnum(e: Bool): Int =
                    When<Bool, Int>(e)
                            .case { it == False }.then { 0 }
                            .els { 1 }

            override fun succ(e: Bool): Bool =
                    When<Bool, Bool>(e)
                            .case { it == False }.then { True }
                            .els { throw IllegalArgumentException("Illegal Argument for Enum.Bool.succ") }

            override fun pred(e: Bool): Bool =
                    When<Bool, Bool>(e)
                            .case { it == True }.then { False }
                            .els { throw IllegalArgumentException("Illegal Argument for Enum.Bool.pred") }
        }
    }
}
