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

package org.springframework.beans;

import org.springframework.core.AttributeAccessorSupport;

/**
 * Extension of {@link org.springframework.core.AttributeAccessorSupport},
 * holding attributes as {@link BeanMetadataAttribute} objects in order
 * to keep track of the definition source.
 *
 *<p> 扩展org.springframework.core.AttributeAccessorSupport，将属性保存为BeanMetadataAttribute对象，以便跟踪定义源。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
@SuppressWarnings("serial")
public class BeanMetadataAttributeAccessor extends AttributeAccessorSupport implements BeanMetadataElement {

	private Object source;


	/**
	 * Set the configuration source {@code Object} for this metadata element.
	 * 
	 * <p> 为此元数据元素设置配置源Object。
	 * 
	 * <p>The exact type of the object will depend on the configuration mechanism used.
	 * 
	 * <p> 对象的确切类型取决于所使用的配置机制。
	 * 
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return this.source;
	}


	/**
	 * Add the given BeanMetadataAttribute to this accessor's set of attributes.
	 * 
	 * <p> 将给定的BeanMetadataAttribute添加到此访问者的属性集中。
	 * 
	 * @param attribute the BeanMetadataAttribute object to register
	 * 
	 * <p> 要注册的BeanMetadataAttribute对象
	 * 
	 */
	public void addMetadataAttribute(BeanMetadataAttribute attribute) {
		super.setAttribute(attribute.getName(), attribute);
	}

	/**
	 * Look up the given BeanMetadataAttribute in this accessor's set of attributes.
	 * 
	 * <p> 在此访问者的属性集中查找给定的BeanMetadataAttribute。
	 * 
	 * @param name the name of the attribute - 属性的名称
	 * @return the corresponding BeanMetadataAttribute object,
	 * or {@code null} if no such attribute defined
	 * 
	 * <p> 相应的BeanMetadataAttribute对象，如果没有定义此类属性，则返回null
	 * 
	 */
	public BeanMetadataAttribute getMetadataAttribute(String name) {
		return (BeanMetadataAttribute) super.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		super.setAttribute(name, new BeanMetadataAttribute(name, value));
	}

	@Override
	public Object getAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.getAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}

	@Override
	public Object removeAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.removeAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}

}
