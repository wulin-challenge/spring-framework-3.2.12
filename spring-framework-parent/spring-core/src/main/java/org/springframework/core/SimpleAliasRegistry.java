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

package org.springframework.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * Simple implementation of the {@link AliasRegistry} interface.
 * Serves as base class for
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
 * implementations.
 * 
 * <p>简单实现AliasRegistry接口。 用作org.springframework.beans.factory.support.BeanDefinitionRegistry实现的基类。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
public class SimpleAliasRegistry implements AliasRegistry {

	/** Map from alias to canonical name */
	/** 从别名映射到规范名称 */
	private final Map<String, String> aliasMap = new ConcurrentHashMap<String, String>(16);


	public void registerAlias(String name, String alias) {
		Assert.hasText(name, "'name' must not be empty");
		Assert.hasText(alias, "'alias' must not be empty");
		//如果beanName与alias相同的话不记录alias,并删除对应的alias
		if (alias.equals(name)) {
			this.aliasMap.remove(alias);
		}
		else {
			//如果alias不允许被覆盖则抛出异常
 			if (!allowAliasOverriding()) {
				String registeredName = this.aliasMap.get(alias);
				if (registeredName != null && !registeredName.equals(name)) {
					throw new IllegalStateException("Cannot register alias '" + alias + "' for name '" +
							name + "': It is already registered for name '" + registeredName + "'.");
				}
			}
			//当A->B存在时,若再次出现A->C->B时候则会抛出异常
			checkForAliasCircle(name, alias);
			this.aliasMap.put(alias, name);
		}
	}

	/**
	 * Return whether alias overriding is allowed.
	 * Default is {@code true}.
	 * 
	 * <p> 返回是否允许别名覆盖。 默认为true。
	 */
	protected boolean allowAliasOverriding() {
		return true;
	}

	public void removeAlias(String alias) {
		String name = this.aliasMap.remove(alias);
		if (name == null) {
			throw new IllegalStateException("No alias '" + alias + "' registered");
		}
	}

	public boolean isAlias(String name) {
		return this.aliasMap.containsKey(name);
	}

	public String[] getAliases(String name) {
		List<String> result = new ArrayList<String>();
		synchronized (this.aliasMap) {
			retrieveAliases(name, result);
		}
		return StringUtils.toStringArray(result);
	}

	/**
	 * Transitively retrieve all aliases for the given name.
	 * 
	 * <p> 传递性地检索给定名称的所有别名。
	 * @param name the target name to find aliases for - 要查找别名的目标名称
	 * @param result the resulting aliases list - 生成的别名列表
	 */
	private void retrieveAliases(String name, List<String> result) {
		for (Map.Entry<String, String> entry : this.aliasMap.entrySet()) {
			String registeredName = entry.getValue();
			if (registeredName.equals(name)) {
				String alias = entry.getKey();
				result.add(alias);
				retrieveAliases(alias, result);
			}
		}
	}

	/**
	 * Resolve all alias target names and aliases registered in this
	 * factory, applying the given StringValueResolver to them.
	 * 
	 * <p> 解析在此工厂中注册的所有别名目标名称和别名，将给定的StringValueResolver应用于它们。
	 * 
	 * <p>The value resolver may for example resolve placeholders
	 * in target bean names and even in alias names.
	 * 
	 * <p> 例如，值解析器可以解析目标bean名称中的占位符，甚至可以解析别名中的占位符。
	 * 
	 * @param valueResolver the StringValueResolver to apply - 要应用的StringValueResolver
	 */
	public void resolveAliases(StringValueResolver valueResolver) {
		Assert.notNull(valueResolver, "StringValueResolver must not be null");
		synchronized (this.aliasMap) {
			Map<String, String> aliasCopy = new HashMap<String, String>(this.aliasMap);
			for (String alias : aliasCopy.keySet()) {
				String registeredName = aliasCopy.get(alias);
				String resolvedAlias = valueResolver.resolveStringValue(alias);
				String resolvedName = valueResolver.resolveStringValue(registeredName);
				if (resolvedAlias.equals(resolvedName)) {
					this.aliasMap.remove(alias);
				}
				else if (!resolvedAlias.equals(alias)) {
					String existingName = this.aliasMap.get(resolvedAlias);
					if (existingName != null && !existingName.equals(resolvedName)) {
						throw new IllegalStateException(
								"Cannot register resolved alias '" + resolvedAlias + "' (original: '" + alias +
								"') for name '" + resolvedName + "': It is already registered for name '" +
								registeredName + "'.");
					}
					checkForAliasCircle(resolvedName, resolvedAlias);
					this.aliasMap.remove(alias);
					this.aliasMap.put(resolvedAlias, resolvedName);
				}
				else if (!registeredName.equals(resolvedName)) {
					this.aliasMap.put(alias, resolvedName);
				}
			}
		}
	}

	/**
	 * Determine the raw name, resolving aliases to canonical names.
	 * 
	 * <p> 确定原始名称，将别名解析为规范名称。
	 * 
	 * @param name the user-specified name - 用户指定的名称
	 * @return the transformed name - 改造后的名字
	 */
	public String canonicalName(String name) {
		String canonicalName = name;
		// Handle aliasing...
		// 处理别名......
		String resolvedName;
		do {
			resolvedName = this.aliasMap.get(canonicalName);
			if (resolvedName != null) {
				canonicalName = resolvedName;
			}
		}
		while (resolvedName != null);
		return canonicalName;
	}

	/**
	 * Check whether the given name points back to given alias as an alias
	 * in the other direction, catching a circular reference upfront and
	 * throwing a corresponding IllegalStateException.
	 * 
	 * <p> 检查给定名称是否指向给定别名作为另一个方向的别名，预先捕获循环引用并抛出相应的IllegalStateException。
	 * 
	 * @param name the candidate name - 候选名称
	 * @param alias the candidate alias - 候选别名
	 * @see #registerAlias
	 */
	protected void checkForAliasCircle(String name, String alias) {
		if (alias.equals(canonicalName(name))) {
			throw new IllegalStateException("Cannot register alias '" + alias +
					"' for name '" + name + "': Circular reference - '" +
					name + "' is a direct or indirect alias for '" + alias + "' already");
		}
	}

}
