/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Object to hold information and value for an individual bean property.
 * Using an object here, rather than just storing all properties in
 * a map keyed by property name, allows for more flexibility, and the
 * ability to handle indexed properties etc in an optimized way.
 * 
 * <p> 用于保存单个bean属性的信息和值的对象。 在此处使用对象，而不是仅将所有属性存储在由属性名称键入的
 * 映射中，允许更灵活，并且能够以优化的方式处理索引属性等。
 *
 * <p>Note that the value doesn't need to be the final required type:
 * A {@link BeanWrapper} implementation should handle any necessary conversion,
 * as this object doesn't know anything about the objects it will be applied to.
 * 
 * <p> 请注意，该值不需要是最终所需的类型：BeanWrapper实现应该处理任何必要的转换，因为此对象不知道它将应用于哪些对象。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 13 May 2001
 * @see PropertyValues
 * @see BeanWrapper
 */
@SuppressWarnings("serial")
public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {

	private final String name;

	private final Object value;

	private Object source;

	private boolean optional = false;

	private boolean converted = false;

	private Object convertedValue;

	/** Package-visible field that indicates whether conversion is necessary */
	/** 包可见字段，指示是否需要转换 */
	volatile Boolean conversionNecessary;

	/** Package-visible field for caching the resolved property path tokens */
	/** 包可见字段，用于缓存已解析的属性路径标记 */
	volatile Object resolvedTokens;

	/** Package-visible field for caching the resolved PropertyDescriptor */
	/** 用于缓存已解析的PropertyDescriptor的包可见字段 */
	volatile PropertyDescriptor resolvedDescriptor;


	/**
	 * Create a new PropertyValue instance.
	 * 
	 * <p> 创建一个新的PropertyValue实例。
	 * 
	 * @param name the name of the property (never {@code null}) - 属性的名称（永远不为null）
	 * @param value the value of the property (possibly before type conversion) - 属性的值（可能在类型转换之前）
	 */
	public PropertyValue(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Copy constructor. - 复制构造函数。
	 * @param original the PropertyValue to copy (never {@code null})
	 * 
	 * <p> 要复制的PropertyValue（从不为null）
	 * 
	 */
	public PropertyValue(PropertyValue original) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = original.getValue();
		this.source = original.getSource();
		this.optional = original.isOptional();
		this.converted = original.converted;
		this.convertedValue = original.convertedValue;
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		this.resolvedDescriptor = original.resolvedDescriptor;
		copyAttributesFrom(original);
	}

	/**
	 * Constructor that exposes a new value for an original value holder.
	 * The original holder will be exposed as source of the new holder.
	 * 
	 * <p> 为原始值持有者公开新值的构造方法。 原始持有人将作为新持有人的来源。
	 * 
	 * @param original the PropertyValue to link to (never {@code null})
	 * 
	 * <p> 要链接到的PropertyValue（从不为null）
	 * 
	 * @param newValue the new value to apply - 要应用的新价值
	 */
	public PropertyValue(PropertyValue original, Object newValue) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = newValue;
		this.source = original;
		this.optional = original.isOptional();
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		this.resolvedDescriptor = original.resolvedDescriptor;
		copyAttributesFrom(original);
	}


	/**
	 * Return the name of the property.
	 * 
	 * <p> 返回属性的名称。
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return the value of the property.
	 * 
	 * <p> 返回属性的值。
	 * 
	 * <p>Note that type conversion will <i>not</i> have occurred here.
	 * It is the responsibility of the BeanWrapper implementation to
	 * perform type conversion.
	 * 
	 * <p> 请注意，此处不会发生类型转换。 BeanWrapper实现负责执行类型转换。
	 */
	public Object getValue() {
		return this.value;
	}

	/**
	 * Return the original PropertyValue instance for this value holder.
	 * 
	 * <p> 返回此值持有者的原始PropertyValue实例。
	 * 
	 * @return the original PropertyValue (either a source of this
	 * value holder or this value holder itself).
	 * 
	 * <p> 原始PropertyValue（此值持有者的来源或此值持有者本身）。
	 * 
	 */
	public PropertyValue getOriginalPropertyValue() {
		PropertyValue original = this;
		while (original.source instanceof PropertyValue && original.source != original) {
			original = (PropertyValue) original.source;
		}
		return original;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public boolean isOptional() {
		return this.optional;
	}

	/**
	 * Return whether this holder contains a converted value already ({@code true}),
	 * or whether the value still needs to be converted ({@code false}).
	 * 
	 * <p> 返回此持有者是否已包含已转换的值（true），或者是否仍需要转换该值（false）。
	 * 
	 */
	public synchronized boolean isConverted() {
		return this.converted;
	}

	/**
	 * Set the converted value of the constructor argument,
	 * after processed type conversion.
	 * 
	 * <p> 在处理的类型转换后，设置构造函数参数的转换值。
	 * 
	 */
	public synchronized void setConvertedValue(Object value) {
		this.converted = true;
		this.convertedValue = value;
	}

	/**
	 * Return the converted value of the constructor argument,
	 * after processed type conversion.
	 * 
	 * <p> 处理后的类型转换后，返回构造函数参数的转换值。
	 * 
	 */
	public synchronized Object getConvertedValue() {
		return this.convertedValue;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PropertyValue)) {
			return false;
		}
		PropertyValue otherPv = (PropertyValue) other;
		return (this.name.equals(otherPv.name) &&
				ObjectUtils.nullSafeEquals(this.value, otherPv.value) &&
				ObjectUtils.nullSafeEquals(this.source, otherPv.source));
	}

	@Override
	public int hashCode() {
		return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
	}

	@Override
	public String toString() {
		return "bean property '" + this.name + "'";
	}

}
