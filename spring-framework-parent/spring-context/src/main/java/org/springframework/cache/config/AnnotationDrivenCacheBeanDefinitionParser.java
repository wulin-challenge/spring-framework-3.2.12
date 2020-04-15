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

package org.springframework.cache.config;

import org.w3c.dom.Element;

import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.AnnotationConfigUtils;

/**
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser}
 * implementation that allows users to easily configure all the
 * infrastructure beans required to enable annotation-driven cache
 * demarcation.
 * 
 * <p> org.springframework.beans.factory.xml.BeanDefinitionParser实现，
 * 使用户可以轻松配置启用注释驱动的缓存划分所需的所有基础结构bean。
 *
 * <p>By default, all proxies are created as JDK proxies. This may cause
 * some problems if you are injecting objects as concrete classes rather
 * than interfaces. To overcome this restriction you can set the
 * '{@code proxy-target-class}' attribute to '{@code true}', which will
 * result in class-based proxies being created.
 * 
 * <p> 默认情况下，所有代理都创建为JDK代理。 如果将对象作为具体的类而不是接口注入，则可能会引起一些问题。 
 * 为了克服此限制，您可以将'proxy-target-class'属性设置为'true'，这将导致创建基于类的代理。
 *
 * @author Costin Leau
 * @since 3.1
 */
class AnnotationDrivenCacheBeanDefinitionParser implements BeanDefinitionParser {

	/**
	 * Parses the '{@code <cache:annotation-driven>}' tag. Will
	 * {@link AopNamespaceUtils#registerAutoProxyCreatorIfNecessary
	 * register an AutoProxyCreator} with the container as necessary.
	 * 
	 * <p> 解析“ <cache：annotation-driven>”标签。 必要时将在容器中注册一个AutoProxyCreator。
	 */
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String mode = element.getAttribute("mode");
		if ("aspectj".equals(mode)) {
			// mode="aspectj"
			registerCacheAspect(element, parserContext);
		}
		else {
			// mode="proxy"
			AopAutoProxyConfigurer.configureAutoProxyCreator(element, parserContext);
		}

		return null;
	}

	private static void parseCacheManagerProperty(Element element, BeanDefinition def) {
		def.getPropertyValues().add("cacheManager",
				new RuntimeBeanReference(CacheNamespaceHandler.extractCacheManager(element)));
	}

	/**
	 * Registers a
	 * <pre class="code">
	 * <bean id="cacheAspect" class="org.springframework.cache.aspectj.AnnotationCacheAspect" factory-method="aspectOf">
	 *   <property name="cacheManager" ref="cacheManager"/>
	 *   <property name="keyGenerator" ref="keyGenerator"/>
	 * </bean>
	 * </pre>
	 */
	private void registerCacheAspect(Element element, ParserContext parserContext) {
		if (!parserContext.getRegistry().containsBeanDefinition(AnnotationConfigUtils.CACHE_ASPECT_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition();
			def.setBeanClassName(AnnotationConfigUtils.CACHE_ASPECT_CLASS_NAME);
			def.setFactoryMethodName("aspectOf");
			parseCacheManagerProperty(element, def);
			CacheNamespaceHandler.parseKeyGenerator(element, def);
			parserContext.registerBeanComponent(new BeanComponentDefinition(def, AnnotationConfigUtils.CACHE_ASPECT_BEAN_NAME));
		}
	}


	/**
	 * Inner class to just introduce an AOP framework dependency when actually in proxy mode.
	 * 
	 * <p> 内部类，仅在实际处于代理模式时才引入AOP框架依赖关系。
	 */
	private static class AopAutoProxyConfigurer {

		public static void configureAutoProxyCreator(Element element, ParserContext parserContext) {
			AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);

			if (!parserContext.getRegistry().containsBeanDefinition(AnnotationConfigUtils.CACHE_ADVISOR_BEAN_NAME)) {
				Object eleSource = parserContext.extractSource(element);

				// Create the CacheOperationSource definition.
				RootBeanDefinition sourceDef = new RootBeanDefinition("org.springframework.cache.annotation.AnnotationCacheOperationSource");
				sourceDef.setSource(eleSource);
				sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				String sourceName = parserContext.getReaderContext().registerWithGeneratedName(sourceDef);

				// Create the CacheInterceptor definition.
				RootBeanDefinition interceptorDef = new RootBeanDefinition(CacheInterceptor.class);
				interceptorDef.setSource(eleSource);
				interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				parseCacheManagerProperty(element, interceptorDef);
				CacheNamespaceHandler.parseKeyGenerator(element, interceptorDef);
				interceptorDef.getPropertyValues().add("cacheOperationSources", new RuntimeBeanReference(sourceName));
				String interceptorName = parserContext.getReaderContext().registerWithGeneratedName(interceptorDef);

				// Create the CacheAdvisor definition.
				RootBeanDefinition advisorDef = new RootBeanDefinition(BeanFactoryCacheOperationSourceAdvisor.class);
				advisorDef.setSource(eleSource);
				advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				advisorDef.getPropertyValues().add("cacheOperationSource", new RuntimeBeanReference(sourceName));
				advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);
				if (element.hasAttribute("order")) {
					advisorDef.getPropertyValues().add("order", element.getAttribute("order"));
				}
				parserContext.getRegistry().registerBeanDefinition(AnnotationConfigUtils.CACHE_ADVISOR_BEAN_NAME, advisorDef);

				CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(),
						eleSource);
				compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef, AnnotationConfigUtils.CACHE_ADVISOR_BEAN_NAME));
				parserContext.registerComponent(compositeDef);
			}
		}
	}

}
