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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Holder for a BeanDefinition with name and aliases.
 * Can be registered as a placeholder for an inner bean.
 * 
 * <p>持有BeanDefinition的名称和别名。 可以注册为内部bean的占位符。
 *
 * <p>Can also be used for programmatic registration of inner bean
 * definitions. If you don't care about BeanNameAware and the like,
 * registering RootBeanDefinition or ChildBeanDefinition is good enough.
 * 
 * <p>也可以用于内部bean定义的编程注册。 如果您不关心BeanNameAware等，注册RootBeanDefinition或ChildBeanDefinition就足够了。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see org.springframework.beans.factory.BeanNameAware
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public class BeanDefinitionHolder implements BeanMetadataElement {

	private final BeanDefinition beanDefinition;

	private final String beanName;

	private final String[] aliases;


	/**
	 * Create a new BeanDefinitionHolder.
	 * 
	 * <p>创建一个新的BeanDefinitionHolder。
	 * @param beanDefinition the BeanDefinition to wrap - 要包装的BeanDefinition
	 * @param beanName the name of the bean, as specified for the bean definition
	 * 
	 * <p>bean的名称，如bean定义所指定的
	 */
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
		this(beanDefinition, beanName, null);
	}

	/**
	 * Create a new BeanDefinitionHolder. 
	 * 
	 * <p>创建一个新的BeanDefinitionHolder。
	 * 
	 * @param beanDefinition the BeanDefinition to wrap - 要包装的BeanDefinition
	 * @param beanName the name of the bean, as specified for the bean definition - bean的名称，如bean定义所指定的
	 * @param aliases alias names for the bean, or {@code null} if none - bean的别名，如果没有则为null
	 */
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.aliases = aliases;
	}

	/**
	 * Copy constructor: Create a new BeanDefinitionHolder with the
	 * same contents as the given BeanDefinitionHolder instance.
	 * 
	 * <p>复制构造函数：创建一个新的BeanDefinitionHolder，其内容与给定的BeanDefinitionHolder实例相同。
	 * 
	 * <p>Note: The wrapped BeanDefinition reference is taken as-is;
	 * it is {@code not} deeply copied.
	 * 
	 * <p>注意：包装的BeanDefinition引用按原样进行; 它没有被深深复制。
	 * 
	 * @param beanDefinitionHolder the BeanDefinitionHolder to copy - 要复制的BeanDefinitionHolder
	 */
	public BeanDefinitionHolder(BeanDefinitionHolder beanDefinitionHolder) {
		Assert.notNull(beanDefinitionHolder, "BeanDefinitionHolder must not be null");
		this.beanDefinition = beanDefinitionHolder.getBeanDefinition();
		this.beanName = beanDefinitionHolder.getBeanName();
		this.aliases = beanDefinitionHolder.getAliases();
	}


	/**
	 * Return the wrapped BeanDefinition.
	 * 
	 * <p>返回包装的BeanDefinition。
	 * 
	 */
	public BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}

	/**
	 * Return the primary name of the bean, as specified for the bean definition.
	 * 
	 * <p>返回bean的主名称，如bean定义所指定。
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * Return the alias names for the bean, as specified directly for the bean definition.
	 * 
	 * <p>返回bean的别名，直接为bean定义指定。
	 * 
	 * @return the array of alias names, or {@code null} if none
	 * 
	 * <p>别名的数组，如果没有则为null
	 * 
	 */
	public String[] getAliases() {
		return this.aliases;
	}

	/**
	 * Expose the bean definition's source object. - 公开bean定义的源对象。
	 * @see BeanDefinition#getSource()
	 */
	public Object getSource() {
		return this.beanDefinition.getSource();
	}

	/**
	 * Determine whether the given candidate name matches the bean name
	 * or the aliases stored in this bean definition.
	 * 
	 * <p>确定给定的候选名称是否与bean名称或此bean定义中存储的别名匹配。
	 * 
	 */
	public boolean matchesName(String candidateName) {
		return (candidateName != null &&
				(candidateName.equals(this.beanName) || ObjectUtils.containsElement(this.aliases, candidateName)));
	}


	/**
	 * Return a friendly, short description for the bean, stating name and aliases.
	 * 
	 * <p>返回bean的友好简短描述，说明名称和别名。
	 * 
	 * @see #getBeanName()
	 * @see #getAliases()
	 */
	public String getShortDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Bean definition with name '").append(this.beanName).append("'");
		if (this.aliases != null) {
			sb.append(" and aliases [").append(StringUtils.arrayToCommaDelimitedString(this.aliases)).append("]");
		}
		return sb.toString();
	}

	/**
	 * Return a long description for the bean, including name and aliases
	 * as well as a description of the contained {@link BeanDefinition}.
	 * 
	 * <p>返回bean的长描述，包括名称和别名以及包含的BeanDefinition的描述。
	 * 
	 * @see #getShortDescription()
	 * @see #getBeanDefinition()
	 */
	public String getLongDescription() {
		StringBuilder sb = new StringBuilder(getShortDescription());
		sb.append(": ").append(this.beanDefinition);
		return sb.toString();
	}

	/**
	 * This implementation returns the long description. Can be overridden
	 * to return the short description or any kind of custom description instead.
	 * 
	 * <p>此实现返回长描述。 可以重写以返回简短描述或任何类型的自定义描述。
	 * 
	 * @see #getLongDescription()
	 * @see #getShortDescription()
	 */
	@Override
	public String toString() {
		return getLongDescription();
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanDefinitionHolder)) {
			return false;
		}
		BeanDefinitionHolder otherHolder = (BeanDefinitionHolder) other;
		return this.beanDefinition.equals(otherHolder.beanDefinition) &&
				this.beanName.equals(otherHolder.beanName) &&
				ObjectUtils.nullSafeEquals(this.aliases, otherHolder.aliases);
	}

	@Override
	public int hashCode() {
		int hashCode = this.beanDefinition.hashCode();
		hashCode = 29 * hashCode + this.beanName.hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.aliases);
		return hashCode;
	}

}
