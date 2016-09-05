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
import valless.type.data.Bool
import valless.type.data.Eq
import valless.type.data.Ord
import valless.util.flow.If

/**
 * Haskell's <code>Any</code>
 */
data class Or(val any: Bool) : Comparable<Or>, _0<OrInstances> {
    constructor(any: Boolean) : this(Eq.booleanToBool(any))

    override fun compareTo(other: Or): Int = this.any.compareTo(other.any)
}

object OrInstances :
        Eq._1_<Or>
        , Ord._1_<Or>
        , Monoid._1_<Or> {
    override val eqInstance: Eq<Or> get() = Eq.fromEquals()

    override val ordInstance: Ord<Or> get() = Ord.fromComparable<Or>()

    override val monoidInstance: Monoid<Or> get() = object : Monoid<Or> {
        override fun empty(): Or = Or(Bool.False)

        override fun append(x: Or, y: Or): Or = If(x.any == Bool.False) { y }.els { x }
    }
}

data class And(val all: Bool) : Comparable<And>, _0<AndInstances> {

    constructor(all: Boolean) : this(Eq.booleanToBool(all))

    override fun compareTo(other: And): Int = this.all.compareTo(other.all)
}

/**
 * Haskell's <code>All</code>
 */
object AndInstances :
        Eq._1_<And>
        , Ord._1_<And>
        , Monoid._1_<And> {

    override val monoidInstance: Monoid<And> get() = object : Monoid<And> {
        override fun empty(): And = And(Bool.True)

        override fun append(x: And, y: And): And =
                If(y.all == Bool.False) { y }.els { x }

    }

    override val eqInstance: Eq<And> get() = Eq.fromEquals()

    override val ordInstance: Ord<And> get() = Ord.fromComparable()
}
