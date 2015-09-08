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

package org.jetbrains.kotlin.idea.stubindex

import com.intellij.psi.stubs.PsiClassHolderFileStub
import com.intellij.util.io.StringRef
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.JetFile
import org.jetbrains.kotlin.psi.stubs.KotlinFileStub
import org.jetbrains.kotlin.psi.stubs.impl
import org.jetbrains.kotlin.psi.stubs.impl.KotlinFileStubImpl

public class KotlinFileStubForIde(
        jetFile: JetFile?,
        packageName: StringRef,
        isScript: Boolean,
        val facadeSimpleName: StringRef?,
        val partSimpleName: StringRef?
) : KotlinFileStubImpl(jetFile, packageName, isScript), KotlinFileStub, PsiClassHolderFileStub<JetFile> {

    private fun StringRef.relativeToPackage() = getPackageFqName().child(Name.identifier(this.string))

    val facadeFqName: FqName?
        get() = facadeSimpleName?.relativeToPackage()

    val partFqName: FqName?
        get() = partSimpleName?.relativeToPackage()

    public constructor(jetFile: JetFile?, packageName: String, isScript: Boolean)
    : this(jetFile, StringRef.fromString(packageName)!!, isScript, null, null)

    companion object {
        public fun forFile(packageFqName: FqName, isScript: Boolean): impl.KotlinFileStubImpl =
                KotlinFileStubForIde(jetFile = null,
                                     packageName = StringRef.fromString(packageFqName.asString())!!,
                                     facadeSimpleName = null,
                                     partSimpleName = null,
                                     isScript = isScript)

        public fun forFileFacadeStub(facadeFqName: FqName, isScript: Boolean): impl.KotlinFileStubImpl =
                KotlinFileStubForIde(jetFile = null,
                                     packageName = StringRef.fromString(facadeFqName.parent().asString())!!,
                                     facadeSimpleName = StringRef.fromString(facadeFqName.shortName().asString())!!,
                                     partSimpleName = StringRef.fromString(facadeFqName.shortName().asString())!!,
                                     isScript = isScript)
    }
}