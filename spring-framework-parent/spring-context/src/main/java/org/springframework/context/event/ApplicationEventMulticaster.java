/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Interface to be implemented by objects that can manage a number of
 * {@link ApplicationListener} objects, and publish events to them.
 * 
 * <p> 由可以管理大量ApplicationListener对象并向其发布事件的对象实现的接口。
 *
 * <p>An {@link org.springframework.context.ApplicationEventPublisher}, typically
 * a Spring {@link org.springframework.context.ApplicationContext}, can use an
 * ApplicationEventMulticaster as a delegate for actually publishing events.
 *
 * <p> org.springframework.context.ApplicationEventPublisher（通常是Spring 
 * org.springframework.context.ApplicationContext）可以使用ApplicationEventMulticaster
 * 作为实际发布事件的委托。
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface ApplicationEventMulticaster {

	/**
	 * Add a listener to be notified of all events.
	 * 
	 * <p> 添加一个侦听器以通知所有事件。
	 * 
	 * @param listener the listener to add - 要添加的监听器
	 */
	void addApplicationListener(ApplicationListener listener);

	/**
	 * Add a listener bean to be notified of all events.
	 * 
	 * <p> 添加一个监听器bean以通知所有事件。
	 * 
	 * @param listenerBeanName the name of the listener bean to add - 要添加的侦听器bean的名称
	 */
	void addApplicationListenerBean(String listenerBeanName);

	/**
	 * Remove a listener from the notification list.
	 * 
	 * <p> 从通知列表中删除侦听器。
	 * 
	 * @param listener the listener to remove - 要删除的监听器
	 */
	void removeApplicationListener(ApplicationListener listener);

	/**
	 * Remove a listener bean from the notification list.
	 * 
	 * <p> 从通知列表中删除侦听器bean。
	 * 
	 * @param listenerBeanName the name of the listener bean to add - 要添加的侦听器bean的名称
	 */
	void removeApplicationListenerBean(String listenerBeanName);

	/**
	 * Remove all listeners registered with this multicaster.
	 * 
	 * <p> 删除在此传播程序中注册的所有侦听器。
	 * 
	 * <p>After a remove call, the multicaster will perform no action
	 * on event notification until new listeners are being registered.
	 * 
	 * <p> 在删除调用之后，多播器将不会对事件通知执行任何操作，直到注册新的侦听器。
	 * 
	 */
	void removeAllListeners();

	/**
	 * Multicast the given application event to appropriate listeners.
	 * 
	 * <p> 将给定的应用程序事件传播到适当的侦听器。
	 * 
	 * @param event the event to multicast - 传播事件
	 */
	void multicastEvent(ApplicationEvent event);

}
