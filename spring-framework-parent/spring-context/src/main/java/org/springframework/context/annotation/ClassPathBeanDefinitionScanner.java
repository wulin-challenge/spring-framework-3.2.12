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

package org.springframework.context.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

/**
 * A bean definition scanner that detects bean candidates on the classpath,
 * registering corresponding bean definitions with a given registry ({@code BeanFactory}
 * or {@code ApplicationContext}).
 * 
 * <p> 一个bean定义扫描程序，它检测类路径上的bean候选者，使用给定的注册表（BeanFactory或ApplicationContext）注册相应的bean定义。
 *
 * <p>Candidate classes are detected through configurable type filters. The
 * default filters include classes that are annotated with Spring's
 * {@link org.springframework.stereotype.Component @Component},
 * {@link org.springframework.stereotype.Repository @Repository},
 * {@link org.springframework.stereotype.Service @Service}, or
 * {@link org.springframework.stereotype.Controller @Controller} stereotype.
 * 
 * <p> 通过可配置的类型过滤器检测候选类。 默认过滤器包括使用Spring的@ Component，@ Repository，@ Service或@Controller构造型注释的类。
 *
 * <p>Also supports Java EE 6's {@link javax.annotation.ManagedBean} and
 * JSR-330's {@link javax.inject.Named} annotations, if available.
 * 
 * <p> 还支持Java EE 6的javax.annotation.ManagedBean和JSR-330的javax.inject.Named注释（如果可用）。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see AnnotationConfigApplicationContext#scan
 * @see org.springframework.stereotype.Component
 * @see org.springframework.stereotype.Repository
 * @see org.springframework.stereotype.Service
 * @see org.springframework.stereotype.Controller
 */
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

	private final BeanDefinitionRegistry registry;

	private BeanDefinitionDefaults beanDefinitionDefaults = new BeanDefinitionDefaults();

	private String[] autowireCandidatePatterns;

	private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	private boolean includeAnnotationConfig = true;


	/**
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory.
	 * 
	 * <p> 为给定的bean工厂创建一个新的ClassPathBeanDefinitionScanner。
	 * 
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 * 
	 * <p> BeanFactory以BeanDefinitionRegistry的形式加载bean定义
	 * 
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		this(registry, true);
	}

	/**
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory.
	 * 
	 * <p> 为给定的bean工厂创建一个新的ClassPathBeanDefinitionScanner。
	 * 
	 * <p>If the passed-in bean factory does not only implement the
	 * {@code BeanDefinitionRegistry} interface but also the {@code ResourceLoader}
	 * interface, it will be used as default {@code ResourceLoader} as well. This will
	 * usually be the case for {@link org.springframework.context.ApplicationContext}
	 * implementations.
	 * 
	 * <p> 如果传入的bean工厂不仅实现了BeanDefinitionRegistry接口，而且还实现了ResourceLoader接口，
	 * 它也将用作默认的ResourceLoader。 这通常是org.springframework.context.ApplicationContext实现的情况。
	 * 
	 * <p>If given a plain {@code BeanDefinitionRegistry}, the default {@code ResourceLoader}
	 * will be a {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}.
	 * 
	 * <p> 如果给定一个普通的BeanDefinitionRegistry，则默认的ResourceLoader将是
	 * org.springframework.core.io.support.PathMatchingResourcePatternResolver。
	 * 
	 * <p>If the the passed-in bean factory also implements {@link EnvironmentCapable} its
	 * environment will be used by this reader.  Otherwise, the reader will initialize and
	 * use a {@link org.springframework.core.env.StandardEnvironment}. All
	 * {@code ApplicationContext} implementations are {@code EnvironmentCapable}, while
	 * normal {@code BeanFactory} implementations are not.
	 * 
	 * <p> 如果传入的bean工厂也实现了EnvironmentCapable，则此读取器将使用其环境。 否则，读者将初始化并使用
	 * org.springframework.core.env.StandardEnvironment。 所有ApplicationContext实现都是EnvironmentCapable，
	 * 而普通的BeanFactory实现则不是。
	 * 
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 * 
	 * <p> BeanFactory以BeanDefinitionRegistry的形式加载bean定义
	 * 
	 * @param useDefaultFilters whether to include the default filters for the
	 * {@link org.springframework.stereotype.Component @Component},
	 * {@link org.springframework.stereotype.Repository @Repository},
	 * {@link org.springframework.stereotype.Service @Service}, and
	 * {@link org.springframework.stereotype.Controller @Controller} stereotype
	 * annotations.
	 * 
	 * <p> 是否包含@ Component，@ Repository，@ Service和@Controller构造型注释的默认过滤器。
	 * 
	 * @see #setResourceLoader
	 * @see #setEnvironment
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
		this(registry, useDefaultFilters, getOrCreateEnvironment(registry));
	}

	/**
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory and
	 * using the given {@link Environment} when evaluating bean definition profile metadata.
	 * 
	 * <p> 为给定的bean工厂创建一个新的ClassPathBeanDefinitionScanner，并在评估bean定义概要文件元数据时使用给定的Environment。
	 * 
	 * <p>If the passed-in bean factory does not only implement the {@code
	 * BeanDefinitionRegistry} interface but also the {@link ResourceLoader} interface, it
	 * will be used as default {@code ResourceLoader} as well. This will usually be the
	 * case for {@link org.springframework.context.ApplicationContext} implementations.
	 * 
	 * <p> 如果传入的bean工厂不仅实现了BeanDefinitionRegistry接口，而且还实现了ResourceLoader接口，它也将用作默认的ResourceLoader。 
	 * 这通常是org.springframework.context.ApplicationContext实现的情况。
	 * 
	 * <p>If given a plain {@code BeanDefinitionRegistry}, the default {@code ResourceLoader}
	 * will be a {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}.
	 * 
	 * <p> 如果给定一个普通的BeanDefinitionRegistry，则默认的ResourceLoader将是
	 * org.springframework.core.io.support.PathMatchingResourcePatternResolver。
	 * 
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 * 
	 * <p> BeanFactory以BeanDefinitionRegistry的形式加载bean定义
	 * 
	 * @param useDefaultFilters whether to include the default filters for the
	 * 
	 * <p> 是否包含默认过滤器
	 * 
	 * @param environment the Spring {@link Environment} to use when evaluating bean
	 * definition profile metadata.
	 * {@link org.springframework.stereotype.Component @Component},
	 * {@link org.springframework.stereotype.Repository @Repository},
	 * {@link org.springframework.stereotype.Service @Service}, and
	 * {@link org.springframework.stereotype.Controller @Controller} stereotype
	 * annotations.
	 * 
	 * <p> 在评估bean定义概要文件元数据时使用的Spring Environment。 @ Component，@ Repository，@ Service和@Controller构造型注释。
	 * 
	 * @since 3.1
	 * @see #setResourceLoader
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment) {
		super(useDefaultFilters, environment);

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		// Determine ResourceLoader to use.
		// 确定要使用的ResourceLoader。
		if (this.registry instanceof ResourceLoader) {
			setResourceLoader((ResourceLoader) this.registry);
		}
	}


	/**
	 * Return the BeanDefinitionRegistry that this scanner operates on.
	 * 
	 * <p> 返回此扫描程序操作的BeanDefinitionRegistry。
	 * 
	 */
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * Set the defaults to use for detected beans.
	 * 
	 * <p> 设置用于检测到的bean的默认值。
	 * 
	 * @see BeanDefinitionDefaults
	 */
	public void setBeanDefinitionDefaults(BeanDefinitionDefaults beanDefinitionDefaults) {
		this.beanDefinitionDefaults =
				(beanDefinitionDefaults != null ? beanDefinitionDefaults : new BeanDefinitionDefaults());
	}

	/**
	 * Set the name-matching patterns for determining autowire candidates.
	 * 
	 * <p> 设置名称匹配模式以确定autowire候选者。
	 * 
	 * @param autowireCandidatePatterns the patterns to match against
	 * 
	 * <p> 要匹配的模式
	 * 
	 */
	public void setAutowireCandidatePatterns(String... autowireCandidatePatterns) {
		this.autowireCandidatePatterns = autowireCandidatePatterns;
	}

	/**
	 * Set the BeanNameGenerator to use for detected bean classes.
	 * 
	 * <p> 设置BeanNameGenerator以用于检测到的bean类。
	 * 
	 * <p>Default is a {@link AnnotationBeanNameGenerator}.
	 * 
	 * <p> 默认是AnnotationBeanNameGenerator。
	 * 
	 */
	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new AnnotationBeanNameGenerator());
	}

	/**
	 * Set the ScopeMetadataResolver to use for detected bean classes.
	 * Note that this will override any custom "scopedProxyMode" setting.
	 * 
	 * <p> 设置ScopeMetadataResolver以用于检测到的bean类。 请注意，这将覆盖任何自定义“scopedProxyMode”设置。
	 * 
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * 
	 * <p> 默认值为AnnotationScopeMetadataResolver。
	 * 
	 * @see #setScopedProxyMode
	 */
	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver = (scopeMetadataResolver != null ? scopeMetadataResolver : new AnnotationScopeMetadataResolver());
	}

	/**
	 * Specify the proxy behavior for non-singleton scoped beans.
	 * Note that this will override any custom "scopeMetadataResolver" setting.
	 * 
	 * <p> 指定非单例作用域的代理行为。 请注意，这将覆盖任何自定义“scopeMetadataResolver”设置。
	 * 
	 * <p>The default is {@link ScopedProxyMode#NO}.
	 * 
	 * <p> 默认值为ScopedProxyMode.NO。
	 * 
	 * @see #setScopeMetadataResolver
	 */
	public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
		this.scopeMetadataResolver = new AnnotationScopeMetadataResolver(scopedProxyMode);
	}

	/**
	 * Specify whether to register annotation config post-processors.
	 * 
	 * <p> 指定是否注册注释配置后处理器。
	 * 
	 * <p>The default is to register the post-processors. Turn this off
	 * to be able to ignore the annotations or to process them differently.
	 * 
	 * <p> 默认是注册后处理器。 将其关闭可以忽略注释或以不同方式处理它们。
	 * 
	 */
	public void setIncludeAnnotationConfig(boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
	}


	/**
	 * Perform a scan within the specified base packages.
	 * 
	 * <p> 在指定的基础包中执行扫描。
	 * 
	 * @param basePackages the packages to check for annotated classes
	 * 
	 * <p> 用于检查带注释类的包
	 * 
	 * @return number of beans registered - 注册的bean数量
	 */
	public int scan(String... basePackages) {
		int beanCountAtScanStart = this.registry.getBeanDefinitionCount();

		doScan(basePackages);

		// Register annotation config processors, if necessary.
		// 如有必要，注册注解配置处理器。
		
		//如果配置了 includeAnnotationConfig ,则注册对应注解的处理器以保证注解功能的正常使用.
		if (this.includeAnnotationConfig) {
			AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
		}

		return (this.registry.getBeanDefinitionCount() - beanCountAtScanStart);
	}

	/**
	 * Perform a scan within the specified base packages,
	 * returning the registered bean definitions.
	 * 
	 * <p> 在指定的基础包中执行扫描，返回已注册的bean定义。
	 * 
	 * <p>This method does <i>not</i> register an annotation config processor
	 * but rather leaves this up to the caller.
	 * 
	 * <p> 此方法不会注册注解配置处理器，而是将其留给调用者。
	 * 
	 * @param basePackages the packages to check for annotated classes
	 * 
	 * <p> 用于检查带注释类的包
	 * 
	 * @return set of beans registered if any for tooling registration purposes (never {@code null})
	 * 
	 * <p> 为工具注册目的注册的bean集（永不为null）
	 * 
	 */
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
		for (String basePackage : basePackages) {
			//扫描 basePackage路径下的java文件
			Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
			for (BeanDefinition candidate : candidates) {
				//解析scope属性
				ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
				candidate.setScope(scopeMetadata.getScopeName());
				String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);
				if (candidate instanceof AbstractBeanDefinition) {
					postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
				}
				if (candidate instanceof AnnotatedBeanDefinition) {
					//如果是 AnnotatedBeanDefinition 类型的bean,需要检测下常用注解如:primary,lazy等
					AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
				}
				//检测当前bean是否已经注册
				if (checkCandidate(beanName, candidate)) {
					BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
					//如果当前bean是用于生产代理的bean那么需要进一步处理
					definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
					beanDefinitions.add(definitionHolder);
					registerBeanDefinition(definitionHolder, this.registry);
				}
			}
		}
		return beanDefinitions;
	}

	/**
	 * Apply further settings to the given bean definition,
	 * beyond the contents retrieved from scanning the component class.
	 * 
	 * <p> 除了从扫描组件类检索的内容之外，还将其他设置应用于给定的bean定义。
	 * 
	 * @param beanDefinition the scanned bean definition
	 * 
	 * <p> 扫描的bean定义
	 * 
	 * @param beanName the generated bean name for the given bean
	 * 
	 * <p> 给定bean的生成bean名称
	 * 
	 */
	protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
		beanDefinition.applyDefaults(this.beanDefinitionDefaults);
		if (this.autowireCandidatePatterns != null) {
			beanDefinition.setAutowireCandidate(PatternMatchUtils.simpleMatch(this.autowireCandidatePatterns, beanName));
		}
	}

	/**
	 * Register the specified bean with the given registry.
	 * 
	 * <p> 使用给定的注册表注册指定的bean。
	 * 
	 * <p>Can be overridden in subclasses, e.g. to adapt the registration
	 * process or to register further bean definitions for each scanned bean.
	 * 
	 * <p> 可以在子类中重写，例如 调整注册过程或为每个扫描的bean注册更多的bean定义。
	 * 
	 * @param definitionHolder the bean definition plus bean name for the bean
	 * 
	 * <p> bean的定义以及bean的bean名称
	 * 
	 * @param registry the BeanDefinitionRegistry to register the bean with
	 * 
	 * <p> 用于注册bean的BeanDefinitionRegistry
	 * 
	 */
	protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}


	/**
	 * Check the given candidate's bean name, determining whether the corresponding
	 * bean definition needs to be registered or conflicts with an existing definition.
	 * 
	 * <p> 检查给定候选者的bean名称，确定是否需要注册相应的bean定义或与现有定义冲突。
	 * 
	 * @param beanName the suggested name for the bean - bean的建议名称
	 * @param beanDefinition the corresponding bean definition - 相应的bean定义
	 * @return {@code true} if the bean can be registered as-is;
	 * {@code false} if it should be skipped because there is an
	 * existing, compatible bean definition for the specified name
	 * 
	 * <p> 如果bean可以按原样注册，则为true; 如果应该跳过它，则为false，因为存在指定名称的现有兼容bean定义
	 * 
	 * @throws ConflictingBeanDefinitionException if an existing, incompatible
	 * bean definition has been found for the specified name
	 * 
	 * <p> 如果找到了指定名称的现有不兼容bean定义
	 */
	protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
		if (!this.registry.containsBeanDefinition(beanName)) {
			return true;
		}
		BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
		BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
		if (originatingDef != null) {
			existingDef = originatingDef;
		}
		if (isCompatible(beanDefinition, existingDef)) {
			return false;
		}
		throw new ConflictingBeanDefinitionException("Annotation-specified bean name '" + beanName +
				"' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " +
				"non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
	}

	/**
	 * Determine whether the given new bean definition is compatible with
	 * the given existing bean definition.
	 * 
	 * <p> 确定给定的新bean定义是否与给定的现有bean定义兼容。
	 * 
	 * <p>The default implementation considers them as compatible when the existing
	 * bean definition comes from the same source or from a non-scanning source.
	 * 
	 * <p> 当现有bean定义来自同一源或来自非扫描源时，默认实现将它们视为兼容。
	 * 
	 * @param newDefinition the new bean definition, originated from scanning
	 * 
	 * <p> 新的bean定义，源于扫描
	 * 
	 * @param existingDefinition the existing bean definition, potentially an
	 * explicitly defined one or a previously generated one from scanning
	 * 
	 * <p> 现有的bean定义，可能是明确定义的bean定义，也可能是先前从扫描中生成的定义
	 * 
	 * @return whether the definitions are considered as compatible, with the
	 * new definition to be skipped in favor of the existing definition
	 * 
	 * <p> 这些定义是否被认为是兼容的，新的定义有利于现有的定义
	 * 
	 */
	protected boolean isCompatible(BeanDefinition newDefinition, BeanDefinition existingDefinition) {
		return (!(existingDefinition instanceof ScannedGenericBeanDefinition) ||  // explicitly registered overriding bean - 显式注册覆盖bean
				newDefinition.getSource().equals(existingDefinition.getSource()) ||  // scanned same file twice - 扫描相同的文件两次
				newDefinition.equals(existingDefinition));  // scanned equivalent class twice - 两次扫描等效类
	}


	/**
	 * Get the Environment from the given registry if possible, otherwise return a new
	 * StandardEnvironment.
	 * 
	 * <p> 如果可能，从给定的注册表中获取环境，否则返回新的StandardEnvironment。
	 */
	private static Environment getOrCreateEnvironment(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		if (registry instanceof EnvironmentCapable) {
			return ((EnvironmentCapable) registry).getEnvironment();
		}
		return new StandardEnvironment();
	}

}
