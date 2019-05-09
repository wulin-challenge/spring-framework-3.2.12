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

package org.springframework.aop.framework.autoproxy;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Conventions;

/**
 * Utilities for auto-proxy aware components.
 * Mainly for internal use within the framework.
 * 
 * <p> 自动代理感知组件的实用程序。 主要供框架内部使用。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see AbstractAutoProxyCreator
 */
public abstract class AutoProxyUtils {

	/**
	 * Bean definition attribute that may indicate whether a given bean is supposed
	 * to be proxied with its target class (in case of it getting proxied in the first
	 * place). The value is {@code Boolean.TRUE} or {@code Boolean.FALSE}.
	 * 
	 * <p> Bean定义属性，可以指示给定的bean是否应该使用其目标类代理（如果它首先被代理）。 
	 * 值为Boolean.TRUE或Boolean.FALSE。
	 * 
	 * <p>Proxy factories can set this attribute if they built a target class proxy
	 * for a specific bean, and want to enforce that that bean can always be cast
	 * to its target class (even if AOP advices get applied through auto-proxying).
	 * 
	 * <p> 如果代理工厂为特定bean构建了目标类代理，并且希望强制该bean始终可以强制转换为其目标类（即使通过自动代理应用AOP建议），
	 * 代理工厂也可以设置此属性。
	 * 
	 */
	public static final String PRESERVE_TARGET_CLASS_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(AutoProxyUtils.class, "preserveTargetClass");


	/**
	 * Determine whether the given bean should be proxied with its target
	 * class rather than its interfaces. Checks the
	 * {@link #PRESERVE_TARGET_CLASS_ATTRIBUTE "preserveTargetClass" attribute}
	 * of the corresponding bean definition.
	 * 
	 * <p> 确定给定的bean是否应该使用其目标类而不是其接口进行代理。 检查相应bean定义的“preserveTargetClass”属性。
	 * 
	 * @param beanFactory the containing ConfigurableListableBeanFactory
	 * 
	 * <p> 包含ConfigurableListableBeanFactory
	 * 
	 * @param beanName the name of the bean - bean 的名称
	 * 
	 * @return whether the given bean should be proxied with its target class
	 * 
	 * <p> 是否应该使用其目标类代理给定的bean
	 * 
	 */
	public static boolean shouldProxyTargetClass(ConfigurableListableBeanFactory beanFactory, String beanName) {
		if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
			BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
			return Boolean.TRUE.equals(bd.getAttribute(PRESERVE_TARGET_CLASS_ATTRIBUTE));
		}
		return false;
	}

}
