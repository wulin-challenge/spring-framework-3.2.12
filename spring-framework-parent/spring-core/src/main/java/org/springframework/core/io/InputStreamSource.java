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

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple interface for objects that are sources for an {@link InputStream}.
 * 
 * <p>作为InputStream源的对象的简单接口。
 *
 * <p>This is the base interface for Spring's more extensive {@link Resource} interface.
 * 
 * <p>这是Spring更广泛的Resource接口的基本接口。
 *
 * <p>For single-use streams, {@link InputStreamResource} can be used for any
 * given {@code InputStream}. Spring's {@link ByteArrayResource} or any
 * file-based {@code Resource} implementation can be used as a concrete
 * instance, allowing one to read the underlying content stream multiple times.
 * This makes this interface useful as an abstract content source for mail
 * attachments, for example.
 * 
 * <p.对于一次性使用的流，InputStreamResource可用于任何给定的InputStream。 Spring的ByteArrayResource或任何基于文
 * 件的Resource实现都可以用作具体实例，允许用户多次读取底层内容流。 例如，这使得此接口可用作邮件附件的抽象内容源。
 * 
 *
 * @author Juergen Hoeller
 * @since 20.01.2004
 * @see java.io.InputStream
 * @see Resource
 * @see InputStreamResource
 * @see ByteArrayResource
 */
public interface InputStreamSource {

	/**
	 * Return an {@link InputStream}.
	 * 
	 * <p>返回一个InputStream。
	 * 
	 * <p>It is expected that each call creates a <i>fresh</i> stream.
	 * 
	 * <p>预计每次通话都会产生新的流。
	 * 
	 * <p>This requirement is particularly important when you consider an API such
	 * as JavaMail, which needs to be able to read the stream multiple times when
	 * creating mail attachments. For such a use case, it is <i>required</i>
	 * that each {@code getInputStream()} call returns a fresh stream.
	 * 
	 * <p>当您考虑诸如JavaMail之类的API时，此要求尤为重要，JavaMail需要能够在创建邮件附件时多次读取流。 对于这样的用例，
	 * 需要每个getInputStream（）调用返回一个新流。
	 * 
	 * @return the input stream for the underlying resource (must not be {@code null})
	 * 
	 * <p>底层资源的输入流（不得为null）
	 * 
	 * @throws IOException if the stream could not be opened - 如果流无法打开
	 * @see org.springframework.mail.javamail.MimeMessageHelper#addAttachment(String, InputStreamSource)
	 */
	InputStream getInputStream() throws IOException;

}
