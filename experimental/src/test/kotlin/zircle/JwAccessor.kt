package zircle

internal interface  JwInterfaceI {
    var x: Int
}

internal class JwClassI(override var x: Int, y: Int): JwInterfaceI

internal class JwAAA {
    private var x = 42

    fun setX(x: Int) {
        this.x = x
    }
}

internal  class JwProtect {
    var x = 42
        protected set get

    var y = 42
        private set
}

internal object JwNested {
    var x = 42
    object  JwAgain {
        var x = 42
    }
}

internal  class  JwKeyword {
    var default = 1
    var `this` = 1
}

internal class JwMatchSetterGetter {
    var value: Any? = null
        private  set
    fun setValue(s: String) { value = s}
}

class JwAccessor {
    @org.junit.Test fun showSet() {
       var s = JwMatchSetterGetter()
        s.setValue("200")
        //s.value = 200
    }
}


