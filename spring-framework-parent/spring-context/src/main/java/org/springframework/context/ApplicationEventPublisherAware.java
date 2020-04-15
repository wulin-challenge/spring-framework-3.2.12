/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.context;

import org.springframework.beans.factory.Aware;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the ApplicationEventPublisher (typically the ApplicationContext)
 * that it runs in.
 * 
 * <p> 希望由希望在其运行的ApplicationEventPublisher（通常是ApplicationContext）通知的任何对象实现的接口。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.1
 * @see ApplicationContextAware
 */
public interface ApplicationEventPublisherAware extends Aware {

	/**
	 * Set the ApplicationEventPublisher that this object runs in.
	 * 
	 * <p> 设置运行该对象的ApplicationEventPublisher。
	 * 
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's afterPropertiesSet or a custom init-method.
	 * Invoked before ApplicationContextAware's setApplicationContext.
	 * 
	 * <p> 在填充常规bean属性之后但在诸如InitializingBean的afterPropertiesSet或自定义
	 * init方法之类的init回调之前调用。 在ApplicationContextAware的setApplicationContext之前调用。
	 * 
	 * @param applicationEventPublisher event publisher to be used by this object
	 * 
	 * <p> 此对象将使用的事件发布者
	 */
	void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher);

}
