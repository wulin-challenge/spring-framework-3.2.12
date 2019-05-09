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

package org.springframework.beans.factory.annotation;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.ObjectUtils;

/**
 * Convenience methods performing bean lookups related to annotations, for example
 * Spring's {@link Qualifier @Qualifier} annotation.
 * 
 * <p> 执行与注释相关的bean查找的便捷方法，例如Spring的@Qualifier注释。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1.2
 * @see BeanFactoryUtils
 */
public class BeanFactoryAnnotationUtils {

	/**
	 * Obtain a bean of type {@code T} from the given {@code BeanFactory} declaring a
	 * qualifier (e.g. via {@code <qualifier>} or {@code @Qualifier}) matching the given
	 * qualifier, or having a bean name matching the given qualifier.
	 * 
	 * <p> 从给定的BeanFactory中获取类型为T的bean，声明匹配给定限定符的限定符
	 * （例如，通过<qualifier>或@Qualifier），或者具有与给定限定符匹配的bean名称。
	 * 
	 * @param beanFactory the BeanFactory to get the target bean from
	 * 
	 * <p> 从中获取目标bean的BeanFactory
	 * 
	 * @param beanType the type of bean to retrieve
	 * 
	 * <p> 要检索的bean的类型
	 * 
	 * @param qualifier the qualifier for selecting between multiple bean matches
	 * 
	 * <p> 在多个bean匹配之间进行选择的限定符
	 * 
	 * @return the matching bean of type {@code T} (never {@code null})
	 * 
	 * <p> 类型为T的匹配bean（永不为null）
	 * 
	 * @throws NoSuchBeanDefinitionException if no matching bean of type {@code T} found
	 * 
	 * <p> 如果找不到类型为T的匹配bean
	 * 
	 */
	public static <T> T qualifiedBeanOfType(BeanFactory beanFactory, Class<T> beanType, String qualifier) {
		if (beanFactory instanceof ConfigurableListableBeanFactory) {
			// Full qualifier matching supported.
			// 支持完全限定符匹配。
			return qualifiedBeanOfType((ConfigurableListableBeanFactory) beanFactory, beanType, qualifier);
		}
		else if (beanFactory.containsBean(qualifier)) {
			// Fallback: target bean at least found by bean name.
			// 回退：目标bean至少由bean名称找到。
			return beanFactory.getBean(qualifier, beanType);
		}
		else {
			throw new NoSuchBeanDefinitionException(qualifier, "No matching " + beanType.getSimpleName() +
					" bean found for bean name '" + qualifier +
					"'! (Note: Qualifier matching not supported because given " +
					"BeanFactory does not implement ConfigurableListableBeanFactory.)");
		}
	}

	/**
	 * Obtain a bean of type {@code T} from the given {@code BeanFactory} declaring a qualifier
	 * (e.g. {@code <qualifier>} or {@code @Qualifier}) matching the given qualifier).
	 * 
	 * <p> 从给定的BeanFactory中获取类型为T的bean，声明与给定限定符匹配的限定符（例如<qualifier>或@Qualifier）。
	 * 
	 * @param bf the BeanFactory to get the target bean from
	 * 
	 * <p> 从中获取目标bean的BeanFactory
	 * 
	 * @param beanType the type of bean to retrieve
	 * 
	 * <p> 要检索的bean的类型
	 * 
	 * @param qualifier the qualifier for selecting between multiple bean matches
	 * 
	 * <p> 在多个bean匹配之间进行选择的限定符
	 * 
	 * @return the matching bean of type {@code T} (never {@code null})
	 * 
	 * <p> 类型为T的匹配bean（永不为null）
	 * 
	 * @throws NoSuchBeanDefinitionException if no matching bean of type {@code T} found
	 * 
	 * <p> 如果找不到类型为T的匹配bean
	 * 
	 */
	private static <T> T qualifiedBeanOfType(ConfigurableListableBeanFactory bf, Class<T> beanType, String qualifier) {
		Map<String, T> candidateBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(bf, beanType);
		T matchingBean = null;
		for (String beanName : candidateBeans.keySet()) {
			if (isQualifierMatch(qualifier, beanName, bf)) {
				if (matchingBean != null) {
					throw new NoSuchBeanDefinitionException(qualifier, "No unique " + beanType.getSimpleName() +
							" bean found for qualifier '" + qualifier + "'");
				}
				matchingBean = candidateBeans.get(beanName);
			}
		}
		if (matchingBean != null) {
			return matchingBean;
		}
		else if (bf.containsBean(qualifier)) {
			// Fallback: target bean at least found by bean name - probably a manually registered singleton.
			// 回退：至少通过bean名称找到目标bean  - 可能是手动注册的单例。
			return bf.getBean(qualifier, beanType);
		}
		else {
			throw new NoSuchBeanDefinitionException(qualifier, "No matching " + beanType.getSimpleName() +
					" bean found for qualifier '" + qualifier + "' - neither qualifier match nor bean name match!");
		}
	}

	/**
	 * Check whether the named bean declares a qualifier of the given name.
	 * 
	 * <p> 检查指定的bean是否声明了给定名称的限定符。
	 * 
	 * @param qualifier the qualifier to match
	 * 
	 * <p> 匹配的限定符
	 * 
	 * @param beanName the name of the candidate bean
	 * 
	 * <p> 候选bean的名称
	 * 
	 * @param bf the {@code BeanFactory} from which to retrieve the named bean
	 * 
	 * <p> 从中检索命名bean的BeanFactory
	 * 
	 * @return {@code true} if either the bean definition (in the XML case)
	 * or the bean's factory method (in the {@code @Bean} case) defines a matching
	 * qualifier value (through {@code <qualifier>} or {@code @Qualifier})
	 * 
	 * <p> 如果bean定义（在XML情况下）或bean的工厂方法（在@Bean情况下）定义匹配限定符值（通过<qualifier>或@Qualifier），则返回true
	 * 
	 */
	private static boolean isQualifierMatch(String qualifier, String beanName, ConfigurableListableBeanFactory bf) {
		if (bf.containsBean(beanName)) {
			try {
				BeanDefinition bd = bf.getMergedBeanDefinition(beanName);
				if (bd instanceof AbstractBeanDefinition) {
					AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
					AutowireCandidateQualifier candidate = abd.getQualifier(Qualifier.class.getName());
					if ((candidate != null && qualifier.equals(candidate.getAttribute(AutowireCandidateQualifier.VALUE_KEY))) ||
							qualifier.equals(beanName) || ObjectUtils.containsElement(bf.getAliases(beanName), qualifier)) {
						return true;
					}
				}
				if (bd instanceof RootBeanDefinition) {
					Method factoryMethod = ((RootBeanDefinition) bd).getResolvedFactoryMethod();
					if (factoryMethod != null) {
						Qualifier targetAnnotation = factoryMethod.getAnnotation(Qualifier.class);
						if (targetAnnotation != null && qualifier.equals(targetAnnotation.value())) {
							return true;
						}
					}
				}
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore - can't compare qualifiers for a manually registered singleton object
				// 忽略 - 无法比较手动注册的单例对象的限定符
			}
		}
		return false;
	}

}
