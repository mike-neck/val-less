import java.io.File

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

infix fun <T, R> T?.then(f: (T) -> R?): R? = if (this == null) null else f(this)
infix fun <T> T?.unit(f: (T) -> Unit): Unit {
    if (this != null) f(this)
}

infix operator fun <A, B, C> Pair<A, B>.plus(c: C): Triple<A, B, C> = Triple(this.first, this.second, c)

object Curry {

    fun funType(num: Int): Func = when (num) {
        0 -> Func()
        1 -> Func(Type.Single())
        else -> Func(num)
    }
}

interface TypeParam {
    val type: String
    val parameter: String get() = type.toLowerCase()
}

sealed class Type : TypeParam {
    abstract class Arg : Type() {
        val argument: String get() = type.toLowerCase()
    }

    abstract class Return : Type()

    val asIn: String get() = "in $type"

    val asOut: String get() = "out $type"

    class Normal : Return() {
        override val type: String get() = "R"
    }

    class Single : Arg() {
        override val type: String get() = "P"
    }

    class Argument(val num: Int) : Arg() {
        override val type: String get() = "P$num"
    }

    class UnitParam : Arg() {
        override val type: String get() = "()"
        override val parameter: String get() = ""
    }

    class UnitReturn : Return() {
        override val type: String get() = "Unit"
        override val parameter: String get() = ""
    }
}

class Func(val arg: List<Type.Arg>, val rtn: Type.Return = Type.Normal()) {

    constructor(r: Type.Return) : this(listOf(Type.UnitParam()), r)
    constructor() : this(Type.UnitParam())
    constructor(arg: Type.Arg) : this(listOf(arg))
    constructor(num: Int) : this((1..num).map { Type.Argument(it) })

    val type: String get() = "$param -> ${rtn.type}"
    fun generics(sign: Sign): String =
            (arg.map(Type.Arg::type) + listOf(rtn.type) + listOf(function(sign)))
                    .joinToString(", ", "<", ">")

    val param: String get() = arg.map(Type.Arg::type).joinToString(", ", "(", ")")
    fun function(s: Sign): String = "${s.sign}: $type"
    val curry: String get() = arg.map(Type.Arg::type).foldRight(rtn.type) { p, r -> "($p) -> ($r)" }
    fun applyTo(f: String) = arg.map(Type.Arg::argument).joinToString(", ", "$f(", ")")
    fun curryBody(f: String): String = arg.map(Type.Arg::argument).foldRight(applyTo(f)) { p, a -> "{ $p -> $a }" }

    enum class Sign(val sign: String) {
        _F("F"), _G("G"), _H("H"), _I("I"), _J("J"), _K("K")
    }
}

(1..22).map { Curry.funType(it) }
        .map { (it.generics(Func.Sign._F) to it.curry) + it.curryBody("this") }
        .map { "val ${it.first} F.curry: ${it.second} get() = ${it.third}" }
        .mapIndexed { i, s -> "/* Function${i + 1} curry */" to s }
        .map { "${it.first}\n${it.second}" }
        .let { File("src/main/kotlin/util/function/curry.kt") to it }
        .unit { it.first.writeText(it.second.joinToString("\n\n")) }
