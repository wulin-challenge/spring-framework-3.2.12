/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.core;

/**
 * Extension of the {@link Ordered} interface, expressing a 'priority'
 * ordering: Order values expressed by PriorityOrdered objects always
 * apply before order values of 'plain' Ordered values.
 * 
 * <p> Ordered接口的扩展，表示“优先级”排序：PriorityOrdered对象表示的顺序值始终在“plain”有序值的顺序值之前应用。
 *
 * <p>This is primarily a special-purpose interface, used for objects
 * where it is particularly important to determine 'prioritized'
 * objects first, without even obtaining the remaining objects.
 * A typical example: Prioritized post-processors in a Spring
 * {@link org.springframework.context.ApplicationContext}.
 * 
 * <p> 这主要是一个专用接口，用于首先确定“优先级”对象尤为重要的对象，甚至不需要获取剩余的对象。 
 * 一个典型的例子：Spring org.springframework.context.ApplicationContext中的优先级后处理器。
 *
 * <p>Note: PriorityOrdered post-processor beans are initialized in
 * a special phase, ahead of other post-processor beans. This subtly
 * affects their autowiring behavior: They will only be autowired against
 * beans which do not require eager initialization for type matching.
 * 
 * <p> 注意：PriorityOrdered后处理器bean在特殊阶段初始化，优先于其他后处理器bean。 
 * 这会巧妙地影响它们的自动装配行为：它们只会针对不需要急切初始化类型匹配的bean进行自动装配。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.beans.factory.config.PropertyOverrideConfigurer
 * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
 */
public interface PriorityOrdered extends Ordered {

}
