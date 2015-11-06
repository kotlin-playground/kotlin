package zircle

/**
 * Created by wk on 11/6/2015 AD.
 */

annotation class JwAnno(val name: String)

@JwAnno(name="Jw")
annotation class JwInfo

@JwInfo class HelloJw { }

annotation class VarargAnno(vararg val value: String)
@VarargAnno("a", "b", "c", "d") class HelloVararg {}
