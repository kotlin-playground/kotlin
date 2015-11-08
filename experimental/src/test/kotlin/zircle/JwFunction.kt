package zircle

import kotlin.test.assertEquals

/**
 * Created by wk on 11/8/2015 AD.
 */


interface  JwFunction {
    abstract val noofGears: Int
    abstract fun main()
}

interface JwFunction2 {
    val finalString: String
        get() = ""
}

class  JwJava8Lampda {
    fun foo0(r: (s:Int) -> Int): Int {
        return  r(10)
    }
}


class JwLampdaSpec {
    @org.junit.Test fun call() {
        var x = { i:Int -> i + 1 }
        var l = JwJava8Lampda()
        var rs = l.foo0(x)
        assertEquals(11, rs)


        var k = {
            var x = 100
            x + x
        }

        assertEquals(200, k())
    }
}