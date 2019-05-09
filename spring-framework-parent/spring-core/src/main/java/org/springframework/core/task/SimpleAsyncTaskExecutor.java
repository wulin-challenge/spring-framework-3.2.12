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

package org.springframework.core.task;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;

import org.springframework.util.Assert;
import org.springframework.util.ConcurrencyThrottleSupport;
import org.springframework.util.CustomizableThreadCreator;

/**
 * {@link TaskExecutor} implementation that fires up a new Thread for each task,
 * executing it asynchronously.
 * 
 * <p> TaskExecutor实现，为每个任务触发一个新线程，异步执行它。
 *
 * <p>Supports limiting concurrent threads through the "concurrencyLimit"
 * bean property. By default, the number of concurrent threads is unlimited.
 * 
 * <p> 支持通过“concurrencyLimit”bean属性限制并发线程。 默认情况下，并发线程数不受限制。
 *
 * <p><b>NOTE: This implementation does not reuse threads!</b> Consider a
 * thread-pooling TaskExecutor implementation instead, in particular for
 * executing a large number of short-lived tasks.
 * 
 * <p> 注意：此实现不重用线程！ 相反，请考虑使用线程池TaskExecutor实现，尤其是执行大量短期任务。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setConcurrencyLimit
 * @see SyncTaskExecutor
 * @see org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
 * @see org.springframework.scheduling.commonj.WorkManagerTaskExecutor
 */
@SuppressWarnings("serial")
public class SimpleAsyncTaskExecutor extends CustomizableThreadCreator implements AsyncTaskExecutor, Serializable {

	/**
	 * Permit any number of concurrent invocations: that is, don't throttle concurrency.
	 * 
	 * <p> 允许任意数量的并发调用：即不要限制并发。
	 */
	public static final int UNBOUNDED_CONCURRENCY = ConcurrencyThrottleSupport.UNBOUNDED_CONCURRENCY;

	/**
	 * Switch concurrency 'off': that is, don't allow any concurrent invocations.
	 * 
	 * <p> 切换并发'关'：即不允许任何并发调用。
	 * 
	 */
	public static final int NO_CONCURRENCY = ConcurrencyThrottleSupport.NO_CONCURRENCY;


	/** Internal concurrency throttle used by this executor */
	/** 此执行程序使用的内部并发限制 */
	private final ConcurrencyThrottleAdapter concurrencyThrottle = new ConcurrencyThrottleAdapter();

	private ThreadFactory threadFactory;


	/**
	 * Create a new SimpleAsyncTaskExecutor with default thread name prefix.
	 * 
	 * <p> 使用默认线程名称前缀创建一个新的SimpleAsyncTaskExecutor。
	 */
	public SimpleAsyncTaskExecutor() {
		super();
	}

	/**
	 * Create a new SimpleAsyncTaskExecutor with the given thread name prefix.
	 * 
	 * <p> 使用给定的线程名称前缀创建一个新的SimpleAsyncTaskExecutor。
	 * 
	 * @param threadNamePrefix the prefix to use for the names of newly created threads
	 * 
	 * <p> 用于新创建的线程名称的前缀
	 * 
	 */
	public SimpleAsyncTaskExecutor(String threadNamePrefix) {
		super(threadNamePrefix);
	}

	/**
	 * Create a new SimpleAsyncTaskExecutor with the given external thread factory.
	 * 
	 * <p> 使用给定的外部线程工厂创建一个新的SimpleAsyncTaskExecutor。
	 * 
	 * @param threadFactory the factory to use for creating new Threads
	 * 
	 * <p> 用于创建新线程的工厂
	 * 
	 */
	public SimpleAsyncTaskExecutor(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}


	/**
	 * Specify an external factory to use for creating new Threads,
	 * instead of relying on the local properties of this executor.
	 * 
	 * <p> 指定用于创建新线程的外部工厂，而不是依赖此执行程序的本地属性。
	 * 
	 * <p>You may specify an inner ThreadFactory bean or also a ThreadFactory reference
	 * obtained from JNDI (on a Java EE 6 server) or some other lookup mechanism.
	 * 
	 * <p>您可以指定内部ThreadFactory bean或从JNDI（在Java EE 6服务器上）或某些其他查找机制获得的ThreadFactory引用。
	 * 
	 * @see #setThreadNamePrefix
	 * @see #setThreadPriority
	 */
	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	/**
	 * Return the external factory to use for creating new Threads, if any.
	 * 
	 * <p> 返回外部工厂以用于创建新线程（如果有）。
	 * 
	 */
	public final ThreadFactory getThreadFactory() {
		return this.threadFactory;
	}

	/**
	 * Set the maximum number of parallel accesses allowed.
	 * -1 indicates no concurrency limit at all.
	 * 
	 * <p> 设置允许的最大并行访问次数。 -1表示根本没有并发限制。
	 * 
	 * <p>In principle, this limit can be changed at runtime,
	 * although it is generally designed as a config time setting.
	 * NOTE: Do not switch between -1 and any concrete limit at runtime,
	 * as this will lead to inconsistent concurrency counts: A limit
	 * of -1 effectively turns off concurrency counting completely.
	 * 
	 * <p> 原则上，此限制可以在运行时更改，但通常设计为配置时间设置。 注意：不要在运行时在-1和任何具体限制之间切换，
	 * 因为这会导致并发计数不一致：限制为-1会有效地完全关闭并发计数。
	 * 
	 * @see #UNBOUNDED_CONCURRENCY
	 */
	public void setConcurrencyLimit(int concurrencyLimit) {
		this.concurrencyThrottle.setConcurrencyLimit(concurrencyLimit);
	}

	/**
	 * Return the maximum number of parallel accesses allowed.
	 * 
	 * <p> 返回允许的最大并行访问次数。
	 * 
	 */
	public final int getConcurrencyLimit() {
		return this.concurrencyThrottle.getConcurrencyLimit();
	}

	/**
	 * Return whether this throttle is currently active.
	 * 
	 * <p> 返回此油门当前是否有效。
	 * 
	 * @return {@code true} if the concurrency limit for this instance is active
	 * 
	 * <p> 如果此实例的并发限制处于活动状态，则为true
	 * 
	 * @see #getConcurrencyLimit()
	 * @see #setConcurrencyLimit
	 */
	public final boolean isThrottleActive() {
		return this.concurrencyThrottle.isThrottleActive();
	}


	/**
	 * Executes the given task, within a concurrency throttle
	 * if configured (through the superclass's settings).
	 * 
	 * <p> 如果配置（通过超类的设置），则在并发限制内执行给定任务。
	 * 
	 * @see #doExecute(Runnable)
	 */
	public void execute(Runnable task) {
		execute(task, TIMEOUT_INDEFINITE);
	}

	/**
	 * Executes the given task, within a concurrency throttle
	 * if configured (through the superclass's settings).
	 * 
	 * <p> 如果配置（通过超类的设置），则在并发限制内执行给定任务。
	 * 
	 * <p>Executes urgent tasks (with 'immediate' timeout) directly,
	 * bypassing the concurrency throttle (if active). All other
	 * tasks are subject to throttling.
	 * 
	 * <p> 直接执行紧急任务（具有'立即'超时），绕过并发限制（如果激活）。 所有其他任务都受到限制。
	 * 
	 * @see #TIMEOUT_IMMEDIATE
	 * @see #doExecute(Runnable)
	 */
	public void execute(Runnable task, long startTimeout) {
		Assert.notNull(task, "Runnable must not be null");
		if (isThrottleActive() && startTimeout > TIMEOUT_IMMEDIATE) {
			this.concurrencyThrottle.beforeAccess();
			doExecute(new ConcurrencyThrottlingRunnable(task));
		}
		else {
			doExecute(task);
		}
	}

	public Future<?> submit(Runnable task) {
		FutureTask<Object> future = new FutureTask<Object>(task, null);
		execute(future, TIMEOUT_INDEFINITE);
		return future;
	}

	public <T> Future<T> submit(Callable<T> task) {
		FutureTask<T> future = new FutureTask<T>(task);
		execute(future, TIMEOUT_INDEFINITE);
		return future;
	}

	/**
	 * Template method for the actual execution of a task.
	 * 
	 * <p> 用于实际执行任务的模板方法。
	 * 
	 * <p>The default implementation creates a new Thread and starts it.
	 * 
	 * <p> 默认实现创建一个新的Thread并启动它。
	 * 
	 * @param task the Runnable to execute - 要运行的Runnable
	 * @see #setThreadFactory
	 * @see #createThread
	 * @see java.lang.Thread#start()
	 */
	protected void doExecute(Runnable task) {
		Thread thread = (this.threadFactory != null ? this.threadFactory.newThread(task) : createThread(task));
		thread.start();
	}


	/**
	 * Subclass of the general ConcurrencyThrottleSupport class,
	 * making {@code beforeAccess()} and {@code afterAccess()}
	 * visible to the surrounding class.
	 * 
	 * <p> 一般ConcurrencyThrottleSupport类的子类，使beforeAccess（）和afterAccess（）对周围的类可见。
	 * 
	 */
	private static class ConcurrencyThrottleAdapter extends ConcurrencyThrottleSupport {

		@Override
		protected void beforeAccess() {
			super.beforeAccess();
		}

		@Override
		protected void afterAccess() {
			super.afterAccess();
		}
	}


	/**
	 * This Runnable calls {@code afterAccess()} after the
	 * target Runnable has finished its execution.
	 * 
	 * <p> 在目标Runnable完成执行后，此Runnable调用afterAccess（）。
	 * 
	 */
	private class ConcurrencyThrottlingRunnable implements Runnable {

		private final Runnable target;

		public ConcurrencyThrottlingRunnable(Runnable target) {
			this.target = target;
		}

		public void run() {
			try {
				this.target.run();
			}
			finally {
				concurrencyThrottle.afterAccess();
			}
		}
	}

}
