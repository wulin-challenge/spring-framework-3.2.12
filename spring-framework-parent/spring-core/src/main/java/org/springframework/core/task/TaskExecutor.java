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

import java.util.concurrent.Executor;

/**
 * Simple task executor interface that abstracts the execution
 * of a {@link Runnable}.
 * 
 * <p> 简单的任务执行器接口，用于抽象Runnable的执行。
 *
 * <p>Implementations can use all sorts of different execution strategies,
 * such as: synchronous, asynchronous, using a thread pool, and more.
 * 
 * <p> 实现可以使用各种不同的执行策略，例如：同步，异步，使用线程池等。
 *
 * <p>Equivalent to JDK 1.5's {@link java.util.concurrent.Executor}
 * interface; extending it now in Spring 3.0, so that clients may declare
 * a dependency on an Executor and receive any TaskExecutor implementation.
 * This interface remains separate from the standard Executor interface
 * mainly for backwards compatibility with JDK 1.4 in Spring 2.x.
 * 
 * <p> 相当于JDK 1.5的java.util.concurrent.Executor接口; 现在在Spring 3.0中扩展它，
 * 以便客户端可以声明对Executor的依赖并接收任何TaskExecutor实现。 此接口与标准Executor接口保持独立，
 * 主要是为了向后兼容Spring 2.x中的JDK 1.4。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.concurrent.Executor
 */
public interface TaskExecutor extends Executor {

	/**
	 * Execute the given {@code task}.
	 * 
	 * <p> 执行给定的任务。
	 * 
	 * <p>The call might return immediately if the implementation uses
	 * an asynchronous execution strategy, or might block in the case
	 * of synchronous execution.
	 * 
	 * <p> 如果实现使用异步执行策略，则调用可能立即返回，或者在同步执行的情况下可能会阻塞。
	 * 
	 * @param task the {@code Runnable} to execute (never {@code null})
	 * 
	 * <p> Runnable执行（永不为null）
	 * 
	 * @throws TaskRejectedException if the given task was not accepted - 如果给定的任务未被接受
	 */
	void execute(Runnable task);

}
