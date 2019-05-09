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

package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.SpringProperties;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Internal class that caches JavaBeans {@link java.beans.PropertyDescriptor}
 * information for a Java class. Not intended for direct use by application code.
 * 
 * <p> 用于为Java类缓存JavaBeans java.beans.PropertyDescriptor信息的内部类。不适合应用程序代码直
 * 接使用。
 *
 * <p>Necessary for own caching of descriptors within the application's
 * ClassLoader, rather than rely on the JDK's system-wide BeanInfo cache
 * (in order to avoid leaks on ClassLoader shutdown).
 * 
 * <p> 必须在应用程序的ClassLoader中自己缓存描述符，而不是依赖于JDK的系统范围BeanInfo缓
 * 存（以避免ClassLoader关闭时出现泄漏）。
 *
 * <p>Information is cached statically, so we don't need to create new
 * objects of this class for every JavaBean we manipulate. Hence, this class
 * implements the factory design pattern, using a private constructor and
 * a static {@link #forClass(Class)} factory method to obtain instances.
 * 
 * <p> 信息是静态缓存的，因此我们不需要为每个操作的JavaBean创建此类的新对象。因此，此类实现工厂设
 * 计模式，使用私有构造函数和静态forClass（Class）工厂方法来获取实例。
 *
 * <p>Note that for caching to work effectively, some preconditions need to be met:
 * Prefer an arrangement where the Spring jars live in the same ClassLoader as the
 * application classes, which allows for clean caching along with the application's
 * lifecycle in any case. For a web application, consider declaring a local
 * {@link org.springframework.web.util.IntrospectorCleanupListener} in {@code web.xml}
 * in case of a multi-ClassLoader layout, which will allow for effective caching as well.
 * 
 * <p> 请注意，要使缓存有效工作，需要满足一些先决条件：首选Spring jar与应用程序类位于同一ClassLoader中的安排，
 * 这样可以在任何情况下实现干净缓存以及应用程序的生命周期。对于Web应用程序，考虑在多类ClassLoader布局的情
 * 况下在web.xml中声明本地org.springframework.web.util.IntrospectorCleanupListener，
 * 这也将允许有效的缓存。
 *
 * <p>In case of a non-clean ClassLoader arrangement without a cleanup listener having
 * been set up, this class will fall back to a weak-reference-based caching model that
 * recreates much-requested entries every time the garbage collector removed them. In
 * such a scenario, consider the {@link #IGNORE_BEANINFO_PROPERTY_NAME} system property.
 * 
 * <p> 如果没有设置清理侦听器的非干净ClassLoader安排，此类将回退到基于弱引用的缓存模型，每次垃圾收集器删除它们时都会重
 * 新创建请求很多的条目。在这种情况下，请考虑IGNORE_BEANINFO_PROPERTY_NAME系统属性。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 05 May 2001
 * @see #acceptClassLoader(ClassLoader)
 * @see #clearClassLoader(ClassLoader)
 * @see #forClass(Class)
 */
public class CachedIntrospectionResults {

	/**
	 * System property that instructs Spring to use the {@link Introspector#IGNORE_ALL_BEANINFO}
	 * mode when calling the JavaBeans {@link Introspector}: "spring.beaninfo.ignore", with a
	 * value of "true" skipping the search for {@code BeanInfo} classes (typically for scenarios
	 * where no such classes are being defined for beans in the application in the first place).
	 * 
	 * <p> 指示Spring在调用JavaBeans Introspector时使用Introspector.IGNORE_ALL_BEANINFO模式的系统属
	 * 性：“spring.beaninfo.ignore”，其值为“true”，跳过搜索BeanInfo类（通常用于没有定义此类的情况）首先应用于应
	 * 用程序中的bean）。
	 * 
	 * <p>The default is "false", considering all {@code BeanInfo} metadata classes, like for
	 * standard {@link Introspector#getBeanInfo(Class)} calls. Consider switching this flag to
	 * "true" if you experience repeated ClassLoader access for non-existing {@code BeanInfo}
	 * classes, in case such access is expensive on startup or on lazy loading.
	 * 
	 * <p> 考虑到所有BeanInfo元数据类，默认值为“false”，例如标准的Introspector.getBeanInfo（Class）调用。
	 * 如果您遇到对不存在的BeanInfo类的重复ClassLoader访问，请考虑将此标志切换为“true”，以防此类
	 * 访问在启动或延迟加载时很昂贵。
	 * 
	 * <p>Note that such an effect may also indicate a scenario where caching doesn't work
	 * effectively: Prefer an arrangement where the Spring jars live in the same ClassLoader
	 * as the application classes, which allows for clean caching along with the application's
	 * lifecycle in any case. For a web application, consider declaring a local
	 * {@link org.springframework.web.util.IntrospectorCleanupListener} in {@code web.xml}
	 * in case of a multi-ClassLoader layout, which will allow for effective caching as well.
	 * 
	 * <p> 请注意，这样的效果还可能表示缓存无法有效工作的场景：首选Spring jar与应用程序类位于同一ClassLoader中的安排，
	 * 这样可以在任何情况下实现干净缓存以及应用程序的生命周期。对于Web应用程序，考虑在多类ClassLoader布局的情况下在web.xml中声
	 * 明本地org.springframework.web.util.IntrospectorCleanupListener，这也将允许有效的缓存。
	 * 
	 * @see Introspector#getBeanInfo(Class, int)
	 */
	public static final String IGNORE_BEANINFO_PROPERTY_NAME = "spring.beaninfo.ignore";


	private static final boolean shouldIntrospectorIgnoreBeaninfoClasses =
			SpringProperties.getFlag(IGNORE_BEANINFO_PROPERTY_NAME);

	/** Stores the BeanInfoFactory instances */
	/** 存储BeanInfoFactory实例 */
	private static List<BeanInfoFactory> beanInfoFactories = SpringFactoriesLoader.loadFactories(
			BeanInfoFactory.class, CachedIntrospectionResults.class.getClassLoader());

	private static final Log logger = LogFactory.getLog(CachedIntrospectionResults.class);

	/**
	 * Set of ClassLoaders that this CachedIntrospectionResults class will always
	 * accept classes from, even if the classes do not qualify as cache-safe.
	 * 
	 * <p> 这个CachedIntrospectionResults类将始终接受类的ClassLoaders集，即使这些类不符合缓存安全性。
	 */
	static final Set<ClassLoader> acceptedClassLoaders = new HashSet<ClassLoader>();

	/**
	 * Map keyed by class containing CachedIntrospectionResults.
	 * Needs to be a WeakHashMap with WeakReferences as values to allow
	 * for proper garbage collection in case of multiple class loaders.
	 * 
	 * <p> 由包含CachedIntrospectionResults的类键控的映射。 需要是WeakHashMap，WeakReferences作为值，
	 * 以便在多个类加载器的情况下允许正确的垃圾收集。
	 * 
	 */
	static final Map<Class<?>, Object> classCache = new WeakHashMap<Class<?>, Object>();


	/**
	 * Accept the given ClassLoader as cache-safe, even if its classes would
	 * not qualify as cache-safe in this CachedIntrospectionResults class.
	 * 
	 * <p> 接受给定的ClassLoader作为缓存安全，即使它的类在此CachedIntrospectionResults类中不符合缓存安全性。
	 * 
	 * <p>This configuration method is only relevant in scenarios where the Spring
	 * classes reside in a 'common' ClassLoader (e.g. the system ClassLoader)
	 * whose lifecycle is not coupled to the application. In such a scenario,
	 * CachedIntrospectionResults would by default not cache any of the application's
	 * classes, since they would create a leak in the common ClassLoader.
	 * 
	 * <p> 此配置方法仅在Spring类驻留在其生命周期未与应用程序耦合的“公共”ClassLoader（例如系统ClassLoader）的情况下相关。 
	 * 在这种情况下，CachedIntrospectionResults默认不会缓存任何应用程序的类，因为它们会在公共ClassLoader中创建泄漏。
	 * 
	 * <p>Any {@code acceptClassLoader} call at application startup should
	 * be paired with a {@link #clearClassLoader} call at application shutdown.
	 * 
	 * <p> 应用程序启动时的任何acceptClassLoader调用都应该在应用程序关闭时与clearClassLoader调用配对。
	 * 
	 * @param classLoader the ClassLoader to accept
	 */
	public static void acceptClassLoader(ClassLoader classLoader) {
		if (classLoader != null) {
			synchronized (acceptedClassLoaders) {
				acceptedClassLoaders.add(classLoader);
			}
		}
	}

	/**
	 * Clear the introspection cache for the given ClassLoader, removing the
	 * introspection results for all classes underneath that ClassLoader, and
	 * removing the ClassLoader (and its children) from the acceptance list.
	 * 
	 * <p> 清除给定ClassLoader的内省缓存，删除该ClassLoader下所有类的内省结果，并从接受列表中删除ClassLoader（及其子项）。
	 * 
	 * @param classLoader the ClassLoader to clear the cache for
	 * 
	 * <p> 用于清除缓存的ClassLoader
	 * 
	 */
	public static void clearClassLoader(ClassLoader classLoader) {
		synchronized (classCache) {
			for (Iterator<Class<?>> it = classCache.keySet().iterator(); it.hasNext();) {
				Class<?> beanClass = it.next();
				if (isUnderneathClassLoader(beanClass.getClassLoader(), classLoader)) {
					it.remove();
				}
			}
		}
		synchronized (acceptedClassLoaders) {
			for (Iterator<ClassLoader> it = acceptedClassLoaders.iterator(); it.hasNext();) {
				ClassLoader registeredLoader = it.next();
				if (isUnderneathClassLoader(registeredLoader, classLoader)) {
					it.remove();
				}
			}
		}
	}

	/**
	 * Create CachedIntrospectionResults for the given bean class.
	 * 
	 * <p> 为给定的bean类创建CachedIntrospectionResults。
	 * 
	 * @param beanClass the bean class to analyze - 要分析的bean类
	 * @return the corresponding CachedIntrospectionResults - 相应的CachedIntrospectionResults
	 * @throws BeansException in case of introspection failure - 在内省失败的情况下
	 */
	@SuppressWarnings("unchecked")
	static CachedIntrospectionResults forClass(Class<?> beanClass) throws BeansException {
		CachedIntrospectionResults results;
		Object value;
		synchronized (classCache) {
			value = classCache.get(beanClass);
		}
		if (value instanceof Reference) {
			Reference<CachedIntrospectionResults> ref = (Reference<CachedIntrospectionResults>) value;
			results = ref.get();
		}
		else {
			results = (CachedIntrospectionResults) value;
		}
		if (results == null) {
			if (ClassUtils.isCacheSafe(beanClass, CachedIntrospectionResults.class.getClassLoader()) ||
					isClassLoaderAccepted(beanClass.getClassLoader())) {
				results = new CachedIntrospectionResults(beanClass);
				synchronized (classCache) {
					classCache.put(beanClass, results);
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Not strongly caching class [" + beanClass.getName() + "] because it is not cache-safe");
				}
				results = new CachedIntrospectionResults(beanClass);
				synchronized (classCache) {
					classCache.put(beanClass, new SoftReference<CachedIntrospectionResults>(results));
				}
			}
		}
		return results;
	}

	/**
	 * Check whether this CachedIntrospectionResults class is configured
	 * to accept the given ClassLoader.
	 * 
	 * <p> 检查此CachedIntrospectionResults类是否配置为接受给定的ClassLoader。
	 * 
	 * @param classLoader the ClassLoader to check - 要检查的ClassLoader
	 * @return whether the given ClassLoader is accepted - 是否接受给定的ClassLoader
	 * @see #acceptClassLoader
	 */
	private static boolean isClassLoaderAccepted(ClassLoader classLoader) {
		// Iterate over array copy in order to avoid synchronization for the entire
		// ClassLoader check (avoiding a synchronized acceptedClassLoaders Iterator).
		
		// 迭代数组副本以避免同步整个ClassLoader检查（避免同步的acceptedClassLoaders迭代器）。
		ClassLoader[] acceptedLoaderArray;
		synchronized (acceptedClassLoaders) {
			acceptedLoaderArray = acceptedClassLoaders.toArray(new ClassLoader[acceptedClassLoaders.size()]);
		}
		for (ClassLoader acceptedLoader : acceptedLoaderArray) {
			if (isUnderneathClassLoader(classLoader, acceptedLoader)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the given ClassLoader is underneath the given parent,
	 * that is, whether the parent is within the candidate's hierarchy.
	 * 
	 * <p> 检查给定的ClassLoader是否在给定父级下面，即父级是否在候选者的层次结构中。
	 * 
	 * @param candidate the candidate ClassLoader to check - 要检查的候选ClassLoader
	 * @param parent the parent ClassLoader to check for - 要检查的父ClassLoader
	 */
	private static boolean isUnderneathClassLoader(ClassLoader candidate, ClassLoader parent) {
		if (candidate == parent) {
			return true;
		}
		if (candidate == null) {
			return false;
		}
		ClassLoader classLoaderToCheck = candidate;
		while (classLoaderToCheck != null) {
			classLoaderToCheck = classLoaderToCheck.getParent();
			if (classLoaderToCheck == parent) {
				return true;
			}
		}
		return false;
	}


	/** The BeanInfo object for the introspected bean class */
	/** 内省bean类的BeanInfo对象 */
	private final BeanInfo beanInfo;

	/** PropertyDescriptor objects keyed by property name String */
	/** PropertyDescriptor对象由属性名称String键控 */
	private final Map<String, PropertyDescriptor> propertyDescriptorCache;


	/**
	 * Create a new CachedIntrospectionResults instance for the given class.
	 * 
	 * <p> 为给定的类创建一个新的CachedIntrospectionResults实例。
	 * 
	 * @param beanClass the bean class to analyze - 要分析的bean类
	 * @throws BeansException in case of introspection failure - 在内省失败的情况下
	 */
	private CachedIntrospectionResults(Class<?> beanClass) throws BeansException {
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("Getting BeanInfo for class [" + beanClass.getName() + "]");
			}

			BeanInfo beanInfo = null;
			for (BeanInfoFactory beanInfoFactory : beanInfoFactories) {
				beanInfo = beanInfoFactory.getBeanInfo(beanClass);
				if (beanInfo != null) {
					break;
				}
			}
			if (beanInfo == null) {
				// If none of the factories supported the class, fall back to the default
				// 如果没有工厂支持该类，则回退到默认值
				beanInfo = (shouldIntrospectorIgnoreBeaninfoClasses ?
						Introspector.getBeanInfo(beanClass, Introspector.IGNORE_ALL_BEANINFO) :
						Introspector.getBeanInfo(beanClass));
			}
			this.beanInfo = beanInfo;

			// Only bother with flushFromCaches if the Introspector actually cached...
			// 如果Introspector实际缓存了，只需麻烦flushFromCaches ......
			if (!shouldIntrospectorIgnoreBeaninfoClasses) {
				// Immediately remove class from Introspector cache, to allow for proper
				// garbage collection on class loader shutdown - we cache it here anyway,
				// in a GC-friendly manner. In contrast to CachedIntrospectionResults,
				// Introspector does not use WeakReferences as values of its WeakHashMap!
				
				/**
				 * 立即从Introspector缓存中删除类，以便在类加载器关闭时允许正确的垃圾收集 - 我们无论如何都
				 * 以GC友好的方式将其缓存在此处。 与CachedIntrospectionResults相反，Introspector不
				 * 使用WeakReferences作为其WeakHashMap的值！
				 */
				Class<?> classToFlush = beanClass;
				do {
					Introspector.flushFromCaches(classToFlush);
					classToFlush = classToFlush.getSuperclass();
				}
				while (classToFlush != null && classToFlush != Object.class);
			}

			if (logger.isTraceEnabled()) {
				logger.trace("Caching PropertyDescriptors for class [" + beanClass.getName() + "]");
			}
			this.propertyDescriptorCache = new LinkedHashMap<String, PropertyDescriptor>();

			// This call is slow so we do it once.
			PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				if (Class.class.equals(beanClass) &&
						("classLoader".equals(pd.getName()) ||  "protectionDomain".equals(pd.getName()))) {
					// Ignore Class.getClassLoader() and getProtectionDomain() methods - nobody needs to bind to those
					continue;
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Found bean property '" + pd.getName() + "'" +
							(pd.getPropertyType() != null ? " of type [" + pd.getPropertyType().getName() + "]" : "") +
							(pd.getPropertyEditorClass() != null ?
									"; editor [" + pd.getPropertyEditorClass().getName() + "]" : ""));
				}
				pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
				this.propertyDescriptorCache.put(pd.getName(), pd);
			}
		}
		catch (IntrospectionException ex) {
			throw new FatalBeanException("Failed to obtain BeanInfo for class [" + beanClass.getName() + "]", ex);
		}
	}

	BeanInfo getBeanInfo() {
		return this.beanInfo;
	}

	Class<?> getBeanClass() {
		return this.beanInfo.getBeanDescriptor().getBeanClass();
	}

	PropertyDescriptor getPropertyDescriptor(String name) {
		PropertyDescriptor pd = this.propertyDescriptorCache.get(name);
		if (pd == null && StringUtils.hasLength(name)) {
			// Same lenient fallback checking as in PropertyTypeDescriptor...
			pd = this.propertyDescriptorCache.get(name.substring(0, 1).toLowerCase() + name.substring(1));
			if (pd == null) {
				pd = this.propertyDescriptorCache.get(name.substring(0, 1).toUpperCase() + name.substring(1));
			}
		}
		return (pd == null || pd instanceof GenericTypeAwarePropertyDescriptor ? pd :
				buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
	}

	PropertyDescriptor[] getPropertyDescriptors() {
		PropertyDescriptor[] pds = new PropertyDescriptor[this.propertyDescriptorCache.size()];
		int i = 0;
		for (PropertyDescriptor pd : this.propertyDescriptorCache.values()) {
			pds[i] = (pd instanceof GenericTypeAwarePropertyDescriptor ? pd :
					buildGenericTypeAwarePropertyDescriptor(getBeanClass(), pd));
			i++;
		}
		return pds;
	}

	private PropertyDescriptor buildGenericTypeAwarePropertyDescriptor(Class<?> beanClass, PropertyDescriptor pd) {
		try {
			return new GenericTypeAwarePropertyDescriptor(beanClass, pd.getName(), pd.getReadMethod(),
					pd.getWriteMethod(), pd.getPropertyEditorClass());
		}
		catch (IntrospectionException ex) {
			throw new FatalBeanException("Failed to re-introspect class [" + beanClass.getName() + "]", ex);
		}
	}

}
