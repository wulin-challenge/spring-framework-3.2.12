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

/**
 * Interface to be implemented by factories that are able to create
 * AOP proxies based on {@link AdvisedSupport} configuration objects.
 * 
 * <p> 由能够基于AdvisedSupport配置对象创建AOP代理的工厂实现的接口。
 *
 * <p>Proxies should observe the following contract:
 * 
 * <p> 代理人应遵守以下合同：
 * 
 * <ul>
 * <li>They should implement all interfaces that the configuration
 * indicates should be proxied.
 * 
 * <li> 它们应实现配置指示应代理的所有接口。
 * 
 * <li>They should implement the {@link Advised} interface.
 * 
 * <li> 他们应该实施Advised界面。
 * 
 * <li>They should implement the equals method to compare proxied
 * interfaces, advice, and target.
 * 
 * <li> 他们应该实现equals方法来比较代理接口，建议和目标。
 * 
 * <li>They should be serializable if all advisors and target
 * are serializable.
 * 
 * <li> 如果所有顾问程序和目标都是可序列化的，则它们应该是可序列化的。
 * 
 * <li>They should be thread-safe if advisors and target
 * are thread-safe.
 * 
 * <li> 如果顾问程序和目标是线程安全的，它们应该是线程安全的。
 * </ul>
 *
 * <p>Proxies may or may not allow advice changes to be made.
 * If they do not permit advice changes (for example, because
 * the configuration was frozen) a proxy should throw an
 * {@link AopConfigException} on an attempted advice change.
 * 
 * <li> 代理可能会也可能不会允许进行建议更改。 如果他们不允许更改建议（例如，因为配置被冻结），
 * 则代理应该对尝试的建议更改抛出AopConfigException。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface AopProxyFactory {

	/**
	 * Create an {@link AopProxy} for the given AOP configuration.
	 * 
	 * <p> 为给定的AOP配置创建AopProxy。
	 * 
	 * @param config the AOP configuration in the form of an
	 * AdvisedSupport object
	 * 
	 * <p> Advised配置以AdvisedSupport对象的形式
	 * 
	 * @return the corresponding AOP proxy - 相应的AOP代理
	 * @throws AopConfigException if the configuration is invalid
	 * 
	 * <p> 如果配置无效
	 * 
	 */
	AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;

}
