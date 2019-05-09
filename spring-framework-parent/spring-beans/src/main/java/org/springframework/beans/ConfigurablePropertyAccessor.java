/*
 * Copyright 2002-2009 the original author or authors.
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

import org.springframework.core.convert.ConversionService;

/**
 * Interface that encapsulates configuration methods for a PropertyAccessor.
 * Also extends the PropertyEditorRegistry interface, which defines methods
 * for PropertyEditor management.
 * 
 * <p> 封装PropertyAccessor的配置方法的接口。 还扩展了PropertyEditorRegistry接口，该接口定义了PropertyEditor管理的方法。
 *
 * <p>Serves as base interface for {@link BeanWrapper}.
 * 
 * <p> 用作BeanWrapper的基本接口。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeanWrapper
 */
public interface ConfigurablePropertyAccessor extends PropertyAccessor, PropertyEditorRegistry, TypeConverter {

	/**
	 * Specify a Spring 3.0 ConversionService to use for converting
	 * property values, as an alternative to JavaBeans PropertyEditors.
	 * 
	 * <p> 指定用于转换属性值的Spring 3.0 ConversionService，作为JavaBeans PropertyEditors的替代方法。
	 * 
	 */
	void setConversionService(ConversionService conversionService);

	/**
	 * Return the associated ConversionService, if any.
	 * 
	 * <p> 返回关联的ConversionService（如果有）。
	 * 
	 */
	ConversionService getConversionService();

	/**
	 * Set whether to extract the old property value when applying a
	 * property editor to a new value for a property.
	 * 
	 * <p> 设置在将属性编辑器应用于属性的新值时是否提取旧属性值。
	 * 
	 */
	void setExtractOldValueForEditor(boolean extractOldValueForEditor);

	/**
	 * Return whether to extract the old property value when applying a
	 * property editor to a new value for a property.
	 * 
	 * <p> 返回在将属性编辑器应用于属性的新值时是否提取旧属性值。
	 */
	boolean isExtractOldValueForEditor();

}
