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

fun main(args: Array<String>) {
    
}

object Curry {

    fun funType(num: Int): Func = when(num) {
        -1   -> Func(Type.UnitReturn())
        0    -> Func()
        1    -> Func(Type.Single())
        else -> Func(num)
    }
}

interface TypeParam {
    val type: String
    val parameter: String get() = type.toLowerCase()
}

sealed class Type: TypeParam {
    abstract class Arg: Type()
    abstract class Return: Type()

    class Normal: Return() { override val type: String get() = "R" }

    class Single: Arg() { override val type: String get() = "P" }
    class Argument(val num: Int): Arg() { override val type: String get() = "P$num" }
    class UnitParam: Arg() {
        override val type: String get() = "()"
        override val parameter: String get() = ""
    }

    class UnitReturn: Return() {
        override val type: String get() = "Unit"
        override val parameter: String get() = ""
    }
}

class Func(val arguments: List<Type.Arg>, val returnType: Type.Return = Type.Normal()) {

    constructor(r: Type.Return): this(listOf(Type.UnitParam()), r)
    constructor(): this(Type.UnitParam())
    constructor(arg: Type.Arg): this(listOf(arg))
    constructor(num: Int): this((1..num).map { Type.Argument(it) })

    fun type(): String = "${param()} -> ${returnType.type}"

    fun param(): String = arguments.map(Type.Arg::type).joinToString(", ", "(", ")")
}
