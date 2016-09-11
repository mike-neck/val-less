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
package valless.type.data.functor

import valless.type._1
import valless.type._3
import valless.type.data.Either
import valless.type.data.Maybe

/**
 * Containered container type.
 *
 * The kind of [F] is 2 or more.
 * The kind of [G] is 2 or more.
 * The kind of [T] is 1.
 *
 * Example 1: Simple example.
 * <code><pre>
 *     val a: Maybe&lt;Identity&lt;Int&gt;&gt; = ...
 *     val compose: Compose&lt;Maybe.Companion, Identity.Companion, Int&gt; = Companion(a)
 * </pre></code>
 *
 * Example 2: Complex example.
 * <code><pre>
 *     val a: Maybe&lt;Either&lt;String, Int&gt;&gt; = ...
 *     // Either&lt;String, Int&gt; = _2&lt;Either.Companion, String, Int&gt;
 *     val b: Maybe&lt;_2&lt;Either.Companion, String, Int&gt;&gt; = a
 *     // _2&lt;Either.Companion, String, Int&gt; = _1&lt;_1&lt;Either.Companion, String&gt;, Int&gt;
 *     val c: Maybe&lt;_1&lt;_1&lt;Either.Companion, String&gt;, Int&gt;&gt; = b
 *     val com: Compose&lt;Maybe.Companion, _1&lt;Either.Companion, String&gt;, Int&gt;
 * </pre></code>
 */
class Compose<F, G, T>(val compose: _1<F, _1<G, T>>) : _3<Compose.Companion, F, G, T> {

    companion object
}

fun main(args: Array<String>) {
    val right: _1<_1<Either.Companion, String>, Int> = Either.Right<String, Int>(10)
    val just: _1<Maybe.Companion, _1<_1<Either.Companion, String>, Int>> = Maybe.Just(right)
    val compose: Compose<Maybe.Companion, _1<Either.Companion, String>, Int> = Compose(just)
}
