import test.*

fun box(): String {
    val loci = listOf("a", "b", "c")
    var gene = "g1"

    inlineCall {
        val value = 10.0
        inlineCall {
            {
                value
                gene = "OK"
            }()
        }
    }

    return gene
}
