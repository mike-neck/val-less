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
package valless.type.data.functor.classes

import valless.type._2
import valless.type.data.Bool
import valless.type.data.Either
import valless.type.data.Eq
import valless.type.data.narrow
import valless.util.function.uncurry

interface Eq2<F> {

    fun <P, Q> eq(p: Eq<P>, q: Eq<Q>, x: _2<F, P, Q>, y: _2<F, P, Q>): Bool =
            liftEq(x, y, p.eq.uncurry, q.eq.uncurry)

    fun <I, J, P, Q> liftEq(x: _2<F, I, P>, y: _2<F, J, Q>, f: (I, J) -> Bool, g: (P, Q) -> Bool): Bool

    companion object {

        val either: Eq2<Either.Companion> = object : Eq2<Either.Companion> {
            override fun <I, J, P, Q> liftEq(x: _2<Either.Companion, I, P>, y: _2<Either.Companion, J, Q>, f: (I, J) -> Bool, g: (P, Q) -> Bool): Bool =
                    eitherCompare(x.narrow, y.narrow)
                            .left_left(f)
                            .left_right { a, b -> Bool.False }
                            .right_left { a, b -> Bool.False }
                            .right_right(g)
        }
    }
}

