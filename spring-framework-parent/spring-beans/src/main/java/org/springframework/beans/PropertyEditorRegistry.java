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

import java.beans.PropertyEditor;

/**
 * Encapsulates methods for registering JavaBeans {@link PropertyEditor PropertyEditors}.
 * This is the central interface that a {@link PropertyEditorRegistrar} operates on.
 * 
 * <p> 封装用于注册JavaBeans PropertyEditors的方法。 这是PropertyEditorRegistrar操作的中央接口。
 *
 * <p>Extended by {@link BeanWrapper}; implemented by {@link BeanWrapperImpl}
 * and {@link org.springframework.validation.DataBinder}.
 *
 * <p> 由BeanWrapper扩展; 由BeanWrapperImpl和org.springframework.validation.DataBinder实现。
 * 
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see java.beans.PropertyEditor
 * @see PropertyEditorRegistrar
 * @see BeanWrapper
 * @see org.springframework.validation.DataBinder
 */
public interface PropertyEditorRegistry {

	/**
	 * Register the given custom property editor for all properties of the given type.
	 * 
	 * <p> 为给定类型的所有属性注册给定的自定义属性编辑器。
	 * 
	 * @param requiredType the type of the property - 属性的类型
	 * @param propertyEditor the editor to register - 编辑注册
	 */
	void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);

	/**
	 * Register the given custom property editor for the given type and
	 * property, or for all properties of the given type.
	 * 
	 * <p> 为给定的类型和属性或给定类型的所有属性注册给定的自定义属性编辑器。
	 * 
	 * <p>If the property path denotes an array or Collection property,
	 * the editor will get applied either to the array/Collection itself
	 * (the {@link PropertyEditor} has to create an array or Collection value) or
	 * to each element (the {@code PropertyEditor} has to create the element type),
	 * depending on the specified required type.
	 * 
	 * <p> 如果属性路径表示数组或Collection属性，则编辑器将应用于数组/ Collection本身（PropertyEditor必须创
	 * 建数组或Collection值）或每个元素（PropertyEditor必须创建元素类型）， 取决于指定的所需类型。
	 * 
	 * <p>Note: Only one single registered custom editor per property path
	 * is supported. In the case of a Collection/array, do not register an editor
	 * for both the Collection/array and each element on the same property.
	 * 
	 * <p> 注意：每个属性路径仅支持一个注册的自定义编辑器。 对于Collection / array，不要
	 * 为Collection / array和同一属性上的每个元素注册一个编辑器。
	 * 
	 * <p>For example, if you wanted to register an editor for "items[n].quantity"
	 * (for all values n), you would use "items.quantity" as the value of the
	 * 'propertyPath' argument to this method.
	 * 
	 * <p> 例如，如果要为“items [n] .quantity”（对于所有值n）注册编辑器，
	 * 则可以使用“items.quantity”作为此方法的“propertyPath”参数的值。
	 * 
	 * @param requiredType the type of the property. This may be {@code null}
	 * if a property is given but should be specified in any case, in particular in
	 * case of a Collection - making clear whether the editor is supposed to apply
	 * to the entire Collection itself or to each of its entries. So as a general rule:
	 * <b>Do not specify {@code null} here in case of a Collection/array!</b>
	 * 
	 * <p> 属性的类型。 如果给出属性但是在任何情况下都应该指定，这可能为null，
	 * 特别是在Collection的情况下 - 明确编辑器是应该应用于整个Collection本身还是应用于每个条目。
	 *  因此，作为一般规则：如果是Collection / array，请不要在此处指定null！
	 * 
	 * @param propertyPath the path of the property (name or nested path), or
	 * {@code null} if registering an editor for all properties of the given type
	 * 
	 * <p> 属性的路径（名称或嵌套路径），如果为给定类型的所有属性注册编辑器，则为null
	 * 
	 * @param propertyEditor editor to register - 编辑注册
	 */
	void registerCustomEditor(Class<?> requiredType, String propertyPath, PropertyEditor propertyEditor);

	/**
	 * Find a custom property editor for the given type and property.
	 * 
	 * <p> 查找给定类型和属性的自定义属性编辑器。
	 * 
	 * @param requiredType the type of the property (can be {@code null} if a property
	 * is given but should be specified in any case for consistency checking)
	 * 
	 * <p> 属性的类型（如果给出了属性，则可以为null，但在任何情况下都应指定用于一致性检查）
	 * 
	 * @param propertyPath the path of the property (name or nested path), or
	 * {@code null} if looking for an editor for all properties of the given type
	 * 
	 * <p> 属性的路径（名称或嵌套路径），如果查找给定类型的所有属性的编辑器，则返回null
	 * 
	 * @return the registered editor, or {@code null} if none
	 * 
	 * <p> 已注册的编辑器，如果没有则为null
	 * 
	 */
	PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath);

}
