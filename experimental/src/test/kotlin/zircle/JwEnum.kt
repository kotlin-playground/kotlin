package zircle

import kotlin.test.assertEquals


internal  enum class Film constructor(var x:Int) {
    Drama(10) , Comedy(20)
}


class JwEnumSpec {
    @org.junit.Test fun showCreateEnum() {
        var enum = Film.Comedy
        //assertEquals(Film(20), enum)
    }
}

