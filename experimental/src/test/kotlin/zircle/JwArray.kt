package zircle

import org.junit.Test
import org.junit.Assert.*

/**
 * Created by wk on 11/6/2015 AD.
 */

class JwArray {

    @Test fun shouldInitializeArray() {
        var d2 = arrayOf<Int>()
        assertEquals(0, d2.size)
    }

    @Test fun shouldInitializeNullArray() {
        var d2 = arrayOfNulls<Int>(5)
        assertEquals(5, d2.size)
        assertEquals(null, d2[0])
    }

    @Test fun shouldInitializeWithArrayConstructor() {
        var d2 = Array(5) { 10 }
        assertEquals(5, d2.size)
        assertEquals(10, d2[0])
    }
    @Test fun emptyStringArray() {
        var ss = Array(5) { "SS" }
        assertEquals(5, ss.size)
        assertEquals("SS", ss[0])
    }

    @Test fun longArray() {
        var l = longArrayOf(1,2,3)
        assertEquals(3, l.size)
        assertEquals(l[2], 3)
    }

    @Test fun intArray() {
        var i = intArrayOf(1,2,3)
        assertEquals(1, i[0])
    }

    @Test fun stringArray() {
        var s = arrayOf("string")
        assertEquals("string", s[0])
    }

    @Test fun initialize() {
        var d = doubleArrayOf(1.0, 2.0, 3.0)
        var f = floatArrayOf(1f,2f,3f)
        var i = IntArray(10)
        var doubleArray = arrayOf(1,0, 2.0, 3.0)
        var floatArray = arrayOf(1f, 2f, 3f)
        var byteArray = arrayOf(1,2,3)
        var intArray = arrayOf(1,2,3)
        var objectArray = arrayOfNulls<Any>(10)
        var oneDim = intArrayOf(1,2,3)
        var `is` = intArrayOf(1,2,3)
        var twoDim = arrayOf(intArrayOf(1,2), intArrayOf(2,3))
    }


    @Test fun arrayAccess() {

    }
}

