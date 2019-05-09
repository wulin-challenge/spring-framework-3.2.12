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

package org.springframework.beans.factory.xml;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;

/**
 * Convenience extension of {@link DefaultListableBeanFactory} that reads bean definitions
 * from an XML document. Delegates to {@link XmlBeanDefinitionReader} underneath; effectively
 * equivalent to using an XmlBeanDefinitionReader with a DefaultListableBeanFactory.
 * 
 * <p>DefaultListableBeanFactory的便捷扩展，它从XML文档中读取bean定义。
 * 委托下面的XmlBeanDefinitionReader;实际上相当于使用带有DefaultListableBeanFactory的XmlBeanDefinitionReader。
 *
 * <p>The structure, element and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). "beans" doesn't need to be the root element of the XML
 * document: This class will parse all bean definition elements in the XML file.
 * 
 * <p>所需XML文档的结构，元素和属性名称在此类中进行了硬编码。
 *  （当然，如果需要生成这种格式，可以运行转换）。 “beans”不需要是XML文档的根元素：该类将解析XML文件中的所有bean定义元素。
 *
 * <p>This class registers each bean definition with the {@link DefaultListableBeanFactory}
 * superclass, and relies on the latter's implementation of the {@link BeanFactory} interface.
 * It supports singletons, prototypes, and references to either of these kinds of bean.
 * See {@code "spring-beans-3.x.xsd"} (or historically, {@code "spring-beans-2.0.dtd"}) for
 * details on options and configuration style.
 * 
 * <p>此类使用DefaultListableBeanFactory超类注册每个bean定义，并依赖于后者对BeanFactory接口的实现。它支持单例，原型和对这些bean中的任何一种的引用。
 * 有关选项和配置样式的详细信息，请参阅“spring-beans-3.x.xsd”（或历史上，“spring-beans-2.0.dtd”）。
 *
 * <p><b>For advanced needs, consider using a {@link DefaultListableBeanFactory} with
 * an {@link XmlBeanDefinitionReader}.</b> The latter allows for reading from multiple XML
 * resources and is highly configurable in its actual XML parsing behavior.
 * 
 * <p>对于高级需求，请考虑将DefaultListableBeanFactory与XmlBeanDefinitionReader一起使用。
 * 后者允许从多个XML资源中读取，并且在其实际的XML解析行为中具有高度可配置性。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 15 April 2001
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see XmlBeanDefinitionReader
 * @deprecated as of Spring 3.1 in favor of {@link DefaultListableBeanFactory} and
 * {@link XmlBeanDefinitionReader}
 */
@Deprecated
@SuppressWarnings({"serial", "all"})
public class XmlBeanFactory extends DefaultListableBeanFactory {

	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);


	/**
	 * Create a new XmlBeanFactory with the given resource,
	 * which must be parsable using DOM.
	 * 
	 * <p>使用给定资源创建一个新的XmlBeanFactory，该资源必须可以使用DOM进行解析。
	 * @param resource XML resource to load bean definitions from 从中加载bean定义的资源XML资源
	 * @throws BeansException in case of loading or parsing errors  加载或解析错误时出现BeansException
	 */
	public XmlBeanFactory(Resource resource) throws BeansException {
		this(resource, null);
	}

	/**
	 * Create a new XmlBeanFactory with the given input stream,
	 * which must be parsable using DOM.
	 * 
	 * <p>使用给定的输入流创建一个新的XmlBeanFactory，它必须可以使用DOM进行解析。
	 * 
	 * @param resource XML resource to load bean definitions from  从中加载bean定义的XML资源
	 * @param parentBeanFactory parent bean factory
	 * @throws BeansException in case of loading or parsing errors 加载或解析错误时出现BeansException
	 */
	public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		this.reader.loadBeanDefinitions(resource);
	}

}
