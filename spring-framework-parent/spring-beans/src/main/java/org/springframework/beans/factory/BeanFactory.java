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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * The root interface for accessing a Spring bean container.
 * This is the basic client view of a bean container;
 * further interfaces such as {@link ListableBeanFactory} and
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}
 * are available for specific purposes.
 * 
 * <p>用于访问Spring bean容器的根接口。这是bean容器的基本客户端视图;其他接
 * 口（如ListableBeanFactory和org.springframework.beans.factory.config.ConfigurableBeanFactory）可用于特定目的。
 *
 * <p>This interface is implemented by objects that hold a number of bean definitions,
 * each uniquely identified by a String name. Depending on the bean definition,
 * the factory will return either an independent instance of a contained object
 * (the Prototype design pattern), or a single shared instance (a superior
 * alternative to the Singleton design pattern, in which the instance is a
 * singleton in the scope of the factory). Which type of instance will be returned
 * depends on the bean factory configuration: the API is the same. Since Spring
 * 2.0, further scopes are available depending on the concrete application
 * context (e.g. "request" and "session" scopes in a web environment).
 * 
 * <p>此接口由包含多个bean定义的对象实现，每个bean定义由String名称唯一标识。根据bean定义，工厂将返回包含对
 * 象的独立实例（Prototype设计模式）或单个共享实例（Singleton设计模式的替代实例，其中实例是范围中的单例工厂）。将
 * 返回哪种类型的实例取决于bean工厂配置：API是相同的。从Spring 2.0开始，根据具体的应用程序上下文（例如，Web环境
 * 中的“请求”和“会话”范围），可以使用更多范围。
 * 
 *
 * <p>The point of this approach is that the BeanFactory is a central registry
 * of application components, and centralizes configuration of application
 * components (no more do individual objects need to read properties files,
 * for example). See chapters 4 and 11 of "Expert One-on-One J2EE Design and
 * Development" for a discussion of the benefits of this approach.
 * 
 * <p>这种方法的重点是BeanFactory是应用程序组件的中央注册表，并集中应用程序组件的配置（例如，不再需
 * 要单个对象读取属性文件）。有关此方法的优点的讨论，请参阅“Expert One-on-One J2EE设计和开发”的第4章和第11章。
 *
 * <p>Note that it is generally better to rely on Dependency Injection
 * ("push" configuration) to configure application objects through setters
 * or constructors, rather than use any form of "pull" configuration like a
 * BeanFactory lookup. Spring's Dependency Injection functionality is
 * implemented using this BeanFactory interface and its subinterfaces.
 * 
 * <p>请注意，通常最好依靠依赖注入（“推送”配置）通过setter或构造函数来配置应用程序对象，而不是
 * 像BeanFactory查找一样使用任何形式的“拉”配置。 Spring的依赖注入功能是使用这个BeanFactory接口及其子接口实现的。
 *
 * <p>Normally a BeanFactory will load bean definitions stored in a configuration
 * source (such as an XML document), and use the {@code org.springframework.beans}
 * package to configure the beans. However, an implementation could simply return
 * Java objects it creates as necessary directly in Java code. There are no
 * constraints on how the definitions could be stored: LDAP, RDBMS, XML,
 * properties file, etc. Implementations are encouraged to support references
 * amongst beans (Dependency Injection).
 * 
 * <p>通常，BeanFactory将加载存储在配置源（例如XML文档）中的bean定义，并使用org.springframework.beans包来配
 * 置bean。但是，实现可以直接在Java代码中直接返回它创建的Java对象。对如何存储定义没有限制：LDAP，RDBMS，XML，属性文
 * 件等。鼓励实现支持bean之间的引用（依赖注入）。
 *
 * <p>In contrast to the methods in {@link ListableBeanFactory}, all of the
 * operations in this interface will also check parent factories if this is a
 * {@link HierarchicalBeanFactory}. If a bean is not found in this factory instance,
 * the immediate parent factory will be asked. Beans in this factory instance
 * are supposed to override beans of the same name in any parent factory.
 * 
 * <p>与ListableBeanFactory中的方法相反，如果这是HierarchicalBeanFactory，则此接口中的所有操作也将检查
 * 父工厂。如果在此工厂实例中找不到bean，则会询问直接父工厂。此工厂实例中的Bean应该在任何父工厂中覆盖同名的Bean。
 *
 * <p>Bean factory implementations should support the standard bean lifecycle interfaces
 * as far as possible. The full set of initialization methods and their standard order is:<br>
 * 
 * <p>Bean工厂实现应尽可能支持标准bean生命周期接口。 完整的初始化方法及其标准顺序是：<br>
 * 1. BeanNameAware's {@code setBeanName}<br>
 * 2. BeanClassLoaderAware's {@code setBeanClassLoader}<br>
 * 3. BeanFactoryAware's {@code setBeanFactory}<br>
 * 4. ResourceLoaderAware's {@code setResourceLoader} 
 * (only applicable when running in an application context)仅适用于在应用程序上下文中运行时<br>
 * 5. ApplicationEventPublisherAware's {@code setApplicationEventPublisher} 
 * (only applicable when running in an application context)仅适用于在应用程序上下文中运行时<br>
 * 6. MessageSourceAware's {@code setMessageSource}
 * (only applicable when running in an application context)仅适用于在应用程序上下文中运行时<br>
 * 7. ApplicationContextAware's {@code setApplicationContext}
 * (only applicable when running in an application context)仅适用于在应用程序上下文中运行时<br>
 * 8. ServletContextAware's {@code setServletContext}
 * (only applicable when running in a web application context)仅适用于在应用程序上下文中运行时<br>
 * 9. {@code postProcessBeforeInitialization} methods of BeanPostProcessors<br>
 * 10. InitializingBean's {@code afterPropertiesSet}<br>
 * 11. a custom init-method definition<br>
 * 12. {@code postProcessAfterInitialization} methods of BeanPostProcessors 自定义init方法定义
 *
 * <p>On shutdown of a bean factory, the following lifecycle methods apply:<br>
 * <p>关闭bean工厂时，以下生命周期方法适用：<br>
 * 1. DisposableBean's {@code destroy}<br>
 * 2. a custom destroy-method definition - 自定义销毁方法定义
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 13 April 2001
 * @see BeanNameAware#setBeanName
 * @see BeanClassLoaderAware#setBeanClassLoader
 * @see BeanFactoryAware#setBeanFactory
 * @see org.springframework.context.ResourceLoaderAware#setResourceLoader
 * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher
 * @see org.springframework.context.MessageSourceAware#setMessageSource
 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
 * @see org.springframework.web.context.ServletContextAware#setServletContext
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization
 * @see InitializingBean#afterPropertiesSet
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization
 * @see DisposableBean#destroy
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName
 */
public interface BeanFactory {

	/**
	 * Used to dereference a {@link FactoryBean} instance and distinguish it from
	 * beans <i>created</i> by the FactoryBean. For example, if the bean named
	 * {@code myJndiObject} is a FactoryBean, getting {@code &myJndiObject}
	 * will return the factory, not the instance returned by the factory.
	 * 
	 * <p>用于取消引用FactoryBean实例，并将其与FactoryBean创建的bean区分开来。 
	 * 例如，如果名为myJndiObject的bean是FactoryBean，则获取＆myJndiObject将返回工厂，而不是工厂返回的实例。
	 */
	String FACTORY_BEAN_PREFIX = "&";

	/**
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * 
	 * <p>返回指定bean的实例，该实例可以是共享的或独立的。
	 * 
	 * <p>This method allows a Spring BeanFactory to be used as a replacement for the
	 * Singleton or Prototype design pattern. Callers may retain references to
	 * returned objects in the case of Singleton beans.
	 * 
	 * <p>此方法允许Spring BeanFactory用作Singleton或Prototype设计模式的替代。 在Singleton bean的情况下，调
	 * 用者可以保留对返回对象的引用。
	 * 
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * 
	 * <p>将别名转换回相应的规范bean名称。 将询问父工厂是否在此工厂实例中找不到bean。
	 * 
	 * @param name the name of the bean to retrieve - 要检索的bean的名称
	 * @return an instance of the bean - bean的一个实例
	 * @throws NoSuchBeanDefinitionException if there is no bean definition
	 * with the specified name - 如果没有具有指定名称的bean定义
	 * @throws BeansException if the bean could not be obtained - 如果无法获得bean
	 */
	Object getBean(String name) throws BeansException;

	/**
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * 
	 * <p>返回指定bean的实例，该实例可以是共享的或独立的。
	 * 
	 * <p>Behaves the same as {@link #getBean(String)}, but provides a measure of type
	 * safety by throwing a BeanNotOfRequiredTypeException if the bean is not of the
	 * required type. This means that ClassCastException can't be thrown on casting
	 * the result correctly, as can happen with {@link #getBean(String)}.
	 * 
	 * <p>行为与getBean（String）相同，但如果bean不是所需类型，则通过抛出BeanNotOfRequiredTypeException来提供类型安全
	 * 性度量。 这意味着在正确转换结果时不能抛出ClassCastException，就像getBean（String）一样。
	 * 
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * 
	 * <p>将别名转换回相应的规范bean名称。 将询问父工厂是否在此工厂实例中找不到bean。
	 * 
	 * @param name the name of the bean to retrieve - 要检索的bean的名称
	 * @param requiredType type the bean must match. Can be an interface or superclass
	 * of the actual class, or {@code null} for any match. For example, if the value
	 * is {@code Object.class}, this method will succeed whatever the class of the
	 * returned instance.
	 * 
	 * <p>键入bean必须匹配。 可以是实际类的接口或超类，也可以是任何匹配的null。 例如，如果值为Object.class，则此方法将成功返回实例的任何类。
	 * 
	 * @return an instance of the bean
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition - 如果没有这样的bean定义
	 * @throws BeanNotOfRequiredTypeException if the bean is not of the required type - 如果bean不是所需的类型
	 * @throws BeansException if the bean could not be created - 如果无法创建bean
	 */
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;

	/**
	 * Return the bean instance that uniquely matches the given object type, if any.
	 * 
	 * <p>返回唯一匹配给定对象类型的bean实例（如果有）。
	 * 
	 * @param requiredType type the bean must match; can be an interface or superclass.
	 * {@code null} is disallowed.
	 * 
	 * <p>键入bean必须匹配; 可以是接口或超类。 不允许使用null。
	 * 
	 * <p>This method goes into {@link ListableBeanFactory} by-type lookup territory
	 * but may also be translated into a conventional by-name lookup based on the name
	 * of the given type. For more extensive retrieval operations across sets of beans,
	 * use {@link ListableBeanFactory} and/or {@link BeanFactoryUtils}.
	 * 
	 * <p>此方法进入ListableBeanFactory按类型查找区域，但也可以根据给定类型的名称转换为常规的按名称查找。 对于
	 * 跨Bean集的更广泛的检索操作，请使用ListableBeanFactory和/或BeanFactoryUtils。
	 * 
	 * @return an instance of the single bean matching the required type - 匹配所需类型的单个bean的实例
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found - 如果没有找到给定类型的bean
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found - 如果找到多个给定类型的bean
	 * @since 3.0
	 * @see ListableBeanFactory
	 */
	<T> T getBean(Class<T> requiredType) throws BeansException;

	/**
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * 
	 * <p>返回指定bean的实例，该实例可以是共享的或独立的。
	 * 
	 * <p>Allows for specifying explicit constructor arguments / factory method arguments,
	 * overriding the specified default arguments (if any) in the bean definition.
	 * 
	 * <p>允许指定显式构造函数参数/工厂方法参数，覆盖bean定义中指定的缺省参数（如果有）。
	 * 
	 * @param name the name of the bean to retrieve - 要检索的bean名称
	 * @param args arguments to use if creating a prototype using explicit arguments - 使用显式参数创建原型时使用的参数
	 * @return an instance of the bean
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition - 如果没有这样的bean定义
	 * @throws BeanDefinitionStoreException if arguments have been given but 
	 * the affected bean isn't a prototype - 如果已经给出了参数但受影响的bean不是原型
	 * @throws BeansException if the bean could not be created - 如果无法创建bean
	 * @since 2.5
	 */
	Object getBean(String name, Object... args) throws BeansException;

	/**
	 * Does this bean factory contain a bean definition or externally registered singleton
	 * instance with the given name?
	 * 
	 * <p>此bean工厂是否包含具有给定名称的bean定义或外部注册的单例实例？
	 * 
	 * <p>If the given name is an alias, it will be translated back to the corresponding
	 * canonical bean name.
	 * 
	 * <p>如果给定的名称是别名，则它将被转换回相应的规范bean名称。
	 * 
	 * <p>If this factory is hierarchical, will ask any parent factory if the bean cannot
	 * be found in this factory instance.
	 * 
	 * <p>如果此工厂是分层的，则会询问任何父工厂是否在此工厂实例中找不到该bean。
	 * 
	 * <p>If a bean definition or singleton instance matching the given name is found,
	 * this method will return {@code true} whether the named bean definition is concrete
	 * or abstract, lazy or eager, in scope or not. Therefore, note that a {@code true}
	 * return value from this method does not necessarily indicate that {@link #getBean}
	 * will be able to obtain an instance for the same name.
	 * 
	 * <p>如果找到与给定名称匹配的bean定义或单例实例，则此方法将返回true，无论命名bean定义是具体的还是抽象的，惰
	 * 性的还是渴望的，是否在范围内。 因此，请注意，此方法的true返回值不一定表示getBean将能够获取同名的实例。
	 * 
	 * @param name the name of the bean to query - 要查询的bean的名称
	 * @return whether a bean with the given name is present - 是否存在具有给定名称的bean
	 */
	boolean containsBean(String name);

	/**
	 * Is this bean a shared singleton? That is, will {@link #getBean} always
	 * return the same instance?
	 * 
	 * <p>这个bean是共享单例吗？ 也就是说，getBean总会返回相同的实例吗？
	 * 
	 * <p>Note: This method returning {@code false} does not clearly indicate
	 * independent instances. It indicates non-singleton instances, which may correspond
	 * to a scoped bean as well. Use the {@link #isPrototype} operation to explicitly
	 * check for independent instances.
	 * 
	 * <p>注意：此方法返回false并不能清楚地指示独立实例。 它表示非单例实例，也可以对应于作用域bean。 使用isPrototype操作显
	 * 式检查独立实例。
	 * 
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * 
	 * <p>将别名转换回相应的规范bean名称。 将询问父工厂是否在此工厂实例中找不到bean。
	 * 
	 * @param name the name of the bean to query - 要查询bean的名称
	 * @return whether this bean corresponds to a singleton instance - 此bean是否对应于单例实例
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name - 如果没有给定名称的bean
	 * @see #getBean
	 * @see #isPrototype
	 */
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Is this bean a prototype? That is, will {@link #getBean} always return
	 * independent instances?
	 * 
	 * <p>这个bean是原型吗？ 也就是说，getBean总是会返回独立的实例吗？
	 * 
	 * <p>Note: This method returning {@code false} does not clearly indicate
	 * a singleton object. It indicates non-independent instances, which may correspond
	 * to a scoped bean as well. Use the {@link #isSingleton} operation to explicitly
	 * check for a shared singleton instance.
	 * 
	 * <p>注意：此方法返回false并不能清楚地指示单个对象。 它表示非独立实例，也可以对应于范围bean。 使
	 * 用isSingleton操作显式检查共享单例实例。
	 * 
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * 
	 * <p>将别名转换回相应的规范bean名称。 将询问父工厂是否在此工厂实例中找不到bean。
	 * 
	 * @param name the name of the bean to query - 要查询bean的名称
	 * @return whether this bean will always deliver independent instances - 这个bean是否总是提供独立的实例
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name - 如果没有给定名称的bean
	 * @since 2.0.3
	 * @see #getBean
	 * @see #isSingleton
	 */
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Check whether the bean with the given name matches the specified type.
	 * More specifically, check whether a {@link #getBean} call for the given name
	 * would return an object that is assignable to the specified target type.
	 * 
	 * <p>检查具有给定名称的bean是否与指定的类型匹配。 更具体地说，检查给定名称的getBean调用是否将返回可分配给指定目标类型的对象。
	 * 
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * 
	 * <p>将别名转换回相应的规范bean名称。 将询问父工厂是否在此工厂实例中找不到bean。
	 * @param name the name of the bean to query - 要查询bean的名称
	 * @param targetType the type to match against - 要匹配的类型
	 * @return {@code true} if the bean type matches,
	 * {@code false} if it doesn't match or cannot be determined yet
	 * <p>如果bean类型匹配，则返回true;如果不匹配或无法确定，则返回false
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name - 如果没有给定名称的bean
	 * @since 2.0.1
	 * @see #getBean
	 * @see #getType
	 */
	boolean isTypeMatch(String name, Class<?> targetType) throws NoSuchBeanDefinitionException;

	/**
	 * Determine the type of the bean with the given name. More specifically,
	 * determine the type of object that {@link #getBean} would return for the given name.
	 * 
	 * <p>确定具有给定名称的bean的类型。 更具体地说，确定getBean将为给定名称返回的对象类型。
	 * 
	 * <p>For a {@link FactoryBean}, return the type of object that the FactoryBean creates,
	 * as exposed by {@link FactoryBean#getObjectType()}.
	 * 
	 * <p>对于FactoryBean，返回FactoryBean创建的对象类型，由FactoryBean.getObjectType（）公开。
	 * 
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * Will ask the parent factory if the bean cannot be found in this factory instance.
	 * 
	 * <p>将别名转换回相应的规范bean名称。 将询问父工厂是否在此工厂实例中找不到bean。
	 * @param name the name of the bean to query - 要查询bean的名称
	 * @return the type of the bean, or {@code null} if not determinable - bean的类型，如果不可确定则为null
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name - 如果没有给定名称的bean
	 * @since 1.1.2
	 * @see #getBean
	 * @see #isTypeMatch
	 */
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Return the aliases for the given bean name, if any.
	 * All of those aliases point to the same bean when used in a {@link #getBean} call.
	 * 
	 * <p>返回给定bean名称的别名（如果有）。 当在getBean调用中使用时，所有这些别名都指向同一个bean。
	 * 
	 * <p>If the given name is an alias, the corresponding original bean name
	 * and other aliases (if any) will be returned, with the original bean name
	 * being the first element in the array.
	 * 
	 * <p>如果给定名称是别名，则将返回相应的原始bean名称和其他别名（如果有），原始bean名称是数组中的第一个元素。
	 * 
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * 
	 * <p>将询问父工厂是否在此工厂实例中找不到bean。
	 * 
	 * @param name the bean name to check for aliases - 用于检查别名的bean名称
	 * @return the aliases, or an empty array if none - 别名，如果没有，则为空数组
	 * @see #getBean
	 */
	String[] getAliases(String name);

}
