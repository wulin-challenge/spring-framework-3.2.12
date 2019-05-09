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

import java.util.Properties;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.core.Constants;
import org.springframework.core.SpringProperties;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.StringValueResolver;

/**
 * {@link PlaceholderConfigurerSupport} subclass that resolves ${...} placeholders
 * against {@link #setLocation local} {@link #setProperties properties} and/or system properties
 * and environment variables.
 * 
 * <p> PlaceholderConfigurerSupport子类，用于根据本地属性和/或系统属性和环境变量解析$ {...}占位符。
 *
 * <p>As of Spring 3.1, {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * PropertySourcesPlaceholderConfigurer} should be used preferentially over this implementation; it is
 * more flexible through taking advantage of the {@link org.springframework.core.env.Environment Environment} and
 * {@link org.springframework.core.env.PropertySource PropertySource} mechanisms also made available in Spring 3.1.
 * 
 * <p> 从Spring 3.1开始，PropertySourcesPlaceholderConfigurer应优先用于此实现;
 * 通过利用Spring 3.1中提供的Environment和PropertySource机制，它更加灵活。
 *
 * <p>{@link PropertyPlaceholderConfigurer} is still appropriate for use when:
 * 
 * <p> PropertyPlaceholderConfigurer仍然适合在以下情况下使用：
 * 
 * <ul>
 * <li>the {@code spring-context} module is not available (i.e., one is using Spring's
 * {@code BeanFactory} API as opposed to {@code ApplicationContext}).
 * <li>existing configuration makes use of the {@link #setSystemPropertiesMode(int) "systemPropertiesMode"} and/or
 * {@link #setSystemPropertiesModeName(String) "systemPropertiesModeName"} properties. Users are encouraged to move
 * away from using these settings, and rather configure property source search order through the container's
 * {@code Environment}; however, exact preservation of functionality may be maintained by continuing to
 * use {@code PropertyPlaceholderConfigurer}.
 * </ul>
 *
 * <ul>
 * <li> spring-context模块不可用（即，一个使用Spring的BeanFactory API而不是ApplicationContext）。
 * <li> 现有配置使用“systemPropertiesMode”和/或“systemPropertiesModeName”属性。建议用户不要使用这些设置，
 *  而是通过容器的环境配置属性源搜索顺序;但是，通过继续使用PropertyPlaceholderConfigurer可以保持功能的精确保存。
 * </ul>
 * 
 * <p>Prior to Spring 3.1, the {@code <context:property-placeholder/>} namespace element
 * registered an instance of {@code PropertyPlaceholderConfigurer}. It will still do so if
 * using the {@code spring-context-3.0.xsd} definition of the namespace. That is, you can preserve
 * registration of {@code PropertyPlaceholderConfigurer} through the namespace, even if using Spring 3.1;
 * simply do not update your {@code xsi:schemaLocation} and continue using the 3.0 XSD.
 * 
 * <p> 在Spring 3.1之前，<context：property-placeholder /> namespace元素注册了
 * PropertyPlaceholderConfigurer的一个实例。如果使用命名空间的spring-context-3.0.xsd定义，它仍然会这样做。
 * 也就是说，即使使用Spring 3.1，也可以通过命名空间保留PropertyPlaceholderConfigurer的注册;
 * 只是不要更新您的xsi：schemaLocation并继续使用3.0 XSD。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 02.10.2003
 * @see #setSystemPropertiesModeName
 * @see PlaceholderConfigurerSupport
 * @see PropertyOverrideConfigurer
 * @see org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 */
public class PropertyPlaceholderConfigurer extends PlaceholderConfigurerSupport {

	/** Never check system properties. */
	/** 切勿检查系统属性。 */
	public static final int SYSTEM_PROPERTIES_MODE_NEVER = 0;

	/**
	 * Check system properties if not resolvable in the specified properties.
	 * This is the default.
	 * 
	 * <p> 如果在指定的属性中无法解析，请检查系统属性。 这是默认值。
	 */
	public static final int SYSTEM_PROPERTIES_MODE_FALLBACK = 1;

	/**
	 * Check system properties first, before trying the specified properties.
	 * This allows system properties to override any other property source.
	 * 
	 * <p> 在尝试指定的属性之前，首先检查系统属性。 这允许系统属性覆盖任何其他属性源。
	 * 
	 */
	public static final int SYSTEM_PROPERTIES_MODE_OVERRIDE = 2;


	private static final Constants constants = new Constants(PropertyPlaceholderConfigurer.class);

	private int systemPropertiesMode = SYSTEM_PROPERTIES_MODE_FALLBACK;

	private boolean searchSystemEnvironment =
			!SpringProperties.getFlag(AbstractEnvironment.IGNORE_GETENV_PROPERTY_NAME);


	/**
	 * Set the system property mode by the name of the corresponding constant,
	 * e.g. "SYSTEM_PROPERTIES_MODE_OVERRIDE".
	 * 
	 * <p> 通过相应常量的名称设置系统属性模式，例如“SYSTEM_PROPERTIES_MODE_OVERRIDE”。
	 * 
	 * @param constantName name of the constant - 常数的名称
	 * @throws java.lang.IllegalArgumentException if an invalid constant was specified - 如果指定了无效常量
	 * @see #setSystemPropertiesMode
	 */
	public void setSystemPropertiesModeName(String constantName) throws IllegalArgumentException {
		this.systemPropertiesMode = constants.asNumber(constantName).intValue();
	}

	/**
	 * Set how to check system properties: as fallback, as override, or never.
	 * For example, will resolve ${user.dir} to the "user.dir" system property.
	 * 
	 * <p> 设置如何检查系统属性：作为后备，覆盖或永不。 例如，将$ {user.dir}解析为“user.dir”系统属性。
	 * 
	 * <p>The default is "fallback": If not being able to resolve a placeholder
	 * with the specified properties, a system property will be tried.
	 * "override" will check for a system property first, before trying the
	 * specified properties. "never" will not check system properties at all.
	 * 
	 * <p> 默认值为“fallback”：如果无法解析具有指定属性的占位符，则将尝试使用系统属性。 在尝试指定的属性之前，
	 * “override”将首先检查系统属性。 “never”根本不会检查系统属性。
	 * 
	 * @see #SYSTEM_PROPERTIES_MODE_NEVER
	 * @see #SYSTEM_PROPERTIES_MODE_FALLBACK
	 * @see #SYSTEM_PROPERTIES_MODE_OVERRIDE
	 * @see #setSystemPropertiesModeName
	 */
	public void setSystemPropertiesMode(int systemPropertiesMode) {
		this.systemPropertiesMode = systemPropertiesMode;
	}

	/**
	 * Set whether to search for a matching system environment variable
	 * if no matching system property has been found. Only applied when
	 * "systemPropertyMode" is active (i.e. "fallback" or "override"), right
	 * after checking JVM system properties.
	 * 
	 * <p> 设置是否在未找到匹配的系统属性的情况下搜索匹配的系统环境变量。 
	 * 仅在“systemPropertyMode”处于活动状态时（即“后备”或“覆盖”），在检查JVM系统属性后立即应用。
	 * 
	 * <p>Default is "true". Switch this setting off to never resolve placeholders
	 * against system environment variables. Note that it is generally recommended
	 * to pass external values in as JVM system properties: This can easily be
	 * achieved in a startup script, even for existing environment variables.
	 * 
	 * <p> 默认为“true”。 关闭此设置以永远不会根据系统环境变量解析占位符。 请注意，通常建议将外部值作为JVM系统属性传递：
	 * 这可以在启动脚本中轻松实现，即使对于现有环境变量也是如此。
	 * 
	 * <p><b>NOTE:</b> Access to environment variables does not work on the
	 * Sun VM 1.4, where the corresponding {@link System#getenv} support was
	 * disabled - before it eventually got re-enabled for the Sun VM 1.5.
	 * Please upgrade to 1.5 (or higher) if you intend to rely on the
	 * environment variable support.
	 * 
	 * <p> 注意：访问环境变量不适用于Sun VM 1.4，其中相应的System.getenv支持已被禁用 - 在最终为Sun VM 1.5重新启用之前。 
	 * 如果您打算依赖环境变量支持，请升级到1.5（或更高）。
	 * 
	 * @see #setSystemPropertiesMode
	 * @see java.lang.System#getProperty(String)
	 * @see java.lang.System#getenv(String)
	 */
	public void setSearchSystemEnvironment(boolean searchSystemEnvironment) {
		this.searchSystemEnvironment = searchSystemEnvironment;
	}

	/**
	 * Resolve the given placeholder using the given properties, performing
	 * a system properties check according to the given mode.
	 * 
	 * <p> 使用给定属性解析给定占位符，根据给定模式执行系统属性检查。
	 * 
	 * <p>The default implementation delegates to {@code resolvePlaceholder
	 * (placeholder, props)} before/after the system properties check.
	 * 
	 * <p> 默认实现在系统属性检查之前/之后委托给resolvePlaceholder（占位符，props）。
	 * 
	 * <p>Subclasses can override this for custom resolution strategies,
	 * including customized points for the system properties check.
	 * 
	 * <p> 子类可以为自定义解析策略覆盖此选项，包括系统属性检查的自定义点。
	 * 
	 * @param placeholder the placeholder to resolve - 要占用的占位符
	 * @param props the merged properties of this configurer - 此配置程序的合并属性
	 * @param systemPropertiesMode the system properties mode,
	 * according to the constants in this class
	 * 
	 * <p> 系统属性模式，根据此类中的常量
	 * 
	 * @return the resolved value, of null if none - 已解析的值，如果没有则为null
	 * @see #setSystemPropertiesMode
	 * @see System#getProperty
	 * @see #resolvePlaceholder(String, java.util.Properties)
	 */
	protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
		String propVal = null;
		if (systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE) {
			propVal = resolveSystemProperty(placeholder);
		}
		if (propVal == null) {
			propVal = resolvePlaceholder(placeholder, props);
		}
		if (propVal == null && systemPropertiesMode == SYSTEM_PROPERTIES_MODE_FALLBACK) {
			propVal = resolveSystemProperty(placeholder);
		}
		return propVal;
	}

	/**
	 * Resolve the given placeholder using the given properties.
	 * The default implementation simply checks for a corresponding property key.
	 * 
	 * <p> 使用给定的属性解析给定的占位符。 默认实现只检查相应的属性键。
	 * 
	 * <p>Subclasses can override this for customized placeholder-to-key mappings
	 * or custom resolution strategies, possibly just using the given properties
	 * as fallback.
	 * 
	 * <p> 子类可以为自定义的占位符到键映射或自定义解析策略覆盖它，可能只使用给定的属性作为回退。
	 * 
	 * <p>Note that system properties will still be checked before respectively
	 * after this method is invoked, according to the system properties mode.
	 * 
	 * <p> 请注意，根据系统属性模式，在调用此方法之后，仍将分别检查系统属性。
	 * 
	 * @param placeholder the placeholder to resolve - 要占用的占位符
	 * @param props the merged properties of this configurer - 此配置程序的合并属性
	 * @return the resolved value, of {@code null} if none - 已解析的值，如果没有则为null
	 * @see #setSystemPropertiesMode
	 */
	protected String resolvePlaceholder(String placeholder, Properties props) {
		return props.getProperty(placeholder);
	}

	/**
	 * Resolve the given key as JVM system property, and optionally also as
	 * system environment variable if no matching system property has been found.
	 * 
	 * <p> 将给定键解析为JVM系统属性，如果未找到匹配的系统属性，也可以选择将其解析为系统环境变量。
	 * 
	 * @param key the placeholder to resolve as system property key - 占位符要解析为系统属性键
	 * @return the system property value, or {@code null} if not found
	 * 
	 * <p> 系统属性值，如果未找到，则返回null
	 * 
	 * @see #setSearchSystemEnvironment
	 * @see System#getProperty(String)
	 * @see System#getenv(String)
	 */
	protected String resolveSystemProperty(String key) {
		try {
			String value = System.getProperty(key);
			if (value == null && this.searchSystemEnvironment) {
				value = System.getenv(key);
			}
			return value;
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not access system property '" + key + "': " + ex);
			}
			return null;
		}
	}


	/**
	 * Visit each bean definition in the given bean factory and attempt to replace ${...} property
	 * placeholders with values from the given properties.
	 * 
	 * <p> 访问给定bean工厂中的每个bean定义，并尝试使用给定属性中的值替换$ {...}属性占位符。
	 * 
	 */
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {

		StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(props);

		this.doProcessProperties(beanFactoryToProcess, valueResolver);
	}

	/**
	 * Parse the given String value for placeholder resolution.
	 * 
	 * <p> 解析占位符分辨率的给定String值。
	 * 
	 * @param strVal the String value to parse - 要解析的String值
	 * @param props the Properties to resolve placeholders against - 解决占位符的属性
	 * @param visitedPlaceholders the placeholders that have already been visited
	 * during the current resolution attempt (ignored in this version of the code)
	 * 
	 * <p> 在当前解析尝试期间已访问过的占位符（在此版本的代码中被忽略）
	 * 
	 * @deprecated as of Spring 3.0, in favor of using {@link #resolvePlaceholder}
	 * with {@link org.springframework.util.PropertyPlaceholderHelper}.
	 * Only retained for compatibility with Spring 2.5 extensions.
	 * 
	 * <p> 从Spring 3.0开始，支持将resolvePlaceholder与org.springframework.util.PropertyPlaceholderHelper一起使用。 
	 * 仅保留与Spring 2.5扩展的兼容性。
	 */
	@Deprecated
	protected String parseStringValue(String strVal, Properties props, Set<?> visitedPlaceholders) {
		PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(
				placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
		PlaceholderResolver resolver = new PropertyPlaceholderConfigurerResolver(props);
		return helper.replacePlaceholders(strVal, resolver);
	}


	private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

		private final PropertyPlaceholderHelper helper;

		private final PlaceholderResolver resolver;

		public PlaceholderResolvingStringValueResolver(Properties props) {
			this.helper = new PropertyPlaceholderHelper(
					placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
			this.resolver = new PropertyPlaceholderConfigurerResolver(props);
		}

		public String resolveStringValue(String strVal) throws BeansException {
			String value = this.helper.replacePlaceholders(strVal, this.resolver);
			return (value.equals(nullValue) ? null : value);
		}
	}


	private class PropertyPlaceholderConfigurerResolver implements PlaceholderResolver {

		private final Properties props;

		private PropertyPlaceholderConfigurerResolver(Properties props) {
			this.props = props;
		}

		public String resolvePlaceholder(String placeholderName) {
			return PropertyPlaceholderConfigurer.this.resolvePlaceholder(placeholderName, props, systemPropertiesMode);
		}
	}

}
