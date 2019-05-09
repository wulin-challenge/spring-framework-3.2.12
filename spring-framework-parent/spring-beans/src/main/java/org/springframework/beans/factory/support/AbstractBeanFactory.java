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

package org.springframework.beans.factory.support;

import java.beans.PropertyEditor;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.DecoratingClassLoader;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * Abstract base class for {@link org.springframework.beans.factory.BeanFactory}
 * implementations, providing the full capabilities of the
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory} SPI.
 * Does <i>not</i> assume a listable bean factory: can therefore also be used
 * as base class for bean factory implementations which obtain bean definitions
 * from some backend resource (where bean definition access is an expensive operation).
 * 
 * <p>org.springframework.beans.factory.BeanFactory实现的抽象基类，
 * 提供org.springframework.beans.factory.config.ConfigurableBeanFactory SPI的全部功能。
 * 不假设可列出的bean工厂：因此也可以用作bean工厂实现的基类，它从某些后端资源获取bean定义（其中bean定义访问是一项昂贵的操作）。
 *
 * <p>This class provides a singleton cache (through its base class
 * {@link org.springframework.beans.factory.support.DefaultSingletonBeanRegistry},
 * singleton/prototype determination, {@link org.springframework.beans.factory.FactoryBean}
 * handling, aliases, bean definition merging for child bean definitions,
 * and bean destruction ({@link org.springframework.beans.factory.DisposableBean}
 * interface, custom destroy methods). Furthermore, it can manage a bean factory
 * hierarchy (delegating to the parent in case of an unknown bean), through implementing
 * the {@link org.springframework.beans.factory.HierarchicalBeanFactory} interface.
 * 
 * <p>该类提供单例缓存（通过其基类org.springframework.beans.factory.support.DefaultSingletonBeanRegistry，
 * 单例/原型确定，org.springframework.beans.factory.FactoryBean处理，别名，用于子bean定义的bean定义合并，
 * 以及bean destroy（org.springframework.beans.factory.DisposableBean接口，自定义销毁方法）。
 * 此外，它可以通过实现org.springframework.beans来管理bean工厂层次结构（在未知bean的情况下委托给父级）。 
 * factory.HierarchicalBeanFactory接口。
 *
 * <p>The main template methods to be implemented by subclasses are
 * {@link #getBeanDefinition} and {@link #createBean}, retrieving a bean definition
 * for a given bean name and creating a bean instance for a given bean definition,
 * respectively. Default implementations of those operations can be found in
 * {@link DefaultListableBeanFactory} and {@link AbstractAutowireCapableBeanFactory}.
 * 
 * <p>子类实现的主要模板方法是getBeanDefinition和createBean，分别检索给定bean名称的bean定义并为给定的bean定义创建bean实例。
 * 可以在DefaultListableBeanFactory和AbstractAutowireCapableBeanFactory中找到这些操作的默认实现。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Costin Leau
 * @author Chris Beams
 * @since 15 April 2001
 * @see #getBeanDefinition
 * @see #createBean
 * @see AbstractAutowireCapableBeanFactory#createBean
 * @see DefaultListableBeanFactory#getBeanDefinition
 */
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {

	/** Parent bean factory, for bean inheritance support */
	/** 父bean工厂，用于bean继承支持 */
	private BeanFactory parentBeanFactory;

	/** ClassLoader to resolve bean class names with, if necessary */
	/** 如有必要，ClassLoader用于解析bean类名 */
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/** ClassLoader to temporarily resolve bean class names with, if necessary */
	/** 如有必要，ClassLoader暂时解析bean类名 */
	private ClassLoader tempClassLoader;

	/** Whether to cache bean metadata or rather reobtain it for every access */
	/** 是否缓存bean元数据，或者为每次访问重新获取它 */
	private boolean cacheBeanMetadata = true;

	/** Resolution strategy for expressions in bean definition values */
	/** bean定义值中表达式的解析策略 */
	private BeanExpressionResolver beanExpressionResolver;

	/** Spring ConversionService to use instead of PropertyEditors */
	/** 使用Spring ConversionService而不是PropertyEditors */
	private ConversionService conversionService;

	/** Custom PropertyEditorRegistrars to apply to the beans of this factory */
	/** 自定义PropertyEditorRegistrar适用于此工厂的bean */
	private final Set<PropertyEditorRegistrar> propertyEditorRegistrars =
			new LinkedHashSet<PropertyEditorRegistrar>(4);

	/** A custom TypeConverter to use, overriding the default PropertyEditor mechanism */
	/** 要使用的自定义TypeConverter，重写默认的PropertyEditor机制 */
	private TypeConverter typeConverter;

	/** Custom PropertyEditors to apply to the beans of this factory */
	/** 自定义PropertyEditors应用于此工厂的bean */
	private final Map<Class<?>, Class<? extends PropertyEditor>> customEditors =
			new HashMap<Class<?>, Class<? extends PropertyEditor>>(4);

	/** String resolvers to apply e.g. to annotation attribute values */
	/** 要解决的字符串解析器，例如 注入属性值 */
	private final List<StringValueResolver> embeddedValueResolvers = new LinkedList<StringValueResolver>();

	/** BeanPostProcessors to apply in createBean */
	/** 要在createBean中应用的BeanPostProcessors */
	private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<BeanPostProcessor>();

	/** Indicates whether any InstantiationAwareBeanPostProcessors have been registered */
	/** 指示是否已注册任何InstantiationAwareBeanPostProcessors */
	private boolean hasInstantiationAwareBeanPostProcessors;

	/** Indicates whether any DestructionAwareBeanPostProcessors have been registered */
	/** 指示是否已注册任何DestructionAwareBeanPostProcessors */
	private boolean hasDestructionAwareBeanPostProcessors;

	/** Map from scope identifier String to corresponding Scope */
	/** 从范围标识符String映射到相应的Scope */
	private final Map<String, Scope> scopes = new HashMap<String, Scope>(8);

	/** Security context used when running with a SecurityManager */
	/** 使用SecurityManager运行时使用的安全上下文 */
	private SecurityContextProvider securityContextProvider;

	/** Map from bean name to merged RootBeanDefinition */
	/** 从bean名称映射到合并的RootBeanDefinition */
	private final Map<String, RootBeanDefinition> mergedBeanDefinitions =
			new ConcurrentHashMap<String, RootBeanDefinition>(64);

	/** Names of beans that have already been created at least once */
	/** 已经创建至少一次的bean的名称 */
	private final Map<String, Boolean> alreadyCreated = new ConcurrentHashMap<String, Boolean>(64);

	/** Names of beans that are currently in creation */
	/** 当前正在创建的bean的名称 */
	private final ThreadLocal<Object> prototypesCurrentlyInCreation =
			new NamedThreadLocal<Object>("Prototype beans currently in creation");


	/**
	 * Create a new AbstractBeanFactory.
	 * 创建一个新的AbstractBeanFactory。
	 */
	public AbstractBeanFactory() {
	}

	/**
	 * Create a new AbstractBeanFactory with the given parent.
	 * <p>使用给定父级创建新的AbstractBeanFactory。
	 * @param parentBeanFactory parent bean factory, or {@code null} if none 父亲bean工厂,若没有则返回null
	 * @see #getBean
	 */
	public AbstractBeanFactory(BeanFactory parentBeanFactory) {
		this.parentBeanFactory = parentBeanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory interface
	// BeanFactory接口的实现
	//---------------------------------------------------------------------

	public Object getBean(String name) throws BeansException {
		return doGetBean(name, null, null, false);
	}

	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return doGetBean(name, requiredType, null, false);
	}

	public Object getBean(String name, Object... args) throws BeansException {
		return doGetBean(name, null, args, false);
	}

	/**
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>返回指定bean的实例，该实例可以是共享的或独立的。
	 * @param name the name of the bean to retrieve 要检索的bean的名称
	 * @param requiredType the required type of the bean to retrieve 要检索的bean的必需类型
	 * @param args arguments to use if creating a prototype using explicit arguments to a
	 * static factory method. It is invalid to use a non-null args value in any other case.
	 * <p>在使用静态工厂方法的显式参数创建原型时使用的参数。 在任何其他情况下使用非null args值无效。返回：
	 * @return an instance of the bean bean的一个实例
	 * @throws BeansException if the bean could not be created 如果无法创建bean
	 */
	public <T> T getBean(String name, Class<T> requiredType, Object... args) throws BeansException {
		return doGetBean(name, requiredType, args, false);
	}

	/**
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>返回指定bean的实例，该实例可以是共享的或独立的。
	 * @param name the name of the bean to retrieve 要检索的bean的名称
	 * @param requiredType the required type of the bean to retrieve  要检索的bean的必需类型
	 * @param args arguments to use if creating a prototype using explicit arguments to a
	 * static factory method. It is invalid to use a non-null args value in any other case.
	 * <p>在使用静态工厂方法的显式参数创建原型时使用的参数。 在任何其他情况下使用非null args值无效。
	 * @param typeCheckOnly whether the instance is obtained for a type check,
	 * not for actual use
	 * <p>是否为类型检查获取实例，而不是实际使用
	 * @return an instance of the bean bean的实例
	 * @throws BeansException if the bean could not be created 如果无法创建bean
	 */
	@SuppressWarnings("unchecked")
	protected <T> T doGetBean(
			final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly)
			throws BeansException {

		//提取对应的beanName
		final String beanName = transformedBeanName(name);
		Object bean;

		// Eagerly check singleton cache for manually registered singletons.
		// 急切地检查单例缓存以手动注册单例。
		
		/**
		 * 检查缓存中或实例工厂中是否有对应的实例
		 * 为什么首先会使用这段代码呢,
		 * 因为在创建单例bean的时候会存在依赖注入的情况,而在创建依赖的时候为了避免循环依赖,
		 * spring创建bean的原则是不等bean创建完成就会创建bean的ObjectFactory提早曝光,
		 * 也就是将ObjectFactory加入到缓存中,一旦下个bean创建的时候需要依赖上个bean则直接使用ObjectFactory,
		 */
		//直接尝试从缓存获取或者singleFactories中的ObjectFactory中获取
		Object sharedInstance = getSingleton(beanName);
		if (sharedInstance != null && args == null) {
			if (logger.isDebugEnabled()) {
				if (isSingletonCurrentlyInCreation(beanName)) {
					logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
							"' that is not fully initialized yet - a consequence of a circular reference");
				}
				else {
					logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
				}
			}
			//返回对应的实例,有时候在诸如BeanFactory的情况并不是直接返回实例本身而是返回指定方法返回的实例
			bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
		}

		else {
			// Fail if we're already creating this bean instance:
			// We're assumably within a circular reference.
			// 如果我们已经创建了这个bean实例，则会失败：我们可能会在循环引用中。
			
			/**
			 * 只有在单例情况才会尝试解决循环依赖,原型模式情况下,如果存在A中有B的属性,B中有A的属性,
			 * 那么当依赖注入的时候,就会产生当A还未创建完的时候因为对于B的创建再次返回创建A,造成循环依赖,也就是下面的情况
			 * isPrototypeCurrentlyInCreation(beanName) 为true
			 */
			if (isPrototypeCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName);
			}

			// Check if bean definition exists in this factory.
			// 检查此工厂中是否存在bean定义。
			BeanFactory parentBeanFactory = getParentBeanFactory();
			
			//如果 beanDefinitionMap中也就是在所有已经加载的类中不包含 beanName 则尝试从 parentBeanFactory中检测
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// Not found -> check parent.
				// 找不到 - >检查父级。
				String nameToLookup = originalBeanName(name);
				//递归到BeanFactory
				if (args != null) {
					// Delegation to parent with explicit args.
					// 使用显式args委托父级。
					return (T) parentBeanFactory.getBean(nameToLookup, args);
				}
				else {
					// No args -> delegate to standard getBean method.
					// 没有args - >委托给标准的getBean方法。
					return parentBeanFactory.getBean(nameToLookup, requiredType);
				}
			}

			//如果不是仅仅做类型检查则是创建bean,这里要进行记录
			if (!typeCheckOnly) {
				markBeanAsCreated(beanName);
			}

			try {
				//将存储XML配置文件的GernericBeanDefinition转换为RootBeanDefinition,如果指定BeanName
				//是子Bean的话,同时会合并父亲的相关属性
				final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				checkMergedBeanDefinition(mbd, beanName, args);

				// Guarantee initialization of beans that the current bean depends on.
				// 保证当前bean依赖的bean的初始化。
				String[] dependsOn = mbd.getDependsOn();
				//若存在依赖则需要递归实例化依赖bean
				if (dependsOn != null) {
					for (String dependsOnBean : dependsOn) {
						getBean(dependsOnBean);
						//缓存依赖调用
						registerDependentBean(dependsOnBean, beanName);
					}
				}

				// Create bean instance.
				// 创建bean实例。
				//实例化依赖的bean后便可以实例化mbd本身了,singleton模式的创建
				if (mbd.isSingleton()) {
					sharedInstance = getSingleton(beanName, new ObjectFactory<Object>() {
						public Object getObject() throws BeansException {
							try {
								return createBean(beanName, mbd, args);
							}
							catch (BeansException ex) {
								// Explicitly remove instance from singleton cache: It might have been put there
								// eagerly by the creation process, to allow for circular reference resolution.
								// Also remove any beans that received a temporary reference to the bean.
								
								//从单例缓存中显式删除实例：它可能已经被创建过程急切地放在那里，以允许循环引用解析。还删除任何接收到bean的临时引用的bean。
								destroySingleton(beanName);
								throw ex;
							}
						}
					});
					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
				}

				else if (mbd.isPrototype()) {
					// It's a prototype -> create a new instance.
					// 这是一个原型 - >创建一个新实例。
					
					//prototype模式的创建(new)
					Object prototypeInstance = null;
					try {
						beforePrototypeCreation(beanName);
						prototypeInstance = createBean(beanName, mbd, args);
					}
					finally {
						afterPrototypeCreation(beanName);
					}
					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
				}

				else {
					//指定的scope上实例化
					String scopeName = mbd.getScope();
					final Scope scope = this.scopes.get(scopeName);
					if (scope == null) {
						throw new IllegalStateException("No Scope registered for scope '" + scopeName + "'");
					}
					try {
						Object scopedInstance = scope.get(beanName, new ObjectFactory<Object>() {
							public Object getObject() throws BeansException {
								beforePrototypeCreation(beanName);
								try {
									return createBean(beanName, mbd, args);
								}
								finally {
									afterPrototypeCreation(beanName);
								}
							}
						});
						bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
					}
					catch (IllegalStateException ex) {
						throw new BeanCreationException(beanName,
								"Scope '" + scopeName + "' is not active for the current thread; " +
								"consider defining a scoped proxy for this bean if you intend to refer to it from a singleton",
								ex);
					}
				}
			}
			catch (BeansException ex) {
				cleanupAfterBeanCreationFailure(beanName);
				throw ex;
			}
		}

		// Check if required type matches the type of the actual bean instance.
		// 检测需要的类型是否符合bean的实际类型
		if (requiredType != null && bean != null && !requiredType.isAssignableFrom(bean.getClass())) {
			try {
				return getTypeConverter().convertIfNecessary(bean, requiredType);
			}
			catch (TypeMismatchException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to convert bean '" + name + "' to required type [" +
							ClassUtils.getQualifiedName(requiredType) + "]", ex);
				}
				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
			}
		}
		return (T) bean;
	}

	public boolean containsBean(String name) {
		String beanName = transformedBeanName(name);
		if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
			return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(name));
		}
		// Not found -> check parent.
		// 找不到 - >检查父级。
		BeanFactory parentBeanFactory = getParentBeanFactory();
		return (parentBeanFactory != null && parentBeanFactory.containsBean(originalBeanName(name)));
	}

	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean) {
				return (BeanFactoryUtils.isFactoryDereference(name) || ((FactoryBean<?>) beanInstance).isSingleton());
			}
			else {
				return !BeanFactoryUtils.isFactoryDereference(name);
			}
		}
		else if (containsSingleton(beanName)) {
			return true;
		}

		else {
			// No singleton instance found -> check bean definition.
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// No bean definition found in this factory -> delegate to parent.
				return parentBeanFactory.isSingleton(originalBeanName(name));
			}

			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

			// In case of FactoryBean, return singleton status of created object if not a dereference.
			if (mbd.isSingleton()) {
				if (isFactoryBean(beanName, mbd)) {
					if (BeanFactoryUtils.isFactoryDereference(name)) {
						return true;
					}
					FactoryBean<?> factoryBean = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
					return factoryBean.isSingleton();
				}
				else {
					return !BeanFactoryUtils.isFactoryDereference(name);
				}
			}
			else {
				return false;
			}
		}
	}

	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
			// No bean definition found in this factory -> delegate to parent.
			return parentBeanFactory.isPrototype(originalBeanName(name));
		}

		RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
		if (mbd.isPrototype()) {
			// In case of FactoryBean, return singleton status of created object if not a dereference.
			return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName, mbd));
		}
		else {
			// Singleton or scoped - not a prototype.
			// However, FactoryBean may still produce a prototype object...
			if (BeanFactoryUtils.isFactoryDereference(name)) {
				return false;
			}
			if (isFactoryBean(beanName, mbd)) {
				final FactoryBean<?> factoryBean = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
				if (System.getSecurityManager() != null) {
					return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
						public Boolean run() {
							return ((factoryBean instanceof SmartFactoryBean && ((SmartFactoryBean<?>) factoryBean).isPrototype()) ||
									!factoryBean.isSingleton());
						}
					}, getAccessControlContext());
				}
				else {
					return ((factoryBean instanceof SmartFactoryBean && ((SmartFactoryBean<?>) factoryBean).isPrototype()) ||
							!factoryBean.isSingleton());
				}
			}
			else {
				return false;
			}
		}
	}

	public boolean isTypeMatch(String name, Class<?> targetType) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		Class<?> typeToMatch = (targetType != null ? targetType : Object.class);

		// Check manually registered singletons.
		// 检查手动注册的单身人士。
		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean) {
				if (!BeanFactoryUtils.isFactoryDereference(name)) {
					Class<?> type = getTypeForFactoryBean((FactoryBean<?>) beanInstance);
					return (type != null && ClassUtils.isAssignable(typeToMatch, type));
				}
				else {
					return ClassUtils.isAssignableValue(typeToMatch, beanInstance);
				}
			}
			else {
				return !BeanFactoryUtils.isFactoryDereference(name) &&
						ClassUtils.isAssignableValue(typeToMatch, beanInstance);
			}
		}
		else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
			// null instance registered
			// null实例已注册
			return false;
		}

		else {
			// No singleton instance found -> check bean definition.
			// 找不到单例实例 - >检查bean定义。
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// No bean definition found in this factory -> delegate to parent.
				// 在此工厂中找不到bean定义 - >委托给父项。
				return parentBeanFactory.isTypeMatch(originalBeanName(name), targetType);
			}

			// Retrieve corresponding bean definition.
			// 检索相应的bean定义。
			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

			Class<?>[] typesToMatch = (FactoryBean.class.equals(typeToMatch) ?
					new Class<?>[] {typeToMatch} : new Class<?>[] {FactoryBean.class, typeToMatch});

			// Check decorated bean definition, if any: We assume it'll be easier
			// to determine the decorated bean's type than the proxy's type.
			// 检查修饰的bean定义，如果有的话：我们假设确定装饰bean的类型比代理的类型更容易。
			BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
			if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
				RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
				Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd, typesToMatch);
				if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
					return typeToMatch.isAssignableFrom(targetClass);
				}
			}

			Class<?> beanType = predictBeanType(beanName, mbd, typesToMatch);
			if (beanType == null) {
				return false;
			}

			// Check bean class whether we're dealing with a FactoryBean.
			// 检查bean类是否正在处理FactoryBean。
			if (FactoryBean.class.isAssignableFrom(beanType)) {
				if (!BeanFactoryUtils.isFactoryDereference(name)) {
					// If it's a FactoryBean, we want to look at what it creates, not the factory class.
					// 如果它是FactoryBean，我们想要查看它创建的内容，而不是工厂类。
					beanType = getTypeForFactoryBean(beanName, mbd);
					if (beanType == null) {
						return false;
					}
				}
			}
			else if (BeanFactoryUtils.isFactoryDereference(name)) {
				// Special case: A SmartInstantiationAwareBeanPostProcessor returned a non-FactoryBean
				// type but we nevertheless are being asked to dereference a FactoryBean...
				// Let's check the original bean class and proceed with it if it is a FactoryBean.
				/**
				 * 特殊情况：SmartInstantiationAwareBeanPostProcessor返回了非FactoryBean类型，但我们仍被要求取消引用FactoryBean ...
				 * 让我们检查原始bean类，如果它是FactoryBean则继续它。
				 */
				beanType = predictBeanType(beanName, mbd, FactoryBean.class);
				if (beanType == null || !FactoryBean.class.isAssignableFrom(beanType)) {
					return false;
				}
			}

			return typeToMatch.isAssignableFrom(beanType);
		}
	}

	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		// Check manually registered singletons.
		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
				return getTypeForFactoryBean((FactoryBean<?>) beanInstance);
			}
			else {
				return beanInstance.getClass();
			}
		}
		else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
			// null instance registered
			return null;
		}

		else {
			// No singleton instance found -> check bean definition.
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// No bean definition found in this factory -> delegate to parent.
				return parentBeanFactory.getType(originalBeanName(name));
			}

			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

			// Check decorated bean definition, if any: We assume it'll be easier
			// to determine the decorated bean's type than the proxy's type.
			BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
			if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
				RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
				Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd);
				if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
					return targetClass;
				}
			}

			Class<?> beanClass = predictBeanType(beanName, mbd);

			// Check bean class whether we're dealing with a FactoryBean.
			if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass)) {
				if (!BeanFactoryUtils.isFactoryDereference(name)) {
					// If it's a FactoryBean, we want to look at what it creates, not at the factory class.
					return getTypeForFactoryBean(beanName, mbd);
				}
				else {
					return beanClass;
				}
			}
			else {
				return (!BeanFactoryUtils.isFactoryDereference(name) ? beanClass : null);
			}
		}
	}

	@Override
	public String[] getAliases(String name) {
		String beanName = transformedBeanName(name);
		List<String> aliases = new ArrayList<String>();
		boolean factoryPrefix = name.startsWith(FACTORY_BEAN_PREFIX);
		String fullBeanName = beanName;
		if (factoryPrefix) {
			fullBeanName = FACTORY_BEAN_PREFIX + beanName;
		}
		if (!fullBeanName.equals(name)) {
			aliases.add(fullBeanName);
		}
		String[] retrievedAliases = super.getAliases(beanName);
		for (String retrievedAlias : retrievedAliases) {
			String alias = (factoryPrefix ? FACTORY_BEAN_PREFIX : "") + retrievedAlias;
			if (!alias.equals(name)) {
				aliases.add(alias);
			}
		}
		if (!containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null) {
				aliases.addAll(Arrays.asList(parentBeanFactory.getAliases(fullBeanName)));
			}
		}
		return StringUtils.toStringArray(aliases);
	}


	//---------------------------------------------------------------------
	// Implementation of HierarchicalBeanFactory interface
	// HierarchicalBeanFactory接口的实现
	//---------------------------------------------------------------------

	public BeanFactory getParentBeanFactory() {
		return this.parentBeanFactory;
	}

	public boolean containsLocalBean(String name) {
		String beanName = transformedBeanName(name);
		return ((containsSingleton(beanName) || containsBeanDefinition(beanName)) &&
				(!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName)));
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableBeanFactory interface
	// ConfigurableBeanFactory接口的实现
	//---------------------------------------------------------------------

	public void setParentBeanFactory(BeanFactory parentBeanFactory) {
		if (this.parentBeanFactory != null && this.parentBeanFactory != parentBeanFactory) {
			throw new IllegalStateException("Already associated with parent BeanFactory: " + this.parentBeanFactory);
		}
		this.parentBeanFactory = parentBeanFactory;
	}

	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = (beanClassLoader != null ? beanClassLoader : ClassUtils.getDefaultClassLoader());
	}

	public ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	public void setTempClassLoader(ClassLoader tempClassLoader) {
		this.tempClassLoader = tempClassLoader;
	}

	public ClassLoader getTempClassLoader() {
		return this.tempClassLoader;
	}

	public void setCacheBeanMetadata(boolean cacheBeanMetadata) {
		this.cacheBeanMetadata = cacheBeanMetadata;
	}

	public boolean isCacheBeanMetadata() {
		return this.cacheBeanMetadata;
	}

	public void setBeanExpressionResolver(BeanExpressionResolver resolver) {
		this.beanExpressionResolver = resolver;
	}

	public BeanExpressionResolver getBeanExpressionResolver() {
		return this.beanExpressionResolver;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public ConversionService getConversionService() {
		return this.conversionService;
	}

	public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
		Assert.notNull(registrar, "PropertyEditorRegistrar must not be null");
		this.propertyEditorRegistrars.add(registrar);
	}

	/**
	 * Return the set of PropertyEditorRegistrars.
	 */
	public Set<PropertyEditorRegistrar> getPropertyEditorRegistrars() {
		return this.propertyEditorRegistrars;
	}

	public void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass) {
		Assert.notNull(requiredType, "Required type must not be null");
		Assert.isAssignable(PropertyEditor.class, propertyEditorClass);
		this.customEditors.put(requiredType, propertyEditorClass);
	}

	public void copyRegisteredEditorsTo(PropertyEditorRegistry registry) {
		registerCustomEditors(registry);
	}

	/**
	 * Return the map of custom editors, with Classes as keys and PropertyEditor classes as values.
	 */
	public Map<Class<?>, Class<? extends PropertyEditor>> getCustomEditors() {
		return this.customEditors;
	}

	public void setTypeConverter(TypeConverter typeConverter) {
		this.typeConverter = typeConverter;
	}

	/**
	 * Return the custom TypeConverter to use, if any.
	 * <p>返回要使用的自定义TypeConverter（如果有）。
	 * @return the custom TypeConverter, or {@code null} if none specified
	 * <p>自定义TypeConverter，如果没有指定，则返回null
	 */
	protected TypeConverter getCustomTypeConverter() {
		return this.typeConverter;
	}

	public TypeConverter getTypeConverter() {
		TypeConverter customConverter = getCustomTypeConverter();
		if (customConverter != null) {
			return customConverter;
		}
		else {
			// Build default TypeConverter, registering custom editors.
			SimpleTypeConverter typeConverter = new SimpleTypeConverter();
			typeConverter.setConversionService(getConversionService());
			registerCustomEditors(typeConverter);
			return typeConverter;
		}
	}

	public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
		Assert.notNull(valueResolver, "StringValueResolver must not be null");
		this.embeddedValueResolvers.add(valueResolver);
	}

	public String resolveEmbeddedValue(String value) {
		String result = value;
		for (StringValueResolver resolver : this.embeddedValueResolvers) {
			if (result == null) {
				return null;
			}
			result = resolver.resolveStringValue(result);
		}
		return result;
	}

	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");
		this.beanPostProcessors.remove(beanPostProcessor);
		this.beanPostProcessors.add(beanPostProcessor);
		if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
			this.hasInstantiationAwareBeanPostProcessors = true;
		}
		if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
			this.hasDestructionAwareBeanPostProcessors = true;
		}
	}

	public int getBeanPostProcessorCount() {
		return this.beanPostProcessors.size();
	}

	/**
	 * Return the list of BeanPostProcessors that will get applied
	 * to beans created with this factory.
	 * 
	 * <p> 返回将应用于使用此工厂创建的bean的BeanPostProcessors列表。
	 */
	public List<BeanPostProcessor> getBeanPostProcessors() {
		return this.beanPostProcessors;
	}

	/**
	 * Return whether this factory holds a InstantiationAwareBeanPostProcessor
	 * that will get applied to singleton beans on shutdown.
	 * 
	 * <p> 返回此工厂是否持有将在关闭时应用于单例bean的InstantiationAwareBeanPostProcessor。
	 * 
	 * @see #addBeanPostProcessor
	 * @see org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor
	 */
	protected boolean hasInstantiationAwareBeanPostProcessors() {
		return this.hasInstantiationAwareBeanPostProcessors;
	}

	/**
	 * Return whether this factory holds a DestructionAwareBeanPostProcessor
	 * that will get applied to singleton beans on shutdown.
	 * 
	 * <p> 返回此工厂是否持有将在关闭时应用于单例bean的DestructionAwareBeanPostProcessor。
	 * 
	 * @see #addBeanPostProcessor
	 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
	 */
	protected boolean hasDestructionAwareBeanPostProcessors() {
		return this.hasDestructionAwareBeanPostProcessors;
	}

	public void registerScope(String scopeName, Scope scope) {
		Assert.notNull(scopeName, "Scope identifier must not be null");
		Assert.notNull(scope, "Scope must not be null");
		if (SCOPE_SINGLETON.equals(scopeName) || SCOPE_PROTOTYPE.equals(scopeName)) {
			throw new IllegalArgumentException("Cannot replace existing scopes 'singleton' and 'prototype'");
		}
		this.scopes.put(scopeName, scope);
	}

	public String[] getRegisteredScopeNames() {
		return StringUtils.toStringArray(this.scopes.keySet());
	}

	public Scope getRegisteredScope(String scopeName) {
		Assert.notNull(scopeName, "Scope identifier must not be null");
		return this.scopes.get(scopeName);
	}

	/**
	 * Set the security context provider for this bean factory. If a security manager
	 * is set, interaction with the user code will be executed using the privileged
	 * of the provided security context.
	 * 
	 * <p> 为此Bean工厂设置安全上下文提供程序。 如果设置了安全管理器，则将使用提供的安全上下文的特权执行与用户代码的交互。
	 */
	public void setSecurityContextProvider(SecurityContextProvider securityProvider) {
		this.securityContextProvider = securityProvider;
	}

	/**
	 * Delegate the creation of the access control context to the
	 * {@link #setSecurityContextProvider SecurityContextProvider}.
	 * 
	 * <p> 将访问控制上下文的创建委派给SecurityContextProvider。
	 */
	@Override
	public AccessControlContext getAccessControlContext() {
		return (this.securityContextProvider != null ?
				this.securityContextProvider.getAccessControlContext() :
				AccessController.getContext());
	}

	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		Assert.notNull(otherFactory, "BeanFactory must not be null");
		setBeanClassLoader(otherFactory.getBeanClassLoader());
		setCacheBeanMetadata(otherFactory.isCacheBeanMetadata());
		setBeanExpressionResolver(otherFactory.getBeanExpressionResolver());
		if (otherFactory instanceof AbstractBeanFactory) {
			AbstractBeanFactory otherAbstractFactory = (AbstractBeanFactory) otherFactory;
			this.customEditors.putAll(otherAbstractFactory.customEditors);
			this.propertyEditorRegistrars.addAll(otherAbstractFactory.propertyEditorRegistrars);
			this.beanPostProcessors.addAll(otherAbstractFactory.beanPostProcessors);
			this.hasInstantiationAwareBeanPostProcessors = this.hasInstantiationAwareBeanPostProcessors ||
					otherAbstractFactory.hasInstantiationAwareBeanPostProcessors;
			this.hasDestructionAwareBeanPostProcessors = this.hasDestructionAwareBeanPostProcessors ||
					otherAbstractFactory.hasDestructionAwareBeanPostProcessors;
			this.scopes.putAll(otherAbstractFactory.scopes);
			this.securityContextProvider = otherAbstractFactory.securityContextProvider;
		}
		else {
			setTypeConverter(otherFactory.getTypeConverter());
		}
	}

	/**
	 * Return a 'merged' BeanDefinition for the given bean name,
	 * merging a child bean definition with its parent if necessary.
	 * 
	 * <p> 返回给定bean名称的'merged'BeanDefinition，如果需要，将子bean定义与其父bean合并。
	 * 
	 * <p>This {@code getMergedBeanDefinition} considers bean definition
	 * in ancestors as well.
	 * 
	 * <p> 此getMergedBeanDefinition也考虑祖先中的bean定义。
	 * 
	 * @param name the name of the bean to retrieve the merged definition for
	 * (may be an alias)
	 * 
	 * <p> 要检索合并定义的bean的名称（可能是别名）
	 * 
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * 
	 * <p> 给定bean的（可能合并的）RootBeanDefinition
	 * 
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name - 如果没有给定名称的bean
	 * @throws BeanDefinitionStoreException in case of an invalid bean definition - 如果bean定义无效
	 */
	public BeanDefinition getMergedBeanDefinition(String name) throws BeansException {
		String beanName = transformedBeanName(name);

		// Efficiently check whether bean definition exists in this factory.
		// 有效地检查此工厂中是否存在bean定义。
		if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
			return ((ConfigurableBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(beanName);
		}
		// Resolve merged bean definition locally.
		// 在本地解析合并的bean定义。
		return getMergedLocalBeanDefinition(beanName);
	}

	public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			return (beanInstance instanceof FactoryBean);
		}
		else if (containsSingleton(beanName)) {
			// null instance registered
			// null实例已注册
			return false;
		}

		// No singleton instance found -> check bean definition.
		// 找不到单例实例 - >检查bean定义。
		if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
			// No bean definition found in this factory -> delegate to parent.
			// 在此工厂中找不到bean定义 - >委托给父项。
			return ((ConfigurableBeanFactory) getParentBeanFactory()).isFactoryBean(name);
		}

		return isFactoryBean(beanName, getMergedLocalBeanDefinition(beanName));
	}

	@Override
	public boolean isActuallyInCreation(String beanName) {
		return isSingletonCurrentlyInCreation(beanName) || isPrototypeCurrentlyInCreation(beanName);
	}

	/**
	 * Return whether the specified prototype bean is currently in creation
	 * (within the current thread).
	 * 
	 * <p> 返回指定的原型bean当前是否正在创建（在当前线程内）。
	 * 
	 * @param beanName the name of the bean - bean的名称
	 */
	protected boolean isPrototypeCurrentlyInCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		return (curVal != null &&
				(curVal.equals(beanName) || (curVal instanceof Set && ((Set<?>) curVal).contains(beanName))));
	}

	/**
	 * Callback before prototype creation.
	 * 
	 * <p> 在原型创建之前回调。
	 * 
	 * <p>The default implementation register the prototype as currently in creation.
	 * 
	 * <p> 默认实现将原型注册为当前创建的。
	 * 
	 * @param beanName the name of the prototype about to be created
	 * 
	 * <p> 即将创建的原型的名称
	 * 
	 * @see #isPrototypeCurrentlyInCreation
	 */
	@SuppressWarnings("unchecked")
	protected void beforePrototypeCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		if (curVal == null) {
			this.prototypesCurrentlyInCreation.set(beanName);
		}
		else if (curVal instanceof String) {
			Set<String> beanNameSet = new HashSet<String>(2);
			beanNameSet.add((String) curVal);
			beanNameSet.add(beanName);
			this.prototypesCurrentlyInCreation.set(beanNameSet);
		}
		else {
			Set<String> beanNameSet = (Set<String>) curVal;
			beanNameSet.add(beanName);
		}
	}

	/**
	 * Callback after prototype creation.
	 * 
	 * <p> 原型创建后回调。
	 * 
	 * <p>The default implementation marks the prototype as not in creation anymore.
	 * 
	 * <p> 默认实现将原型标记为不再在创建中。
	 * 
	 * @param beanName the name of the prototype that has been created
	 * 
	 * <p> 已创建的原型的名称
	 * 
	 * @see #isPrototypeCurrentlyInCreation
	 */
	@SuppressWarnings("unchecked")
	protected void afterPrototypeCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		if (curVal instanceof String) {
			this.prototypesCurrentlyInCreation.remove();
		}
		else if (curVal instanceof Set) {
			Set<String> beanNameSet = (Set<String>) curVal;
			beanNameSet.remove(beanName);
			if (beanNameSet.isEmpty()) {
				this.prototypesCurrentlyInCreation.remove();
			}
		}
	}

	public void destroyBean(String beanName, Object beanInstance) {
		destroyBean(beanName, beanInstance, getMergedLocalBeanDefinition(beanName));
	}

	/**
	 * Destroy the given bean instance (usually a prototype instance
	 * obtained from this factory) according to the given bean definition.
	 * 
	 * <p> 根据给定的bean定义销毁给定的bean实例（通常是从此工厂获取的原型实例）。
	 * 
	 * @param beanName the name of the bean definition - bean定义的名称
	 * @param beanInstance the bean instance to destroy - 要破坏的bean实例
	 * @param mbd the merged bean definition - 合并的bean定义
	 */
	protected void destroyBean(String beanName, Object beanInstance, RootBeanDefinition mbd) {
		new DisposableBeanAdapter(beanInstance, beanName, mbd, getBeanPostProcessors(), getAccessControlContext()).destroy();
	}

	public void destroyScopedBean(String beanName) {
		RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
		if (mbd.isSingleton() || mbd.isPrototype()) {
			throw new IllegalArgumentException(
					"Bean name '" + beanName + "' does not correspond to an object in a mutable scope");
		}
		String scopeName = mbd.getScope();
		Scope scope = this.scopes.get(scopeName);
		if (scope == null) {
			throw new IllegalStateException("No Scope SPI registered for scope '" + scopeName + "'");
		}
		Object bean = scope.remove(beanName);
		if (bean != null) {
			destroyBean(beanName, bean, mbd);
		}
	}


	//---------------------------------------------------------------------
	// Implementation methods
	// 实现方法
	//---------------------------------------------------------------------

	/**
	 * Return the bean name, stripping out the factory dereference prefix if necessary,
	 * and resolving aliases to canonical names.
	 * 
	 * <p> 返回bean名称，必要时删除工厂dereference前缀，并将别名解析为规范名称。
	 * 
	 * @param name the user-specified name - 用户指定的名称
	 * @return the transformed bean name - 转换后的bean名称
	 */
	protected String transformedBeanName(String name) {
		return canonicalName(BeanFactoryUtils.transformedBeanName(name));
	}

	/**
	 * Determine the original bean name, resolving locally defined aliases to canonical names.
	 * 
	 * <p> 确定原始bean名称，将本地定义的别名解析为规范名称。
	 * 
	 * @param name the user-specified name - 用户指定的名称
	 * @return the original bean name - 原始的bean名称
	 */
	protected String originalBeanName(String name) {
		String beanName = transformedBeanName(name);
		if (name.startsWith(FACTORY_BEAN_PREFIX)) {
			beanName = FACTORY_BEAN_PREFIX + beanName;
		}
		return beanName;
	}

	/**
	 * Initialize the given BeanWrapper with the custom editors registered
	 * with this factory. To be called for BeanWrappers that will create
	 * and populate bean instances.
	 * 
	 * <p> 使用在此工厂注册的自定义编辑器初始化给定的BeanWrapper。 要为将创建和填充bean实例的BeanWrappers调用。
	 * 
	 * <p>The default implementation delegates to {@link #registerCustomEditors}.
	 * Can be overridden in subclasses.
	 * 
	 * <p> 默认实现委托给registerCustomEditors。 可以在子类中重写。
	 * 
	 * @param bw the BeanWrapper to initialize - BeanWrapper初始化
	 */
	protected void initBeanWrapper(BeanWrapper bw) {
		bw.setConversionService(getConversionService());
		registerCustomEditors(bw);
	}

	/**
	 * Initialize the given PropertyEditorRegistry with the custom editors
	 * that have been registered with this BeanFactory.
	 * 
	 * <p> 使用已在此BeanFactory中注册的自定义编辑器初始化给定的PropertyEditorRegistry。
	 * 
	 * <p>To be called for BeanWrappers that will create and populate bean
	 * instances, and for SimpleTypeConverter used for constructor argument
	 * and factory method type conversion.
	 * 
	 * <p> 要为将创建和填充bean实例的BeanWrappers以及用于构造函数参数和工厂方法类型转换的SimpleTypeConverter调用。
	 * 
	 * @param registry the PropertyEditorRegistry to initialize - 要初始化的PropertyEditorRegistry
	 */
	protected void registerCustomEditors(PropertyEditorRegistry registry) {
		PropertyEditorRegistrySupport registrySupport =
				(registry instanceof PropertyEditorRegistrySupport ? (PropertyEditorRegistrySupport) registry : null);
		if (registrySupport != null) {
			registrySupport.useConfigValueEditors();
		}
		if (!this.propertyEditorRegistrars.isEmpty()) {
			for (PropertyEditorRegistrar registrar : this.propertyEditorRegistrars) {
				try {
					registrar.registerCustomEditors(registry);
				}
				catch (BeanCreationException ex) {
					Throwable rootCause = ex.getMostSpecificCause();
					if (rootCause instanceof BeanCurrentlyInCreationException) {
						BeanCreationException bce = (BeanCreationException) rootCause;
						if (isCurrentlyInCreation(bce.getBeanName())) {
							if (logger.isDebugEnabled()) {
								logger.debug("PropertyEditorRegistrar [" + registrar.getClass().getName() +
										"] failed because it tried to obtain currently created bean '" +
										ex.getBeanName() + "': " + ex.getMessage());
							}
							onSuppressedException(ex);
							continue;
						}
					}
					throw ex;
				}
			}
		}
		if (!this.customEditors.isEmpty()) {
			for (Map.Entry<Class<?>, Class<? extends PropertyEditor>> entry : this.customEditors.entrySet()) {
				Class<?> requiredType = entry.getKey();
				Class<? extends PropertyEditor> editorClass = entry.getValue();
				registry.registerCustomEditor(requiredType, BeanUtils.instantiateClass(editorClass));
			}
		}
	}


	/**
	 * Return a merged RootBeanDefinition, traversing the parent bean definition
	 * if the specified bean corresponds to a child bean definition.
	 * 
	 * <p> 返回合并的RootBeanDefinition，如果指定的bean对应于子bean定义，则遍历父bean定义。
	 * 
	 * @param beanName the name of the bean to retrieve the merged definition for
	 * 
	 * <p> 要检索合并定义的bean的名称
	 * 
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * 
	 * <p> 给定bean的（可能合并的）RootBeanDefinition
	 * 
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * 
	 * <p> 如果没有给定名称的bean
	 * 
	 * @throws BeanDefinitionStoreException in case of an invalid bean definition
	 * 
	 * <p> 如果bean定义无效
	 * 
	 */
	protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
		// Quick check on the concurrent map first, with minimal locking.
		// 首先快速检查并发映射，锁定最小。
		RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
		if (mbd != null) {
			return mbd;
		}
		return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
	}

	/**
	 * Return a RootBeanDefinition for the given top-level bean, by merging with
	 * the parent if the given bean's definition is a child bean definition.
	 * 
	 * <p> 如果给定bean的定义是子bean定义，则通过与父项合并返回给定顶级bean的RootBeanDefinition。
	 * 
	 * @param beanName the name of the bean definition - bean定义的名称
	 * @param bd the original bean definition (Root/ChildBeanDefinition) - 原始bean定义（Root / ChildBeanDefinition）
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * 
	 * <p> 给定bean的（可能合并的）RootBeanDefinition
	 * 
	 * @throws BeanDefinitionStoreException in case of an invalid bean definition
	 * 
	 * <p> 如果bean定义无效
	 * 
	 */
	protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd)
			throws BeanDefinitionStoreException {

		return getMergedBeanDefinition(beanName, bd, null);
	}

	/**
	 * Return a RootBeanDefinition for the given bean, by merging with the
	 * parent if the given bean's definition is a child bean definition.
	 * 
	 * <p> 如果给定bean的定义是子bean定义，则通过与父项合并返回给定bean的RootBeanDefinition。
	 * 
	 * @param beanName the name of the bean definition - bean定义的名称
	 * @param bd the original bean definition (Root/ChildBeanDefinition)
	 * 
	 * <p> 原始bean定义（Root / ChildBeanDefinition）
	 * 
	 * @param containingBd the containing bean definition in case of inner bean,
	 * or {@code null} in case of a top-level bean
	 * 
	 * <p> 在内部bean的情况下包含bean定义，或者在顶级bean的情况下为null
	 * 
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * 
	 * <p> 给定bean的（可能合并的）RootBeanDefinition
	 * 
	 * @throws BeanDefinitionStoreException in case of an invalid bean definition
	 * 
	 * <p> 如果bean定义无效
	 * 
	 */
	protected RootBeanDefinition getMergedBeanDefinition(
			String beanName, BeanDefinition bd, BeanDefinition containingBd)
			throws BeanDefinitionStoreException {

		synchronized (this.mergedBeanDefinitions) {
			RootBeanDefinition mbd = null;

			// Check with full lock now in order to enforce the same merged instance.
			// 现在检查完全锁定以强制执行相同的合并实例。
			if (containingBd == null) {
				mbd = this.mergedBeanDefinitions.get(beanName);
			}

			if (mbd == null) {
				if (bd.getParentName() == null) {
					// Use copy of given root bean definition.
					// 使用给定根bean定义的副本。
					if (bd instanceof RootBeanDefinition) {
						mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
					}
					else {
						mbd = new RootBeanDefinition(bd);
					}
				}
				else {
					// Child bean definition: needs to be merged with parent.
					// 子bean定义：需要与父级合并。
					BeanDefinition pbd;
					try {
						String parentBeanName = transformedBeanName(bd.getParentName());
						if (!beanName.equals(parentBeanName)) {
							pbd = getMergedBeanDefinition(parentBeanName);
						}
						else {
							if (getParentBeanFactory() instanceof ConfigurableBeanFactory) {
								pbd = ((ConfigurableBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(parentBeanName);
							}
							else {
								throw new NoSuchBeanDefinitionException(bd.getParentName(),
										"Parent name '" + bd.getParentName() + "' is equal to bean name '" + beanName +
										"': cannot be resolved without an AbstractBeanFactory parent");
							}
						}
					}
					catch (NoSuchBeanDefinitionException ex) {
						throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
								"Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
					}
					// Deep copy with overridden values.
					// 具有重写值的深层复制。
					mbd = new RootBeanDefinition(pbd);
					mbd.overrideFrom(bd);
				}

				// Set default singleton scope, if not configured before.
				// 如果之前未配置，则设置默认单例范围。
				if (!StringUtils.hasLength(mbd.getScope())) {
					mbd.setScope(RootBeanDefinition.SCOPE_SINGLETON);
				}

				// A bean contained in a non-singleton bean cannot be a singleton itself.
				// Let's correct this on the fly here, since this might be the result of
				// parent-child merging for the outer bean, in which case the original inner bean
				// definition will not have inherited the merged outer bean's singleton status.
				
				//包含在非单例bean中的bean本身不能是单例。请在此处动态更正，因为这可能是外部bean的父子合并的结果，
				// 在这种情况下，原始内部bean定义将不具有 继承了合并外bean的单例状态。
				if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
					mbd.setScope(containingBd.getScope());
				}

				// Only cache the merged bean definition if we're already about to create an
				// instance of the bean, or at least have already created an instance before.
				
				// 如果我们已经要创建bean的实例，或者至少之前已经创建了一个实例，那么只缓存合并的bean定义。
				if (containingBd == null && isCacheBeanMetadata() && isBeanEligibleForMetadataCaching(beanName)) {
					this.mergedBeanDefinitions.put(beanName, mbd);
				}
			}

			return mbd;
		}
	}

	/**
	 * Check the given merged bean definition,
	 * potentially throwing validation exceptions.
	 * 
	 * <p> 检查给定的合并bean定义，可能会抛出验证异常。
	 * 
	 * @param mbd the merged bean definition to check - 要检查的合并bean定义
	 * @param beanName the name of the bean - bean的名称
	 * @param args the arguments for bean creation, if any - bean创建的参数，如果有的话
	 * @throws BeanDefinitionStoreException in case of validation failure
	 * 
	 * <p> 在验证失败的情况下
	 */
	protected void checkMergedBeanDefinition(RootBeanDefinition mbd, String beanName, Object[] args)
			throws BeanDefinitionStoreException {

		// check if bean definition is not abstract
		// 检查bean定义是否不是抽象的
		if (mbd.isAbstract()) {
			throw new BeanIsAbstractException(beanName);
		}

		// Check validity of the usage of the args parameter. This can
		// only be used for prototypes constructed via a factory method.
		
		// 检查args参数用法的有效性。 这只能用于通过工厂方法构建的原型。
		if (args != null && !mbd.isPrototype()) {
			throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
					"Can only specify arguments for the getBean method when referring to a prototype bean definition");
		}
	}

	/**
	 * Remove the merged bean definition for the specified bean,
	 * recreating it on next access.
	 * 
	 * <p> 删除指定bean的合并bean定义，并在下次访问时重新创建它。
	 * 
	 * @param beanName the bean name to clear the merged definition for
	 * 
	 * <p> 用于清除合并定义的bean名称
	 * 
	 */
	protected void clearMergedBeanDefinition(String beanName) {
		this.mergedBeanDefinitions.remove(beanName);
	}

	/**
	 * Resolve the bean class for the specified bean definition,
	 * resolving a bean class name into a Class reference (if necessary)
	 * and storing the resolved Class in the bean definition for further use.
	 * 
	 * <p> 解析指定bean定义的bean类，将bean类名解析为Class引用（如果需要），并将已解析的Class存储在bean定义中以供进一步使用。
	 * 
	 * @param mbd the merged bean definition to determine the class for
	 * 
	 * <p> 合并的bean定义来确定类的类
	 * 
	 * @param beanName the name of the bean (for error handling purposes)
	 * 
	 * <p> bean的名称（用于错误处理）
	 * 
	 * @param typesToMatch the types to match in case of internal type matching purposes
	 * (also signals that the returned {@code Class} will never be exposed to application code)
	 * 
	 * <p> 在内部类型匹配的情况下匹配的类型（也表示返回的类永远不会暴露给应用程序代码）
	 * 
	 * @return the resolved bean class (or {@code null} if none) - 已解析的bean类（如果没有则为null）
	 * @throws CannotLoadBeanClassException if we failed to load the class - 如果我们没有加载该类
	 */
	protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch)
			throws CannotLoadBeanClassException {
		try {
			if (mbd.hasBeanClass()) {
				return mbd.getBeanClass();
			}
			if (System.getSecurityManager() != null) {
				return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
					public Class<?> run() throws Exception {
						return doResolveBeanClass(mbd, typesToMatch);
					}
				}, getAccessControlContext());
			}
			else {
				return doResolveBeanClass(mbd, typesToMatch);
			}
		}
		catch (PrivilegedActionException pae) {
			ClassNotFoundException ex = (ClassNotFoundException) pae.getException();
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
		}
		catch (ClassNotFoundException ex) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
		}
		catch (LinkageError err) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), err);
		}
	}

	private Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch) throws ClassNotFoundException {
		if (!ObjectUtils.isEmpty(typesToMatch)) {
			ClassLoader tempClassLoader = getTempClassLoader();
			if (tempClassLoader != null) {
				if (tempClassLoader instanceof DecoratingClassLoader) {
					DecoratingClassLoader dcl = (DecoratingClassLoader) tempClassLoader;
					for (Class<?> typeToMatch : typesToMatch) {
						dcl.excludeClass(typeToMatch.getName());
					}
				}
				String className = mbd.getBeanClassName();
				return (className != null ? ClassUtils.forName(className, tempClassLoader) : null);
			}
		}
		return mbd.resolveBeanClass(getBeanClassLoader());
	}

	/**
	 * Evaluate the given String as contained in a bean definition,
	 * potentially resolving it as an expression.
	 * 
	 * <p> 评估bean定义中包含的给定String，可能将其解析为表达式。
	 * 
	 * @param value the value to check - 要检查的值
	 * @param beanDefinition the bean definition that the value comes from - 值来自的bean定义
	 * @return the resolved value - 已解析的价值
	 * @see #setBeanExpressionResolver
	 */
	protected Object evaluateBeanDefinitionString(String value, BeanDefinition beanDefinition) {
		if (this.beanExpressionResolver == null) {
			return value;
		}
		Scope scope = (beanDefinition != null ? getRegisteredScope(beanDefinition.getScope()) : null);
		return this.beanExpressionResolver.evaluate(value, new BeanExpressionContext(this, scope));
	}


	/**
	 * Predict the eventual bean type (of the processed bean instance) for the
	 * specified bean. Called by {@link #getType} and {@link #isTypeMatch}.
	 * Does not need to handle FactoryBeans specifically, since it is only
	 * supposed to operate on the raw bean type.
	 * 
	 * <p> 预测指定bean的最终bean类型（已处理bean实例）。 由getType和isTypeMatch调用。 
	 * 不需要专门处理FactoryBeans，因为它只应该在raw bean类型上运行。
	 * 
	 * <p>This implementation is simplistic in that it is not able to
	 * handle factory methods and InstantiationAwareBeanPostProcessors.
	 * It only predicts the bean type correctly for a standard bean.
	 * To be overridden in subclasses, applying more sophisticated type detection.
	 * 
	 * <p> 此实现过于简单，因为它无法处理工厂方法和InstantiationAwareBeanPostProcessors。 
	 * 它只为标准bean正确预测bean类型。 要在子类中重写，应用更复杂的类型检测。
	 * 
	 * @param beanName the name of the bean - bean 的名称
	 * @param mbd the merged bean definition to determine the type for
	 * 
	 * <p> 合并的bean定义，以确定其类型
	 * 
	 * @param typesToMatch the types to match in case of internal type matching purposes
	 * (also signals that the returned {@code Class} will never be exposed to application code)
	 * 
	 * <p> 在内部类型匹配的情况下匹配的类型（也表示返回的类永远不会暴露给应用程序代码）
	 * 
	 * @return the type of the bean, or {@code null} if not predictable
	 * 
	 * <p> bean的类型，如果不可预测则为null
	 * 
	 */
	protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		if (mbd.getFactoryMethodName() != null) {
			return null;
		}
		return resolveBeanClass(mbd, beanName, typesToMatch);
	}

	/**
	 * Check whether the given bean is defined as a {@link FactoryBean}.
	 * 
	 * <p> 检查给定的bean是否定义为FactoryBean。
	 * 
	 * @param beanName the name of the bean - bean 的名称
	 * @param mbd the corresponding bean definition - 相应的bean定义
	 */
	protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {
		Class<?> beanType = predictBeanType(beanName, mbd, FactoryBean.class);
		return (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
	}

	/**
	 * Determine the bean type for the given FactoryBean definition, as far as possible.
	 * Only called if there is no singleton instance registered for the target bean already.
	 * 
	 * <p> 尽可能确定给定FactoryBean定义的bean类型。 仅在没有为目标bean注册单例实例时才调用。
	 * 
	 * <p>The default implementation creates the FactoryBean via {@code getBean}
	 * to call its {@code getObjectType} method. Subclasses are encouraged to optimize
	 * this, typically by just instantiating the FactoryBean but not populating it yet,
	 * trying whether its {@code getObjectType} method already returns a type.
	 * If no type found, a full FactoryBean creation as performed by this implementation
	 * should be used as fallback.
	 * 
	 * <p> 默认实现通过getBean创建FactoryBean以调用其getObjectType方法。 
	 * 鼓励子类优化它，通常只是实例化FactoryBean但不填充它，尝试它的getObjectType方法是否已经返回一个类型。
	 * 如果未找到类型，则应将此实现执行的完整FactoryBean创建用作回退。
	 *  
	 * @param beanName the name of the bean - bean的名称
	 * @param mbd the merged bean definition for the bean - bean的合并bean定义
	 * @return the type for the bean if determinable, or {@code null} else
	 * 
	 * <p> bean的类型（如果可确定），或者为null
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 * @see #getBean(String)
	 */
	protected Class<?> getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
		if (!mbd.isSingleton()) {
			return null;
		}
		try {
			FactoryBean<?> factoryBean = doGetBean(FACTORY_BEAN_PREFIX + beanName, FactoryBean.class, null, true);
			return getTypeForFactoryBean(factoryBean);
		}
		catch (BeanCreationException ex) {
			// Can only happen when getting a FactoryBean.
			// 只有在获得FactoryBean时才会发生。
			if (logger.isDebugEnabled()) {
				logger.debug("Ignoring bean creation exception on FactoryBean type check: " + ex);
			}
			onSuppressedException(ex);
			return null;
		}
	}

	/**
	 * Mark the specified bean as already created (or about to be created).
	 * 
	 * <p> 将指定的bean标记为已创建（或即将创建）。
	 * 
	 * <p>This allows the bean factory to optimize its caching for repeated
	 * creation of the specified bean.
	 * 
	 * <p> 这允许bean工厂优化其缓存以重复创建指定的bean。
	 * 
	 * @param beanName the name of the bean - bean 的名称
	 */
	protected void markBeanAsCreated(String beanName) {
		if (!this.alreadyCreated.containsKey(beanName)) {
			this.alreadyCreated.put(beanName, Boolean.TRUE);
		}
	}

	/**
	 * Perform appropriate cleanup of cached metadata after bean creation failed.
	 * 
	 * <p> bean创建失败后，执行适当的缓存元数据清理。
	 * 
	 * @param beanName the name of the bean - bean的名称
	 */
	protected void cleanupAfterBeanCreationFailure(String beanName) {
		this.alreadyCreated.remove(beanName);
	}

	/**
	 * Determine whether the specified bean is eligible for having
	 * its bean definition metadata cached.
	 * 
	 * <p> 确定指定的bean是否有资格缓存其bean定义元数据。
	 * 
	 * @param beanName the name of the bean - bean的名称
	 * @return {@code true} if the bean's metadata may be cached
	 * at this point already
	 * 
	 * <p> 如果此时可以缓存bean的元数据，则为true
	 * 
	 */
	protected boolean isBeanEligibleForMetadataCaching(String beanName) {
		return this.alreadyCreated.containsKey(beanName);
	}

	/**
	 * Remove the singleton instance (if any) for the given bean name,
	 * but only if it hasn't been used for other purposes than type checking.
	 * 
	 * <p> 删除给定bean名称的单例实例（如果有），但前提是它尚未用于除类型检查之外的其他目的。
	 * 
	 * @param beanName the name of the bean - bean 的名称
	 * @return {@code true} if actually removed, {@code false} otherwise
	 * 
	 * <p> 如果实际删除则为true，否则为false
	 * 
	 */
	protected boolean removeSingletonIfCreatedForTypeCheckOnly(String beanName) {
		if (!this.alreadyCreated.containsKey(beanName)) {
			removeSingleton(beanName);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Get the object for the given bean instance, either the bean
	 * instance itself or its created object in case of a FactoryBean.
	 * 
	 * <p> 获取给定bean实例的对象，如果是FactoryBean，则为bean实例本身或其创建的对象。
	 * 
	 * @param beanInstance the shared bean instance - 共享bean实例
	 * @param name name that may include factory dereference prefix - 可能包含工厂解除引用前缀的名称
	 * @param beanName the canonical bean name - 规范的bean名称
	 * @param mbd the merged bean definition - 合并的bean定义
	 * @return the object to expose for the bean - 要为bean公开的对象
	 */
	protected Object getObjectForBeanInstance(
			Object beanInstance, String name, String beanName, RootBeanDefinition mbd) {

		// Don't let calling code try to dereference the factory if the bean isn't a factory.
		// 如果bean不是工厂，请不要让调用代码尝试取消引用工厂。
		
		//如果指定的name是工厂相关(以&为前缀)且beanInstance又不是FactoryBean类型则验证不通过
		if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
		}

		// Now we have the bean instance, which may be a normal bean or a FactoryBean.
		// If it's a FactoryBean, we use it to create a bean instance, unless the
		// caller actually wants a reference to the factory.
		
		// 现在我们有了bean实例，它可能是普通的bean或FactoryBean。
		// 如果是FactoryBean，我们使用它来创建bean实例，除非调用者实际上想要引用工厂。
		
		/**
		 * 现在我们有了bean实例,这个实例可能是正常的bean或者是FactoryBean,如果是FactoryBean,我们使用它创建实例,但是如果用户
		 * 想要直接获取工厂实例而不是工厂的getObject方法对应的实例那么传入的name应该加入前缀&
		 */
		if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
			return beanInstance;
		}

		//加载FactoryBean
		Object object = null;
		if (mbd == null) {
			object = getCachedObjectForFactoryBean(beanName);
		}
		if (object == null) {
			// Return bean instance from factory.
			// 从工厂返回bean实例。
			
			//到这里已经明确知道beanInstance一定时FactoryBean类型
			FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
			// Caches object obtained from FactoryBean if it is a singleton.
			// 缓存从FactoryBean获取的对象（如果它是单例）。
			
			//containsBeanDefinition检测BeanDefinitionMap中也就是在所有已经加载的类中检测是否定义beanName
			if (mbd == null && containsBeanDefinition(beanName)) {
				/**
				 * 将存储XML配置文件的GenericBeanDefinition转换为RootBeanDefinition,如果
				 * 指定BeanName是子bean的话同时会合并父类的相关属性
				 */
				mbd = getMergedLocalBeanDefinition(beanName);
			}
			//是否是用户定义的而不是应用程序本身定义的
			boolean synthetic = (mbd != null && mbd.isSynthetic());
			object = getObjectFromFactoryBean(factory, beanName, !synthetic);
		}
		return object;
	}

	/**
	 * Determine whether the given bean name is already in use within this factory,
	 * i.e. whether there is a local bean or alias registered under this name or
	 * an inner bean created with this name.
	 * 
	 * <p> 确定给定的bean名称是否已在此工厂中使用，即是否存在以此名称注册的本地bean或别名，或者是否使用此名称创建的内部bean。
	 * 
	 * @param beanName the name to check - 要检查的名称
	 */
	public boolean isBeanNameInUse(String beanName) {
		return isAlias(beanName) || containsLocalBean(beanName) || hasDependentBean(beanName);
	}

	/**
	 * Determine whether the given bean requires destruction on shutdown.
	 * 
	 * <p> 确定给定的bean是否需要在关闭时销毁。
	 * 
	 * <p>The default implementation checks the DisposableBean interface as well as
	 * a specified destroy method and registered DestructionAwareBeanPostProcessors.
	 * 
	 * <p> 默认实现检查DisposableBean接口以及指定的destroy方法和已注册的DestructionAwareBeanPostProcessors。
	 * 
	 * @param bean the bean instance to check - 要检查的bean实例
	 * @param mbd the corresponding bean definition - 相应的bean定义
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see AbstractBeanDefinition#getDestroyMethodName()
	 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
	 */
	protected boolean requiresDestruction(Object bean, RootBeanDefinition mbd) {
		return (bean != null &&
				(DisposableBeanAdapter.hasDestroyMethod(bean, mbd) || hasDestructionAwareBeanPostProcessors()));
	}

	/**
	 * Add the given bean to the list of disposable beans in this factory,
	 * registering its DisposableBean interface and/or the given destroy method
	 * to be called on factory shutdown (if applicable). Only applies to singletons.
	 * 
	 * <p> 将给定的bean添加到此工厂中的一次性Bean列表中，注册其DisposableBean接口和/或在工厂关闭时调用的给定destroy方法（如果适用）。 
	 * 仅适用于单例。
	 * 
	 * @param beanName the name of the bean - bean的名称
	 * @param bean the bean instance - bean实例
	 * @param mbd the bean definition for the bean - bean的bean定义
	 * @see RootBeanDefinition#isSingleton
	 * @see RootBeanDefinition#getDependsOn
	 * @see #registerDisposableBean
	 * @see #registerDependentBean
	 */
	protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
		AccessControlContext acc = (System.getSecurityManager() != null ? getAccessControlContext() : null);
		if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
			if (mbd.isSingleton()) {
				// Register a DisposableBean implementation that performs all destruction
				// work for the given bean: DestructionAwareBeanPostProcessors,
				// DisposableBean interface, custom destroy method.
				
				// 注册一个DisposableBean实现，该实现执行给定bean的所有销毁工
				// 作：DestructionAwareBeanPostProcessors，DisposableBean接口，自定义销毁方法。
				
				/**
				 * 单例模式下注册需要销毁的bean,此方法中会处理实现DisposableBean的bean,并且对所有的bean使用
				 * DestructionAwareBeanPostProcessors处理
				 * DisposableBean的DestructionAwareBeanPostProcessors
				 */
				registerDisposableBean(beanName,
						new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
			}
			else {
				// A bean with a custom scope...
				// 具有自定义范围的bean ...
				
				//自定义scope的处理
				Scope scope = this.scopes.get(mbd.getScope());
				if (scope == null) {
					throw new IllegalStateException("No Scope registered for scope '" + mbd.getScope() + "'");
				}
				scope.registerDestructionCallback(beanName,
						new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
			}
		}
	}


	//---------------------------------------------------------------------
	// Abstract methods to be implemented by subclasses
	// 由子类实现的抽象方法
	//---------------------------------------------------------------------

	/**
	 * Check if this bean factory contains a bean definition with the given name.
	 * Does not consider any hierarchy this factory may participate in.
	 * Invoked by {@code containsBean} when no cached singleton instance is found.
	 * 
	 * <p> 检查此bean工厂是否包含具有给定名称的bean定义。 
	 * 不考虑此工厂可能参与的任何层次结构。当找不到缓存的单例实例时，由containsBean调用。
	 * 
	 * <p>Depending on the nature of the concrete bean factory implementation,
	 * this operation might be expensive (for example, because of directory lookups
	 * in external registries). However, for listable bean factories, this usually
	 * just amounts to a local hash lookup: The operation is therefore part of the
	 * public interface there. The same implementation can serve for both this
	 * template method and the public interface method in that case.
	 * 
	 * <p> 根据具体bean工厂实现的性质，此操作可能很昂贵（例如，由于外部注册表中的目录查找）。 
	 * 但是，对于可列出的bean工厂，这通常只相当于本地哈希查找：因此操作是那里的公共接口的一部分。 在这种情况下，
	 * 相同的实现可以用于此模板方法和公共接口方法。
	 * 
	 * @param beanName the name of the bean to look for - 要查找的bean的名称
	 * @return if this bean factory contains a bean definition with the given name
	 * 
	 * <p> 如果此bean工厂包含具有给定名称的bean定义
	 * 
	 * @see #containsBean
	 * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition
	 */
	protected abstract boolean containsBeanDefinition(String beanName);

	/**
	 * Return the bean definition for the given bean name.
	 * Subclasses should normally implement caching, as this method is invoked
	 * by this class every time bean definition metadata is needed.
	 * 
	 * <p> 返回给定bean名称的bean定义。 子类通常应该实现缓存，因为每次需要bean定义元数据时，此类都会调用此方法。
	 * 
	 * <p>Depending on the nature of the concrete bean factory implementation,
	 * this operation might be expensive (for example, because of directory lookups
	 * in external registries). However, for listable bean factories, this usually
	 * just amounts to a local hash lookup: The operation is therefore part of the
	 * public interface there. The same implementation can serve for both this
	 * template method and the public interface method in that case.
	 * 
	 * <p> 根据具体bean工厂实现的性质，此操作可能很昂贵（例如，由于外部注册表中的目录查找）。 
	 * 但是，对于可列出的bean工厂，这通常只相当于本地哈希查找：因此操作是那里的公共接口的一部分。 
	 * 在这种情况下，相同的实现可以用于此模板方法和公共接口方法。
	 * 
	 * @param beanName the name of the bean to find a definition for
	 * 
	 * <p> 要查找其定义的bean的名称
	 * 
	 * @return the BeanDefinition for this prototype name (never {@code null})
	 * 
	 * <p> 此原型名称的BeanDefinition（永不为null）
	 * 
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if the bean definition cannot be resolved
	 * 
	 * <p> 如果bean定义无法解决
	 * 
	 * @throws BeansException in case of errors - 如果有错误
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#getBeanDefinition
	 */
	protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

	/**
	 * Create a bean instance for the given bean definition.
	 * The bean definition will already have been merged with the parent
	 * definition in case of a child definition.
	 * 
	 * <p> 为给定的bean定义创建一个bean实例。 在子定义的情况下，bean定义已经与父定义合并。
	 * 
	 * <p>All the other methods in this class invoke this method, although
	 * beans may be cached after being instantiated by this method. All bean
	 * instantiation within this class is performed by this method.
	 * 
	 * <p> 此类中的所有其他方法都会调用此方法，尽管bean可以在通过此方法实例化后进行缓存。 
	 * 此类中的所有bean实例化都由此方法执行。
	 * 
	 * @param beanName the name of the bean - bean 的名称
	 * @param mbd the merged bean definition for the bean - bean的合并bean定义
	 * @param args arguments to use if creating a prototype using explicit arguments to a
	 * static factory method. This parameter must be {@code null} except in this case.
	 * 
	 * <p> 在使用静态工厂方法的显式参数创建原型时使用的参数。 除非在这种情况下，否则此参数必须为null。
	 * 
	 * @return a new instance of the bean - bean的新实例
	 * @throws BeanCreationException if the bean could not be created - 如果无法创建bean
	 */
	protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args)
			throws BeanCreationException;

}
