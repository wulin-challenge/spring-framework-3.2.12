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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link PropertyValues} interface.
 * Allows simple manipulation of properties, and provides constructors
 * to support deep copy and construction from a Map.
 * 
 * <p> PropertyValues接口的默认实现。 允许对属性进行简单操作，并提供构造函数以支持Map的深度复制和构造。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 13 May 2001
 */
@SuppressWarnings("serial")
public class MutablePropertyValues implements PropertyValues, Serializable {

	private final List<PropertyValue> propertyValueList;

	private Set<String> processedProperties;

	private volatile boolean converted = false;


	/**
	 * Creates a new empty MutablePropertyValues object.
	 * 
	 * <p> 创建一个新的空MutablePropertyValues对象。
	 * 
	 * <p>Property values can be added with the {@code add} method.
	 * 
	 * <p> 可以使用add方法添加属性值。
	 * 
	 * @see #add(String, Object)
	 */
	public MutablePropertyValues() {
		this.propertyValueList = new ArrayList<PropertyValue>(0);
	}

	/**
	 * Deep copy constructor. Guarantees PropertyValue references
	 * are independent, although it can't deep copy objects currently
	 * referenced by individual PropertyValue objects.
	 * 
	 * <p> 深拷贝构造函数。 保证PropertyValue引用是独立的，但它不能深度复制当前由各个PropertyValue对象引用的对象。
	 * 
	 * @param original the PropertyValues to copy - 要复制的PropertyValues
	 * @see #addPropertyValues(PropertyValues)
	 */
	public MutablePropertyValues(PropertyValues original) {
		// We can optimize this because it's all new:
		// There is no replacement of existing property values.
		
		// 我们可以优化它，因为它是全新的：没有替换现有的属性值。
		if (original != null) {
			PropertyValue[] pvs = original.getPropertyValues();
			this.propertyValueList = new ArrayList<PropertyValue>(pvs.length);
			for (PropertyValue pv : pvs) {
				this.propertyValueList.add(new PropertyValue(pv));
			}
		}
		else {
			this.propertyValueList = new ArrayList<PropertyValue>(0);
		}
	}

	/**
	 * Construct a new MutablePropertyValues object from a Map.
	 * 
	 * <p> 从Map构造一个新的MutablePropertyValues对象。
	 * 
	 * @param original Map with property values keyed by property name Strings
	 * 
	 * <p> 使用属性名称字符串键入的属性值映射
	 * 
	 * @see #addPropertyValues(Map)
	 */
	public MutablePropertyValues(Map<?, ?> original) {
		// We can optimize this because it's all new:
		// There is no replacement of existing property values.
		if (original != null) {
			this.propertyValueList = new ArrayList<PropertyValue>(original.size());
			for (Map.Entry entry : original.entrySet()) {
				this.propertyValueList.add(new PropertyValue(entry.getKey().toString(), entry.getValue()));
			}
		}
		else {
			this.propertyValueList = new ArrayList<PropertyValue>(0);
		}
	}

	/**
	 * Construct a new MutablePropertyValues object using the given List of
	 * PropertyValue objects as-is.
	 * 
	 * <p> 使用给定的PropertyValue对象List构造一个新的MutablePropertyValues对象。
	 * 
	 * <p>This is a constructor for advanced usage scenarios.
	 * It is not intended for typical programmatic use.
	 * 
	 * <p> 这是高级使用方案的构造函数。 它不适用于典型的程序化使用。
	 * 
	 * @param propertyValueList List of PropertyValue objects
	 * 
	 * <p> PropertyValue对象列表
	 * 
	 */
	public MutablePropertyValues(List<PropertyValue> propertyValueList) {
		this.propertyValueList =
				(propertyValueList != null ? propertyValueList : new ArrayList<PropertyValue>());
	}


	/**
	 * Return the underlying List of PropertyValue objects in its raw form.
	 * The returned List can be modified directly, although this is not recommended.
	 * 
	 * <p> 以原始形式返回PropertyValue对象的基础List。 返回的List可以直接修改，但不建议这样做。
	 * 
	 * <p>This is an accessor for optimized access to all PropertyValue objects.
	 * It is not intended for typical programmatic use.
	 * 
	 * <p> 这是对所有PropertyValue对象进行优化访问的访问器。 它不适用于典型的程序化使用。
	 * 
	 */
	public List<PropertyValue> getPropertyValueList() {
		return this.propertyValueList;
	}

	/**
	 * Return the number of PropertyValue entries in the list.
	 * 
	 * <p> 返回列表中PropertyValue条目的数量。
	 * 
	 */
	public int size() {
		return this.propertyValueList.size();
	}

	/**
	 * Copy all given PropertyValues into this object. Guarantees PropertyValue
	 * references are independent, although it can't deep copy objects currently
	 * referenced by individual PropertyValue objects.
	 * 
	 * <p> 将所有给定的PropertyValues复制到此对象中。 保证PropertyValue引用是独立的，
	 * 但它不能深度复制当前由各个PropertyValue对象引用的对象。
	 * 
	 * @param other the PropertyValues to copy - 要复制的PropertyValues
	 * @return this in order to allow for adding multiple property values in a chain
	 * 
	 * <p> 这是为了允许在链中添加多个属性值
	 * 
	 */
	public MutablePropertyValues addPropertyValues(PropertyValues other) {
		if (other != null) {
			PropertyValue[] pvs = other.getPropertyValues();
			for (PropertyValue pv : pvs) {
				addPropertyValue(new PropertyValue(pv));
			}
		}
		return this;
	}

	/**
	 * Add all property values from the given Map.
	 * 
	 * <p> 添加给定Map中的所有属性值。
	 * 
	 * @param other Map with property values keyed by property name,
	 * which must be a String
	 * 
	 * <p> 使用属性名称键入的属性值映射，该属性值必须是String
	 * 
	 * @return this in order to allow for adding multiple property values in a chain
	 * 
	 * <p> 这是为了允许在链中添加多个属性值
	 * 
	 */
	public MutablePropertyValues addPropertyValues(Map<?, ?> other) {
		if (other != null) {
			for (Map.Entry<?, ?> entry : other.entrySet()) {
				addPropertyValue(new PropertyValue(entry.getKey().toString(), entry.getValue()));
			}
		}
		return this;
	}

	/**
	 * Add a PropertyValue object, replacing any existing one for the
	 * corresponding property or getting merged with it (if applicable).
	 * 
	 * <p> 添加PropertyValue对象，替换相应属性的任何现有对象或与其合并（如果适用）。
	 * 
	 * @param pv PropertyValue object to add - 要添加的PropertyValue对象
	 * @return this in order to allow for adding multiple property values in a chain
	 * 
	 * <p> 这是为了允许在链中添加多个属性值
	 * 
	 */
	public MutablePropertyValues addPropertyValue(PropertyValue pv) {
		for (int i = 0; i < this.propertyValueList.size(); i++) {
			PropertyValue currentPv = this.propertyValueList.get(i);
			if (currentPv.getName().equals(pv.getName())) {
				pv = mergeIfRequired(pv, currentPv);
				setPropertyValueAt(pv, i);
				return this;
			}
		}
		this.propertyValueList.add(pv);
		return this;
	}

	/**
	 * Overloaded version of {@code addPropertyValue} that takes
	 * a property name and a property value.
	 * 
	 * <p> addPropertyValue的重载版本，它接受属性名称和属性值。
	 * 
	 * <p>Note: As of Spring 3.0, we recommend using the more concise
	 * and chaining-capable variant {@link #add}.
	 * 
	 * <p> 注意：从Spring 3.0开始，我们建议使用更简洁且具有链接功能的变体添加。
	 * 
	 * @param propertyName name of the property - 属性名称
	 * @param propertyValue value of the property - 属性值
	 * @see #addPropertyValue(PropertyValue)
	 */
	public void addPropertyValue(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
	}

	/**
	 * Add a PropertyValue object, replacing any existing one for the
	 * corresponding property or getting merged with it (if applicable).
	 * 
	 * <p> 添加PropertyValue对象，替换相应属性的任何现有对象或与其合并（如果适用）。
	 * 
	 * @param propertyName name of the property - 属性名称
	 * @param propertyValue value of the property - 属性值
	 * @return this in order to allow for adding multiple property values in a chain
	 * 
	 * <p> 这是为了允许在链中添加多个属性值
	 * 
	 */
	public MutablePropertyValues add(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
		return this;
	}

	/**
	 * Modify a PropertyValue object held in this object.
	 * Indexed from 0.
	 * 
	 * <p> 修改此对象中保存的PropertyValue对象。 从0开始索引。
	 */
	public void setPropertyValueAt(PropertyValue pv, int i) {
		this.propertyValueList.set(i, pv);
	}

	/**
	 * Merges the value of the supplied 'new' {@link PropertyValue} with that of
	 * the current {@link PropertyValue} if merging is supported and enabled.
	 * 
	 * <p> 如果支持并启用合并，则将提供的“新”PropertyValue的值与当前PropertyValue的值合并。
	 * 
	 * @see Mergeable
	 */
	private PropertyValue mergeIfRequired(PropertyValue newPv, PropertyValue currentPv) {
		Object value = newPv.getValue();
		if (value instanceof Mergeable) {
			Mergeable mergeable = (Mergeable) value;
			if (mergeable.isMergeEnabled()) {
				Object merged = mergeable.merge(currentPv.getValue());
				return new PropertyValue(newPv.getName(), merged);
			}
		}
		return newPv;
	}

	/**
	 * Remove the given PropertyValue, if contained.
	 * 
	 * <p> 删除给定的PropertyValue（如果包含）。
	 * 
	 * @param pv the PropertyValue to remove - 要删除的PropertyValue
	 */
	public void removePropertyValue(PropertyValue pv) {
		this.propertyValueList.remove(pv);
	}

	/**
	 * Overloaded version of {@code removePropertyValue} that takes a property name.
	 * 
	 * <p> 带有属性名称的removePropertyValue的重载版本。
	 * @param propertyName name of the property - 属性名称
	 * @see #removePropertyValue(PropertyValue)
	 */
	public void removePropertyValue(String propertyName) {
		this.propertyValueList.remove(getPropertyValue(propertyName));
	}


	public PropertyValue[] getPropertyValues() {
		return this.propertyValueList.toArray(new PropertyValue[this.propertyValueList.size()]);
	}

	public PropertyValue getPropertyValue(String propertyName) {
		for (PropertyValue pv : this.propertyValueList) {
			if (pv.getName().equals(propertyName)) {
				return pv;
			}
		}
		return null;
	}

	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		if (old == this) {
			return changes;
		}

		// for each property value in the new set
		for (PropertyValue newPv : this.propertyValueList) {
			// if there wasn't an old one, add it
			PropertyValue pvOld = old.getPropertyValue(newPv.getName());
			if (pvOld == null) {
				changes.addPropertyValue(newPv);
			}
			else if (!pvOld.equals(newPv)) {
				// it's changed
				changes.addPropertyValue(newPv);
			}
		}
		return changes;
	}

	public boolean contains(String propertyName) {
		return (getPropertyValue(propertyName) != null ||
				(this.processedProperties != null && this.processedProperties.contains(propertyName)));
	}

	public boolean isEmpty() {
		return this.propertyValueList.isEmpty();
	}


	/**
	 * Register the specified property as "processed" in the sense
	 * of some processor calling the corresponding setter method
	 * outside of the PropertyValue(s) mechanism.
	 * 
	 * <p> 在某些处理器的意义上将指定的属性注册为“已处理”，并在PropertyValue（s）机制之外调用相应的setter方法。
	 * 
	 * <p>This will lead to {@code true} being returned from
	 * a {@link #contains} call for the specified property.
	 * 
	 * <p> 这将导致从指定属性的包含调用返回true。
	 * 
	 * @param propertyName the name of the property.
	 */
	public void registerProcessedProperty(String propertyName) {
		if (this.processedProperties == null) {
			this.processedProperties = new HashSet<String>();
		}
		this.processedProperties.add(propertyName);
	}

	/**
	 * Mark this holder as containing converted values only
	 * (i.e. no runtime resolution needed anymore).
	 * 
	 * <p> 将此持有者标记为仅包含转换值（即不再需要运行时分辨率）。
	 * 
	 */
	public void setConverted() {
		this.converted = true;
	}

	/**
	 * Return whether this holder contains converted values only ({@code true}),
	 * or whether the values still need to be converted ({@code false}).
	 * 
	 * <p> 返回此持有者是否仅包含转换值（true），或者是否仍需要转换值（false）。
	 */
	public boolean isConverted() {
		return this.converted;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MutablePropertyValues)) {
			return false;
		}
		MutablePropertyValues that = (MutablePropertyValues) other;
		return this.propertyValueList.equals(that.propertyValueList);
	}

	@Override
	public int hashCode() {
		return this.propertyValueList.hashCode();
	}

	@Override
	public String toString() {
		PropertyValue[] pvs = getPropertyValues();
		StringBuilder sb = new StringBuilder("PropertyValues: length=").append(pvs.length);
		if (pvs.length > 0) {
			sb.append("; ").append(StringUtils.arrayToDelimitedString(pvs, "; "));
		}
		return sb.toString();
	}

}
