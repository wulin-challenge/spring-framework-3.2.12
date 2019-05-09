/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.core.type.filter;

import java.io.IOException;

import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * Base interface for type filters using a
 * {@link org.springframework.core.type.classreading.MetadataReader}.
 * 
 * <p> 使用org.springframework.core.type.classreading.MetadataReader的类型过滤器的基本接口。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 */
public interface TypeFilter {

	/**
	 * Determine whether this filter matches for the class described by
	 * the given metadata.
	 * 
	 * <p> 确定此筛选器是否与给定元数据描述的类匹配。
	 * 
	 * @param metadataReader the metadata reader for the target class
	 * 
	 * <p> 目标类的元数据读取器
	 * 
	 * @param metadataReaderFactory a factory for obtaining metadata readers
	 * for other classes (such as superclasses and interfaces)
	 * 
	 * <p> 用于获取其他类（例如超类和接口）的元数据读取器的工厂
	 * 
	 * @return whether this filter matches
	 * 
	 * <p> 此过滤器是否匹配
	 * 
	 * @throws IOException in case of I/O failure when reading metadata
	 * 
	 * <p> 在读取元数据时I / O失败的情况下
	 */
	boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
			throws IOException;

}
