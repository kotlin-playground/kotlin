package zircle

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by wk on 11/7/2015 AD.
 */


class JwBlock {

    @Test fun block() {
        var x = 100

        run {
            x += 10
        }

        run {
            x -= 5
        }

        assertEquals(105, x)
    }
}