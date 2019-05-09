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

package org.springframework.core.env;

import java.util.Map;

/**
 * Configuration interface to be implemented by most if not all {@link Environment} types.
 * Provides facilities for setting active and default profiles and manipulating underlying
 * property sources. Allows clients to set and validate required properties, customize the
 * conversion service and more through the {@link ConfigurablePropertyResolver}
 * superinterface.
 * 
 * <p>配置接口由大多数（如果不是全部）环境类型实现。 提供用于设置活动和默认配置文件以及操作基础属性源的工具。 
 * 允许客户端通过ConfigurablePropertyResolver超级接口设置和验证所需的属性，自定义转换服务等。
 *
 * <h2>Manipulating property sources</h2>
 * 
 * <h2>操作属性源</h2>
 * 
 * <p>Property sources may be removed, reordered, or replaced; and additional
 * property sources may be added using the {@link MutablePropertySources}
 * instance returned from {@link #getPropertySources()}. The following examples
 * are against the {@link StandardEnvironment} implementation of
 * {@code ConfigurableEnvironment}, but are generally applicable to any implementation,
 * though particular default property sources may differ.
 * 
 * <p>属性源可能被删除，重新排序或替换; 可以使用从getPropertySources（）返回的MutablePropertySources实例添加其他属性源。
 *  以下示例针对ConfigurableEnvironment的StandardEnvironment实现，但通常适用于任何实现，但特定的默认属性源可能不同。
 *
 * <h4>Example: adding a new property source with highest search priority</h4>
 * <h4>示例：添加具有最高搜索优先级的新属性源</h4>
 * <pre class="code">
 *   ConfigurableEnvironment environment = new StandardEnvironment();
 *   MutablePropertySources propertySources = environment.getPropertySources();
 *   Map<String, String> myMap = new HashMap<String, String>();
 *   myMap.put("xyz", "myValue");
 *   propertySources.addFirst(new MapPropertySource("MY_MAP", myMap));
 * </pre>
 *
 * <h4>Example: removing the default system properties property source</h4>
 * <h4>示例：删除默认系统属性属性源</h4>
 * <pre class="code">
 *   MutablePropertySources propertySources = environment.getPropertySources();
 *   propertySources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)
 * </pre>
 *
 * <h4>Example: mocking the system environment for testing purposes</h4>
 * <h4>示例：模拟系统环境以进行测试</h4>
 * <pre class="code">
 *   MutablePropertySources propertySources = environment.getPropertySources();
 *   MockPropertySource mockEnvVars = new MockPropertySource().withProperty("xyz", "myValue");
 *   propertySources.replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, mockEnvVars);
 * </pre>
 *
 * When an {@link Environment} is being used by an {@code ApplicationContext}, it is
 * important that any such {@code PropertySource} manipulations be performed
 * <em>before</em> the context's {@link
 * org.springframework.context.support.AbstractApplicationContext#refresh() refresh()}
 * method is called. This ensures that all property sources are available during the
 * container bootstrap process, including use by {@linkplain
 * org.springframework.context.support.PropertySourcesPlaceholderConfigurer property
 * placeholder configurers}.
 * 
 * <p>当ApplicationContext使用Environment时，重要的是在调用上下文的refresh（）方法之前执行任何此类PropertySource操作。 
 * 这可确保在容器引导过程中所有属性源都可用，包括属性占位符配置器使用。
 *
 *
 * @author Chris Beams
 * @since 3.1
 * @see StandardEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment
 */
public interface ConfigurableEnvironment extends Environment, ConfigurablePropertyResolver {

	/**
	 * Specify the set of profiles active for this {@code Environment}. Profiles are
	 * evaluated during container bootstrap to determine whether bean definitions
	 * should be registered with the container.
	 * 
	 * <p>指定此环境的活动配置文件集。 在容器引导期间评估配置文件以确定是否应该向容器注册bean定义。
	 * 
	 * <p>Any existing active profiles will be replaced with the given arguments; call
	 * with zero arguments to clear the current set of active profiles. Use
	 * {@link #addActiveProfile} to add a profile while preserving the existing set.
	 * 
	 * <p>任何现有的活动配置文件都将替换为给定的参数; 使用零参数调用以清除当前的活动配置文件集。 
	 * 使用addActiveProfile添加配置文件，同时保留现有集。
	 * @see #addActiveProfile
	 * @see #setDefaultProfiles
	 * @see org.springframework.context.annotation.Profile
	 * @see AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 * @throws IllegalArgumentException if any profile is null, empty or whitespace-only
	 */
	void setActiveProfiles(String... profiles);

	/**
	 * Add a profile to the current set of active profiles.
	 * 
	 * <p>将配置文件添加到当前活动配置文件集。
	 * 
	 * @see #setActiveProfiles
	 * @throws IllegalArgumentException if the profile is null, empty or whitespace-only
	 * <p>如果配置文件为null，空或仅空白
	 */
	void addActiveProfile(String profile);

	/**
	 * Specify the set of profiles to be made active by default if no other profiles
	 * are explicitly made active through {@link #setActiveProfiles}.
	 * 
	 * <p>如果没有通过setActiveProfiles显式激活其他配置文件，则指定默认情况下要激活的配置文件集。
	 * 
	 * @see AbstractEnvironment#DEFAULT_PROFILES_PROPERTY_NAME
	 * @throws IllegalArgumentException if any profile is null, empty or whitespace-only
	 * <p>如果任何配置文件为null，空或仅空白
	 */
	void setDefaultProfiles(String... profiles);

	/**
	 * Return the {@link PropertySources} for this {@code Environment} in mutable form,
	 * allowing for manipulation of the set of {@link PropertySource} objects that should
	 * be searched when resolving properties against this {@code Environment} object.
	 * The various {@link MutablePropertySources} methods such as
	 * {@link MutablePropertySources#addFirst addFirst},
	 * {@link MutablePropertySources#addFirst addLast},
	 * {@link MutablePropertySources#addFirst addBefore} and
	 * {@link MutablePropertySources#addFirst addAfter} allow for fine-grained control
	 * over property source ordering. This is useful, for example, in ensuring that
	 * certain user-defined property sources have search precedence over default property
	 * sources such as the set of system properties or the set of system environment
	 * variables.
	 * 
	 * <p>以可变形式返回此环境的PropertySources，允许操作在解析此Environment对象的属性时应搜索的PropertySource对象集。 
	 * 各种MutablePropertySources方法（如addFirst，addLast，addBefore和addAfter）允许对属性源排序进行细粒度控制。
	 *  例如，这有助于确保某些用户定义的属性源具有优先于默认属性源（例如系统属性集或系统环境变量集）的搜索优先级。

	 * @see AbstractEnvironment#customizePropertySources
	 */
	MutablePropertySources getPropertySources();

	/**
	 * Return the value of {@link System#getenv()} if allowed by the current
	 * {@link SecurityManager}, otherwise return a map implementation that will attempt
	 * to access individual keys using calls to {@link System#getenv(String)}.
	 * 
	 * <p>如果当前SecurityManager允许，则返回System.getenv（）的值，否则返回将尝试使
	 * 用对System.getenv（String）的调用来访问各个键的映射实现。
	 * 
	 * <p>Note that most {@link Environment} implementations will include this system
	 * environment map as a default {@link PropertySource} to be searched. Therefore, it
	 * is recommended that this method not be used directly unless bypassing other
	 * property sources is expressly intended.
	 * 
	 * <p>请注意，大多数Environment实现都将此系统环境映射作为要搜索的默认PropertySource。 因此，
	 * 建议不要直接使用此方法，除非明确地绕过其他属性源。
	 * 
	 * <p>Calls to {@link Map#get(Object)} on the Map returned will never throw
	 * {@link IllegalAccessException}; in cases where the SecurityManager forbids access
	 * to a property, {@code null} will be returned and an INFO-level log message will be
	 * issued noting the exception.
	 * 
	 * <p>在返回的Map上调用Map.get（Object）将永远不会抛出IllegalAccessException; 
	 * 在SecurityManager禁止访问属性的情况下，将返回null并发出INFO级别的日志消息，注意该异常。
	 */
	Map<String, Object> getSystemEnvironment();

	/**
	 * Return the value of {@link System#getProperties()} if allowed by the current
	 * {@link SecurityManager}, otherwise return a map implementation that will attempt
	 * to access individual keys using calls to {@link System#getProperty(String)}.
	 * 
	 * <p>如果当前SecurityManager允许，则返回System.getProperties（）的值，
	 * 否则返回将尝试使用对System.getProperty（String）的调用来访问各个键的map实现。
	 * 
	 * <p>Note that most {@code Environment} implementations will include this system
	 * properties map as a default {@link PropertySource} to be searched. Therefore, it is
	 * recommended that this method not be used directly unless bypassing other property
	 * sources is expressly intended.
	 * 
	 * <p>请注意，大多数Environment实现都将此系统属性映射包含为要搜索的默认PropertySource。 
	 * 因此，建议不要直接使用此方法，除非明确地绕过其他属性源。
	 * 
	 * <p>Calls to {@link Map#get(Object)} on the Map returned will never throw
	 * {@link IllegalAccessException}; in cases where the SecurityManager forbids access
	 * to a property, {@code null} will be returned and an INFO-level log message will be
	 * issued noting the exception.
	 * 
	 * <p>在返回的Map上调用Map.get（Object）将永远不会抛出IllegalAccessException; 在SecurityManager禁
	 * 止访问属性的情况下，将返回null并发出INFO级别的日志消息，注意该异常。
	 */
	Map<String, Object> getSystemProperties();

	/**
	 * Append the given parent environment's active profiles, default profiles and
	 * property sources to this (child) environment's respective collections of each.
	 * 
	 * <p>将给定父环境的活动配置文件，默认配置文件和属性源附加到此（子）环境的各个集合。
	 * 
	 * <p>For any identically-named {@code PropertySource} instance existing in both
	 * parent and child, the child instance is to be preserved and the parent instance
	 * discarded. This has the effect of allowing overriding of property sources by the
	 * child as well as avoiding redundant searches through common property source types,
	 * e.g. system environment and system properties.
	 * 
	 * <p>对于父和子中都存在的任何具有相同名称的PropertySource实例，将保留子实例并丢弃父实例。
	 *  这具有允许孩子覆盖属性源以及避免通过公共属性源类型的冗余搜索的效果，例如， 系统环境和系统属性。
	 *  
	 * <p>Active and default profile names are also filtered for duplicates, to avoid
	 * confusion and redundant storage.
	 * 
	 * <p>还会对活动和默认配置文件名称进行重复过滤，以避免混淆和冗余存储。
	 * 
	 * <p>The parent environment remains unmodified in any case. Note that any changes to
	 * the parent environment occurring after the call to {@code merge} will not be
	 * reflected in the child. Therefore, care should be taken to configure parent
	 * property sources and profile information prior to calling {@code merge}.
	 * 
	 * <p>在任何情况下，父环境都保持不变。 请注意，在调用merge之后发生的对父环境的任何更改都不会反映在子项中。
	 *  因此，在调用merge之前，应注意配置父属性源和配置文件信息。
	 *  
	 * @param parent the environment to merge with - 合并的环境
	 * @since 3.1.2
	 * @see org.springframework.context.support.AbstractApplicationContext#setParent
	 */
	void merge(ConfigurableEnvironment parent);

}
