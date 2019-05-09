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

package org.springframework.beans.factory.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * EntityResolver implementation that tries to resolve entity references
 * through a {@link org.springframework.core.io.ResourceLoader} (usually,
 * relative to the resource base of an ApplicationContext), if applicable.
 * Extends {@link DelegatingEntityResolver} to also provide DTD and XSD lookup.
 * 
 * <p>尝试通过org.springframework.core.io.ResourceLoader解析实体引用的EntityResolver实现
 * （通常，相对于ApplicationContext的资源库）（如果适用）。 扩展DelegatingEntityResolver以提供DTD和XSD查找。
 *
 * <p>Allows to use standard XML entities to include XML snippets into an
 * application context definition, for example to split a large XML file
 * into various modules. The include paths can be relative to the
 * application context's resource base as usual, instead of relative
 * to the JVM working directory (the XML parser's default).
 * 
 * <p>允许使用标准XML实体将XML片段包含到应用程序上下文定义中，例如将大型XML文件拆分为各种模块。 
 * include路径可以像往常一样相对于应用程序上下文的资源库，而不是相对于JVM工作目录（XML解析器的默认值）。
 *
 * <p>Note: In addition to relative paths, every URL that specifies a
 * file in the current system root, i.e. the JVM working directory,
 * will be interpreted relative to the application context too.
 * 
 * <p>注意：除了相对路径之外，在当前系统根目录中指定文件的每个URL（即JVM工作目录）也将相对于应用程序上下文进行解释。
 *
 * @author Juergen Hoeller
 * @since 31.07.2003
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.context.ApplicationContext
 */
public class ResourceEntityResolver extends DelegatingEntityResolver {

	private static final Log logger = LogFactory.getLog(ResourceEntityResolver.class);

	private final ResourceLoader resourceLoader;


	/**
	 * Create a ResourceEntityResolver for the specified ResourceLoader
	 * (usually, an ApplicationContext).
	 * 
	 * <p>为指定的ResourceLoader创建ResourceEntityResolver（通常是ApplicationContext）。
	 * 
	 * @param resourceLoader the ResourceLoader (or ApplicationContext)
	 * to load XML entity includes with
	 * 
	 * <p>用于加载XML实体的ResourceLoader（或ApplicationContext）包括
	 */
	public ResourceEntityResolver(ResourceLoader resourceLoader) {
		super(resourceLoader.getClassLoader());
		this.resourceLoader = resourceLoader;
	}


	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		InputSource source = super.resolveEntity(publicId, systemId);
		if (source == null && systemId != null) {
			String resourcePath = null;
			try {
				String decodedSystemId = URLDecoder.decode(systemId);
				String givenUrl = new URL(decodedSystemId).toString();
				String systemRootUrl = new File("").toURI().toURL().toString();
				// Try relative to resource base if currently in system root.
				if (givenUrl.startsWith(systemRootUrl)) {
					resourcePath = givenUrl.substring(systemRootUrl.length());
				}
			}
			catch (Exception ex) {
				// Typically a MalformedURLException or AccessControlException.
				if (logger.isDebugEnabled()) {
					logger.debug("Could not resolve XML entity [" + systemId + "] against system root URL", ex);
				}
				// No URL (or no resolvable URL) -> try relative to resource base.
				resourcePath = systemId;
			}
			if (resourcePath != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Trying to locate XML entity [" + systemId + "] as resource [" + resourcePath + "]");
				}
				Resource resource = this.resourceLoader.getResource(resourcePath);
				source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				if (logger.isDebugEnabled()) {
					logger.debug("Found XML entity [" + systemId + "]: " + resource);
				}
			}
		}
		return source;
	}

}
