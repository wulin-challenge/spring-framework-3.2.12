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

package org.springframework.core.io.support;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

/**
 * Base class for JavaBean-style components that need to load properties
 * from one or more resources. Supports local properties as well, with
 * configurable overriding.
 * 
 * <p> 需要从一个或多个资源加载属性的JavaBean样式组件的基类。 支持本地属性，具有可配置的覆盖。
 *
 * @author Juergen Hoeller
 * @since 1.2.2
 */
public abstract class PropertiesLoaderSupport {

	/** Logger available to subclasses */
	/** 记录器可用于子类 */
	protected final Log logger = LogFactory.getLog(getClass());

	protected Properties[] localProperties;

	protected boolean localOverride = false;

	private Resource[] locations;

	private boolean ignoreResourceNotFound = false;

	private String fileEncoding;

	private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();


	/**
	 * Set local properties, e.g. via the "props" tag in XML bean definitions.
	 * These can be considered defaults, to be overridden by properties
	 * loaded from files.
	 * 
	 * <p> 设置本地属性，例如 通过XML bean定义中的“props”标签。 这些可以被视为默认值，由文件加载的属性覆盖。
	 * 
	 */
	public void setProperties(Properties properties) {
		this.localProperties = new Properties[] {properties};
	}

	/**
	 * Set local properties, e.g. via the "props" tag in XML bean definitions,
	 * allowing for merging multiple properties sets into one.
	 * 
	 * <p> 设置本地属性，例如 通过XML bean定义中的“props”标签，允许将多个属性集合并为一个。
	 * 
	 */
	public void setPropertiesArray(Properties... propertiesArray) {
		this.localProperties = propertiesArray;
	}

	/**
	 * Set a location of a properties file to be loaded.
	 * 
	 * <p> 设置要加载的属性文件的位置。
	 * 
	 * <p>Can point to a classic properties file or to an XML file
	 * that follows JDK 1.5's properties XML format.
	 * 
	 * <p> 可以指向经典属性文件或遵循JDK 1.5属性XML格式的XML文件。
	 * 
	 */
	public void setLocation(Resource location) {
		this.locations = new Resource[] {location};
	}

	/**
	 * Set locations of properties files to be loaded.
	 * 
	 * <p> 设置要加载的属性文件的位置。
	 * 
	 * <p>Can point to classic properties files or to XML files
	 * that follow JDK 1.5's properties XML format.
	 * 
	 * <p> 可以指向经典属性文件或遵循JDK 1.5属性XML格式的XML文件。
	 * 
	 * <p>Note: Properties defined in later files will override
	 * properties defined earlier files, in case of overlapping keys.
	 * Hence, make sure that the most specific files are the last
	 * ones in the given list of locations.
	 * 
	 * <p> 注意：在重叠键的情况下，在以后的文件中定义的属性将覆盖先前定义的文件的属性。 因此，
	 * 请确保最具体的文件是给定位置列表中的最后一个文件。
	 */
	public void setLocations(Resource... locations) {
		this.locations = locations;
	}

	/**
	 * Set whether local properties override properties from files.
	 * 
	 * <p> 设置本地属性是否覆盖文件的属性。
	 * 
	 * <p>Default is "false": Properties from files override local defaults.
	 * Can be switched to "true" to let local properties override defaults
	 * from files.
	 * 
	 * <p> 默认值为“false”：文件中的属性覆盖本地默认值。 可以切换为“true”以使本地属性覆盖文件的默认值。
	 * 
	 */
	public void setLocalOverride(boolean localOverride) {
		this.localOverride = localOverride;
	}

	/**
	 * Set if failure to find the property resource should be ignored.
	 * 
	 * <p> 设置是否应忽略找不到属性资源的情况。
	 * 
	 * <p>"true" is appropriate if the properties file is completely optional.
	 * Default is "false".
	 * 
	 * <p> 如果属性文件是完全可选的，则“true”是合适的。 默认为“false”。
	 * 
	 */
	public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
		this.ignoreResourceNotFound = ignoreResourceNotFound;
	}

	/**
	 * Set the encoding to use for parsing properties files.
	 * 
	 * <p> 设置用于解析属性文件的编码。
	 * 
	 * <p>Default is none, using the {@code java.util.Properties}
	 * default encoding.
	 * 
	 * <p> 默认为none，使用java.util.Properties默认编码。
	 * 
	 * <p>Only applies to classic properties files, not to XML files.
	 * 
	 * <p> 仅适用于经典属性文件，而不适用于XML文件。
	 * 
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setFileEncoding(String encoding) {
		this.fileEncoding = encoding;
	}

	/**
	 * Set the PropertiesPersister to use for parsing properties files.
	 * The default is DefaultPropertiesPersister.
	 * 
	 * <p> 设置PropertiesPersister以用于解析属性文件。 默认值为DefaultPropertiesPersister。
	 * 
	 * @see org.springframework.util.DefaultPropertiesPersister
	 */
	public void setPropertiesPersister(PropertiesPersister propertiesPersister) {
		this.propertiesPersister =
				(propertiesPersister != null ? propertiesPersister : new DefaultPropertiesPersister());
	}


	/**
	 * Return a merged Properties instance containing both the
	 * loaded properties and properties set on this FactoryBean.
	 * 
	 * <p> 返回合并的Properties实例，该实例包含在此FactoryBean上设置的已加载属性和属性。
	 * 
	 */
	protected Properties mergeProperties() throws IOException {
		Properties result = new Properties();

		if (this.localOverride) {
			// Load properties from file upfront, to let local properties override.
			// 从文件前端加载属性，以使本地属性覆盖。
			loadProperties(result);
		}

		if (this.localProperties != null) {
			for (Properties localProp : this.localProperties) {
				CollectionUtils.mergePropertiesIntoMap(localProp, result);
			}
		}

		if (!this.localOverride) {
			// Load properties from file afterwards, to let those properties override.
			// 之后从文件加载属性，以覆盖这些属性。
			loadProperties(result);
		}

		return result;
	}

	/**
	 * Load properties into the given instance.
	 * 
	 * <p> 将属性加载到给定实例中。
	 * 
	 * @param props the Properties instance to load into
	 * 
	 * <p> 要加载的Properties实例
	 * 
	 * @throws IOException in case of I/O errors - 在I / O错误的情况下
	 * @see #setLocations
	 */
	protected void loadProperties(Properties props) throws IOException {
		if (this.locations != null) {
			for (Resource location : this.locations) {
				if (logger.isInfoEnabled()) {
					logger.info("Loading properties file from " + location);
				}
				try {
					PropertiesLoaderUtils.fillProperties(
							props, new EncodedResource(location, this.fileEncoding), this.propertiesPersister);
				}
				catch (IOException ex) {
					if (this.ignoreResourceNotFound) {
						if (logger.isWarnEnabled()) {
							logger.warn("Could not load properties from " + location + ": " + ex.getMessage());
						}
					}
					else {
						throw ex;
					}
				}
			}
		}
	}

}
