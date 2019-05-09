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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.env.Environment;

/**
 * SPI for parsing an XML document that contains Spring bean definitions.
 * Used by XmlBeanDefinitionReader for actually parsing a DOM document.
 * 
 * <p>SPI用于解析包含Spring bean定义的XML文档。 由XmlBeanDefinitionReader用于实际解析DOM文档。
 *
 * <p>Instantiated per document to parse: Implementations can hold
 * state in instance variables during the execution of the
 * {@code registerBeanDefinitions} method, for example global
 * settings that are defined for all bean definitions in the document.
 * 
 * <p>每个要解析的文档实例化：实现可以在执行registerBeanDefinitions方法期间保存实例变量中的状
 * 态，例如为文档中的所有bean定义定义的全局设置。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 18.12.2003
 * @see XmlBeanDefinitionReader#setDocumentReaderClass
 */
public interface BeanDefinitionDocumentReader {

	/**
	 * Set the Environment to use when reading bean definitions.
	 * 
	 * <p>设置在读取bean定义时使用的Environment。
	 * 
	 * <p>Used for evaluating profile information to determine whether a
	 * {@code <beans/>} document/element should be included or ignored.
	 * 
	 * <p>用于评估配置文件信息，以确定是否应包含或忽略<beans />文档/元素。
	 * 
	 * @deprecated in favor of Environment access via XmlReaderContext - 支持通过XmlReaderContext进行环境访问
	 */
	@Deprecated
	void setEnvironment(Environment environment);

	/**
	 * Read bean definitions from the given DOM document and
	 * register them with the registry in the given reader context.
	 * 
	 * <p>从给定的DOM文档中读取bean定义，并在给定的reader上下文中将其注册到注册表中。
	 * 
	 * @param doc the DOM document - DOM文档
	 * @param readerContext the current context of the reader
	 * (includes the target registry and the resource being parsed)
	 * 
	 * <p>读者的当前上下文（包括目标注册表和正在解析的资源）
	 * 
	 * @throws BeanDefinitionStoreException in case of parsing errors - 在解析错误的情况下
	 */
	void registerBeanDefinitions(Document doc, XmlReaderContext readerContext)
			throws BeanDefinitionStoreException;

}
