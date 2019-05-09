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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Holder for constructor argument values, typically as part of a bean definition.
 * 
 * <p> 持久化构造函数参数值，通常作为bean定义的一部分。
 *
 * <p>Supports values for a specific index in the constructor argument list
 * as well as for generic argument matches by type.
 * 
 * <p>支持构造函数参数列表中特定索引的值以及类型的泛型参数匹配。
 *
 * @author Juergen Hoeller
 * @since 09.11.2003
 * @see BeanDefinition#getConstructorArgumentValues
 */
public class ConstructorArgumentValues {

	private final Map<Integer, ValueHolder> indexedArgumentValues = new LinkedHashMap<Integer, ValueHolder>(0);

	private final List<ValueHolder> genericArgumentValues = new LinkedList<ValueHolder>();


	/**
	 * Create a new empty ConstructorArgumentValues object.
	 * 
	 * <p>创建一个新的空ConstructorArgumentValues对象。
	 * 
	 */
	public ConstructorArgumentValues() {
	}

	/**
	 * Deep copy constructor.
	 * 
	 * <p>深拷贝构造函数。
	 * 
	 * @param original the ConstructorArgumentValues to copy - 要复制的ConstructorArgumentValues
	 */
	public ConstructorArgumentValues(ConstructorArgumentValues original) {
		addArgumentValues(original);
	}


	/**
	 * Copy all given argument values into this object, using separate holder
	 * instances to keep the values independent from the original object.
	 * 
	 * <p>将所有给定的参数值复制到此对象中，使用单独的holder实例使值保持独立于原始对象。
	 * 
	 * <p>Note: Identical ValueHolder instances will only be registered once,
	 * to allow for merging and re-merging of argument value definitions. Distinct
	 * ValueHolder instances carrying the same content are of course allowed.
	 * 
	 * <p>注意：相同的ValueHolder实例只会注册一次，以允许合并和重新合并参数值定义。 当然，允许携带相同内容的不同ValueHolder实例。
	 * 
	 */
	public void addArgumentValues(ConstructorArgumentValues other) {
		if (other != null) {
			for (Map.Entry<Integer, ValueHolder> entry : other.indexedArgumentValues.entrySet()) {
				addOrMergeIndexedArgumentValue(entry.getKey(), entry.getValue().copy());
			}
			for (ValueHolder valueHolder : other.genericArgumentValues) {
				if (!this.genericArgumentValues.contains(valueHolder)) {
					addOrMergeGenericArgumentValue(valueHolder.copy());
				}
			}
		}
	}


	/**
	 * Add an argument value for the given index in the constructor argument list.
	 * 
	 * <p>在构造函数参数列表中为给定索引添加参数值。
	 * 
	 * @param index the index in the constructor argument list - 构造函数参数列表中的索引
	 * @param value the argument value - 参数值
	 */
	public void addIndexedArgumentValue(int index, Object value) {
		addIndexedArgumentValue(index, new ValueHolder(value));
	}

	/**
	 * Add an argument value for the given index in the constructor argument list.
	 * 
	 * <p>在构造函数参数列表中为给定索引添加参数值。
	 * 
	 * @param index the index in the constructor argument list - 构造函数参数列表中的索引
	 * @param value the argument value - 参数值
	 * @param type the type of the constructor argument - 构造函数参数的类型
	 */
	public void addIndexedArgumentValue(int index, Object value, String type) {
		addIndexedArgumentValue(index, new ValueHolder(value, type));
	}

	/**
	 * Add an argument value for the given index in the constructor argument list.
	 * 
	 * <p>在构造函数参数列表中为给定索引添加参数值。
	 * 
	 * @param index the index in the constructor argument list - 构造函数参数列表中的索引
	 * @param newValue the argument value in the form of a ValueHolder - ValueHolder形式的参数值
	 */
	public void addIndexedArgumentValue(int index, ValueHolder newValue) {
		Assert.isTrue(index >= 0, "Index must not be negative");
		Assert.notNull(newValue, "ValueHolder must not be null");
		addOrMergeIndexedArgumentValue(index, newValue);
	}

	/**
	 * Add an argument value for the given index in the constructor argument list,
	 * merging the new value (typically a collection) with the current value
	 * if demanded: see {@link org.springframework.beans.Mergeable}.
	 * 
	 * <p>在构造函数参数列表中为给定索引添加参数值，如果需要，将新值（通常是集合）与当前值合
	 * 并：请参阅org.springframework.beans.Mergeable。
	 * 
	 * @param key the index in the constructor argument list - 构造函数参数列表中的索引
	 * @param newValue the argument value in the form of a ValueHolder - ValueHolder形式的参数值
	 */
	private void addOrMergeIndexedArgumentValue(Integer key, ValueHolder newValue) {
		ValueHolder currentValue = this.indexedArgumentValues.get(key);
		if (currentValue != null && newValue.getValue() instanceof Mergeable) {
			Mergeable mergeable = (Mergeable) newValue.getValue();
			if (mergeable.isMergeEnabled()) {
				newValue.setValue(mergeable.merge(currentValue.getValue()));
			}
		}
		this.indexedArgumentValues.put(key, newValue);
	}

	/**
	 * Check whether an argument value has been registered for the given index.
	 * 
	 * <p>检查是否已为给定索引注册参数值。
	 * 
	 * @param index the index in the constructor argument list - 构造函数参数列表中的索引
	 */
	public boolean hasIndexedArgumentValue(int index) {
		return this.indexedArgumentValues.containsKey(index);
	}

	/**
	 * Get argument value for the given index in the constructor argument list.
	 * 
	 * <p> 获取构造函数参数列表中给定索引的参数值。
	 * 
	 * @param index the index in the constructor argument list - 构造函数参数列表中的索引
	 * @param requiredType the type to match (can be {@code null} to match
	 * untyped values only)
	 * 
	 * <p> 要匹配的类型（可以为null以仅匹配无类型值）
	 * 
	 * @return the ValueHolder for the argument, or {@code null} if none set
	 * 
	 * <p>参数的ValueHolder，如果没有设置则为null
	 * 
	 */
	public ValueHolder getIndexedArgumentValue(int index, Class<?> requiredType) {
		return getIndexedArgumentValue(index, requiredType, null);
	}

	/**
	 * Get argument value for the given index in the constructor argument list.
	 * 
	 * <p> 获取构造函数参数列表中给定索引的参数值。
	 * 
	 * @param index the index in the constructor argument list - 构造函数参数列表中的索引
	 * @param requiredType the type to match (can be {@code null} to match
	 * untyped values only)
	 * 
	 * <p> 要匹配的类型（可以为null以仅匹配无类型值）
	 * 
	 * @param requiredName the type to match (can be {@code null} to match
	 * unnamed values only)
	 * 
	 * <p> 要匹配的类型（可以为null以仅匹配未命名的值）
	 * 
	 * @return the ValueHolder for the argument, or {@code null} if none set
	 * 
	 * <p> 参数的ValueHolder，如果没有设置则为null
	 * 
	 */
	public ValueHolder getIndexedArgumentValue(int index, Class<?> requiredType, String requiredName) {
		Assert.isTrue(index >= 0, "Index must not be negative");
		ValueHolder valueHolder = this.indexedArgumentValues.get(index);
		if (valueHolder != null &&
				(valueHolder.getType() == null ||
						(requiredType != null && ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) &&
				(valueHolder.getName() == null ||
						(requiredName != null && requiredName.equals(valueHolder.getName())))) {
			return valueHolder;
		}
		return null;
	}

	/**
	 * Return the map of indexed argument values.
	 * 
	 * <p> 返回索引参数值的映射。
	 * 
	 * @return unmodifiable Map with Integer index as key and ValueHolder as value
	 * 
	 * <p> 不可修改的Map，其中Integer索引为键，ValueHolder为value
	 * 
	 * @see ValueHolder
	 */
	public Map<Integer, ValueHolder> getIndexedArgumentValues() {
		return Collections.unmodifiableMap(this.indexedArgumentValues);
	}


	/**
	 * Add a generic argument value to be matched by type.
	 * 
	 * <p> 添加要按类型匹配的通用参数值。
	 * 
	 * <p>Note: A single generic argument value will just be used once,
	 * rather than matched multiple times.
	 * 
	 * <p> 注意：单个通用参数值将只使用一次，而不是多次匹配。
	 * 
	 * @param value the argument value - 参数值
	 */
	public void addGenericArgumentValue(Object value) {
		this.genericArgumentValues.add(new ValueHolder(value));
	}

	/**
	 * Add a generic argument value to be matched by type.
	 * 
	 * <p> 添加要按类型匹配的通用参数值。
	 * 
	 * <p>Note: A single generic argument value will just be used once,
	 * rather than matched multiple times.
	 * 
	 * <p> 注意：单个通用参数值将只使用一次，而不是多次匹配。
	 * 
	 * @param value the argument value - 参数值
	 * @param type the type of the constructor argument - 构造函数参数的类型
	 */
	public void addGenericArgumentValue(Object value, String type) {
		this.genericArgumentValues.add(new ValueHolder(value, type));
	}

	/**
	 * Add a generic argument value to be matched by type or name (if available).
	 * 
	 * <p> 添加要通过类型或名称匹配的通用参数值（如果可用）。
	 * 
	 * <p>Note: A single generic argument value will just be used once,
	 * rather than matched multiple times.
	 * 
	 * <p> 注意：单个通用参数值将只使用一次，而不是多次匹配。
	 * 
	 * @param newValue the argument value in the form of a ValueHolder
	 * 
	 * <p> ValueHolder形式的参数值
	 * 
	 * <p>Note: Identical ValueHolder instances will only be registered once,
	 * to allow for merging and re-merging of argument value definitions. Distinct
	 * ValueHolder instances carrying the same content are of course allowed.
	 * 
	 * <p> 注意：相同的ValueHolder实例只会注册一次，以允许合并和重新合并参数值定义。 当然，允许携带相同内容的不同ValueHolder实例。
	 * 
	 */
	public void addGenericArgumentValue(ValueHolder newValue) {
		Assert.notNull(newValue, "ValueHolder must not be null");
		if (!this.genericArgumentValues.contains(newValue)) {
			addOrMergeGenericArgumentValue(newValue);
		}
	}

	/**
	 * Add a generic argument value, merging the new value (typically a collection)
	 * with the current value if demanded: see {@link org.springframework.beans.Mergeable}.
	 * 
	 * <p> 添加通用参数值，如果需要，将新值（通常是集合）与当前值合并：请参阅org.springframework.beans.Mergeable。
	 * 
	 * @param newValue the argument value in the form of a ValueHolder
	 * 
	 * <p> ValueHolder形式的参数值
	 * 
	 */
	private void addOrMergeGenericArgumentValue(ValueHolder newValue) {
		if (newValue.getName() != null) {
			for (Iterator<ValueHolder> it = this.genericArgumentValues.iterator(); it.hasNext();) {
				ValueHolder currentValue = it.next();
				if (newValue.getName().equals(currentValue.getName())) {
					if (newValue.getValue() instanceof Mergeable) {
						Mergeable mergeable = (Mergeable) newValue.getValue();
						if (mergeable.isMergeEnabled()) {
							newValue.setValue(mergeable.merge(currentValue.getValue()));
						}
					}
					it.remove();
				}
			}
		}
		this.genericArgumentValues.add(newValue);
	}

	/**
	 * Look for a generic argument value that matches the given type.
	 * 
	 * <p> 查找与给定类型匹配的泛型参数值。
	 * 
	 * @param requiredType the type to match - 要匹配的类型
	 * @return the ValueHolder for the argument, or {@code null} if none set
	 * 
	 * <p> 参数的ValueHolder，如果没有设置则为null
	 * 
	 */
	public ValueHolder getGenericArgumentValue(Class<?> requiredType) {
		return getGenericArgumentValue(requiredType, null, null);
	}

	/**
	 * Look for a generic argument value that matches the given type.
	 * 
	 * <p> 查找与给定类型匹配的泛型参数值。
	 * 
	 * @param requiredType the type to match - 要匹配的类型
	 * @param requiredName the name to match - 要匹配的名称
	 * @return the ValueHolder for the argument, or {@code null} if none set
	 * 
	 * <p> 参数的ValueHolder，如果没有设置则为null
	 * 
	 */
	public ValueHolder getGenericArgumentValue(Class<?> requiredType, String requiredName) {
		return getGenericArgumentValue(requiredType, requiredName, null);
	}

	/**
	 * Look for the next generic argument value that matches the given type,
	 * ignoring argument values that have already been used in the current
	 * resolution process.
	 * 
	 * <p> 查找与给定类型匹配的下一个泛型参数值，忽略已在当前解析过程中使用的参数值。
	 * 
	 * @param requiredType the type to match (can be {@code null} to find
	 * an arbitrary next generic argument value)
	 * 
	 * <p> 要匹配的类型（可以为null以查找任意下一个通用参数值）
	 * 
	 * @param requiredName the name to match (can be {@code null} to not
	 * match argument values by name)
	 * 
	 * <p> 要匹配的名称（可以为null，以便不按名称匹配参数值）
	 * 
	 * @param usedValueHolders a Set of ValueHolder objects that have already been used
	 * in the current resolution process and should therefore not be returned again
	 * 
	 * <p> 一组已在当前解析过程中使用过的ValueHolder对象，因此不应再次返回
	 * 
	 * @return the ValueHolder for the argument, or {@code null} if none found
	 * 
	 * <p> 参数的ValueHolder，如果没有找到则为null
	 * 
	 */
	public ValueHolder getGenericArgumentValue(Class<?> requiredType, String requiredName, Set<ValueHolder> usedValueHolders) {
		for (ValueHolder valueHolder : this.genericArgumentValues) {
			if (usedValueHolders != null && usedValueHolders.contains(valueHolder)) {
				continue;
			}
			if (valueHolder.getName() != null &&
					(requiredName == null || !valueHolder.getName().equals(requiredName))) {
				continue;
			}
			if (valueHolder.getType() != null &&
					(requiredType == null || !ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) {
				continue;
			}
			if (requiredType != null && valueHolder.getType() == null && valueHolder.getName() == null &&
					!ClassUtils.isAssignableValue(requiredType, valueHolder.getValue())) {
				continue;
			}
			return valueHolder;
		}
		return null;
	}

	/**
	 * Return the list of generic argument values.
	 * 
	 * <p> 返回通用参数值列表。
	 * 
	 * @return unmodifiable List of ValueHolders - 不可修改的ValueHolders列表
	 * @see ValueHolder
	 */
	public List<ValueHolder> getGenericArgumentValues() {
		return Collections.unmodifiableList(this.genericArgumentValues);
	}


	/**
	 * Look for an argument value that either corresponds to the given index
	 * in the constructor argument list or generically matches by type.
	 * 
	 * <p> 查找与构造函数参数列表中的给定索引相对应的参数值，或者按类型进行一般匹配。
	 * 
	 * @param index the index in the constructor argument list - 构造函数参数列表中的索引
	 * @param requiredType the parameter type to match - 要匹配的参数类型
	 * @return the ValueHolder for the argument, or {@code null} if none set
	 * 
	 * <p> 参数的ValueHolder，如果没有设置则为null
	 * 
	 */
	public ValueHolder getArgumentValue(int index, Class<?> requiredType) {
		return getArgumentValue(index, requiredType, null, null);
	}

	/**
	 * Look for an argument value that either corresponds to the given index
	 * in the constructor argument list or generically matches by type.
	 * 
	 * <p> 查找与构造函数参数列表中的给定索引相对应的参数值，或者按类型进行一般匹配。
	 * 
	 * @param index the index in the constructor argument list - 构造函数参数列表中的索引
	 * @param requiredType the parameter type to match - 要匹配的参数类型
	 * @param requiredName the parameter name to match - 要匹配的参数名称
	 * @return the ValueHolder for the argument, or {@code null} if none set
	 * 
	 * <p> 参数的ValueHolder，如果没有设置则为null
	 * 
	 */
	public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName) {
		return getArgumentValue(index, requiredType, requiredName, null);
	}

	/**
	 * Look for an argument value that either corresponds to the given index
	 * in the constructor argument list or generically matches by type.
	 * 
	 * <p> 查找与构造函数参数列表中的给定索引相对应的参数值，或者按类型进行一般匹配。
	 * 
	 * @param index the index in the constructor argument list 
	 * 
	 * <p> 构造函数参数列表中的索引
	 * 
	 * @param requiredType the parameter type to match (can be {@code null}
	 * to find an untyped argument value)
	 * 
	 * <p> 要匹配的参数类型（可以为null以查找无类型参数值）
	 * 
	 * @param requiredName the parameter name to match (can be {@code null}
	 * to find an unnamed argument value)
	 * 
	 * <p> 要匹配的参数名称（可以为null以查找未命名的参数值）
	 * 
	 * @param usedValueHolders a Set of ValueHolder objects that have already
	 * been used in the current resolution process and should therefore not
	 * be returned again (allowing to return the next generic argument match
	 * in case of multiple generic argument values of the same type)
	 * 
	 * <p> 一组已在当前解析过程中使用过的ValueHolder对象，因此不应再次返回（允许在多个相同类型的通用参数值的情况下返回下一个通用参数匹配）
	 * 
	 * @return the ValueHolder for the argument, or {@code null} if none set
	 * 
	 * <p> 参数的ValueHolder，如果没有设置则为null
	 * 
	 */
	public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName, Set<ValueHolder> usedValueHolders) {
		Assert.isTrue(index >= 0, "Index must not be negative");
		ValueHolder valueHolder = getIndexedArgumentValue(index, requiredType, requiredName);
		if (valueHolder == null) {
			valueHolder = getGenericArgumentValue(requiredType, requiredName, usedValueHolders);
		}
		return valueHolder;
	}

	/**
	 * Return the number of argument values held in this instance,
	 * counting both indexed and generic argument values.
	 * 
	 * <p> 返回此实例中保存的参数值的数量，计算索引和泛型参数值。
	 * 
	 */
	public int getArgumentCount() {
		return (this.indexedArgumentValues.size() + this.genericArgumentValues.size());
	}

	/**
	 * Return if this holder does not contain any argument values,
	 * neither indexed ones nor generic ones.
	 * 
	 * <p> 如果此持有者不包含任何参数值，既不是索引值也不是通用值，则返回。
	 * 
	 */
	public boolean isEmpty() {
		return (this.indexedArgumentValues.isEmpty() && this.genericArgumentValues.isEmpty());
	}

	/**
	 * Clear this holder, removing all argument values.
	 * 
	 * <p> 清除此持有者，删除所有参数值。
	 * 
	 */
	public void clear() {
		this.indexedArgumentValues.clear();
		this.genericArgumentValues.clear();
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ConstructorArgumentValues)) {
			return false;
		}
		ConstructorArgumentValues that = (ConstructorArgumentValues) other;
		if (this.genericArgumentValues.size() != that.genericArgumentValues.size() ||
				this.indexedArgumentValues.size() != that.indexedArgumentValues.size()) {
			return false;
		}
		Iterator<ValueHolder> it1 = this.genericArgumentValues.iterator();
		Iterator<ValueHolder> it2 = that.genericArgumentValues.iterator();
		while (it1.hasNext() && it2.hasNext()) {
			ValueHolder vh1 = it1.next();
			ValueHolder vh2 = it2.next();
			if (!vh1.contentEquals(vh2)) {
				return false;
			}
		}
		for (Map.Entry<Integer, ValueHolder> entry : this.indexedArgumentValues.entrySet()) {
			ValueHolder vh1 = entry.getValue();
			ValueHolder vh2 = that.indexedArgumentValues.get(entry.getKey());
			if (!vh1.contentEquals(vh2)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 7;
		for (ValueHolder valueHolder : this.genericArgumentValues) {
			hashCode = 31 * hashCode + valueHolder.contentHashCode();
		}
		hashCode = 29 * hashCode;
		for (Map.Entry<Integer, ValueHolder> entry : this.indexedArgumentValues.entrySet()) {
			hashCode = 31 * hashCode + (entry.getValue().contentHashCode() ^ entry.getKey().hashCode());
		}
		return hashCode;
	}


	/**
	 * Holder for a constructor argument value, with an optional type
	 * attribute indicating the target type of the actual constructor argument.
	 * 
	 * <p> 持久化构造函数参数值，带有可选的type属性，指示实际构造函数参数的目标类型。
	 * 
	 */
	public static class ValueHolder implements BeanMetadataElement {

		private Object value;

		private String type;

		private String name;

		private Object source;

		private boolean converted = false;

		private Object convertedValue;

		/**
		 * Create a new ValueHolder for the given value.
		 * 
		 * <p> 为给定值创建一个新的ValueHolder。
		 * 
		 * @param value the argument value - 参数值
		 */
		public ValueHolder(Object value) {
			this.value = value;
		}

		/**
		 * Create a new ValueHolder for the given value and type.
		 * 
		 * <p> 为给定的值和类型创建一个新的ValueHolder。
		 * 
		 * @param value the argument value - 参数值
		 * @param type the type of the constructor argument - 构造函数参数的类型
		 */
		public ValueHolder(Object value, String type) {
			this.value = value;
			this.type = type;
		}

		/**
		 * Create a new ValueHolder for the given value, type and name.
		 * 
		 * <p> 为给定的值，类型和名称创建一个新的ValueHolder。
		 * 
		 * @param value the argument value - 参数值
		 * @param type the type of the constructor argument - 构造函数参数的类型
		 * @param name the name of the constructor argument - 构造函数参数的名称
		 */
		public ValueHolder(Object value, String type, String name) {
			this.value = value;
			this.type = type;
			this.name = name;
		}

		/**
		 * Set the value for the constructor argument.
		 * 
		 * <p> 设置构造函数参数的值。
		 * 
		 * @see PropertyPlaceholderConfigurer
		 */
		public void setValue(Object value) {
			this.value = value;
		}

		/**
		 * Return the value for the constructor argument.
		 * 
		 * <p> 返回构造函数参数的值。
		 * 
		 */
		public Object getValue() {
			return this.value;
		}

		/**
		 * Set the type of the constructor argument.
		 * 
		 * <p> 设置构造函数参数的类型。
		 * 
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * Return the type of the constructor argument.
		 * 
		 * <p> 返回构造函数参数的类型。
		 * 
		 */
		public String getType() {
			return this.type;
		}

		/**
		 * Set the name of the constructor argument.
		 * 
		 * <p> 设置构造函数参数的名称。
		 * 
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Return the name of the constructor argument.
		 * 
		 * <p> 返回构造函数参数的名称。
		 * 
		 */
		public String getName() {
			return this.name;
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

		/**
		 * Determine whether the content of this ValueHolder is equal
		 * to the content of the given other ValueHolder.
		 * 
		 * <p> 确定此ValueHolder的内容是否等于给定的其他ValueHolder的内容。
		 * 
		 * <p>Note that ValueHolder does not implement {@code equals}
		 * directly, to allow for multiple ValueHolder instances with the
		 * same content to reside in the same Set.
		 * 
		 * <p> 请注意，ValueHolder不直接实现equals，以允许具有相同内容的多个ValueHolder实例驻留在同一个Set中。
		 * 
		 */
		private boolean contentEquals(ValueHolder other) {
			return (this == other ||
					(ObjectUtils.nullSafeEquals(this.value, other.value) && ObjectUtils.nullSafeEquals(this.type, other.type)));
		}

		/**
		 * Determine whether the hash code of the content of this ValueHolder.
		 * 
		 * <p> 确定此ValueHolder内容的哈希码。
		 * 
		 * <p>Note that ValueHolder does not implement {@code hashCode}
		 * directly, to allow for multiple ValueHolder instances with the
		 * same content to reside in the same Set.
		 * 
		 * <p> 请注意，ValueHolder不直接实现hashCode，以允许具有相同内容的多个ValueHolder实例驻留在同一个Set中。
		 * 
		 */
		private int contentHashCode() {
			return ObjectUtils.nullSafeHashCode(this.value) * 29 + ObjectUtils.nullSafeHashCode(this.type);
		}

		/**
		 * Create a copy of this ValueHolder: that is, an independent
		 * ValueHolder instance with the same contents.
		 * 
		 * <p> 创建此ValueHolder的副本：即具有相同内容的独立ValueHolder实例。
		 * 
		 */
		public ValueHolder copy() {
			ValueHolder copy = new ValueHolder(this.value, this.type, this.name);
			copy.setSource(this.source);
			return copy;
		}
	}

}
