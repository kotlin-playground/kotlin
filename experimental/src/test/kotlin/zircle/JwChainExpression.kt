package zircle

import org.junit.Test
import kotlin.test.assertEquals


/**
 * Created by wk on 11/7/2015 AD.
 */


internal object Library {
    val ourOut: java.io.PrintStream?

    init {
        ourOut = null
    }
}

internal object  Library2 {
    fun call() = ""

    val string: String
        get() = ""

    var string2: String = "Hello"
        set(s) {  this.string2 = s }
}

class JwChainExpression {
    @Test fun callChain() {
        var rs = Library.ourOut?.print(1)
        assertEquals(null, rs)
    }

    @Test fun callMethod() {
        assertEquals("", Library2.call())
        assertEquals("", Library2.string)
    }
}