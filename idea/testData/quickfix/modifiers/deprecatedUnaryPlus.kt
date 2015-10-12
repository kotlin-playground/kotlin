// "Rename to 'unaryPlus'" "true"
operator fun String.plus() = this

fun a() {
    <caret>+""
}