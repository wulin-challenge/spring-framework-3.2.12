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

package org.springframework.aop.framework;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * Convenience superclass for configuration used in creating proxies,
 * to ensure that all proxy creators have consistent properties.
 * 
 * <p> 用于创建代理的配置的便捷超类，以确保所有代理创建者具有一致的属性。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AdvisedSupport
 */
public class ProxyConfig implements Serializable {

	/** use serialVersionUID from Spring 1.2 for interoperability */
	/** 使用Spring 1.2中的serialVersionUID实现互操作性 */
	private static final long serialVersionUID = -8409359707199703185L;


	private boolean proxyTargetClass = false;

	//优化
	private boolean optimize = false;

	//不透明的
	boolean opaque = false;

	boolean exposeProxy = false;

	//冻结
	private boolean frozen = false;


	/**
	 * Set whether to proxy the target class directly, instead of just proxying
	 * specific interfaces. Default is "false".
	 * 
	 * <p> 设置是否直接代理目标类，而不是仅代理特定的接口。 默认为“false”。
	 * 
	 * <p>Set this to "true" to force proxying for the TargetSource's exposed
	 * target class. If that target class is an interface, a JDK proxy will be
	 * created for the given interface. If that target class is any other class,
	 * a CGLIB proxy will be created for the given class.
	 * 
	 * <p> 将其设置为“true”以强制代理TargetSource的公开目标类。 如果该目标类是接口，则将为给定接口创建JDK代理。
	 *  如果该目标类是任何其他类，则将为给定类创建CGLIB代理。
	 *  
	 * <p>Note: Depending on the configuration of the concrete proxy factory,
	 * the proxy-target-class behavior will also be applied if no interfaces
	 * have been specified (and no interface autodetection is activated).
	 * 
	 * <p> 注意：根据具体代理工厂的配置，如果未指定任何接口（并且未激活任何接口自动检测），则还将应用proxy-target-class行为。
	 * 
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	/**
	 * Return whether to proxy the target class directly as well as any interfaces.
	 * 
	 * <p> 返回是否直接代理目标类以及任何接口。
	 */
	public boolean isProxyTargetClass() {
		return this.proxyTargetClass;
	}

	/**
	 * Set whether proxies should perform aggressive optimizations.
	 * The exact meaning of "aggressive optimizations" will differ
	 * between proxies, but there is usually some tradeoff.
	 * Default is "false".
	 * 
	 * <p> 设置代理是否应执行积极优化。 代理人之间“积极优化”的确切含义会有所不同，但通常会有一些权衡。 默认为“false”。
	 * 
	 * <p>For example, optimization will usually mean that advice changes won't
	 * take effect after a proxy has been created. For this reason, optimization
	 * is disabled by default. An optimize value of "true" may be ignored
	 * if other settings preclude optimization: for example, if "exposeProxy"
	 * is set to "true" and that's not compatible with the optimization.
	 * 
	 * <p> 例如，优化通常意味着在创建代理后，建议更改不会生效。 因此，默认情况下禁用优化。 如果其他设置阻止优化，
	 * 则可以忽略优化值“true”：例如，如果“exposeProxy”设置为“true”并且与优化不兼容。
	 * 
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	/**
	 * Return whether proxies should perform aggressive optimizations.
	 * 
	 * <p> 返回代理是否应执行积极的优化。
	 */
	public boolean isOptimize() {
		return this.optimize;
	}

	/**
	 * Set whether proxies created by this configuration should be prevented
	 * from being cast to {@link Advised} to query proxy status.
	 * 
	 * <p> 设置是否应阻止将此配置创建的代理强制转换为建议查询代理状态。
	 * 
	 * <p>Default is "false", meaning that any AOP proxy can be cast to
	 * {@link Advised}.
	 * 
	 * <p> 默认值为“false”，表示任何AOP代理都可以转换为Advised。
	 */
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	/**
	 * Return whether proxies created by this configuration should be
	 * prevented from being cast to {@link Advised}.
	 * 
	 * <p> 返回是否应阻止此配置创建的代理转换为Advised。
	 */
	public boolean isOpaque() {
		return this.opaque;
	}

	/**
	 * Set whether the proxy should be exposed by the AOP framework as a
	 * ThreadLocal for retrieval via the AopContext class. This is useful
	 * if an advised object needs to call another advised method on itself.
	 * (If it uses {@code this}, the invocation will not be advised).
	 * 
	 * <p> 设置代理是否应该由AOP框架作为ThreadLocal公开，以便通过AopContext类进行检索。 
	 * 如果建议的对象需要自己调用另一个建议的方法，这将非常有用。 （如果使用它，则不会建议调用）。
	 * 
	 * <p>Default is "false", in order to avoid unnecessary extra interception.
	 * This means that no guarantees are provided that AopContext access will
	 * work consistently within any method of the advised object.
	 * 
	 * <p> 默认为“false”，以避免不必要的额外拦截。 这意味着不提供AopContext访问将在建议对象的任何方法中一致地工作的保证。
	 */
	public void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}

	/**
	 * Return whether the AOP proxy will expose the AOP proxy for
	 * each invocation.
	 * 
	 * <p> 返回AOP代理是否将为每次调用公开AOP代理。
	 */
	public boolean isExposeProxy() {
		return this.exposeProxy;
	}

	/**
	 * Set whether this config should be frozen.
	 * 
	 * <p> 设置是否应冻结此配置。
	 * 
	 * <p>When a config is frozen, no advice changes can be made. This is
	 * useful for optimization, and useful when we don't want callers to
	 * be able to manipulate configuration after casting to Advised.
	 * 
	 * <p> 当配置被冻结时，不能进行任何建议更改。 这对于优化很有用，当我们不希望调用者在转换为Advised之后能够操作配置时非常有用。
	 * 
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	/**
	 * Return whether the config is frozen, and no advice changes can be made.
	 * 
	 * <p> 返回是否冻结配置，并且不能进行任何建议更改。
	 */
	public boolean isFrozen() {
		return this.frozen;
	}


	/**
	 * Copy configuration from the other config object.
	 * 
	 * <p> 从其他配置对象复制配置。
	 * 
	 * @param other object to copy configuration from - 从中复制配置的对象
	 */
	public void copyFrom(ProxyConfig other) {
		Assert.notNull(other, "Other ProxyConfig object must not be null");
		this.proxyTargetClass = other.proxyTargetClass;
		this.optimize = other.optimize;
		this.exposeProxy = other.exposeProxy;
		this.frozen = other.frozen;
		this.opaque = other.opaque;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("proxyTargetClass=").append(this.proxyTargetClass).append("; ");
		sb.append("optimize=").append(this.optimize).append("; ");
		sb.append("opaque=").append(this.opaque).append("; ");
		sb.append("exposeProxy=").append(this.exposeProxy).append("; ");
		sb.append("frozen=").append(this.frozen);
		return sb.toString();
	}

}
