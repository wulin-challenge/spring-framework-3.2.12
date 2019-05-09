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

package org.springframework.context.event;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.OrderComparator;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Abstract implementation of the {@link ApplicationEventMulticaster} interface,
 * providing the basic listener registration facility.
 * 
 * <p> ApplicationEventMulticaster接口的抽象实现，提供基本的监听器注册工具。
 *
 * <p>Doesn't permit multiple instances of the same listener by default,
 * as it keeps listeners in a linked Set. The collection class used to hold
 * ApplicationListener objects can be overridden through the "collectionClass"
 * bean property.
 * 
 * <p> 默认情况下不允许同一侦听器的多个实例，因为它会将侦听器保留在链接的Set中。 用于保存
 * ApplicationListener对象的集合类可以通过“collectionClass”bean属性重写。
 *
 * <p>Implementing ApplicationEventMulticaster's actual {@link #multicastEvent} method
 * is left to subclasses. {@link SimpleApplicationEventMulticaster} simply multicasts
 * all events to all registered listeners, invoking them in the calling thread.
 * Alternative implementations could be more sophisticated in those respects.
 * 
 * <p> 实现ApplicationEventMulticaster的实际multicastEvent方法留给了子类。 
 * SimpleApplicationEventMulticaster简单地将所有事件多播到所有已注册的侦听器，并在调用线程中调用它们。 
 * 在这些方面，替代实现可能更复杂。
 *
 * @author Juergen Hoeller
 * @since 1.2.3
 * @see #getApplicationListeners(ApplicationEvent)
 * @see SimpleApplicationEventMulticaster
 */
public abstract class AbstractApplicationEventMulticaster
		implements ApplicationEventMulticaster, BeanClassLoaderAware, BeanFactoryAware {

	private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);

	private final Map<ListenerCacheKey, ListenerRetriever> retrieverCache =
			new ConcurrentHashMap<ListenerCacheKey, ListenerRetriever>(64);

	private ClassLoader beanClassLoader;

	private BeanFactory beanFactory;


	public void addApplicationListener(ApplicationListener listener) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.add(listener);
			this.retrieverCache.clear();
		}
	}

	public void addApplicationListenerBean(String listenerBeanName) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.add(listenerBeanName);
			this.retrieverCache.clear();
		}
	}

	public void removeApplicationListener(ApplicationListener listener) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.remove(listener);
			this.retrieverCache.clear();
		}
	}

	public void removeApplicationListenerBean(String listenerBeanName) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.remove(listenerBeanName);
			this.retrieverCache.clear();
		}
	}

	public void removeAllListeners() {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.clear();
			this.defaultRetriever.applicationListenerBeans.clear();
			this.retrieverCache.clear();
		}
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		if (this.beanClassLoader == null && beanFactory instanceof ConfigurableBeanFactory) {
			this.beanClassLoader = ((ConfigurableBeanFactory) beanFactory).getBeanClassLoader();
		}
	}

	private BeanFactory getBeanFactory() {
		if (this.beanFactory == null) {
			throw new IllegalStateException("ApplicationEventMulticaster cannot retrieve listener beans " +
					"because it is not associated with a BeanFactory");
		}
		return this.beanFactory;
	}


	/**
	 * Return a Collection containing all ApplicationListeners.
	 * 
	 * <p> 返回包含所有ApplicationListeners的Collection。
	 * 
	 * @return a Collection of ApplicationListeners - ApplicationListeners的集合
	 * @see org.springframework.context.ApplicationListener
	 */
	protected Collection<ApplicationListener> getApplicationListeners() {
		synchronized (this.defaultRetriever) {
			return this.defaultRetriever.getApplicationListeners();
		}
	}

	/**
	 * Return a Collection of ApplicationListeners matching the given
	 * event type. Non-matching listeners get excluded early.
	 * 
	 * <p> 返回与给定事件类型匹配的ApplicationListeners集合。 不匹配的听众会尽早被排除在外。
	 * 
	 * @param event the event to be propagated. Allows for excluding
	 * non-matching listeners early, based on cached matching information.
	 * 
	 * <p> 要传播的事件。 允许根据缓存的匹配信息尽早排除不匹配的侦听器。
	 * 
	 * @return a Collection of ApplicationListeners - ApplicationListeners的集合
	 * @see org.springframework.context.ApplicationListener
	 */
	protected Collection<ApplicationListener> getApplicationListeners(ApplicationEvent event) {
		Class<? extends ApplicationEvent> eventType = event.getClass();
		Object source = event.getSource();
		Class<?> sourceType = (source != null ? source.getClass() : null);
		ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);
		ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
		if (retriever != null) {
			return retriever.getApplicationListeners();
		}
		else {
			retriever = new ListenerRetriever(true);
			LinkedList<ApplicationListener> allListeners = new LinkedList<ApplicationListener>();
			Set<ApplicationListener> listeners;
			Set<String> listenerBeans;
			synchronized (this.defaultRetriever) {
				listeners = new LinkedHashSet<ApplicationListener>(this.defaultRetriever.applicationListeners);
				listenerBeans = new LinkedHashSet<String>(this.defaultRetriever.applicationListenerBeans);
			}
			for (ApplicationListener listener : listeners) {
				if (supportsEvent(listener, eventType, sourceType)) {
					retriever.applicationListeners.add(listener);
					allListeners.add(listener);
				}
			}
			if (!listenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for (String listenerBeanName : listenerBeans) {
					ApplicationListener listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
					if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
						retriever.applicationListenerBeans.add(listenerBeanName);
						allListeners.add(listener);
					}
				}
			}
			OrderComparator.sort(allListeners);
			if (this.beanClassLoader == null ||
					(ClassUtils.isCacheSafe(eventType, this.beanClassLoader) &&
							(sourceType == null || ClassUtils.isCacheSafe(sourceType, this.beanClassLoader)))) {
				this.retrieverCache.put(cacheKey, retriever);
			}
			return allListeners;
		}
	}

	/**
	 * Determine whether the given listener supports the given event.
	 * 
	 * <p> 确定给定侦听器是否支持给定事件。
	 * 
	 * <p>The default implementation detects the {@link SmartApplicationListener}
	 * interface. In case of a standard {@link ApplicationListener}, a
	 * {@link GenericApplicationListenerAdapter} will be used to introspect
	 * the generically declared type of the target listener.
	 * 
	 * <p> 默认实现检测SmartApplicationListener接口。 对于标准ApplicationListener，
	 * 将使用GenericApplicationListenerAdapter来内省目标侦听器的一般声明类型。
	 * 
	 * @param listener the target listener to check - 要检查的目标侦听器
	 * @param eventType the event type to check against - 要检查的事件类型
	 * @param sourceType the source type to check against - 要检查的源类型
	 * @return whether the given listener should be included in the candidates
	 * for the given event type
	 * 
	 * <p> 是否应将给定的侦听器包含在给定事件类型的候选者中
	 * 
	 */
	protected boolean supportsEvent(
			ApplicationListener listener, Class<? extends ApplicationEvent> eventType, Class sourceType) {

		SmartApplicationListener smartListener = (listener instanceof SmartApplicationListener ?
				(SmartApplicationListener) listener : new GenericApplicationListenerAdapter(listener));
		return (smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType));
	}


	/**
	 * Cache key for ListenerRetrievers, based on event type and source type.
	 * 
	 * <p> ListenerRetrievers的缓存键，基于事件类型和源类型。
	 * 
	 */
	private static class ListenerCacheKey {

		private final Class<?> eventType;

		private final Class<?> sourceType;

		public ListenerCacheKey(Class<?> eventType, Class<?> sourceType) {
			this.eventType = eventType;
			this.sourceType = sourceType;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			ListenerCacheKey otherKey = (ListenerCacheKey) other;
			return ObjectUtils.nullSafeEquals(this.eventType, otherKey.eventType) &&
					ObjectUtils.nullSafeEquals(this.sourceType, otherKey.sourceType);
		}

		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(this.eventType) * 29 + ObjectUtils.nullSafeHashCode(this.sourceType);
		}
	}


	/**
	 * Helper class that encapsulates a specific set of target listeners,
	 * allowing for efficient retrieval of pre-filtered listeners.
	 * 
	 * <p> Helper类，它封装了一组特定的目标侦听器，允许有效检索预过滤的侦听器。
	 * 
	 * <p>An instance of this helper gets cached per event type and source type.
	 * 
	 * <p> 每个事件类型和源类型都会缓存此帮助程序的实例。
	 * 
	 */
	private class ListenerRetriever {

		public final Set<ApplicationListener> applicationListeners;

		public final Set<String> applicationListenerBeans;

		private final boolean preFiltered;

		public ListenerRetriever(boolean preFiltered) {
			this.applicationListeners = new LinkedHashSet<ApplicationListener>();
			this.applicationListenerBeans = new LinkedHashSet<String>();
			this.preFiltered = preFiltered;
		}

		public Collection<ApplicationListener> getApplicationListeners() {
			LinkedList<ApplicationListener> allListeners = new LinkedList<ApplicationListener>();
			for (ApplicationListener listener : this.applicationListeners) {
				allListeners.add(listener);
			}
			if (!this.applicationListenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for (String listenerBeanName : this.applicationListenerBeans) {
					ApplicationListener listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
					if (this.preFiltered || !allListeners.contains(listener)) {
						allListeners.add(listener);
					}
				}
			}
			OrderComparator.sort(allListeners);
			return allListeners;
		}
	}

}
