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

/**
 * Interface for strategies that register custom
 * {@link java.beans.PropertyEditor property editors} with a
 * {@link org.springframework.beans.PropertyEditorRegistry property editor registry}.
 * 
 * <p>用于使用属性编辑器注册表注册自定义属性编辑器的策略的接口。
 *
 * <p>This is particularly useful when you need to use the same set of
 * property editors in several different situations: write a corresponding
 * registrar and reuse that in each case.
 * 
 * <p>当您需要在几种不同的情况下使用同一组属性编辑器时，这一点特别有用：编写相应的注册器并在每种情况下重用它。
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see PropertyEditorRegistry
 * @see java.beans.PropertyEditor
 */
public interface PropertyEditorRegistrar {

	/**
	 * Register custom {@link java.beans.PropertyEditor PropertyEditors} with
	 * the given {@code PropertyEditorRegistry}.
	 * 
	 * <p>使用给定的PropertyEditorRegistry注册自定义PropertyEditors。
	 * 
	 * <p>The passed-in registry will usually be a {@link BeanWrapper} or a
	 * {@link org.springframework.validation.DataBinder DataBinder}.
	 * 
	 * <p>传入的注册表通常是BeanWrapper或DataBinder。
	 * 
	 * <p>It is expected that implementations will create brand new
	 * {@code PropertyEditors} instances for each invocation of this
	 * method (since {@code PropertyEditors} are not threadsafe).
	 * 
	 * <p>预计实现将为每次调用此方法创建全新的PropertyEditors实例（因为PropertyEditors不是线程安全的）。
	 * 
	 * @param registry the {@code PropertyEditorRegistry} to register the
	 * custom {@code PropertyEditors} with
	 * 
	 * <p>PropertyEditorRegistry用于注册自定义PropertyEditors
	 * 
	 */
	void registerCustomEditors(PropertyEditorRegistry registry);

}
