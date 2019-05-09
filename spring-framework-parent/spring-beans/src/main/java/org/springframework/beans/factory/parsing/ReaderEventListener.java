/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.parsing;

import java.util.EventListener;

/**
 * Interface that receives callbacks for component, alias and import
 * registrations during a bean definition reading process.
 * 
 * <p>在bean定义读取过程中接收组件，别名和导入注册的回调的接口。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see ReaderContext
 */
public interface ReaderEventListener extends EventListener {

	/**
	 * Notification that the given defaults has been registered.
	 * 
	 * <p>通知已注册给定的默认值。
	 * 
	 * @param defaultsDefinition a descriptor for the defaults - 默认值的描述符
	 * @see org.springframework.beans.factory.xml.DocumentDefaultsDefinition
	 */
	void defaultsRegistered(DefaultsDefinition defaultsDefinition);

	/**
	 * Notification that the given component has been registered. 
	 * 
	 * <p>通知给定组件已注册。
	 * 
	 * @param componentDefinition a descriptor for the new component - 新组件的描述符
	 * @see BeanComponentDefinition
	 */
	void componentRegistered(ComponentDefinition componentDefinition);

	/**
	 * Notification that the given alias has been registered.
	 * 
	 * <p>通知给定别名已注册。
	 * 
	 * @param aliasDefinition a descriptor for the new alias - 新别名的描述符
	 */
	void aliasRegistered(AliasDefinition aliasDefinition);

	/**
	 * Notification that the given import has been processed.
	 * 
	 * <p>通知已处理给定导入。
	 * 
	 * @param importDefinition a descriptor for the import - 导入的描述符
	 */
	void importProcessed(ImportDefinition importDefinition);

}
