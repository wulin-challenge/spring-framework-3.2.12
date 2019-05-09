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

package org.springframework.transaction.config;

/**
 * Configuration constants for internal sharing across subpackages.
 * 
 * <p> 跨子包进行内部共享的配置常量。
 *
 * @author Chris Beams
 * @since 3.1
 */
public abstract class TransactionManagementConfigUtils {

	/**
	 * The bean name of the internally managed transaction advisor (used when mode == PROXY).
	 * 
	 * <p> 内部管理的事务顾问的bean名称（在mode == PROXY时使用）。
	 */
	public static final String TRANSACTION_ADVISOR_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAdvisor";

	/**
	 * The bean name of the internally managed transaction aspect (used when mode == ASPECTJ).
	 * 
	 * <p> 内部管理的事务方面的bean名称（在mode == ASPECTJ时使用）。
	 */
	public static final String TRANSACTION_ASPECT_BEAN_NAME =
			"org.springframework.transaction.config.internalTransactionAspect";

	/**
	 * The class name of the AspectJ transaction management aspect.
	 * 
	 * <p> AspectJ事务管理方面的类名。
	 */
	public static final String TRANSACTION_ASPECT_CLASS_NAME =
			"org.springframework.transaction.aspectj.AnnotationTransactionAspect";

	/**
	 * The name of the AspectJ transaction management @{@code Configuration} class.
	 * 
	 * <p> AspectJ事务管理@Configuration类的名称。
	 */
	public static final String TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME =
			"org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration";

}
