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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

/**
 * Base interface used by the {@link DefaultBeanDefinitionDocumentReader}
 * for handling custom namespaces in a Spring XML configuration file.
 * 
 * <p>DefaultBeanDefinitionDocumentReader用于处理Spring XML配置文件中的自定义命名空间的基接口。
 *
 * <p>Implementations are expected to return implementations of the
 * {@link BeanDefinitionParser} interface for custom top-level tags and
 * implementations of the {@link BeanDefinitionDecorator} interface for
 * custom nested tags.
 * 
 * <p>实现期望返回BeanDefinitionParser接口的实现，以用于自定义顶级标记和BeanDefinitionDecorator接口的实现，
 * 以用于自定义嵌套标记。
 *
 * <p>The parser will call {@link #parse} when it encounters a custom tag
 * directly under the {@code &lt;beans&gt;} tags and {@link #decorate} when
 * it encounters a custom tag directly under a {@code &lt;bean&gt;} tag.
 * 
 * <p>当解析器直接在<beans>下遇到自定义标记时，它将调用parse。下遇到自定义标记时，它将调用parse。 
 * 直接在<bean>下遇到自定义标记时标记和装饰 标签。
 *
 * <p>Developers writing their own custom element extensions typically will
 * not implement this interface drectly, but rather make use of the provided
 * {@link NamespaceHandlerSupport} class.
 * 
 * <p>编写自己的自定义元素扩展的开发人员通常不会直接实现此接口，而是使用提供的NamespaceHandlerSupport类。
 *
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 2.0
 * @see DefaultBeanDefinitionDocumentReader
 * @see NamespaceHandlerResolver
 */
public interface NamespaceHandler {

	/**
	 * Invoked by the {@link DefaultBeanDefinitionDocumentReader} after
	 * construction but before any custom elements are parsed.
	 * 
	 * <p>在构造之后但在解析任何自定义元素之前由DefaultBeanDefinitionDocumentReader调用。
	 * 
	 * @see NamespaceHandlerSupport#registerBeanDefinitionParser(String, BeanDefinitionParser)
	 */
	void init();

	/**
	 * Parse the specified {@link Element} and register any resulting
	 * {@link BeanDefinition BeanDefinitions} with the
	 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * that is embedded in the supplied {@link ParserContext}.
	 * 
	 * <p>解析指定的Element并使用嵌入在提供的ParserContext中
	 * 的org.springframework.beans.factory.support.BeanDefinitionRegistry注册任何生成的BeanDefinition。
	 * 
	 * <p>Implementations should return the primary {@code BeanDefinition}
	 * that results from the parse phase if they wish to be used nested
	 * inside (for example) a {@code &lt;property&gt;} tag.
	 * 
	 * <p>实现应返回由解析阶段产生的主BeanDefinition，如果它们希望嵌套在内部（例如）<property> 标签。
	 * 
	 * <p>Implementations may return {@code null} if they will
	 * <strong>not</strong> be used in a nested scenario.
	 * 
	 * <p>如果实现不在嵌套方案中使用，则实现可能返回null。
	 * 
	 * @param element the element that is to be parsed into one or more {@code BeanDefinitions}
	 * 
	 * <p>要解析为一个或多个BeanDefinitions的元素
	 * 
	 * @param parserContext the object encapsulating the current state of the parsing process
	 * 
	 * <p>封装解析过程当前状态的对象
	 * 
	 * @return the primary {@code BeanDefinition} (can be {@code null} as explained above)
	 * 
	 * <p>主BeanDefinition（如上所述可以为null）
	 * 
	 */
	BeanDefinition parse(Element element, ParserContext parserContext);

	/**
	 * Parse the specified {@link Node} and decorate the supplied
	 * {@link BeanDefinitionHolder}, returning the decorated definition.
	 * 
	 * <p>解析指定的Node并装饰提供的BeanDefinitionHolder，返回修饰后的定义。
	 * 
	 * <p>The {@link Node} may be either an {@link org.w3c.dom.Attr} or an
	 * {@link Element}, depending on whether a custom attribute or element
	 * is being parsed.
	 * 
	 * <p>Node可以是org.w3c.dom.Attr或Element，具体取决于是否正在解析自定义属性或元素。
	 * 
	 * <p>Implementations may choose to return a completely new definition,
	 * which will replace the original definition in the resulting
	 * {@link org.springframework.beans.factory.BeanFactory}.
	 * 
	 * <p>实现可以选择返回一个全新的定义，它将替换生成的org.springframework.beans.factory.BeanFactory中的原始定义。
	 * 
	 * <p>The supplied {@link ParserContext} can be used to register any
	 * additional beans needed to support the main definition.
	 * 
	 * <p>提供的ParserContext可用于注册支持主定义所需的任何其他bean。
	 * 
	 * @param source the source element or attribute that is to be parsed
	 * 
	 * <p>要解析的源元素或属性
	 * 
	 * @param definition the current bean definition - 当前的bean定义
	 * @param parserContext the object encapsulating the current state of the parsing process
	 * 
	 * <p>封装解析过程当前状态的对象
	 * 
	 * @return the decorated definition (to be registered in the BeanFactory),
	 * or simply the original bean definition if no decoration is required.
	 * A {@code null} value is strictly speaking invalid, but will be leniently
	 * treated like the case where the original bean definition gets returned.
	 * 
	 * <p>装饰定义（要在BeanFactory中注册），或者只是原始bean定义（如果不需要装饰）。 严格地说，null值无效，
	 * 但是会像返回原始bean定义的情况一样对其进行宽松处理。
	 * 
	 */
	BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder definition, ParserContext parserContext);

}
