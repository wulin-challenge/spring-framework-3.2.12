/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.serviceloader;

import java.util.ServiceLoader;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Abstract base class for FactoryBeans operating on the
 * JDK 1.6 {@link java.util.ServiceLoader} facility.
 * 
 * <p> FactoryBeans的抽象基类，在JDK 1.6 java.util.ServiceLoader工具上运行。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see java.util.ServiceLoader
 */
public abstract class AbstractServiceLoaderBasedFactoryBean extends AbstractFactoryBean
		implements BeanClassLoaderAware {

	private Class serviceType;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	/**
	 * Specify the desired service type (typically the service's public API).
	 * 
	 * <p> 指定所需的服务类型（通常是服务的公共API）。
	 */
	public void setServiceType(Class serviceType) {
		this.serviceType = serviceType;
	}

	/**
	 * Return the desired service type.
	 * 
	 * <p> 返回所需的服务类型。
	 */
	public Class getServiceType() {
		return this.serviceType;
	}

	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}


	/**
	 * Delegates to {@link #getObjectToExpose(java.util.ServiceLoader)}.
	 * 
	 * <p> 委托getObjectToExpose（java.util.ServiceLoader）。
	 * 
	 * @return the object to expose - 要暴露的对象
	 */
	@Override
	protected Object createInstance() {
		Assert.notNull(getServiceType(), "Property 'serviceType' is required");
		return getObjectToExpose(ServiceLoader.load(getServiceType(), this.beanClassLoader));
	}

	/**
	 * Determine the actual object to expose for the given ServiceLoader.
	 * 
	 * <p> 确定要为给定的ServiceLoader公开的实际对象。
	 * 
	 * <p>Left to concrete subclasses.
	 * 
	 * <p>留给具体的子类。
	 * 
	 * @param serviceLoader the ServiceLoader for the configured service class
	 * 
	 * <p> 已配置服务类的ServiceLoader
	 * 
	 * @return the object to expose - 要暴露的对象
	 */
	protected abstract Object getObjectToExpose(ServiceLoader serviceLoader);

}
