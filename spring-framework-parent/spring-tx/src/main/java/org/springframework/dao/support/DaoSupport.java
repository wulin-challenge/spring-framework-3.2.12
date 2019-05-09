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

package org.springframework.dao.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

/**
 * Generic base class for DAOs, defining template methods for DAO initialization.
 * 
 * <p> DAO的通用基类，定义DAO初始化的模板方法。
 *
 * <p>Extended by Spring's specific DAO support classes, such as:
 * JdbcDaoSupport, JdoDaoSupport, etc.
 * 
 * <p> 由Spring的特定DAO支持类扩展，例如：JdbcDaoSupport，JdoDaoSupport等。
 *
 * @author Juergen Hoeller
 * @since 1.2.2
 * @see org.springframework.jdbc.core.support.JdbcDaoSupport
 * @see org.springframework.orm.jdo.support.JdoDaoSupport
 */
public abstract class DaoSupport implements InitializingBean {

	/** Logger available to subclasses */
	/** 记录器可用于子类 */
	protected final Log logger = LogFactory.getLog(getClass());


	public final void afterPropertiesSet() throws IllegalArgumentException, BeanInitializationException {
		// Let abstract subclasses check their configuration.
		// 让抽象子类检查它们的配置。
		checkDaoConfig();

		// Let concrete implementations initialize themselves.
		// 让具体实现初始化自己。
		try {
			initDao();
		}
		catch (Exception ex) {
			throw new BeanInitializationException("Initialization of DAO failed", ex);
		}
	}

	/**
	 * Abstract subclasses must override this to check their configuration.
	 * 
	 * <p> 抽象子类必须覆盖它以检查其配置。
	 * 
	 * <p>Implementors should be marked as {@code final} if concrete subclasses
	 * are not supposed to override this template method themselves.
	 * 
	 * <p> 如果具体的子类本身不应覆盖此模板方法，则应将实现者标记为final。
	 * 
	 * @throws IllegalArgumentException in case of illegal configuration
	 * 
	 * <p> 在非法配置的情况下
	 */
	protected abstract void checkDaoConfig() throws IllegalArgumentException;

	/**
	 * Concrete subclasses can override this for custom initialization behavior.
	 * Gets called after population of this instance's bean properties.
	 * 
	 * <p> 具体的子类可以覆盖此自定义初始化行为。 在此实例的bean属性的填充之后调用。
	 * 
	 * @throws Exception if DAO initialization fails
	 * (will be rethrown as a BeanInitializationException)
	 * 
	 * <p> 如果DAO初始化失败（将作为BeanInitializationException重新抛出）
	 * 
	 * @see org.springframework.beans.factory.BeanInitializationException
	 */
	protected void initDao() throws Exception {
	}

}
