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

package org.jetbrains.kotlin.codegen;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.codegen.annotation.AnnotatedSimple;
import org.jetbrains.kotlin.codegen.annotation.AnnotatedWithFakeAnnotations;
import org.jetbrains.kotlin.codegen.context.*;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.codegen.state.JetTypeMapper;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.descriptors.annotations.Annotated;
import org.jetbrains.kotlin.descriptors.annotations.AnnotationSplitter;
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget;
import org.jetbrains.kotlin.descriptors.annotations.Annotations;
import org.jetbrains.kotlin.load.java.JvmAbi;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.psi.psiUtil.PsiUtilsKt;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.DescriptorFactory;
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils;
import org.jetbrains.kotlin.resolve.annotations.AnnotationUtilKt;
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall;
import org.jetbrains.kotlin.resolve.constants.ConstantValue;
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKt;
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature;
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPropertyDescriptor;
import org.jetbrains.kotlin.storage.LockBasedStorageManager;
import org.jetbrains.kotlin.types.ErrorUtils;
import org.jetbrains.kotlin.types.KotlinType;
import org.jetbrains.org.objectweb.asm.FieldVisitor;
import org.jetbrains.org.objectweb.asm.MethodVisitor;
import org.jetbrains.org.objectweb.asm.Opcodes;
import org.jetbrains.org.objectweb.asm.Type;
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter;
import org.jetbrains.org.objectweb.asm.commons.Method;

import java.util.List;

import static org.jetbrains.kotlin.codegen.AsmUtil.*;
import static org.jetbrains.kotlin.codegen.JvmCodegenUtil.isJvmInterface;
import static org.jetbrains.kotlin.codegen.serialization.JvmSerializationBindings.*;
import static org.jetbrains.kotlin.resolve.DescriptorUtils.*;
import static org.jetbrains.kotlin.resolve.jvm.AsmTypes.PROPERTY_METADATA_TYPE;
import static org.jetbrains.kotlin.resolve.jvm.annotations.AnnotationUtilKt.hasJvmFieldAnnotation;
import static org.jetbrains.org.objectweb.asm.Opcodes.*;

public class PropertyCodegen {
    private final GenerationState state;
    private final ClassBuilder v;
    private final FunctionCodegen functionCodegen;
    private final JetTypeMapper typeMapper;
    private final BindingContext bindingContext;
    private final FieldOwnerContext context;
    private final MemberCodegen<?> memberCodegen;
    private final OwnerKind kind;

    public PropertyCodegen(
            @NotNull FieldOwnerContext context,
            @NotNull ClassBuilder v,
            @NotNull FunctionCodegen functionCodegen,
            @NotNull MemberCodegen<?> memberCodegen
    ) {
        this.state = functionCodegen.state;
        this.v = v;
        this.functionCodegen = functionCodegen;
        this.typeMapper = state.getTypeMapper();
        this.bindingContext = state.getBindingContext();
        this.context = context;
        this.memberCodegen = memberCodegen;
        this.kind = context.getContextKind();
    }

    public void gen(@NotNull KtProperty property) {
        VariableDescriptor variableDescriptor = bindingContext.get(BindingContext.VARIABLE, property);
        assert variableDescriptor instanceof PropertyDescriptor : "Property " + property.getText() + " should have a property descriptor: " + variableDescriptor;

        PropertyDescriptor propertyDescriptor = (PropertyDescriptor) variableDescriptor;
        gen(property, propertyDescriptor, property.getGetter(), property.getSetter());
    }

    public void generateInPackageFacade(@NotNull DeserializedPropertyDescriptor deserializedProperty) {
        assert context instanceof DelegatingFacadeContext : "should be called only for generating facade: " + context;
        gen(null, deserializedProperty, null, null);
    }

    private void gen(
            @Nullable KtProperty declaration,
            @NotNull PropertyDescriptor descriptor,
            @Nullable KtPropertyAccessor getter,
            @Nullable KtPropertyAccessor setter
    ) {
        assert kind == OwnerKind.PACKAGE || kind == OwnerKind.IMPLEMENTATION || kind == OwnerKind.DEFAULT_IMPLS
                : "Generating property with a wrong kind (" + kind + "): " + descriptor;

        String implClassName = CodegenContextUtil.getImplementationClassShortName(context);
        if (implClassName != null) {
            v.getSerializationBindings().put(IMPL_CLASS_NAME_FOR_CALLABLE, descriptor, implClassName);
        }

        if (CodegenContextUtil.isImplClassOwner(context)) {
            assert declaration != null : "Declaration is null for different context: " + context;

            boolean hasBackingField = hasBackingField(declaration, descriptor);

            AnnotationSplitter annotationSplitter = AnnotationSplitter.create(LockBasedStorageManager.NO_LOCKS,
                    descriptor.getAnnotations(), AnnotationSplitter.getTargetSet(false, descriptor.isVar(), hasBackingField));

            Annotations fieldAnnotations = annotationSplitter.getAnnotationsForTarget(AnnotationUseSiteTarget.FIELD);
            Annotations propertyAnnotations = annotationSplitter.getAnnotationsForTarget(AnnotationUseSiteTarget.PROPERTY);

            generateBackingField(declaration, descriptor, fieldAnnotations);
            generateSyntheticMethodIfNeeded(descriptor, propertyAnnotations);
        }

        if (isAccessorNeeded(declaration, descriptor, getter)) {
            generateGetter(declaration, descriptor, getter);
        }
        if (isAccessorNeeded(declaration, descriptor, setter)) {
            generateSetter(declaration, descriptor, setter);
        }

        context.recordSyntheticAccessorIfNeeded(descriptor, bindingContext);
    }

    /**
     * Determines if it's necessary to generate an accessor to the property, i.e. if this property can be referenced via getter/setter
     * for any reason
     *
     * @see JvmCodegenUtil#couldUseDirectAccessToProperty
     */
    private boolean isAccessorNeeded(
            @Nullable KtProperty declaration,
            @NotNull PropertyDescriptor descriptor,
            @Nullable KtPropertyAccessor accessor
    ) {
        if (hasJvmFieldAnnotation(descriptor)) return false;

        boolean isDefaultAccessor = accessor == null || !accessor.hasBody();

        // Don't generate accessors for interface properties with default accessors in DefaultImpls
        if (kind == OwnerKind.DEFAULT_IMPLS && isDefaultAccessor) return false;

        if (declaration == null) return true;

        // Delegated or extension properties can only be referenced via accessors
        if (declaration.hasDelegate() || declaration.getReceiverTypeReference() != null) return true;

        // Companion object properties always should have accessors, because their backing fields are moved/copied to the outer class
        if (isCompanionObject(descriptor.getContainingDeclaration())) return true;

        // Private class properties have accessors only in cases when those accessors are non-trivial
        if (Visibilities.isPrivate(descriptor.getVisibility())) {
            return !isDefaultAccessor;
        }

        return true;
    }

    public void generatePrimaryConstructorProperty(KtParameter p, PropertyDescriptor descriptor) {
        AnnotationSplitter annotationSplitter = AnnotationSplitter.create(LockBasedStorageManager.NO_LOCKS,
                descriptor.getAnnotations(), AnnotationSplitter.getTargetSet(true, descriptor.isVar(), hasBackingField(p, descriptor)));

        Annotations fieldAnnotations = annotationSplitter.getAnnotationsForTarget(AnnotationUseSiteTarget.FIELD);
        Annotations propertyAnnotations = annotationSplitter.getAnnotationsForTarget(AnnotationUseSiteTarget.PROPERTY);

        generateBackingField(p, descriptor, fieldAnnotations);
        generateSyntheticMethodIfNeeded(descriptor, propertyAnnotations);

        if (!Visibilities.isPrivate(descriptor.getVisibility())) {
            generateGetter(p, descriptor, null);
            if (descriptor.isVar()) {
                generateSetter(p, descriptor, null);
            }
        }
    }

    public void generateConstructorPropertyAsMethodForAnnotationClass(KtParameter p, PropertyDescriptor descriptor) {
        JvmMethodSignature signature = typeMapper.mapAnnotationParameterSignature(descriptor);
        String name = p.getName();
        if (name == null) return;
        MethodVisitor mv = v.newMethod(
                JvmDeclarationOriginKt.OtherOrigin(p, descriptor), ACC_PUBLIC | ACC_ABSTRACT, name,
                signature.getAsmMethod().getDescriptor(),
                signature.getGenericsSignature(),
                null
        );

        KtExpression defaultValue = p.getDefaultValue();
        if (defaultValue != null) {
            ConstantValue<?> constant = ExpressionCodegen.getCompileTimeConstant(defaultValue, bindingContext);
            assert state.getClassBuilderMode() != ClassBuilderMode.FULL || constant != null
                    : "Default value for annotation parameter should be compile time value: " + defaultValue.getText();
            if (constant != null) {
                AnnotationCodegen annotationCodegen = AnnotationCodegen.forAnnotationDefaultValue(mv, typeMapper);
                annotationCodegen.generateAnnotationDefaultValue(constant, descriptor.getType());
            }
        }

        mv.visitEnd();
    }

    private boolean hasBackingField(@NotNull KtNamedDeclaration p, @NotNull PropertyDescriptor descriptor) {
        return !isJvmInterface(descriptor.getContainingDeclaration()) &&
               kind != OwnerKind.DEFAULT_IMPLS &&
               !Boolean.FALSE.equals(bindingContext.get(BindingContext.BACKING_FIELD_REQUIRED, descriptor));
    }

    private boolean generateBackingField(
            @NotNull KtNamedDeclaration p,
            @NotNull PropertyDescriptor descriptor,
            @NotNull Annotations annotations
    ) {
        if (isJvmInterface(descriptor.getContainingDeclaration()) || kind == OwnerKind.DEFAULT_IMPLS) {
            return false;
        }

        if (p instanceof KtProperty && ((KtProperty) p).hasDelegate()) {
            generatePropertyDelegateAccess((KtProperty) p, descriptor, annotations);
        }
        else if (Boolean.TRUE.equals(bindingContext.get(BindingContext.BACKING_FIELD_REQUIRED, descriptor))) {
            generateBackingFieldAccess(p, descriptor, annotations);
        }
        else {
            return false;
        }
        return true;
    }

    // Annotations on properties are stored in bytecode on an empty synthetic method. This way they're still
    // accessible via reflection, and 'deprecated' and 'private' flags prevent this method from being called accidentally
    private void generateSyntheticMethodIfNeeded(@NotNull PropertyDescriptor descriptor, Annotations annotations) {
        if (annotations.getAllAnnotations().isEmpty()) return;

        ReceiverParameterDescriptor receiver = descriptor.getExtensionReceiverParameter();
        String name = JvmAbi.getSyntheticMethodNameForAnnotatedProperty(descriptor.getName());
        String desc = receiver == null ? "()V" : "(" + typeMapper.mapType(receiver.getType()) + ")V";

        if (!isInterface(context.getContextDescriptor()) || kind == OwnerKind.DEFAULT_IMPLS) {
            int flags = ACC_DEPRECATED | ACC_FINAL | ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC;
            MethodVisitor mv = v.newMethod(JvmDeclarationOriginKt.OtherOrigin(descriptor), flags, name, desc, null, null);
            AnnotationCodegen.forMethod(mv, typeMapper)
                    .genAnnotations(new AnnotatedSimple(annotations), Type.VOID_TYPE, AnnotationUseSiteTarget.PROPERTY);
            mv.visitCode();
            mv.visitInsn(Opcodes.RETURN);
            mv.visitEnd();
        }
        else {
            Type tImplType = typeMapper.mapDefaultImpls((ClassDescriptor) context.getContextDescriptor());
            v.getSerializationBindings().put(IMPL_CLASS_NAME_FOR_CALLABLE, descriptor, shortNameByAsmType(tImplType));
        }

        if (kind != OwnerKind.DEFAULT_IMPLS) {
            v.getSerializationBindings().put(SYNTHETIC_METHOD_FOR_PROPERTY, descriptor, new Method(name, desc));
        }
    }

    private void generateBackingField(
            KtNamedDeclaration element,
            PropertyDescriptor propertyDescriptor,
            boolean isDelegate,
            KotlinType jetType,
            Object defaultValue,
            Annotations annotations
    ) {
        int modifiers = getDeprecatedAccessFlag(propertyDescriptor);

        for (AnnotationCodegen.JvmFlagAnnotation flagAnnotation : AnnotationCodegen.FIELD_FLAGS) {
            if (flagAnnotation.hasAnnotation(propertyDescriptor.getOriginal())) {
                modifiers |= flagAnnotation.getJvmFlag();
            }
        }

        if (kind == OwnerKind.PACKAGE) {
            modifiers |= ACC_STATIC;
        }

        if (!propertyDescriptor.isLateInit() && (!propertyDescriptor.isVar() || isDelegate)) {
            modifiers |= ACC_FINAL;
        }

        if (AnnotationUtilKt.hasJvmSyntheticAnnotation(propertyDescriptor)) {
            modifiers |= ACC_SYNTHETIC;
        }

        Type type = typeMapper.mapType(jetType);

        ClassBuilder builder = v;

        boolean hasJvmFieldAnnotation = hasJvmFieldAnnotation(propertyDescriptor);

        FieldOwnerContext backingFieldContext = context;
        boolean takeVisibilityFromDescriptor = propertyDescriptor.isLateInit() || propertyDescriptor.isConst();
        if (AsmUtil.isInstancePropertyWithStaticBackingField(propertyDescriptor) ) {
            modifiers |= ACC_STATIC;

            if (takeVisibilityFromDescriptor) {
                modifiers |= getVisibilityAccessFlag(propertyDescriptor);
            }
            else if (hasJvmFieldAnnotation && !isDelegate) {
                modifiers |= getDefaultVisibilityFlag(propertyDescriptor.getVisibility());
            }
            else {
                modifiers |= getVisibilityForSpecialPropertyBackingField(propertyDescriptor, isDelegate);
            }

            if (AsmUtil.isPropertyWithBackingFieldInOuterClass(propertyDescriptor)) {
                ImplementationBodyCodegen codegen = (ImplementationBodyCodegen) memberCodegen.getParentCodegen();
                builder = codegen.v;
                backingFieldContext = codegen.context;
                v.getSerializationBindings().put(STATIC_FIELD_IN_OUTER_CLASS, propertyDescriptor);
            }

            if (isObject(propertyDescriptor.getContainingDeclaration()) &&
                !hasJvmFieldAnnotation &&
                !propertyDescriptor.isConst() &&
                (modifiers & ACC_PRIVATE) == 0) {
                modifiers |= ACC_DEPRECATED;
            }
        }
        else if (takeVisibilityFromDescriptor) {
            modifiers |= getVisibilityAccessFlag(propertyDescriptor);
        }
        else if (!isDelegate && hasJvmFieldAnnotation) {
            modifiers |= getDefaultVisibilityFlag(propertyDescriptor.getVisibility());
        }
        else {
            modifiers |= ACC_PRIVATE;
        }

        if (AsmUtil.isPropertyWithBackingFieldCopyInOuterClass(propertyDescriptor)) {
            ImplementationBodyCodegen parentBodyCodegen = (ImplementationBodyCodegen) memberCodegen.getParentCodegen();
            parentBodyCodegen.addCompanionObjectPropertyToCopy(propertyDescriptor, defaultValue);
        }

        String name = backingFieldContext.getFieldName(propertyDescriptor, isDelegate);

        v.getSerializationBindings().put(FIELD_FOR_PROPERTY, propertyDescriptor, Pair.create(type, name));

        FieldVisitor fv = builder.newField(JvmDeclarationOriginKt.OtherOrigin(element, propertyDescriptor), modifiers, name, type.getDescriptor(),
                                           typeMapper.mapFieldSignature(jetType), defaultValue);

        Annotated fieldAnnotated = new AnnotatedWithFakeAnnotations(propertyDescriptor, annotations);
        AnnotationCodegen.forField(fv, typeMapper).genAnnotations(fieldAnnotated, type, AnnotationUseSiteTarget.FIELD);
    }

    private void generatePropertyDelegateAccess(KtProperty p, PropertyDescriptor propertyDescriptor, Annotations annotations) {
        KtExpression delegateExpression = p.getDelegateExpression();
        KotlinType delegateType = delegateExpression != null ? bindingContext.getType(p.getDelegateExpression()) : null;
        if (delegateType == null) {
            // If delegate expression is unresolved reference
            delegateType = ErrorUtils.createErrorType("Delegate type");
        }

        generateBackingField(p, propertyDescriptor, true, delegateType, null, annotations);
    }

    private void generateBackingFieldAccess(KtNamedDeclaration p, PropertyDescriptor propertyDescriptor, Annotations annotations) {
        Object value = null;

        if (shouldWriteFieldInitializer(propertyDescriptor)) {
            ConstantValue<?> initializer = propertyDescriptor.getCompileTimeInitializer();
            if (initializer != null) {
                value = initializer.getValue();
            }
        }

        generateBackingField(p, propertyDescriptor, false, propertyDescriptor.getType(), value, annotations);
    }

    private boolean shouldWriteFieldInitializer(@NotNull PropertyDescriptor descriptor) {
        //final field of primitive or String type
        if (!descriptor.isVar()) {
            Type type = typeMapper.mapType(descriptor);
            return AsmUtil.isPrimitive(type) || "java.lang.String".equals(type.getClassName());
        }
        return false;
    }

    private void generateGetter(@Nullable KtNamedDeclaration p, @NotNull PropertyDescriptor descriptor, @Nullable KtPropertyAccessor getter) {
        generateAccessor(p, getter, descriptor.getGetter() != null
                                    ? descriptor.getGetter()
                                    : DescriptorFactory.createDefaultGetter(descriptor, Annotations.Companion.getEMPTY()));
    }

    private void generateSetter(@Nullable KtNamedDeclaration p, @NotNull PropertyDescriptor descriptor, @Nullable KtPropertyAccessor setter) {
        if (!descriptor.isVar()) return;

        generateAccessor(p, setter, descriptor.getSetter() != null
                                    ? descriptor.getSetter()
                                    : DescriptorFactory.createDefaultSetter(descriptor, Annotations.Companion.getEMPTY()));
    }

    private void generateAccessor(
            @Nullable KtNamedDeclaration p,
            @Nullable KtPropertyAccessor accessor,
            @NotNull PropertyAccessorDescriptor accessorDescriptor
    ) {
        if (context instanceof MultifileClassFacadeContext && Visibilities.isPrivate(accessorDescriptor.getVisibility())) {
            return;
        }

        FunctionGenerationStrategy strategy;
        if (accessor == null || !accessor.hasBody()) {
            if (p instanceof KtProperty && ((KtProperty) p).hasDelegate()) {
                strategy = new DelegatedPropertyAccessorStrategy(state, accessorDescriptor, indexOfDelegatedProperty((KtProperty) p));
            }
            else {
                strategy = new DefaultPropertyAccessorStrategy(state, accessorDescriptor);
            }
        }
        else {
            strategy = new FunctionGenerationStrategy.FunctionDefault(state, accessorDescriptor, accessor);
        }

        functionCodegen.generateMethod(JvmDeclarationOriginKt.OtherOrigin(accessor != null ? accessor : p, accessorDescriptor), accessorDescriptor, strategy);
    }

    public static int indexOfDelegatedProperty(@NotNull KtProperty property) {
        PsiElement parent = property.getParent();
        KtDeclarationContainer container;
        if (parent instanceof KtClassBody) {
            container = ((KtClassOrObject) parent.getParent());
        }
        else if (parent instanceof KtFile) {
            container = (KtFile) parent;
        }
        else {
            throw new UnsupportedOperationException("Unknown delegated property container: " + parent);
        }

        int index = 0;
        for (KtDeclaration declaration : container.getDeclarations()) {
            if (declaration instanceof KtProperty && ((KtProperty) declaration).hasDelegate()) {
                if (declaration == property) {
                    return index;
                }
                index++;
            }
        }

        throw new IllegalStateException("Delegated property not found in its parent: " + PsiUtilsKt.getElementTextWithContext(property));
    }


    private static class DefaultPropertyAccessorStrategy extends FunctionGenerationStrategy.CodegenBased<PropertyAccessorDescriptor> {
        public DefaultPropertyAccessorStrategy(@NotNull GenerationState state, @NotNull PropertyAccessorDescriptor descriptor) {
            super(state, descriptor);
        }

        @Override
        public void doGenerateBody(@NotNull ExpressionCodegen codegen, @NotNull JvmMethodSignature signature) {
            InstructionAdapter v = codegen.v;
            PropertyDescriptor propertyDescriptor = callableDescriptor.getCorrespondingProperty();
            StackValue property = codegen.intermediateValueForProperty(propertyDescriptor, true, null, StackValue.LOCAL_0);

            PsiElement jetProperty = DescriptorToSourceUtils.descriptorToDeclaration(propertyDescriptor);
            if (jetProperty instanceof KtProperty || jetProperty instanceof KtParameter) {
                codegen.markLineNumber((KtElement) jetProperty, false);
            }

            if (callableDescriptor instanceof PropertyGetterDescriptor) {
                Type type = signature.getReturnType();
                property.put(type, v);
                v.areturn(type);
            }
            else if (callableDescriptor instanceof PropertySetterDescriptor) {
                List<ValueParameterDescriptor> valueParameters = callableDescriptor.getValueParameters();
                assert valueParameters.size() == 1 : "Property setter should have only one value parameter but has " + callableDescriptor;
                int parameterIndex = codegen.lookupLocalIndex(valueParameters.get(0));
                assert parameterIndex >= 0 : "Local index for setter parameter should be positive or zero: " + callableDescriptor;
                Type type = codegen.typeMapper.mapType(propertyDescriptor);
                property.store(StackValue.local(parameterIndex, type), codegen.v);
                v.visitInsn(RETURN);
            }
            else {
                throw new IllegalStateException("Unknown property accessor: " + callableDescriptor);
            }
        }
    }

    public static StackValue invokeDelegatedPropertyConventionMethod(
            @NotNull PropertyDescriptor propertyDescriptor,
            @NotNull ExpressionCodegen codegen,
            @NotNull JetTypeMapper typeMapper,
            @NotNull ResolvedCall<FunctionDescriptor> resolvedCall,
            final int indexInPropertyMetadataArray,
            int propertyMetadataArgumentIndex
    ) {
        CodegenContext<? extends ClassOrPackageFragmentDescriptor> ownerContext = codegen.getContext().getClassOrPackageParentContext();
        final Type owner;
        if (ownerContext instanceof ClassContext) {
            owner = typeMapper.mapClass(((ClassContext) ownerContext).getContextDescriptor());
        }
        else if (ownerContext instanceof PackageContext) {
            owner = ((PackageContext) ownerContext).getPackagePartType();
        }
        else if (ownerContext instanceof MultifileClassContextBase) {
            owner = ((MultifileClassContextBase) ownerContext).getFilePartType();
        }
        else {
            throw new UnsupportedOperationException("Unknown context: " + ownerContext);
        }

        codegen.tempVariables.put(
                resolvedCall.getCall().getValueArguments().get(propertyMetadataArgumentIndex).asElement(),
                new StackValue(PROPERTY_METADATA_TYPE) {
                    @Override
                    public void putSelector(@NotNull Type type, @NotNull InstructionAdapter v) {
                        Field array = StackValue
                                .field(Type.getType("[" + PROPERTY_METADATA_TYPE), owner, JvmAbi.DELEGATED_PROPERTIES_ARRAY_NAME, true,
                                       StackValue.none());
                        StackValue.arrayElement(PROPERTY_METADATA_TYPE, array, StackValue.constant(indexInPropertyMetadataArray, Type.INT_TYPE)).put(type, v);
                    }
                }
        );

        StackValue delegatedProperty = codegen.intermediateValueForProperty(propertyDescriptor, true, null, StackValue.LOCAL_0);
        return codegen.invokeFunction(resolvedCall, delegatedProperty);
    }

    private static class DelegatedPropertyAccessorStrategy extends FunctionGenerationStrategy.CodegenBased<PropertyAccessorDescriptor> {
        private final int index;

        public DelegatedPropertyAccessorStrategy(@NotNull GenerationState state, @NotNull PropertyAccessorDescriptor descriptor, int index) {
            super(state, descriptor);
            this.index = index;
        }

        @Override
        public void doGenerateBody(@NotNull ExpressionCodegen codegen, @NotNull JvmMethodSignature signature) {
            InstructionAdapter v = codegen.v;

            BindingContext bindingContext = state.getBindingContext();
            ResolvedCall<FunctionDescriptor> resolvedCall =
                    bindingContext.get(BindingContext.DELEGATED_PROPERTY_RESOLVED_CALL, callableDescriptor);
            assert resolvedCall != null : "Resolve call should be recorded for delegate call " + signature.toString();

            StackValue lastValue = invokeDelegatedPropertyConventionMethod(callableDescriptor.getCorrespondingProperty(),
                                                                           codegen, state.getTypeMapper(), resolvedCall, index, 1);
            Type asmType = signature.getReturnType();
            lastValue.put(asmType, v);
            v.areturn(asmType);
        }
    }

    public void genDelegate(@NotNull PropertyDescriptor delegate, @NotNull PropertyDescriptor delegateTo, @NotNull StackValue field) {
        ClassDescriptor toClass = (ClassDescriptor) delegateTo.getContainingDeclaration();

        PropertyGetterDescriptor getter = delegate.getGetter();
        if (getter != null) {
            //noinspection ConstantConditions
            functionCodegen.genDelegate(getter, delegateTo.getGetter().getOriginal(), toClass, field);
        }

        PropertySetterDescriptor setter = delegate.getSetter();
        if (setter != null) {
            //noinspection ConstantConditions
            functionCodegen.genDelegate(setter, delegateTo.getSetter().getOriginal(), toClass, field);
        }
    }
}
