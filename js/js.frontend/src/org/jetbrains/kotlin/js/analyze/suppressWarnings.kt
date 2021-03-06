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

package org.jetbrains.kotlin.js.analyze

import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.js.PredefinedAnnotation.*
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.js.config.LibrarySourcesConfig
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters1
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticsWithSuppression
import org.jetbrains.kotlin.resolve.diagnostics.SuppressDiagnosticsByAnnotations
import org.jetbrains.kotlin.resolve.diagnostics.FUNCTION_NO_BODY_ERRORS
import org.jetbrains.kotlin.resolve.diagnostics.PROPERTY_NOT_INITIALIZED_ERRORS

private val NATIVE_ANNOTATIONS = arrayOf(NATIVE.fqName, NATIVE_INVOKE.fqName, NATIVE_GETTER.fqName, NATIVE_SETTER.fqName)

public class SuppressUnusedParameterForJsNative : SuppressDiagnosticsByAnnotations(listOf(Errors.UNUSED_PARAMETER), *NATIVE_ANNOTATIONS)

public class SuppressNoBodyErrorsForNativeDeclarations : SuppressDiagnosticsByAnnotations(FUNCTION_NO_BODY_ERRORS + PROPERTY_NOT_INITIALIZED_ERRORS, *NATIVE_ANNOTATIONS)

public class SuppressUninitializedErrorsForNativeDeclarations : DiagnosticsWithSuppression.DiagnosticSuppressor {
    override fun isSuppressed(diagnostic: Diagnostic): Boolean {
        if (diagnostic.getFactory() != Errors.UNINITIALIZED_VARIABLE) return false

        @Suppress("UNCHECKED_CAST")
        val diagnosticWithParameters = diagnostic as DiagnosticWithParameters1<KtSimpleNameExpression, VariableDescriptor>

        val variableDescriptor = diagnosticWithParameters.getA()

        return AnnotationsUtils.isNativeObject(variableDescriptor)
    }
}

public class SuppressWarningsFromExternalModules : DiagnosticsWithSuppression.DiagnosticSuppressor {
    override fun isSuppressed(diagnostic: Diagnostic): Boolean {
        val file = diagnostic.getPsiFile()
        return diagnostic.getSeverity() == Severity.WARNING &&
               file is KtFile && file.getUserData(LibrarySourcesConfig.EXTERNAL_MODULE_NAME) != null
    }
}
