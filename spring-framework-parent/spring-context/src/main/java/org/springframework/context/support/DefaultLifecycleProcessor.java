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

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.Lifecycle;
import org.springframework.context.LifecycleProcessor;
import org.springframework.context.Phased;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

/**
 * Default implementation of the {@link LifecycleProcessor} strategy.
 * 
 * <p> LifecycleProcessor策略的默认实现。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 3.0
 */
public class DefaultLifecycleProcessor implements LifecycleProcessor, BeanFactoryAware {

	private final Log logger = LogFactory.getLog(getClass());

	private volatile long timeoutPerShutdownPhase = 30000;

	private volatile boolean running;

	private volatile ConfigurableListableBeanFactory beanFactory;


	/**
	 * Specify the maximum time allotted in milliseconds for the shutdown of
	 * any phase (group of SmartLifecycle beans with the same 'phase' value).
	 * The default value is 30 seconds.
	 * 
	 * <p> 指定关闭任何阶段（具有相同“阶段”值的SmartLifecycle bean组）分配的最长时间（以毫秒为单位）。 默认值为30秒。
	 */
	public void setTimeoutPerShutdownPhase(long timeoutPerShutdownPhase) {
		this.timeoutPerShutdownPhase = timeoutPerShutdownPhase;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory);
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}


	// Lifecycle implementation
	// Lifecycle 接口的实现

	/**
	 * Start all registered beans that implement Lifecycle and are
	 * <i>not</i> already running. Any bean that implements SmartLifecycle
	 * will be started within its 'phase', and all phases will be ordered
	 * from lowest to highest value. All beans that do not implement
	 * SmartLifecycle will be started in the default phase 0. A bean
	 * declared as a dependency of another bean will be started before
	 * the dependent bean regardless of the declared phase.
	 * 
	 * <p> 启动所有实现Lifecycle且尚未运行的已注册Bean。 任何实现SmartLifecycle的bean
	 * 都将在其“阶段”内启动，并且所有阶段都将从最低值到最高值进行排序。 所有未实现
	 * SmartLifecycle的bean都将在默认阶段0中启动。声明为另一个bean的依赖项的bean将在依赖bean之前启动，
	 * 而不管声明的阶段如何。
	 */
	public void start() {
		startBeans(false);
		this.running = true;
	}

	/**
	 * Stop all registered beans that implement Lifecycle and <i>are</i>
	 * currently running. Any bean that implements SmartLifecycle
	 * will be stopped within its 'phase', and all phases will be ordered
	 * from highest to lowest value. All beans that do not implement
	 * SmartLifecycle will be stopped in the default phase 0. A bean
	 * declared as dependent on another bean will be stopped before
	 * the dependency bean regardless of the declared phase.
	 * 
	 * <p> 停止所有实现Lifecycle并且当前正在运行的已注册Bean。 任何实现SmartLifecycle的
	 * bean都将在其“阶段”内停止，并且所有阶段将从最高值到最低值进行排序。 所有未实现SmartLifecycle的
	 * bean都将在默认阶段0停止。声明为依赖于另一个bean的bean将在依赖关系bean之前停止，而不管声明的阶段如何。
	 */
	public void stop() {
		stopBeans();
		this.running = false;
	}

	public void onRefresh() {
		startBeans(true);
		this.running = true;
	}

	public void onClose() {
		stopBeans();
		this.running = false;
	}

	public boolean isRunning() {
		return this.running;
	}


	// internal helpers
	// 内部帮助者

	private void startBeans(boolean autoStartupOnly) {
		Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
		Map<Integer, LifecycleGroup> phases = new HashMap<Integer, LifecycleGroup>();
		for (Map.Entry<String, ? extends Lifecycle> entry : lifecycleBeans.entrySet()) {
			Lifecycle bean = entry.getValue();
			if (!autoStartupOnly || (bean instanceof SmartLifecycle && ((SmartLifecycle) bean).isAutoStartup())) {
				int phase = getPhase(bean);
				LifecycleGroup group = phases.get(phase);
				if (group == null) {
					group = new LifecycleGroup(phase, this.timeoutPerShutdownPhase, lifecycleBeans, autoStartupOnly);
					phases.put(phase, group);
				}
				group.add(entry.getKey(), bean);
			}
		}
		if (phases.size() > 0) {
			List<Integer> keys = new ArrayList<Integer>(phases.keySet());
			Collections.sort(keys);
			for (Integer key : keys) {
				phases.get(key).start();
			}
		}
	}

	/**
	 * Start the specified bean as part of the given set of Lifecycle beans,
	 * making sure that any beans that it depends on are started first.
	 * 
	 * <p> 将指定的bean作为给定生命周期bean集的一部分启动，确保首先启动它所依赖的任何bean。
	 * 
	 * @param lifecycleBeans Map with bean name as key and Lifecycle instance as value
	 * 
	 * <p> 将bean名称作为键映射，将Lifecycle实例作为值映射
	 * 
	 * @param beanName the name of the bean to start - 要启动的bean的名称
	 */
	private void doStart(Map<String, ? extends Lifecycle> lifecycleBeans, String beanName, boolean autoStartupOnly) {
		Lifecycle bean = lifecycleBeans.remove(beanName);
		if (bean != null && !this.equals(bean)) {
			String[] dependenciesForBean = this.beanFactory.getDependenciesForBean(beanName);
			for (String dependency : dependenciesForBean) {
				doStart(lifecycleBeans, dependency, autoStartupOnly);
			}
			if (!bean.isRunning() &&
					(!autoStartupOnly || !(bean instanceof SmartLifecycle) || ((SmartLifecycle) bean).isAutoStartup())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Starting bean '" + beanName + "' of type [" + bean.getClass() + "]");
				}
				try {
					bean.start();
				}
				catch (Throwable ex) {
					throw new ApplicationContextException("Failed to start bean '" + beanName + "'", ex);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully started bean '" + beanName + "'");
				}
			}
		}
	}

	private void stopBeans() {
		Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
		Map<Integer, LifecycleGroup> phases = new HashMap<Integer, LifecycleGroup>();
		for (Map.Entry<String, Lifecycle> entry : lifecycleBeans.entrySet()) {
			Lifecycle bean = entry.getValue();
			int shutdownOrder = getPhase(bean);
			LifecycleGroup group = phases.get(shutdownOrder);
			if (group == null) {
				group = new LifecycleGroup(shutdownOrder, this.timeoutPerShutdownPhase, lifecycleBeans, false);
				phases.put(shutdownOrder, group);
			}
			group.add(entry.getKey(), bean);
		}
		if (phases.size() > 0) {
			List<Integer> keys = new ArrayList<Integer>(phases.keySet());
			Collections.sort(keys, Collections.reverseOrder());
			for (Integer key : keys) {
				phases.get(key).stop();
			}
		}
	}

	/**
	 * Stop the specified bean as part of the given set of Lifecycle beans,
	 * making sure that any beans that depends on it are stopped first.
	 * 
	 * <p> 将指定的bean作为给定生命周期bean集的一部分停止，确保首先停止依赖于它的任何bean。
	 * 
	 * @param lifecycleBeans Map with bean name as key and Lifecycle instance as value
	 * 
	 * <p> 将bean名称作为键映射，将Lifecycle实例作为值映射
	 * 
	 * @param beanName the name of the bean to stop - 要停止的bean的名称
	 */
	private void doStop(Map<String, ? extends Lifecycle> lifecycleBeans, final String beanName,
			final CountDownLatch latch, final Set<String> countDownBeanNames) {

		Lifecycle bean = lifecycleBeans.remove(beanName);
		if (bean != null) {
			String[] dependentBeans = this.beanFactory.getDependentBeans(beanName);
			for (String dependentBean : dependentBeans) {
				doStop(lifecycleBeans, dependentBean, latch, countDownBeanNames);
			}
			try {
				if (bean.isRunning()) {
					if (bean instanceof SmartLifecycle) {
						if (logger.isDebugEnabled()) {
							logger.debug("Asking bean '" + beanName + "' of type [" + bean.getClass() + "] to stop");
						}
						countDownBeanNames.add(beanName);
						((SmartLifecycle) bean).stop(new Runnable() {
							public void run() {
								latch.countDown();
								countDownBeanNames.remove(beanName);
								if (logger.isDebugEnabled()) {
									logger.debug("Bean '" + beanName + "' completed its stop procedure");
								}
							}
						});
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug("Stopping bean '" + beanName + "' of type [" + bean.getClass() + "]");
						}
						bean.stop();
						if (logger.isDebugEnabled()) {
							logger.debug("Successfully stopped bean '" + beanName + "'");
						}
					}
				}
				else if (bean instanceof SmartLifecycle) {
					// don't wait for beans that aren't running
					latch.countDown();
				}
			}
			catch (Throwable ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Failed to stop bean '" + beanName + "'", ex);
				}
			}
		}
	}


	// overridable hooks

	/**
	 * Retrieve all applicable Lifecycle beans: all singletons that have already been created,
	 * as well as all SmartLifecycle beans (even if they are marked as lazy-init).
	 * 
	 * <p> 检索所有适用的Lifecycle bean：已创建的所有单例，以及所有SmartLifecycle bean（即使它们被标记为lazy-init）。
	 * 
	 * @return the Map of applicable beans, with bean names as keys and bean instances as values
	 * 
	 * <p> 适用bean的映射，bean名称为键，bean实例为值
	 * 
	 */
	protected Map<String, Lifecycle> getLifecycleBeans() {
		Map<String, Lifecycle> beans = new LinkedHashMap<String, Lifecycle>();
		String[] beanNames = this.beanFactory.getBeanNamesForType(Lifecycle.class, false, false);
		for (String beanName : beanNames) {
			String beanNameToRegister = BeanFactoryUtils.transformedBeanName(beanName);
			boolean isFactoryBean = this.beanFactory.isFactoryBean(beanNameToRegister);
			String beanNameToCheck = (isFactoryBean ? BeanFactory.FACTORY_BEAN_PREFIX + beanName : beanName);
			if ((this.beanFactory.containsSingleton(beanNameToRegister) &&
					(!isFactoryBean || Lifecycle.class.isAssignableFrom(this.beanFactory.getType(beanNameToCheck)))) ||
					SmartLifecycle.class.isAssignableFrom(this.beanFactory.getType(beanNameToCheck))) {
				Lifecycle bean = this.beanFactory.getBean(beanNameToCheck, Lifecycle.class);
				if (bean != this) {
					beans.put(beanNameToRegister, bean);
				}
			}
		}
		return beans;
	}

	/**
	 * Determine the lifecycle phase of the given bean.
	 * 
	 * <p> 确定给定bean的生命周期阶段。
	 * 
	 * <p>The default implementation checks for the {@link Phased} interface.
	 * Can be overridden to apply other/further policies.
	 * 
	 * <p> 默认实现检查Phased接口。 可以覆盖以应用其他/进一步的策略。
	 * 
	 * @param bean the bean to introspect - 内省的bean
	 * @return the phase an an integer value. The suggested default is 0.
	 * 
	 * <p> 相位是一个整数值。 建议的默认值为0。
	 * 
	 * @see Phased
	 * @see SmartLifecycle
	 */
	protected int getPhase(Lifecycle bean) {
		return (bean instanceof Phased ? ((Phased) bean).getPhase() : 0);
	}


	/**
	 * Helper class for maintaining a group of Lifecycle beans that should be started
	 * and stopped together based on their 'phase' value (or the default value of 0).
	 * 
	 * <p> Helper类，用于维护一组Lifecycle bean，这些bean应根据其“phase”值（或默认值0）一起启动和停止。
	 * 
	 */
	private class LifecycleGroup {

		private final List<LifecycleGroupMember> members = new ArrayList<LifecycleGroupMember>();

		private final int phase;

		private final long timeout;

		private final Map<String, ? extends Lifecycle> lifecycleBeans;

		private final boolean autoStartupOnly;

		private volatile int smartMemberCount;

		public LifecycleGroup(int phase, long timeout, Map<String, ? extends Lifecycle> lifecycleBeans, boolean autoStartupOnly) {
			this.phase = phase;
			this.timeout = timeout;
			this.lifecycleBeans = lifecycleBeans;
			this.autoStartupOnly = autoStartupOnly;
		}

		public void add(String name, Lifecycle bean) {
			if (bean instanceof SmartLifecycle) {
				this.smartMemberCount++;
			}
			this.members.add(new LifecycleGroupMember(name, bean));
		}

		public void start() {
			if (this.members.isEmpty()) {
				return;
			}
			if (logger.isInfoEnabled()) {
				logger.info("Starting beans in phase " + this.phase);
			}
			Collections.sort(this.members);
			for (LifecycleGroupMember member : this.members) {
				if (this.lifecycleBeans.containsKey(member.name)) {
					doStart(this.lifecycleBeans, member.name, this.autoStartupOnly);
				}
			}
		}

		public void stop() {
			if (this.members.isEmpty()) {
				return;
			}
			if (logger.isInfoEnabled()) {
				logger.info("Stopping beans in phase " + this.phase);
			}
			Collections.sort(this.members, Collections.reverseOrder());
			CountDownLatch latch = new CountDownLatch(this.smartMemberCount);
			Set<String> countDownBeanNames = Collections.synchronizedSet(new LinkedHashSet<String>());
			for (LifecycleGroupMember member : this.members) {
				if (this.lifecycleBeans.containsKey(member.name)) {
					doStop(this.lifecycleBeans, member.name, latch, countDownBeanNames);
				}
				else if (member.bean instanceof SmartLifecycle) {
					// already removed, must have been a dependent
					latch.countDown();
				}
			}
			try {
				latch.await(this.timeout, TimeUnit.MILLISECONDS);
				if (latch.getCount() > 0 && !countDownBeanNames.isEmpty() && logger.isWarnEnabled()) {
					logger.warn("Failed to shut down " + countDownBeanNames.size() + " bean" +
							(countDownBeanNames.size() > 1 ? "s" : "") + " with phase value " +
							this.phase + " within timeout of " + this.timeout + ": " + countDownBeanNames);
				}
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}


	/**
	 * Adapts the Comparable interface onto the lifecycle phase model.
	 * 
	 * <p> 使Comparable接口适应生命周期阶段模型。
	 */
	private class LifecycleGroupMember implements Comparable<LifecycleGroupMember> {

		private final String name;

		private final Lifecycle bean;

		LifecycleGroupMember(String name, Lifecycle bean) {
			this.name = name;
			this.bean = bean;
		}

		public int compareTo(LifecycleGroupMember other) {
			int thisOrder = getPhase(this.bean);
			int otherOrder = getPhase(other.bean);
			return (thisOrder == otherOrder ? 0 : (thisOrder < otherOrder) ? -1 : 1);
		}
	}

}
