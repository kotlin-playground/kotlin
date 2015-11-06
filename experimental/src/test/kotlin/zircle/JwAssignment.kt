package zircle

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

/**
 * Created by wk on 11/6/2015 AD.
 */


class JwAssignment {
    @Test fun _and() {
        var x = 1 and 2
        assertEquals(0, x)

        var y = 2 and 2
        assertEquals(2, y)
    }

    @Test fun assing() {
        var y = 100
        y /= 2
        y += 2
        y *= 2
        y %= 2
    }

    @Test fun nullability() {
        var o2:HashSet<Any?>? = null
        try {
            var size = o2!!.size
        }catch(e: Exception) {
            assertEquals(true, e is KotlinNullPointerException)
        }
    }

    @Test fun notNullable() {
        var o = HashSet<Any>()
        //o = null
        assertEquals(0, o.size)
    }

    @Test fun _or() {
        var x = 1 or 2
        assertEquals(x,3)


        var y = 2 or 2
        assertEquals(2, y)
    }

    @Test fun shift() {
        var x = 1 shl 1
        assertEquals(2, x)

        var y = 1 shr 1
        assertEquals(0, y)
    }

    @Test fun xor() {
        var x = 1 xor 2
        assertEquals(3, x)

        var y = 2 xor 2
        assertEquals(0, y)
    }
}