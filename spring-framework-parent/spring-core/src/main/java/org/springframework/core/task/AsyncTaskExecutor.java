/*
 * Copyright 2002-2013 the original author or authors.
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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Extended interface for asynchronous {@link TaskExecutor} implementations,
 * offering an overloaded {@link #execute(Runnable, long)} variant with a start
 * timeout parameter as well support for {@link java.util.concurrent.Callable}.
 * 
 * <p> 异步TaskExecutor实现的扩展接口，提供带有start timeout参数的重载执行（Runnable，long）
 * 变体以及对java.util.concurrent.Callable的支持。
 *
 * <p>Note: The {@link java.util.concurrent.Executors} class includes a set of
 * methods that can convert some other common closure-like objects, for example,
 * {@link java.security.PrivilegedAction} to {@link Callable} before executing them.
 * 
 * <p> 注意：java.util.concurrent.Executors类包含一组方法，这些方法可以在执行它们之前将一些其他常见的类似
 * 闭包的对象（例如，java.security.PrivilegedAction）转换为Callable。
 *
 * <p>Implementing this interface also indicates that the {@link #execute(Runnable)}
 * method will not execute its Runnable in the caller's thread but rather
 * asynchronously in some other thread.
 * 
 * <p> 实现此接口还表明execute（Runnable）方法不会在调用者的线程中执行其Runnable，而是在其他某个线程中异步执行。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see SimpleAsyncTaskExecutor
 * @see org.springframework.scheduling.SchedulingTaskExecutor
 * @see java.util.concurrent.Callable
 * @see java.util.concurrent.Executors
 */
public interface AsyncTaskExecutor extends TaskExecutor {

	/** Constant that indicates immediate execution */
	/** 表示立即执行的常量 */
	long TIMEOUT_IMMEDIATE = 0;

	/** Constant that indicates no time limit */
	/** 表示没有时间限制的常量 */
	long TIMEOUT_INDEFINITE = Long.MAX_VALUE;


	/**
	 * Execute the given {@code task}.
	 * 
	 * <p> 执行给定的任务。
	 * 
	 * @param task the {@code Runnable} to execute (never {@code null}) - Runnable执行（永不为null）
	 * @param startTimeout the time duration (milliseconds) within which the task is
	 * supposed to start. This is intended as a hint to the executor, allowing for
	 * preferred handling of immediate tasks. Typical values are {@link #TIMEOUT_IMMEDIATE}
	 * or {@link #TIMEOUT_INDEFINITE} (the default as used by {@link #execute(Runnable)}).
	 * 
	 * <p> 任务应该开始的持续时间（毫秒）。 这旨在作为执行者的提示，允许对即时任务进行首选处理。 
	 * 典型值为TIMEOUT_IMMEDIATE或TIMEOUT_INDEFINITE（执行（Runnable）使用的默认值）。
	 * 
	 * @throws TaskTimeoutException in case of the task being rejected because
	 * of the timeout (i.e. it cannot be started in time)
	 * 
	 * <p> 如果任务因超时而被拒绝（即无法及时启动）
	 * 
	 * @throws TaskRejectedException if the given task was not accepted
	 * 
	 * <p> 如果给定的任务未被接受
	 * 
	 */
	void execute(Runnable task, long startTimeout);

	/**
	 * Submit a Runnable task for execution, receiving a Future representing that task.
	 * The Future will return a {@code null} result upon completion.
	 * 
	 * <p> 提交Runnable任务以执行，接收表示该任务的Future。 Future将在完成后返回null结果。
	 * 
	 * @param task the {@code Runnable} to execute (never {@code null})
	 * 
	 * <p> Runnable执行（永不为null）
	 * 
	 * @return a Future representing pending completion of the task - 表示未完成任务的Future
	 * @throws TaskRejectedException if the given task was not accepted - 如果给定的任务未被接受
	 * @since 3.0
	 */
	Future<?> submit(Runnable task);

	/**
	 * Submit a Callable task for execution, receiving a Future representing that task.
	 * The Future will return the Callable's result upon completion.
	 * 
	 * <p> 提交可执行的Callable任务，接收表示该任务的Future。 Future将在完成后返回Callable的结果。
	 * 
	 * @param task the {@code Callable} to execute (never {@code null}) - 要执行的Callable（永不为null）
	 * @return a Future representing pending completion of the task - 表示未完成任务的Future
	 * @throws TaskRejectedException if the given task was not accepted - 如果给定的任务未被接受
	 * @since 3.0
	 */
	<T> Future<T> submit(Callable<T> task);

}
