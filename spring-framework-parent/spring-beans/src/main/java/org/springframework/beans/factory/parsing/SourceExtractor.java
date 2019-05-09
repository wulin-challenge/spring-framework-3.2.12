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

package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;

/**
 * Simple strategy allowing tools to control how source metadata is attached
 * to the bean definition metadata.
 * 
 * <p>简单的策略，允许工具控制源元数据如何附加到bean定义元数据。
 *
 * <p>Configuration parsers <strong>may</strong> provide the ability to attach
 * source metadata during the parse phase. They will offer this metadata in a
 * generic format which can be further modified by a {@link SourceExtractor}
 * before being attached to the bean definition metadata.
 * 
 * <p>配置解析器可以提供在解析阶段附加源元数据的能力。 它们将以通用格式提供此元数据，
 * 在附加到bean定义元数据之前，可以由SourceExtractor进一步修改。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.BeanMetadataElement#getSource()
 * @see org.springframework.beans.factory.config.BeanDefinition
 */
public interface SourceExtractor {

	/**
	 * Extract the source metadata from the candidate object supplied
	 * by the configuration parser.
	 * 
	 * <p>从配置解析器提供的候选对象中提取源元数据。
	 * 
	 * @param sourceCandidate the original source metadata (never {@code null})
	 * 
	 * <p>原始源元数据（永不为null）
	 * 
	 * @param definingResource the resource that defines the given source object
	 * (may be {@code null})
	 * 
	 * <p>定义给定源对象的资源（可以为null）
	 * 
	 * @return the source metadata object to store (may be {@code null})
	 * 
	 * <p>要存储的源元数据对象（可以为null）
	 * 
	 */
	Object extractSource(Object sourceCandidate, Resource definingResource);

}
