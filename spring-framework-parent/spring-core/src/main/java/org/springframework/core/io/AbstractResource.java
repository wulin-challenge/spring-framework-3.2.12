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

package org.springframework.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.springframework.core.NestedIOException;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

/**
 * Convenience base class for {@link Resource} implementations,
 * pre-implementing typical behavior.
 * 
 * <p>资源实现的便捷基类，预先实现典型行为。
 *
 * <p>The "exists" method will check whether a File or InputStream can
 * be opened; "isOpen" will always return false; "getURL" and "getFile"
 * throw an exception; and "toString" will return the description.
 * 
 * <p>“exists”方法将检查是否可以打开File或InputStream; “isOpen”总是会返回false; 
 * “getURL”和“getFile”抛出异常; 和“toString”将返回描述。
 * 
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 */
public abstract class AbstractResource implements Resource {

	/**
	 * This implementation checks whether a File can be opened,
	 * falling back to whether an InputStream can be opened.
	 * This will cover both directories and content resources.
	 * 
	 * <p>此实现检查是否可以打开文件，然后回退到是否可以打开InputStream。 这将涵盖目录和内容资源。
	 * 
	 */
	public boolean exists() {
		// Try file existence: can we find the file in the file system?
		// 尝试文件存在：我们可以在文件系统中找到该文件吗？
		try {
			return getFile().exists();
		}
		catch (IOException ex) {
			// Fall back to stream existence: can we open the stream?
			// 回归流存在：我们可以打开流吗？
			try {
				InputStream is = getInputStream();
				is.close();
				return true;
			}
			catch (Throwable isEx) {
				return false;
			}
		}
	}

	/**
	 * This implementation always returns {@code true}.
	 * 
	 * <p> 此实现始终返回true。
	 */
	public boolean isReadable() {
		return true;
	}

	/**
	 * This implementation always returns {@code false}.
	 * 
	 * <p> 此实现始终返回false。
	 */
	public boolean isOpen() {
		return false;
	}

	/**
	 * This implementation throws a FileNotFoundException, assuming
	 * that the resource cannot be resolved to a URL.
	 * 
	 * <p>假设无法将资源解析为URL，此实现会抛出FileNotFoundException。
	 * 
	 */
	public URL getURL() throws IOException {
		throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
	}

	/**
	 * This implementation builds a URI based on the URL returned
	 * by {@link #getURL()}.
	 * 
	 * <p>此实现基于getURL（）返回的URL构建URI。
	 * 
	 */
	public URI getURI() throws IOException {
		URL url = getURL();
		try {
			return ResourceUtils.toURI(url);
		}
		catch (URISyntaxException ex) {
			throw new NestedIOException("Invalid URI [" + url + "]", ex);
		}
	}

	/**
	 * This implementation throws a FileNotFoundException, assuming
	 * that the resource cannot be resolved to an absolute file path.
	 * 
	 * <p>假设无法将资源解析为绝对文件路径，此实现会抛出FileNotFoundException。
	 * 
	 */
	public File getFile() throws IOException {
		throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
	}

	/**
	 * This implementation reads the entire InputStream to calculate the
	 * content length. Subclasses will almost always be able to provide
	 * a more optimal version of this, e.g. checking a File length.
	 * 
	 * <p>此实现读取整个InputStream以计算内容长度。 子类几乎总能提供更优化的版本，例如 检查文件长度。
	 * 
	 * @see #getInputStream()
	 * @throws IllegalStateException if {@link #getInputStream()} returns null. 
	 * 
	 * <p> 如果资源无法解析（在文件系统中或作为其他一些已知的物理资源类型）
	 */
	public long contentLength() throws IOException {
		InputStream is = this.getInputStream();
		Assert.state(is != null, "resource input stream must not be null");
		try {
			long size = 0;
			byte[] buf = new byte[255];
			int read;
			while ((read = is.read(buf)) != -1) {
				size += read;
			}
			return size;
		}
		finally {
			try {
				is.close();
			}
			catch (IOException ex) {
			}
		}
	}

	/**
	 * This implementation checks the timestamp of the underlying File,
	 * if available.
	 * 
	 * <p>此实现检查基础文件的时间戳（如果可用）。
	 * @see #getFileForLastModifiedCheck()
	 */
	public long lastModified() throws IOException {
		long lastModified = getFileForLastModifiedCheck().lastModified();
		if (lastModified == 0L) {
			throw new FileNotFoundException(getDescription() +
					" cannot be resolved in the file system for resolving its last-modified timestamp");
		}
		return lastModified;
	}

	/**
	 * Determine the File to use for timestamp checking.
	 * 
	 * <p>确定用于时间戳检查的文件。
	 * 
	 * <p>The default implementation delegates to {@link #getFile()}.
	 * 
	 * <p>默认实现委托给getFile（）。
	 * 
	 * @return the File to use for timestamp checking (never {@code null}) - 用于时间戳检查的文件（永不为null）
	 * @throws IOException if the resource cannot be resolved as absolute
	 * file path, i.e. if the resource is not available in a file system
	 * 
	 * <p>如果资源无法解析为绝对文件路径，即资源在文件系统中不可用
	 * 
	 */
	protected File getFileForLastModifiedCheck() throws IOException {
		return getFile();
	}

	/**
	 * This implementation throws a FileNotFoundException, assuming
	 * that relative resources cannot be created for this resource.
	 * 
	 * <p>假设无法为此资源创建相对资源，此实现将抛出FileNotFoundException。
	 * 
	 */
	public Resource createRelative(String relativePath) throws IOException {
		throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
	}

	/**
	 * This implementation always returns {@code null},
	 * assuming that this resource type does not have a filename.
	 * 
	 * <p>假设此资源类型没有文件名，此实现始终返回null。
	 * 
	 */
	public String getFilename() {
		return null;
	}


	/**
	 * This implementation returns the description of this resource.
	 * 
	 * <p>此实现返回此资源的描述。
	 * 
	 * @see #getDescription()
	 */
	@Override
	public String toString() {
		return getDescription();
	}

	/**
	 * This implementation compares description strings.
	 * 
	 * <p> 此实现比较描述字符串。
	 * 
	 * @see #getDescription()
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj == this ||
			(obj instanceof Resource && ((Resource) obj).getDescription().equals(getDescription())));
	}

	/**
	 * This implementation returns the description's hash code.
	 * 
	 * <p>此实现返回描述的哈希码。
	 * 
	 * @see #getDescription()
	 */
	@Override
	public int hashCode() {
		return getDescription().hashCode();
	}

}
