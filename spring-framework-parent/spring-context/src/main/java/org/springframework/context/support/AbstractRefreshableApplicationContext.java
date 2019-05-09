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

package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

/**
 * Base class for {@link org.springframework.context.ApplicationContext}
 * implementations which are supposed to support multiple calls to {@link #refresh()},
 * creating a new internal bean factory instance every time.
 * Typically (but not necessarily), such a context will be driven by
 * a set of config locations to load bean definitions from.
 * 
 * <p> org.springframework.context.ApplicationContext实现的基类，它们应该支持对refresh（）的多次调用，
 * 每次都创建一个新的内部bean工厂实例。 通常（但不一定），这样的上下文将由一组配置位置驱动以从中加载bean定义。
 *
 * <p>The only method to be implemented by subclasses is {@link #loadBeanDefinitions},
 * which gets invoked on each refresh. A concrete implementation is supposed to load
 * bean definitions into the given
 * {@link org.springframework.beans.factory.support.DefaultListableBeanFactory},
 * typically delegating to one or more specific bean definition readers.
 * 
 * <p> 子类实现的唯一方法是loadBeanDefinitions，它在每次刷新时调用。 具体实现应该将bean定义加载到给定的
 * org.springframework.beans.factory.support.DefaultListableBeanFactory中，
 * 通常委托给一个或多个特定的bean定义读取器。
 *
 * <p><b>Note that there is a similar base class for WebApplicationContexts.</b>
 * {@link org.springframework.web.context.support.AbstractRefreshableWebApplicationContext}
 * provides the same subclassing strategy, but additionally pre-implements
 * all context functionality for web environments. There is also a
 * pre-defined way to receive config locations for a web context.
 * 
 * <p> 请注意，WebApplicationContexts有一个类似的基类。 
 * org.springframework.web.context.support.AbstractRefreshableWebApplicationContext提供相
 * 同的子类策略，但另外预先实现了Web环境的所有上下文功能。 还有一种预定义的方法来接收Web上下文的配置位置。
 *
 * <p>Concrete standalone subclasses of this base class, reading in a
 * specific bean definition format, are {@link ClassPathXmlApplicationContext}
 * and {@link FileSystemXmlApplicationContext}, which both derive from the
 * common {@link AbstractXmlApplicationContext} base class;
 * {@link org.springframework.context.annotation.AnnotationConfigApplicationContext}
 * supports {@code @Configuration}-annotated classes as a source of bean definitions.
 * 
 * <p> 这个基类的具体独立子类，以特定的bean定义格式读取，
 * 是ClassPathXmlApplicationContext和FileSystemXmlApplicationContext，
 * 它们都来自公共的AbstractXmlApplicationContext基类; 
 * org.springframework.context.annotation.AnnotationConfigApplicationContext支
 * 持@Configuration-annotated类作为bean定义的来源。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.3
 * @see #loadBeanDefinitions
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext
 * @see AbstractXmlApplicationContext
 * @see ClassPathXmlApplicationContext
 * @see FileSystemXmlApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {

	private Boolean allowBeanDefinitionOverriding;

	private Boolean allowCircularReferences;

	/** Bean factory for this context */
	/** 针对此上下文的Bean工厂 */
	private DefaultListableBeanFactory beanFactory;

	/** Synchronization monitor for the internal BeanFactory */
	/** 内部BeanFactory的同步监视器 */
	private final Object beanFactoryMonitor = new Object();


	/**
	 * Create a new AbstractRefreshableApplicationContext with no parent.
	 * 
	 * <p> 创建一个没有父级的新AbstractRefreshableApplicationContext。
	 */
	public AbstractRefreshableApplicationContext() {
	}

	/**
	 * Create a new AbstractRefreshableApplicationContext with the given parent context.
	 * 
	 * <p> 使用给定的父上下文创建新的AbstractRefreshableApplicationContext。
	 * 
	 * @param parent the parent context - 父上下文
	 */
	public AbstractRefreshableApplicationContext(ApplicationContext parent) {
		super(parent);
	}


	/**
	 * Set whether it should be allowed to override bean definitions by registering
	 * a different definition with the same name, automatically replacing the former.
	 * If not, an exception will be thrown. Default is "true".
	 * 
	 * <p> 设置是否允许通过注册具有相同名称的其他定义来覆盖bean定义，自动替换前者。 如果没有，将抛出异常。 默认为“true”。
	 * 
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	/**
	 * Set whether to allow circular references between beans - and automatically
	 * try to resolve them.
	 * 
	 * <p> 设置是否允许bean之间的循环引用 - 并自动尝试解决它们。
	 * 
	 * <p>Default is "true". Turn this off to throw an exception when encountering
	 * a circular reference, disallowing them completely.
	 * 
	 * <p> 默认为“true”。 将其关闭以在遇到循环引用时抛出异常，完全禁止它们。
	 * 
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowCircularReferences
	 */
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}


	/**
	 * This implementation performs an actual refresh of this context's underlying
	 * bean factory, shutting down the previous bean factory (if any) and
	 * initializing a fresh bean factory for the next phase of the context's lifecycle.
	 * 
	 * <p> 此实现执行此上下文的基础bean工厂的实际刷新，关闭先前的bean工厂（如果有）并初始化上一个生命周期的下一阶段的新bean工厂。
	 */
	@Override
	protected final void refreshBeanFactory() throws BeansException {
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}
		try {
			//创建DefaultListableBeanFactory
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			//为了序列化指定id,如果需要的话,让这个BeanFactory从id反序列化到BeanFactory对象
			beanFactory.setSerializationId(getId());
			/**
			 * 定制beanfactory ,设置相关属性,包括是否允许覆盖同名称的不同定义的对象以及循环依赖以及设置@Autowired 和 @Qalifier 注
			 * 解解析器 QualifierAnnotationAutowireCandidateResolver 
			 */
			customizeBeanFactory(beanFactory);
			//初始化DocumentReader,并进行xml文件读取及解析
			loadBeanDefinitions(beanFactory);
			synchronized (this.beanFactoryMonitor) {
				this.beanFactory = beanFactory;
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}

	@Override
	protected void cancelRefresh(BeansException ex) {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory != null)
				this.beanFactory.setSerializationId(null);
		}
		super.cancelRefresh(ex);
	}

	@Override
	protected final void closeBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			this.beanFactory.setSerializationId(null);
			this.beanFactory = null;
		}
	}

	/**
	 * Determine whether this context currently holds a bean factory,
	 * i.e. has been refreshed at least once and not been closed yet.
	 * 
	 * <p> 确定此上下文当前是否包含bean工厂，即已刷新至少一次但尚未关闭。
	 * 
	 */
	protected final boolean hasBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			return (this.beanFactory != null);
		}
	}

	@Override
	public final ConfigurableListableBeanFactory getBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("BeanFactory not initialized or already closed - " +
						"call 'refresh' before accessing beans via the ApplicationContext");
			}
			return this.beanFactory;
		}
	}


	/**
	 * Create an internal bean factory for this context.
	 * Called for each {@link #refresh()} attempt.
	 * 
	 * <p> 为此上下文创建内部bean工厂。 为每次refresh（）尝试调用。
	 * 
	 * <p>The default implementation creates a
	 * {@link org.springframework.beans.factory.support.DefaultListableBeanFactory}
	 * with the {@linkplain #getInternalParentBeanFactory() internal bean factory} of this
	 * context's parent as parent bean factory. Can be overridden in subclasses,
	 * for example to customize DefaultListableBeanFactory's settings.
	 * 
	 * <p> 默认实现创建一个org.springframework.beans.factory.support.DefaultListableBeanFactory，
	 * 并将此上下文的父内部bean工厂作为父bean工厂。 可以在子类中重写，例如，自定义DefaultListableBeanFactory的设置。
	 * 
	 * @return the bean factory for this context - 这个上下文的bean工厂
	 * 
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowEagerClassLoading
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowCircularReferences
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowRawInjectionDespiteWrapping
	 */
	protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory(getInternalParentBeanFactory());
	}

	/**
	 * Customize the internal bean factory used by this context.
	 * Called for each {@link #refresh()} attempt.
	 * 
	 * <p> 自定义此上下文使用的内部bean工厂。 为每次refresh（）尝试调用。
	 * 
	 * <p>The default implementation applies this context's
	 * {@linkplain #setAllowBeanDefinitionOverriding "allowBeanDefinitionOverriding"}
	 * and {@linkplain #setAllowCircularReferences "allowCircularReferences"} settings,
	 * if specified. Can be overridden in subclasses to customize any of
	 * {@link DefaultListableBeanFactory}'s settings.
	 * 
	 * <p> 默认实现应用此上下文的“allowBeanDefinitionOverriding”和“allowCircularReferences”设置（如果已指定）。 
	 * 可以在子类中重写以自定义任何DefaultListableBeanFactory的设置。
	 * 
	 * @param beanFactory the newly created bean factory for this context
	 * 
	 * <p> 这个上下文新创建的bean工厂
	 * 
	 * @see DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 * @see DefaultListableBeanFactory#setAllowCircularReferences
	 * @see DefaultListableBeanFactory#setAllowRawInjectionDespiteWrapping
	 * @see DefaultListableBeanFactory#setAllowEagerClassLoading
	 */
	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
		/**
		 * 如果属性 allowBeanDefinitionOverriding 不为空,设置给BeanFactory对象相应属性,次属性的含
		 * 义:是否允许覆盖同名称的不同定义的对象
		 */
		if (this.allowBeanDefinitionOverriding != null) {
			beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
		
		/**
		 * 如果属性 allowCircularReferences 不为空,设置给BeanFactory对象相应属性,
		 * 此属性的含义: 是否允许bean之间存在循环依赖
		 */
		if (this.allowCircularReferences != null) {
			beanFactory.setAllowCircularReferences(this.allowCircularReferences);
		}
		//用于@Qualifier 和 @Autowired
		beanFactory.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
	}

	/**
	 * Load bean definitions into the given bean factory, typically through
	 * delegating to one or more bean definition readers.
	 * 
	 * <p> 通常通过委托给一个或多个bean定义读取器，将bean定义加载到给定的bean工厂中。
	 * 
	 * @param beanFactory the bean factory to load bean definitions into
	 * 
	 * <p> bean工厂将bean定义加载到
	 * 
	 * @throws BeansException if parsing of the bean definitions failed - 如果解析bean定义失败
	 * @throws IOException if loading of bean definition files failed - 如果加载bean定义文件失败
	 * @see org.springframework.beans.factory.support.PropertiesBeanDefinitionReader
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 */
	protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory)
			throws BeansException, IOException;

}
