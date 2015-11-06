package zircle

import org.junit.Test
import kotlin.test.assertEquals

class JwBreak {

    @Test fun `break`() {
        var x = 100;
        for (k in 1..100) {
            if(k == 5) break;
            x = k
        }

        assertEquals(4, x)
    }
}
