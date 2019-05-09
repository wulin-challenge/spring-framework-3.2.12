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

package org.springframework.beans.factory.config;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanInitializationException;

/**
 * Property resource configurer that overrides bean property values in an application
 * context definition. It <i>pushes</i> values from a properties file into bean definitions.
 * 
 * <p> 属性资源配置器，它覆盖应用程序上下文定义中的bean属性值。 它将属性文件中的值推送到bean定义中。
 *
 * <p>Configuration lines are expected to be of the following form:
 * 
 * <p> 配置行应具有以下形式：
 *
 * <pre class="code">beanName.property=value</pre>
 *
 * Example properties file:
 * 
 * <p> 示例属性文件
 *
 * <pre class="code">dataSource.driverClassName=com.mysql.jdbc.Driver
 * dataSource.url=jdbc:mysql:mydb</pre>
 *
 * In contrast to PropertyPlaceholderConfigurer, the original definition can have default
 * values or no values at all for such bean properties. If an overriding properties file does
 * not have an entry for a certain bean property, the default context definition is used.
 * 
 * <p> 与PropertyPlaceholderConfigurer相比，原始定义对于此类bean属性可以具有默认值或根本没有值。 
 * 如果覆盖属性文件没有某个bean属性的条目，则使用默认上下文定义。
 *
 * <p>Note that the context definition <i>is not</i> aware of being overridden;
 * so this is not immediately obvious when looking at the XML definition file.
 * Furthermore, note that specified override values are always <i>literal</i> values;
 * they are not translated into bean references. This also applies when the original
 * value in the XML bean definition specifies a bean reference.
 * 
 * <p> 请注意，上下文定义不知道被覆盖; 因此，在查看XML定义文件时，这并不是很明显。 
 * 此外，请注意指定的覆盖值始终是文字值; 它们不会被翻译成bean引用。 当XML bean定义中的原始值指定bean引用时，这也适用。
 *
 * <p>In case of multiple PropertyOverrideConfigurers that define different values for
 * the same bean property, the <i>last</i> one will win (due to the overriding mechanism).
 * 
 * <p> 如果多个PropertyOverrideConfigurers为同一个bean属性定义不同的值，则最后一个将获胜（由于覆盖机制）。
 *
 * <p>Property values can be converted after reading them in, through overriding
 * the {@code convertPropertyValue} method. For example, encrypted values
 * can be detected and decrypted accordingly before processing them.
 * 
 * <p> 通过覆盖convertPropertyValue方法，可以在读取属性值后转换它们。 例如，可以在处理加密值之前相应地检测和解密加密值。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 12.03.2003
 * @see #convertPropertyValue
 * @see PropertyPlaceholderConfigurer
 */
public class PropertyOverrideConfigurer extends PropertyResourceConfigurer {

	public static final String DEFAULT_BEAN_NAME_SEPARATOR = ".";


	private String beanNameSeparator = DEFAULT_BEAN_NAME_SEPARATOR;

	private boolean ignoreInvalidKeys = false;

	/**
	 * Contains names of beans that have overrides
	 * (using a ConcurrentHashMap as a Set)
	 * 
	 * <p> 包含具有覆盖的bean的名称（使用ConcurrentHashMap作为Set）
	 */
	private Map<String, Boolean> beanNames = new ConcurrentHashMap<String, Boolean>(16);


	/**
	 * Set the separator to expect between bean name and property path.
	 * Default is a dot (".").
	 * 
	 * <p> 在bean名称和属性路径之间设置分隔符。 默认是一个点（“。”）。
	 */
	public void setBeanNameSeparator(String beanNameSeparator) {
		this.beanNameSeparator = beanNameSeparator;
	}

	/**
	 * Set whether to ignore invalid keys. Default is "false".
	 * 
	 * <p> 设置是否忽略无效键。 默认为“false”。
	 * 
	 * <p>If you ignore invalid keys, keys that do not follow the 'beanName.property' format
	 * (or refer to invalid bean names or properties) will just be logged at debug level.
	 * This allows one to have arbitrary other keys in a properties file.
	 * 
	 * <p> 如果忽略无效键，则不会在调试级别记录不遵循“beanName.property”格式的键（或引用无效的bean名称或属性）。 
	 * 这允许在属性文件中具有任意其他键。
	 */
	public void setIgnoreInvalidKeys(boolean ignoreInvalidKeys) {
		this.ignoreInvalidKeys = ignoreInvalidKeys;
	}


	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException {

		for (Enumeration names = props.propertyNames(); names.hasMoreElements();) {
			String key = (String) names.nextElement();
			try {
				processKey(beanFactory, key, props.getProperty(key));
			}
			catch (BeansException ex) {
				String msg = "Could not process key '" + key + "' in PropertyOverrideConfigurer";
				if (!this.ignoreInvalidKeys) {
					throw new BeanInitializationException(msg, ex);
				}
				if (logger.isDebugEnabled()) {
					logger.debug(msg, ex);
				}
			}
		}
	}

	/**
	 * Process the given key as 'beanName.property' entry.
	 * 
	 * <p> 将给定键作为'beanName.property'条目处理。
	 * 
	 */
	protected void processKey(ConfigurableListableBeanFactory factory, String key, String value)
			throws BeansException {

		int separatorIndex = key.indexOf(this.beanNameSeparator);
		if (separatorIndex == -1) {
			throw new BeanInitializationException("Invalid key '" + key +
					"': expected 'beanName" + this.beanNameSeparator + "property'");
		}
		String beanName = key.substring(0, separatorIndex);
		String beanProperty = key.substring(separatorIndex+1);
		this.beanNames.put(beanName, Boolean.TRUE);
		applyPropertyValue(factory, beanName, beanProperty, value);
		if (logger.isDebugEnabled()) {
			logger.debug("Property '" + key + "' set to value [" + value + "]");
		}
	}

	/**
	 * Apply the given property value to the corresponding bean.
	 */
	protected void applyPropertyValue(
			ConfigurableListableBeanFactory factory, String beanName, String property, String value) {

		BeanDefinition bd = factory.getBeanDefinition(beanName);
		while (bd.getOriginatingBeanDefinition() != null) {
			bd = bd.getOriginatingBeanDefinition();
		}
		PropertyValue pv = new PropertyValue(property, value);
		pv.setOptional(this.ignoreInvalidKeys);
		bd.getPropertyValues().addPropertyValue(pv);
	}


	/**
	 * Were there overrides for this bean?
	 * Only valid after processing has occurred at least once.
	 * @param beanName name of the bean to query status for
	 * @return whether there were property overrides for
	 * the named bean
	 */
	public boolean hasPropertyOverridesFor(String beanName) {
		return this.beanNames.containsKey(beanName);
	}

}
