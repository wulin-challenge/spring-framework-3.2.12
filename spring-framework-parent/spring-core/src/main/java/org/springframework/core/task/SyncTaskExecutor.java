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

import org.springframework.util.Assert;

/**
 * {@link TaskExecutor} implementation that executes each task <i>synchronously</i>
 * in the calling thread.
 * 
 * <p> TaskExecutor实现，在调用线程中同步执行每个任务。
 *
 * <p>Mainly intended for testing scenarios.
 * 
 * <p> 主要用于测试场景。
 *
 * <p>Execution in the calling thread does have the advantage of participating
 * in it's thread context, for example the thread context class loader or the
 * thread's current transaction association. That said, in many cases,
 * asynchronous execution will be preferable: choose an asynchronous
 * {@code TaskExecutor} instead for such scenarios.
 * 
 * <p> 调用线程中的执行确实具有参与其线程上下文的优点，例如线程上下文类加载器或线程的当前事务关联。 
 * 也就是说，在许多情况下，异步执行将是更可取的：为这种情况选择异步TaskExecutor。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SimpleAsyncTaskExecutor
 */
@SuppressWarnings("serial")
public class SyncTaskExecutor implements TaskExecutor, Serializable {

	/**
	 * Executes the given {@code task} synchronously, through direct
	 * invocation of it's {@link Runnable#run() run()} method.
	 * 
	 * <p> 通过直接调用run（）方法同步执行给定任务。
	 * 
	 * @throws IllegalArgumentException if the given {@code task} is {@code null} - 如果给定的任务为null
	 */
	public void execute(Runnable task) {
		Assert.notNull(task, "Runnable must not be null");
		task.run();
	}

}
