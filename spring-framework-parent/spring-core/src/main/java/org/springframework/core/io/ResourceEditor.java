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

package org.springframework.core.io;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link java.beans.PropertyEditor Editor} for {@link Resource}
 * descriptors, to automatically convert {@code String} locations
 * e.g. {@code file:C:/myfile.txt} or {@code classpath:myfile.txt} to
 * {@code Resource} properties instead of using a {@code String} location property.
 * 
 * <p> 资源描述符的编辑器，用于自动转换字符串位置，
 * 例如 file：C：/myfile.txt或classpath：myfile.txt到Resource属性，而不是使用String location属性。
 *
 * <p>The path may contain {@code ${...}} placeholders, to be
 * resolved as {@link org.springframework.core.env.Environment} properties:
 * e.g. {@code ${user.dir}}. Unresolvable placeholders are ignored by default.
 * 
 * <p> 该路径可能包含$ {...}}占位符，需要解析为
 * org.springframework.core.env.Environment属性：例如$ {user.dir来}}。 默认情况下会忽略无法解析的占位符。
 *
 * <p>Delegates to a {@link ResourceLoader} to do the heavy lifting,
 * by default using a {@link DefaultResourceLoader}.
 * 
 * <p> 默认情况下，使用DefaultResourceLoader委派ResourceLoader进行繁重的工作。
 *
 * @author Juergen Hoeller
 * @author Dave Syer
 * @author Chris Beams
 * @since 28.12.2003
 * @see Resource
 * @see ResourceLoader
 * @see DefaultResourceLoader
 * @see PropertyResolver#resolvePlaceholders
 */
public class ResourceEditor extends PropertyEditorSupport {

	private final ResourceLoader resourceLoader;

	private PropertyResolver propertyResolver;

	private final boolean ignoreUnresolvablePlaceholders;


	/**
	 * Create a new instance of the {@link ResourceEditor} class
	 * using a {@link DefaultResourceLoader} and {@link StandardEnvironment}.
	 */
	public ResourceEditor() {
		this(new DefaultResourceLoader(), null);
	}

	/**
	 * Create a new instance of the {@link ResourceEditor} class
	 * using the given {@link ResourceLoader} and a {@link StandardEnvironment}.
	 * @param resourceLoader the {@code ResourceLoader} to use
	 * @deprecated as of Spring 3.1 in favor of
	 * {@link #ResourceEditor(ResourceLoader, PropertyResolver)}
	 */
	@Deprecated
	public ResourceEditor(ResourceLoader resourceLoader) {
		this(resourceLoader, null, true);
	}

	/**
	 * Create a new instance of the {@link ResourceEditor} class
	 * using the given {@link ResourceLoader}.
	 * @param resourceLoader the {@code ResourceLoader} to use
	 * @param ignoreUnresolvablePlaceholders whether to ignore unresolvable placeholders
	 * if no corresponding property could be found
	 * @deprecated as of Spring 3.1 in favor of
	 * {@link #ResourceEditor(ResourceLoader, PropertyResolver, boolean)}
	 */
	@Deprecated
	public ResourceEditor(ResourceLoader resourceLoader, boolean ignoreUnresolvablePlaceholders) {
		this(resourceLoader, null, ignoreUnresolvablePlaceholders);
	}

	/**
	 * Create a new instance of the {@link ResourceEditor} class
	 * using the given {@link ResourceLoader} and {@link PropertyResolver}.
	 * 
	 * <p> 使用给定的ResourceLoader和PropertyResolver创建ResourceEditor类的新实例。
	 * 
	 * @param resourceLoader the {@code ResourceLoader} to use - 要使用的{@code ResourceLoader}
	 * @param propertyResolver the {@code PropertyResolver} to use - 要使用的{@code PropertyResolver}
	 */
	public ResourceEditor(ResourceLoader resourceLoader, PropertyResolver propertyResolver) {
		this(resourceLoader, propertyResolver, true);
	}

	/**
	 * Create a new instance of the {@link ResourceEditor} class
	 * using the given {@link ResourceLoader}.
	 * 
	 * <p> 使用给定的ResourceLoader创建ResourceEditor类的新实例。
	 * 
	 * @param resourceLoader the {@code ResourceLoader} to use - 要使用的{@code ResourceLoader}
	 * @param propertyResolver the {@code PropertyResolver} to use - 要使用的{@code PropertyResolver}
	 * 
	 * @param ignoreUnresolvablePlaceholders whether to ignore unresolvable placeholders
	 * if no corresponding property could be found in the given {@code propertyResolver}
	 * 
	 * <p> 如果在给定的propertyResolver中找不到相应的属性，是否忽略不可解析的占位符
	 */
	public ResourceEditor(ResourceLoader resourceLoader, PropertyResolver propertyResolver, boolean ignoreUnresolvablePlaceholders) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		this.resourceLoader = resourceLoader;
		this.propertyResolver = propertyResolver;
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}


	@Override
	public void setAsText(String text) {
		if (StringUtils.hasText(text)) {
			String locationToUse = resolvePath(text).trim();
			setValue(this.resourceLoader.getResource(locationToUse));
		}
		else {
			setValue(null);
		}
	}

	/**
	 * Resolve the given path, replacing placeholders with corresponding
	 * property values from the {@code environment} if necessary.
	 * @param path the original file path
	 * @return the resolved file path
	 * @see PropertyResolver#resolvePlaceholders
	 * @see PropertyResolver#resolveRequiredPlaceholders
	 */
	protected String resolvePath(String path) {
		if (this.propertyResolver == null) {
			this.propertyResolver = new StandardEnvironment();
		}
		return (this.ignoreUnresolvablePlaceholders ? this.propertyResolver.resolvePlaceholders(path) :
				this.propertyResolver.resolveRequiredPlaceholders(path));
	}


	@Override
	public String getAsText() {
		Resource value = (Resource) getValue();
		try {
			// Try to determine URL for resource.
			return (value != null ? value.getURL().toExternalForm() : "");
		}
		catch (IOException ex) {
			// Couldn't determine resource URL - return null to indicate
			// that there is no appropriate text representation.
			return null;
		}
	}

}
