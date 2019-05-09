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

package org.springframework.aop.config;

import java.util.List;

import org.w3c.dom.Node;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Base implementation for
 * {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator BeanDefinitionDecorators}
 * wishing to add an {@link org.aopalliance.intercept.MethodInterceptor interceptor}
 * to the resulting bean.
 * 
 * <p> BeanDefinitionDecorators的基本实现，希望为生成的bean添加拦截器。
 *
 * <p>This base class controls the creation of the {@link ProxyFactoryBean} bean definition
 * and wraps the original as an inner-bean definition for the {@code target} property
 * of {@link ProxyFactoryBean}.
 * 
 * <p> 此基类控制ProxyFactoryBean bean定义的创建，并将原始包装为ProxyFactoryBean的target属性的内部bean定义。
 *
 * <p>Chaining is correctly handled, ensuring that only one {@link ProxyFactoryBean} definition
 * is created. If a previous {@link org.springframework.beans.factory.xml.BeanDefinitionDecorator}
 * already created the {@link org.springframework.aop.framework.ProxyFactoryBean} then the
 * interceptor is simply added to the existing definition.
 * 
 * <p> 正确处理链接，确保只创建一个ProxyFactoryBean定义。 如果之前的
 * org.springframework.beans.factory.xml.BeanDefinitionDecorator已经创建了
 * org.springframework.aop.framework.ProxyFactoryBean，那么拦截器就会被添加到现有定义中。
 *
 * <p>Subclasses have only to create the {@code BeanDefinition} to the interceptor that
 * they wish to add.
 * 
 * <p> 子类只需要为他们希望添加的拦截器创建BeanDefinition。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.aopalliance.intercept.MethodInterceptor
 */
public abstract class AbstractInterceptorDrivenBeanDefinitionDecorator implements BeanDefinitionDecorator {

	public final BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definitionHolder, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();

		// get the root bean name - will be the name of the generated proxy factory bean
		// 获取根bean名称 - 将是生成的代理工厂bean的名称
		String existingBeanName = definitionHolder.getBeanName();
		BeanDefinition targetDefinition = definitionHolder.getBeanDefinition();
		BeanDefinitionHolder targetHolder = new BeanDefinitionHolder(targetDefinition, existingBeanName + ".TARGET");

		// delegate to subclass for interceptor definition
		// 委托给拦截器定义的子类
		BeanDefinition interceptorDefinition = createInterceptorDefinition(node);

		// generate name and register the interceptor
		// 生成名称并注册拦截器
		String interceptorName = existingBeanName + "." + getInterceptorNameSuffix(interceptorDefinition);
		BeanDefinitionReaderUtils.registerBeanDefinition(
				new BeanDefinitionHolder(interceptorDefinition, interceptorName), registry);

		BeanDefinitionHolder result = definitionHolder;

		if (!isProxyFactoryBeanDefinition(targetDefinition)) {
			// create the proxy definition
			// 创建代理定义
			RootBeanDefinition proxyDefinition = new RootBeanDefinition();
			// create proxy factory bean definition
			// 创建代理工厂bean定义
			proxyDefinition.setBeanClass(ProxyFactoryBean.class);
			proxyDefinition.setScope(targetDefinition.getScope());
			proxyDefinition.setLazyInit(targetDefinition.isLazyInit());
			// set the target
			// 设定目标
			proxyDefinition.setDecoratedDefinition(targetHolder);
			proxyDefinition.getPropertyValues().add("target", targetHolder);
			// create the interceptor names list
			// 创建拦截器名称列表
			proxyDefinition.getPropertyValues().add("interceptorNames", new ManagedList<String>());
			// copy autowire settings from original bean definition.
			// 从原始bean定义复制autowire设置。
			proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
			proxyDefinition.setPrimary(targetDefinition.isPrimary());
			if (targetDefinition instanceof AbstractBeanDefinition) {
				proxyDefinition.copyQualifiersFrom((AbstractBeanDefinition) targetDefinition);
			}
			// wrap it in a BeanDefinitionHolder with bean name
			// 将它包装在带有bean名称的BeanDefinitionHolder中
			result = new BeanDefinitionHolder(proxyDefinition, existingBeanName);
		}

		addInterceptorNameToList(interceptorName, result.getBeanDefinition());
		return result;
	}

	@SuppressWarnings("unchecked")
	private void addInterceptorNameToList(String interceptorName, BeanDefinition beanDefinition) {
		List<String> list = (List<String>)
				beanDefinition.getPropertyValues().getPropertyValue("interceptorNames").getValue();
		list.add(interceptorName);
	}

	private boolean isProxyFactoryBeanDefinition(BeanDefinition existingDefinition) {
		return ProxyFactoryBean.class.getName().equals(existingDefinition.getBeanClassName());
	}

	protected String getInterceptorNameSuffix(BeanDefinition interceptorDefinition) {
		return StringUtils.uncapitalize(ClassUtils.getShortName(interceptorDefinition.getBeanClassName()));
	}

	/**
	 * Subclasses should implement this method to return the {@code BeanDefinition}
	 * for the interceptor they wish to apply to the bean being decorated.
	 * 
	 * <p> 子类应该实现此方法以返回他们希望应用于正在装饰的bean的拦截器的BeanDefinition。
	 */
	protected abstract BeanDefinition createInterceptorDefinition(Node node);

}
