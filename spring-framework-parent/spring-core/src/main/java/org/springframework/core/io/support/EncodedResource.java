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

package org.springframework.core.io.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Holder that combines a {@link org.springframework.core.io.Resource}
 * with a specific encoding to be used for reading from the resource.
 * 
 * <p>将org.springframework.core.io.Resource与用于从资源中读取的特定编码组合在一起的Holder。
 *
 * <p>Used as argument for operations that support to read content with
 * a specific encoding (usually through a {@code java.io.Reader}.
 * 
 * <p>用作支持使用特定编码读取内容的操作的参数（通常通过java.io.Reader。
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see java.io.Reader
 */
public class EncodedResource {

	private final Resource resource;

	private String encoding;

	private Charset charset;


	/**
	 * Create a new EncodedResource for the given Resource,
	 * not specifying a specific encoding.
	 * 
	 * <p>为给定的资源创建新的EncodedResource，而不指定特定的编码。
	 * @param resource the Resource to hold - 要持有的资源
	 */
	public EncodedResource(Resource resource) {
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
	}

	/**
	 * Create a new EncodedResource for the given Resource,
	 * using the specified encoding.
	 * 
	 * <p>使用指定的编码为给定的资源创建新的EncodedResource。
	 * 
	 * @param resource the Resource to hold - 要持有的资源
	 * @param encoding the encoding to use for reading from the resource - 用于从资源中读取的编码
	 */
	public EncodedResource(Resource resource, String encoding) {
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
		this.encoding = encoding;
	}

	/**
	 * Create a new EncodedResource for the given Resource,
	 * using the specified encoding.
	 * 
	 * <p>使用指定的编码为给定的资源创建新的EncodedResource。
	 * 
	 * @param resource the Resource to hold - 要持有的资源
	 * @param charset the charset to use for reading from the resource - 用于从资源中读取的字符集
	 */
	public EncodedResource(Resource resource, Charset charset) {
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
		this.charset = charset;
	}


	/**
	 * Return the Resource held. - 返回持有的资源。
	 */
	public final Resource getResource() {
		return this.resource;
	}

	/**
	 * Return the encoding to use for reading from the resource,
	 * or {@code null} if none specified.
	 * 
	 * <p>返回用于从资源读取的编码，如果没有指定，则返回null。
	 */
	public final String getEncoding() {
		return this.encoding;
	}

	/**
	 * Return the charset to use for reading from the resource,
	 * or {@code null} if none specified.
	 * 
	 * <p>返回用于从资源读取的charset，如果没有指定，则返回null。
	 * 
	 */
	public final Charset getCharset() {
		return this.charset;
	}


	/**
	 * Determine whether a {@link Reader} is required as opposed to an {@link InputStream},
	 * i.e. whether an encoding or a charset has been specified.
	 * 
	 * <p>确定是否需要Reader而不是InputStream，即是否已指定编码或字符集。
	 * 
	 * @see #getReader()
	 * @see #getInputStream()
	 */
	public boolean requiresReader() {
		return (this.encoding != null || this.charset != null);
	}

	/**
	 * Open a {@code java.io.Reader} for the specified resource,
	 * using the specified encoding (if any).
	 * 
	 * <p>使用指定的编码（如果有）打开指定资源的java.io.Reader。
	 * 
	 * @throws IOException if opening the Reader failed - 如果打开阅读器失败
	 * @see #requiresReader()
	 */
	public Reader getReader() throws IOException {
		if (this.charset != null) {
			return new InputStreamReader(this.resource.getInputStream(), this.charset);
		}
		else if (this.encoding != null) {
			return new InputStreamReader(this.resource.getInputStream(), this.encoding);
		}
		else {
			return new InputStreamReader(this.resource.getInputStream());
		}
	}

	/**
	 * Open an {@code java.io.InputStream} for the specified resource,
	 * typically assuming that there is no specific encoding to use.
	 * 
	 * <p>打开指定资源的java.io.InputStream，通常假设没有要使用的特定编码。
	 * @throws IOException if opening the InputStream failed - 如果打开InputStream失败
	 * @see #requiresReader()
	 */
	public InputStream getInputStream() throws IOException {
		return this.resource.getInputStream();
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof EncodedResource) {
			EncodedResource otherRes = (EncodedResource) obj;
			return (this.resource.equals(otherRes.resource) &&
					ObjectUtils.nullSafeEquals(this.encoding, otherRes.encoding));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.resource.hashCode();
	}

	@Override
	public String toString() {
		return this.resource.toString();
	}

}
