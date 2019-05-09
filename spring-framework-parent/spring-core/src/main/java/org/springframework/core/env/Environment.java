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

package org.springframework.core.env;

/**
 * Interface representing the environment in which the current application is running.
 * Models two key aspects of the application environment: <em>profiles</em> and
 * <em>properties</em>. Methods related to property access are exposed via the
 * {@link PropertyResolver} superinterface.
 * 
 * <p>表示当前应用程序正在运行的环境的接口。模拟应用程序环境的两个关键方面：配置文件和属性。与属性访问相关的方法通过PropertyResolver超接口公开。
 *
 * <p>A <em>profile</em> is a named, logical group of bean definitions to be registered
 * with the container only if the given profile is <em>active</em>. Beans may be assigned
 * to a profile whether defined in XML or via annotations; see the spring-beans 3.1 schema
 * or the {@link org.springframework.context.annotation.Profile @Profile} annotation for
 * syntax details. The role of the {@code Environment} object with relation to profiles is
 * in determining which profiles (if any) are currently {@linkplain #getActiveProfiles
 * active}, and which profiles (if any) should be {@linkplain #getDefaultProfiles active
 * by default}.
 * 
 * <p>配置文件是仅在给定配置文件处于活动状态时才向容器注册的Bean定义的命名逻辑组。可以将Bean分配给配置文件，
 * 无论是以XML还是通过注释定义;有关语法详细信息，请参阅spring-beans 3.1模式或@Profile注释。与配置文件相
 * 关的Environment对象的作用是确定哪些配置文件（如果有）当前处于活动状态，以及默认情况下哪些配置文件（如果有）应处于活动状态。
 *
 * <p><em>Properties</em> play an important role in almost all applications, and may
 * originate from a variety of sources: properties files, JVM system properties, system
 * environment variables, JNDI, servlet context parameters, ad-hoc Properties objects,
 * Maps, and so on. The role of the environment object with relation to properties is to
 * provide the user with a convenient service interface for configuring property sources
 * and resolving properties from them.
 * 
 * <p>属性在几乎所有应用程序中都发挥着重要作用，并且可能源自各种源：属性文件，JVM系统属性，系统环境变量，JNDI，
 * servlet上下文参数，ad-hoc属性对象，映射等。与属性相关的环境对象的作用是为用户提供方便的服务接口，用于配置属性源和从中解析属性。
 *
 * <p>Beans managed within an {@code ApplicationContext} may register to be {@link
 * org.springframework.context.EnvironmentAware EnvironmentAware} or {@code @Inject} the
 * {@code Environment} in order to query profile state or resolve properties directly.
 * 
 * <p>在ApplicationContext中管理的Bean可以注册为EnvironmentAware或@Inject the Environment，
 * 以便直接查询配置文件状态或解析属性。
 *
 * <p>In most cases, however, application-level beans should not need to interact with the
 * {@code Environment} directly but instead may have to have {@code ${...}} property
 * values replaced by a property placeholder configurer such as
 * {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * PropertySourcesPlaceholderConfigurer}, which itself is {@code EnvironmentAware} and
 * as of Spring 3.1 is registered by default when using
 * {@code <context:property-placeholder/>}.
 * 
 * <p>但是，在大多数情况下，应用程序级bean不需要直接与Environment交互，而是可能必须将$ {...}}属性值替换为属性占位符配置器，
 * 例如PropertySourcesPlaceholderConfigurer，它本身就是EnvironmentAware，并且 使
 * 用<context：property-placeholder />时，默认情况下会注册Spring 3.1。
 *
 * <p>Configuration of the environment object must be done through the
 * {@code ConfigurableEnvironment} interface, returned from all
 * {@code AbstractApplicationContext} subclass {@code getEnvironment()} methods. See
 * {@link ConfigurableEnvironment} Javadoc for usage examples demonstrating manipulation
 * of property sources prior to application context {@code refresh()}.
 * 
 * <p>必须通过ConfigurableEnvironment接口完成环境对象的配置，该接口从所有AbstractApplicationContext子
 * 类getEnvironment（）方法返回。 有关在应用程序上下文 refresh()之前演示属性源操作的用法示例，
 * 请参阅ConfigurableEnvironment Javadoc。
 *
 * @author Chris Beams
 * @since 3.1
 * @see PropertyResolver
 * @see EnvironmentCapable
 * @see ConfigurableEnvironment
 * @see AbstractEnvironment
 * @see StandardEnvironment
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#setEnvironment
 * @see org.springframework.context.support.AbstractApplicationContext#createEnvironment
 */
public interface Environment extends PropertyResolver {

	/**
	 * Return the set of profiles explicitly made active for this environment. Profiles
	 * are used for creating logical groupings of bean definitions to be registered
	 * conditionally, for example based on deployment environment.  Profiles can be
	 * activated by setting {@linkplain AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 * "spring.profiles.active"} as a system property or by calling
	 * {@link ConfigurableEnvironment#setActiveProfiles(String...)}.
	 * 
	 * <p>返回显式为此环境激活的配置文件集。 配置文件用于创建要有条件地注册的bean定义的逻辑分组，例如基于部署环境。 
	 * 可以通过将“spring.profiles.active”设置为系统属性或通过调
	 * 用ConfigurableEnvironment.setActiveProfiles（String）来激活配置文件。
	 * 
	 * <p>If no profiles have explicitly been specified as active, then any {@linkplain
	 * #getDefaultProfiles() default profiles} will automatically be activated.
	 * 
	 * <p>如果未明确指定任何配置文件为活动，则将自动激活任何默认配置文件。
	 * 
	 * @see #getDefaultProfiles
	 * @see ConfigurableEnvironment#setActiveProfiles
	 * @see AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 */
	String[] getActiveProfiles();

	/**
	 * Return the set of profiles to be active by default when no active profiles have
	 * been set explicitly.
	 * 
	 * <p>如果未明确设置活动配置文件，则默认情况下返回要激活的配置文件集。
	 * 
	 * @see #getActiveProfiles
	 * @see ConfigurableEnvironment#setDefaultProfiles
	 * @see AbstractEnvironment#DEFAULT_PROFILES_PROPERTY_NAME
	 */
	String[] getDefaultProfiles();

	/**
	 * Return whether one or more of the given profiles is active or, in the case of no
	 * explicit active profiles, whether one or more of the given profiles is included in
	 * the set of default profiles. If a profile begins with '!' the logic is inverted,
	 * i.e. the method will return true if the given profile is <em>not</em> active.
	 * For example, <pre class="code">env.acceptsProfiles("p1", "!p2")</pre> will
	 * return {@code true} if profile 'p1' is active or 'p2' is not active.
	 * 
	 * <p>返回一个或多个给定配置文件是否处于活动状态，或者在没有显式活动配置文件的情况下，返回一组或多个给定配置文件是否包含在默认配置文件集中。 如果个人资料以'！'开头 逻辑被反转，即如果给定的配置文件不活动，该方法将返回true。 例如，
	 * <pre class="code">env.acceptsProfiles("p1", "!p2")</pre>
	 * 如果配置文件“p1”处于活动状态或“p2”未激活，则返回true。
	 * @throws IllegalArgumentException if called with zero arguments
	 * or if any profile is {@code null}, empty or whitespace-only
	 * @see #getActiveProfiles
	 * @see #getDefaultProfiles
	 */
	boolean acceptsProfiles(String... profiles);

}
