package zircle

import org.junit.Test
import kotlin.test.assertEquals


internal abstract class  JwA {
    internal abstract fun callMe()
    fun callMeToo() {
        println("This is a concrete method.")
    }
}

internal abstract class JwShape {
    var color: String = ""
    abstract fun area(): Double
}

internal  class  JwTest

internal class JwShort(s: String) {
    companion object {
        fun valueOf(value: String): Int {
            return java.lang.Integer.valueOf(value)
        }
    }
}

internal  class JwT {
    fun main() {}
    fun i(): Int = 0
    fun s(): String = ""
}

internal  class JwF {
    var a = "abc"
    var b = 10
}

internal open class JwS1()
internal class JwS2 : JwS1()

internal interface JwI1 {
    abstract fun a()
}
internal interface JwI2 {
    abstract  fun b(): String
}

internal class Jw3: JwI1, JwI2 {
    override fun a() = Unit
    override fun b(): String = ""
}

internal class  JwFinalClass

internal  class  JwGenericClass<K, V>

internal  interface  JwInInterface {
    class X {
        val value = 10
    }
}

internal class JwOutter {
    interface  innner class  JsInner
}

internal class JwOutterA {
    internal enum class JwInnerEnum {
        A, B, C
    }
}

internal  interface  JwOuterInterface {
    interface I
}

internal class  JwOuterStaticClass {
    internal class  Inner
}

internal  class  JsConstructorOverload {
    constructor() { }
    constructor(s: String) {}
}

internal open class  JwNotUtlityClass {
    companion object {
        val CONSTANT= 10
    }
}

internal class JwDerived: JwNotUtlityClass() {
    fun foo() = CONSTANT
}


internal class JwOneStaticOneNot {
    fun nonStatic(): Boolean = false
    companion object {
        var static = 10
    }
}

internal  object JwOneStatic {
    fun static(): Boolean = false
}

private class JwPrivateClass

internal class JwPrivateMethod {
    private fun init() {}
}

//protected class Test() {}

class JwPublicClass

open class  JwBase
class  JwChild: JwBase()

internal object JwTwoStaticMethod {
    fun m1() = true
    fun m2() = false
}

class JwShortTest {

    @Test fun test() {
        var a1 = JwShort.valueOf("1")
        var a2 = JwShort.valueOf("1")

        assertEquals(a1, a2)
    }

    @Test fun field() {
        var f = JwF()
        f.a = "ss"
        assertEquals(f.a, "ss")
    }

    @Test fun utility() {
        var rs = JwDerived().foo()
        assertEquals(10, rs)
    }

    @Test fun static() {
        var rs = JwOneStaticOneNot.static
        assertEquals(10, rs)

        var rs2 = JwOneStaticOneNot().nonStatic()
        assertEquals(false, rs2)
    }

    @Test fun oneStatic() {
        var rs = JwOneStatic.static()
        assertEquals(false, rs)
    }

    @Test fun privateClass() {
        var rs = JwPrivateClass()
        //JwPrivateMethod().init()
    }
}

