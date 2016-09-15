import java.io.File

listOf(
        "package valless.type.data.function",
        (1..22).map {
            "object Fun$it"
        }.joinToString("\n\n")
).joinToString("\n\n")
        .let {
            it to File("src/main/kotlin/valless/type/data/function/functions.kt")
        }
        .let {
            it.second.writeText(it.first)
        }
