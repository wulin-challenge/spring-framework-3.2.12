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

package org.springframework.core;

/**
 * Interface defining a generic contract for attaching and accessing metadata
 * to/from arbitrary objects.
 * 
 * <p> 定义用于向/从任意对象附加和访问元数据的通用契约的接口。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public interface AttributeAccessor {

	/**
	 * Set the attribute defined by {@code name} to the supplied	{@code value}.
	 * If {@code value} is {@code null}, the attribute is {@link #removeAttribute removed}.
	 * 
	 * <p> 将name定义的属性设置为提供的值。 如果value为null，则删除该属性。
	 * 
	 * <p>In general, users should take care to prevent overlaps with other
	 * metadata attributes by using fully-qualified names, perhaps using
	 * class or package names as prefix.
	 * 
	 * <p> 通常，用户应注意通过使用完全限定名称来防止与其他元数据属性重叠，可能使用类或包名称作为前缀。
	 * 
	 * @param name the unique attribute key - 唯一属性键
	 * @param value the attribute value to be attached - 要附加的属性值
	 */
	void setAttribute(String name, Object value);

	/**
	 * Get the value of the attribute identified by {@code name}.
	 * Return {@code null} if the attribute doesn't exist.
	 * 
	 * <p> 获取由name标识的属性的值。 如果该属性不存在，则返回null。
	 * 
	 * @param name the unique attribute key - 唯一属性键
	 * @return the current value of the attribute, if any - 属性的当前值（如果有）
	 */
	Object getAttribute(String name);

	/**
	 * Remove the attribute identified by {@code name} and return its value.
	 * Return {@code null} if no attribute under {@code name} is found.
	 * 
	 * <p> 删除由name标识的属性并返回其值。 如果找不到名称下的属性，则返回null。
	 * 
	 * @param name the unique attribute key - 唯一属性键
	 * @return the last value of the attribute, if any - 属性的最后一个值，如果有的话
	 */
	Object removeAttribute(String name);

	/**
	 * Return {@code true} if the attribute identified by {@code name} exists.
	 * Otherwise return {@code false}.
	 * 
	 * <p> 如果名称标识的属性存在，则返回true。 否则返回false。
	 * 
	 * @param name the unique attribute key
	 * 
	 * <p> 唯一属性键
	 * 
	 */
	boolean hasAttribute(String name);

	/**
	 * Return the names of all attributes.
	 * 
	 * <p> 返回所有属性的名称。
	 * 
	 */
	String[] attributeNames();

}
