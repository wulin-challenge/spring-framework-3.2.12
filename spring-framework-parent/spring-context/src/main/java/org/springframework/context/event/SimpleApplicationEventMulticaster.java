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

package org.springframework.context.event;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Simple implementation of the {@link ApplicationEventMulticaster} interface.
 * 
 * <p> ApplicationEventMulticaster接口的简单实现。
 *
 * <p>Multicasts all events to all registered listeners, leaving it up to
 * the listeners to ignore events that they are not interested in.
 * Listeners will usually perform corresponding {@code instanceof}
 * checks on the passed-in event object.
 * 
 * <p> 将所有事件多播到所有已注册的侦听器，将其留给侦听器以忽略他们不感兴趣的事件。
 * 侦听器通常会对传入的事件对象执行相应的实例检查。
 *
 * <p>By default, all listeners are invoked in the calling thread.
 * This allows the danger of a rogue listener blocking the entire application,
 * but adds minimal overhead. Specify an alternative TaskExecutor to have
 * listeners executed in different threads, for example from a thread pool.
 * 
 * <p> 默认情况下，在调用线程中调用所有侦听器。 这允许恶意侦听器阻塞整个应用程序的危险，但增加了最小的开销。 
 * 指定备用TaskExecutor以使侦听器在不同的线程中执行，例如从线程池中执行。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setTaskExecutor
 */
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

	private Executor taskExecutor;


	/**
	 * Create a new SimpleApplicationEventMulticaster.
	 * 
	 * <p> 创建一个新的SimpleApplicationEventMulticaster。
	 */
	public SimpleApplicationEventMulticaster() {
	}

	/**
	 * Create a new SimpleApplicationEventMulticaster for the given BeanFactory.
	 * 
	 * <p> 为给定的BeanFactory创建一个新的SimpleApplicationEventMulticaster。
	 * 
	 */
	public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}


	/**
	 * Set the TaskExecutor to execute application listeners with.
	 * 
	 * <p> 设置TaskExecutor以执行应用程序侦听器。
	 * 
	 * <p>Default is a SyncTaskExecutor, executing the listeners synchronously
	 * in the calling thread.
	 * 
	 * <p> 默认是SyncTaskExecutor，在调用线程中同步执行侦听器。
	 * 
	 * <p>Consider specifying an asynchronous TaskExecutor here to not block the
	 * caller until all listeners have been executed. However, note that asynchronous
	 * execution will not participate in the caller's thread context (class loader,
	 * transaction association) unless the TaskExecutor explicitly supports this.
	 * 
	 * <p> 考虑在此指定一个异步TaskExecutor，以便在所有侦听器都被执行之前不阻塞调用者。 
	 * 但是，请注意异步执行不会参与调用者的线程上下文（类加载器，事务关联），除非TaskExecutor明确支持此操作。
	 * 
	 * @see org.springframework.core.task.SyncTaskExecutor
	 * @see org.springframework.core.task.SimpleAsyncTaskExecutor
	 */
	public void setTaskExecutor(Executor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Return the current TaskExecutor for this multicaster.
	 * 
	 * <p> 返回此传播器的当前TaskExecutor。
	 */
	protected Executor getTaskExecutor() {
		return this.taskExecutor;
	}


	@SuppressWarnings("unchecked")
	public void multicastEvent(final ApplicationEvent event) {
		for (final ApplicationListener listener : getApplicationListeners(event)) {
			Executor executor = getTaskExecutor();
			if (executor != null) {
				executor.execute(new Runnable() {
					public void run() {
						listener.onApplicationEvent(event);
					}
				});
			}
			else {
				listener.onApplicationEvent(event);
			}
		}
	}

}
