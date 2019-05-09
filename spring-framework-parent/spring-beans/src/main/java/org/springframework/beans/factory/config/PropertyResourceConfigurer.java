/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.util.ObjectUtils;

/**
 * Allows for configuration of individual bean property values from a property resource,
 * i.e. a properties file. Useful for custom config files targeted at system
 * administrators that override bean properties configured in the application context.
 * 
 * <p> 允许从属性资源（即属性文件）配置各个bean属性值。 对于以系统管理员为目标的自定义配置文件非常有用，这些文件覆盖在应用程
 *
 * <p>Two concrete implementations are provided in the distribution:
 * 
 * <p> 分发中提供了两个具体实现：
 * 
 * <ul>
 * <li>{@link PropertyOverrideConfigurer} for "beanName.property=value" style overriding
 * (<i>pushing</i> values from a properties file into bean definitions)
 * 
 * <li> PropertyOverrideConfigurer用于“beanName.property = value”样式重写（将属性文件中的值推送到bean定义中）
 * 
 * <li>{@link PropertyPlaceholderConfigurer} for replacing "${...}" placeholders
 * (<i>pulling</i> values from a properties file into bean definitions)
 * 
 * <li> PropertyPlaceholderConfigurer用于替换“$ {...}”占位符（将属性文件中的值拉入bean定义）
 * </ul>
 *
 * <p>Property values can be converted after reading them in, through overriding
 * the {@link #convertPropertyValue} method. For example, encrypted values
 * can be detected and decrypted accordingly before processing them.
 * 
 * <p> 通过覆盖convertPropertyValue方法，可以在读取属性值后转换它们。 例如，可以在处理加密值之前相应地检测和解密加密值。
 *
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 * @see PropertyPlaceholderConfigurer
 */
public abstract class PropertyResourceConfigurer extends PropertiesLoaderSupport
		implements BeanFactoryPostProcessor, PriorityOrdered {

	/**
	 * default: same as non-Ordered
	 * 
	 * <p> 默认值：与非订购相同
	 * 
	 */
	private int order = Ordered.LOWEST_PRECEDENCE; 


	/**
	 * Set the order value of this object for sorting purposes.
	 * 
	 * <p> 设置此对象的顺序值以进行排序。
	 * 
	 * @see PriorityOrdered
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return this.order;
	}


	/**
	 * {@linkplain #mergeProperties Merge}, {@linkplain #convertProperties convert} and
	 * {@linkplain #processProperties process} properties against the given bean factory.
	 * 
	 * <p> 针对给定的bean工厂合并，转换和处理属性。
	 * 
	 * @throws BeanInitializationException if any properties cannot be loaded - 如果无法加载任何属性
	 */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			Properties mergedProps = mergeProperties();

			// Convert the merged properties, if necessary.
			// 如有必要，转换合并的属性。
			convertProperties(mergedProps);

			// Let the subclass process the properties.
			// 让子类处理属性。
			processProperties(beanFactory, mergedProps);
		}
		catch (IOException ex) {
			throw new BeanInitializationException("Could not load properties", ex);
		}
	}

	/**
	 * Convert the given merged properties, converting property values
	 * if necessary. The result will then be processed.
	 * 
	 * <p> 转换给定的合并属性，必要时转换属性值。 然后将处理结果。
	 * 
	 * <p>The default implementation will invoke {@link #convertPropertyValue}
	 * for each property value, replacing the original with the converted value.
	 * 
	 * <p> 默认实现将为每个属性值调用convertPropertyValue，将原始值替换为转换后的值。
	 * 
	 * @param props the Properties to convert - 要转换的属性
	 * @see #processProperties
	 */
	protected void convertProperties(Properties props) {
		Enumeration<?> propertyNames = props.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = props.getProperty(propertyName);
			String convertedValue = convertProperty(propertyName, propertyValue);
			if (!ObjectUtils.nullSafeEquals(propertyValue, convertedValue)) {
				props.setProperty(propertyName, convertedValue);
			}
		}
	}

	/**
	 * Convert the given property from the properties source to the value
	 * which should be applied.
	 * 
	 * <p> 将给定属性从属性源转换为应该应用的值。
	 * 
	 * <p>The default implementation calls {@link #convertPropertyValue(String)}.
	 * 
	 * <p> 默认实现调用convertPropertyValue（String）。
	 * 
	 * @param propertyName the name of the property that the value is defined for
	 * 
	 * <p> 为其定义值的属性的名称
	 * 
	 * @param propertyValue the original value from the properties source
	 * 
	 * <p> 来自属性源的原始值
	 * 
	 * @return the converted value, to be used for processing - 转换后的值，用于处理
	 * @see #convertPropertyValue(String)
	 */
	protected String convertProperty(String propertyName, String propertyValue) {
		return convertPropertyValue(propertyValue);
	}

	/**
	 * Convert the given property value from the properties source to the value
	 * which should be applied.
	 * 
	 * <p> 将给定属性值从属性源转换为应该应用的值。
	 * 
	 * <p>The default implementation simply returns the original value.
	 * Can be overridden in subclasses, for example to detect
	 * encrypted values and decrypt them accordingly.
	 * 
	 * <p> 默认实现只返回原始值。 可以在子类中重写，例如检测加密值并相应地解密它们。
	 * 
	 * @param originalValue the original value from the properties source
	 * (properties file or local "properties")
	 * 
	 * <p> 来自属性源的原始值（属性文件或本地“属性”）
	 * 
	 * @return the converted value, to be used for processing
	 * 
	 * <p> 转换后的值，用于处理
	 * 
	 * @see #setProperties
	 * @see #setLocations
	 * @see #setLocation
	 * @see #convertProperty(String, String)
	 */
	protected String convertPropertyValue(String originalValue) {
		return originalValue;
	}


	/**
	 * Apply the given Properties to the given BeanFactory.
	 * 
	 * <p> 将给定的Properties应用于给定的BeanFactory。
	 * 
	 * @param beanFactory the BeanFactory used by the application context
	 * 
	 * <p> 应用程序上下文使用的BeanFactory
	 * 
	 * @param props the Properties to apply - 要应用的属性
	 * @throws org.springframework.beans.BeansException in case of errors - 如果有错误
	 */
	protected abstract void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException;

}
