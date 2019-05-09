/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.parsing;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;

/**
 * Base implementation of {@link ComponentDefinition} that provides a basic implementation of
 * {@link #getDescription} which delegates to {@link #getName}. Also provides a base implementation
 * of {@link #toString} which delegates to {@link #getDescription} in keeping with the recommended
 * implementation strategy. Also provides default implementations of {@link #getInnerBeanDefinitions}
 * and {@link #getBeanReferences} that return an empty array.
 * 
 * <p> ComponentDefinition的基本实现，它提供了委托给getName的getDescription的基本实现。 还提供了toString的基本实现，
 * 它根据推荐的实现策略委托getDescription。 还提供了返回空数组的getInnerBeanDefinitions和getBeanReferences的默认实现。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractComponentDefinition implements ComponentDefinition {

	/**
	 * Delegates to {@link #getName}.
	 * 
	 * <p> 代表getName。
	 */
	public String getDescription() {
		return getName();
	}

	/**
	 * Returns an empty array.
	 * 
	 * <p> 返回一个空数组。
	 */
	public BeanDefinition[] getBeanDefinitions() {
		return new BeanDefinition[0];
	}

	/**
	 * Returns an empty array.
	 * 
	 * <p> 返回一个空数组。
	 * 
	 */
	public BeanDefinition[] getInnerBeanDefinitions() {
		return new BeanDefinition[0];
	}

	/**
	 * Returns an empty array.
	 * 
	 * <p> 返回一个空数组。
	 * 
	 */
	public BeanReference[] getBeanReferences() {
		return new BeanReference[0];
	}

	/**
	 * Delegates to {@link #getDescription}.
	 * 
	 * <p> 对象的字符串表示形式。
	 * 
	 */
	@Override
	public String toString() {
		return getDescription();
	}

}
