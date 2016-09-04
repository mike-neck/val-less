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

data class Any(val any: Bool) : Comparable<Any>, _0<AnyInstances> {
    constructor(any: Boolean) : this(Eq.booleanToBool(any))

    override fun compareTo(other: Any): Int = this.any.compareTo(other.any)
}

object AnyInstances :
        Eq.Instance<Any>
        , Ord.Instance<Any>
        , Monoid.Instance<Any> {
    override val eqInstance: Eq<Any> get() = Eq.fromEquals()

    override val ordInstance: Ord<Any> get() = Ord.fromComparable<Any>()

    override val monoidInstance: Monoid<Any> get() = object : Monoid<Any> {
        override fun mempty(): Any = Any(Bool.False)

        override fun append(x: Any, y: Any): Any = If(x.any == Bool.False) { y }.els { x }
    }
}

data class All(val all: Bool) : Comparable<All>, _0<AllInstances> {

    constructor(all: Boolean) : this(Eq.booleanToBool(all))

    override fun compareTo(other: All): Int = this.all.compareTo(other.all)
}

object AllInstances :
        Eq.Instance<All>
        , Ord.Instance<All>
        , Monoid.Instance<All> {

    override val monoidInstance: Monoid<All> get() = object : Monoid<All> {
        override fun mempty(): All = All(Bool.True)

        override fun append(x: All, y: All): All =
                If(y.all == Bool.False) { y }.els { x }

    }

    override val eqInstance: Eq<All> get() = Eq.fromEquals()

    override val ordInstance: Ord<All> get() = Ord.fromComparable()
}
