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

package org.springframework.context;

/**
 * Interface defining methods for start/stop lifecycle control.
 * The typical use case for this is to control asynchronous processing.
 * 
 * <p> 界面定义启动/停止生命周期控制的方法。这种情况的典型用例是控制异步处理。
 *
 * <p>Can be implemented by both components (typically a Spring bean defined in
 * a Spring {@link org.springframework.beans.factory.BeanFactory}) and containers
 * (typically a Spring {@link ApplicationContext}). Containers will propagate
 * start/stop signals to all components that apply.
 * 
 * <p> 可以由两个组件（通常是Spring org.springframework.beans.factory.BeanFactory中定
 * 义的Spring bean）和容器（通常是Spring ApplicationContext）实现。
 * 容器会将启动/停止信号传播到所有适用的组件。
 *
 * <p>Can be used for direct invocations or for management operations via JMX.
 * In the latter case, the {@link org.springframework.jmx.export.MBeanExporter}
 * will typically be defined with an
 * {@link org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler},
 * restricting the visibility of activity-controlled components to the Lifecycle
 * interface.
 * 
 * <p> 可以通过JMX用于直接调用或管理操作。在后一种情况下，org.springframework.jmx.export.MBeanExporter
 * 通常使用org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler定义，
 * 从而限制活动控制组件对Lifecycle接口的可见性。
 *
 * <p>Note that the Lifecycle interface is only supported on <b>top-level singleton beans</b>.
 * On any other component, the Lifecycle interface will remain undetected and hence ignored.
 * Also, note that the extended {@link SmartLifecycle} interface provides more sophisticated
 * integration with the container's startup and shutdown phases.
 * 
 * <p> 请注意，仅在顶级单例bean上支持Lifecycle接口。在任何其他组件上，Lifecycle接口将保持未被检测到，因此将被忽略。
 * 另请注意，扩展的SmartLifecycle接口提供了与容器启动和关闭阶段的更复杂的集成。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SmartLifecycle
 * @see ConfigurableApplicationContext
 * @see org.springframework.jms.listener.AbstractMessageListenerContainer
 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean
 */
public interface Lifecycle {

	/**
	 * Start this component.
	 * Should not throw an exception if the component is already running.
	 * 
	 * <p> 启动此组件。 如果组件已在运行，则不应抛出异常。
	 * 
	 * <p>In the case of a container, this will propagate the start signal
	 * to all components that apply.
	 * 
	 * <p> 对于容器，这会将启动信号传播到所有适用的组件。
	 * 
	 */
	void start();

	/**
	 * Stop this component, typically in a synchronous fashion, such that
	 * the component is fully stopped upon return of this method. Consider
	 * implementing {@link SmartLifecycle} and its {@code stop(Runnable)}
	 * variant in cases where asynchronous stop behavior is necessary.
	 * 
	 * <p> 通常以同步方式停止此组件，以便在返回此方法时组件完全停止。 在需要异步停止行为的情况下，
	 * 请考虑实现SmartLifecycle及其stop（Runnable）变体。
	 * 
	 * <p>Should not throw an exception if the component isn't started yet.
	 * 
	 * <p> 如果组件尚未启动，则不应抛出异常。
	 * 
	 * <p>In the case of a container, this will propagate the stop signal
	 * to all components that apply.
	 * 
	 * <p> 对于容器，这会将停止信号传播到所有适用的组件。
	 * 
	 * @see SmartLifecycle#stop(Runnable)
	 */
	void stop();

	/**
	 * Check whether this component is currently running.
	 * 
	 * <p> 检查此组件当前是否正在运行。
	 * 
	 * <p>In the case of a container, this will return {@code true}
	 * only if <i>all</i> components that apply are currently running.
	 * @return whether the component is currently running
	 * 
	 * <p> 对于容器，仅当应用的所有组件当前正在运行时，才会返回true。
	 * 
	 */
	boolean isRunning();

}
