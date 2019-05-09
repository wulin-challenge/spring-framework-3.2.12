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

import java.beans.PropertyEditor;
import java.security.AccessControlContext;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringValueResolver;

/**
 * Configuration interface to be implemented by most bean factories. Provides
 * facilities to configure a bean factory, in addition to the bean factory
 * client methods in the {@link org.springframework.beans.factory.BeanFactory}
 * interface.
 * 
 * <p>配置接口由大多数bean工厂实现。 除了org.springframework.beans.factory.BeanFactory接口中的bean工厂
 * 客户机方法之外，还提供配置Bean工厂的工具。
 * 
 *
 * <p>This bean factory interface is not meant to be used in normal application
 * code: Stick to {@link org.springframework.beans.factory.BeanFactory} or
 * {@link org.springframework.beans.factory.ListableBeanFactory} for typical
 * needs. This extended interface is just meant to allow for framework-internal
 * plug'n'play and for special access to bean factory configuration methods.
 *
 *<p>这个bean工厂接口并不适用于普通的应用程序代码：坚持使用org.springframework.beans.factory.BeanFactory
 *或org.springframework.beans.factory.ListableBeanFactory来满足典型需求。 这个扩展接口只是为了允许框架内部的
 *即插即用和对bean工厂配置方法的特殊访问。
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see ConfigurableListableBeanFactory
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

	/**
	 * Scope identifier for the standard singleton scope: "singleton".
	 * Custom scopes can be added via {@code registerScope}.
	 * 
	 * <p>标准单例范围的范围标识符：“singleton”。 可以通过registerScope添加自定义范围。
	 * 
	 * @see #registerScope
	 */
	String SCOPE_SINGLETON = "singleton";

	/**
	 * Scope identifier for the standard prototype scope: "prototype".
	 * Custom scopes can be added via {@code registerScope}.
	 * 
	 * <p>标准原型范围的范围标识符：“prototype”。 可以通过registerScope添加自定义范围。
	 * 
	 * @see #registerScope
	 */
	String SCOPE_PROTOTYPE = "prototype";


	/**
	 * Set the parent of this bean factory.
	 * 
	 * <p>设置此bean工厂的父级。
	 * 
	 * <p>Note that the parent cannot be changed: It should only be set outside
	 * a constructor if it isn't available at the time of factory instantiation.
	 * 
	 * <p>请注意，不能更改父级：如果在工厂实例化时不可用，则只应在构造函数外部设置父级。
	 * 
	 * @param parentBeanFactory the parent BeanFactory - 父亲BeanFactory
	 * @throws IllegalStateException if this factory is already associated with
	 * a parent BeanFactory - 如果此工厂已与父BeanFactory关联
	 * @see #getParentBeanFactory()
	 */
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

	/**
	 * Set the class loader to use for loading bean classes.
	 * Default is the thread context class loader.
	 * 
	 * <p>设置类加载器以用于加载bean类。 默认是线程上下文类加载器。
	 * 
	 * <p>Note that this class loader will only apply to bean definitions
	 * that do not carry a resolved bean class yet. This is the case as of
	 * Spring 2.0 by default: Bean definitions only carry bean class names,
	 * to be resolved once the factory processes the bean definition.
	 * 
	 * <p> 请注意，此类加载器仅适用于尚未带有已解析bean类的bean定义。 默认情况下就是Spring 2.0的情
	 * 况：Bean定义只带有bean类名，一旦工厂处理bean定义就要解析。
	 * 
	 * @param beanClassLoader the class loader to use,
	 * or {@code null} to suggest the default class loader
	 * 
	 * <p>要使用的类加载器，或null以建议默认的类加载器
	 */
	void setBeanClassLoader(ClassLoader beanClassLoader);

	/**
	 * Return this factory's class loader for loading bean classes.
	 * 
	 * <p>返回此工厂的类加载器以加载bean类。
	 * 
	 */
	ClassLoader getBeanClassLoader();

	/**
	 * Specify a temporary ClassLoader to use for type matching purposes.
	 * Default is none, simply using the standard bean ClassLoader.
	 * 
	 * <p>指定用于类型匹配目的的临时ClassLoader。 默认为none，只使用标准bean ClassLoader。
	 * 
	 * <p>A temporary ClassLoader is usually just specified if
	 * <i>load-time weaving</i> is involved, to make sure that actual bean
	 * classes are loaded as lazily as possible. The temporary loader is
	 * then removed once the BeanFactory completes its bootstrap phase.
	 * 
	 * <p>如果涉及加载时编织，通常只指定一个临时的ClassLoader，以确保尽可能懒惰地加载实际的bean类。 
	 * 一旦BeanFactory完成其引导阶段，就会删除临时加载程序。
	 * 
	 * @since 2.5
	 */
	void setTempClassLoader(ClassLoader tempClassLoader);

	/**
	 * Return the temporary ClassLoader to use for type matching purposes,
	 * if any.
	 * 
	 * <p>返回临时ClassLoader以用于类型匹配目的（如果有）。
	 * 
	 * @since 2.5
	 */
	ClassLoader getTempClassLoader();

	/**
	 * Set whether to cache bean metadata such as given bean definitions
	 * (in merged fashion) and resolved bean classes. Default is on.
	 * 
	 * <p>设置是否缓存bean元数据，例如给定的bean定义（以合并方式）和已解析的bean类。 默认打开。
	 * 
	 * <p>Turn this flag off to enable hot-refreshing of bean definition objects
	 * and in particular bean classes. If this flag is off, any creation of a bean
	 * instance will re-query the bean class loader for newly resolved classes.
	 * 
	 * <p>关闭此标志以启用bean定义对象的热刷新，特别是bean类。 如果此标志关闭，则任何bean实例的创建都将为新解析的类重新查询bean类加载器。
	 * 
	 */
	void setCacheBeanMetadata(boolean cacheBeanMetadata);

	/**
	 * Return whether to cache bean metadata such as given bean definitions
	 * (in merged fashion) and resolved bean classes.
	 * 
	 * <p>返回是否缓存bean元数据，例如给定的bean定义（以合并方式）和已解析的bean类。
	 * 
	 */
	boolean isCacheBeanMetadata();

	/**
	 * Specify the resolution strategy for expressions in bean definition values.
	 * 
	 * <p>为bean定义值中的表达式指定解析策略。
	 * 
	 * <p>There is no expression support active in a BeanFactory by default.
	 * An ApplicationContext will typically set a standard expression strategy
	 * here, supporting "#{...}" expressions in a Unified EL compatible style.
	 * 
	 * <p>默认情况下，BeanFactory中没有活动表达式支持。 ApplicationContext通常会在此处设置标准表达式策略，
	 * 支持Unified EL兼容样式中的“＃{...}”表达式。
	 * 
	 * @since 3.0
	 */
	void setBeanExpressionResolver(BeanExpressionResolver resolver);

	/**
	 * Return the resolution strategy for expressions in bean definition values.
	 * 
	 * <p>返回bean定义值中表达式的解析策略。
	 * 
	 * @since 3.0
	 */
	BeanExpressionResolver getBeanExpressionResolver();

	/**
	 * Specify a Spring 3.0 ConversionService to use for converting
	 * property values, as an alternative to JavaBeans PropertyEditors.
	 * 
	 * <p>指定用于转换属性值的Spring 3.0 ConversionService，作为JavaBeans PropertyEditors的替代方法。
	 * 
	 * @since 3.0
	 */
	void setConversionService(ConversionService conversionService);

	/**
	 * Return the associated ConversionService, if any.
	 * 
	 * <p>返回关联的ConversionService（如果有）。
	 * 
	 * @since 3.0
	 */
	ConversionService getConversionService();

	/**
	 * Add a PropertyEditorRegistrar to be applied to all bean creation processes.
	 * 
	 * <p>添加PropertyEditorRegistrar以应用于所有bean创建过程。
	 * 
	 * <p>Such a registrar creates new PropertyEditor instances and registers them
	 * on the given registry, fresh for each bean creation attempt. This avoids
	 * the need for synchronization on custom editors; hence, it is generally
	 * preferable to use this method instead of {@link #registerCustomEditor}.
	 * 
	 * <p>这样的注册器创建新的PropertyEditor实例并在给定的注册表上注册它们，对于每个bean创建尝试都是新的。 
	 * 这避免了在自定义编辑器上进行同步的需要; 因此，通常最好使用此方法而不是registerCustomEditor。
	 * 
	 * @param registrar the PropertyEditorRegistrar to register - PropertyEditorRegistrar进行注册
	 */
	void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);

	/**
	 * Register the given custom property editor for all properties of the
	 * given type. To be invoked during factory configuration.
	 * 
	 * <p>为给定类型的所有属性注册给定的自定义属性编辑器。 在工厂配置期间调用。
	 * 
	 * <p>Note that this method will register a shared custom editor instance;
	 * access to that instance will be synchronized for thread-safety. It is
	 * generally preferable to use {@link #addPropertyEditorRegistrar} instead
	 * of this method, to avoid for the need for synchronization on custom editors.
	 * 
	 * <p>请注意，此方法将注册共享自定义编辑器实例; 为了线程安全，将同步对该实例的访问。 
	 * 通常最好使用addPropertyEditorRegistrar而不是这种方法，以避免需要
	 * 
	 * @param requiredType type of the property - 属性的类型
	 * @param propertyEditorClass the {@link PropertyEditor} class to register - 要注册的PropertyEditor类
	 */
	void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);

	/**
	 * Initialize the given PropertyEditorRegistry with the custom editors
	 * that have been registered with this BeanFactory.
	 * 
	 * <p>使用已在此BeanFactory中注册的自定义编辑器初始化给定的PropertyEditorRegistry。
	 * 
	 * @param registry the PropertyEditorRegistry to initialize - 要初始化的PropertyEditorRegistry
	 */
	void copyRegisteredEditorsTo(PropertyEditorRegistry registry);

	/**
	 * Set a custom type converter that this BeanFactory should use for converting
	 * bean property values, constructor argument values, etc.
	 * 
	 * <p>设置此BeanFactory应用于转换bean属性值，构造函数参数值等的自定义类型转换器。
	 * 
	 * <p>This will override the default PropertyEditor mechanism and hence make
	 * any custom editors or custom editor registrars irrelevant.
	 * 
	 * <p>这将覆盖默认的PropertyEditor机制，因此使任何自定义编辑器或自定义编辑器注册表都无关紧要。
	 * 
	 * @see #addPropertyEditorRegistrar
	 * @see #registerCustomEditor
	 * @since 2.5
	 */
	void setTypeConverter(TypeConverter typeConverter);

	/**
	 * Obtain a type converter as used by this BeanFactory. This may be a fresh
	 * instance for each call, since TypeConverters are usually <i>not</i> thread-safe.
	 * 
	 * <p>获取此BeanFactory使用的类型转换器。 这可能是每次调用的新实例，因为TypeConverters通常不是线程安全的。
	 * 
	 * <p>If the default PropertyEditor mechanism is active, the returned
	 * TypeConverter will be aware of all custom editors that have been registered.
	 * 
	 * <p>如果默认的PropertyEditor机制处于活动状态，则返回的TypeConverter将知道已注册的所有自定义编辑器。
	 * 
	 * @since 2.5
	 */
	TypeConverter getTypeConverter();

	/**
	 * Add a String resolver for embedded values such as annotation attributes.
	 * 
	 * <p>为嵌入值（例如注释属性）添加String解析器。
	 * 
	 * @param valueResolver the String resolver to apply to embedded values - 要应用于嵌入值的String解析器
	 * @since 3.0
	 */
	void addEmbeddedValueResolver(StringValueResolver valueResolver);

	/**
	 * Resolve the given embedded value, e.g. an annotation attribute.
	 * 
	 * <p>解析给定的嵌入值，例如 注释属性。
	 * 
	 * @param value the value to resolve 要解决的值
	 * @return the resolved value (may be the original value as-is) -已解决的值（可能是原始值）
	 * @since 3.0
	 */
	String resolveEmbeddedValue(String value);

	/**
	 * Add a new BeanPostProcessor that will get applied to beans created
	 * by this factory. To be invoked during factory configuration.
	 * 
	 * <p>添加一个新的BeanPostProcessor，它将应用于此工厂创建的bean。 在工厂配置期间调用。
	 * 
	 * <p>Note: Post-processors submitted here will be applied in the order of
	 * registration; any ordering semantics expressed through implementing the
	 * {@link org.springframework.core.Ordered} interface will be ignored. Note
	 * that autodetected post-processors (e.g. as beans in an ApplicationContext)
	 * will always be applied after programmatically registered ones.
	 * 
	 * <p>注意：此处提交的后处理器将按注册顺序应用; 通过实现org.springframework.core.Ordered接口表
	 * 达的任何排序语义都将被忽略。 请注意，自动检测的后处理器（例如，作为ApplicationContext中的bean）将始终在以编程方式注册后应用。
	 * 
	 * @param beanPostProcessor the post-processor to register - 后处理器注册
	 */
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * Return the current number of registered BeanPostProcessors, if any.
	 * 
	 * <p>返回已注册的BeanPostProcessors的当前数量（如果有）。
	 * 
	 */
	int getBeanPostProcessorCount();

	/**
	 * Register the given scope, backed by the given Scope implementation.
	 * 
	 * <p>注册给定范围，由给定的Scope实现支持。
	 * 
	 * @param scopeName the scope identifier - 范围标识符
	 * @param scope the backing Scope implementation - 支持范围的实现
	 */
	void registerScope(String scopeName, Scope scope);

	/**
	 * Return the names of all currently registered scopes.
	 * 
	 * <p>返回所有当前注册的范围的名称。
	 * 
	 * <p>This will only return the names of explicitly registered scopes.
	 * Built-in scopes such as "singleton" and "prototype" won't be exposed.
	 * 
	 * <p>这只会返回显式注册范围的名称。 “singleton”和“prototype”等内置示波器不会暴露。
	 * 
	 * @return the array of scope names, or an empty array if none - 范围名称数组，如果没有，则为空数组
	 * @see #registerScope
	 */
	String[] getRegisteredScopeNames();

	/**
	 * Return the Scope implementation for the given scope name, if any.
	 * 
	 * <p> 返回给定范围名称的Scope实现（如果有）。
	 * 
	 * <p>This will only return explicitly registered scopes.
	 * Built-in scopes such as "singleton" and "prototype" won't be exposed.
	 * 
	 * <p> 这只会返回明确注册的范围。 “singleton”和“prototype”等内置示波器不会暴露。
	 * @param scopeName the name of the scope - 范围的名称
	 * @return the registered Scope implementation, or {@code null} if none - 已注册的Scope实现，如果没有则为null
	 * @see #registerScope
	 */
	Scope getRegisteredScope(String scopeName);

	/**
	 * Provides a security access control context relevant to this factory.
	 * 
	 * <p>提供与此工厂相关的安全访问控制上下文。
	 * 
	 * @return the applicable AccessControlContext (never {@code null})
	 * 
	 * <p> 适用的AccessControlContext（永不为null）
	 * @since 3.0
	 */
	AccessControlContext getAccessControlContext();

	/**
	 * Copy all relevant configuration from the given other factory.
	 * 
	 * <p> 复制给定其他工厂的所有相关配置。
	 * <p>Should include all standard configuration settings as well as
	 * BeanPostProcessors, Scopes, and factory-specific internal settings.
	 * Should not include any metadata of actual bean definitions,
	 * such as BeanDefinition objects and bean name aliases.
	 * 
	 * <p>应包括所有标准配置设置以及BeanPostProcessors，Scopes和工厂特定的内部设置。 不应包含实际bean定义的任何元数据，
	 * 例如BeanDefinition对象和bean名称别名。
	 * 
	 * @param otherFactory the other BeanFactory to copy from - 要从中复制的另一个BeanFactory
	 */
	void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

	/**
	 * Given a bean name, create an alias. We typically use this method to
	 * support names that are illegal within XML ids (used for bean names).
	 * 
	 * <p>给定bean名称，创建别名。 我们通常使用此方法来支持XML ID中非法的名称（用于bean名称）。
	 * 
	 * <p>Typically invoked during factory configuration, but can also be
	 * used for runtime registration of aliases. Therefore, a factory
	 * implementation should synchronize alias access.
	 * 
	 * <p.通常在工厂配置期间调用，但也可用于别名的运行时注册。 因此，工厂实现应同步别名访问。
	 * 
	 * @param beanName the canonical name of the target bean - 目标bean的规范名称
	 * @param alias the alias to be registered for the bean - 要为bean注册的别名
	 * @throws BeanDefinitionStoreException if the alias is already in use - 如果别名已被使用
	 */
	void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;

	/**
	 * Resolve all alias target names and aliases registered in this
	 * factory, applying the given StringValueResolver to them.
	 * 
	 * <p>解析在此工厂中注册的所有别名目标名称和别名，将给定的StringValueResolver应用于它们。
	 * 
	 * <p>The value resolver may for example resolve placeholders
	 * in target bean names and even in alias names.
	 * 
	 * <p>例如，值解析器可以解析目标bean名称中的占位符，甚至可以解析别名中的占位符。
	 * 
	 * @param valueResolver the StringValueResolver to apply - 要应用的StringValueResolver
	 * @since 2.5
	 */
	void resolveAliases(StringValueResolver valueResolver);

	/**
	 * Return a merged BeanDefinition for the given bean name,
	 * merging a child bean definition with its parent if necessary.
	 * Considers bean definitions in ancestor factories as well.
	 * 
	 * <p>返回给定bean名称的合并BeanDefinition，如果需要，将子bean定义与其父bean合并。 也考虑祖先工厂中的bean定义。
	 * 
	 * @param beanName the name of the bean to retrieve the merged definition for - 要检索合并定义的bean的名称
	 * @return a (potentially merged) BeanDefinition for the given bean - 给定bean的（可能合并的）BeanDefinition
	 * @throws NoSuchBeanDefinitionException if there is no bean definition with the given name
	 * 
	 * <p>如果没有给定名称的bean定义
	 * 
	 * @since 2.5
	 */
	BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Determine whether the bean with the given name is a FactoryBean.
	 * 
	 * <p>确定具有给定名称的bean是否为FactoryBean。
	 * 
	 * @param name the name of the bean to check - 要检查的bean的名称
	 * @return whether the bean is a FactoryBean
	 * ({@code false} means the bean exists but is not a FactoryBean)
	 * 
	 * <p>bean是否为FactoryBean（false表示bean存在但不是FactoryBean）
	 * 
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * 
	 * <p>如果没有给定名称的bean
	 * 
	 * @since 2.5
	 */
	boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Explicitly control the current in-creation status of the specified bean.
	 * For container-internal use only.
	 * 
	 * <p>显式控制指定bean的当前创建状态。 仅限容器内部使用。
	 * 
	 * @param beanName the name of the bean bean的名称
	 * @param inCreation whether the bean is currently in creation - bean是否正在创建中
	 * @since 3.1
	 */
	void setCurrentlyInCreation(String beanName, boolean inCreation);

	/**
	 * Determine whether the specified bean is currently in creation.
	 * 
	 * <p>确定指定的bean当前是否正在创建。
	 * 
	 * @param beanName the name of the bean bean的名称
	 * @return whether the bean is currently in creation - bean是否正在创建中
	 * @since 2.5
	 */
	boolean isCurrentlyInCreation(String beanName);

	/**
	 * Register a dependent bean for the given bean,
	 * to be destroyed before the given bean is destroyed.
	 * 
	 * <p>为给定的bean注册一个依赖bean，在销毁给定bean之前销毁它。
	 * 
	 * @param beanName the name of the bean bean的名称
	 * @param dependentBeanName the name of the dependent bean - 依赖bean的名称
	 * @since 2.5
	 */
	void registerDependentBean(String beanName, String dependentBeanName);

	/**
	 * Return the names of all beans which depend on the specified bean, if any.
	 * 
	 * <p>返回依赖于指定bean的所有bean的名称（如果有）。
	 * 
	 * @param beanName the name of the bean bean的名称
	 * @return the array of dependent bean names, or an empty array if none
	 * 
	 * <p>依赖bean名称的数组，如果没有，则为空数组
	 * 
	 * @since 2.5
	 */
	String[] getDependentBeans(String beanName);

	/**
	 * Return the names of all beans that the specified bean depends on, if any.
	 * 
	 * <p>返回指定bean所依赖的所有bean的名称（如果有）。
	 * 
	 * @param beanName the name of the bean -bean的名称
	 * @return the array of names of beans which the bean depends on,
	 * or an empty array if none
	 * 
	 * <p>bean所依赖的bean的名称数组，如果没有，则为空数组
	 * @since 2.5
	 */
	String[] getDependenciesForBean(String beanName);

	/**
	 * Destroy the given bean instance (usually a prototype instance
	 * obtained from this factory) according to its bean definition.
	 * 
	 * <p>根据bean的定义，销毁给定的bean实例（通常是从这个工厂获得的原型实例）。
	 * 
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 * 
	 * <p>应该捕获并记录在销毁期间出现的任何异常，而不是传播给此方法的调用方。
	 * 
	 * @param beanName the name of the bean definition - bean定义的名称
	 * @param beanInstance the bean instance to destroy - 要销毁的bean实例
	 */
	void destroyBean(String beanName, Object beanInstance);

	/**
	 * Destroy the specified scoped bean in the current target scope, if any.
	 * 
	 * <p>销毁当前目标作用域中指定的作用域bean（如果有）。
	 * 
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 * 
	 * <p>应该捕获并记录在销毁期间出现的任何异常，而不是传播给此方法的调用方。
	 * @param beanName the name of the scoped bean - 范围bean的名称
	 */
	void destroyScopedBean(String beanName);

	/**
	 * Destroy all singleton beans in this factory, including inner beans that have
	 * been registered as disposable. To be called on shutdown of a factory.
	 * 
	 * <p>销毁此工厂中的所有单例bean，包括已注册为一次性的内部bean。 在关闭工厂时被叫。
	 * 
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 * 
	 * <p>应该捕获并记录在销毁期间出现的任何异常，而不是传播给此方法的调用方。
	 * 
	 */
	void destroySingletons();

}
