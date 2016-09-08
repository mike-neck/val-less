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
import valless.type.data.Eq
import valless.type.data.Ord

/**
 * [Monoid] under addition.
 */
data class Sum(val sum: Int) : _0<SumInstances>, Comparable<Sum> {
    override fun compareTo(other: Sum): Int = this.sum.compareTo(other.sum)
}

object SumInstances : Monoid._1_<Sum>
        , Eq._1_<Sum>
        , Ord._1_<Sum> {
    override val eq: Eq<Sum> get() = Eq.fromEquals()

    override val ordInstance: Ord<Sum> get() = Ord.fromComparable()

    override val monoid: Monoid<Sum>
        get() = Monoid.empty { Sum(0) }.append { x, y -> Sum(x.sum + y.sum) }
}

/**
 * [Monoid] under multiplication.
 */
data class Product(val product: Long) : _0<ProductInstance>, Comparable<Product> {
    override fun compareTo(other: Product): Int = this.product.compareTo(other.product)
}

object ProductInstance :
        Eq._1_<Product>
        , Ord._1_<Product>
        , Monoid._1_<Product> {

    override val eq: Eq<Product> get() = Eq.fromEquals()

    override val ordInstance: Ord<Product> get() = Ord.fromComparable()

    override val monoid: Monoid<Product>
        get() = Monoid.empty { Product(1) }.append { x, y -> Product(x.product * y.product) }
}
