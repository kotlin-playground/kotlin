package zircle

import org.junit.Test

/**
 * Created by wk on 11/6/2015 AD.
 */


class JwAssert {
    @Test fun simpleAssert() {
        assert(true)
    }

    @Test fun stringDetail() {
        assert(true) { "string detail here ..." }
    }
}