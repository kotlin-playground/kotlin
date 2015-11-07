package zircle

import kotlin.test.assertEquals

internal class JwC1(val args1: Int) {
    var args2:Int = 0
    var args3:Int = 0

    constructor(args1: Int, args2:Int, args3: Int): this(args1) {
        this.args2 = args2
        this.args3 = args3
    }

    init {
        args2 = 2
        args3 = 3
    }
}

internal  class  JwC2(args1: Int, args2:Int = 0, args3:Int = 0) {

}

internal class JwC3 {
    private val s: String = "String"
    private var b: Boolean = false
    private var d: Double = 0.toDouble()

    constructor() {
        b = true
    }
}

internal class JwC4(args1: Int, args2: Int, args3:Int, args4:Int) {
    constructor(args1: Int, args2:Int): this(args1, args2, 1, 1) {
    }

    //constructor(args1: Int) {}
}

internal class JwBuilder() {
    var firstName = "Homer"
    var lastName = "Simpson"

    fun WithFirstName(s: String): JwBuilder {
        firstName = s
        return this
    }

    fun WithLastName(s: String): JwBuilder {
        lastName = s
        return this
    }
}

internal class JwIF1(private val p1: Int, var p2: Int)
internal class JwIF2( p1:Int) {
    val p1: Int = p1
}

internal class JwGeneric<T>(var name: T) {
}

/*
internal class JwA(nested: JwA.JwNest = JwA.JwNest(JwA.JwNest.FIELD)) {
    internal class  JwNest(p:Int) {
        companion object {
            val FIELD = 0
        }
    }
}*/

internal class JwModify(var args1: Int) {
    init {
        var args1 = args1
        args1++

        this.args1--;
    }
}

internal class JwPrivateCons {
    private  constructor() {}
    val x = 100
    companion object {
        val init = JwPrivateCons()
    }
}

class JsConstructor {
    @org.junit.Test fun create() {
        JwC1(100)
        JwC1(100, 200, 300)

        JwC2(100)
        JwC2(100, 200)
        JwC2(100, 200, 300)
    }

    @org.junit.Test fun build() {
        var builder = JwBuilder()
                .WithFirstName("wk").WithLastName("wk")
        assertEquals("wk", builder.firstName)
    }

    @org.junit.Test fun init() {
        var j1 = JwIF1(10, 20)
        assertEquals(j1.p2, 20)

        var j2 = JwIF2(10)
        assertEquals(j2.p1, 10)
    }

    @org.junit.Test fun generic() {
        var g1 = JwGeneric<String>("Hello")
        var g2 = JwGeneric<Int>(10)

        assertEquals(g1.name, "Hello")
        assertEquals(g2.name, 10)
    }

    @org.junit.Test fun modify() {
        var m1 = JwModify(10)
        assertEquals(m1.args1, 9)
    }

    @org.junit.Test fun privateCons() {
        var p1 = JwPrivateCons.init
        assertEquals(100, p1.x)
    }
}

