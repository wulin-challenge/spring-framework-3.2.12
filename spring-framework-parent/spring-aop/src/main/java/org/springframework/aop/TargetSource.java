/*<
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

package org.springframework.aop;

/**
 * A {@code TargetSource} is used to obtain the current "target" of
 * an AOP invocation, which will be invoked via reflection if no around
 * advice chooses to end the interceptor chain itself.
 * 
 * <p> TargetSource用于获取AOP调用的当前“目标”，如果没有周围的建议选择结束拦截器链本身，则将通过反射调用该目标。
 *
 * <p>If a {@code TargetSource} is "static", it will always return
 * the same target, allowing optimizations in the AOP framework. Dynamic
 * target sources can support pooling, hot swapping, etc.
 * 
 * <p> 如果TargetSource是“静态的”，它将始终返回相同的目标，允许在AOP框架中进行优化。 动态目标源可以支持池，热交换等。
 *
 * <p>Application developers don't usually need to work with
 * {@code TargetSources} directly: this is an AOP framework interface.
 * 
 * <p> 应用程序开发人员通常不需要直接使用TargetSources：这是一个AOP框架接口。
 *
 * @author Rod Johnson
 */
public interface TargetSource extends TargetClassAware {

	/**
	 * Return the type of targets returned by this {@link TargetSource}.
	 * 
	 * <p> 返回此TargetSource返回的目标类型。
	 * 
	 * <p>Can return {@code null}, although certain usages of a
	 * {@code TargetSource} might just work with a predetermined
	 * target class.
	 * 
	 * <p> 可以返回null，尽管TargetSource的某些用法可能只适用于预定的目标类。
	 * 
	 * @return the type of targets returned by this {@link TargetSource}
	 * 
	 * <p> 此TargetSource返回的目标类型
	 */
	Class<?> getTargetClass();

	/**
	 * Will all calls to {@link #getTarget()} return the same object?
	 * 
	 * <p> 是否所有对getTarget（）的调用都返回相同的对象？
	 * 
	 * <p>In that case, there will be no need to invoke
	 * {@link #releaseTarget(Object)}, and the AOP framework can cache
	 * the return value of {@link #getTarget()}.
	 * 
	 * <p> 在这种情况下，不需要调用releaseTarget（Object），AOP框架可以缓存getTarget（）的返回值。
	 * 
	 * @return {@code true} if the target is immutable
	 * 
	 * <p> 如果目标是不可变的，则为true
	 * 
	 * @see #getTarget
	 */
	boolean isStatic();

	/**
	 * Return a target instance. Invoked immediately before the
	 * AOP framework calls the "target" of an AOP method invocation.
	 * 
	 * <p> 返回目标实例。 在AOP框架调用AOP方法调用的“目标”之前立即调用。
	 * 
	 * @return the target object, which contains the joinpoint
	 * 
	 * <p> 目标对象，包含连接点
	 * 
	 * @throws Exception if the target object can't be resolved
	 * 
	 * <p> 如果目标对象无法解析
	 */
	Object getTarget() throws Exception;

	/**
	 * Release the given target object obtained from the
	 * {@link #getTarget()} method.
	 * 
	 * <p> 释放从getTarget（）方法获取的给定目标对象。
	 * 
	 * @param target object obtained from a call to {@link #getTarget()}
	 * 
	 * <p> 通过调用getTarget（）获得的对象
	 * 
	 * @throws Exception if the object can't be released
	 * 
	 * <p> 如果对象无法释放
	 * 
	 */
	void releaseTarget(Object target) throws Exception;

}
