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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.StringUtils;

/**
 * Abstract {@link BeanDefinitionParser} implementation providing
 * a number of convenience methods and a
 * {@link AbstractBeanDefinitionParser#parseInternal template method}
 * that subclasses must override to provide the actual parsing logic.
 * 
 * <p> 摘要BeanDefinitionParser实现提供了许多便捷方法和子类必须覆盖的模板方法，以提供实际的解析逻辑。
 *
 * <p>Use this {@link BeanDefinitionParser} implementation when you want
 * to parse some arbitrarily complex XML into one or more
 * {@link BeanDefinition BeanDefinitions}. If you just want to parse some
 * XML into a single {@code BeanDefinition}, you may wish to consider
 * the simpler convenience extensions of this class, namely
 * {@link AbstractSingleBeanDefinitionParser} and
 * {@link AbstractSimpleBeanDefinitionParser}.
 * 
 * <p> 当您想要将一些任意复杂的XML解析为一个或多个BeanDefinition时，请使用此BeanDefinitionParser实现。 
 * 如果您只想将一些XML解析为单个BeanDefinition，您可能希望考虑此类的更简单的便利扩展，
 * 即AbstractSingleBeanDefinitionParser和AbstractSimpleBeanDefinitionParser。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @author Dave Syer
 * @since 2.0
 */
public abstract class AbstractBeanDefinitionParser implements BeanDefinitionParser {

	/** Constant for the id attribute */
	/** id属性的常量 */
	public static final String ID_ATTRIBUTE = "id";

	/** Constant for the name attribute */
	/** name属性的常量 */
	public static final String NAME_ATTRIBUTE = "name";

	public final BeanDefinition parse(Element element, ParserContext parserContext) {
		AbstractBeanDefinition definition = parseInternal(element, parserContext);
		if (definition != null && !parserContext.isNested()) {
			try {
				String id = resolveId(element, definition, parserContext);
				if (!StringUtils.hasText(id)) {
					parserContext.getReaderContext().error(
							"Id is required for element '" + parserContext.getDelegate().getLocalName(element)
									+ "' when used as a top-level tag", element);
				}
				String[] aliases = new String[0];
				String name = element.getAttribute(NAME_ATTRIBUTE);
				if (StringUtils.hasLength(name)) {
					aliases = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(name));
				}
				BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, id, aliases);
				registerBeanDefinition(holder, parserContext.getRegistry());
				if (shouldFireEvents()) {
					BeanComponentDefinition componentDefinition = new BeanComponentDefinition(holder);
					postProcessComponentDefinition(componentDefinition);
					parserContext.registerComponent(componentDefinition);
				}
			}
			catch (BeanDefinitionStoreException ex) {
				parserContext.getReaderContext().error(ex.getMessage(), element);
				return null;
			}
		}
		return definition;
	}

	/**
	 * Resolve the ID for the supplied {@link BeanDefinition}.
	 * 
	 * <p> 解析提供的BeanDefinition的ID。
	 * 
	 * <p>When using {@link #shouldGenerateId generation}, a name is generated automatically.
	 * Otherwise, the ID is extracted from the "id" attribute, potentially with a
	 * {@link #shouldGenerateIdAsFallback() fallback} to a generated id.
	 * 
	 * <p> 使用生成时，会自动生成名称。 否则，ID将从“id”属性中提取，可能会回退到生成的id。
	 * 
	 * @param element the element that the bean definition has been built from
	 * 
	 * <p> 构造bean定义的元素
	 * 
	 * @param definition the bean definition to be registered - 定义要注册的bean定义
	 * 
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 * provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * 
	 * <p> 封装解析过程当前状态的对象; 提供对org.springframework.beans.factory.support.BeanDefinitionRegistry的访问
	 * 
	 * @return the resolved id - 已解决的ID
	 * @throws BeanDefinitionStoreException if no unique name could be generated
	 * for the given bean definition
	 * 
	 * <p> 如果没有为给定的bean定义生成唯一名称
	 * 
	 */
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {

		if (shouldGenerateId()) {
			return parserContext.getReaderContext().generateBeanName(definition);
		}
		else {
			String id = element.getAttribute(ID_ATTRIBUTE);
			if (!StringUtils.hasText(id) && shouldGenerateIdAsFallback()) {
				id = parserContext.getReaderContext().generateBeanName(definition);
			}
			return id;
		}
	}

	/**
	 * Register the supplied {@link BeanDefinitionHolder bean} with the supplied
	 * {@link BeanDefinitionRegistry registry}.
	 * 
	 * <p> 使用提供的注册表注册提供的bean。
	 * 
	 * <p>Subclasses can override this method to control whether or not the supplied
	 * {@link BeanDefinitionHolder bean} is actually even registered, or to
	 * register even more beans.
	 * 
	 * <p> 子类可以重写此方法来控制提供的bean是否实际上是甚至已注册，或者是否注册更多的bean。
	 * 
	 * <p>The default implementation registers the supplied {@link BeanDefinitionHolder bean}
	 * with the supplied {@link BeanDefinitionRegistry registry} only if the {@code isNested}
	 * parameter is {@code false}, because one typically does not want inner beans
	 * to be registered as top level beans.
	 * 
	 * <p> 仅当isNested参数为false时，默认实现才会使用提供的注册表注册提供的bean，因为通常不希望将内部bean注册为顶级bean。
	 * 
	 * @param definition the bean definition to be registered - 定义要注册的bean定义
	 * @param registry the registry that the bean is to be registered with - 注册表注册bean的注册表
	 * @see BeanDefinitionReaderUtils#registerBeanDefinition(BeanDefinitionHolder, BeanDefinitionRegistry)
	 */
	protected void registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
		BeanDefinitionReaderUtils.registerBeanDefinition(definition, registry);
	}


	/**
	 * Central template method to actually parse the supplied {@link Element}
	 * into one or more {@link BeanDefinition BeanDefinitions}.
	 * 
	 * <p> 用于将提供的Element实际解析为一个或多个BeanDefinitions的中央模板方法。
	 * 
	 * @param element	the element that is to be parsed into one or more {@link BeanDefinition BeanDefinitions}
	 * 
	 * <p> 要解析为一个或多个BeanDefinitions的元素
	 * 
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 * provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * 
	 * <p> 封装解析过程当前状态的对象; 提供对org.springframework.beans.factory.support.BeanDefinitionRegistry的访问
	 * 
	 * @return the primary {@link BeanDefinition} resulting from the parsing of the supplied {@link Element}
	 * 
	 * <p> 解析所提供的Element产生的主BeanDefinition
	 * 
	 * @see #parse(org.w3c.dom.Element, ParserContext)
	 * @see #postProcessComponentDefinition(org.springframework.beans.factory.parsing.BeanComponentDefinition)
	 */
	protected abstract AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext);

	/**
	 * Should an ID be generated instead of read from the passed in {@link Element}?
	 * 
	 * <p> 是否应该生成ID而不是从传入的元素中读取？
	 * 
	 * <p>Disabled by default; subclasses can override this to enable ID generation.
	 * Note that this flag is about <i>always</i> generating an ID; the parser
	 * won't even check for an "id" attribute in this case.
	 * 
	 * <p> 默认禁用; 子类可以覆盖它以启用ID生成。 请注意，此标志始终生成ID; 在这种情况下，解析器甚至不会检查“id”属性。
	 * 
	 * @return whether the parser should always generate an id - 解析器是否应始终生成id
	 */
	protected boolean shouldGenerateId() {
		return false;
	}

	/**
	 * Should an ID be generated instead if the passed in {@link Element} does not
	 * specify an "id" attribute explicitly?
	 * 
	 * <p> 如果传入的Element没有明确指定“id”属性，是否应该生成ID？
	 * 
	 * <p>Disabled by default; subclasses can override this to enable ID generation
	 * as fallback: The parser will first check for an "id" attribute in this case,
	 * only falling back to a generated ID if no value was specified.
	 * 
	 * <p> 默认禁用; 子类可以覆盖它以启用ID生成作为回退：在这种情况下，解析器将首先检查“id”属性，如果没有指定值，则仅返回生成的ID。
	 * 
	 * @return whether the parser should generate an id if no id was specified
	 * 
	 * <p> 如果没有指定id，解析器是否应该生成id
	 * 
	 */
	protected boolean shouldGenerateIdAsFallback() {
		return false;
	}

	/**
	 * Controls whether this parser is supposed to fire a
	 * {@link org.springframework.beans.factory.parsing.BeanComponentDefinition}
	 * event after parsing the bean definition.
	 * 
	 * <p> 控制解析bean定义后是否应该解析此解析器org.springframework.beans.factory.parsing.BeanComponentDefinition事件。
	 * 
	 * <p>This implementation returns {@code true} by default; that is,
	 * an event will be fired when a bean definition has been completely parsed.
	 * Override this to return {@code false} in order to suppress the event.
	 * 
	 * <p> 默认情况下，此实现返回true; 也就是说，在完全解析bean定义时将触发事件。 重写此方法以返回false以抑制事件。
	 * 
	 * @return {@code true} in order to fire a component registration event
	 * after parsing the bean definition; {@code false} to suppress the event
	 * 
	 * <p> 为了在解析bean定义后触发组件注册事件，为true; false来压制事件
	 * 
	 * @see #postProcessComponentDefinition
	 * @see org.springframework.beans.factory.parsing.ReaderContext#fireComponentRegistered
	 */
	protected boolean shouldFireEvents() {
		return true;
	}

	/**
	 * Hook method called after the primary parsing of a
	 * {@link BeanComponentDefinition} but before the
	 * {@link BeanComponentDefinition} has been registered with a
	 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
	 * 
	 * <p> 在BeanComponentDefinition的主要解析之后但在使
	 * 用org.springframework.beans.factory.support.BeanDefinitionRegistry注
	 * 册BeanComponentDefinition之前调用的Hook方法。
	 * 
	 * <p>Derived classes can override this method to supply any custom logic that
	 * is to be executed after all the parsing is finished.
	 * 
	 * <p> 派生类可以覆盖此方法，以提供在完成所有解析后要执行的任何自定义逻辑。
	 * 
	 * <p>The default implementation is a no-op.
	 * 
	 * <p> 默认实现是无操作。
	 * 
	 * @param componentDefinition the {@link BeanComponentDefinition} that is to be processed
	 * 
	 * <p> 要处理的BeanComponentDefinition
	 * 
	 */
	protected void postProcessComponentDefinition(BeanComponentDefinition componentDefinition) {
	}

}
