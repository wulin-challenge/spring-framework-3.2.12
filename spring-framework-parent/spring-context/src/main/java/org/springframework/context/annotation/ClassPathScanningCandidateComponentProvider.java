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

package org.springframework.context.annotation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * A component provider that scans the classpath from a base package. It then
 * applies exclude and include filters to the resulting classes to find candidates.
 * 
 * <p> 从基础包扫描类路径的组件提供程序。 然后，它会对结果类应用排除和包含过滤器以查找候选项。
 *
 * <p>This implementation is based on Spring's
 * {@link org.springframework.core.type.classreading.MetadataReader MetadataReader}
 * facility, backed by an ASM {@link org.springframework.asm.ClassReader ClassReader}.
 * 
 * <p> 此实现基于Spring的MetadataReader工具，由ASM ClassReader支持。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author Chris Beams
 * @since 2.5
 * @see org.springframework.core.type.classreading.MetadataReaderFactory
 * @see org.springframework.core.type.AnnotationMetadata
 * @see ScannedGenericBeanDefinition
 */
public class ClassPathScanningCandidateComponentProvider implements EnvironmentCapable, ResourceLoaderAware {

	static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	protected final Log logger = LogFactory.getLog(getClass());

	private Environment environment;

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private MetadataReaderFactory metadataReaderFactory =
			new CachingMetadataReaderFactory(this.resourcePatternResolver);

	private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

	private final List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();

	private final List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();


	/**
	 * Create a ClassPathScanningCandidateComponentProvider with a {@link StandardEnvironment}.
	 * 
	 * <p> 使用StandardEnvironment创建ClassPathScanningCandidateComponentProvider。
	 * 
	 * @param useDefaultFilters whether to register the default filters for the
	 * {@link Component @Component}, {@link Repository @Repository},
	 * {@link Service @Service}, and {@link Controller @Controller}
	 * stereotype annotations
	 * 
	 * <p> 是否注册@ Component，@ Repository，@ Service和@Controller构造型注释的默认过滤器
	 * 
	 * @see #registerDefaultFilters()
	 */
	public ClassPathScanningCandidateComponentProvider(boolean useDefaultFilters) {
		this(useDefaultFilters, new StandardEnvironment());
	}

	/**
	 * Create a ClassPathScanningCandidateComponentProvider with the given {@link Environment}.
	 * 
	 * <p> 使用给定的环境创建ClassPathScanningCandidateComponentProvider。
	 * 
	 * @param useDefaultFilters whether to register the default filters for the
	 * {@link Component @Component}, {@link Repository @Repository},
	 * {@link Service @Service}, and {@link Controller @Controller}
	 * stereotype annotations
	 * 
	 * <p> 是否注册@ Component，@ Repository，@ Service和@Controller构造型注释的默认过滤器
	 * 
	 * @param environment the Environment to use - 要使用的环境
	 * 
	 * @see #registerDefaultFilters()
	 */
	public ClassPathScanningCandidateComponentProvider(boolean useDefaultFilters, Environment environment) {
		if (useDefaultFilters) {
			registerDefaultFilters();
		}
		this.environment = environment;
	}


	/**
	 * Set the ResourceLoader to use for resource locations.
	 * This will typically be a ResourcePatternResolver implementation.
	 * 
	 * <p> 设置ResourceLoader以用于资源位置。 这通常是ResourcePatternResolver实现。
	 * 
	 * <p>Default is PathMatchingResourcePatternResolver, also capable of
	 * resource pattern resolving through the ResourcePatternResolver interface.
	 * 
	 * <p> 默认值为PathMatchingResourcePatternResolver，也可以通过ResourcePatternResolver接口解析资源模式。
	 * 
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
	}

	/**
	 * Return the ResourceLoader that this component provider uses.
	 * 
	 * <p> 返回此组件提供程序使用的ResourceLoader。
	 */
	public final ResourceLoader getResourceLoader() {
		return this.resourcePatternResolver;
	}

	/**
	 * Set the {@link MetadataReaderFactory} to use.
	 * 
	 * <p> 设置要使用的MetadataReaderFactory。
	 * 
	 * <p>Default is a {@link CachingMetadataReaderFactory} for the specified
	 * {@linkplain #setResourceLoader resource loader}.
	 * 
	 * <p> Default是指定资源加载器的CachingMetadataReaderFactory。
	 * 
	 * <p>Call this setter method <i>after</i> {@link #setResourceLoader} in order
	 * for the given MetadataReaderFactory to override the default factory.
	 * 
	 * <p> 在setResourceLoader之后调用此setter方法，以便给定的MetadataReaderFactory覆盖默认工厂。
	 * 
	 */
	public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
		this.metadataReaderFactory = metadataReaderFactory;
	}

	/**
	 * Return the MetadataReaderFactory used by this component provider.
	 * 
	 * <p> 返回此组件提供程序使用的MetadataReaderFactory。
	 */
	public final MetadataReaderFactory getMetadataReaderFactory() {
		return this.metadataReaderFactory;
	}

	/**
	 * Set the Environment to use when resolving placeholders and evaluating
	 * {@link Profile @Profile}-annotated component classes.
	 * 
	 * <p> 设置在解析占位符和评估@ Profile-annotated组件类时使用的Environment。
	 * 
	 * <p>The default is a {@link StandardEnvironment}
	 * 
	 * <p> 默认值为StandardEnvironment
	 * 
	 * @param environment the Environment to use - 要使用的环境
	 */
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public final Environment getEnvironment() {
		return this.environment;
	}

	/**
	 * Set the resource pattern to use when scanning the classpath.
	 * This value will be appended to each base package name.
	 * 
	 * <p> 设置扫描类路径时要使用的资源模式。 此值将附加到每个基本包名称。
	 * 
	 * @see #findCandidateComponents(String)
	 * @see #DEFAULT_RESOURCE_PATTERN
	 */
	public void setResourcePattern(String resourcePattern) {
		Assert.notNull(resourcePattern, "'resourcePattern' must not be null");
		this.resourcePattern = resourcePattern;
	}

	/**
	 * Add an include type filter to the <i>end</i> of the inclusion list.
	 * 
	 * <p> 将包含类型过滤器添加到包含列表的末尾。
	 */
	public void addIncludeFilter(TypeFilter includeFilter) {
		this.includeFilters.add(includeFilter);
	}

	/**
	 * Add an exclude type filter to the <i>front</i> of the exclusion list.
	 * 
	 * <p> 将排除类型过滤器添加到排除列表的前面。
	 * 
	 */
	public void addExcludeFilter(TypeFilter excludeFilter) {
		this.excludeFilters.add(0, excludeFilter);
	}

	/**
	 * Reset the configured type filters.
	 * 
	 * <p> 重置配置的类型过滤器。
	 * 
	 * @param useDefaultFilters whether to re-register the default filters for
	 * the {@link Component @Component}, {@link Repository @Repository},
	 * {@link Service @Service}, and {@link Controller @Controller}
	 * stereotype annotations
	 * 
	 * <p> 是否重新注册@ Component，@ Repository，@ Service和@Controller构造型注释的默认过滤器
	 * 
	 * @see #registerDefaultFilters()
	 */
	public void resetFilters(boolean useDefaultFilters) {
		this.includeFilters.clear();
		this.excludeFilters.clear();
		if (useDefaultFilters) {
			registerDefaultFilters();
		}
	}

	/**
	 * Register the default filter for {@link Component @Component}.
	 * 
	 * <p> 注册@Component的默认过滤器。
	 * 
	 * <p>This will implicitly register all annotations that have the
	 * {@link Component @Component} meta-annotation including the
	 * {@link Repository @Repository}, {@link Service @Service}, and
	 * {@link Controller @Controller} stereotype annotations.
	 * 
	 * <p> 这将隐式注册所有具有@Component元注释的注释，包括@ Repository，@ Service和@Controller构造型注释。
	 * 
	 * <p>Also supports Java EE 6's {@link javax.annotation.ManagedBean} and
	 * JSR-330's {@link javax.inject.Named} annotations, if available.
	 * 
	 * <p> 还支持Java EE 6的javax.annotation.ManagedBean和JSR-330的javax.inject.Named注释（如果可用）。
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected void registerDefaultFilters() {
	    // 添加Component 注解过滤器
	    //这就是为什么 @Service @Controller @Repostory @Component 能够起作用的原因。
		this.includeFilters.add(new AnnotationTypeFilter(Component.class));
		ClassLoader cl = ClassPathScanningCandidateComponentProvider.class.getClassLoader();
		try {
			 // 添加ManagedBean 注解过滤器
			this.includeFilters.add(new AnnotationTypeFilter(
					((Class<? extends Annotation>) ClassUtils.forName("javax.annotation.ManagedBean", cl)), false));
			logger.debug("JSR-250 'javax.annotation.ManagedBean' found and supported for component scanning");
		}
		catch (ClassNotFoundException ex) {
			// JSR-250 1.1 API (as included in Java EE 6) not available - simply skip.
			// JSR-250 1.1 API（包含在Java EE 6中）不可用 - 只需跳过。
		}
		try {
			// 添加Named 注解过滤器
			this.includeFilters.add(new AnnotationTypeFilter(
					((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Named", cl)), false));
			logger.debug("JSR-330 'javax.inject.Named' annotation found and supported for component scanning");
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - simply skip.
			// JSR-330 API不可用 - 只需跳过。
		}
	}


	/**
	 * Scan the class path for candidate components.
	 * 
	 * <p> 扫描候选组件的类路径。
	 * 
	 * @param basePackage the package to check for annotated classes
	 * 
	 * <p> 用于检查带注解类的包
	 * 
	 * @return a corresponding Set of autodetected bean definitions
	 * 
	 * <p> 相应的一组自动检测的bean定义
	 * 
	 */
	public Set<BeanDefinition> findCandidateComponents(String basePackage) {
		Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
		try {
			// 1.根据指定包名 生成包搜索路径
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					resolveBasePackage(basePackage) + "/" + this.resourcePattern;
			//2. 资源加载器 加载搜索路径下的 所有class 转换为 Resource[]
			Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
			boolean traceEnabled = logger.isTraceEnabled();
			boolean debugEnabled = logger.isDebugEnabled();
			// 3. 循环 处理每一个 resource 
			for (Resource resource : resources) {
				if (traceEnabled) {
					logger.trace("Scanning " + resource);
				}
				if (resource.isReadable()) {
					try {
						// 读取类的 注解信息 和 类信息 ，信息储存到  MetadataReader
						MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
						// 执行判断是否符合 过滤器规则，函数内部用过滤器 对metadataReader 过滤  
						if (isCandidateComponent(metadataReader)) {
							//把符合条件的 类转换成 BeanDefinition
							ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
							sbd.setResource(resource);
							sbd.setSource(resource);
							// 再次判断 如果是实体类 返回true,如果是抽象类，但是抽象方法 被 @Lookup 注解注释返回true 
							if (isCandidateComponent(sbd)) {
								if (debugEnabled) {
									logger.debug("Identified candidate component class: " + resource);
								}
								candidates.add(sbd);
							}
							else {
								if (debugEnabled) {
									logger.debug("Ignored because not a concrete top-level class: " + resource);
								}
							}
						}
						else {
							if (traceEnabled) {
								logger.trace("Ignored because not matching any filter: " + resource);
							}
						}
					}
					catch (Throwable ex) {
						throw new BeanDefinitionStoreException(
								"Failed to read candidate component class: " + resource, ex);
					}
				}
				else {
					if (traceEnabled) {
						logger.trace("Ignored because not readable: " + resource);
					}
				}
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}


	/**
	 * Resolve the specified base package into a pattern specification for
	 * the package search path.
	 * 
	 * <p> 将指定的基础包解析为包搜索路径的模式规范。
	 * 
	 * <p>The default implementation resolves placeholders against system properties,
	 * and converts a "."-based package path to a "/"-based resource path.
	 * 
	 * <p> 默认实现根据系统属性解析占位符，并将基于“。”的包路径转换为基于“/”的资源路径。
	 * 
	 * @param basePackage the base package as specified by the user
	 * 
	 * <p> 用户指定的基础包
	 * 
	 * @return the pattern specification to be used for package searching
	 * 
	 * <p> 用于包搜索的模式规范
	 * 
	 */
	protected String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(environment.resolveRequiredPlaceholders(basePackage));
	}

	/**
	 * Determine whether the given class does not match any exclude filter
	 * and does match at least one include filter.
	 * 
	 * <p> 确定给定的类是否与任何排除过滤器不匹配，并且确实匹配至少一个包含过滤器。
	 * 
	 * @param metadataReader the ASM ClassReader for the class
	 * 
	 * <p> 该类的ASM ClassReader
	 * 
	 * @return whether the class qualifies as a candidate component
	 * 
	 * <p> 该类是否有资格作为候选组件
	 * 
	 */
	protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
		for (TypeFilter tf : this.excludeFilters) {
			if (tf.match(metadataReader, this.metadataReaderFactory)) {
				return false;
			}
		}
		for (TypeFilter tf : this.includeFilters) {
			if (tf.match(metadataReader, this.metadataReaderFactory)) {
				AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
				if (!metadata.isAnnotated(Profile.class.getName())) {
					return true;
				}
				AnnotationAttributes profile = MetadataUtils.attributesFor(metadata, Profile.class);
				return this.environment.acceptsProfiles(profile.getStringArray("value"));
			}
		}
		return false;
	}

	/**
	 * Determine whether the given bean definition qualifies as candidate.
	 * 
	 * <p> 确定给定的bean定义是否符合候选条件。
	 * 
	 * <p>The default implementation checks whether the class is concrete
	 * (i.e. not abstract and not an interface). Can be overridden in subclasses.
	 * 
	 * <p> 默认实现检查类是否具体（即不是抽象而不是接口）。 可以在子类中重写。
	 * 
	 * @param beanDefinition the bean definition to check - 要检查的bean定义
	 * @return whether the bean definition qualifies as a candidate component
	 * 
	 * <p> bean定义是否有资格作为候选组件
	 * 
	 */
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return (beanDefinition.getMetadata().isConcrete() && beanDefinition.getMetadata().isIndependent());
	}


	/**
	 * Clear the underlying metadata cache, removing all cached class metadata.
	 * 
	 * <p> 清除基础元数据缓存，删除所有缓存的类元数据。
	 * 
	 */
	public void clearCache() {
		if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory) {
			((CachingMetadataReaderFactory) this.metadataReaderFactory).clearCache();
		}
	}

}
