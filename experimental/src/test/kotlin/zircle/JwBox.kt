package zircle

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by wk on 11/7/2015 AD.
 */

class JwBox {
    @Test fun box() {
        var i = 10
        var nullableInt:Int? = 0
        i = nullableInt!!
        assertEquals(0, i)
        nullableInt = null
        assertEquals(null, nullableInt)

        var notNullableInt: Int = 10
        i = notNullableInt
        assertEquals(10, i)
        //notNullableInt = null
    }
}