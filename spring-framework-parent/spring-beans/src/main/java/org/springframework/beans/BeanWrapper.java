/*
 * Copyright 2002-2014 the original author or authors.
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

/**
 * The central interface of Spring's low-level JavaBeans infrastructure.
 * 
 * <p>Spring的低级JavaBeans基础结构的中央接口。
 *
 * <p>Typically not used directly but rather implicitly via a
 * {@link org.springframework.beans.factory.BeanFactory} or a
 * {@link org.springframework.validation.DataBinder}.
 *
 * <p>通常不直接使用，而是通过org.springframework.beans.factory.BeanFactory
 * 或org.springframework.validation.DataBinder隐式使用。
 * 
 * <p>Provides operations to analyze and manipulate standard JavaBeans:
 * the ability to get and set property values (individually or in bulk),
 * get property descriptors, and query the readability/writability of properties.
 * 
 * <p>提供分析和操作标准JavaBeans的操作：获取和设置属性值（单独或批量），获取属性描述符以及查询属性的可读性/可写性的能力。
 *
 * <p>This interface supports <b>nested properties</b> enabling the setting
 * of properties on subproperties to an unlimited depth.
 * 
 * <p>此接口支持嵌套属性，可以将子属性上的属性设置为无限深度。
 *
 * <p>A BeanWrapper's default for the "extractOldValueForEditor" setting
 * is "false", to avoid side effects caused by getter method invocations.
 * Turn this to "true" to expose present property values to custom editors.
 * 
 * <p>BeanWrapper的“extractOldValueForEditor”设置的默认值为“false”，
 * 以避免由getter方法调用引起的副作用。 将其设置为“true”以将当前属性值公开给自定义编辑器。
 * 
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @see PropertyAccessor
 * @see PropertyEditorRegistry
 * @see PropertyAccessorFactory#forBeanPropertyAccess
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.validation.BeanPropertyBindingResult
 * @see org.springframework.validation.DataBinder#initBeanPropertyAccess()
 */
public interface BeanWrapper extends ConfigurablePropertyAccessor {

	/**
	 * Return the bean instance wrapped by this object, if any.
	 * 
	 * <p> 返回此对象包装的bean实例（如果有）。
	 * 
	 * @return the bean instance, or {@code null} if none set
	 * 
	 * <p> bean实例，如果没有设置则为null
	 * 
	 */
	Object getWrappedInstance();

	/**
	 * Return the type of the wrapped JavaBean object.
	 * 
	 * <p> 返回包装的JavaBean对象的类型。
	 * 
	 * @return the type of the wrapped bean instance,
	 * or {@code null} if no wrapped object has been set
	 * 
	 * <p> 包装bean实例的类型，如果没有设置包装对象，则返回null
	 * 
	 */
	Class<?> getWrappedClass();

	/**
	 * Obtain the PropertyDescriptors for the wrapped object
	 * (as determined by standard JavaBeans introspection).
	 * 
	 * <p> 获取包装对象的PropertyDescriptors（由标准JavaBeans内省确定）。
	 * 
	 * @return the PropertyDescriptors for the wrapped object
	 * 
	 * <p> 包装对象的PropertyDescriptors
	 * 
	 */
	PropertyDescriptor[] getPropertyDescriptors();

	/**
	 * Obtain the property descriptor for a specific property
	 * of the wrapped object.
	 * 
	 * <p> 获取包装对象的特定属性的属性描述符。
	 * 
	 * @param propertyName the property to obtain the descriptor for
	 * (may be a nested path, but no indexed/mapped property)
	 * 
	 * <p> 获取描述符的属性（可能是嵌套路径，但没有索引/映射属性）返回：
	 * 
	 * @return the property descriptor for the specified property
	 * 
	 * <p> 指定属性的属性描述符
	 * 
	 * @throws InvalidPropertyException if there is no such property - 如果没有这样的属性
	 */
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException;

	/**
	 * Set whether this BeanWrapper should attempt to "auto-grow" a
	 * nested path that contains a {@code null} value.
	 * 
	 * <p> 设置此BeanWrapper是否应尝试“自动增长”包含空值的嵌套路径。
	 * 
	 * <p>If {@code true}, a {@code null} path location will be populated
	 * with a default object value and traversed instead of resulting in a
	 * {@link NullValueInNestedPathException}. Turning this flag on also enables
	 * auto-growth of collection elements when accessing an out-of-bounds index.
	 * 
	 * <p> 如果为true，则将使用默认对象值填充空路径位置并遍历，而不是导致NullValueInNestedPathException。
	 *  打开此标志还可以在访问越界索引时启用集合元素的自动增长。
	 * 
	 * <p>Default is {@code false} on a plain BeanWrapper.\
	 * 
	 * <p> 普通BeanWrapper上的默认值为false。
	 */
	void setAutoGrowNestedPaths(boolean autoGrowNestedPaths);

	/**
	 * Return whether "auto-growing" of nested paths has been activated.
	 * 
	 * <p> 返回是否已激活嵌套路径的“自动增长”。
	 */
	boolean isAutoGrowNestedPaths();

	/**
	 * Specify a limit for array and collection auto-growing.
	 * 
	 * <p> 指定阵列和集合自动增长的限制。
	 * 
	 * <p>Default is unlimited on a plain BeanWrapper.
	 * 
	 * <p> 普通BeanWrapper的默认值是无限制的。
	 * 
	 */
	void setAutoGrowCollectionLimit(int autoGrowCollectionLimit);

	/**
	 * Return the limit for array and collection auto-growing.
	 * 
	 * <p> 返回数组和集合自动增长的限制。
	 * 
	 */
	int getAutoGrowCollectionLimit();

}
