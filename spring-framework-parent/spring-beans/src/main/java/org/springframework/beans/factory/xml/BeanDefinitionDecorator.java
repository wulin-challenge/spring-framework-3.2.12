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

import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinitionHolder;

/**
 * Interface used by the {@link DefaultBeanDefinitionDocumentReader}
 * to handle custom, nested (directly under a {@code &lt;bean&gt;}) tags.
 * 
 * <p>DefaultBeanDefinitionDocumentReader用于处理自定义嵌套（直接在<bean>下）标记的接口。
 *
 * <p>Decoration may also occur based on custom attributes applied to the
 * {@code &lt;bean&gt;} tag. Implementations are free to turn the metadata in the
 * custom tag into as many
 * {@link org.springframework.beans.factory.config.BeanDefinition BeanDefinitions} as
 * required and to transform the
 * {@link org.springframework.beans.factory.config.BeanDefinition} of the enclosing
 * {@code &lt;bean&gt;} tag, potentially even returning a completely different
 * {@link org.springframework.beans.factory.config.BeanDefinition} to replace the
 * original.
 * 
 * <p>装饰也可以基于应用于<bean>的自定义属性进行。标签。实现可以自由地将自定义标记中的元数据转换为所需数
 * 量的BeanDefinitions，并转换封闭
 * 的<bean>的org.springframework.beans.factory.config.BeanDefinition。标记，
 * 甚至可能返回一个完全不同的org.springframework.beans.factory.config.BeanDefinition来替换原始文件。
 *
 * <p>{@link BeanDefinitionDecorator BeanDefinitionDecorators} should be aware that
 * they may be part of a chain. In particular, a {@link BeanDefinitionDecorator} should
 * be aware that a previous {@link BeanDefinitionDecorator} may have replaced the
 * original {@link org.springframework.beans.factory.config.BeanDefinition} with a
 * {@link org.springframework.aop.framework.ProxyFactoryBean} definition allowing for
 * custom {@link org.aopalliance.intercept.MethodInterceptor interceptors} to be added.
 * 
 * <p>BeanDefinitionDecorators应该意识到它们可能是链的一部分。特别是，BeanDefinitionDecorator应该知
 * 道以前的BeanDefinitionDecorator可能已经用org.springframework.aop.framework.ProxyFactoryBean定
 * 义替换了原始的org.springframework.beans.factory.config.BeanDefinition，允许添加自定义拦截器。
 *
 * <p>{@link BeanDefinitionDecorator BeanDefinitionDecorators} that wish to add an
 * interceptor to the enclosing bean should extend
 * {@link org.springframework.aop.config.AbstractInterceptorDrivenBeanDefinitionDecorator}
 * which handles the chaining ensuring that only one proxy is created and that it
 * contains all interceptors from the chain.
 * 
 * <p>希望向封闭bean添加拦截器的BeanDefinitionDecorators应该扩
 * 展org.springframework.aop.config.AbstractInterceptorDrivenBeanDefinitionDecorator，
 * 它处理链接，确保只创建一个代理并且它包含链中的所有拦截器。
 *
 * <p>The parser locates a {@link BeanDefinitionDecorator} from the
 * {@link NamespaceHandler} for the namespace in which the custom tag resides.
 * 
 * <p>解析器从NamespaceHandler中为自定义标记所在的命名空间定位BeanDefinitionDecorator。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see BeanDefinitionParser
 */
public interface BeanDefinitionDecorator {

	/**
	 * Parse the specified {@link Node} (either an element or an attribute) and decorate
	 * the supplied {@link org.springframework.beans.factory.config.BeanDefinition},
	 * returning the decorated definition.
	 * 
	 * <p>解析指定的Node（元素或属性）并装饰提供的org.springframework.beans.factory.config.BeanDefinition，返回修饰的定义。
	 * 
	 * <p>Implementations may choose to return a completely new definition, which will
	 * replace the original definition in the resulting
	 * {@link org.springframework.beans.factory.BeanFactory}.
	 * 
	 * <p>实现可以选择返回一个全新的定义，它将替换生成的org.springframework.beans.factory.BeanFactory中的原始定义。
	 * 
	 * <p>The supplied {@link ParserContext} can be used to register any additional
	 * beans needed to support the main definition.
	 * 
	 * <p>提供的ParserContext可用于注册支持主定义所需的任何其他bean。
	 */
	BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext);

}
