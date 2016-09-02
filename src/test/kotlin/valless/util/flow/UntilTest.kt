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
package valless.util.flow

import org.junit.Test
import valless.util.shouldBe

class UntilTest {

    @Test fun runsUpTo11() =
            1.doing { it + 1 }
                    .until { it > 10 } shouldBe 11

    @Test fun counterImpl() =
            run { 0 to 0 }
                    .doing { it.second to (it.second + 1) }
                    .until { it.first > 10 } shouldBe (11 to 12)
}
