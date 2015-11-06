package zircle
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by wk on 11/6/2015 AD.
 */
class JwTest {
    val message : String
    init {
        message = "Hello"
    }
}

object JwSingle {
    var message: String
    init {
        message = "Hello"
    }
}

class JwBlock {
    @Test fun annoBlock() {
        var t =  JwTest()
        assertEquals("Hello", t.message)
    }

    @Test fun objectBlock() {
        var msg =  JwSingle.message
        assertEquals("Hello", msg)
    }
}