/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Holder for a typed String value. Can be added to bean definitions
 * in order to explicitly specify a target type for a String value,
 * for example for collection elements.
 * 
 * <p> 持有者为类型化的字符串值。 可以添加到bean定义中，以便为String值显式指定目标类型，例如对于集合元素。
 *
 * <p>This holder will just store the String value and the target type.
 * The actual conversion will be performed by the bean factory.
 * 
 * <p> 该持有者将只存储String值和目标类型。 实际的转换将由bean工厂执行。
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see BeanDefinition#getPropertyValues
 * @see org.springframework.beans.MutablePropertyValues#addPropertyValue
 */
public class TypedStringValue implements BeanMetadataElement {

	private String value;

	private volatile Object targetType;

	private Object source;

	private String specifiedTypeName;

	private volatile boolean dynamic;


	/**
	 * Create a new {@link TypedStringValue} for the given String value.
	 * 
	 * <p> 为给定的String值创建一个新的TypedStringValue。
	 * 
	 * @param value the String value
	 */
	public TypedStringValue(String value) {
		setValue(value);
	}

	/**
	 * Create a new {@link TypedStringValue} for the given String value
	 * and target type.
	 * 
	 * <p> 为给定的String值和目标类型创建一个新的TypedStringValue。
	 * 
	 * @param value the String value - String值
	 * @param targetType the type to convert to - 要转换为的类型
	 */
	public TypedStringValue(String value, Class<?> targetType) {
		setValue(value);
		setTargetType(targetType);
	}

	/**
	 * Create a new {@link TypedStringValue} for the given String value
	 * and target type.
	 * 
	 * <p> 为给定的String值和目标类型创建一个新的TypedStringValue。
	 * @param value the String value - String值
	 * @param targetTypeName the type to convert to - 要转换为的类型
	 */
	public TypedStringValue(String value, String targetTypeName) {
		setValue(value);
		setTargetTypeName(targetTypeName);
	}


	/**
	 * Set the String value. - 设置String值。
	 * <p>Only necessary for manipulating a registered value,
	 * for example in BeanFactoryPostProcessors.
	 * 
	 * <p> 只有在操作注册值时才需要，例如在BeanFactoryPostProcessors中。
	 * 
	 * @see PropertyPlaceholderConfigurer
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Return the String value.
	 * 
	 * <p> 返回String值。
	 * 
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Set the type to convert to.
	 * 
	 * <p> 设置要转换为的类型。
	 * 
	 * <p>Only necessary for manipulating a registered value,
	 * for example in BeanFactoryPostProcessors.
	 * 
	 * <p> 只有在操作注册值时才需要，例如在BeanFactoryPostProcessors中。
	 * 
	 * @see PropertyPlaceholderConfigurer
	 */
	public void setTargetType(Class<?> targetType) {
		Assert.notNull(targetType, "'targetType' must not be null");
		this.targetType = targetType;
	}

	/**
	 * Return the type to convert to.
	 * 
	 * <p> 返回要转换的类型。
	 * 
	 */
	public Class<?> getTargetType() {
		Object targetTypeValue = this.targetType;
		if (!(targetTypeValue instanceof Class)) {
			throw new IllegalStateException("Typed String value does not carry a resolved target type");
		}
		return (Class) targetTypeValue;
	}

	/**
	 * Specify the type to convert to.
	 * 
	 * <p> 指定要转换的类型。
	 */
	public void setTargetTypeName(String targetTypeName) {
		Assert.notNull(targetTypeName, "'targetTypeName' must not be null");
		this.targetType = targetTypeName;
	}

	/**
	 * Return the type to convert to.
	 * 
	 * <p> 返回要转换的类型。
	 * 
	 */
	public String getTargetTypeName() {
		Object targetTypeValue = this.targetType;
		if (targetTypeValue instanceof Class) {
			return ((Class) targetTypeValue).getName();
		}
		else {
			return (String) targetTypeValue;
		}
	}

	/**
	 * Return whether this typed String value carries a target type .
	 * 
	 * <p> 返回此类型的String值是否包含目标类型。
	 * 
	 */
	public boolean hasTargetType() {
		return (this.targetType instanceof Class);
	}

	/**
	 * Determine the type to convert to, resolving it from a specified class name
	 * if necessary. Will also reload a specified Class from its name when called
	 * with the target type already resolved.
	 * 
	 * <p> 确定要转换的类型，必要时从指定的类名解析它。 在使用已解析的目标类型调用时，还将从其名称重新加载指定的Class。
	 * 
	 * @param classLoader the ClassLoader to use for resolving a (potential) class name
	 * 
	 * <p> 用于解析（潜在）类名的ClassLoader
	 * 
	 * @return the resolved type to convert to - 要转换为的已解析类型
	 * @throws ClassNotFoundException if the type cannot be resolved - 如果类型无法解决
	 */
	public Class<?> resolveTargetType(ClassLoader classLoader) throws ClassNotFoundException {
		if (this.targetType == null) {
			return null;
		}
		Class<?> resolvedClass = ClassUtils.forName(getTargetTypeName(), classLoader);
		this.targetType = resolvedClass;
		return resolvedClass;
	}


	/**
	 * Set the configuration source {@code Object} for this metadata element.
	 * 
	 * <p> 为此元数据元素设置配置源Object。
	 * 
	 * <p>The exact type of the object will depend on the configuration mechanism used.
	 * 
	 * <p> 对象的确切类型取决于所使用的配置机制。
	 * 
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return this.source;
	}

	/**
	 * Set the type name as actually specified for this particular value, if any.
	 * 
	 * <p> 设置实际为此特定值指定的类型名称（如果有）。
	 * 
	 */
	public void setSpecifiedTypeName(String specifiedTypeName) {
		this.specifiedTypeName = specifiedTypeName;
	}

	/**
	 * Return the type name as actually specified for this particular value, if any.
	 * 
	 * <p> 返回实际为此特定值指定的类型名称（如果有）。
	 * 
	 */
	public String getSpecifiedTypeName() {
		return this.specifiedTypeName;
	}

	/**
	 * Mark this value as dynamic, i.e. as containing an expression
	 * and hence not being subject to caching.
	 * 
	 * <p> 将此值标记为动态，即包含表达式，因此不受缓存限制。
	 */
	public void setDynamic() {
		this.dynamic = true;
	}

	/**
	 * Return whether this value has been marked as dynamic.
	 * 
	 * <p> 返回此值是否已标记为动态。
	 * 
	 */
	public boolean isDynamic() {
		return this.dynamic;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TypedStringValue)) {
			return false;
		}
		TypedStringValue otherValue = (TypedStringValue) other;
		return (ObjectUtils.nullSafeEquals(this.value, otherValue.value) &&
				ObjectUtils.nullSafeEquals(this.targetType, otherValue.targetType));
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.value) * 29 + ObjectUtils.nullSafeHashCode(this.targetType);
	}

	@Override
	public String toString() {
		return "TypedStringValue: value [" + this.value + "], target type [" + this.targetType + "]";
	}

}
