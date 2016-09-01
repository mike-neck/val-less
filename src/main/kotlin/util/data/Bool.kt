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
package util.data

import util.type.Type

object BoolType {
    
}

enum class Bool(val primitive: Boolean): Type.Kind0<BoolType> {
    True(true),
    False(false);

    infix fun and(y: Bool) = if (this == True && y == True) True else False

    infix fun or(y: Bool) = if (this == True || y == True) True else False

    companion object {

        @JvmStatic
        val TRUE: List<Pair<Bool, Bool>> = listOf(True to True, False to False)

        @JvmStatic
        fun eq(x: Bool, y: Bool): Bool = if (Pair(x, y) in TRUE) True else False

        @JvmStatic
        fun not(x: Bool) = when (x) {
            True  -> False
            False -> True
        }

        fun <T> bool(x: T, y: T, b: Bool): T = if (b == True) y else x

        fun <T> bool(): (T) -> ((T) -> (Bool) -> T) = { x -> { y -> { b -> bool(x, y, b) } } }
    }
}

object OrdBool : Ord.Type0<BoolType, OrdBool> {
    override fun Type.Kind0<BoolType>.comp(y: Type.Kind0<BoolType>): Ordering =
            if (this is Bool && y is Bool) when (this) {
                Bool.True  -> if (y == Bool.True) Ordering.EQ else Ordering.GT
                Bool.False -> if (y == Bool.True) Ordering.LT else Ordering.EQ
                else       -> throw IllegalStateException("Comparing Bool but they are not Bool.[x:$this, y:$y]")
            }
            else throw IllegalArgumentException("Cannot compare these values.[x:$this, y:$y]")

    override fun Type.Kind0<BoolType>.eqTo(y: Type.Kind0<BoolType>): Bool =
            if (this is Bool && y is Bool) Bool.eq(this, y)
            else Bool.False
}

object EnumBool: Enumerated.Type0<BoolType, EnumBool> {
    override fun toEnum(i: Int): Type.Kind0<BoolType> = when (i) {
        0    -> Bool.False
        1    -> Bool.True
        else -> throw IllegalArgumentException("Cannot convert $i to Bool.")
    }

    override fun fromEnum(e: Type.Kind0<BoolType>): Int = when (e) {
        Bool.False -> 0
        Bool.True  -> 1
        else       -> throw IllegalArgumentException("Cannot convert $e to Int.")
    }

    override fun succ(e: Type.Kind0<BoolType>): Type.Kind0<BoolType> = when (e) {
        Bool.False -> Bool.True
        else       -> throw IllegalArgumentException("Successor for $e is not found.")
    }

    override fun pred(e: Type.Kind0<BoolType>): Type.Kind0<BoolType> = when (e) {
        Bool.True -> Bool.False
        else      -> throw IllegalArgumentException("Predecessor for $e is not found.")
    }
}

object BoundedBool: Bounded.Type0<BoolType, BoundedBool> {

    override fun minBound(): Type.Kind0<BoolType> = Bool.False

    override fun maxBound(): Type.Kind0<BoolType> = Bool.True
}
