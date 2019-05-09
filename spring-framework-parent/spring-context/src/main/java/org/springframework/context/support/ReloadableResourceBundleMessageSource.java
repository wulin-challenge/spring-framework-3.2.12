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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.StringUtils;

/**
 * Spring-specific {@link org.springframework.context.MessageSource} implementation
 * that accesses resource bundles using specified basenames, participating in the
 * Spring {@link org.springframework.context.ApplicationContext}'s resource loading.
 * 
 * <p> 特定于Spring的org.springframework.context.MessageSource实现，它使用指定的基本名称访问资源包，
 * 参与Spring org.springframework.context.ApplicationContext的资源加载。
 *
 * <p>In contrast to the JDK-based {@link ResourceBundleMessageSource}, this class uses
 * {@link java.util.Properties} instances as its custom data structure for messages,
 * loading them via a {@link org.springframework.util.PropertiesPersister} strategy
 * from Spring {@link Resource} handles. This strategy is not only capable of
 * reloading files based on timestamp changes, but also of loading properties files
 * with a specific character encoding. It will detect XML property files as well.
 * 
 * <p> 与基于JDK的ResourceBundleMessageSource相比，此类使用java.util.Properties实例作为消息的自定义数据结构，
 * 通过Spring资源句柄中的org.springframework.util.PropertiesPersister策略加载它们。
 * 此策略不仅能够根据时间戳更改重新加载文件，还能够加载具有特定字符编码的属性文件。它还将检测XML属性文件。
 *
 * <p>In contrast to {@link ResourceBundleMessageSource}, this class supports
 * reloading of properties files through the {@link #setCacheSeconds "cacheSeconds"}
 * setting, and also through programmatically clearing the properties cache.
 * Since application servers typically cache all files loaded from the classpath,
 * it is necessary to store resources somewhere else (for example, in the
 * "WEB-INF" directory of a web app). Otherwise changes of files in the
 * classpath will <i>not</i> be reflected in the application.
 * 
 * <p> 与ResourceBundleMessageSource相比，此类支持通过“cacheSeconds”设置重新加载属性文件，
 * 并通过编程方式清除属性缓存。由于应用程序服务器通常会缓存从类路径加载的所有文件，因此有必要将资源存储在其他位置
 * （例如，在Web应用程序的“WEB-INF”目录中）。否则，类路径中文件的更改将不会反映在应用程序中。
 *
 * <p>Note that the base names set as {@link #setBasenames "basenames"} property
 * are treated in a slightly different fashion than the "basenames" property of
 * {@link ResourceBundleMessageSource}. It follows the basic ResourceBundle rule of not
 * specifying file extension or language codes, but can refer to any Spring resource
 * location (instead of being restricted to classpath resources). With a "classpath:"
 * prefix, resources can still be loaded from the classpath, but "cacheSeconds" values
 * other than "-1" (caching forever) will not work in this case.
 * 
 * <p> 请注意，设置为“basenames”属性的基本名称的处理方式与ResourceBundleMessageSource的“basenames”属性
 * 略有不同。它遵循不指定文件扩展名或语言代码的基本ResourceBundle规则，但可以引用任何Spring资源位置（而不是仅限于类路径资源）。
 * 使用“classpath：”前缀，仍然可以从类路径加载资源，但在这种情况下，除“-1”（永久缓存）之外的“cacheSeconds”值将不起作用。
 *
 * <p>This MessageSource implementation is usually slightly faster than
 * {@link ResourceBundleMessageSource}, which builds on {@link java.util.ResourceBundle}
 * - in the default mode, i.e. when caching forever. With "cacheSeconds" set to 1,
 * message lookup takes about twice as long - with the benefit that changes in
 * individual properties files are detected with a maximum delay of 1 second.
 * Higher "cacheSeconds" values usually <i>do not</i> make a significant difference.
 * 
 * <p> 这个MessageSource实现通常比ResourceBundleMessageSource快一点，后者构建在
 * java.util.ResourceBundle上 - 在默认模式下，即永远缓存时。将“cacheSeconds”设置为1时，
 * 消息查找大约需要两倍的时间 - 这样可以检测到各个属性文件中的更改，最大延迟为1秒。
 * 较高的“cacheSeconds”值通常不会产生显着差异。
 *
 * <p>This MessageSource can easily be used outside of an
 * {@link org.springframework.context.ApplicationContext}: It will use a
 * {@link org.springframework.core.io.DefaultResourceLoader} as default,
 * simply getting overridden with the ApplicationContext's resource loader
 * if running in a context. It does not have any other specific dependencies.
 * 
 * <p> 这个MessageSource可以很容易地在org.springframework.context.ApplicationContext之外使用：
 * 它将使用org.springframework.core.io.DefaultResourceLoader作为默认值，如果在上下文中运行，
 * 只需使用ApplicationContext的资源加载器进行覆盖。它没有任何其他特定依赖项。
 *
 * <p>Thanks to Thomas Achleitner for providing the initial implementation of
 * this message source!
 * 
 * <p> 感谢Thomas Achleitner提供此消息源的初始实现！
 *
 * @author Juergen Hoeller
 * @see #setCacheSeconds
 * @see #setBasenames
 * @see #setDefaultEncoding
 * @see #setFileEncodings
 * @see #setPropertiesPersister
 * @see #setResourceLoader
 * @see org.springframework.util.DefaultPropertiesPersister
 * @see org.springframework.core.io.DefaultResourceLoader
 * @see ResourceBundleMessageSource
 * @see java.util.ResourceBundle
 */
public class ReloadableResourceBundleMessageSource extends AbstractMessageSource
		implements ResourceLoaderAware {

	private static final String PROPERTIES_SUFFIX = ".properties";

	private static final String XML_SUFFIX = ".xml";


	private String[] basenames = new String[0];

	private String defaultEncoding;

	private Properties fileEncodings;

	private boolean fallbackToSystemLocale = true;

	private long cacheMillis = -1;

	private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/** Cache to hold filename lists per Locale */
	/** 缓存以保存每个区域设置的文件名列表 */
	private final Map<String, Map<Locale, List<String>>> cachedFilenames =
			new HashMap<String, Map<Locale, List<String>>>();

	/** Cache to hold already loaded properties per filename */
	/** 缓存以保存每个文件名已加载的属性 */
	private final Map<String, PropertiesHolder> cachedProperties = new HashMap<String, PropertiesHolder>();

	/** Cache to hold merged loaded properties per locale */
	/** 缓存以保存每个区域设置的合并加载属性 */
	private final Map<Locale, PropertiesHolder> cachedMergedProperties = new HashMap<Locale, PropertiesHolder>();


	/**
	 * Set a single basename, following the basic ResourceBundle convention of
	 * not specifying file extension or language codes, but in contrast to
	 * {@link ResourceBundleMessageSource} referring to a Spring resource location:
	 * e.g. "WEB-INF/messages" for "WEB-INF/messages.properties",
	 * "WEB-INF/messages_en.properties", etc.
	 * 
	 * <p> 设置单个基本名称，遵循未指定文件扩展名或语言代码的基本ResourceBundle约定，
	 * 但与引用Spring资源位置的ResourceBundleMessageSource相反： 
	 * “WEB-INF / messages.properties”，“WEB-INF / messages_en.properties”等
	 * 的“WEB-INF / messages”。
	 * 
	 * <p>XML properties files are also supported: .g. "WEB-INF/messages" will find
	 * and load "WEB-INF/messages.xml", "WEB-INF/messages_en.xml", etc as well.
	 * 
	 * <p> 还支持XML属性文件：.g。 “WEB-INF / messages”也将查找并加载“WEB-INF / messages.xml”，“WEB-INF / messages_en.xml”等。
	 * 
	 * @param basename the single basename - 单个基名
	 * @see #setBasenames
	 * @see org.springframework.core.io.ResourceEditor
	 * @see java.util.ResourceBundle
	 */
	public void setBasename(String basename) {
		setBasenames(basename);
	}

	/**
	 * Set an array of basenames, each following the basic ResourceBundle convention
	 * of not specifying file extension or language codes, but in contrast to
	 * {@link ResourceBundleMessageSource} referring to a Spring resource location:
	 * e.g. "WEB-INF/messages" for "WEB-INF/messages.properties",
	 * "WEB-INF/messages_en.properties", etc.
	 * 
	 * <p> 设置一个基本名称数组，每个基本名称遵循不指定文件扩展名或语言代码的基本ResourceBundle约定，
	 * 但与引用Spring资源位置的ResourceBundleMessageSource相反： “WEB-INF / messages.properties”，
	 * “WEB-INF / messages_en.properties”等的“WEB-INF / messages”。
	 * 
	 * <p>XML properties files are also supported: .g. "WEB-INF/messages" will find
	 * and load "WEB-INF/messages.xml", "WEB-INF/messages_en.xml", etc as well.
	 * 
	 * <p> 还支持XML属性文件：.g。 “WEB-INF / messages”也将查找并加载“WEB-INF / messages.xml”，
	 * “WEB-INF / messages_en.xml”等。
	 * 
	 * <p>The associated resource bundles will be checked sequentially when resolving
	 * a message code. Note that message definitions in a <i>previous</i> resource
	 * bundle will override ones in a later bundle, due to the sequential lookup.
	 * 
	 * <p> 在解析消息代码时，将依次检查关联的资源包。 请注意，由于顺序查找，先前资源包中的消息定义将覆盖后一个包中的消息定义。
	 * 
	 * @param basenames an array of basenames - 多个基本名称
	 * @see #setBasename
	 * @see java.util.ResourceBundle
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
	 * Set the default charset to use for parsing properties files.
	 * Used if no file-specific charset is specified for a file.
	 * 
	 * <p> 设置用于解析属性文件的默认字符集。 如果没有为文件指定特定于文件的字符集，则使用此选项。
	 * 
	 * <p>Default is none, using the {@code java.util.Properties}
	 * default encoding: ISO-8859-1.
	 * 
	 * <p> 默认值为none，使用java.util.Properties默认编码：ISO-8859-1。
	 * 
	 * <p>Only applies to classic properties files, not to XML files.
	 * 
	 * <p> 仅适用于经典属性文件，而不适用于XML文件。
	 * 
	 * @param defaultEncoding the default charset
	 * @see #setFileEncodings
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Set per-file charsets to use for parsing properties files.
	 * 
	 * <p> 设置每个文件的字符集以用于解析属性文件。
	 * 
	 * <p>Only applies to classic properties files, not to XML files.
	 * 
	 * <p> 仅适用于经典属性文件，而不适用于XML文件。
	 * 
	 * @param fileEncodings Properties with filenames as keys and charset
	 * names as values. Filenames have to match the basename syntax,
	 * with optional locale-specific appendices: e.g. "WEB-INF/messages"
	 * or "WEB-INF/messages_en".
	 * 
	 * <p> 将文件名作为键，将字符集名称作为值的属性。 文件名必须与基本名称语法匹配，并具有可选的特定于语言环境的附录：
	 * 例如 “WEB-INF / messages”或“WEB-INF / messages_en”。
	 * 
	 * @see #setBasenames
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setFileEncodings(Properties fileEncodings) {
		this.fileEncodings = fileEncodings;
	}

	/**
	 * Set whether to fall back to the system Locale if no files for a specific
	 * Locale have been found. Default is "true"; if this is turned off, the only
	 * fallback will be the default file (e.g. "messages.properties" for
	 * basename "messages").
	 * 
	 * <p> 设置是否在找不到特定区域设置的文件时是否回退到系统区域设置。 默认为“true”; 如果关闭此选项，则唯一的回退将是默认文件
	 * （例如，对于basename“messages”，“messages.properties”）。
	 * 
	 * <p>Falling back to the system Locale is the default behavior of
	 * {@code java.util.ResourceBundle}. However, this is often not desirable
	 * in an application server environment, where the system Locale is not relevant
	 * to the application at all: Set this flag to "false" in such a scenario.
	 * 
	 * <p> 回退到系统Locale是java.util.ResourceBundle的默认行为。 但是，在应用程序服务器环境中，
	 * 这通常是不可取的，因为系统区域设置根本与应用程序无关：在这种情况下将此标志设置为“false”。
	 * 
	 */
	public void setFallbackToSystemLocale(boolean fallbackToSystemLocale) {
		this.fallbackToSystemLocale = fallbackToSystemLocale;
	}

	/**
	 * Set the number of seconds to cache loaded properties files.
	 * 
	 * <p> 设置缓存已加载属性文件的秒数。
	 * <ul>
	 * <li>Default is "-1", indicating to cache forever (just like
	 * {@code java.util.ResourceBundle}).
	 * 
	 * <li> 默认值为“-1”，表示永久缓存（就像java.util.ResourceBundle一样）。
	 * 
	 * <li>A positive number will cache loaded properties files for the given
	 * number of seconds. This is essentially the interval between refresh checks.
	 * Note that a refresh attempt will first check the last-modified timestamp
	 * of the file before actually reloading it; so if files don't change, this
	 * interval can be set rather low, as refresh attempts will not actually reload.
	 * 
	 * <li> 正数将在指定的秒数内缓存已加载的属性文件。 这实际上是刷新检查之间的间隔。 
	 * 请注意，刷新尝试将在实际重新加载之前首先检查文件的上次修改时间戳; 
	 * 因此，如果文件没有更改，则可以将此间隔设置得相当低，因为刷新尝试实际上不会重新加载。
	 * 
	 * <li>A value of "0" will check the last-modified timestamp of the file on
	 * every message access. <b>Do not use this in a production environment!</b>
	 * 
	 * <li> 值“0”将检查每次消息访问时文件的上次修改时间戳。 不要在生产环境中使用它！
	 * </ul>
	 */
	public void setCacheSeconds(int cacheSeconds) {
		this.cacheMillis = (cacheSeconds * 1000);
	}

	/**
	 * Set the PropertiesPersister to use for parsing properties files.
	 * 
	 * <p> 设置PropertiesPersister以用于解析属性文件。
	 * 
	 * <p>The default is a DefaultPropertiesPersister.
	 * 
	 * <p> 默认值为DefaultPropertiesPersister。
	 * 
	 * @see org.springframework.util.DefaultPropertiesPersister
	 */
	public void setPropertiesPersister(PropertiesPersister propertiesPersister) {
		this.propertiesPersister =
				(propertiesPersister != null ? propertiesPersister : new DefaultPropertiesPersister());
	}

	/**
	 * Set the ResourceLoader to use for loading bundle properties files.
	 * 
	 * <p> 设置ResourceLoader以用于加载包属性文件。
	 * 
	 * <p>The default is a DefaultResourceLoader. Will get overridden by the
	 * ApplicationContext if running in a context, as it implements the
	 * ResourceLoaderAware interface. Can be manually overridden when
	 * running outside of an ApplicationContext.
	 * 
	 * <p> 默认值为DefaultResourceLoader。 如果在上下文中运行，将被ApplicationContext覆盖，
	 * 因为它实现了ResourceLoaderAware接口。 在ApplicationContext外部运行时可以手动覆盖。
	 * 
	 * @see org.springframework.core.io.DefaultResourceLoader
	 * @see org.springframework.context.ResourceLoaderAware
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
	}


	/**
	 * Resolves the given message code as key in the retrieved bundle files,
	 * returning the value found in the bundle as-is (without MessageFormat parsing).
	 * 
	 * <p> 将给定的消息代码解析为检索到的捆绑包文件中的密钥，按原样返回捆绑包中找到的值（不使用MessageFormat解析）。
	 */
	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		if (this.cacheMillis < 0) {
			PropertiesHolder propHolder = getMergedProperties(locale);
			String result = propHolder.getProperty(code);
			if (result != null) {
				return result;
			}
		}
		else {
			for (String basename : this.basenames) {
				List<String> filenames = calculateAllFilenames(basename, locale);
				for (String filename : filenames) {
					PropertiesHolder propHolder = getProperties(filename);
					String result = propHolder.getProperty(code);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Resolves the given message code as key in the retrieved bundle files,
	 * using a cached MessageFormat instance per message code.
	 * 
	 * <p> 使用每个消息代码的缓存MessageFormat实例，将给定的消息代码解析为检索到的捆绑包文件中的密钥。
	 * 
	 */
	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		if (this.cacheMillis < 0) {
			PropertiesHolder propHolder = getMergedProperties(locale);
			MessageFormat result = propHolder.getMessageFormat(code, locale);
			if (result != null) {
				return result;
			}
		}
		else {
			for (String basename : this.basenames) {
				List<String> filenames = calculateAllFilenames(basename, locale);
				for (String filename : filenames) {
					PropertiesHolder propHolder = getProperties(filename);
					MessageFormat result = propHolder.getMessageFormat(code, locale);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}


	/**
	 * Get a PropertiesHolder that contains the actually visible properties
	 * for a Locale, after merging all specified resource bundles.
	 * Either fetches the holder from the cache or freshly loads it.
	 * 
	 * <p> 在合并所有指定的资源包之后，获取包含Locale的实际可见属性的PropertiesHolder。 从缓存中提取持有者或新加载它。
	 * 
	 * <p>Only used when caching resource bundle contents forever, i.e.
	 * with cacheSeconds < 0. Therefore, merged properties are always
	 * cached forever.
	 * 
	 * <p> 仅在永久缓存资源包内容时使用，即使用cacheSeconds <0。因此，合并的属性始终永远缓存。
	 * 
	 */
	protected PropertiesHolder getMergedProperties(Locale locale) {
		synchronized (this.cachedMergedProperties) {
			PropertiesHolder mergedHolder = this.cachedMergedProperties.get(locale);
			if (mergedHolder != null) {
				return mergedHolder;
			}
			Properties mergedProps = new Properties();
			mergedHolder = new PropertiesHolder(mergedProps, -1);
			for (int i = this.basenames.length - 1; i >= 0; i--) {
				List filenames = calculateAllFilenames(this.basenames[i], locale);
				for (int j = filenames.size() - 1; j >= 0; j--) {
					String filename = (String) filenames.get(j);
					PropertiesHolder propHolder = getProperties(filename);
					if (propHolder.getProperties() != null) {
						mergedProps.putAll(propHolder.getProperties());
					}
				}
			}
			this.cachedMergedProperties.put(locale, mergedHolder);
			return mergedHolder;
		}
	}

	/**
	 * Calculate all filenames for the given bundle basename and Locale.
	 * Will calculate filenames for the given Locale, the system Locale
	 * (if applicable), and the default file.
	 * 
	 * <p> 计算给定bundle basename和Locale的所有文件名。 
	 * 将计算给定区域设置，系统区域设置（如果适用）和默认文件的文件名。
	 * 
	 * @param basename the basename of the bundle - 捆绑的基本名称
	 * @param locale the locale - 语言环境
	 * @return the List of filenames to check - 要检查的文件名列表
	 * @see #setFallbackToSystemLocale
	 * @see #calculateFilenamesForLocale
	 */
	protected List<String> calculateAllFilenames(String basename, Locale locale) {
		synchronized (this.cachedFilenames) {
			Map<Locale, List<String>> localeMap = this.cachedFilenames.get(basename);
			if (localeMap != null) {
				List<String> filenames = localeMap.get(locale);
				if (filenames != null) {
					return filenames;
				}
			}
			List<String> filenames = new ArrayList<String>(7);
			filenames.addAll(calculateFilenamesForLocale(basename, locale));
			if (this.fallbackToSystemLocale && !locale.equals(Locale.getDefault())) {
				List<String> fallbackFilenames = calculateFilenamesForLocale(basename, Locale.getDefault());
				for (String fallbackFilename : fallbackFilenames) {
					if (!filenames.contains(fallbackFilename)) {
						// Entry for fallback locale that isn't already in filenames list.
						filenames.add(fallbackFilename);
					}
				}
			}
			filenames.add(basename);
			if (localeMap != null) {
				localeMap.put(locale, filenames);
			}
			else {
				localeMap = new HashMap<Locale, List<String>>();
				localeMap.put(locale, filenames);
				this.cachedFilenames.put(basename, localeMap);
			}
			return filenames;
		}
	}

	/**
	 * Calculate the filenames for the given bundle basename and Locale,
	 * appending language code, country code, and variant code.
	 * E.g.: basename "messages", Locale "de_AT_oo" -> "messages_de_AT_OO",
	 * "messages_de_AT", "messages_de".
	 * 
	 * <p> 计算给定包基本名称和区域设置的文件名，附加语言代码，国家/地区代码和变体代码。 
	 * 例如：basename“messages”，Locale“de_AT_oo” - >“messages_de_AT_OO”，“messages_de_AT”，“messages_de”。
	 * 
	 * <p>Follows the rules defined by {@link java.util.Locale#toString()}.
	 * 
	 * <p> 遵循java.util.Locale.toString（）定义的规则。
	 * 
	 * @param basename the basename of the bundle - 捆绑的基本名称
	 * @param locale the locale - 语言环境
	 * @return the List of filenames to check - 要检查的文件名列表
	 */
	protected List<String> calculateFilenamesForLocale(String basename, Locale locale) {
		List<String> result = new ArrayList<String>(3);
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		StringBuilder temp = new StringBuilder(basename);

		temp.append('_');
		if (language.length() > 0) {
			temp.append(language);
			result.add(0, temp.toString());
		}

		temp.append('_');
		if (country.length() > 0) {
			temp.append(country);
			result.add(0, temp.toString());
		}

		if (variant.length() > 0 && (language.length() > 0 || country.length() > 0)) {
			temp.append('_').append(variant);
			result.add(0, temp.toString());
		}

		return result;
	}


	/**
	 * Get a PropertiesHolder for the given filename, either from the
	 * cache or freshly loaded.
	 * 
	 * <p> 从缓存或新加载的文件获取给定文件名的PropertiesHolder。
	 * 
	 * @param filename the bundle filename (basename + Locale) - 包文件名（basename + Locale）
	 * @return the current PropertiesHolder for the bundle - 捆绑包的当前PropertiesHolder
	 */
	protected PropertiesHolder getProperties(String filename) {
		synchronized (this.cachedProperties) {
			PropertiesHolder propHolder = this.cachedProperties.get(filename);
			if (propHolder != null &&
					(propHolder.getRefreshTimestamp() < 0 ||
					 propHolder.getRefreshTimestamp() > System.currentTimeMillis() - this.cacheMillis)) {
				// up to date
				return propHolder;
			}
			return refreshProperties(filename, propHolder);
		}
	}

	/**
	 * Refresh the PropertiesHolder for the given bundle filename.
	 * The holder can be {@code null} if not cached before, or a timed-out cache entry
	 * (potentially getting re-validated against the current last-modified timestamp).
	 * 
	 * <p> 刷新给定包文件名的PropertiesHolder。 如果未在之前缓存，则持有者可以为null，
	 * 或者是超时缓存条目（可能会针对当前上次修改的时间戳重新进行验证）。
	 * 
	 * @param filename the bundle filename (basename + Locale) - 包文件名（basename + Locale）
	 * @param propHolder the current PropertiesHolder for the bundle - 捆绑包的当前PropertiesHolder
	 */
	protected PropertiesHolder refreshProperties(String filename, PropertiesHolder propHolder) {
		long refreshTimestamp = (this.cacheMillis < 0 ? -1 : System.currentTimeMillis());

		Resource resource = this.resourceLoader.getResource(filename + PROPERTIES_SUFFIX);
		if (!resource.exists()) {
			resource = this.resourceLoader.getResource(filename + XML_SUFFIX);
		}

		if (resource.exists()) {
			long fileTimestamp = -1;
			if (this.cacheMillis >= 0) {
				// Last-modified timestamp of file will just be read if caching with timeout.
				try {
					fileTimestamp = resource.lastModified();
					if (propHolder != null && propHolder.getFileTimestamp() == fileTimestamp) {
						if (logger.isDebugEnabled()) {
							logger.debug("Re-caching properties for filename [" + filename + "] - file hasn't been modified");
						}
						propHolder.setRefreshTimestamp(refreshTimestamp);
						return propHolder;
					}
				}
				catch (IOException ex) {
					// Probably a class path resource: cache it forever.
					if (logger.isDebugEnabled()) {
						logger.debug(
								resource + " could not be resolved in the file system - assuming that is hasn't changed", ex);
					}
					fileTimestamp = -1;
				}
			}
			try {
				Properties props = loadProperties(resource, filename);
				propHolder = new PropertiesHolder(props, fileTimestamp);
			}
			catch (IOException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Could not parse properties file [" + resource.getFilename() + "]", ex);
				}
				// Empty holder representing "not valid".
				propHolder = new PropertiesHolder();
			}
		}

		else {
			// Resource does not exist.
			if (logger.isDebugEnabled()) {
				logger.debug("No properties file found for [" + filename + "] - neither plain properties nor XML");
			}
			// Empty holder representing "not found".
			propHolder = new PropertiesHolder();
		}

		propHolder.setRefreshTimestamp(refreshTimestamp);
		this.cachedProperties.put(filename, propHolder);
		return propHolder;
	}

	/**
	 * Load the properties from the given resource.
	 * 
	 * <p> 从给定资源加载属性。
	 * 
	 * @param resource the resource to load from - 要加载的资源
	 * @param filename the original bundle filename (basename + Locale) - 原始包文件名（basename + Locale）
	 * @return the populated Properties instance - 填充的Properties实例
	 * @throws IOException if properties loading failed - 如果属性加载失败
	 */
	protected Properties loadProperties(Resource resource, String filename) throws IOException {
		InputStream is = resource.getInputStream();
		Properties props = new Properties();
		try {
			if (resource.getFilename().endsWith(XML_SUFFIX)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Loading properties [" + resource.getFilename() + "]");
				}
				this.propertiesPersister.loadFromXml(props, is);
			}
			else {
				String encoding = null;
				if (this.fileEncodings != null) {
					encoding = this.fileEncodings.getProperty(filename);
				}
				if (encoding == null) {
					encoding = this.defaultEncoding;
				}
				if (encoding != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Loading properties [" + resource.getFilename() + "] with encoding '" + encoding + "'");
					}
					this.propertiesPersister.load(props, new InputStreamReader(is, encoding));
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Loading properties [" + resource.getFilename() + "]");
					}
					this.propertiesPersister.load(props, is);
				}
			}
			return props;
		}
		finally {
			is.close();
		}
	}


	/**
	 * Clear the resource bundle cache.
	 * Subsequent resolve calls will lead to reloading of the properties files.
	 * 
	 * <p> 清除资源包缓存。 后续的解析调用将导致重新加载属性文件。
	 * 
	 */
	public void clearCache() {
		logger.debug("Clearing entire resource bundle cache");
		synchronized (this.cachedProperties) {
			this.cachedProperties.clear();
		}
		synchronized (this.cachedMergedProperties) {
			this.cachedMergedProperties.clear();
		}
	}

	/**
	 * Clear the resource bundle caches of this MessageSource and all its ancestors.
	 * 
	 * <p> 清除此MessageSource及其所有祖先的资源包缓存。
	 * 
	 * @see #clearCache
	 */
	public void clearCacheIncludingAncestors() {
		clearCache();
		if (getParentMessageSource() instanceof ReloadableResourceBundleMessageSource) {
			((ReloadableResourceBundleMessageSource) getParentMessageSource()).clearCacheIncludingAncestors();
		}
	}


	@Override
	public String toString() {
		return getClass().getName() + ": basenames=[" + StringUtils.arrayToCommaDelimitedString(this.basenames) + "]";
	}


	/**
	 * PropertiesHolder for caching.
	 * Stores the last-modified timestamp of the source file for efficient
	 * change detection, and the timestamp of the last refresh attempt
	 * (updated every time the cache entry gets re-validated).
	 * 
	 * <p> PropertiesHolder用于缓存。 存储源文件的上次修改时间戳以进行有效的更改检测，
	 * 以及上次刷新尝试的时间戳（每次重新验证缓存条目时更新）。
	 * 
	 */
	protected class PropertiesHolder {

		private Properties properties;

		private long fileTimestamp = -1;

		private long refreshTimestamp = -1;

		/** Cache to hold already generated MessageFormats per message code */
		/** 缓存以保存每个消息代码已生成的MessageFormats */
		private final Map<String, Map<Locale, MessageFormat>> cachedMessageFormats =
				new HashMap<String, Map<Locale, MessageFormat>>();

		public PropertiesHolder(Properties properties, long fileTimestamp) {
			this.properties = properties;
			this.fileTimestamp = fileTimestamp;
		}

		public PropertiesHolder() {
		}

		public Properties getProperties() {
			return properties;
		}

		public long getFileTimestamp() {
			return fileTimestamp;
		}

		public void setRefreshTimestamp(long refreshTimestamp) {
			this.refreshTimestamp = refreshTimestamp;
		}

		public long getRefreshTimestamp() {
			return refreshTimestamp;
		}

		public String getProperty(String code) {
			if (this.properties == null) {
				return null;
			}
			return this.properties.getProperty(code);
		}

		public MessageFormat getMessageFormat(String code, Locale locale) {
			if (this.properties == null) {
				return null;
			}
			synchronized (this.cachedMessageFormats) {
				Map<Locale, MessageFormat> localeMap = this.cachedMessageFormats.get(code);
				if (localeMap != null) {
					MessageFormat result = localeMap.get(locale);
					if (result != null) {
						return result;
					}
				}
				String msg = this.properties.getProperty(code);
				if (msg != null) {
					if (localeMap == null) {
						localeMap = new HashMap<Locale, MessageFormat>();
						this.cachedMessageFormats.put(code, localeMap);
					}
					MessageFormat result = createMessageFormat(msg, locale);
					localeMap.put(locale, result);
					return result;
				}
				return null;
			}
		}
	}

}
