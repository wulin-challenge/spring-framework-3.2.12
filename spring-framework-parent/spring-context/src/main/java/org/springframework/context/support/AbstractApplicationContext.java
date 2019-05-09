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

package org.springframework.context.support;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.LifecycleProcessor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.context.weaving.LoadTimeWeaverAwareProcessor;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Abstract implementation of the {@link org.springframework.context.ApplicationContext}
 * interface. Doesn't mandate the type of storage used for configuration; simply
 * implements common context functionality. Uses the Template Method design pattern,
 * requiring concrete subclasses to implement abstract methods.
 * 
 * <p> org.springframework.context.ApplicationContext接口的抽象实现。不要求用于配置的存储类型;
 * 只需实现常见的上下文功能使用Template Method设计模式，需要具体的子类来实现抽象方法。
 *
 * <p>In contrast to a plain BeanFactory, an ApplicationContext is supposed
 * to detect special beans defined in its internal bean factory:
 * Therefore, this class automatically registers
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessors},
 * {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessors}
 * and {@link org.springframework.context.ApplicationListener ApplicationListeners}
 * which are defined as beans in the context.
 * 
 * <p> 与普通BeanFactory相比，ApplicationContext应该检测在其内部bean工厂中定义的特殊bean：
 * 因此，该类自动注册BeanFactoryPostProcessors，BeanPostProcessors和ApplicationListeners，
 * 它们在上下文中定义为bean。
 *
 * <p>A {@link org.springframework.context.MessageSource} may also be supplied
 * as a bean in the context, with the name "messageSource"; otherwise, message
 * resolution is delegated to the parent context. Furthermore, a multicaster
 * for application events can be supplied as "applicationEventMulticaster" bean
 * of type {@link org.springframework.context.event.ApplicationEventMulticaster}
 * in the context; otherwise, a default multicaster of type
 * {@link org.springframework.context.event.SimpleApplicationEventMulticaster} will be used.
 * 
 * <p> org.springframework.context.MessageSource也可以在上下文中作为bean提供，
 * 名称为“messageSource”;否则，将消息解析委托给父上下文。此外，应用程序事件的多播器可以在上下文中作
 * 为org.springframework.context.event.ApplicationEventMulticaster类型
 * 的“applicationEventMulticaster”bean提供;否则，将使
 * 用org.springframework.context.event.SimpleApplicationEventMulticaster类型的默认多播器。
 *
 * <p>Implements resource loading through extending
 * {@link org.springframework.core.io.DefaultResourceLoader}.
 * Consequently treats non-URL resource paths as class path resources
 * (supporting full class path resource names that include the package path,
 * e.g. "mypackage/myresource.dat"), unless the {@link #getResourceByPath}
 * method is overwritten in a subclass.
 * 
 * <p> 通过扩展org.springframework.core.io.DefaultResourceLoader实现资源加载。
 * 因此，将非URL资源路径视为类路径资源（支持包含包路径的完整类路径资源名称，例
 * 如“mypackage / myresource.dat”），除非在子类中覆盖getResourceByPath方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since January 21, 2001
 * @see #refreshBeanFactory
 * @see #getBeanFactory
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.event.ApplicationEventMulticaster
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.MessageSource
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader
		implements ConfigurableApplicationContext, DisposableBean {

	/**
	 * Name of the MessageSource bean in the factory.
	 * If none is supplied, message resolution is delegated to the parent.
	 * 
	 * <p> 工厂中MessageSource bean的名称。 如果未提供，则将消息解析委派给父级。
	 * @see MessageSource
	 */
	public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";

	/**
	 * Name of the LifecycleProcessor bean in the factory.
	 * If none is supplied, a DefaultLifecycleProcessor is used.
	 * 
	 * <p> 工厂中LifecycleProcessor bean的名称。 如果未提供，则使用DefaultLifecycleProcessor。
	 * 
	 * @see org.springframework.context.LifecycleProcessor
	 * @see org.springframework.context.support.DefaultLifecycleProcessor
	 */
	public static final String LIFECYCLE_PROCESSOR_BEAN_NAME = "lifecycleProcessor";

	/**
	 * Name of the ApplicationEventMulticaster bean in the factory.
	 * If none is supplied, a default SimpleApplicationEventMulticaster is used.
	 * 
	 * <p> 工厂中ApplicationEventMulticaster bean的名称。 如果未提供，则使用默认的SimpleApplicationEventMulticaster。
	 * 
	 * @see org.springframework.context.event.ApplicationEventMulticaster
	 * @see org.springframework.context.event.SimpleApplicationEventMulticaster
	 */
	public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";


	static {
		// Eagerly load the ContextClosedEvent class to avoid weird classloader issues
		// on application shutdown in WebLogic 8.1. (Reported by Dustin Woods.)
		
		//急切加载ContextClosedEvent类，以避免在WebLogic 8.1中关闭应用程序时出现奇怪的类加载器问题。 （Dustin Woods报道。）
		ContextClosedEvent.class.getName();
	}


	/** Logger used by this class. Available to subclasses. */
	/** 此类使用的记录器。 可用于子类。 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Unique id for this context, if any */
	/** 此上下文的唯一ID（如果有） */
	private String id = ObjectUtils.identityToString(this);

	/** Display name */
	/** 显示名称 */
	private String displayName = ObjectUtils.identityToString(this);

	/** Parent context */
	/** 父亲上下文 */
	private ApplicationContext parent;

	/** BeanFactoryPostProcessors to apply on refresh */
	/** 要在刷新时应用的BeanFactoryPostProcessors */
	private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors =
			new ArrayList<BeanFactoryPostProcessor>();

	/** System time in milliseconds when this context started */
	/** 此上下文启动时的系统时间（以毫秒为单位） */
	private long startupDate;

	/** Flag that indicates whether this context is currently active */
	/** 指示此上下文当前是否处于活动状态的标志 */
	private boolean active = false;

	/** Flag that indicates whether this context has been closed already */
	/** 指示此上下文是否已关闭的标志 */
	private boolean closed = false;

	/** Synchronization monitor for the "active" flag */
	/** “active”标志的同步监视器 */
	private final Object activeMonitor = new Object();

	/** Synchronization monitor for the "refresh" and "destroy" */
	/** 用于“刷新”和“销毁”的同步监视器 */
	private final Object startupShutdownMonitor = new Object();

	/** Reference to the JVM shutdown hook, if registered */
	/** 如果已注册，则引用JVM关闭钩子 */
	private Thread shutdownHook;

	/** ResourcePatternResolver used by this context */
	/** 此上下文使用的ResourcePatternResolver */
	private ResourcePatternResolver resourcePatternResolver;

	/** LifecycleProcessor for managing the lifecycle of beans within this context */
	/** LifecycleProcessor，用于在此上下文中管理bean的生命周期 */
	private LifecycleProcessor lifecycleProcessor;

	/** MessageSource we delegate our implementation of this interface to */
	/** MessageSource我们将这个接口的实现委托给 */
	private MessageSource messageSource;

	/** Helper class used in event publishing */
	/** 事件发布中使用的助手类 */
	private ApplicationEventMulticaster applicationEventMulticaster;

	/** Statically specified listeners */
	/** 静态指定的侦听器 */
	private Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<ApplicationListener<?>>();

	/** Environment used by this context; initialized by {@link #createEnvironment()} */
	/** 本文所使用的环境; 由createEnvironment（）初始化 */
	private ConfigurableEnvironment environment;


	/**
	 * Create a new AbstractApplicationContext with no parent.
	 * 
	 * <p> 创建一个没有父项的新AbstractApplicationContext。
	 */
	public AbstractApplicationContext() {
		this.resourcePatternResolver = getResourcePatternResolver();
	}

	/**
	 * Create a new AbstractApplicationContext with the given parent context.
	 * 
	 * <p> 使用给定的父上下文创建新的AbstractApplicationContext。
	 * 
	 * @param parent the parent context - 父上下文
	 */
	public AbstractApplicationContext(ApplicationContext parent) {
		this();
		setParent(parent);
	}


	//---------------------------------------------------------------------
	// Implementation of ApplicationContext interface
	// ApplicationContext接口的实现
	//---------------------------------------------------------------------

	/**
	 * Set the unique id of this application context.
	 * 
	 * <p> 设置此应用程序上下文的唯一ID。
	 * 
	 * <p>Default is the object id of the context instance, or the name
	 * of the context bean if the context is itself defined as a bean.
	 * 
	 * <p> 默认值是上下文实例的对象标识，如果上下文本身定义为bean，则为上下文bean的名称。
	 * 
	 * @param id the unique id of the context - 上下文的唯一ID
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public String getApplicationName() {
		return "";
	}

	/**
	 * Set a friendly name for this context.
	 * Typically done during initialization of concrete context implementations.
	 * 
	 * <p> 为此上下文设置友好名称。 通常在具体上下文实现的初始化期间完成
	 * 
	 * <p>Default is the object id of the context instance.
	 * 
	 * <p> 默认值是上下文实例的对象ID。
	 */
	public void setDisplayName(String displayName) {
		Assert.hasLength(displayName, "Display name must not be empty");
		this.displayName = displayName;
	}

	/**
	 * Return a friendly name for this context.
	 * 
	 * <p> 返回此上下文的友好名称。
	 * @return a display name for this context (never {@code null})- 此上下文的显示名称（永不为null）
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Return the parent context, or {@code null} if there is no parent
	 * (that is, this context is the root of the context hierarchy).
	 * 
	 * <p> 返回父上下文，如果没有父上下文，则返回null（即，此上下文是上下文层次结构的根）。
	 */
	public ApplicationContext getParent() {
		return this.parent;
	}

	/**
	 * {@inheritDoc}
	 * <p>If {@code null}, a new environment will be initialized via
	 * {@link #createEnvironment()}.
	 * 
	 * <p> 如果为null，则将通过createEnvironment（）初始化新环境。
	 */
	public ConfigurableEnvironment getEnvironment() {
		if (this.environment == null) {
			this.environment = createEnvironment();
		}
		return this.environment;
	}

	/**
	 * {@inheritDoc}
	 * <p>Default value is determined by {@link #createEnvironment()}. Replacing the
	 * default with this method is one option but configuration through {@link
	 * #getEnvironment()} should also be considered. In either case, such modifications
	 * should be performed <em>before</em> {@link #refresh()}.
	 * 
	 * <p> 默认值由createEnvironment（）确定。 使用此方法替换默认值是一种选择，但也应考虑通过getEnvironment（）进行配置。 
	 * 在任何一种情况下，都应在refresh（）之前执行此类修改。
	 * 
	 * @see org.springframework.context.support.AbstractApplicationContext#createEnvironment
	 */
	public void setEnvironment(ConfigurableEnvironment environment) {
		this.environment = environment;
	}

	/**
	 * Return this context's internal bean factory as AutowireCapableBeanFactory,
	 * if already available.
	 * 
	 * <p> 如果已经可用，则将此上下文的内部bean工厂作为AutowireCapableBeanFactory返回。
	 * 
	 * @see #getBeanFactory()
	 */
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		return getBeanFactory();
	}

	/**
	 * Return the timestamp (ms) when this context was first loaded.
	 * 
	 * <p> 首次加载此上下文时，返回时间戳（ms）。
	 */
	public long getStartupDate() {
		return this.startupDate;
	}

	/**
	 * Publish the given event to all listeners.
	 * 
	 * <p> 将给定事件发布给所有侦听器。
	 * 
	 * <p>Note: Listeners get initialized after the MessageSource, to be able
	 * to access it within listener implementations. Thus, MessageSource
	 * implementations cannot publish events.
	 * 
	 * <p> 注意：监听器在MessageSource之后初始化，以便能够在监听器实现中访问它。 因此，MessageSource实现无法发布事件。
	 * 
	 * @param event the event to publish (may be application-specific or a
	 * standard framework event)
	 * 
	 * <p> 要发布的事件（可能是特定于应用程序或标准框架事件）
	 */
	public void publishEvent(ApplicationEvent event) {
		Assert.notNull(event, "Event must not be null");
		if (logger.isTraceEnabled()) {
			logger.trace("Publishing event in " + getDisplayName() + ": " + event);
		}
		getApplicationEventMulticaster().multicastEvent(event);
		if (this.parent != null) {
			this.parent.publishEvent(event);
		}
	}

	/**
	 * Return the internal ApplicationEventMulticaster used by the context.
	 * 
	 * <p> 返回上下文使用的内部ApplicationEventMulticaster。
	 * 
	 * @return the internal ApplicationEventMulticaster (never {@code null})
	 * 
	 * <p> 内部ApplicationEventMulticaster（永远不为null）
	 * 
	 * @throws IllegalStateException if the context has not been initialized yet
	 * 
	 * <p> 如果上下文尚未初始化
	 * 
	 */
	private ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
		if (this.applicationEventMulticaster == null) {
			throw new IllegalStateException("ApplicationEventMulticaster not initialized - " +
					"call 'refresh' before multicasting events via the context: " + this);
		}
		return this.applicationEventMulticaster;
	}

	/**
	 * Return the internal LifecycleProcessor used by the context.
	 * 
	 * <p> 返回上下文使用的内部LifecycleProcessor。
	 * 
	 * @return the internal LifecycleProcessor (never {@code null}) - 内部LifecycleProcessor（永不为null）
	 * @throws IllegalStateException if the context has not been initialized yet - 如果上下文尚未初始化
	 */
	private LifecycleProcessor getLifecycleProcessor() {
		if (this.lifecycleProcessor == null) {
			throw new IllegalStateException("LifecycleProcessor not initialized - " +
					"call 'refresh' before invoking lifecycle methods via the context: " + this);
		}
		return this.lifecycleProcessor;
	}

	/**
	 * Return the ResourcePatternResolver to use for resolving location patterns
	 * into Resource instances. Default is a
	 * {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver},
	 * supporting Ant-style location patterns.
	 * 
	 * <p> 返回ResourcePatternResolver以用于将位置模式解析为Resource实例。 默认
	 * 是org.springframework.core.io.support.PathMatchingResourcePatternResolver，支持Ant样式的位置模式。
	 * 
	 * <p>Can be overridden in subclasses, for extended resolution strategies,
	 * for example in a web environment.
	 * 
	 * <p> 可以在子类中重写，以用于扩展解决策略，例如在Web环境中。
	 * 
	 * <p><b>Do not call this when needing to resolve a location pattern.</b>
	 * Call the context's {@code getResources} method instead, which
	 * will delegate to the ResourcePatternResolver.
	 * 
	 * <p> 需要解析位置模式时不要调用此方法。 相反，调用上下文的getResources方法，该方法将委托给ResourcePatternResolver。
	 * 
	 * @return the ResourcePatternResolver for this context - 此上下文的ResourcePatternResolver
	 * @see #getResources
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 */
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new PathMatchingResourcePatternResolver(this);
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableApplicationContext interface
	// ConfigurableApplicationContext接口的实现
	//---------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * <p>The parent {@linkplain ApplicationContext#getEnvironment() environment} is
	 * {@linkplain ConfigurableEnvironment#merge(ConfigurableEnvironment) merged} with
	 * this (child) application context environment if the parent is non-{@code null} and
	 * its environment is an instance of {@link ConfigurableEnvironment}.
	 * 
	 * <p> 如果父项是非null且其环境是ConfigurableEnvironment的实例，则父环境将与此（子）应用程序上下文环境合并。
	 * 
	 * @see ConfigurableEnvironment#merge(ConfigurableEnvironment)
	 */
	public void setParent(ApplicationContext parent) {
		this.parent = parent;
		if (parent != null) {
			Environment parentEnvironment = parent.getEnvironment();
			if (parentEnvironment instanceof ConfigurableEnvironment) {
				getEnvironment().merge((ConfigurableEnvironment) parentEnvironment);
			}
		}
	}

	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
		this.beanFactoryPostProcessors.add(beanFactoryPostProcessor);
	}


	/**
	 * Return the list of BeanFactoryPostProcessors that will get applied
	 * to the internal BeanFactory.
	 * 
	 * <p> 返回将应用于内部BeanFactory的BeanFactoryPostProcessors列表。
	 */
	public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
		return this.beanFactoryPostProcessors;
	}

	public void addApplicationListener(ApplicationListener<?> listener) {
		if (this.applicationEventMulticaster != null) {
			this.applicationEventMulticaster.addApplicationListener(listener);
		}
		else {
			this.applicationListeners.add(listener);
		}
	}

	/**
	 * Return the list of statically specified ApplicationListeners.
	 * 
	 * <p> 返回静态指定的ApplicationListeners列表。
	 * 
	 */
	public Collection<ApplicationListener<?>> getApplicationListeners() {
		return this.applicationListeners;
	}

	/**
	 * Create and return a new {@link StandardEnvironment}.
	 * 
	 * <p> 创建并返回一个新的StandardEnvironment。
	 * 
	 * <p>Subclasses may override this method in order to supply
	 * a custom {@link ConfigurableEnvironment} implementation.
	 * 
	 * <p> 子类可以重写此方法以提供自定义的ConfigurableEnvironment实现。
	 * 
	 */
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardEnvironment();
	}

	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// Prepare this context for refreshing.
			// 准备此上下文以进行刷新。
			
			//准备刷新的上下文环境
			prepareRefresh();

			// Tell the subclass to refresh the internal bean factory.
			// 告诉子类刷新内部bean工厂。
			
			//初始化BeanFactory,并进行XML文件读取
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// Prepare the bean factory for use in this context.
			// 准备bean工厂以在此上下文中使用。
			
			//对BeanFactory进行各种功能填充
			prepareBeanFactory(beanFactory);

			try {
				// Allows post-processing of the bean factory in context subclasses.
				// 允许在上下文子类中对bean工厂进行后处理。
				
				//子类覆盖方法做额外的处理
				postProcessBeanFactory(beanFactory);

				// Invoke factory processors registered as beans in the context.
				// 在上下文中调用注册为bean的工厂处理器。
				
				//激活各种BeanFactory后处理器
				invokeBeanFactoryPostProcessors(beanFactory);

				// Register bean processors that intercept bean creation.
				// 注册拦截bean创建的bean处理器。
				
				//注册拦截bean创建的Bean处理器,这里只是注册,真正的调用实在getBean时候
				registerBeanPostProcessors(beanFactory);

				// Initialize message source for this context.
				// 初始化此上下文的消息源。
				
				//为上下文初始化Message源,即不同语言的消息体,国际化处理
				initMessageSource();

				// Initialize event multicaster for this context.
				// 初始化此上下文的事件多播器。
				
				//初始化引用消息广播器,并放入 "applicationEventMulticaster" bean中
				initApplicationEventMulticaster();

				// Initialize other special beans in specific context subclasses.
				// 在特定上下文子类中初始化其他特殊bean。
				
				//留给子类来初始化其他的bean
				onRefresh();

				// Check for listener beans and register them.
				// 检查监听器bean并注册它们。
				
				//在所有注册的bean中查找Listener bean ,注册到消息广播器中
				registerListeners();

				// Instantiate all remaining (non-lazy-init) singletons.
				// 实例化所有剩余（非延迟初始化）单例。
				
				//初始化剩下的单实例(非惰性的)
				
				//初始化非延迟加载单例
				finishBeanFactoryInitialization(beanFactory);

				// Last step: publish corresponding event.
				// 最后一步：发布相应的事件。
				
				//完成刷新过程,通知生命周期处理器 lifecycleProcessor 刷新过程,同时发出 ContextRefreshEvent通知别人
				finishRefresh();
			}

			catch (BeansException ex) {
				logger.warn("Exception encountered during context initialization - cancelling refresh attempt", ex);

				// Destroy already created singletons to avoid dangling resources.
				// 摧毁已经创建的单例以避免悬空资源。
				destroyBeans();

				// Reset 'active' flag.
				// 重置'有效'标志。
				cancelRefresh(ex);

				// Propagate exception to caller.
				// 向调用者传播异常。
				throw ex;
			}
		}
	}

	/**
	 * Prepare this context for refreshing, setting its startup date and
	 * active flag as well as performing any initialization of property sources.
	 * 
	 * <p> 准备此上下文以进行刷新，设置其启动日期和活动标志以及执行属性源的任何初始化。
	 * 
	 */
	protected void prepareRefresh() {
		this.startupDate = System.currentTimeMillis();

		synchronized (this.activeMonitor) {
			this.active = true;
		}

		if (logger.isInfoEnabled()) {
			logger.info("Refreshing " + this);
		}

		// Initialize any placeholder property sources in the context environment
		// 在上下文环境中初始化任何占位符属性源
		
		//留给子类实现
		initPropertySources();

		// Validate that all properties marked as required are resolvable
		// see ConfigurablePropertyResolver#setRequiredProperties
		
		// 验证标记为必需的所有属性是否可解析请参阅ConfigurablePropertyResolver＃setRequiredProperties
		
		//验证需要的属性文件是否都已经放入环境中
		getEnvironment().validateRequiredProperties();
	}

	/**
	 * <p>Replace any stub property sources with actual instances.
	 * 
	 * <p> 用实际实例替换任何存根属性源。
	 * 
	 * @see org.springframework.core.env.PropertySource.StubPropertySource
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#initServletPropertySources
	 */
	protected void initPropertySources() {
		// For subclasses: do nothing by default.
		// 对于子类：默认情况下不执行任何操作。
	}

	/**
	 * Tell the subclass to refresh the internal bean factory.
	 * 
	 * <p> 告诉子类刷新内部bean工厂。
	 * 
	 * @return the fresh BeanFactory instance
	 * 
	 * <p> 新鲜的BeanFactory实例
	 * 
	 * @see #refreshBeanFactory()
	 * @see #getBeanFactory()
	 */
	protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
		//初始化BeanFactory,并进行XML文件读取,并将得到的BeanFactory记录在当前实体的属性中
		refreshBeanFactory();
		//返回当前实体的beanFactory属性
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (logger.isDebugEnabled()) {
			logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
		}
		return beanFactory;
	}

	/**
	 * Configure the factory's standard context characteristics,
	 * such as the context's ClassLoader and post-processors.
	 * 
	 * <p> 配置工厂的标准上下文特征，例如上下文的ClassLoader和后处理器。
	 * @param beanFactory the BeanFactory to configure - 要配置的BeanFactory
	 */
	protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// Tell the internal bean factory to use the context's class loader etc.
		// 告诉内部bean工厂使用上下文的类加载器等。
		
		//设置BeanFactory的classLoader 为当前context的classLoader
		beanFactory.setBeanClassLoader(getClassLoader());
		/**
		 * 设置BeanFactory的表达式语言处理器,spring3增加了表达式语言的支持,默认可以使用 #{bean.xxx}的形式来调用相关属性值
		 */
		beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver());
		//为BeanFactory增加了一个默认的propertyEditor,这个主要是对bean的属性等设置管理的一个工具
		beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

		// Configure the bean factory with context callbacks.
		// 使用上下文回调配置bean工厂。
		
		//添加  BeanPostProcessor
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
		
		//设置了几个忽略自动装配的接口
		beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);

		// BeanFactory interface not registered as resolvable type in a plain factory.
		// MessageSource registered (and found for autowiring) as a bean.
		
		// BeanFactory接口未在普通工厂中注册为可解析类型。 MessageSource作为bean注册（并发现用于自动装配）。
		
		//设置了几个自动装配的特殊规则
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);

		// Detect a LoadTimeWeaver and prepare for weaving, if found.
		// 检测到LoadTimeWeaver并准备编织（如果找到）。
		
		//增加对AspectJ的支持
		if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			// Set a temporary ClassLoader for type matching.
			// 为类型匹配设置临时ClassLoader。
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}

		// Register default environment beans.
		// 注册默认环境bean。
		
		//添加默认的系统环境bean
		if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
		}
		if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
		}
		if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
		}
	}

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for registering special
	 * BeanPostProcessors etc in certain ApplicationContext implementations.
	 * 
	 * <p> 在标准初始化之后修改应用程序上下文的内部bean工厂。 将加载所有bean定义，但尚未实例化任何bean。 
	 * 这允许在某些ApplicationContext实现中注册特殊的BeanPostProcessors等。
	 * 
	 * @param beanFactory the bean factory used by the application context
	 * 
	 * <p> 应用程序上下文使用的bean工厂
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
	}

	/**
	 * Instantiate and invoke all registered BeanFactoryPostProcessor beans,
	 * respecting explicit order if given.
	 * 
	 * <p>实例化并调用所有已注册的BeanFactoryPostProcessor bean，如果给定，则遵守显式顺序。
	 * 
	 * <p>Must be called before singleton instantiation.
	 * 
	 * <p> 必须在单例实例化之前调用。
	 * 
	 */
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		// 首先调用BeanDefinitionRegistryPostProcessors，如果有的话。
		Set<String> processedBeans = new HashSet<String>();
		
		//对 BeanDefinitionRegistry类型的处理
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new LinkedList<BeanFactoryPostProcessor>();
			List<BeanDefinitionRegistryPostProcessor> registryPostProcessors =
					new LinkedList<BeanDefinitionRegistryPostProcessor>();
			
			/**
			 * 硬编码注册的后处理器
			 */
			for (BeanFactoryPostProcessor postProcessor : getBeanFactoryPostProcessors()) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryPostProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					/**
					 * 对于 BeanDefinitionRegistryPostProcessor 类型,在 BeanFactoryPostProcessor
					 * 的基础上还有自己定义的方法,需要调用
					 */
					registryPostProcessor.postProcessBeanDefinitionRegistry(registry);
					//记录常规 BeanFactoryPostProcessor
					registryPostProcessors.add(registryPostProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}
			/**
			 * 配置注册的后处理器
			 */
			Map<String, BeanDefinitionRegistryPostProcessor> beanMap =
					beanFactory.getBeansOfType(BeanDefinitionRegistryPostProcessor.class, true, false);
			List<BeanDefinitionRegistryPostProcessor> registryPostProcessorBeans =
					new ArrayList<BeanDefinitionRegistryPostProcessor>(beanMap.values());
			OrderComparator.sort(registryPostProcessorBeans);
			for (BeanDefinitionRegistryPostProcessor postProcessor : registryPostProcessorBeans) {
				//BeanDefinitionRegistryPostProcessor 的特殊处理
				postProcessor.postProcessBeanDefinitionRegistry(registry);
			}
			/**
			 * 激活 postProcessBeanFactory 方法,之前激活的是 postProcessBeanDefinitionRegistry
			 * 硬编码设置的 BeanDefinitionRegistryPostProcessor
			 */
			invokeBeanFactoryPostProcessors(registryPostProcessors, beanFactory);
			
			//配置的  BeanDefinitionRegistryPostProcessor
			invokeBeanFactoryPostProcessors(registryPostProcessorBeans, beanFactory);
			
			//常规 BeanFactoryPostProcessor
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
			processedBeans.addAll(beanMap.keySet());
		}
		else {
			// Invoke factory processors registered with the context instance.
			// 调用在上下文实例中注册的工厂处理器。
			invokeBeanFactoryPostProcessors(getBeanFactoryPostProcessors(), beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		// 不要在这里初始化FactoryBeans：我们需要保留所有未初始化的常规bean，让bean工厂的后处理器适用于它们！
		
		//对于配置中读取的 BeanFactorypostProcessor的处理
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		
		// 在实现PriorityOrdered，Ordered和其余部分的BeanFactoryPostProcessors之间分开。
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		List<String> orderedPostProcessorNames = new ArrayList<String>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();
		
		//对后处理器进行分类
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
				// skip - 已在上面的第一阶段处理过
				
				//已经处理器过了
			}
			else if (isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		// 首先，调用实现PriorityOrdered的BeanFactoryPostProcessors。
		
		//按照优先级进行排序
		OrderComparator.sort(priorityOrderedPostProcessors);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		// 接下来，调用实现Ordered的BeanFactoryPostProcessors。
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		
		//按照order排序
		OrderComparator.sort(orderedPostProcessors);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		// 最后，调用所有其他BeanFactoryPostProcessors。
		
		//无序,直接调用
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 * 
	 * <p> 调用给定的BeanFactoryPostProcessor bean。
	 */
	private void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Instantiate and invoke all registered BeanPostProcessor beans,
	 * respecting explicit order if given.
	 * 
	 * <p> 实例化并调用所有已注册的BeanPostProcessor bean，如果给定，则遵守显式顺序。
	 * 
	 * <p>Must be called before any instantiation of application beans.
	 * 
	 * <p> 必须在应用程序bean的任何实例化之前调用。
	 */
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		
		// 注册BeanPostProcessorChecker，在BeanPostProcessor实例化期间创建bean时记录信息消息，
		// 即当bean不符合由所有BeanPostProcessors处理的资格时。
		
		/**
		 * BeanPostProcessorChecker是一个普通的信息打印,可能会有些情况,当Spring的配置中的后处理器还没有被注册已经开始
		 * 了bean的初始化时便会打印出 BeanPostProcessorChecker中设定的信息
		 */
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		
		// 实现PriorityOrdered，Ordered和其余的BeanPostProcessors之间分开。
		// 使用PriorityOrdered保证排序
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<BeanPostProcessor>();
		//使用Ordered保证排序
		List<String> orderedPostProcessorNames = new ArrayList<String>();
		//无序BeanPostProcessor
		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();
		for (String ppName : postProcessorNames) {
			if (isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		// 首先，注册实现PriorityOrdered的BeanPostProcessors。
		OrderComparator.sort(priorityOrderedPostProcessors);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		// 接下来，注册实现Ordered的BeanPostProcessors。
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<BeanPostProcessor>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		OrderComparator.sort(orderedPostProcessors);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		// 现在，注册所有常规BeanPostProcessors。
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		// 最后，重新注册所有内部BeanPostProcessors。
		OrderComparator.sort(internalPostProcessors);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector());
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 *
	 * <p> 注册给定的BeanPostProcessor bean。
	 */
	private void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}

	/**
	 * Initialize the MessageSource.
	 * Use parent's if none defined in this context.
	 * 
	 * <p> 初始化MessageSource。 如果在此上下文中未定义，则使用parent。
	 * 
	 */
	protected void initMessageSource() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
			//如果在配置中已经设置了 messageSource , 那么将 messageSource 提取并记录在 this.messageSource中
			this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
			// Make MessageSource aware of parent MessageSource.
			// 使MessageSource知道父MessageSource。
			if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
				HierarchicalMessageSource hms = (HierarchicalMessageSource) this.messageSource;
				if (hms.getParentMessageSource() == null) {
					// Only set parent context as parent MessageSource if no parent MessageSource
					// registered already.
					
					//如果尚未注册父MessageSource，则仅将父上下文设置为父MessageSource。
					hms.setParentMessageSource(getInternalParentMessageSource());
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Using MessageSource [" + this.messageSource + "]");
			}
		}
		else {
			// Use empty MessageSource to be able to accept getMessage calls.
			// 使用空MessageSource可以接受getMessage调用。
			
			//如果用户没有定义配置文件,那么使用临时的 DelegatingMassageSource以便于作为调用getMessage方法的返回
			DelegatingMessageSource dms = new DelegatingMessageSource();
			dms.setParentMessageSource(getInternalParentMessageSource());
			this.messageSource = dms;
			beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate MessageSource with name '" + MESSAGE_SOURCE_BEAN_NAME +
						"': using default [" + this.messageSource + "]");
			}
		}
	}

	/**
	 * Initialize the ApplicationEventMulticaster.
	 * Uses SimpleApplicationEventMulticaster if none defined in the context.
	 * 
	 * <p> 初始化ApplicationEventMulticaster。 如果在上下文中没有定义，则使用SimpleApplicationEventMulticaster。
	 * 
	 * @see org.springframework.context.event.SimpleApplicationEventMulticaster
	 */
	protected void initApplicationEventMulticaster() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
			this.applicationEventMulticaster =
					beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
			}
		}
		else {
			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate ApplicationEventMulticaster with name '" +
						APPLICATION_EVENT_MULTICASTER_BEAN_NAME +
						"': using default [" + this.applicationEventMulticaster + "]");
			}
		}
	}

	/**
	 * Initialize the LifecycleProcessor.
	 * Uses DefaultLifecycleProcessor if none defined in the context.
	 * 
	 * <p> 初始化LifecycleProcessor。 如果在上下文中没有定义，则使用DefaultLifecycleProcessor。
	 * 
	 * @see org.springframework.context.support.DefaultLifecycleProcessor
	 */
	protected void initLifecycleProcessor() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
			this.lifecycleProcessor =
					beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
			}
		}
		else {
			DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
			defaultProcessor.setBeanFactory(beanFactory);
			this.lifecycleProcessor = defaultProcessor;
			beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate LifecycleProcessor with name '" +
						LIFECYCLE_PROCESSOR_BEAN_NAME +
						"': using default [" + this.lifecycleProcessor + "]");
			}
		}
	}

	/**
	 * Template method which can be overridden to add context-specific refresh work.
	 * Called on initialization of special beans, before instantiation of singletons.
	 * 
	 * <p> 模板方法，可以重写以添加特定于上下文的刷新工作。 在实例化单例之前调用特殊bean的初始化。
	 * 
	 * <p>This implementation is empty.
	 * 
	 * <p> 此实现为空。
	 * 
	 * @throws BeansException in case of errors - 如果有错误
	 * @see #refresh()
	 */
	protected void onRefresh() throws BeansException {
		// For subclasses: do nothing by default.
		// 对于子类：默认情况下不执行任何操作。
	}

	/**
	 * Add beans that implement ApplicationListener as listeners.
	 * Doesn't affect other listeners, which can be added without being beans.
	 * 
	 * <p> 添加实现ApplicationListener作为侦听器的bean。 不影响其他侦听器，可以添加而不是bean。
	 * 
	 */
	protected void registerListeners() {
		// Register statically specified listeners first.
		// 首先注册静态指定的侦听器。
		
		//硬编码方式注册的监听器处理
		for (ApplicationListener<?> listener : getApplicationListeners()) {
			getApplicationEventMulticaster().addApplicationListener(listener);
		}
		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let post-processors apply to them!
		
		// 不要在这里初始化FactoryBeans：我们需要保留所有未初始化的常规bean，让后处理器适用于它们！
		
		//配置文件注册的监听器处理
		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String lisName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(lisName);
		}
	}

	/**
	 * Subclasses can invoke this method to register a listener.
	 * Any beans in the context that are listeners are automatically added.
	 * 
	 * <p> 子类可以调用此方法来注册侦听器。 上下文中作为侦听器的任何bean都会自动添加。
	 * 
	 * <p>Note: This method only works within an active application context,
	 * i.e. when an ApplicationEventMulticaster is already available. Generally
	 * prefer the use of {@link #addApplicationListener} which is more flexible.
	 * 
	 * <p> 注意：此方法仅适用于活动的应用程序上下文，即当ApplicationEventMulticaster已经可用时。 
	 * 通常更喜欢使用更灵活的addApplicationListener。
	 * 
	 * @param listener the listener to register - 听众要注册
	 * @deprecated as of Spring 3.0, in favor of {@link #addApplicationListener}
	 * 
	 * <p> 从Spring 3.0开始，支持addApplicationListener
	 */
	@Deprecated
	protected void addListener(ApplicationListener<?> listener) {
		getApplicationEventMulticaster().addApplicationListener(listener);
	}

	/**
	 * Finish the initialization of this context's bean factory,
	 * initializing all remaining singleton beans.
	 * 
	 * <p> 完成此上下文的bean工厂的初始化，初始化所有剩余的单例bean。
	 * 
	 */
	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		// Initialize conversion service for this context.
		// 初始化此上下文的转换服务。
		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
				beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
			beanFactory.setConversionService(
					beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
		}

		// Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
		// 尽早初始化LoadTimeWeaverAware bean以允许尽早注册其变换器。
		String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
		for (String weaverAwareName : weaverAwareNames) {
			getBean(weaverAwareName);
		}

		// Stop using the temporary ClassLoader for type matching.
		// 停止使用临时ClassLoader进行类型匹配。
		beanFactory.setTempClassLoader(null);

		// Allow for caching all bean definition metadata, not expecting further changes.
		// 允许缓存所有bean定义元数据，而不期望进一步的更改。
		
		//冻结所有的bean定义,说明注册的bean定义将不被修改或任何进一步的处理.
		beanFactory.freezeConfiguration();

		// Instantiate all remaining (non-lazy-init) singletons.
		// 实例化所有剩余（非延迟初始化）单例。
		
		//初始化剩下的单实例(非惰性的)
		beanFactory.preInstantiateSingletons();
	}

	/**
	 * Finish the refresh of this context, invoking the LifecycleProcessor's
	 * onRefresh() method and publishing the
	 * {@link org.springframework.context.event.ContextRefreshedEvent}.
	 * 
	 * <p> 完成此上下文的刷新，调用LifecycleProcessor的onRefresh（）方法并发布
	 * org.springframework.context.event.ContextRefreshedEvent。
	 * 
	 */
	protected void finishRefresh() {
		// Initialize lifecycle processor for this context.
		// 为此上下文初始化生命周期处理器。
		initLifecycleProcessor();

		// Propagate refresh to lifecycle processor first.
		// 首先将刷新传播到生命周期处理器。
		getLifecycleProcessor().onRefresh();

		// Publish the final event.
		// 发布最终活动。
		publishEvent(new ContextRefreshedEvent(this));

		// Participate in LiveBeansView MBean, if active.
		// 如果处于活动状态，请参与LiveBeansView MBean。
		LiveBeansView.registerApplicationContext(this);
	}

	/**
	 * Cancel this context's refresh attempt, resetting the {@code active} flag
	 * after an exception got thrown.
	 * 
	 * <p> 取消此上下文的刷新尝试，在抛出异常后重置活动标志。
	 * 
	 * @param ex the exception that led to the cancellation
	 * 
	 * <p> 导致取消的例外情况
	 */
	protected void cancelRefresh(BeansException ex) {
		synchronized (this.activeMonitor) {
			this.active = false;
		}
	}


	/**
	 * Register a shutdown hook with the JVM runtime, closing this context
	 * on JVM shutdown unless it has already been closed at that time.
	 * 
	 * <p> 向JVM运行时注册关闭挂钩，在JVM关闭时关闭此上下文，除非此时已关闭。
	 * 
	 * <p>Delegates to {@code doClose()} for the actual closing procedure.
	 * 
	 * <p> 代表doClose（）进行实际的结账程序。
	 * 
	 * @see Runtime#addShutdownHook
	 * @see #close()
	 * @see #doClose()
	 */
	public void registerShutdownHook() {
		if (this.shutdownHook == null) {
			// No shutdown hook registered yet.
			// 尚未注册关闭挂钩。
			this.shutdownHook = new Thread() {
				@Override
				public void run() {
					doClose();
				}
			};
			Runtime.getRuntime().addShutdownHook(this.shutdownHook);
		}
	}

	/**
	 * DisposableBean callback for destruction of this instance.
	 * Only called when the ApplicationContext itself is running
	 * as a bean in another BeanFactory or ApplicationContext,
	 * which is rather unusual.
	 * 
	 * <p> DisposableBean回调用于销毁此实例。 仅当ApplicationContext本身作为另一个BeanFactory
	 * 或ApplicationContext中的bean运行时才调用，这是非常不寻常的。
	 * 
	 * <p>The {@code close} method is the native way to
	 * shut down an ApplicationContext.
	 * 
	 * <p> close方法是关闭ApplicationContext的本机方法。
	 * 
	 * @see #close()
	 * @see org.springframework.beans.factory.access.SingletonBeanFactoryLocator
	 */
	public void destroy() {
		close();
	}

	/**
	 * Close this application context, destroying all beans in its bean factory.
	 * 
	 * <p> 关闭此应用程序上下文，销毁其bean工厂中的所有bean。
	 * 
	 * <p>Delegates to {@code doClose()} for the actual closing procedure.
	 * Also removes a JVM shutdown hook, if registered, as it's not needed anymore.
	 * 
	 * <p> 代表doClose（）进行实际的结账程序。 如果已注册，还会删除JVM关闭挂钩，因为它不再需要。
	 * 
	 * @see #doClose()
	 * @see #registerShutdownHook()
	 */
	public void close() {
		synchronized (this.startupShutdownMonitor) {
			doClose();
			// If we registered a JVM shutdown hook, we don't need it anymore now:
			// We've already explicitly closed the context.
			
			// 如果我们注册了一个JVM关闭钩子，我们现在不再需要了它：我们已经明确地关闭了上下文。
			if (this.shutdownHook != null) {
				try {
					Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
				}
				catch (IllegalStateException ex) {
					// ignore - VM is already shutting down
					// 忽略 - VM已经关闭
				}
			}
		}
	}

	/**
	 * Actually performs context closing: publishes a ContextClosedEvent and
	 * destroys the singletons in the bean factory of this application context.
	 * 
	 * <p> 实际上执行上下文关闭：发布ContextClosedEvent并销毁此应用程序上下文的bean工厂中的单例。
	 * 
	 * <p>Called by both {@code close()} and a JVM shutdown hook, if any.
	 * 
	 * <p> 由close（）和JVM关闭挂钩调用，如果有的话。
	 * 
	 * @see org.springframework.context.event.ContextClosedEvent
	 * @see #destroyBeans()
	 * @see #close()
	 * @see #registerShutdownHook()
	 */
	protected void doClose() {
		boolean actuallyClose;
		synchronized (this.activeMonitor) {
			actuallyClose = this.active && !this.closed;
			this.closed = true;
		}

		if (actuallyClose) {
			if (logger.isInfoEnabled()) {
				logger.info("Closing " + this);
			}

			LiveBeansView.unregisterApplicationContext(this);

			try {
				// Publish shutdown event.
				// 发布关闭事件。
				publishEvent(new ContextClosedEvent(this));
			}
			catch (Throwable ex) {
				logger.warn("Exception thrown from ApplicationListener handling ContextClosedEvent", ex);
			}

			// Stop all Lifecycle beans, to avoid delays during individual destruction.
			// 停止所有生命周期bean，以避免在个别销毁期间出现延迟。
			try {
				getLifecycleProcessor().onClose();
			}
			catch (Throwable ex) {
				logger.warn("Exception thrown from LifecycleProcessor on context close", ex);
			}

			// Destroy all cached singletons in the context's BeanFactory.
			// 在上下文的BeanFactory中销毁所有缓存的单例。
			destroyBeans();

			// Close the state of this context itself.
			// 关闭此上下文本身的状态。
			closeBeanFactory();

			// Let subclasses do some final clean-up if they wish...
			// 如果他们希望子类做最后的清理......
			onClose();

			synchronized (this.activeMonitor) {
				this.active = false;
			}
		}
	}

	/**
	 * Template method for destroying all beans that this context manages.
	 * The default implementation destroy all cached singletons in this context,
	 * invoking {@code DisposableBean.destroy()} and/or the specified
	 * "destroy-method".
	 * 
	 * <p> 用于销毁此上下文管理的所有bean的模板方法。 默认实现在此上下文中销毁所有缓存的单例，
	 * 调用DisposableBean.destroy（）和/或指定的“destroy-method”。
	 * 
	 * <p>Can be overridden to add context-specific bean destruction steps
	 * right before or right after standard singleton destruction,
	 * while the context's BeanFactory is still active.
	 * 
	 * <p> 可以重写以在标准单例销毁之前或之后添加特定于上下文的Bean销毁步骤，而上下文的BeanFactory仍处于活动状态。
	 * 
	 * @see #getBeanFactory()
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroySingletons()
	 */
	protected void destroyBeans() {
		getBeanFactory().destroySingletons();
	}

	/**
	 * Template method which can be overridden to add context-specific shutdown work.
	 * The default implementation is empty.
	 * 
	 * <p> 模板方法，可以重写以添加特定于上下文的关闭工作。 默认实现为空。
	 * 
	 * <p>Called at the end of {@link #doClose}'s shutdown procedure, after
	 * this context's BeanFactory has been closed. If custom shutdown logic
	 * needs to execute while the BeanFactory is still active, override
	 * the {@link #destroyBeans()} method instead.
	 * 
	 * <p> 在关闭此上下文的BeanFactory之后，在doClose的关闭过程结束时调用。 
	 * 如果在BeanFactory仍处于活动状态时需要执行自定义关闭逻辑，请改为替换destroyBeans（）方法。
	 * 
	 */
	protected void onClose() {
		// For subclasses: do nothing by default.
		// 对于子类：默认情况下不执行任何操作。
	}

	public boolean isActive() {
		synchronized (this.activeMonitor) {
			return this.active;
		}
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory interface
	// BeanFactory接口的实现
	//---------------------------------------------------------------------

	public Object getBean(String name) throws BeansException {
		return getBeanFactory().getBean(name);
	}

	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return getBeanFactory().getBean(name, requiredType);
	}

	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBeanFactory().getBean(requiredType);
	}

	public Object getBean(String name, Object... args) throws BeansException {
		return getBeanFactory().getBean(name, args);
	}

	public boolean containsBean(String name) {
		return getBeanFactory().containsBean(name);
	}

	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isSingleton(name);
	}

	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isPrototype(name);
	}

	public boolean isTypeMatch(String name, Class<?> targetType) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isTypeMatch(name, targetType);
	}

	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getType(name);
	}

	public String[] getAliases(String name) {
		return getBeanFactory().getAliases(name);
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory interface
	// ListableBeanFactory接口的实现
	//---------------------------------------------------------------------

	public boolean containsBeanDefinition(String beanName) {
		return getBeanFactory().containsBeanDefinition(beanName);
	}

	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	public String[] getBeanNamesForType(Class<?> type) {
		return getBeanFactory().getBeanNamesForType(type);
	}

	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}

	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		return getBeanFactory().getBeansOfType(type);
	}

	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		return getBeanFactory().getBeansOfType(type, includeNonSingletons, allowEagerInit);
	}

	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
			throws BeansException {

		return getBeanFactory().getBeansWithAnnotation(annotationType);
	}

	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
		return getBeanFactory().findAnnotationOnBean(beanName, annotationType);
	}


	//---------------------------------------------------------------------
	// Implementation of HierarchicalBeanFactory interface
	// HierarchicalBeanFactory接口的实现
	//---------------------------------------------------------------------

	public BeanFactory getParentBeanFactory() {
		return getParent();
	}

	public boolean containsLocalBean(String name) {
		return getBeanFactory().containsLocalBean(name);
	}

	/**
	 * Return the internal bean factory of the parent context if it implements
	 * ConfigurableApplicationContext; else, return the parent context itself.
	 * 
	 * <p> 如果它实现了ConfigurableApplicationContext，则返回父上下文的内部bean工厂; 否则，返回父上下文本身。
	 * 
	 * @see org.springframework.context.ConfigurableApplicationContext#getBeanFactory
	 */
	protected BeanFactory getInternalParentBeanFactory() {
		return (getParent() instanceof ConfigurableApplicationContext) ?
				((ConfigurableApplicationContext) getParent()).getBeanFactory() : getParent();
	}


	//---------------------------------------------------------------------
	// Implementation of MessageSource interface
	// MessageSource接口的实现
	//---------------------------------------------------------------------

	public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
		return getMessageSource().getMessage(code, args, defaultMessage, locale);
	}

	public String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(code, args, locale);
	}

	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(resolvable, locale);
	}

	/**
	 * Return the internal MessageSource used by the context.
	 * 
	 * <p> 返回上下文使用的内部MessageSource。
	 * 
	 * @return the internal MessageSource (never {@code null})
	 * 
	 * <p> 内部MessageSource（永远不为null）
	 * 
	 * @throws IllegalStateException if the context has not been initialized yet
	 * 
	 * <p> 如果上下文尚未初始化
	 * 
	 */
	private MessageSource getMessageSource() throws IllegalStateException {
		if (this.messageSource == null) {
			throw new IllegalStateException("MessageSource not initialized - " +
					"call 'refresh' before accessing messages via the context: " + this);
		}
		return this.messageSource;
	}

	/**
	 * Return the internal message source of the parent context if it is an
	 * AbstractApplicationContext too; else, return the parent context itself.
	 * 
	 * <p> 如果它也是AbstractApplicationContext，则返回父上下文的内部消息源; 否则，返回父上下文本身。
	 * 
	 */
	protected MessageSource getInternalParentMessageSource() {
		return (getParent() instanceof AbstractApplicationContext) ?
			((AbstractApplicationContext) getParent()).messageSource : getParent();
	}


	//---------------------------------------------------------------------
	// Implementation of ResourcePatternResolver interface
	// ResourcePatternResolver接口的实现
	//---------------------------------------------------------------------

	public Resource[] getResources(String locationPattern) throws IOException {
		return this.resourcePatternResolver.getResources(locationPattern);
	}


	//---------------------------------------------------------------------
	// Implementation of Lifecycle interface
	// Lifecycle接口的实现
	//---------------------------------------------------------------------

	public void start() {
		getLifecycleProcessor().start();
		publishEvent(new ContextStartedEvent(this));
	}

	public void stop() {
		getLifecycleProcessor().stop();
		publishEvent(new ContextStoppedEvent(this));
	}

	public boolean isRunning() {
		return getLifecycleProcessor().isRunning();
	}


	//---------------------------------------------------------------------
	// Abstract methods that must be implemented by subclasses
	// 必须由子类实现的抽象方法
	//---------------------------------------------------------------------

	/**
	 * Subclasses must implement this method to perform the actual configuration load.
	 * The method is invoked by {@link #refresh()} before any other initialization work.
	 * 
	 * <p> 子类必须实现此方法才能执行实际的配置加载。 在任何其他初始化工作之前，refresh（）调用该方法。
	 * 
	 * <p>A subclass will either create a new bean factory and hold a reference to it,
	 * or return a single BeanFactory instance that it holds. In the latter case, it will
	 * usually throw an IllegalStateException if refreshing the context more than once.
	 * 
	 * <p> 子类将创建一个新的bean工厂并保存对它的引用，或者返回它所拥有的单个BeanFactory实例。 
	 * 在后一种情况下，如果多次刷新上下文，它通常会抛出IllegalStateException。
	 * 
	 * @throws BeansException if initialization of the bean factory failed
	 * 
	 * <p> 如果bean工厂的初始化失败
	 * 
	 * @throws IllegalStateException if already initialized and multiple refresh
	 * attempts are not supported
	 * 
	 * <p> 如果已初始化并且不支持多次刷新尝试
	 * 
	 */
	protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;

	/**
	 * Subclasses must implement this method to release their internal bean factory.
	 * This method gets invoked by {@link #close()} after all other shutdown work.
	 * 
	 * <p> 子类必须实现此方法才能释放其内部bean工厂。 在所有其他关闭工作之后，close（）调用此方法。
	 * 
	 * <p>Should never throw an exception but rather log shutdown failures.
	 * 
	 * <p> 永远不应该抛出异常，而是记录关闭失败。
	 * 
	 */
	protected abstract void closeBeanFactory();

	/**
	 * Subclasses must return their internal bean factory here. They should implement the
	 * lookup efficiently, so that it can be called repeatedly without a performance penalty.
	 * 
	 * <p> 子类必须在此处返回其内部bean工厂。 它们应该有效地实现查找，以便可以重复调用它而不会降低性能。
	 * 
	 * <p>Note: Subclasses should check whether the context is still active before
	 * returning the internal bean factory. The internal factory should generally be
	 * considered unavailable once the context has been closed.
	 * 
	 * <p> 注意：子类应在返回内部bean工厂之前检查上下文是否仍处于活动状态。 一旦关闭上下文，通常应将内部工厂视为不可用。
	 * 
	 * @return this application context's internal bean factory (never {@code null})
	 * 
	 * <p> 这个应用程序上下文的内部bean工厂（永远不为null）
	 * 
	 * @throws IllegalStateException if the context does not hold an internal bean factory yet
	 * (usually if {@link #refresh()} has never been called) or if the context has been
	 * closed already
	 * 
	 * <p> 如果上下文还没有持有内部bean工厂（通常是从未调用过refresh（））或者上下文已经关闭了
	 * 
	 * @see #refreshBeanFactory()
	 * @see #closeBeanFactory()
	 */
	public abstract ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;


	/**
	 * Return information about this context.
	 * 
	 * <p> 返回有关此上下文的信息。
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getDisplayName());
		sb.append(": startup date [").append(new Date(getStartupDate()));
		sb.append("]; ");
		ApplicationContext parent = getParent();
		if (parent == null) {
			sb.append("root of context hierarchy");
		}
		else {
			sb.append("parent: ").append(parent.getDisplayName());
		}
		return sb.toString();
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 * 
	 * <p> BeanPostProcessor，在BeanPostProcessor实例化期间创建bean时记录信息消息，
	 * 即当bean不符合由所有BeanPostProcessors处理的资格时。
	 * 
	 */
	private class BeanPostProcessorChecker implements BeanPostProcessor {

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (bean != null && !(bean instanceof BeanPostProcessor) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}
	}


	/**
	 * BeanPostProcessor that detects beans which implement the ApplicationListener interface.
	 * This catches beans that can't reliably be detected by getBeanNamesForType.
	 * 
	 * <p> BeanPostProcessor，用于检测实现ApplicationListener接口的bean。 
	 * 这会捕获getBeanNamesForType无法可靠检测到的bean。
	 * 
	 */
	private class ApplicationListenerDetector implements MergedBeanDefinitionPostProcessor {

		private final Map<String, Boolean> singletonNames = new ConcurrentHashMap<String, Boolean>(64);

		public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
			if (beanDefinition.isSingleton()) {
				this.singletonNames.put(beanName, Boolean.TRUE);
			}
		}

		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (bean instanceof ApplicationListener) {
				// potentially not detected as a listener by getBeanNamesForType retrieval
				// 可能未被getBeanNamesForType检索检测为侦听器
				Boolean flag = this.singletonNames.get(beanName);
				if (Boolean.TRUE.equals(flag)) {
					// singleton bean (top-level or inner): register on the fly
					// 单例bean（顶级或内部）：即时注册
					addApplicationListener((ApplicationListener<?>) bean);
				}
				else if (flag == null) {
					if (logger.isWarnEnabled() && !containsBean(beanName)) {
						// inner bean with other scope - can't reliably process events
						// 内部bean与其他范围 - 无法可靠地处理事件
						logger.warn("Inner bean '" + beanName + "' implements ApplicationListener interface " +
								"but is not reachable for event multicasting by its containing ApplicationContext " +
								"because it does not have singleton scope. Only top-level listener beans are allowed " +
								"to be of non-singleton scope.");
					}
					this.singletonNames.put(beanName, Boolean.FALSE);
				}
			}
			return bean;
		}
	}

}
