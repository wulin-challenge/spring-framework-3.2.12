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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.JdkVersion;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.context.MessageSource} implementation that
 * accesses resource bundles using specified basenames. This class relies
 * on the underlying JDK's {@link java.util.ResourceBundle} implementation,
 * in combination with the JDK's standard message parsing provided by
 * {@link java.text.MessageFormat}.
 * 
 * <p> org.springframework.context.MessageSource实现，使用指定的基本名称访问资源包。
 *  该类依赖于底层JDK的java.util.ResourceBundle实现，并结合了java.text.MessageFormat提供的JDK标准消息解析。
 *
 * <p>This MessageSource caches both the accessed ResourceBundle instances and
 * the generated MessageFormats for each message. It also implements rendering of
 * no-arg messages without MessageFormat, as supported by the AbstractMessageSource
 * base class. The caching provided by this MessageSource is significantly faster
 * than the built-in caching of the {@code java.util.ResourceBundle} class.
 * 
 * <p> 此MessageSource缓存每个消息的访问的ResourceBundle实例和生成的MessageFormats。 
 * 它还实现了没有MessageFormat的无arg消息的呈现，这是AbstractMessageSource基类所支持的。
 *  此MessageSource提供的缓存明显快于java.util.ResourceBundle类的内置缓存。
 *
 * <p>Unfortunately, {@code java.util.ResourceBundle} caches loaded bundles
 * forever: Reloading a bundle during VM execution is <i>not</i> possible.
 * As this MessageSource relies on ResourceBundle, it faces the same limitation.
 * Consider {@link ReloadableResourceBundleMessageSource} for an alternative
 * that is capable of refreshing the underlying bundle files.
 * 
 * <p> 不幸的是，java.util.ResourceBundle永远缓存加载的bundle：在VM执行期间重新加载bundle是不可能的。 
 * 由于此MessageSource依赖于ResourceBundle，因此它面临同样的限制。 
 * 考虑使用ReloadableResourceBundleMessageSource来替换能够刷新底层包文件的替代方案。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setBasenames
 * @see ReloadableResourceBundleMessageSource
 * @see java.util.ResourceBundle
 * @see java.text.MessageFormat
 */
public class ResourceBundleMessageSource extends AbstractMessageSource implements BeanClassLoaderAware {

	private String[] basenames = new String[0];

	private String defaultEncoding;

	private boolean fallbackToSystemLocale = true;

	private long cacheMillis = -1;

	private ClassLoader bundleClassLoader;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/**
	 * Cache to hold loaded ResourceBundles.
	 * This Map is keyed with the bundle basename, which holds a Map that is
	 * keyed with the Locale and in turn holds the ResourceBundle instances.
	 * This allows for very efficient hash lookups, significantly faster
	 * than the ResourceBundle class's own cache.
	 * 
	 * <p> 缓存以保存已加载的ResourceBundles。 此Map使用bundle basename进行键控，该basename保存一个使用Locale键入的Map，
	 * 然后保存ResourceBundle实例。 这允许非常有效的哈希查找，比ResourceBundle类自己的缓存快得多。
	 * 
	 */
	private final Map<String, Map<Locale, ResourceBundle>> cachedResourceBundles =
			new HashMap<String, Map<Locale, ResourceBundle>>();

	/**
	 * Cache to hold already generated MessageFormats.
	 * This Map is keyed with the ResourceBundle, which holds a Map that is
	 * keyed with the message code, which in turn holds a Map that is keyed
	 * with the Locale and holds the MessageFormat values. This allows for
	 * very efficient hash lookups without concatenated keys.
	 * 
	 * <p> 缓存以保存已生成的MessageFormats。 此Map使用ResourceBundle进行键控，
	 * ResourceBundle包含一个使用消息代码键入的Map，后者依次保存一个使用Locale键入的Map并保存MessageFormat值。 
	 * 这允许非常有效的散列查找而没有连接密钥
	 * 
	 * @see #getMessageFormat
	 */
	private final Map<ResourceBundle, Map<String, Map<Locale, MessageFormat>>> cachedBundleMessageFormats =
			new HashMap<ResourceBundle, Map<String, Map<Locale, MessageFormat>>>();


	/**
	 * Set a single basename, following {@link java.util.ResourceBundle} conventions:
	 * essentially, a fully-qualified classpath location. If it doesn't contain a
	 * package qualifier (such as {@code org.mypackage}), it will be resolved
	 * from the classpath root.
	 * 
	 * <p> 在java.util.ResourceBundle约定之后设置一个基本名称：实质上是一个完全限定的类路径位置。 
	 * 如果它不包含包限定符（例如org.mypackage），它将从类路径根解析。
	 * 
	 * <p>Messages will normally be held in the "/lib" or "/classes" directory of
	 * a web application's WAR structure. They can also be held in jar files on
	 * the class path.
	 * 
	 * <p> 消息通常保存在Web应用程序的WAR结构的“/ lib”或“/ classes”目录中。 它们也可以保存在类路径的jar文件中。
	 * 
	 * <p>Note that ResourceBundle names are effectively classpath locations: As a
	 * consequence, the JDK's standard ResourceBundle treats dots as package separators.
	 * This means that "test.theme" is effectively equivalent to "test/theme",
	 * just like it is for programmatic {@code java.util.ResourceBundle} usage.
	 * 
	 * <p> 请注意，ResourceBundle名称实际上是类路径位置：因此，JDK的标准ResourceBundle将点视为包分隔符。 
	 * 这意味着“test.theme”实际上等同于“test / theme”，就像程序化java.util.ResourceBundle用法一样。
	 * 
	 * @see #setBasenames
	 * @see java.util.ResourceBundle#getBundle(String)
	 */
	public void setBasename(String basename) {
		setBasenames(basename);
	}

	/**
	 * Set an array of basenames, each following {@link java.util.ResourceBundle}
	 * conventions: essentially, a fully-qualified classpath location. If it
	 * doesn't contain a package qualifier (such as {@code org.mypackage}),
	 * it will be resolved from the classpath root.
	 * 
	 * <p> 设置一个基本名数组，每个基本名都遵循java.util.ResourceBundle约定：本质上是一个完全限定的类路径位置。
	 *  如果它不包含包限定符（例如org.mypackage），它将从类路径根解析。
	 * 
	 * <p>The associated resource bundles will be checked sequentially
	 * when resolving a message code. Note that message definitions in a
	 * <i>previous</i> resource bundle will override ones in a later bundle,
	 * due to the sequential lookup.
	 * 
	 * <p> 在解析消息代码时，将依次检查关联的资源包。 请注意，由于顺序查找，先前资源包中的消息定义将覆盖后一个包中的消息定义。
	 * 
	 * <p>Note that ResourceBundle names are effectively classpath locations: As a
	 * consequence, the JDK's standard ResourceBundle treats dots as package separators.
	 * This means that "test.theme" is effectively equivalent to "test/theme",
	 * just like it is for programmatic {@code java.util.ResourceBundle} usage.
	 * 
	 * <p> 请注意，ResourceBundle名称实际上是类路径位置：因此，JDK的标准ResourceBundle将点视为包分隔符。 
	 * 这意味着“test.theme”实际上等同于“test / theme”，就像程序化java.util.ResourceBundle用法一样。
	 * 
	 * @see #setBasename
	 * @see java.util.ResourceBundle#getBundle(String)
	 */
	public void setBasenames(String... basenames) {
		if (basenames != null) {
			this.basenames = new String[basenames.length];
			for (int i = 0; i < basenames.length; i++) {
				String basename = basenames[i];
				Assert.hasText(basename, "Basename must not be empty");
				this.basenames[i] = basename.trim();
			}
		}
		else {
			this.basenames = new String[0];
		}
	}

	/**
	 * Set the default charset to use for parsing resource bundle files.
	 * 
	 * <p> 设置用于解析资源包文件的默认字符集。
	 * 
	 * <p>Default is none, using the {@code java.util.ResourceBundle}
	 * default encoding: ISO-8859-1.
	 * 
	 * <p> 默认值为none，使用java.util.ResourceBundle默认编码：ISO-8859-1。
	 * 
	 * <p><b>NOTE: Only works on JDK 1.6 and higher.</b> Consider using
	 * {@link ReloadableResourceBundleMessageSource} for JDK 1.5 support
	 * and more flexibility in setting of an encoding per file.
	 * 
	 * <p> 注意：仅适用于JDK 1.6及更高版本。 考虑使用ReloadableResourceBundleMessageSource
	 * 来支持JDK 1.5，并且可以更灵活地设置每个文件的编码。
	 * 
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Set whether to fall back to the system Locale if no files for a specific
	 * Locale have been found. Default is "true"; if this is turned off, the only
	 * fallback will be the default file (e.g. "messages.properties" for
	 * basename "messages").
	 * 
	 * <p> 设置是否在找不到特定区域设置的文件时是否回退到系统区域设置。 默认为“true”; 如果关闭此选项，
	 * 则唯一的回退将是默认文件（例如，对于basename“messages”，“messages.properties”）。
	 * 
	 * <p>Falling back to the system Locale is the default behavior of
	 * {@code java.util.ResourceBundle}. However, this is often not desirable
	 * in an application server environment, where the system Locale is not relevant
	 * to the application at all: Set this flag to "false" in such a scenario.
	 * 
	 * <p> 回退到系统Locale是java.util.ResourceBundle的默认行为。 
	 * 但是，在应用程序服务器环境中，这通常是不可取的，因为系统区域设置根本与应用程序无关：
	 * 在这种情况下将此标志设置为“false”。
	 * 
	 * <p><b>NOTE: Only works on JDK 1.6 and higher.</b> Consider using
	 * {@link ReloadableResourceBundleMessageSource} for JDK 1.5 support.
	 * 
	 * <p> 注意：仅适用于JDK 1.6及更高版本。 考虑使用ReloadableResourceBundleMessageSource来支持JDK 1.5。
	 * 
	 */
	public void setFallbackToSystemLocale(boolean fallbackToSystemLocale) {
		this.fallbackToSystemLocale = fallbackToSystemLocale;
	}

	/**
	 * Set the number of seconds to cache loaded resource bundle files.
	 * 
	 * <p> 设置缓存加载的资源包文件的秒数。
	 * 
	 * <ul>
	 * <li>Default is "-1", indicating to cache forever.
	 * 
	 * <li> 默认值为“-1”，表示永久缓存。
	 * 
	 * <li>A positive number will expire resource bundles after the given
	 * number of seconds. This is essentially the interval between refresh checks.
	 * Note that a refresh attempt will first check the last-modified timestamp
	 * of the file before actually reloading it; so if files don't change, this
	 * interval can be set rather low, as refresh attempts will not actually reload.
	 * 
	 * <li> 正数将在给定的秒数后使资源包到期。这实际上是刷新检查之间的间隔。
	 * 请注意，刷新尝试将在实际重新加载之前首先检查文件的上次修改时间戳;因此，如果文件没有更改，则可以将此间隔设置得相当低，
	 * 因为刷新尝试实际上不会重新加载。
	 * 
	 * <li>A value of "0" will check the last-modified timestamp of the file on
	 * every message access. <b>Do not use this in a production environment!</b>
	 * 
	 * <li> 值“0”将检查每次消息访问时文件的上次修改时间戳。不要在生产环境中使用它！
	 * 
	 * <li><b>Note that depending on your ClassLoader, expiration might not work reliably
	 * since the ClassLoader may hold on to a cached version of the bundle file.</b>
	 * Consider {@link ReloadableResourceBundleMessageSource} in combination
	 * with resource bundle files in a non-classpath location.
	 * 
	 * <li> 请注意，根据您的ClassLoader，过期可能无法可靠地工作，因为ClassLoader可能会保留捆绑文件的缓存版本。
	 * 将ReloadableResourceBundleMessageSource与非类路径位置中的资源包文件结合使用。
	 * 
	 * </ul>
	 * <p><b>NOTE: Only works on JDK 1.6 and higher.</b> Consider using
	 * {@link ReloadableResourceBundleMessageSource} for JDK 1.5 support
	 * and more flexibility in terms of the kinds of resources to load from
	 * (in particular from outside of the classpath where expiration works reliably).
	 * 
	 * <p> 注意：仅适用于JDK 1.6及更高版本。考虑使用ReloadableResourceBundleMessageSource来支持JDK 1.5，
	 * 并且在加载资源的类型方面具有更大的灵活性（特别是来自可靠到期的类路径外部）。
	 * 
	 */
	public void setCacheSeconds(int cacheSeconds) {
		this.cacheMillis = (cacheSeconds * 1000);
	}

	/**
	 * Set the ClassLoader to load resource bundles with.
	 * 
	 * <p> 设置ClassLoader以加载资源包。
	 * 
	 * <p>Default is the containing BeanFactory's
	 * {@link org.springframework.beans.factory.BeanClassLoaderAware bean ClassLoader},
	 * or the default ClassLoader determined by
	 * {@link org.springframework.util.ClassUtils#getDefaultClassLoader()}
	 * if not running within a BeanFactory.
	 * 
	 * <p> 默认是包含BeanFactory的bean ClassLoader，或者如果没有在BeanFactory中运行，
	 * 则由org.springframework.util.ClassUtils.getDefaultClassLoader（）确定的默认ClassLoader。
	 * 
	 */
	public void setBundleClassLoader(ClassLoader classLoader) {
		this.bundleClassLoader = classLoader;
	}

	/**
	 * Return the ClassLoader to load resource bundles with.
	 * 
	 * <p> 返回ClassLoader以加载资源包。
	 * 
	 * <p>Default is the containing BeanFactory's bean ClassLoader.
	 * 
	 * <p> 默认是包含BeanFactory的bean ClassLoader。
	 * 
	 * @see #setBundleClassLoader
	 */
	protected ClassLoader getBundleClassLoader() {
		return (this.bundleClassLoader != null ? this.bundleClassLoader : this.beanClassLoader);
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
	}


	/**
	 * Resolves the given message code as key in the registered resource bundles,
	 * returning the value found in the bundle as-is (without MessageFormat parsing).
	 * 
	 * <p> 将给定的消息代码解析为已注册资源包中的键，并按原样返回在包中找到的值（不使用MessageFormat解析）。
	 * 
	 */
	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		String result = null;
		for (int i = 0; result == null && i < this.basenames.length; i++) {
			ResourceBundle bundle = getResourceBundle(this.basenames[i], locale);
			if (bundle != null) {
				result = getStringOrNull(bundle, code);
			}
		}
		return result;
	}

	/**
	 * Resolves the given message code as key in the registered resource bundles,
	 * using a cached MessageFormat instance per message code.
	 * 
	 * <p> 使用每个消息代码的缓存MessageFormat实例，将给定的消息代码解析为已注册资源包中的密钥。
	 * 
	 */
	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		MessageFormat messageFormat = null;
		for (int i = 0; messageFormat == null && i < this.basenames.length; i++) {
			ResourceBundle bundle = getResourceBundle(this.basenames[i], locale);
			if (bundle != null) {
				messageFormat = getMessageFormat(bundle, code, locale);
			}
		}
		return messageFormat;
	}


	/**
	 * Return a ResourceBundle for the given basename and code,
	 * fetching already generated MessageFormats from the cache.
	 * 
	 * <p> 返回给定基本名称和代码的ResourceBundle，从缓存中获取已生成的MessageFormats。
	 * 
	 * @param basename the basename of the ResourceBundle - ResourceBundle的基本名称
	 * @param locale the Locale to find the ResourceBundle for - 用于查找ResourceBundle的Locale
	 * @return the resulting ResourceBundle, or {@code null} if none
	 * found for the given basename and Locale
	 * 
	 * <p> 生成的ResourceBundle，如果找不到给定的basename和Locale，则返回null
	 * 
	 */
	protected ResourceBundle getResourceBundle(String basename, Locale locale) {
		if (this.cacheMillis >= 0) {
			// Fresh ResourceBundle.getBundle call in order to let ResourceBundle
			// do its native caching, at the expense of more extensive lookup steps.
			
			// 新的ResourceBundle.getBundle调用是为了让ResourceBundle执行其本机缓存，代价是更广泛的查找步骤。
			return doGetBundle(basename, locale);
		}
		else {
			// Cache forever: prefer locale cache over repeated getBundle calls.
			
			// 永远缓存：更喜欢区域缓存而不是重复的getBundle调用。
			synchronized (this.cachedResourceBundles) {
				Map<Locale, ResourceBundle> localeMap = this.cachedResourceBundles.get(basename);
				if (localeMap != null) {
					ResourceBundle bundle = localeMap.get(locale);
					if (bundle != null) {
						return bundle;
					}
				}
				try {
					ResourceBundle bundle = doGetBundle(basename, locale);
					if (localeMap == null) {
						localeMap = new HashMap<Locale, ResourceBundle>();
						this.cachedResourceBundles.put(basename, localeMap);
					}
					localeMap.put(locale, bundle);
					return bundle;
				}
				catch (MissingResourceException ex) {
					if (logger.isWarnEnabled()) {
						logger.warn("ResourceBundle [" + basename + "] not found for MessageSource: " + ex.getMessage());
					}
					// Assume bundle not found
					// -> do NOT throw the exception to allow for checking parent message source.
					
					// 假设未找到包 - >不要抛出异常以允许检查父消息源。
					return null;
				}
			}
		}
	}

	/**
	 * Obtain the resource bundle for the given basename and Locale.
	 * 
	 * <p> 获取给定basename和Locale的资源包。
	 * 
	 * @param basename the basename to look for - 要查找的基本名称
	 * @param locale the Locale to look for - 要查找的区域设置
	 * @return the corresponding ResourceBundle - 相应的ResourceBundle
	 * @throws MissingResourceException if no matching bundle could be found - 如果找不到匹配的包
	 * @see java.util.ResourceBundle#getBundle(String, java.util.Locale, ClassLoader)
	 * @see #getBundleClassLoader()
	 */
	protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
		if ((this.defaultEncoding != null && !"ISO-8859-1".equals(this.defaultEncoding)) ||
				!this.fallbackToSystemLocale || this.cacheMillis >= 0) {
			// Custom Control required...
			if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_16) {
				throw new IllegalStateException("Cannot use 'defaultEncoding', 'fallbackToSystemLocale' and " +
						"'cacheSeconds' on the standard ResourceBundleMessageSource when running on Java 5. " +
						"Consider using ReloadableResourceBundleMessageSource instead.");
			}
			return new ControlBasedResourceBundleFactory().getBundle(basename, locale);
		}
		else {
			// Good old standard call...
			return ResourceBundle.getBundle(basename, locale, getBundleClassLoader());
		}
	}

	/**
	 * Return a MessageFormat for the given bundle and code,
	 * fetching already generated MessageFormats from the cache.
	 * 
	 * <p> 返回给定包和代码的MessageFormat，从缓存中获取已生成的MessageFormats。
	 * 
	 * @param bundle the ResourceBundle to work on - 要处理的ResourceBundle
	 * @param code the message code to retrieve - 要检索的消息代码
	 * @param locale the Locale to use to build the MessageFormat - 用于构建MessageFormat的Locale
	 * @return the resulting MessageFormat, or {@code null} if no message
	 * defined for the given code
	 * 
	 * <p> 生成的MessageFormat，如果没有为给定代码定义消息，则返回null
	 * 
	 * @throws MissingResourceException if thrown by the ResourceBundle - 如果被ResourceBundle抛出
	 */
	protected MessageFormat getMessageFormat(ResourceBundle bundle, String code, Locale locale)
			throws MissingResourceException {

		synchronized (this.cachedBundleMessageFormats) {
			Map<String, Map<Locale, MessageFormat>> codeMap = this.cachedBundleMessageFormats.get(bundle);
			Map<Locale, MessageFormat> localeMap = null;
			if (codeMap != null) {
				localeMap = codeMap.get(code);
				if (localeMap != null) {
					MessageFormat result = localeMap.get(locale);
					if (result != null) {
						return result;
					}
				}
			}

			String msg = getStringOrNull(bundle, code);
			if (msg != null) {
				if (codeMap == null) {
					codeMap = new HashMap<String, Map<Locale, MessageFormat>>();
					this.cachedBundleMessageFormats.put(bundle, codeMap);
				}
				if (localeMap == null) {
					localeMap = new HashMap<Locale, MessageFormat>();
					codeMap.put(code, localeMap);
				}
				MessageFormat result = createMessageFormat(msg, locale);
				localeMap.put(locale, result);
				return result;
			}

			return null;
		}
	}

	private String getStringOrNull(ResourceBundle bundle, String key) {
		try {
			return bundle.getString(key);
		}
		catch (MissingResourceException ex) {
			// Assume key not found
			// -> do NOT throw the exception to allow for checking parent message source.
			
			// 假设未找到密钥 - >不抛出异常以允许检查父消息源。
			return null;
		}
	}

	/**
	 * Show the configuration of this MessageSource.
	 * 
	 * <p> 显示此MessageSource的配置。
	 * 
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": basenames=[" +
				StringUtils.arrayToCommaDelimitedString(this.basenames) + "]";
	}


	/**
	 * Factory indirection for runtime isolation of the optional dependencv on
	 * Java 6's Control class.
	 * 
	 * <p> 工厂间接在Java 6的Control类上运行时隔离可选的dependencv。
	 * 
	 * @see ResourceBundle#getBundle(String, java.util.Locale, ClassLoader, java.util.ResourceBundle.Control)
	 * @see MessageSourceControl
	 */
	private class ControlBasedResourceBundleFactory {

		public ResourceBundle getBundle(String basename, Locale locale) {
			return ResourceBundle.getBundle(basename, locale, getBundleClassLoader(), new MessageSourceControl());
		}
	}


	/**
	 * Custom implementation of Java 6's {@code ResourceBundle.Control},
	 * adding support for custom file encodings, deactivating the fallback to the
	 * system locale and activating ResourceBundle's native cache, if desired.
	 * 
	 * <p> 自定义实现Java 6的ResourceBundle.Control，添加对自定义文件编码的支持，
	 * 停用系统区域设置的回退并激活ResourceBundle的本机缓存（如果需要）。
	 * 
	 */
	private class MessageSourceControl extends ResourceBundle.Control {

		@Override
		public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException {
			if (format.equals("java.properties")) {
				String bundleName = toBundleName(baseName, locale);
				final String resourceName = toResourceName(bundleName, "properties");
				final ClassLoader classLoader = loader;
				final boolean reloadFlag = reload;
				InputStream stream;
				try {
					stream = AccessController.doPrivileged(
							new PrivilegedExceptionAction<InputStream>() {
								public InputStream run() throws IOException {
									InputStream is = null;
									if (reloadFlag) {
										URL url = classLoader.getResource(resourceName);
										if (url != null) {
											URLConnection connection = url.openConnection();
											if (connection != null) {
												connection.setUseCaches(false);
												is = connection.getInputStream();
											}
										}
									}
									else {
										is = classLoader.getResourceAsStream(resourceName);
									}
									return is;
								}
							});
				}
				catch (PrivilegedActionException ex) {
					throw (IOException) ex.getException();
				}
				if (stream != null) {
					try {
						return (defaultEncoding != null ?
								new PropertyResourceBundle(new InputStreamReader(stream, defaultEncoding)) :
								new PropertyResourceBundle(stream));
					}
					finally {
						stream.close();
					}
				}
				else {
					return null;
				}
			}
			else {
				return super.newBundle(baseName, locale, format, loader, reload);
			}
		}

		@Override
		public Locale getFallbackLocale(String baseName, Locale locale) {
			return (fallbackToSystemLocale ? super.getFallbackLocale(baseName, locale) : null);
		}

		@Override
		public long getTimeToLive(String baseName, Locale locale) {
			return (cacheMillis >= 0 ? cacheMillis : super.getTimeToLive(baseName, locale));
		}

		@Override
		public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {
			if (super.needsReload(baseName, locale, format, loader, bundle, loadTime)) {
				cachedBundleMessageFormats.remove(bundle);
				return true;
			}
			else {
				return false;
			}
		}
	}

}
