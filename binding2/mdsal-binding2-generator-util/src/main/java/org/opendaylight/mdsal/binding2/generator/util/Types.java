/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding2.model.api.BaseTypeWithRestrictions;
import org.opendaylight.mdsal.binding2.model.api.ConcreteType;
import org.opendaylight.mdsal.binding2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding2.model.api.Restrictions;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.mdsal.binding2.model.api.WildcardType;
import org.opendaylight.mdsal.binding2.spec.Augmentable;
import org.opendaylight.mdsal.binding2.spec.Augmentation;
import org.opendaylight.mdsal.binding2.spec.RpcCallback;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;

@Beta
public final class Types {
    private static final CacheLoader<Class<?>, ConcreteType> TYPE_LOADER =
            new CacheLoader<Class<?>, ConcreteType>() {

                @Override
                public ConcreteType load(Class<?> key) throws Exception {
                    return new ConcreteTypeImpl(key.getPackage().getName(), key.getSimpleName(), null);
                }
            };

    private static final LoadingCache<Class<?>, ConcreteType> TYPE_CACHE =
            CacheBuilder.newBuilder().weakKeys().build(TYPE_LOADER);

    public static final ConcreteType BOOLEAN = typeForClass(Boolean.class);
    public static final ConcreteType RPC_CALLBACK = typeForClass(RpcCallback.class);
    public static final ConcreteType STRING = typeForClass(String.class);
    public static final ConcreteType VOID = typeForClass(Void.class);
    public static final ConcreteType BYTE_ARRAY = primitiveType("byte[]", null);
    public static final ConcreteType CHAR_ARRAY = primitiveType("char[]", null);

    private static final Splitter DOT_SPLITTER = Splitter.on('.');
    private static final Type SET_TYPE = typeForClass(Set.class);
    private static final Type LIST_TYPE = typeForClass(List.class);
    private static final Type MAP_TYPE = typeForClass(Map.class);

    private Types() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates the instance of type
     * {@link org.opendaylight.mdsal.binding2.model.api.ConcreteType
     * ConcreteType} which represents JAVA <code>void</code> type.
     *
     * @return <code>ConcreteType</code> instance which represents JAVA
     *         <code>void</code>
     */
    public static ConcreteType voidType() {
        return VOID;
    }

    /**
     * Creates the instance of type
     * {@link org.opendaylight.mdsal.binding2.model.api.ConcreteType
     * ConcreteType} which represents primitive JAVA type for which package
     * doesn't exist.
     *
     * @param primitiveType
     *            string containing programmatic construction based on
     *            primitive type (e.g byte[])
     * @return <code>ConcreteType</code> instance which represents programmatic
     *         construction with primitive JAVA type
     */
    public static ConcreteType primitiveType(final String primitiveType, final Restrictions restrictions) {
        return new ConcreteTypeImpl("", primitiveType, restrictions);
    }

    /**
     * Returns an instance of {@link ConcreteType} describing the class
     *
     * @param cls
     *            Class to describe
     * @return Description of class
     */
    public static ConcreteType typeForClass(final Class<?> cls) {
        return TYPE_CACHE.getUnchecked(cls);
    }


    public static ConcreteType typeForClass(final Class<?> cls, final Restrictions restrictions) {
        if (restrictions != null) {
            if (restrictions instanceof DefaultRestrictions) {
                return new ConcreteTypeImpl(cls.getPackage().getName(), cls.getSimpleName(), restrictions);
            } else {
                return new BaseTypeWithRestrictionsImpl(cls.getPackage().getName(), cls.getSimpleName(), restrictions);
            }
        } else {
            return typeForClass(cls);
        }
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed
     * {@link Map}&lt;K,V&gt;
     *
     * @param keyType
     *            Key Type
     * @param valueType
     *            Value Type
     * @return Description of generic type instance
     */
    public static ParameterizedType mapTypeFor(final Type keyType, final Type valueType) {
        return parameterizedTypeFor(MAP_TYPE, keyType, valueType);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed
     * {@link Set}&lt;V&gt; with concrete type of value.
     *
     * @param valueType
     *            Value Type
     * @return Description of generic type instance of Set
     */
    public static ParameterizedType setTypeFor(final Type valueType) {
        return parameterizedTypeFor(SET_TYPE, valueType);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed
     * {@link List}&lt;V&gt; with concrete type of value.
     *
     * @param valueType
     *            Value Type
     * @return Description of type instance of List
     */
    public static ParameterizedType listTypeFor(final Type valueType) {
        return parameterizedTypeFor(LIST_TYPE, valueType);
    }

    /**
     * Creates instance of type
     * {@link org.opendaylight.mdsal.binding2.model.api.ParameterizedType
     * ParameterizedType}
     *
     * @param type
     *            JAVA <code>Type</code> for raw type
     * @param parameters
     *            JAVA <code>Type</code>s for actual parameter types
     * @return <code>ParametrizedType</code> reprezentation of <code>type</code>
     *         and its parameters <code>parameters</code>
     */
    public static ParameterizedType parameterizedTypeFor(final Type type, final Type... parameters) {
        return new ParameterizedTypeImpl(type, parameters);
    }

    /**
     * Creates instance of type
     * {@link org.opendaylight.mdsal.binding2.model.api.WildcardType
     * WildcardType}
     *
     * @param packageName
     *            string with the package name
     * @param typeName
     *            string with the type name
     * @return <code>WildcardType</code> representation of
     *         <code>packageName</code> and <code>typeName</code>
     */
    public static WildcardType wildcardTypeFor(final String packageName, final String typeName) {
        return new WildcardTypeImpl(packageName, typeName);
    }

    /**
     * Creates instance of
     * {@link org.opendaylight.mdsal.binding2.model.api.ParameterizedType
     * ParameterizedType} where raw type is
     * {@link org.opendaylight.mdsal.binding2.spec.Augmentable} and actual
     * parameter is <code>valueType</code>.
     *
     * @param valueType
     *            JAVA <code>Type</code> with actual parameter
     * @return <code>ParametrizedType</code> representation of raw type
     *         <code>Augmentable</code> with actual parameter
     *         <code>valueType</code>
     */
    public static ParameterizedType augmentableTypeFor(final Type valueType) {
        final Type augmentable = typeForClass(Augmentable.class);
        return parameterizedTypeFor(augmentable, valueType);
    }

    /**
     * Creates instance of
     * {@link org.opendaylight.mdsal.binding2.model.api.ParameterizedType
     * ParameterizedType} where raw type is
     * {@link org.opendaylight.mdsal.binding2.spec.Augmentation} and actual
     * parameter is <code>valueType</code>.
     *
     * @param valueType
     *            JAVA <code>Type</code> with actual parameter
     * @return <code>ParametrizedType</code> reprezentation of raw type
     *         <code>Augmentation</code> with actual parameter
     *         <code>valueType</code>
     */
    public static ParameterizedType augmentationTypeFor(final Type valueType) {
        final Type augmentation = typeForClass(Augmentation.class);
        return parameterizedTypeFor(augmentation, valueType);
    }


    public static  @Nullable
    String getOuterClassName(final Type valueType) {
        final String pkgName = valueType.getPackageName();
        if(CharMatcher.JAVA_UPPER_CASE.indexIn(pkgName) >= 0) {
            // It is inner class.
            return Iterables.getLast(DOT_SPLITTER.split(pkgName));
        }
        return null;
    }

    /**
     *
     * Represents concrete JAVA type.
     *
     */
    private static final class ConcreteTypeImpl extends AbstractBaseType implements ConcreteType {

        private final Restrictions restrictions;

        /**
         * Creates instance of this class with package <code>pkName</code> and
         * with the type name <code>name</code>.
         *
         * @param pkName
         *            string with package name
         * @param name
         *            string with the name of the type
         */
        private ConcreteTypeImpl(final String pkName, final String name, final Restrictions restrictions) {
            super(pkName, name);
            this.restrictions = restrictions;
        }

        @Override
        public Restrictions getRestrictions() {
            return restrictions;
        }
    }

    /**
     *
     * Represents concrete JAVA type with changed restriction values.
     *
     */
    private static final class BaseTypeWithRestrictionsImpl extends AbstractBaseType implements BaseTypeWithRestrictions {
        private final Restrictions restrictions;

        /**
         * Creates instance of this class with package <code>pkName</code> and
         * with the type name <code>name</code>.
         *
         * @param pkName
         *            string with package name
         * @param name
         *            string with the name of the type
         */
        private BaseTypeWithRestrictionsImpl(final String pkName, final String name, final Restrictions restrictions) {
            super(pkName, name);
            this.restrictions = Preconditions.checkNotNull(restrictions);
        }

        @Override
        public Restrictions getRestrictions() {
            return restrictions;
        }
    }

    /**
     *
     * Represents parametrized JAVA type.
     *
     */
    private static class ParameterizedTypeImpl extends AbstractBaseType implements ParameterizedType {
        /**
         * Array of JAVA actual type parameters.
         */
        private final Type[] actualTypes;

        /**
         * JAVA raw type (like List, Set, Map...)
         */
        private final Type rawType;

        @Override
        public Type[] getActualTypeArguments() {

            return actualTypes;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        /**
         * Creates instance of this class with concrete rawType and array of
         * actual parameters.
         *
         * @param rawType
         *            JAVA <code>Type</code> for raw type
         * @param actTypes
         *            array of actual parameters
         */
        public ParameterizedTypeImpl(final Type rawType, final Type[] actTypes) {
            super(rawType.getPackageName(), rawType.getName());
            this.rawType = rawType;
            this.actualTypes = actTypes.clone();
        }

    }

    /**
     *
     * Represents JAVA bounded wildcard type.
     *
     */
    private static class WildcardTypeImpl extends AbstractBaseType implements WildcardType {
        /**
         * Creates instance of this class with concrete package and type name.
         *
         * @param packageName
         *            string with the package name
         * @param typeName
         *            string with the name of type
         */
        public WildcardTypeImpl(final String packageName, final String typeName) {
            super(packageName, typeName);
        }
    }

    public static <T extends Number> DefaultRestrictions<T> getDefaultRestrictions(final T min, final T max) {
        return new DefaultRestrictions<>(min, max);
    }

    private static final class DefaultRestrictions<T extends Number> implements Restrictions {
        private final T min;
        private final T max;
        private final List<RangeConstraint> rangeConstraints;

        private DefaultRestrictions(final T min, final T max) {
            this.min = Preconditions.checkNotNull(min);
            this.max = Preconditions.checkNotNull(max);
            this.rangeConstraints = Collections.singletonList(BaseConstraints.newRangeConstraint(min, max, Optional
                    .absent(), Optional.absent()));
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public List<RangeConstraint> getRangeConstraints() {
            return rangeConstraints;
        }

        @Override
        public List<PatternConstraint> getPatternConstraints() {
            return ImmutableList.of();
        }

        @Override
        public List<LengthConstraint> getLengthConstraints() {
            return ImmutableList.of();
        }
    }
}
