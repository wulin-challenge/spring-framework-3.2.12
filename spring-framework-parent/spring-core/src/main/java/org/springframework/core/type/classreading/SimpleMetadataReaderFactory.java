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

package org.springframework.core.type.classreading;

import java.io.IOException;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

/**
 * Simple implementation of the {@link MetadataReaderFactory} interface,
 * creating a new ASM {@link org.springframework.asm.ClassReader} for every request.
 * 
 * <p> 简单实现MetadataReaderFactory接口，为每个请求创建一个新的ASM org.springframework.asm.ClassReader。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class SimpleMetadataReaderFactory implements MetadataReaderFactory {

	private final ResourceLoader resourceLoader;


	/**
	 * Create a new SimpleMetadataReaderFactory for the default class loader.
	 * 
	 * <p> 为默认的类加载器创建一个新的SimpleMetadataReaderFactory。
	 * 
	 */
	public SimpleMetadataReaderFactory() {
		this.resourceLoader = new DefaultResourceLoader();
	}

	/**
	 * Create a new SimpleMetadataReaderFactory for the given resource loader.
	 * 
	 * <p> 为给定的资源加载器创建一个新的SimpleMetadataReaderFactory。
	 * 
	 * @param resourceLoader the Spring ResourceLoader to use
	 * (also determines the ClassLoader to use)
	 * 
	 * <p> 要使用的Spring ResourceLoader（也确定要使用的ClassLoader）
	 * 
	 */
	public SimpleMetadataReaderFactory(ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
	}

	/**
	 * Create a new SimpleMetadataReaderFactory for the given class loader.
	 * 
	 * <p> 为给定的类加载器创建一个新的SimpleMetadataReaderFactory。
	 * 
	 * @param classLoader the ClassLoader to use - 要使用的ClassLoader
	 */
	public SimpleMetadataReaderFactory(ClassLoader classLoader) {
		this.resourceLoader =
				(classLoader != null ? new DefaultResourceLoader(classLoader) : new DefaultResourceLoader());
	}


	/**
	 * Return the ResourceLoader that this MetadataReaderFactory has been
	 * constructed with.
	 * 
	 * <p> 返回已使用此MetadataReaderFactory构造的ResourceLoader。
	 */
	public final ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}


	public MetadataReader getMetadataReader(String className) throws IOException {
		String resourcePath = ResourceLoader.CLASSPATH_URL_PREFIX +
				ClassUtils.convertClassNameToResourcePath(className) + ClassUtils.CLASS_FILE_SUFFIX;
		Resource resource = this.resourceLoader.getResource(resourcePath);
		if (!resource.exists()) {
			// Maybe an inner class name using the dot name syntax? Need to use the dollar syntax here...
			// ClassUtils.forName has an equivalent check for resolution into Class references later on.
			int lastDotIndex = className.lastIndexOf('.');
			if (lastDotIndex != -1) {
				String innerClassName =
						className.substring(0, lastDotIndex) + '$' + className.substring(lastDotIndex + 1);
				String innerClassResourcePath = ResourceLoader.CLASSPATH_URL_PREFIX +
						ClassUtils.convertClassNameToResourcePath(innerClassName) + ClassUtils.CLASS_FILE_SUFFIX;
				Resource innerClassResource = this.resourceLoader.getResource(innerClassResourcePath);
				if (innerClassResource.exists()) {
					resource = innerClassResource;
				}
			}
		}
		return getMetadataReader(resource);
	}

	public MetadataReader getMetadataReader(Resource resource) throws IOException {
		return new SimpleMetadataReader(resource, this.resourceLoader.getClassLoader());
	}

}
