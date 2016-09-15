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
package valless.type.controll

import org.junit.Test
import valless.type.control.Category
import valless.type.data.Enum
import valless.type.data.Eq
import valless.type.data.IntInstance
import valless.type.data.function.Fun1
import valless.type.data.function.fun1
import valless.type.test
import valless.util.both
import valless.util.function.`$`
import java.util.*

class CategoryTest {

    val cat: Category<Fun1> = Category.fun1

    @Test fun identity() =
            (cat.plus(cat.id(), String::length.fun1) to cat.plus(String::length.fun1, cat.id())) `$`
                    { (it.first.fun1)("CategoryTest") to (it.second.fun1)("CategoryTest") } `$`
                    IntInstance.eq.test { it.first shouldEqualTo it.second }

    val de: Direction.Companion = Direction.Companion
    val ie: Eq<Int> = IntInstance.eq

    @Test fun composition() =
            (cat.exec { (de.asString.fun1 then de.fromString.fun1) then de.fromEnum.fun1 } to
                    cat.exec { de.asString.fun1 then (de.fromString.fun1 then de.fromEnum.fun1) })
                    .both { it.fun1 } `$`
                    { p -> Direction.random() `$` { p.first(it) to p.second(it) } } `$`
                    ie.test { it.first shouldEqualTo it.second }

    enum class Direction {
        NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST;

        companion object : Enum<Direction> {
            fun random(): Direction = values() `$` { it[Random().nextInt(it.size)] }
            override fun toEnum(i: Int): Direction =
                    Direction.values().find { it.ordinal == i } ?: throw NoSuchElementException()

            override fun fromEnum(e: Direction): Int = e.ordinal
            val fromString: (String) -> Direction = { Direction.valueOf(it) }
            val asString: (Direction) -> String = Direction::toString
        }
    }
}
