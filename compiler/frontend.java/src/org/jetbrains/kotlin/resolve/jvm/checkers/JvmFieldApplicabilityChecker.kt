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

package org.jetbrains.kotlin.resolve.jvm.checkers

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.fileClasses.JvmFileClassUtil
import org.jetbrains.kotlin.fileClasses.isInsideJvmMultifileClassFile
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DeclarationChecker
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.jvm.annotations.findJvmFieldAnnotation
import org.jetbrains.kotlin.resolve.jvm.checkers.JvmFieldApplicabilityChecker.Problem.*
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ErrorsJvm


class JvmFieldApplicabilityChecker : DeclarationChecker {

    enum class Problem(val errorMessage: String) {
        NOT_A_PROPERTY("JvmField can only be applied to a property"),
        NOT_FINAL("JvmField can only be applied to final property"),
        PRIVATE("JvmField has no effect on a private property"),
        CUSTOM_ACCESSOR("JvmField cannot be applied to a property with a custom accessor"),
        NO_BACKING_FIELD("JvmField can only be applied to a property with backing field"),
        OVERRIDES("JvmField cannot be applied to a property that overrides some other property"),
        LATEINIT("JvmField cannot be applied to lateinit property"),
        CONST("JvmField cannot be applied to const property"),
        INSIDE_COMPANION_OF_INTERFACE("JvmField cannot be applied to a property defined in companion object of interface"),
        TOP_LEVEL_PROPERTY_OF_MULTIFILE_FACADE("JvmField cannot be applied to top level property of a file annotated with ${JvmFileClassUtil.JVM_MULTIFILE_CLASS_SHORT}")
    }

    override fun check(
            declaration: KtDeclaration,
            descriptor: DeclarationDescriptor,
            diagnosticHolder: DiagnosticSink,
            bindingContext: BindingContext
    ) {
        val annotation = descriptor.findJvmFieldAnnotation() ?: return

        val problem = when {
            descriptor !is PropertyDescriptor -> NOT_A_PROPERTY
            descriptor.modality.isOverridable -> NOT_FINAL
            Visibilities.isPrivate(descriptor.visibility) -> PRIVATE
            !descriptor.hasBackingField(bindingContext) -> NO_BACKING_FIELD
            descriptor.hasCustomAccessor() -> CUSTOM_ACCESSOR
            descriptor.overriddenDescriptors.isNotEmpty() -> OVERRIDES
            descriptor.isLateInit -> LATEINIT
            descriptor.isConst -> CONST
            descriptor.isInsideCompanionObjectOfInterface() -> INSIDE_COMPANION_OF_INTERFACE
            DescriptorUtils.isTopLevelDeclaration(descriptor) && declaration.isInsideJvmMultifileClassFile() ->
                TOP_LEVEL_PROPERTY_OF_MULTIFILE_FACADE
            else -> return
        }

        val annotationEntry = DescriptorToSourceUtils.getSourceFromAnnotation(annotation) ?: return
        diagnosticHolder.report(ErrorsJvm.INAPPLICABLE_JVM_FIELD.on(annotationEntry, problem.errorMessage))
    }

    private fun PropertyDescriptor.hasCustomAccessor()
            = !(getter?.isDefault ?: true) || !(setter?.isDefault ?: true)

    private fun PropertyDescriptor.hasBackingField(bindingContext: BindingContext)
            = bindingContext.get(BindingContext.BACKING_FIELD_REQUIRED, this) ?: false

    private fun PropertyDescriptor.isInsideCompanionObjectOfInterface(): Boolean {
        val containingClass = containingDeclaration as? ClassDescriptor ?: return false
        if (!DescriptorUtils.isCompanionObject(containingClass)) return false

        val outerClassForObject = containingClass.containingDeclaration as? ClassDescriptor ?: return false
        return DescriptorUtils.isInterface(outerClassForObject)
    }
}