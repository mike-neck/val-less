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
package util.flow

import org.junit.Test
import util.shouldBe
import util.unit

class CloseAfterRunningTest {

    val getText: (Resource) -> String = Resource::getText

    val getMessage: (Exception) -> String = { (it as TestExceptions).msg }

    @Test fun normalCase() =
            Resource.NormalCase()
                    .run(getText)
                    .handle(getMessage) shouldBe "normal case"

    @Test fun errorOnClosing() =
            Resource.ErrorOnClosing()
                    .run(getText)
                    .handle(getMessage) shouldBe "exception : on closing"

    @Test fun errorOnProcessing() =
            Resource.ErrorOnProcessing()
                    .run(getText)
                    .handle(getMessage) shouldBe "exception : on processing"

    @Test fun bothCase() =
            Resource.BothCase()
                    .run(getText)
                    .handle(getMessage) shouldBe "exception : on processing"
}

sealed class Resource : AutoCloseable {

    abstract fun getText(): String

    class NormalCase : Resource() {
        override fun getText(): String = "normal case"
        override fun close() = unit
    }

    class ErrorOnClosing : Resource() {
        override fun getText(): String = "success : on closing"
        override fun close() = throw TestExceptions.OnClosing("exception : on closing")
    }

    class ErrorOnProcessing : Resource() {
        override fun getText(): String = throw TestExceptions.OnProcessing("exception : on processing")
        override fun close() = unit
    }

    class BothCase : Resource() {
        override fun getText(): String = throw TestExceptions.OnProcessing("exception : on processing")
        override fun close() = throw TestExceptions.OnClosing("exception : on closing")
    }
}

sealed class TestExceptions(val msg: String) : RuntimeException() {
    class OnClosing(msg: String) : TestExceptions(msg)
    class OnProcessing(msg: String) : TestExceptions(msg)
}
