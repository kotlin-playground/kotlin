/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.caches.resolve

import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.*
import com.intellij.psi.impl.compiled.ClsModifierListImpl
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.asJava.KtLightField
import org.jetbrains.kotlin.asJava.KtLightMethod
import org.jetbrains.kotlin.asJava.LightClassTestCommon
import org.jetbrains.kotlin.idea.test.KotlinLightCodeInsightFixtureTestCase
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.MockLibraryUtil
import org.jetbrains.kotlin.idea.test.KotlinWithJdkAndRuntimeLightProjectDescriptor
import org.junit.Assert
import java.io.File

abstract class AbstractIdeLightClassTest : KotlinLightCodeInsightFixtureTestCase() {
    override fun setUp() {
        super.setUp()

        val testName = getTestName(false)
        if (testName.startsWith("AllFilesPresentIn")) return

        val libraryName = "libFor" + testName
        val filePath = "${KotlinTestUtils.getTestsRoot(this)}/${getTestName(false)}.kt"

        Assert.assertTrue("File doesn't exist $filePath", File(filePath).exists())

        val libraryJar = MockLibraryUtil.compileLibraryToJar(filePath, libraryName, false, false
        )
        val jarUrl = "jar://" + FileUtilRt.toSystemIndependentName(libraryJar.absolutePath) + "!/"
        ModuleRootModificationUtil.addModuleLibrary(myFixture.module, jarUrl)

        println(myFixture.module)
    }

    override fun tearDown() {
//        ModuleRootModificationUtil.de(myFixture.module, jarUrl)

        super.tearDown()


    }

    fun doTest(testDataPath: String) {
        myFixture.configureByFile(testDataPath)

        val project = project
        LightClassTestCommon.testLightClass(
                File(testDataPath),
                findLightClass = {
                    val clazz = JavaPsiFacade.getInstance(project).findClass(it, GlobalSearchScope.allScope(project))
                    if (clazz != null) {
                        checkPsiElementStructure(clazz)
                    }
                    clazz

                },
                normalizeText = {
                    //NOTE: ide and compiler differ in names generated for parameters with unspecified names
                    it
                            .replace("java.lang.String s,", "java.lang.String p,")
                            .replace("java.lang.String s)", "java.lang.String p)")
                            .replace("java.lang.String s1", "java.lang.String p1")
                            .replace("java.lang.String s2", "java.lang.String p2")
                }
        )
    }

    override fun getProjectDescriptor() = KotlinWithJdkAndRuntimeLightProjectDescriptor.INSTANCE

    val TEST_DATA_KEY = Key.create<Int>("Test Key")

    private fun checkPsiElementStructure(lightClass: PsiClass) {
        checkPsiElement(lightClass)

        val typeParameterList = lightClass.typeParameterList
        if (typeParameterList != null) {
            checkPsiElement(typeParameterList)
            typeParameterList.typeParameters.forEach { checkPsiElement(it) }
        }

        lightClass.innerClasses.forEach { checkPsiElementStructure(it) }

        lightClass.methods.forEach {
            it.parameterList.parameters.forEach { checkPsiElement(it) }
            checkPsiElement(it)
        }

        lightClass.fields.forEach { checkPsiElement(it) }
    }

    private fun checkPsiElement(element: PsiModifierListOwner) {
        val modifierList = element.modifierList
        if (modifierList != null) {
            if (element is KtLightField || element is KtLightMethod) {
                Assert.assertTrue(modifierList is ClsModifierListImpl)
            }
            else {
                checkPsiElement(modifierList)
            }
        }

        checkPsiElement(element as PsiElement)
    }

    private fun checkPsiElement(element: PsiElement) {
        with(element) {
            try {
                Assert.assertEquals("Number of methods have changed. Please update test.", 54, PsiElement::class.java.methods.size)

                project
                Assert.assertTrue(language == KotlinLanguage.INSTANCE)
                manager
                children
                parent
                firstChild
                lastChild
                nextSibling
                prevSibling
                containingFile
                textRange
                startOffsetInParent
                textLength
                findElementAt(0)
                findReferenceAt(0)
                textOffset
                text
                textToCharArray()
                navigationElement
                originalElement
                textMatches("")
                Assert.assertTrue(textMatches(this))
                textContains('a')
                accept(PsiElementVisitor.EMPTY_VISITOR)
                acceptChildren(PsiElementVisitor.EMPTY_VISITOR)

                val copy = copy()
                Assert.assertTrue(copy == null || copy.javaClass == this.javaClass)

                // Modify methods:
                // add(this)
                // addBefore(this, lastChild)
                // addAfter(firstChild, this)
                // checkAdd(this)
                // addRange(firstChild, lastChild)
                // addRangeBefore(firstChild, lastChild, lastChild)
                // addRangeAfter(firstChild, lastChild, firstChild)
                // delete()
                // checkDelete()
                // deleteChildRange(firstChild, lastChild)
                // replace(this)

                Assert.assertTrue(isValid)
                isWritable
                reference
                references
                putCopyableUserData(TEST_DATA_KEY, 12)

                Assert.assertTrue(getCopyableUserData(TEST_DATA_KEY) == 12)
                // Assert.assertTrue(copy().getCopyableUserData(TEST_DATA_KEY) == 12) { this } Doesn't work

                // processDeclarations(...)

                context
                isPhysical
                resolveScope
                useScope
                node
                toString()
                Assert.assertTrue(isEquivalentTo(this))
            }
            catch (t: Throwable) {
                throw AssertionErrorWithCause("Failed for ${this.javaClass} ${this}", t)
            }
        }

    }
}