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

package org.jetbrains.kotlin.idea.run

import com.intellij.execution.Location
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.fileClasses.javaFileFacadeFqName
import org.jetbrains.kotlin.idea.MainFunctionDetector
import org.jetbrains.kotlin.idea.caches.resolve.getResolutionFacade
import org.jetbrains.kotlin.idea.project.ProjectStructureUtil
import org.jetbrains.kotlin.idea.util.ProjectRootsUtil
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

public class KotlinRunConfigurationProducer : RunConfigurationProducer<JetRunConfiguration>(JetRunConfigurationType.getInstance()) {

    override fun setupConfigurationFromContext(configuration: JetRunConfiguration,
                                               context: ConfigurationContext,
                                               sourceElement: Ref<PsiElement>): Boolean {
        val location = context.getLocation() ?: return false
        val module = location.getModule() ?: return false
        val container = getEntryPointContainer(location)
        val startClassFQName = getStartClassFqName(container) ?: return false

        setupConfigurationByQName(module, configuration, startClassFQName)
        return true
    }

    private fun getEntryPointContainer(location: Location<*>?): KtDeclarationContainer? {
        if (location == null) return null
        if (DumbService.getInstance(location.getProject()).isDumb()) return null

        val module = location.getModule() ?: return null

        if (ProjectStructureUtil.isJsKotlinModule(module)) return null

        val locationElement = location.getPsiElement()

        return getEntryPointContainer(locationElement)
    }

    private fun setupConfigurationByQName(module: Module,
                                          configuration: JetRunConfiguration,
                                          fqName: FqName) {
        configuration.setModule(module)
        configuration.setRunClass(fqName.asString())
        configuration.setGeneratedName()
    }

    override fun isConfigurationFromContext(configuration: JetRunConfiguration, context: ConfigurationContext): Boolean {
        val startClassFQName = getStartClassFqName(getEntryPointContainer(context.getLocation())) ?: return false

        return configuration.getRunClass() == startClassFQName.asString() &&
            context.getModule() ==  configuration.getConfigurationModule().getModule()
    }

    companion object {
        public fun getEntryPointContainer(locationElement: PsiElement): KtDeclarationContainer? {
            val psiFile = locationElement.getContainingFile()
            if (!(psiFile is KtFile && ProjectRootsUtil.isInProjectOrLibSource(psiFile))) return null

            val resolutionFacade = psiFile.getResolutionFacade()
            val mainFunctionDetector = MainFunctionDetector { resolutionFacade.resolveToDescriptor(it) as FunctionDescriptor }

            var currentElement = locationElement.declarationContainer(false)
            while (currentElement != null) {
                var entryPointContainer = currentElement
                if (entryPointContainer is KtClass) {
                    entryPointContainer = entryPointContainer.getCompanionObjects().singleOrNull()
                }
                if (entryPointContainer != null && mainFunctionDetector.hasMain(entryPointContainer.getDeclarations())) return entryPointContainer
                currentElement = (currentElement as PsiElement).declarationContainer(true)
            }

            return null
        }

        public fun getStartClassFqName(container: KtDeclarationContainer?): FqName? = when(container) {
            null -> null
            is KtFile -> container.javaFileFacadeFqName
            is KtClassOrObject -> {
                if (container is KtObjectDeclaration && container.isCompanion()) {
                    val containerClass = container.getParentOfType<KtClass>(true)
                    containerClass?.getFqName()
                } else {
                    container.getFqName()
                }
            }
            else -> throw IllegalArgumentException("Invalid entry-point container: " + (container as PsiElement).getText())
        }

        private fun PsiElement.declarationContainer(strict: Boolean): KtDeclarationContainer? {
            val element = if (strict)
                PsiTreeUtil.getParentOfType(this, javaClass<KtClassOrObject>(), javaClass<KtFile>())
            else
                PsiTreeUtil.getNonStrictParentOfType(this, javaClass<KtClassOrObject>(), javaClass<KtFile>())
            return element as KtDeclarationContainer?
        }

    }
}
