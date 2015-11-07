package zircle

import kotlin.test.assertEquals
import org.junit.Test

/**
 * Created by wk on 11/7/2015 AD.
 */

class JwConditionalExpression {

    @Test fun `if`() {

        var b = false
        var c = 0

        if(b) c = 1
        else c = 2

        assertEquals(c, 2)

        var x = ""
        var y = if(x.isEmpty()) 0 else 1
        assertEquals(0, y)

    }
}