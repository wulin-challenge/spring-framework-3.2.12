/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.util;

import java.io.Serializable;

/**
 * Simple customizable helper class for creating new {@link Thread} instances.
 * Provides various bean properties: thread name prefix, thread priority, etc.
 * 
 * <p> 用于创建新Thread实例的简单可定制助手类。 提供各种bean属性：线程名称前缀，线程优先级等。
 *
 * <p>Serves as base class for thread factories such as
 * {@link org.springframework.scheduling.concurrent.CustomizableThreadFactory}.
 * 
 * <p> 用作线程工厂的基类，例如org.springframework.scheduling.concurrent.CustomizableThreadFactory。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see org.springframework.scheduling.concurrent.CustomizableThreadFactory
 */
@SuppressWarnings("serial")
public class CustomizableThreadCreator implements Serializable {

	private String threadNamePrefix;

	private int threadPriority = Thread.NORM_PRIORITY;

	private boolean daemon = false;

	private ThreadGroup threadGroup;

	private int threadCount = 0;

	private final Object threadCountMonitor = new SerializableMonitor();


	/**
	 * Create a new CustomizableThreadCreator with default thread name prefix.
	 * 
	 * <p> 使用默认线程名称前缀创建一个新的CustomizableThreadCreator。
	 */
	public CustomizableThreadCreator() {
		this.threadNamePrefix = getDefaultThreadNamePrefix();
	}

	/**
	 * Create a new CustomizableThreadCreator with the given thread name prefix.
	 * 
	 * <p> 使用给定的线程名称前缀创建一个新的CustomizableThreadCreator。
	 * 
	 * @param threadNamePrefix the prefix to use for the names of newly created threads
	 * 
	 * <p> 用于新创建的线程名称的前缀
	 */
	public CustomizableThreadCreator(String threadNamePrefix) {
		this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
	}


	/**
	 * Specify the prefix to use for the names of newly created threads.
	 * Default is "SimpleAsyncTaskExecutor-".
	 * 
	 * <p> 指定用于新创建的线程名称的前缀。 默认为“SimpleAsyncTaskExecutor-”。
	 */
	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
	}

	/**
	 * Return the thread name prefix to use for the names of newly
	 * created threads.
	 * 
	 * <p> 返回线程名称前缀以用于新创建的线程的名称。
	 */
	public String getThreadNamePrefix() {
		return this.threadNamePrefix;
	}

	/**
	 * Set the priority of the threads that this factory creates.
	 * Default is 5.
	 * 
	 * <p> 设置此工厂创建的线程的优先级。 默认值为5。
	 * 
	 * @see java.lang.Thread#NORM_PRIORITY
	 */
	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}

	/**
	 * Return the priority of the threads that this factory creates.
	 * 
	 * <p> 返回此工厂创建的线程的优先级。
	 */
	public int getThreadPriority() {
		return this.threadPriority;
	}

	/**
	 * Set whether this factory is supposed to create daemon threads,
	 * just executing as long as the application itself is running.
	 * 
	 * <p> 设置此工厂是否应该创建守护程序线程，只要应用程序本身正在运行就执行。
	 * 
	 * <p>Default is "false": Concrete factories usually support explicit cancelling.
	 * Hence, if the application shuts down, Runnables will by default finish their
	 * execution.
	 * 
	 * <p> 默认为“false”：具体工厂通常支持显式取消。 因此，如果应用程序关闭，Runnables将默认完成执行。
	 * 
	 * <p>Specify "true" for eager shutdown of threads which still actively execute
	 * a {@link Runnable} at the time that the application itself shuts down.
	 * 
	 * <p> 为急切关闭仍然在应用程序自身关闭时仍主动执行Runnable的线程指定“true”。
	 * 
	 * @see java.lang.Thread#setDaemon
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * Return whether this factory should create daemon threads.
	 * 
	 * <p> 返回此工厂是否应创建守护程序线程。
	 * 
	 */
	public boolean isDaemon() {
		return this.daemon;
	}

	/**
	 * Specify the name of the thread group that threads should be created in.
	 * 
	 * <p> 指定应在其中创建线程的线程组的名称。
	 * 
	 * @see #setThreadGroup
	 */
	public void setThreadGroupName(String name) {
		this.threadGroup = new ThreadGroup(name);
	}

	/**
	 * Specify the thread group that threads should be created in.
	 * 
	 * <p> 指定应在其中创建线程的线程组。
	 * 
	 * @see #setThreadGroupName
	 */
	public void setThreadGroup(ThreadGroup threadGroup) {
		this.threadGroup = threadGroup;
	}

	/**
	 * Return the thread group that threads should be created in
	 * (or {@code null} for the default group).
	 * 
	 * <p> 返回应在其中创建线程的线程组（对于默认组，返回null）。
	 * 
	 */
	public ThreadGroup getThreadGroup() {
		return this.threadGroup;
	}


	/**
	 * Template method for the creation of a new {@link Thread}.
	 * 
	 * <p> 用于创建新线程的模板方法。
	 * 
	 * <p>The default implementation creates a new Thread for the given
	 * {@link Runnable}, applying an appropriate thread name.
	 * 
	 * <p> 默认实现为给定的Runnable创建一个新的Thread，并应用适当的线程名称。
	 * 
	 * @param runnable the Runnable to execute - 要运行的Runnable
	 * @see #nextThreadName()
	 */
	public Thread createThread(Runnable runnable) {
		Thread thread = new Thread(getThreadGroup(), runnable, nextThreadName());
		thread.setPriority(getThreadPriority());
		thread.setDaemon(isDaemon());
		return thread;
	}

	/**
	 * Return the thread name to use for a newly created {@link Thread}.
	 * 
	 * <p> 返回用于新创建的Thread的线程名称。
	 * 
	 * <p>The default implementation returns the specified thread name prefix
	 * with an increasing thread count appended: e.g. "SimpleAsyncTaskExecutor-0".
	 * 
	 * <p> 默认实现返回指定的线程名称前缀，并附加增加的线程数：例如“SimpleAsyncTaskExecutor-0”。
	 * @see #getThreadNamePrefix()
	 */
	protected String nextThreadName() {
		int threadNumber = 0;
		synchronized (this.threadCountMonitor) {
			this.threadCount++;
			threadNumber = this.threadCount;
		}
		return getThreadNamePrefix() + threadNumber;
	}

	/**
	 * Build the default thread name prefix for this factory.
	 * 
	 * <p> 为此工厂构建默认线程名称前缀。
	 * 
	 * @return the default thread name prefix (never {@code null})
	 * 
	 * <p> 默认的线程名称前缀（从不为null）
	 * 
	 */
	protected String getDefaultThreadNamePrefix() {
		return ClassUtils.getShortName(getClass()) + "-";
	}


	/**
	 * Empty class used for a serializable monitor object.
	 * 
	 * <p> 用于可序列化监视器对象的空类。
	 */
	private static class SerializableMonitor implements Serializable {
	}

}
