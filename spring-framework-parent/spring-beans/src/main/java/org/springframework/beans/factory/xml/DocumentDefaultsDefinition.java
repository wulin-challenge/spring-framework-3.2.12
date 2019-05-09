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

package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.parsing.DefaultsDefinition;

/**
 * Simple JavaBean that holds the defaults specified at the {@code &lt;beans&gt;}
 * level in a standard Spring XML bean definition document:
 * {@code default-lazy-init}, {@code default-autowire}, etc.
 * 
 * <p>包含在< beans >中指定的默认值的简单JavaBean 标准Spring XML bean定义文档中的level：default-lazy-init，default-autowire等。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 */
public class DocumentDefaultsDefinition implements DefaultsDefinition {

	private String lazyInit;

	private String merge;

	private String autowire;

	private String dependencyCheck;

	private String autowireCandidates;

	private String initMethod;

	private String destroyMethod;

	private Object source;


	/**
	 * Set the default lazy-init flag for the document that's currently parsed.
	 * 
	 * <p>为当前解析的文档设置默认的lazy-init标志。
	 */
	public void setLazyInit(String lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * Return the default lazy-init flag for the document that's currently parsed.
	 * 
	 * <p>返回当前解析的文档的默认lazy-init标志。
	 */
	public String getLazyInit() {
		return this.lazyInit;
	}

	/**
	 * Set the default merge setting for the document that's currently parsed.
	 * 
	 * <p>为当前解析的文档设置默认合并设置。
	 */
	public void setMerge(String merge) {
		this.merge = merge;
	}

	/**
	 * Return the default merge setting for the document that's currently parsed.
	 * 
	 * <p>返回当前解析的文档的默认合并设置。
	 */
	public String getMerge() {
		return this.merge;
	}

	/**
	 * Set the default autowire setting for the document that's currently parsed.
	 * 
	 * <p>为当前解析的文档设置默认的自动装配设置。
	 */
	public void setAutowire(String autowire) {
		this.autowire = autowire;
	}

	/**
	 * Return the default autowire setting for the document that's currently parsed.
	 * 
	 * <p>返回当前已解析文档的默认自动装配设置。
	 */
	public String getAutowire() {
		return this.autowire;
	}

	/**
	 * Set the default dependency-check setting for the document that's currently parsed.
	 * 
	 * <p>为当前解析的文档设置默认依赖项检查设置。
	 */
	public void setDependencyCheck(String dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	/**
	 * Return the default dependency-check setting for the document that's currently parsed.
	 * 
	 * <p>返回当前解析的文档的默认依赖项检查设置。
	 */
	public String getDependencyCheck() {
		return this.dependencyCheck;
	}

	/**
	 * Set the default autowire-candidate pattern for the document that's currently parsed.
	 * Also accepts a comma-separated list of patterns.
	 * 
	 * <p>为当前解析的文档设置默认的autowire-candidate模式。 还接受以逗号分隔的模式列表。
	 */
	public void setAutowireCandidates(String autowireCandidates) {
		this.autowireCandidates = autowireCandidates;
	}

	/**
	 * Return the default autowire-candidate pattern for the document that's currently parsed.
	 * May also return a comma-separated list of patterns.
	 * 
	 * <p>返回当前解析的文档的默认autowire-candidate模式。 也可以返回以逗号分隔的模式列表。
	 */
	public String getAutowireCandidates() {
		return this.autowireCandidates;
	}

	/**
	 * Set the default init-method setting for the document that's currently parsed.
	 * 
	 * <p>为当前解析的文档设置默认的init-method设置。
	 */
	public void setInitMethod(String initMethod) {
		this.initMethod = initMethod;
	}

	/**
	 * Return the default init-method setting for the document that's currently parsed.
	 * 
	 * <p>返回当前解析的文档的默认init-method设置。
	 */
	public String getInitMethod() {
		return this.initMethod;
	}

	/**
	 * Set the default destroy-method setting for the document that's currently parsed.
	 * 
	 * <p>为当前解析的文档设置默认的销毁方法设置。
	 */
	public void setDestroyMethod(String destroyMethod) {
		this.destroyMethod = destroyMethod;
	}

	/**
	 * Return the default destroy-method setting for the document that's currently parsed.
	 * 
	 * <p>返回当前解析的文档的默认destroy-method设置。
	 */
	public String getDestroyMethod() {
		return this.destroyMethod;
	}

	/**
	 * Set the configuration source {@code Object} for this metadata element.
	 * 
	 * <p>为此元数据元素设置配置源Object。
	 * 
	 * <p>The exact type of the object will depend on the configuration mechanism used.
	 * 
	 * <p>对象的确切类型取决于所使用的配置机制。
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return this.source;
	}

}
