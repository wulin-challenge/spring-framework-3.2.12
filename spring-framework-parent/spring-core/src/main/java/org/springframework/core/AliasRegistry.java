/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.core;

/**
 * Common interface for managing aliases. Serves as super-interface for
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
 * 
 * <p>用于管理别名的通用界面。 用作org.springframework.beans.factory.support.BeanDefinitionRegistry的超级接口。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
public interface AliasRegistry {

	/**
	 * Given a name, register an alias for it. - 给定名称，为其注册别名。
	 * @param name the canonical name - 规范名称
	 * @param alias the alias to be registered - 要注册的别名
	 * @throws IllegalStateException if the alias is already in use
	 * and may not be overridden - 如果别名已被使用且可能未被覆盖
	 */
	void registerAlias(String name, String alias);

	/**
	 * Remove the specified alias from this registry. - 从此注册表中删除指定的别名。
	 * @param alias the alias to remove - 要删除的别名
	 * @throws IllegalStateException if no such alias was found - 如果没有找到这样的别名
	 */
	void removeAlias(String alias);

	/**
	 * Determine whether this given name is defines as an alias
	 * (as opposed to the name of an actually registered component).
	 * 
	 * <p>确定此给定名称是否定义为别名（而不是实际注册的组件的名称）。
	 * 
	 * @param beanName the bean name to check - 要检查的bean名称
	 * @return whether the given name is an alias - 给定名称是否为别名
	 */
	boolean isAlias(String beanName);

	/**
	 * Return the aliases for the given name, if defined.
	 * 
	 * <p>如果已定义，则返回给定名称的别名。
	 * 
	 * @param name the name to check for aliases - 要检查别名的名称
	 * @return the aliases, or an empty array if none - 别名，如果没有，则为空数组
	 */
	String[] getAliases(String name);

}
