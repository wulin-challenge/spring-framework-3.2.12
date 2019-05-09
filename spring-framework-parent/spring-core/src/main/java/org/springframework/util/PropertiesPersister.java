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

package org.springframework.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

/**
 * Strategy interface for persisting {@code java.util.Properties},
 * allowing for pluggable parsing strategies.
 * 
 * <p> 用于持久化java.util.Properties的策略接口，允许可插入的解析策略。
 *
 * <p>The default implementation is DefaultPropertiesPersister,
 * providing the native parsing of {@code java.util.Properties},
 * but allowing for reading from any Reader and writing to any Writer
 * (which allows to specify an encoding for a properties file).
 * 
 * <p> 默认实现是DefaultPropertiesPersister，提供java.util.Properties的本机解析，
 * 但允许从任何Reader读取并写入任何Writer（允许为属性文件指定编码）。
 *
 * <p>As of Spring 1.2.2, this interface also supports properties XML files,
 * through the {@code loadFromXml} and {@code storeToXml} methods.
 * The default implementations delegate to JDK 1.5's corresponding methods.
 * 
 * <p> 从Spring 1.2.2开始，该接口还通过loadFromXml和storeToXml方法支持属性XML文件。 默认实现委托给JDK 1.5的相应方法。
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see DefaultPropertiesPersister
 * @see java.util.Properties
 */
public interface PropertiesPersister {

	/**
	 * Load properties from the given InputStream into the given
	 * Properties object.
	 * 
	 * <p> 将给定InputStream的属性加载到给定的Properties对象中。
	 * 
	 * @param props the Properties object to load into
	 * 
	 * <p> 要加载的Properties对象
	 * 
	 * @param is the InputStream to load from
	 * 
	 * <p> 从中加载的InputStream
	 * 
	 * @throws IOException in case of I/O errors
	 * 
	 * <p> 在I / O错误的情况下
	 * 
	 * @see java.util.Properties#load
	 */
	void load(Properties props, InputStream is) throws IOException;

	/**
	 * Load properties from the given Reader into the given
	 * Properties object.
	 * 
	 * <p> 将给定Reader中的属性加载到给定的Properties对象中。
	 * 
	 * @param props the Properties object to load into
	 * 
	 * <p> 要加载的Properties对象
	 * 
	 * @param reader the Reader to load from
	 * 
	 * <p> 从中加载的Reader
	 * 
	 * @throws IOException in case of I/O errors
	 * 
	 * <p> 在I / O错误的情况下
	 * 
	 */
	void load(Properties props, Reader reader) throws IOException;


	/**
	 * Write the contents of the given Properties object to the
	 * given OutputStream.
	 * 
	 * <p> 将给定Properties对象的内容写入给定的OutputStream。
	 * 
	 * @param props the Properties object to store
	 * 
	 * <p> 要存储的Properties对象
	 * 
	 * @param os the OutputStream to write to
	 * 
	 * <p> 要写入的OutputStream
	 * 
	 * @param header the description of the property list
	 * 
	 * <p> 属性列表的描述
	 * 
	 * @throws IOException in case of I/O errors
	 * 
	 * <p> 在I / O错误的情况下
	 * 
	 * @see java.util.Properties#store
	 */
	void store(Properties props, OutputStream os, String header) throws IOException;

	/**
	 * Write the contents of the given Properties object to the
	 * given Writer.
	 * 
	 * <p> 将给定Properties对象的内容写入给定的Writer。
	 * 
	 * @param props the Properties object to store
	 * 
	 * <p> 要存储的Properties对象
	 * 
	 * @param writer the Writer to write to
	 * 
	 * <p> write的Writer
	 * 
	 * @param header the description of the property list
	 * 
	 * <p> 属性列表的描述
	 * 
	 * @throws IOException in case of I/O errors
	 * 
	 * <p> 在I / O错误的情况下
	 * 
	 */
	void store(Properties props, Writer writer, String header) throws IOException;


	/**
	 * Load properties from the given XML InputStream into the
	 * given Properties object.
	 * 
	 * <p> 将给定XML InputStream中的属性加载到给定的Properties对象中。
	 * 
	 * @param props the Properties object to load into
	 * 
	 * <p> 要加载的Properties对象
	 * 
	 * @param is the InputStream to load from
	 * 
	 * <p> 从中加载的InputStream
	 * 
	 * @throws IOException in case of I/O errors
	 * 
	 * <p> 在I / O错误的情况下
	 * 
	 * @see java.util.Properties#loadFromXML(java.io.InputStream)
	 */
	void loadFromXml(Properties props, InputStream is) throws IOException;

	/**
	 * Write the contents of the given Properties object to the
	 * given XML OutputStream.
	 * 
	 * <p> 将给定Properties对象的内容写入给定的XML OutputStream。
	 * 
	 * @param props the Properties object to store
	 * 
	 * <p> 要存储的Properties对象
	 * 
	 * @param os the OutputStream to write to
	 * 
	 * <p> 要写入的OutputStream
	 * 
	 * @param header the description of the property list
	 * 
	 * <p> 属性列表的描述
	 * 
	 * @throws IOException in case of I/O errors
	 * 
	 * <p> 在I / O错误的情况下
	 * 
	 * @see java.util.Properties#storeToXML(java.io.OutputStream, String)
	 */
	void storeToXml(Properties props, OutputStream os, String header) throws IOException;

	/**
	 * Write the contents of the given Properties object to the
	 * given XML OutputStream.
	 * 
	 * <p> 将给定Properties对象的内容写入给定的XML OutputStream。
	 * 
	 * @param props the Properties object to store
	 * 
	 * <p> 要存储的Properties对象
	 * 
	 * @param os the OutputStream to write to
	 * 
	 * <p> 要写入的OutputStream
	 * 
	 * @param encoding the encoding to use
	 * 
	 * <p> 要使用的编码
	 * 
	 * @param header the description of the property list
	 * 
	 * <p> 属性列表的描述
	 * 
	 * @throws IOException in case of I/O errors
	 * 
	 * <p> 在I / O错误的情况下
	 * 
	 * @see java.util.Properties#storeToXML(java.io.OutputStream, String, String)
	 */
	void storeToXml(Properties props, OutputStream os, String header, String encoding) throws IOException;

}
