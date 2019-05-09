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

package org.springframework.beans.factory.parsing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;

/**
 * ComponentDefinition based on a standard BeanDefinition, exposing the given bean
 * definition as well as inner bean definitions and bean references for the given bean.
 * 
 * <p> ComponentDefinition基于标准BeanDefinition，公开给定的bean定义以及给定bean的内部bean定义和bean引用。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class BeanComponentDefinition extends BeanDefinitionHolder implements ComponentDefinition {

	private BeanDefinition[] innerBeanDefinitions;

	private BeanReference[] beanReferences;


	/**
	 * Create a new BeanComponentDefinition for the given bean.
	 * 
	 * <p> 为给定的bean创建一个新的BeanComponentDefinition。
	 * 
	 * @param beanDefinition the BeanDefinition
	 * @param beanName the name of the bean - bean的名称
	 */
	public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName) {
		super(beanDefinition, beanName);
		findInnerBeanDefinitionsAndBeanReferences(beanDefinition);
	}

	/**
	 * Create a new BeanComponentDefinition for the given bean.
	 * 
	 * <p> 为给定的bean创建一个新的BeanComponentDefinition。
	 * 
	 * @param beanDefinition the BeanDefinition
	 * @param beanName the name of the bean - bean 名称
	 * @param aliases alias names for the bean, or {@code null} if none - bean的别名，如果没有则为null
	 */
	public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName, String[] aliases) {
		super(beanDefinition, beanName, aliases);
		findInnerBeanDefinitionsAndBeanReferences(beanDefinition);
	}

	/**
	 * Create a new BeanComponentDefinition for the given bean.
	 * 
	 * <p> 为给定的bean创建一个新的BeanComponentDefinition。
	 * 
	 * @param holder the BeanDefinitionHolder encapsulating the
	 * bean definition as well as the name of the bean
	 * 
	 * <p> BeanDefinitionHolder封装了bean定义以及bean的名称
	 */
	public BeanComponentDefinition(BeanDefinitionHolder holder) {
		super(holder);
		findInnerBeanDefinitionsAndBeanReferences(holder.getBeanDefinition());
	}

	/**
	 * find Inner Bean Definitions And Bean References
	 * 
	 * <p> 找到内部Bean定义和Bean引用
	 * @param beanDefinition
	 */
	private void findInnerBeanDefinitionsAndBeanReferences(BeanDefinition beanDefinition) {
		List<BeanDefinition> innerBeans = new ArrayList<BeanDefinition>();
		List<BeanReference> references = new ArrayList<BeanReference>();
		PropertyValues propertyValues = beanDefinition.getPropertyValues();
		for (int i = 0; i < propertyValues.getPropertyValues().length; i++) {
			PropertyValue propertyValue = propertyValues.getPropertyValues()[i];
			Object value = propertyValue.getValue();
			if (value instanceof BeanDefinitionHolder) {
				innerBeans.add(((BeanDefinitionHolder) value).getBeanDefinition());
			}
			else if (value instanceof BeanDefinition) {
				innerBeans.add((BeanDefinition) value);
			}
			else if (value instanceof BeanReference) {
				references.add((BeanReference) value);
			}
		}
		this.innerBeanDefinitions = innerBeans.toArray(new BeanDefinition[innerBeans.size()]);
		this.beanReferences = references.toArray(new BeanReference[references.size()]);
	}


	public String getName() {
		return getBeanName();
	}

	public String getDescription() {
		return getShortDescription();
	}

	public BeanDefinition[] getBeanDefinitions() {
		return new BeanDefinition[] {getBeanDefinition()};
	}

	public BeanDefinition[] getInnerBeanDefinitions() {
		return this.innerBeanDefinitions;
	}

	public BeanReference[] getBeanReferences() {
		return this.beanReferences;
	}


	/**
	 * This implementation returns this ComponentDefinition's description.
	 * 
	 * <p> 此实现返回此ComponentDefinition的描述。
	 * 
	 * @see #getDescription()
	 */
	@Override
	public String toString() {
		return getDescription();
	}

	/**
	 * This implementations expects the other object to be of type BeanComponentDefinition
	 * as well, in addition to the superclass's equality requirements.
	 * 
	 * <p> 除了超类的相等要求之外，此实现还期望其他对象也是BeanComponentDefinition类型。
	 * 
	 */
	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof BeanComponentDefinition && super.equals(other)));
	}

}
