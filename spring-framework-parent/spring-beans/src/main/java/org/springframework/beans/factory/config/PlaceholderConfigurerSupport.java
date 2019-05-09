/*
 * Copyright 2002-2013 the original author or authors.
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

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.StringValueResolver;

/**
 * Abstract base class for property resource configurers that resolve placeholders
 * in bean definition property values. Implementations <em>pull</em> values from a
 * properties file or other {@linkplain org.springframework.core.env.PropertySource
 * property source} into bean definitions.
 * 
 * <p> 属性资源配置器的抽象基类，用于解析bean定义属性值中的占位符。 实现将值从属性文件或其他属性源拉入bean定义。
 *
 * <p>The default placeholder syntax follows the Ant / Log4J / JSP EL style:
 * 
 * <p> 默认占位符语法遵循Ant / Log4J / JSP EL样式：
 *
 *<pre class="code">${...}</pre>
 *
 * Example XML bean definition:
 * 
 * <p> 示例XML bean定义：
 *
 *<pre class="code">{@code
 *<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource"/>
 *    <property name="driverClassName" value="}${driver}{@code "/>
 *    <property name="url" value="jdbc:}${dbname}{@code "/>
 *</bean>
 *}</pre>
 *
 * Example properties file:
 * 
 * <p> 示例属性文件：
 *
 * <pre class="code"> driver=com.mysql.jdbc.Driver
 * dbname=mysql:mydb</pre>
 *
 * Annotated bean definitions may take advantage of property replacement using
 * the {@link org.springframework.beans.factory.annotation.Value @Value} annotation:
 * 
 * <p> 带注释的bean定义可以使用@Value注释来利用属性替换：
 *
 *<pre class="code">@Value("${person.age}")</pre>
 *
 * Implementations check simple property values, lists, maps, props, and bean names
 * in bean references. Furthermore, placeholder values can also cross-reference
 * other placeholders, like:
 * 
 * <p> 实现检查bean引用中的简单属性值，列表，映射，道具和bean名称。 此外，占位符值还可以交叉引用其他占位符，例如：
 *
 *<pre class="code">rootPath=myrootdir
 *subPath=${rootPath}/subdir</pre>
 *
 * In contrast to {@link PropertyOverrideConfigurer}, subclasses of this type allow
 * filling in of explicit placeholders in bean definitions.
 * 
 * <p> 与PropertyOverrideConfigurer相比，此类型的子类允许在bean定义中填充显式占位符。
 *
 * <p>If a configurer cannot resolve a placeholder, a {@link BeanDefinitionStoreException}
 * will be thrown. If you want to check against multiple properties files, specify multiple
 * resources via the {@link #setLocations locations} property. You can also define multiple
 * configurers, each with its <em>own</em> placeholder syntax. Use {@link
 * #ignoreUnresolvablePlaceholders} to intentionally suppress throwing an exception if a
 * placeholder cannot be resolved.
 * 
 * <p> 如果配置程序无法解析占位符，则将抛出BeanDefinitionStoreException。 如果要检查多个属性文件，请通过locations属性指定多个资源。 
 * 您还可以定义多个配置器，每个配置器都有自己的占位符语法。 如果无法解析占位符，请使用ignoreUnresolvablePlaceholders故意禁止抛出异常。
 *
 * <p>Default property values can be defined globally for each configurer instance
 * via the {@link #setProperties properties} property, or on a property-by-property basis
 * using the default value separator which is {@code ":"} by default and
 * customizable via {@link #setValueSeparator(String)}.
 * 
 * <p> 可以通过properties属性为每个configurer实例全局定义默认属性值，也可以使用默认值分隔符逐个属性地定义默认值，
 * 默认值为“：”并可通过setValueSeparator（String）自定义。
 *
 * <p>Example XML property with default value:
 * 
 * <p> 示例具有默认值的XML属性：
 *
 *<pre class="code">{@code
 *  <property name="url" value="jdbc:}${dbname:defaultdb}{@code "/>
 *}</pre>
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see PropertyPlaceholderConfigurer
 * @see org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 */
public abstract class PlaceholderConfigurerSupport extends PropertyResourceConfigurer
		implements BeanNameAware, BeanFactoryAware {

	/** Default placeholder prefix: {@value} */
	/** 默认占位符前缀：“$ {” */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

	/** Default placeholder suffix: {@value} */
	/** 默认占位符后缀：“}” */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	/** Default value separator: {@value} */
	/** 默认值分隔符：“：” */
	public static final String DEFAULT_VALUE_SEPARATOR = ":";


	/** Defaults to {@value #DEFAULT_PLACEHOLDER_PREFIX} */
	/** 默认为 {@value #DEFAULT_PLACEHOLDER_PREFIX} */
	protected String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

	/** Defaults to {@value #DEFAULT_PLACEHOLDER_SUFFIX} */
	/** 默认为 {@value #DEFAULT_PLACEHOLDER_SUFFIX} */
	protected String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

	/** Defaults to {@value #DEFAULT_VALUE_SEPARATOR} */
	/** 默认为 {@value #DEFAULT_VALUE_SEPARATOR} */
	protected String valueSeparator = DEFAULT_VALUE_SEPARATOR;

	protected boolean ignoreUnresolvablePlaceholders = false;

	protected String nullValue;

	private BeanFactory beanFactory;

	private String beanName;


	/**
	 * Set the prefix that a placeholder string starts with.
	 * The default is {@value #DEFAULT_PLACEHOLDER_PREFIX}.
	 * 
	 * <p> 设置占位符字符串开头的前缀。 默认值为“$ {”。
	 */
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}

	/**
	 * Set the suffix that a placeholder string ends with.
	 * The default is {@value #DEFAULT_PLACEHOLDER_SUFFIX}.
	 * 
	 * <p> 设置占位符字符串结尾的后缀。 默认值为“}”。
	 * 
	 */
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}

	/**
	 * Specify the separating character between the placeholder variable
	 * and the associated default value, or {@code null} if no such
	 * special character should be processed as a value separator.
	 * The default is {@value #DEFAULT_VALUE_SEPARATOR}.
	 * 
	 * <p> 指定占位符变量和关联的默认值之间的分隔字符，如果不应将此特殊字符作为值分隔符处理，则为null。 默认值为“：”。
	 * 
	 */
	public void setValueSeparator(String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}

	/**
	 * Set a value that should be treated as {@code null} when
	 * resolved as a placeholder value: e.g. "" (empty String) or "null".
	 * 
	 * <p> 设置一个值，当作为占位符值解析时应该被视为null：例如 “”（空字符串）或“空”。
	 * 
	 * <p>Note that this will only apply to full property values,
	 * not to parts of concatenated values.
	 * 
	 * <p> 请注意，这仅适用于完整属性值，而不适用于部分连接值。
	 * 
	 * <p>By default, no such null value is defined. This means that
	 * there is no way to express {@code null} as a property
	 * value unless you explicitly map a corresponding value here.
	 * 
	 * <p> 默认情况下，不定义此类空值。 这意味着除非您在此处显式映射相应的值，否则无法将null表示为属性值。
	 * 
	 */
	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}

	/**
	 * Set whether to ignore unresolvable placeholders.
	 * 
	 * <p> 设置是否忽略不可解析的占位符。
	 * 
	 * <p>Default is "false": An exception will be thrown if a placeholder fails
	 * to resolve. Switch this flag to "true" in order to preserve the placeholder
	 * String as-is in such a case, leaving it up to other placeholder configurers
	 * to resolve it.
	 * 
	 * <p> 默认值为“false”：如果占位符无法解析，则抛出异常。 将此标志切换为“true”，
	 * 以便在这种情况下保留占位符String，将其留给其他占位符配置器来解决它。
	 * 
	 */
	public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}

	/**
	 * Only necessary to check that we're not parsing our own bean definition,
	 * to avoid failing on unresolvable placeholders in properties file locations.
	 * The latter case can happen with placeholders for system properties in
	 * resource locations.
	 * 
	 * <p> 只需要检查我们是否正在解析我们自己的bean定义，以避免在属性文件位置中无法解析的占位符上失败。 
	 * 后一种情况可能发生在资源位置的系统属性的占位符中。
	 * 
	 * @see #setLocations
	 * @see org.springframework.core.io.ResourceEditor
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Only necessary to check that we're not parsing our own bean definition,
	 * to avoid failing on unresolvable placeholders in properties file locations.
	 * The latter case can happen with placeholders for system properties in
	 * resource locations.
	 * 
	 * <p> 只需要检查我们是否正在解析我们自己的bean定义，以避免在属性文件位置中无法解析的占位符上失败。 
	 * 后一种情况可能发生在资源位置的系统属性的占位符中。
	 * 
	 * @see #setLocations
	 * @see org.springframework.core.io.ResourceEditor
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


	protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
			StringValueResolver valueResolver) {

		BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

		String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
		for (String curName : beanNames) {
			// Check that we're not parsing our own bean definition,
			// to avoid failing on unresolvable placeholders in properties file locations.
			
			// 检查我们是否正在解析自己的bean定义，以避免在属性文件位置中无法解析的占位符失败。
			if (!(curName.equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
				BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(curName);
				try {
					visitor.visitBeanDefinition(bd);
				}
				catch (Exception ex) {
					throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex.getMessage(), ex);
				}
			}
		}

		// New in Spring 2.5: resolve placeholders in alias target names and aliases as well.
		// Spring 2.5中的新功能：解析别名目标名称和别名中的占位符。
		beanFactoryToProcess.resolveAliases(valueResolver);

		// New in Spring 3.0: resolve placeholders in embedded values such as annotation attributes.
		// Spring 3.0中的新功能：解析嵌入值（如注记属性）中的占位符。
		beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
	}

}
