package zircle

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by wk on 11/7/2015 AD.
 */

class JwClassExpression {
    @Test fun declare() {

        var stringClass = arrayOf<Class<*>>(String::class.java, String::class.java)
        var objectClass = arrayOf<Any>(Any::class.java)

        var voidType = Void.TYPE
        var intergerType = Integer.TYPE
        var doubleType = java.lang.Double.TYPE
        var intArrayType = IntArray::class.java
        var stringType = String::class.java
    }
}