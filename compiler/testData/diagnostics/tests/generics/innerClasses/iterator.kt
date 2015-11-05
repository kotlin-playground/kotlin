// !DIAGNOSTICS: -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_VARIABLE
import java.util.*

class A<T> : AbstractCollection<T>() {
    override fun iterator(): MyIt = MyIt()

    override val size: Int
        get() = 1

    inner class MyIt : MutableIterator<T> {
        override fun next(): T {
            throw UnsupportedOperationException()
        }

        override fun hasNext(): Boolean {
            throw UnsupportedOperationException()
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }
    }
}

fun foo() {
    var myIt = A<String>().iterator()
    myIt = <!TYPE_MISMATCH!>A<Int>().iterator()<!>

    val csIt: Iterator<CharSequence> = A<String>().iterator()
}
