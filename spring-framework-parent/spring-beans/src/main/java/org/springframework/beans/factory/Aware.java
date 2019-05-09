/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.beans.factory;

/**
 * Marker superinterface indicating that a bean is eligible to be
 * notified by the Spring container of a particular framework object
 * through a callback-style method. Actual method signature is
 * determined by individual subinterfaces, but should typically
 * consist of just one void-returning method that accepts a single
 * argument.
 * 
 * <p>标记超级接口，指示bean有资格通过回调样式方法由Spring容器通知特定框架对象。 
 * 实际方法签名由各个子接口确定，但通常应该只包含一个接受单个参数的void返回方法。
 *
 * <p>Note that merely implementing {@link Aware} provides no default
 * functionality. Rather, processing must be done explicitly, for example
 * in a {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor}.
 * Refer to {@link org.springframework.context.support.ApplicationContextAwareProcessor}
 * and {@link org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory}
 * for examples of processing {@code *Aware} interface callbacks.
 * 
 * <p>请注意，仅实现Aware不提供默认功能。 相反，必须明确地进行处理，例如在BeanPostProcessor中。 有关处理* Aware接口回调的示例，
 * 请参阅org.springframework.context.support.ApplicationContextAwareProcessor
 * 和org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory。
 *
 * @author Chris Beams
 * @since 3.1
 */
public interface Aware {

}
