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

package org.springframework.beans.factory.support;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;

/**
 * Tag collection class used to hold managed Map values, which may
 * include runtime bean references (to be resolved into bean objects).
 * 
 * <p> 标记集合类用于保存托管Map值，其中可能包括运行时bean引用（要解析为bean对象）。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 27.05.2003
 */
@SuppressWarnings("serial")
public class ManagedMap<K, V> extends LinkedHashMap<K, V> implements Mergeable, BeanMetadataElement {

	private Object source;

	private String keyTypeName;

	private String valueTypeName;

	private boolean mergeEnabled;


	public ManagedMap() {
	}

	public ManagedMap(int initialCapacity) {
		super(initialCapacity);
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
	 * Set the default key type name (class name) to be used for this map.
	 * 
	 * <p> 设置要用于此映射的默认键类型名称（类名称）。
	 * 
	 */
	public void setKeyTypeName(String keyTypeName) {
		this.keyTypeName = keyTypeName;
	}

	/**
	 * Return the default key type name (class name) to be used for this map.
	 * 
	 * <p> 返回要用于此映射的默认键类型名称（类名）。
	 * 
	 */
	public String getKeyTypeName() {
		return this.keyTypeName;
	}

	/**
	 * Set the default value type name (class name) to be used for this map.
	 * 
	 * <p> 设置要用于此映射的默认值类型名称（类名）。
	 * 
	 */
	public void setValueTypeName(String valueTypeName) {
		this.valueTypeName = valueTypeName;
	}

	/**
	 * Return the default value type name (class name) to be used for this map.
	 * 
	 * <p> 返回要用于此映射的默认值类型名称（类名）。
	 * 
	 */
	public String getValueTypeName() {
		return this.valueTypeName;
	}

	/**
	 * Set whether merging should be enabled for this collection,
	 * in case of a 'parent' collection value being present.
	 * 
	 * <p> 设置是否应为此集合启用合并，以防存在“父”集合值。
	 * 
	 */
	public void setMergeEnabled(boolean mergeEnabled) {
		this.mergeEnabled = mergeEnabled;
	}

	public boolean isMergeEnabled() {
		return this.mergeEnabled;
	}

	@SuppressWarnings("unchecked")
	public Object merge(Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
		}
		if (parent == null) {
			return this;
		}
		if (!(parent instanceof Map)) {
			throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
		}
		Map<K, V> merged = new ManagedMap<K, V>();
		merged.putAll((Map) parent);
		merged.putAll(this);
		return merged;
	}

}
