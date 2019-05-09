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

package org.springframework.core.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Interface for a resource descriptor that abstracts from the actual
 * type of underlying resource, such as a file or class path resource.
 * 
 * <p>从实际类型的底层资源（例如文件或类路径资源）中抽象出来的资源描述符的接口。
 *
 * <p>An InputStream can be opened for every resource if it exists in
 * physical form, but a URL or File handle can just be returned for
 * certain resources. The actual behavior is implementation-specific.
 * 
 * <p>如果InputStream以物理形式存在，则可以为每个资源打开它，但只能为某些资源返回URL或File句柄。 实际行为是特定于实现的。
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see #getInputStream()
 * @see #getURL()
 * @see #getURI()
 * @see #getFile()
 * @see WritableResource
 * @see ContextResource
 * @see FileSystemResource
 * @see ClassPathResource
 * @see UrlResource
 * @see ByteArrayResource
 * @see InputStreamResource
 */
public interface Resource extends InputStreamSource {

	/**
	 * Return whether this resource actually exists in physical form.
	 * 
	 * <p>返回此资源是否实际以物理形式存在。
	 * 
	 * <p>This method performs a definitive existence check, whereas the
	 * existence of a {@code Resource} handle only guarantees a
	 * valid descriptor handle.
	 * 
	 * <p>此方法执行确定的存在性检查，而Resource句柄的存在仅保证有效的描述符句柄。
	 */
	boolean exists();

	/**
	 * Return whether the contents of this resource can be read,
	 * e.g. via {@link #getInputStream()} or {@link #getFile()}.
	 * 
	 * <p>返回是否可以读取此资源的内容，例如 通过getInputStream（）或getFile（）。
	 * 
	 * <p>Will be {@code true} for typical resource descriptors;
	 * note that actual content reading may still fail when attempted.
	 * However, a value of {@code false} is a definitive indication
	 * that the resource content cannot be read.
	 * 
	 * <p>对于典型的资源描述符将是如此; 请注意，尝试时实际内容读取可能仍然失败。 但是，值false是无法读取资源内容的明确指示。
	 * @see #getInputStream()
	 */
	boolean isReadable();

	/**
	 * Return whether this resource represents a handle with an open
	 * stream. If true, the InputStream cannot be read multiple times,
	 * and must be read and closed to avoid resource leaks.
	 * 
	 * <p>返回此资源是否表示具有开放流的句柄。 如果为true，则无法多次读取InputStream，必须读取并关闭它以避免资源泄漏。
	 * 
	 * <p>Will be {@code false} for typical resource descriptors. - 对于典型的资源描述符将是错误的。
	 */
	boolean isOpen();

	/**
	 * Return a URL handle for this resource. - 返回此资源的URI句柄。
	 * @throws IOException if the resource cannot be resolved as URL,
	 * i.e. if the resource is not available as descriptor
	 * 
	 * <p>如果资源无法解析为URL，即资源不可用作描述符
	 */
	URL getURL() throws IOException;

	/**
	 * Return a URI handle for this resource. - 返回此资源的URI句柄。
	 * @throws IOException if the resource cannot be resolved as URI,
	 * i.e. if the resource is not available as descriptor
	 * 
	 * <p>如果资源不能解析为URI，即资源不可用作描述符
	 * 
	 */
	URI getURI() throws IOException;

	/**
	 * Return a File handle for this resource. - 返回此资源的File句柄。
	 * @throws IOException if the resource cannot be resolved as absolute
	 * file path, i.e. if the resource is not available in a file systemf
	 * 
	 * <p>如果资源无法解析为绝对文件路径，即资源在文件系统中不可用
	 */
	File getFile() throws IOException;

	/**
	 * Determine the content length for this resource. - 确定此资源的内容长度。
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 * 
	 * <p>如果资源无法解析（在文件系统中或作为其他一些已知的物理资源类型）
	 */
	long contentLength() throws IOException;

	/**
	 * Determine the last-modified timestamp for this resource. - 确定此资源的上次修改时间戳。
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 * 
	 * <p>如果资源无法解析（在文件系统中或作为其他一些已知的物理资源类型）
	 */
	long lastModified() throws IOException;

	/**
	 * Create a resource relative to this resource. - 创建相对于此资源的资源。
	 * @param relativePath the relative path (relative to this resource) - 相对路径（相对于此资源）
	 * @return the resource handle for the relative resource - 相关资源的资源句柄
	 * @throws IOException if the relative resource cannot be determined - 如果相关资源无法确定
	 */
	Resource createRelative(String relativePath) throws IOException;

	/**
	 * Determine a filename for this resource, i.e. typically the last
	 * part of the path: for example, "myfile.txt".
	 * 
	 * <p>确定此资源的文件名，即通常是路径的最后部分：例如，“myfile.txt”。
	 * 
	 * <p>Returns {@code null} if this type of resource does not
	 * have a filename. - 如果此类资源没有文件名，则返回null。
	 */
	String getFilename();

	/**
	 * Return a description for this resource,
	 * to be used for error output when working with the resource.
	 * 
	 * <p>返回此资源的描述，用于处理资源时的错误输出。
	 * 
	 * <p>Implementations are also encouraged to return this value
	 * from their {@code toString} method.
	 * 
	 * <p>还鼓励实现从其toString方法返回此值。
	 * 
	 * @see Object#toString()
	 */
	String getDescription();

}
