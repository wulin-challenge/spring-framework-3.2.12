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

package org.springframework.beans.factory.parsing;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;

/**
 * Interface that describes the logical view of a set of {@link BeanDefinition BeanDefinitions}
 * and {@link BeanReference BeanReferences} as presented in some configuration context.
 * 
 * <p> 描述一些配置上下文中显示的一组BeanDefinitions和BeanReferences的逻辑视图的接口。
 *
 * <p>With the introduction of {@link org.springframework.beans.factory.xml.NamespaceHandler pluggable custom XML tags},
 * it is now possible for a single logical configuration entity, in this case an XML tag, to
 * create multiple {@link BeanDefinition BeanDefinitions} and {@link BeanReference RuntimeBeanReferences}
 * in order to provide more succinct configuration and greater convenience to end users. As such, it can
 * no longer be assumed that each configuration entity (e.g. XML tag) maps to one {@link BeanDefinition}.
 * For tool vendors and other users who wish to present visualization or support for configuring Spring
 * applications it is important that there is some mechanism in place to tie the {@link BeanDefinition BeanDefinitions}
 * in the {@link org.springframework.beans.factory.BeanFactory} back to the configuration data in a way
 * that has concrete meaning to the end user. As such, {@link org.springframework.beans.factory.xml.NamespaceHandler}
 * implementations are able to publish events in the form of a {@code ComponentDefinition} for each
 * logical entity being configured. Third parties can then {@link ReaderEventListener subscribe to these events},
 * allowing for a user-centric view of the bean metadata.
 * 
 * <p> 通过引入可插入的自定义XML标记，现在可以为单个逻辑配置实体（在本例中为XML标记）创建多个BeanDefinitions和RuntimeBeanReferences，
 * 以便为最终用户提供更简洁的配置和更大的便利。因此，不再假设每个配置实体（例如XML标签）映射到一个BeanDefinition。对于希望提供可视化或支持配
 * 置Spring应用程序的工具供应商和其他用户，有一些机制可以将org.springframework.beans.factory.BeanFactory中的BeanDefinitions以
 * 某种方式绑定回配置数据这对最终用户具有实际意义。因此，org.springframework.beans.factory.xml.NamespaceHandler实现能
 * 够以ComponentDefinition的形式为正在配置的每个逻辑实体发布事件。然后，第三方可以订阅这些事件，从而允许以bean为单位的以用户为中心的视图。
 * 
 *
 * <p>Each {@code ComponentDefinition} has a {@link #getSource source object} which is configuration-specific.
 * In the case of XML-based configuration this is typically the {@link org.w3c.dom.Node} which contains the user
 * supplied configuration information. In addition to this, each {@link BeanDefinition} enclosed in a
 * {@code ComponentDefinition} has its own {@link BeanDefinition#getSource() source object} which may point
 * to a different, more specific, set of configuration data. Beyond this, individual pieces of bean metadata such
 * as the {@link org.springframework.beans.PropertyValue PropertyValues} may also have a source object giving an
 * even greater level of detail. Source object extraction is handled through the
 * {@link SourceExtractor} which can be customized as required.
 * 
 * <p> 每个ComponentDefinition都有一个特定于配置的源对象。对于基于XML的配置，这通常是org.w3c.dom.Node，其中包含用户提供的配置信息。
 * 除此之外，ComponentDefinition中包含的每个BeanDefinition都有自己的源对象，可以指向不同的，更具体的配置数据集。除此之外，诸
 * 如PropertyValues之类的bean元数据也可能具有源对象，从而提供更高级别的细节。源对象提取通过SourceExtractor处理，可以根据需要进行自定义。
 *
 * <p>Whilst direct access to important {@link BeanReference BeanReferences} is provided through
 * {@link #getBeanReferences}, tools may wish to inspect all {@link BeanDefinition BeanDefinitions} to gather
 * the full set of {@link BeanReference BeanReferences}. Implementations are required to provide
 * all {@link BeanReference BeanReferences} that are required to validate the configuration of the
 * overall logical entity as well as those required to provide full user visualisation of the configuration.
 * It is expected that certain {@link BeanReference BeanReferences} will not be important to
 * validation or to the user view of the configuration and as such these may be ommitted. A tool may wish to
 * display any additional {@link BeanReference BeanReferences} sourced through the supplied
 * {@link BeanDefinition BeanDefinitions} but this is not considered to be a typical case.
 * 
 * <p> 虽然通过getBeanReferences直接访问重要的BeanReferences，但工具可能希望检查所有BeanDefinition以收集完整
 * 的BeanReferences集。需要实现来提供验证整个逻辑实体的配置所需的所有BeanReferences，以及提供配置的完整用户可视化所需的那些。
 * 期望某些BeanReferences对于验证或配置的用户视图不重要，因此可以省略这些。工具可能希望显示通过提供的BeanDefinitions获取的任何其
 * 他BeanReferences，但这不被视为典型情况。
 *
 * <p>Tools can determine the important of contained {@link BeanDefinition BeanDefinitions} by checking the
 * {@link BeanDefinition#getRole role identifier}. The role is essentially a hint to the tool as to how
 * important the configuration provider believes a {@link BeanDefinition} is to the end user. It is expected
 * that tools will <strong>not</strong> display all {@link BeanDefinition BeanDefinitions} for a given
 * {@code ComponentDefinition} choosing instead to filter based on the role. Tools may choose to make
 * this filtering user configurable. Particular notice should be given to the
 * {@link BeanDefinition#ROLE_INFRASTRUCTURE INFRASTRUCTURE role identifier}. {@link BeanDefinition BeanDefinitions}
 * classified with this role are completely unimportant to the end user and are required only for
 * internal implementation reasons.
 * 
 * <p> 工具可以通过检查角色标识符来确定包含的BeanDefinitions的重要性。该角色本质上是该工具提示配置提供程序认为BeanDefinition对最终用户的重要性。
 * 预计工具不会显示给定ComponentDefinition的所有BeanDefinition，而是选择基于角色进行过滤。工具可以选择使此过滤用户可配置。
 * 应特别注意INFRASTRUCTURE角色标识符。使用此角色分类的BeanDefinition对最终用户完全不重要，仅出于内部实现原因而需要。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see AbstractComponentDefinition
 * @see CompositeComponentDefinition
 * @see BeanComponentDefinition
 * @see ReaderEventListener#componentRegistered(ComponentDefinition)
 */
public interface ComponentDefinition extends BeanMetadataElement {

	/**
	 * Get the user-visible name of this {@code ComponentDefinition}.
	 * 
	 * <p> 获取此ComponentDefinition的用户可见名称。
	 * 
	 * <p>This should link back directly to the corresponding configuration data
	 * for this component in a given context.
	 * 
	 * <p> 这应该直接链接回给定上下文中该组件的相应配置数据。
	 */
	String getName();

	/**
	 * Return a friendly description of the described component.
	 * 
	 * <p> 返回所描述组件的友好描述。
	 * 
	 * <p>Implementations are encouraged to return the same value from
	 * {@code toString()}.
	 * 
	 * <p> 鼓励实现从toString（）返回相同的值。
	 */
	String getDescription();

	/**
	 * Return the {@link BeanDefinition BeanDefinitions} that were registered
	 * to form this {@code ComponentDefinition}.
	 * 
	 * <p> 返回已注册以形成此ComponentDefinition的BeanDefinition。
	 * 
	 * <p>It should be noted that a {@code ComponentDefinition} may well be related with
	 * other {@link BeanDefinition BeanDefinitions} via {@link BeanReference references},
	 * however these are <strong>not</strong> included as they may be not available immediately.
	 * Important {@link BeanReference BeanReferences} are available from {@link #getBeanReferences()}.
	 * 
	 * <p> 应该注意的是，ComponentDefinition可能通过引用与其他BeanDefinitions相关联，但是这些不包括在内，
	 * 因为它们可能不会立即可用。 重要的BeanReferences可从getBeanReferences（）获得。
	 * 
	 * @return the array of BeanDefinitions, or an empty array if none
	 * 
	 * <p> BeanDefinitions数组，如果没有则为空数组
	 * 
	 */
	BeanDefinition[] getBeanDefinitions();

	/**
	 * Return the {@link BeanDefinition BeanDefinitions} that represent all relevant
	 * inner beans within this component.
	 * 
	 * <p> 返回表示此组件中所有相关内部bean的BeanDefinitions。
	 * 
	 * <p>Other inner beans may exist within the associated {@link BeanDefinition BeanDefinitions},
	 * however these are not considered to be needed for validation or for user visualization.
	 * 
	 * <p> 其他内部bean可能存在于关联的BeanDefinitions中，但是这些不被认为是验证或用户可视化所需的。
	 * 
	 * @return the array of BeanDefinitions, or an empty array if none
	 * 
	 * <p> BeanDefinitions数组，如果没有则为空数组
	 * 
	 */
	BeanDefinition[] getInnerBeanDefinitions();

	/**
	 * Return the set of {@link BeanReference BeanReferences} that are considered
	 * to be important to this {@code ComponentDefinition}.
	 * 
	 * <p> 返回被认为对此ComponentDefinition很重要的BeanReferences集。
	 * 
	 * <p>Other {@link BeanReference BeanReferences} may exist within the associated
	 * {@link BeanDefinition BeanDefinitions}, however these are not considered
	 * to be needed for validation or for user visualization.
	 * 
	 * <p> 其他BeanReferences可能存在于关联的BeanDefinitions中，但是这些不被认为是验证或用户可视化所需的。
	 * 
	 * @return the array of BeanReferences, or an empty array if none
	 * 
	 * <p> BeanReferences数组，如果没有则为空数组
	 * 
	 */
	BeanReference[] getBeanReferences();

}
