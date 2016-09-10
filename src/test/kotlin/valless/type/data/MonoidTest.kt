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
package valless.type.data

import org.junit.Test
import valless.type.data.monoid.Monoid
import valless.util.function.flip

interface MonoidTest<T> {

    val mn: Monoid<T>

    val `mappend mempty x`: (T) -> T get() = mn.mappend(mn.mempty)

    @Test fun `mappend mempty x = x`()

    val `mappend x mempty`: (T) -> T get() = mn.mappend.flip()(mn.mempty)

    @Test fun `mappend x mempty = x`()

    val `mappend x {mappend y z}`: (T) -> (T) -> (T) -> T get() =
    { x -> { y -> { z -> mn.append(x, mn.append(y, z)) } } }

    val `__ mappend x {mappend y z}`: (Triple<T, T, T>) -> T get() =
    { t -> `mappend x {mappend y z}`(t.first)(t.second)(t.third) }

    val `mappend {mappend x y} z`: (T) -> (T) -> (T) -> T get() =
    { x -> { y -> { z -> mn.append(mn.append(x, y), z) } } }

    val `__ mappend {mappend x y} z`: (Triple<T, T, T>) -> T get() =
    { t -> `mappend {mappend x y} z`(t.first)(t.second)(t.third) }

    @Test fun `mappend x {mappend y z} = mappend {mappend x y} z`()
}
