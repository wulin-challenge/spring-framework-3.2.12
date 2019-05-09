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

import java.util.Locale;

/**
 * Strategy interface for resolving messages, with support for the parameterization
 * and internationalization of such messages.
 * 
 * <p> 用于解析消息的策略接口，支持此类消息的参数化和国际化。
 *
 * <p>Spring provides two out-of-the-box implementations for production:
 * <ul>
 * <li>{@link org.springframework.context.support.ResourceBundleMessageSource},
 * built on top of the standard {@link java.util.ResourceBundle}
 * <li>{@link org.springframework.context.support.ReloadableResourceBundleMessageSource},
 * being able to reload message definitions without restarting the VM
 * </ul>
 * 
 * <p>Spring为生产提供了两种开箱即用的实现：
 * <ul>
 *   <li>org.springframework.context.support.ResourceBundleMessageSource，构建于标准java.util.ResourceBundle之上
 *   <li>org.springframework.context.support.ReloadableResourceBundleMessageSource，能够重新加载消息定义而无需重新启动VM
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.context.support.ResourceBundleMessageSource
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 */
public interface MessageSource {

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 * 
	 * <p> 尝试解决该消息。 如果未找到任何消息，则返回默认消息。
	 * 
	 * @param code the code to lookup up, such as 'calculator.noRateSet'. Users of
	 * this class are encouraged to base message names on the relevant fully
	 * qualified class name, thus avoiding conflict and ensuring maximum clarity.
	 * 
	 * <p> 要查找的代码，例如'calculator.noRateSet'。 鼓励此类用户在相关的完全限定类名上建立消息名称，从而避免冲突并确保最大程度的清晰度。
	 * 
	 * @param args array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none.
	 * 
	 * <p> 将填充消息中的参数的参数数组（参数在消息中看起来像“{0}”，“{1，date}”，“{2，time}”，如果没有则为null。
	 * 
	 * @param defaultMessage String to return if the lookup fails - 如果查找失败则返回的字符串
	 * @param locale the Locale in which to do the lookup - 要在其中执行查找的区域设置
	 * @return the resolved message if the lookup was successful;
	 * otherwise the default message passed as a parameter
	 * 
	 * <p> 如果查找成功，则解析消息; 否则默认消息作为参数传递
	 * 
	 * @see java.text.MessageFormat
	 */
	String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

	/**
	 * Try to resolve the message. Treat as an error if the message can't be found.
	 * 
	 * <p> 尝试解决该消息。 如果无法找到消息，则视为错误。
	 * 
	 * @param code the code to lookup up, such as 'calculator.noRateSet'
	 * 
	 * <p> 要查找的代码，例如'calculator.noRateSet'
	 * 
	 * @param args Array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none.
	 * 
	 * <p> 将填充消息中的参数的参数数组（参数在消息中看起来像“{0}”，“{1，date}”，“{2，time}”），如果没有则为null。
	 * 
	 * @param locale the Locale in which to do the lookup - 要在其中执行查找的区域设置
	 * @return the resolved message
	 * @throws NoSuchMessageException if the message wasn't found
	 * @see java.text.MessageFormat
	 */
	String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

	/**
	 * Try to resolve the message using all the attributes contained within the
	 * {@code MessageSourceResolvable} argument that was passed in.
	 * 
	 * <p> 尝试使用传入的MessageSourceResolvable参数中包含的所有属性来解析消息。
	 * 
	 * <p>NOTE: We must throw a {@code NoSuchMessageException} on this method
	 * since at the time of calling this method we aren't able to determine if the
	 * {@code defaultMessage} property of the resolvable is null or not.
	 * 
	 * <p> 注意：我们必须对此方法抛出NoSuchMessageException，因为在调用此方法时，我们无法确定resolvable的defaultMessage属性是否为null。
	 * 
	 * @param resolvable value object storing attributes required to properly resolve a message
	 * 
	 * <p> value对象，用于存储正确解析消息所需的属性
	 * 
	 * @param locale the Locale in which to do the lookup - 要在其中执行查找的区域设置
	 * @return the resolved message - 已解决的消息
	 * @throws NoSuchMessageException if the message wasn't found - 如果没有找到该消息
	 * @see java.text.MessageFormat
	 */
	String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;

}
