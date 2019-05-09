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

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * Base class for those {@link BeanDefinitionParser} implementations that
 * need to parse and define just a <i>single</i> {@code BeanDefinition}.
 * 
 * <p> 那些需要解析和定义单个BeanDefinition的BeanDefinitionParser实现的基类。
 *
 * <p>Extend this parser class when you want to create a single bean definition
 * from an arbitrarily complex XML element. You may wish to consider extending
 * the {@link AbstractSimpleBeanDefinitionParser} when you want to create a
 * single bean definition from a relatively simple custom XML element.
 * 
 * <p> 如果要从任意复杂的XML元素创建单个bean定义，请扩展此解析器类。 当您想要从相对简单的自定义XML元
 * 素创建单个bean定义时，您可能希望考虑扩展AbstractSimpleBeanDefinitionParser。
 *
 * <p>The resulting {@code BeanDefinition} will be automatically registered
 * with the {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
 * Your job simply is to {@link #doParse parse} the custom XML {@link Element}
 * into a single {@code BeanDefinition}.
 * 
 * <p> 生成的BeanDefinition将自动注册到org.springframework.beans.factory.support.BeanDefinitionRegistry。 
 * 您的工作只是将自定义XML元素解析为单个BeanDefinition。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0
 * @see #getBeanClass
 * @see #getBeanClassName
 * @see #doParse
 */
public abstract class AbstractSingleBeanDefinitionParser extends AbstractBeanDefinitionParser {

	/**
	 * Creates a {@link BeanDefinitionBuilder} instance for the
	 * {@link #getBeanClass bean Class} and passes it to the
	 * {@link #doParse} strategy method.
	 * 
	 * <p> 为bean类创建BeanDefinitionBuilder实例，并将其传递给doParse策略方法。
	 * 
	 * @param element the element that is to be parsed into a single BeanDefinition
	 * 
	 * <p> 要解析为单个BeanDefinition的元素
	 * 
	 * @param parserContext the object encapsulating the current state of the parsing process
	 * 
	 * <p> 封装解析过程当前状态的对象
	 * 
	 * @return the BeanDefinition resulting from the parsing of the supplied {@link Element}
	 * 
	 * <p> 解析所提供的Element产生的BeanDefinition
	 * 
	 * @throws IllegalStateException if the bean {@link Class} returned from
	 * {@link #getBeanClass(org.w3c.dom.Element)} is {@code null}
	 * 
	 * <p> 如果从getBeanClass（org.w3c.dom.Element）返回的bean类为null
	 * 
	 * @see #doParse
	 */
	@Override
	protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition();
		String parentName = getParentName(element);
		if (parentName != null) {
			builder.getRawBeanDefinition().setParentName(parentName);
		}
		Class<?> beanClass = getBeanClass(element);
		if (beanClass != null) {
			builder.getRawBeanDefinition().setBeanClass(beanClass);
		}
		else {
			String beanClassName = getBeanClassName(element);
			if (beanClassName != null) {
				builder.getRawBeanDefinition().setBeanClassName(beanClassName);
			}
		}
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
		if (parserContext.isNested()) {
			// Inner bean definition must receive same scope as containing bean.
			// 内部bean定义必须接收与包含bean相同的范围。
			builder.setScope(parserContext.getContainingBeanDefinition().getScope());
		}
		if (parserContext.isDefaultLazyInit()) {
			// Default-lazy-init applies to custom bean definitions as well.
			// Default-lazy-init也适用于自定义bean定义。
			builder.setLazyInit(true);
		}
		doParse(element, parserContext, builder);
		return builder.getBeanDefinition();
	}

	/**
	 * Determine the name for the parent of the currently parsed bean,
	 * in case of the current bean being defined as a child bean.
	 * 
	 * <p> 在当前bean被定义为子bean的情况下，确定当前解析的bean的父级的名称。
	 * 
	 * <p>The default implementation returns {@code null},
	 * indicating a root bean definition.
	 * 
	 * <p> 默认实现返回null，表示根bean定义。
	 * 
	 * @param element the {@code Element} that is being parsed
	 * 
	 * <p> 正在解析的元素
	 * 
	 * @return the name of the parent bean for the currently parsed bean,
	 * or {@code null} if none
	 * 
	 * <p> 当前解析的bean的父bean的名称，如果没有则为null
	 * 
	 */
	protected String getParentName(Element element) {
		return null;
	}

	/**
	 * Determine the bean class corresponding to the supplied {@link Element}.
	 * 
	 * <p> 确定与提供的Element对应的bean类。
	 * 
	 * <p>Note that, for application classes, it is generally preferable to
	 * override {@link #getBeanClassName} instead, in order to avoid a direct
	 * dependence on the bean implementation class. The BeanDefinitionParser
	 * and its NamespaceHandler can be used within an IDE plugin then, even
	 * if the application classes are not available on the plugin's classpath.
	 * 
	 * <p> 请注意，对于应用程序类，通常最好覆盖getBeanClassName，以避免直接依赖于bean实现类。 
	 * BeanDefinitionParser及其NamespaceHandler可以在IDE插件中使用，
	 * 即使插件的类路径上没有应用程序类也是如此。
	 * 
	 * @param element the {@code Element} that is being parsed - 正在解析的元素
	 * @return the {@link Class} of the bean that is being defined via parsing
	 * the supplied {@code Element}, or {@code null} if none
	 * 
	 * <p> 通过解析提供的Element定义的bean的类，如果没有，则返回null
	 * 
	 * @see #getBeanClassName
	 */
	protected Class<?> getBeanClass(Element element) {
		return null;
	}

	/**
	 * Determine the bean class name corresponding to the supplied {@link Element}.
	 * 
	 * <p> 确定与提供的Element对应的bean类名称。
	 * 
	 * @param element the {@code Element} that is being parsed - 正在解析的元素
	 * @return the class name of the bean that is being defined via parsing
	 * the supplied {@code Element}, or {@code null} if none
	 * 
	 * <p> 通过解析提供的Element定义的bean的类名，如果没有则为null
	 * 
	 * @see #getBeanClass
	 */
	protected String getBeanClassName(Element element) {
		return null;
	}

	/**
	 * Parse the supplied {@link Element} and populate the supplied
	 * {@link BeanDefinitionBuilder} as required.
	 * 
	 * <p> 解析提供的Element并根据需要填充提供的BeanDefinitionBuilder。
	 * 
	 * <p>The default implementation delegates to the {@code doParse}
	 * version without ParserContext argument.
	 * 
	 * <p> 默认实现委托给没有ParserContext参数的doParse版本。
	 * 
	 * @param element the XML element being parsed - 正在解析的XML元素
	 * @param parserContext the object encapsulating the current state of the parsing process
	 * 
	 * <p> 封装解析过程当前状态的对象
	 * 
	 * @param builder used to define the {@code BeanDefinition} - 用于定义BeanDefinition
	 * @see #doParse(Element, BeanDefinitionBuilder)
	 */
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		doParse(element, builder);
	}

	/**
	 * Parse the supplied {@link Element} and populate the supplied
	 * {@link BeanDefinitionBuilder} as required.
	 * 
	 * <p> 解析提供的Element并根据需要填充提供的BeanDefinitionBuilder。
	 * 
	 * <p>The default implementation does nothing.
	 * 
	 * <p> 默认实现什么都不做。
	 * 
	 * @param element the XML element being parsed - 正在解析的XML元素
	 * @param builder used to define the {@code BeanDefinition} - 用于定义BeanDefinition
	 */
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
	}

}
