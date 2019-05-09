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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Interface used by the {@link DefaultBeanDefinitionDocumentReader} to handle custom,
 * top-level (directly under {@code <beans/>}) tags.
 * 
 * <p>DefaultBeanDefinitionDocumentReader用于处理自定义顶级（直接在<beans />下）标记的接口。
 *
 * <p>Implementations are free to turn the metadata in the custom tag into as many
 * {@link BeanDefinition BeanDefinitions} as required.
 * 
 * <p>实现可以自由地将自定义标记中的元数据转换为所需数量的BeanDefinition。
 *
 * <p>The parser locates a {@link BeanDefinitionParser} from the associated
 * {@link NamespaceHandler} for the namespace in which the custom tag resides.
 * 
 * <p>解析器从关联的NamespaceHandler中找到BeanDefinitionParser，用于自定义标记所在的命名空间。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see AbstractBeanDefinitionParser
 */
public interface BeanDefinitionParser {

	/**
	 * Parse the specified {@link Element} and register the resulting
	 * {@link BeanDefinition BeanDefinition(s)} with the
	 * {@link org.springframework.beans.factory.xml.ParserContext#getRegistry() BeanDefinitionRegistry}
	 * embedded in the supplied {@link ParserContext}.
	 * 
	 * <p>解析指定的Element并使用提供的ParserContext中嵌入的BeanDefinitionRegistry注册生成的BeanDefinition。
	 * 
	 * <p>Implementations must return the primary {@link BeanDefinition} that results
	 * from the parse if they will ever be used in a nested fashion (for example as
	 * an inner tag in a {@code <property/>} tag). Implementations may return
	 * {@code null} if they will <strong>not</strong> be used in a nested fashion.
	 * 
	 * <p>如果它们将以嵌套方式使用（例如作为<property />标记中的内部标记），则实现必须返回由解析产生的主BeanDefinition。 
	 * 如果实现不以嵌套方式使用，则实现可能返回null。
	 * 
	 * @param element the element that is to be parsed into one or more {@link BeanDefinition BeanDefinitions}
	 * 
	 * <p>要解析为一个或多个BeanDefinitions的元素
	 * 
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 * provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * 
	 * <p>封装解析过程当前状态的对象; 提供对org.springframework.beans.factory.support.BeanDefinitionRegistry的访问
	 * 
	 * @return the primary {@link BeanDefinition} - 主BeanDefinition
	 */
	BeanDefinition parse(Element element, ParserContext parserContext);

}
