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

package org.springframework.jdbc.core.support;

import java.io.InputStream;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.jdbc.core.DisposableSqlTypeValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Object to represent an SQL BLOB/CLOB value parameter. BLOBs can either be an
 * InputStream or a byte array. CLOBs can be in the form of a Reader, InputStream
 * or String. Each CLOB/BLOB value will be stored together with its length.
 * The type is based on which constructor is used. Objects of this class are
 * immutable except for the LobCreator reference. Use them and discard them.
 * 
 * <p> 用于表示SQL BLOB / CLOB值参数的对象。 BLOB可以是InputStream或字节数组。 CLOB可以是Reader，
 * InputStream或String的形式。 每个CLOB / BLOB值将与其长度一起存储。 该类型基于使用哪个构造函数。 
 * 除了LobCreator引用之外，此类的对象是不可变的。 使用它们并丢弃它们。
 *
 * <p>This class holds a reference to a LocCreator that must be closed after the
 * update has completed. This is done via a call to the closeLobCreator method.
 * All handling of the LobCreator is done by the framework classes that use it -
 * no need to set or close the LobCreator for end users of this class.
 * 
 * <p> 此类包含对更新完成后必须关闭的LocCreator的引用。 这是通过调用closeLobCreator方法完成的。 
 * LobCreator的所有处理都是由使用它的框架类完成的 - 无需为此类的最终用户设置或关闭LobCreator。
 *
 * <p>A usage example:
 * 
 * <p> 一个用法示例：
 *
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // reusable object
 * LobHandler lobHandler = new DefaultLobHandler();  // reusable object
 *
 * jdbcTemplate.update(
 *     "INSERT INTO imagedb (image_name, content, description) VALUES (?, ?, ?)",
 *     new Object[] {
 *       name,
 *       new SqlLobValue(contentStream, contentLength, lobHandler),
 *       new SqlLobValue(description, lobHandler)
 *     },
 *     new int[] {Types.VARCHAR, Types.BLOB, Types.CLOB});
 * </pre>
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.jdbc.support.lob.LobHandler
 * @see org.springframework.jdbc.support.lob.LobCreator
 * @see org.springframework.jdbc.core.JdbcTemplate#update(String, Object[], int[])
 * @see org.springframework.jdbc.object.SqlUpdate#update(Object[])
 * @see org.springframework.jdbc.object.StoredProcedure#execute(java.util.Map)
 */
public class SqlLobValue implements DisposableSqlTypeValue {

	private final Object content;

	private final int length;

	/**
	 * This contains a reference to the LobCreator - so we can close it
	 * once the update is done.
	 * 
	 * <p> 这包含对LobCreator的引用 - 因此我们可以在更新完成后关闭它。
	 * 
	 */
	private final LobCreator lobCreator;


	/**
	 * Create a new BLOB value with the given byte array,
	 * using a DefaultLobHandler.
	 * 
	 * <p> 使用DefaultLobHandler使用给定的字节数组创建新的BLOB值。
	 * 
	 * @param bytes the byte array containing the BLOB value
	 * 
	 * <p> 包含BLOB值的字节数组
	 * 
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(byte[] bytes) {
		this(bytes, new DefaultLobHandler());
	}

	/**
	 * Create a new BLOB value with the given byte array.
	 * 
	 * <p> 使用给定的字节数组创建新的BLOB值。
	 * 
	 * @param bytes the byte array containing the BLOB value
	 * 
	 * <p> 包含BLOB值的字节数组
	 * 
	 * @param lobHandler the LobHandler to be used
	 * 
	 * <p> 要使用的LobHandler
	 */
	public SqlLobValue(byte[] bytes, LobHandler lobHandler) {
		this.content = bytes;
		this.length = (bytes != null ? bytes.length : 0);
		this.lobCreator = lobHandler.getLobCreator();
	}

	/**
	 * Create a new CLOB value with the given content string,
	 * using a DefaultLobHandler.
	 * @param content the String containing the CLOB value
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(String content) {
		this(content, new DefaultLobHandler());
	}

	/**
	 * Create a new CLOB value with the given content string.
	 * @param content the String containing the CLOB value
	 * @param lobHandler the LobHandler to be used
	 */
	public SqlLobValue(String content, LobHandler lobHandler) {
		this.content = content;
		this.length = (content != null ? content.length() : 0);
		this.lobCreator = lobHandler.getLobCreator();
	}

	/**
	 * Create a new BLOB/CLOB value with the given stream,
	 * using a DefaultLobHandler.
	 * @param stream the stream containing the LOB value
	 * @param length the length of the LOB value
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(InputStream stream, int length) {
		this(stream, length, new DefaultLobHandler());
	}

	/**
	 * Create a new BLOB/CLOB value with the given stream.
	 * @param stream the stream containing the LOB value
	 * @param length the length of the LOB value
	 * @param lobHandler the LobHandler to be used
	 */
	public SqlLobValue(InputStream stream, int length, LobHandler lobHandler) {
		this.content = stream;
		this.length = length;
		this.lobCreator = lobHandler.getLobCreator();
	}

	/**
	 * Create a new CLOB value with the given character stream,
	 * using a DefaultLobHandler.
	 * @param reader the character stream containing the CLOB value
	 * @param length the length of the CLOB value
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler
	 */
	public SqlLobValue(Reader reader, int length) {
		this(reader, length, new DefaultLobHandler());
	}

	/**
	 * Create a new CLOB value with the given character stream.
	 * @param reader the character stream containing the CLOB value
	 * @param length the length of the CLOB value
	 * @param lobHandler the LobHandler to be used
	 */
	public SqlLobValue(Reader reader, int length, LobHandler lobHandler) {
		this.content = reader;
		this.length = length;
		this.lobCreator = lobHandler.getLobCreator();
	}


	/**
	 * Set the specified content via the LobCreator.
	 */
	public void setTypeValue(PreparedStatement ps, int paramIndex, int sqlType, String typeName)
			throws SQLException {
		if (sqlType == Types.BLOB) {
			if (this.content instanceof byte[] || this.content == null) {
				this.lobCreator.setBlobAsBytes(ps, paramIndex, (byte[]) this.content);
			}
			else if (this.content instanceof String) {
				this.lobCreator.setBlobAsBytes(ps, paramIndex, ((String) this.content).getBytes());
			}
			else if (this.content instanceof InputStream) {
				this.lobCreator.setBlobAsBinaryStream(ps, paramIndex, (InputStream) this.content, this.length);
			}
			else {
				throw new IllegalArgumentException(
						"Content type [" + this.content.getClass().getName() + "] not supported for BLOB columns");
			}
		}
		else if (sqlType == Types.CLOB) {
			if (this.content instanceof String || this.content == null) {
				this.lobCreator.setClobAsString(ps, paramIndex, (String) this.content);
			}
			else if (this.content instanceof InputStream) {
				this.lobCreator.setClobAsAsciiStream(ps, paramIndex, (InputStream) this.content, this.length);
			}
			else if (this.content instanceof Reader) {
				this.lobCreator.setClobAsCharacterStream(ps, paramIndex, (Reader) this.content, this.length);
			}
			else {
				throw new IllegalArgumentException(
						"Content type [" + this.content.getClass().getName() + "] not supported for CLOB columns");
			}
		}
		else {
			throw new IllegalArgumentException("SqlLobValue only supports SQL types BLOB and CLOB");
		}
	}

	/**
	 * Close the LobCreator, if any.
	 */
	public void cleanup() {
		this.lobCreator.close();
	}

}
