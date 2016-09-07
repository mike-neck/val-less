package valless.type.data

import valless.type._0
import valless.type.data.monoid.Monoid
import valless.util.flow.If
import valless.util.flow.When

enum class Ordering : _0<Ordering.Companion>, Comparable<Ordering> {
    LT,
    EQ,
    GT;

    companion object :
            Eq._1_<Ordering>
            , Ord._1_<Ordering>
            , Enum._1_<Ordering>
            , Monoid._1_<Ordering> {
        override val eqInstance: Eq<Ordering> = Ord.fromComparable<Ordering>().asEq

        override val ordInstance: Ord<Ordering> = Ord.fromComparable()

        override val enumInstance: Enum<Ordering> = InstanceEnumOrdering

        override val monoid: Monoid<Ordering> get() = object : Monoid<Ordering> {
            override fun empty(): Ordering = EQ

            override fun append(x: Ordering, y: Ordering): Ordering = If(x == EQ) { y }.els { x }

        }
    }
}

private object InstanceEnumOrdering : Enum<Ordering> {
    override fun toEnum(i: Int): Ordering =
            When<Int, Ordering>(i)
                    .case { it == 0 }.then { Ordering.LT }
                    .case { it == 1 }.then { Ordering.EQ }
                    .case { it == 2 }.then { Ordering.GT }
                    .els { throw IllegalArgumentException("Illegal Argument for Enum.Ordering.toEnum") }

    override fun fromEnum(e: Ordering): Int =
            When<Ordering, Int>(e)
                    .case { it == Ordering.LT }.then { 0 }
                    .case { it == Ordering.EQ }.then { 1 }
                    .case { it == Ordering.GT }.then { 2 }
                    .els { throw IllegalArgumentException("Illegal Argument for Enum.Ordering.fromEnum") }

    override fun succ(e: Ordering): Ordering =
            When<Ordering, Ordering>(e)
                    .case { it == Ordering.LT }.then { Ordering.EQ }
                    .case { it == Ordering.EQ }.then { Ordering.GT }
                    .els { throw IllegalArgumentException("Illegal Argument for Enum.Ordering.succ") }

    override fun pred(e: Ordering): Ordering =
            When<Ordering, Ordering>(e)
                    .case { it == Ordering.GT }.then { Ordering.EQ }
                    .case { it == Ordering.EQ }.then { Ordering.LT }
                    .els { throw IllegalArgumentException("Illegal Argument for Enum.Ordering.pred") }
}
