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

package org.springframework.web.servlet.view;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Convenient base class for {@link org.springframework.web.servlet.ViewResolver}
 * implementations. Caches {@link org.springframework.web.servlet.View} objects
 * once resolved: This means that view resolution won't be a performance problem,
 * no matter how costly initial view retrieval is.
 *
 * <p>Subclasses need to implement the {@link #loadView} template method,
 * building the View object for a specific view name and locale.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #loadView
 */
public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver {

	/** Default maximum number of entries for the view cache: 1024 */
	public static final int DEFAULT_CACHE_LIMIT = 1024;

	/** Dummy marker object for unresolved views in the cache Maps */
	private static final View UNRESOLVED_VIEW = new View() {
		public String getContentType() {
			return null;
		}
		public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
		}
	};


	/** The maximum number of entries in the cache */
	private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;

	/** Whether we should refrain from resolving views again if unresolved once */
	private boolean cacheUnresolved = true;

	/** Fast access cache for Views, returning already cached instances without a global lock */
	private final Map<Object, View> viewAccessCache = new ConcurrentHashMap<Object, View>(DEFAULT_CACHE_LIMIT);

	/** Map from view key to View instance, synchronized for View creation */
	@SuppressWarnings("serial")
	private final Map<Object, View> viewCreationCache =
			new LinkedHashMap<Object, View>(DEFAULT_CACHE_LIMIT, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<Object, View> eldest) {
					if (size() > getCacheLimit()) {
						viewAccessCache.remove(eldest.getKey());
						return true;
					}
					else {
						return false;
					}
				}
			};


	/**
	 * Specify the maximum number of entries for the view cache.
	 * Default is 1024.
	 */
	public void setCacheLimit(int cacheLimit) {
		this.cacheLimit = cacheLimit;
	}

	/**
	 * Return the maximum number of entries for the view cache.
	 */
	public int getCacheLimit() {
		return this.cacheLimit;
	}

	/**
	 * Enable or disable caching.
	 * <p>This is equivalent to setting the {@link #setCacheLimit "cacheLimit"}
	 * property to the default limit (1024) or to 0, respectively.
	 * <p>Default is "true": caching is enabled.
	 * Disable this only for debugging and development.
	 */
	public void setCache(boolean cache) {
		this.cacheLimit = (cache ? DEFAULT_CACHE_LIMIT : 0);
	}

	/**
	 * Return if caching is enabled.
	 */
	public boolean isCache() {
		return (this.cacheLimit > 0);
	}

	/**
	 * Whether a view name once resolved to {@code null} should be cached and
	 * automatically resolved to {@code null} subsequently.
	 * <p>Default is "true": unresolved view names are being cached, as of Spring 3.1.
	 * Note that this flag only applies if the general {@link #setCache "cache"}
	 * flag is kept at its default of "true" as well.
	 * <p>Of specific interest is the ability for some AbstractUrlBasedView
	 * implementations (FreeMarker, Velocity, Tiles) to check if an underlying
	 * resource exists via {@link AbstractUrlBasedView#checkResource(Locale)}.
	 * With this flag set to "false", an underlying resource that re-appears
	 * is noticed and used. With the flag set to "true", one check is made only.
	 */
	public void setCacheUnresolved(boolean cacheUnresolved) {
		this.cacheUnresolved = cacheUnresolved;
	}

	/**
	 * Return if caching of unresolved views is enabled.
	 */
	public boolean isCacheUnresolved() {
		return this.cacheUnresolved;
	}


	public View resolveViewName(String viewName, Locale locale) throws Exception {
		if (!isCache()) {
			return createView(viewName, locale);
		}
		else {
			Object cacheKey = getCacheKey(viewName, locale);
			View view = this.viewAccessCache.get(cacheKey);
			if (view == null) {
				synchronized (this.viewCreationCache) {
					view = this.viewCreationCache.get(cacheKey);
					if (view == null) {
						// Ask the subclass to create the View object.
						view = createView(viewName, locale);
						if (view == null && this.cacheUnresolved) {
							view = UNRESOLVED_VIEW;
						}
						if (view != null) {
							this.viewAccessCache.put(cacheKey, view);
							this.viewCreationCache.put(cacheKey, view);
							if (logger.isTraceEnabled()) {
								logger.trace("Cached view [" + cacheKey + "]");
							}
						}
					}
				}
			}
			return (view != UNRESOLVED_VIEW ? view : null);
		}
	}

	/**
	 * Return the cache key for the given view name and the given locale.
	 * <p>Default is a String consisting of view name and locale suffix.
	 * Can be overridden in subclasses.
	 * <p>Needs to respect the locale in general, as a different locale can
	 * lead to a different view resource.
	 */
	protected Object getCacheKey(String viewName, Locale locale) {
		return viewName + "_" + locale;
	}

	/**
	 * Provides functionality to clear the cache for a certain view.
	 * 
	 * <p> 提供清除特定视图的缓存的功能。
	 * 
	 * <p>This can be handy in case developer are able to modify views
	 * (e.g. Velocity templates) at runtime after which you'd need to
	 * clear the cache for the specified view.
	 * 
	 * <p> 如果开发人员能够在运行时修改视图（例如Velocity模板），之后您需要清除指定视图的缓存，这可以很方便。
	 * 
	 * @param viewName the view name for which the cached view object
	 * (if any) needs to be removed
	 * 
	 * <p> 需要删除缓存视图对象（如果有）的视图名称
	 * 
	 * @param locale the locale for which the view object should be removed
	 * 
	 * <p> 应删除视图对象的语言环境
	 */
	public void removeFromCache(String viewName, Locale locale) {
		if (!isCache()) {
			logger.warn("View caching is SWITCHED OFF -- removal not necessary");
		}
		else {
			Object cacheKey = getCacheKey(viewName, locale);
			Object cachedView;
			synchronized (this.viewCreationCache) {
				this.viewAccessCache.remove(cacheKey);
				cachedView = this.viewCreationCache.remove(cacheKey);
			}
			if (logger.isDebugEnabled()) {
				// Some debug output might be useful...
				if (cachedView == null) {
					logger.debug("No cached instance for view '" + cacheKey + "' was found");
				}
				else {
					logger.debug("Cache for view " + cacheKey + " has been cleared");
				}
			}
		}
	}

	/**
	 * Clear the entire view cache, removing all cached view objects.
	 * Subsequent resolve calls will lead to recreation of demanded view objects.
	 * 
	 * <p> 清除整个视图缓存，删除所有缓存的视图对象。 后续的解析调用将导致重新创建所需的视图对象。
	 */
	public void clearCache() {
		logger.debug("Clearing entire view cache");
		synchronized (this.viewCreationCache) {
			this.viewAccessCache.clear();
			this.viewCreationCache.clear();
		}
	}


	/**
	 * Create the actual View object.
	 * 
	 * <p> 创建实际的View对象。
	 * 
	 * <p>The default implementation delegates to {@link #loadView}.
	 * This can be overridden to resolve certain view names in a special fashion,
	 * before delegating to the actual {@code loadView} implementation
	 * provided by the subclass.
	 * 
	 * <p> 默认实现委托给loadView。 在委托子类提供的实际loadView实现之前，可以重写此方法以特殊方式解析某些视图名称。
	 * 
	 * @param viewName the name of the view to retrieve - 要检索的视图的名称
	 * @param locale the Locale to retrieve the view for - 用于检索视图的Locale
	 * @return the View instance, or {@code null} if not found
	 * (optional, to allow for ViewResolver chaining)
	 * 
	 * <p> View实例，如果未找到则为null（可选，允许ViewResolver链接）
	 * 
	 * @throws Exception if the view couldn't be resolved
	 * 
	 * <p> 如果视图无法解决
	 * 
	 * @see #loadView
	 */
	protected View createView(String viewName, Locale locale) throws Exception {
		return loadView(viewName, locale);
	}

	/**
	 * Subclasses must implement this method, building a View object
	 * for the specified view. The returned View objects will be
	 * cached by this ViewResolver base class.
	 * 
	 * <p> 子类必须实现此方法，为指定视图构建View对象。 返回的View对象将由此ViewResolver基类进行缓存。
	 * 
	 * <p>Subclasses are not forced to support internationalization:
	 * A subclass that does not may simply ignore the locale parameter.
	 * 
	 * <p> 子类不被强制支持国际化：不可以忽略locale参数的子类。
	 * 
	 * @param viewName the name of the view to retrieve - 要检索的视图的名称
	 * 
	 * @param locale the Locale to retrieve the view for - 用于检索视图的Locale
	 * 
	 * @return the View instance, or {@code null} if not found
	 * (optional, to allow for ViewResolver chaining)
	 * 
	 * <p> View实例，如果未找到则为null（可选，允许ViewResolver链接）
	 * 
	 * @throws Exception if the view couldn't be resolved
	 * 
	 * <p> 如果视图无法解决
	 * 
	 * @see #resolveViewName
	 */
	protected abstract View loadView(String viewName, Locale locale) throws Exception;

}
