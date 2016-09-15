import java.io.File

class Fun(val num: Int) {
    val name: String = "Fun$num"
    val obj: String = "object $name"
    val params: List<String> = if (num == 0) emptyList() else (1..num).map { "P$it" }
    val typeHeader: List<String> = (0..num).map { "_1<" }
    val typeFooter: String = "R>"
    val comma: String = if (num == 0) "" else ", "
    val sep: String = if (num == 0) "" else ">"
    val generics: String = params.joinToString(", ", "<", "$comma$typeFooter")
    val type: String = params.joinToString(">, ", "${typeHeader.joinToString("")}$name, ", "$sep$comma$typeFooter")
    val function: String = params.joinToString(") -> (", "(", ") -> R")
    val suppress: String = """@Suppress("UNCHECKED_CAST")"""

    fun getContents(): String = """$obj

private class F$num$generics(val f: $function): $type
val $generics $type.fun$num: $function get() = (this as F$num$generics).f
val $generics ($function).fun$num: $type get() = F$num(this)
"""
}

listOf(
        "package valless.type.data.function",
        "import valless.type._1",
        (0..22).map(::Fun).map(Fun::getContents).joinToString("\n")
).joinToString("\n\n")
        .let {
            it to File("src/main/kotlin/valless/type/data/function/functions.kt")
        }
        .let {
            it.second.writeText(it.first)
        }
