package zircle

/**
 * Created by wk on 11/8/2015 AD.
 */

class JwForA {
    fun foo(array: Array<String>) {
        for(i in array.indices.reversed()) { }

        var condition = false
        while(condition) { }

        var n = 2
        for(i in 10 downTo  0){ }
        for(i in 10 downTo 0 + n) {}

        for(i in 0..9) {}
    }

    fun foo2(list: List<String>, list2: List<String>?) {
        for (n in list) { }
        for (n in list!!) { }
    }
}