package zircle
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by wk on 11/6/2015 AD.
 */
public class JwStaticConstructor {
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

class JwConstructorBlock {
    @Test fun annoBlock() {
        var t =  JwStaticConstructor()
        assertEquals("Hello", t.message)
    }

    @Test fun objectBlock() {
        var msg =  JwSingle.message
        assertEquals("Hello", msg)
    }
}