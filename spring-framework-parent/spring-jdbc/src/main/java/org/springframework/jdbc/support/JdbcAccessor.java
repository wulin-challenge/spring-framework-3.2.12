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

package org.springframework.jdbc.support;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for {@link org.springframework.jdbc.core.JdbcTemplate} and
 * other JDBC-accessing DAO helpers, defining common properties such as
 * DataSource and exception translator.
 * 
 * <p> org.springframework.jdbc.core.JdbcTemplate和其他JDBC访问DAO帮助程序的基类，用于定义公共属性，
 * 如DataSource和异常转换程序。
 *
 * <p>Not intended to be used directly.
 * See {@link org.springframework.jdbc.core.JdbcTemplate}.
 * 
 * <p> 不打算直接使用。 请参阅org.springframework.jdbc.core.JdbcTemplate。
 *
 * @author Juergen Hoeller
 * @since 28.11.2003
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public abstract class JdbcAccessor implements InitializingBean {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private DataSource dataSource;

	private SQLExceptionTranslator exceptionTranslator;

	private boolean lazyInit = true;


	/**
	 * Set the JDBC DataSource to obtain connections from.
	 * 
	 * <p> 设置JDBC DataSource以从中获取连接。
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Return the DataSource used by this template.
	 * 
	 * <p> 返回此模板使用的DataSource。
	 */
	public DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * Specify the database product name for the DataSource that this accessor uses.
	 * This allows to initialize a SQLErrorCodeSQLExceptionTranslator without
	 * obtaining a Connection from the DataSource to get the metadata.
	 * 
	 * <p> 指定此访问者使用的DataSource的数据库产品名称。 这允许初始化SQLErrorCodeSQLExceptionTranslator而无需从
	 * DataSource获取Connection以获取元数据。
	 * 
	 * @param dbName the database product name that identifies the error codes entry
	 * 
	 * <p> 标识错误代码条目的数据库产品名称
	 * 
	 * @see SQLErrorCodeSQLExceptionTranslator#setDatabaseProductName
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 */
	public void setDatabaseProductName(String dbName) {
		this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dbName);
	}

	/**
	 * Set the exception translator for this instance.
	 * <p>If no custom translator is provided, a default
	 * {@link SQLErrorCodeSQLExceptionTranslator} is used
	 * which examines the SQLException's vendor-specific error code.
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
	 */
	public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
		this.exceptionTranslator = exceptionTranslator;
	}

	/**
	 * Return the exception translator for this instance.
	 * <p>Creates a default {@link SQLErrorCodeSQLExceptionTranslator}
	 * for the specified DataSource if none set, or a
	 * {@link SQLStateSQLExceptionTranslator} in case of no DataSource.
	 * @see #getDataSource()
	 */
	public synchronized SQLExceptionTranslator getExceptionTranslator() {
		if (this.exceptionTranslator == null) {
			DataSource dataSource = getDataSource();
			if (dataSource != null) {
				this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
			}
			else {
				this.exceptionTranslator = new SQLStateSQLExceptionTranslator();
			}
		}
		return this.exceptionTranslator;
	}

	/**
	 * Set whether to lazily initialize the SQLExceptionTranslator for this accessor,
	 * on first encounter of a SQLException. Default is "true"; can be switched to
	 * "false" for initialization on startup.
	 * <p>Early initialization just applies if {@code afterPropertiesSet()} is called.
	 * @see #getExceptionTranslator()
	 * @see #afterPropertiesSet()
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * Return whether to lazily initialize the SQLExceptionTranslator for this accessor.
	 * @see #getExceptionTranslator()
	 */
	public boolean isLazyInit() {
		return this.lazyInit;
	}

	/**
	 * Eagerly initialize the exception translator, if demanded,
	 * creating a default one for the specified DataSource if none set.
	 */
	public void afterPropertiesSet() {
		if (getDataSource() == null) {
			throw new IllegalArgumentException("Property 'dataSource' is required");
		}
		if (!isLazyInit()) {
			getExceptionTranslator();
		}
	}

}
