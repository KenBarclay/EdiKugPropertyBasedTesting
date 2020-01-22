package edikug

import com.adt.kotlin.kwikcheck.generator.Gen
import com.adt.kotlin.kwikcheck.generator.GenF
import com.adt.kotlin.kwikcheck.property.*
import com.adt.kotlin.kwikcheck.property.Property
import com.adt.kotlin.kwikcheck.property.PropertyF.forAll
import com.adt.kotlin.kwikcheck.property.PropertyF.prop
import com.adt.kotlin.kwikcheck.checkresult.CheckResult

import com.adt.kotlin.data.immutable.list.List
import com.adt.kotlin.data.immutable.list.List.Nil
import com.adt.kotlin.data.immutable.list.List.Cons
import com.adt.kotlin.data.immutable.list.ListF

import com.adt.kotlin.data.immutable.option.*

import com.adt.kotlin.kwikcheck.shrink.Shrink
import com.adt.kotlin.kwikcheck.shrink.ShrinkF

import kotlin.text.*

import kotlin.test.*
import org.junit.Test



sealed class Stack<out A> {
    fun top(): A = when (this) {
        is Empty -> throw Exception("top: empty stack")
        is Push -> el
    }
    object Empty: Stack<Nothing>() {
        override fun toString(): String = "Empty"
    }
    class Push<out A>(val el: A, val st: Stack<A>): Stack<A>() {
        override fun toString(): String = "Push($el, $st)"
    }
}   // Stack

fun <A> Stack<A>.push(el: A): Stack<A> = Stack.Push(el, this)

fun <A> genStack(genA: Gen<A>): Gen<Stack<A>> {
    val genEmpty: Gen<Stack<A>> = GenF.value(Stack.Empty)
    val genPush: Gen<Stack<A>> = genA.bind{a: A ->
        genStack(genA).bind{st: Stack<A> ->
            val push: Gen<Stack<A>> = GenF.value(Stack.Push(a, st))
            push
        }
    }

    return GenF.oneOf(ListF.of(genEmpty, genPush))
}   // genStack






































class PropertyBasedTesting {

    /**
     * Edinburgh Kotlin User Group
     *
     * Property Based Testing in Kotlin
     *   A tale of two libraries
     *
     * Ken Barclay
     *
     */

































    /**
     * Long term interest in the introduction of functional programming
     *   features into object oriented programming. Particular interest
     *   in how the functional community did testing. Initial experimentation
     *   with a dynamic language but ultimately fell foul of the lack
     *   of a type system.
     *
     * Experimented with some statically-typed languages before discovering
     *   and adopting Kotlin. An OO language with functions as first class
     *   citizens. Unlike its contemporaries it came with a working IDE plug-in.
     */


































    /**
     * In traditional unit testing, you run your code with specific
     *   inputs and check that you get the expected outputs. This
     *   means you need to anticipate possible problematic inputs
     *   in advance.
     *
     *   sort([4, 2, 7, 3, 1]) == [1, 2, 3, 4, 7]
     *   sort([1, 2, 3, 4, 7]) == [1, 2, 3, 4, 7]
     *   sort([4, 2, 7, 2, 1]) == [1, 2, 2, 4, 7]
     *   sort([]) == []
     */


    /**
     * Property-based testing is a technique where your tests describe
     *   the properties and behaviour you expect your code to have, and
     *   the testing framework tries to find inputs that violate those
     *   expectations.
     *
     * Test properties are presented as logical propositions. The stack
     *   data type is our standard last-in-first-out data store. Operation
     *   push injects a new element on to the stack top, while operation
     *   top delivers the item at the stack top. For all stack st and
     *   all element el the following proposition is a statement that
     *   affirms or denies the predicate:
     *
     *     top(push(el, st)) == el
     */

    @Test fun stackOperation() {
        val prop: Property = forAll(genStack(GenF.genInt), GenF.genInt){st, el ->
            prop(st.push(el).top() == el)
        }
        val checkResult: CheckResult = prop.check()
        //assertTrue(checkResult.isPassed())
        println(checkResult.summary())
    }








    /**
     * Property-based testing was popularised by the Haskell library
     *   QuickCheck. You specify properties of your code that should
     *   hold regardless of input. QuickCheck then randomly generates
     *   the input data to try to find a case for which the property
     *   fails. This form of testing became known as property based
     *   testing and is actually the principal form of testing in the
     *   Haskell ecosystem.
     */


    /**
     * The KwikCheck test framework for Kotlin is modelled after the
     *   QuickCheck framework. Unsurprisingly QuickCheck exploits the
     *   machinery of functional programming including function composition
     *   and curried functions; immutable and persistent data structures;
     *   as well as many higher order abstractions.
     */
































    /**
     * To mirror QuickCheck in Kotlin I first needed a library of
     *   immutable data types. I also needed the basic functional
     *   programming devices such as currying.
     *
     * The library KDATA is where I learned Kotlin. It is a library
     *   of immutable and persistent data types such as Option, Either,
     *   List, Set and Map. The library is still being expanded and has
     *   had at least one major revision. Sealed classes introduced in
     *   Kotlin were a perfect fit and retro-fitted to greatly enhance
     *   the library.
     */




























    /**
     * Sealed classes are used extensively throughout KDATA. For example:
     *
     *   sealed class Option<out A> {
     *     object None : Option<Nothing>()
     *     class Some<out A>(val value: A) : Option<A>()
     *   }
     *
     * and:
     *
     *   sealed class List<out A> {
     *       object Nil : List<Nothing>()
     *       class Cons<out A>(val hd: A, val tl: List<A>) : List<A>()
     *   }
     *
     * The key benefit of sealed classes comes into play when used in a
     *   when expression. The compiler verifies that all cases are covered
     *   and no else clause is necessary.
     *
     *   sealed class List<out A> {
     *       fun <B> map(f: (A) -> B): List<B> =      // OR polymorphic
     *           when (this) {
     *               is Nil -> Nil
     *               is Cons -> Cons(f(this.hd), this.tl.map(f))
     *           }
     *
     *       object Nil : List<Nothing>()
     *       class Cons<out A>(val hd: A, val tl: List<A>) : List<A>()
     *   }
     */

    @Test fun reverseOperation() {
        val genInt: Gen<Int> = GenF.choose(1, 10)
        val genList: Gen<List<Int>> = GenF.genList(6, genInt)

        val prop: Property = forAll(genList){list ->
            prop(list.reverse().reverse() == list)
        }
        val checkResult: CheckResult = prop.check()
        println(checkResult.summary())
    }




    /**
     * KDATA organisation:
     *
     * file: List.kt
     * -------------
     *
     * sealed class List<out A> {
     *     fun <B> map(f: (A) -> B): List<B> = ...
     *     ...
     * }
     *
     * file: ListE.kt
     * --------------
     *
     * fun <A> List<A>.contains(a: A): Boolean = ...
     * ...
     *
     * file: ListF.kt
     * --------------
     *
     * object ListF {
     *     fun range(from: Int, to: Int, step: Int = 1): List<Int> = ...
     *     fun <A> replicate(n: Int, t: A): List<A> = ...
     *     ...
     * }
     *
     */


















    /**
     * Building this library and the exposure to functional code has
     *   changed how I write functions. In the past my Java methods
     *   may have looked like:
     *
     *   fun f(a: ..., b: ..., ...): T {
     *       var x = ...
     *       var y = ...
     *       while (...) {
     *           x = ...
     *           if (...) {
     *               ...
     *               y = ...
     *           }
     *       }
     *       ...
     *       return ...
     *   }
     *
     * Under the influence of functional programming it now looks like:
     *
     *   fun f(a: ..., b: ..., ...): T {
     *       val x = ...
     *       val y = ...
     *       ...
     *       return ...
     *   }
     *
     * or ...
     *
     *   fun f(a: ..., b: ..., ...): T {
     *       fun g(p: ..., ...): S {
     *           ...
     *       }
     *
     *       val x = ...
     *       val y = ...
     *       ...
     *       return ...
     *   }
     */




    /**
     * One of the challenges of traditional unit testing is that it
     *   represents example based testing. Each unit test essentially
     *   establishes specific input data points, runs the code under
     *   test, and then checks the output. Writing many of these tests
     *   does not mean we will account for all possible behaviours.
     *   As the complexity of our software grows so does the probability
     *   that we will fail to account for a key scenario.
     */


    /**
     * Property based testing, by contrast, presents a high level
     *   description of how some function should behave. Rather than
     *   testing a function against specific inputs we try to reason
     *   about its behaviour over a range of inputs.
     */


    /**
     * Property-based testing is generative testing. You do not supply
     *   specific example inputs with expected outputs as with unit tests.
     *   Instead, you define properties about the code and use a generative-
     *   testing engine to create randomised inputs to ensure the defined
     *   properties are correct.
     */



















    /**
     * Automated test generation means, if you've got a function that
     *   operates on a particular input, you specify a property (or
     *   several properties) about that function, and then generators
     *   will give you variations on the input data to test the function,
     *   the edge cases, etc., and tell you whether the property holds.
     *   You can use default generators for known types, or define your own.
     *
     * Test properties are presented as logical propositions. Examples
     *   include:
     *
     *   !P || !Q == !(P && Q)
     *   (S1 + S2).endsWith(S2)
     *   L.reverse().reverse() == L
     *   L1.reverse().zip(L2.reverse()) == L1.zip(L2).reverse()     // UNTRUE
     */

    @Test fun endsWithOperation() {
        val genStr1: Gen<String> = GenF.genString
        val genStr2: Gen<String> = GenF.genString

        val prop: Property = forAll(genStr1, genStr2){str1, str2 ->
            prop((str1 + str2).endsWith(str2))
        }
        val checkResult: CheckResult = prop.check()
        println(checkResult.summary())
    }



    @Test fun reverseZipOperation() {
        val genInt: Gen<Int> = GenF.choose(1, 10)
        val genIntList: Gen<List<Int>> = GenF.genList(genInt)
        val genAlpha: Gen<Char> = GenF.genAlphaUpperChar
        val genAlphaList: Gen<List<Char>> = GenF.genList(genAlpha)

        val prop: Property = forAll(genIntList, genAlphaList){xs, ys ->
            prop(xs.reverse().zip(ys.reverse()) == xs.zip(ys).reverse())
        }
        val checkResult: CheckResult = prop.check()
        println(checkResult.summary())
    }



    /**
     * KwikCheck is a property based testing framework written in Kotlin.
     *   The core of the library comprises the classes Gen<A> and Property.
     *   The class Gen<A> is a generator class for values of type A.
     *
     * class Gen<A>(val ...) {
     *     fun <B> map(f: (A) -> B): Gen<B> = ...
     *     ...
     * }
     *
     * object GenF
     *     val genInt: Gen<Int> = ...
     *     val genString: Gen<String> = ...
     *     val genAlphaNumStr: Gen<String> = ...
     *     fun <A> genList(genA: Gen<A>): Gen<List<A>> = ...
     *     ...
     * }
     */

    @Test fun samplesOperation() {
        println(GenF.genInt.samples())
        println(GenF.genAlphaNumStr.samples())
    }


    @Test fun traceOperation() {
        val prop: Property = forAll(GenF.genList(GenF.genInt)){list ->
            println(list)
            prop(true)
        }
        val checkResult: CheckResult = prop.check()
        println(checkResult.summary())
    }












    /**
     *
     * The class Property represents an algebraic property that mey be
     *   checked for its truth. Its most important function is check
     *   which delivers a result. The companion function forAll returns
     *   a Property derived from a universal quantification across its
     *   generator parameters.
     */


    /**
     * Interestingly both the Gen and Property classes are immutable and
     *   have a single function for their representation. Most member
     *   functions for these classes return a new instance with a
     *   composed function for its representation.
     */

    /**
     * class Gen<A>(val func: ... -> A) {
     *   fun apply(...): A = ...
     *
     *   fun <B> map(f: (A) -> B): Gen<B> =
     *     Gen{... -> f(this.apply(...))}
     * }
     *
     * The implementation functions do not execute until called by check
     *   in Property or apply in Gen. Both classes use function representations
     *   to be LAZY.
     */
















    /**
     * With KwikCheck you specify properties of your code that should
     *   hold regardless of input. KwikCheck then randomly generates
     *   the input data to try to find a case for which the property
     *   fails.
     */

    @Test fun failingLargestOperation() {
        fun largest(list: List<Int>): Int = list.head()

        val prop: Property = forAll(GenF.genList(1, GenF.genInt)){list ->
            prop(largest(list) == list.sort{a, b -> a.compareTo(b)}.last())
        }
        val checkResult: CheckResult = prop.check()
        println(checkResult.summary())
    }

    @Test fun successfulLargestOperation() {
        fun largest(list: List<Int>): Int {
            fun biggest(list: List<Int>, big: Int): Int =
                when (list) {
                    is Nil -> big
                    is Cons -> if (list.head() > big)
                        biggest(list.tail(), list.head())
                    else
                        biggest(list.tail(), big)
                }   // biggest

            return biggest(list.tail(), list.head())
        }   // largest

        val prop: Property = forAll(GenF.genList(1, GenF.genInt)){list ->
            prop(largest(list) == list.sort{a, b -> a.compareTo(b)}.last())
        }
        val checkResult: CheckResult = prop.check()
        println(checkResult.summary())
    }








    /**
     * KwikCheck randomly generates the input data to try to find
     *   a case for which the property fails. When such a case is
     *   found, it tries to find a minimal test case by shrinking
     *   the input data. This minimal test case is then reported
     *   for easy inspection.
     */

    /**
     * When KwikCheck finds an input that violates a property, it will
     *   first try to find smaller inputs that also violate the property,
     *   in order to give the developer a clearer message about the nature
     *   of the failure.
     *
     *   NB: removeDuplicates removes duplicates from a list retaining
     *   the first occurrence
     */

    @Test fun failingRemoveDuplicatesOperation() {
        val genIntList: Gen<List<Int>> = GenF.genList(GenF.genInt)
        val prop: Property = forAll(genIntList){list ->
            prop(list.removeDuplicates() == list)
        }
        val checkResult: CheckResult = prop.check()
        println(checkResult.summary())
    }

    /**
     * While large inputs may produce the errors, KwikCheck will attempt
     *   to shrink the input sequence to the smallest possible that will
     *   reproduce the error. The smaller the input, the easier it is to
     *   reproduce and fix.
     */

    @Test fun shrinkingRemoveDuplicatesOperation() {
        val genIntList: Gen<List<Int>> = GenF.genList(GenF.genInt)
        val shrinkIntList: Shrink<List<Int>> = ShrinkF.shrinkList(ShrinkF.shrinkInt)
        val prop: Property = forAll(genIntList, shrinkIntList){list ->
            prop(list.removeDuplicates() == list)
        }
        val checkResult: CheckResult = prop.check()
        println(checkResult.summary())
    }


    /**
     * KwikCheck supports creating custom generators.
     */

    @Test fun domainModelGeneratorOperation() {
        class Account(val number: String, val balance: Int) {

            fun credit(amount: Int): Account = Account(number, balance + amount)

            fun debit(amount: Int): Account = Account(number, if (balance >= amount) balance - amount else balance)

        }

        val genAccount: Gen<Account> = GenF.genAlphaNumStr.bind{number ->
            GenF.genPosInt.bind{balance ->
                GenF.value(Account(number, balance))
            }
        }
        val prop: Property = forAll(genAccount, GenF.genPosInt){account, amount ->
            prop(account.debit(amount).balance >= 0)
        }
        val checkResult: CheckResult = prop.check()
        println(checkResult.summary())
    }





















    /**
     * A list is sorted when its first element is smaller than its
     *   second element, when its second elements is smaller than its
     *   third element, ...
     *
     * Note how this function and the function passed to forAll
     *   follow the coding scheme cited above.
     */

    @Test fun sortOperation() {
        val genStr: Gen<String> = GenF.genAlphaStr
        val genStrList: Gen<List<String>> = GenF.genList(1, genStr)     // NOTE 1
        val prop: Property = forAll(genStrList){list ->
            val sorted: List<String> = list.sort{str1, str2 -> str1.compareTo(str2)}    // ASCENDING
            val pairs: List<Pair<String, String>> = sorted.zip(sorted.tail())
            prop(pairs.forAll{pr -> (pr.first <= pr.second)})
        }
        val checkResult: CheckResult = prop.check()
        println(checkResult.summary())
    }

























    /**
     * Many real world Java code is stateful. Currently experimenting with
     *   extensions to the library to test stateful systems. This involves
     *   generating sequences of calls to the API similar to the test cases
     *   prepared by developers.
     */


    /**
     * Now developing property baed tests for KDATA. A by-product of writing
     *   these property tests is an improvement in the clarity of the documentation
     *   many functions.
     */

}   // PropertyBasedTesting
