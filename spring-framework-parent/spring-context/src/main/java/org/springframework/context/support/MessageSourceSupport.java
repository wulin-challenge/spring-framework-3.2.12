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

package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.ObjectUtils;

/**
 * Base class for message source implementations, providing support infrastructure
 * such as {@link java.text.MessageFormat} handling but not implementing concrete
 * methods defined in the {@link org.springframework.context.MessageSource}.
 *
 * <p> 消息源实现的基类，提供支持基础结构，如java.text.MessageFormat处理，
 * 但不实现org.springframework.context.MessageSource中定义的具体方法。
 * 
 * <p>{@link AbstractMessageSource} derives from this class, providing concrete
 * {@code getMessage} implementations that delegate to a central template
 * method for message code resolution.
 * 
 * <p> AbstractMessageSource派生自此类，提供具体的getMessage实现，这些实现委托给用于消息代码解析的中央模板方法。
 *
 * @author Juergen Hoeller
 * @since 2.5.5
 */
public abstract class MessageSourceSupport {

	private static final MessageFormat INVALID_MESSAGE_FORMAT = new MessageFormat("");

	/** Logger available to subclasses */
	/** 记录器可用于子类 */
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean alwaysUseMessageFormat = false;

	/**
	 * Cache to hold already generated MessageFormats per message.
	 * Used for passed-in default messages. MessageFormats for resolved
	 * codes are cached on a specific basis in subclasses.
	 * 
	 * <p> 缓存以保存每条消息已生成的MessageFormats。 用于传入的默认消息。 
	 * 已解析代码的MessageFormats在子类中以特定方式缓存。
	 */
	private final Map<String, Map<Locale, MessageFormat>> messageFormatsPerMessage =
			new HashMap<String, Map<Locale, MessageFormat>>();


	/**
	 * Set whether to always apply the MessageFormat rules, parsing even
	 * messages without arguments.
	 * 
	 * <p> 设置是否始终应用MessageFormat规则，甚至解析不带参数的消息。
	 * 
	 * <p>Default is "false": Messages without arguments are by default
	 * returned as-is, without parsing them through MessageFormat.
	 * Set this to "true" to enforce MessageFormat for all messages,
	 * expecting all message texts to be written with MessageFormat escaping.
	 * 
	 * <p> 默认值为“false”：默认情况下，不带参数的消息按原样返回，而不通过MessageFormat解析它们。 
	 * 将其设置为“true”以对所有消息强制执行MessageFormat，期望使用MessageFormat转义来编写所有消息文本。
	 * 
	 * <p>For example, MessageFormat expects a single quote to be escaped
	 * as "''". If your message texts are all written with such escaping,
	 * even when not defining argument placeholders, you need to set this
	 * flag to "true". Else, only message texts with actual arguments
	 * are supposed to be written with MessageFormat escaping.
	 * 
	 * <p> 例如，MessageFormat期望将单引号转义为“''”。 如果您的消息文本都是使用这样的转义编写的，
	 * 即使没有定义参数占位符，您也需要将此标志设置为“true”。 否则，只有具有实际参数的消息文本才应该使用MessageFormat转义来编写。
	 * 
	 * @see java.text.MessageFormat
	 */
	public void setAlwaysUseMessageFormat(boolean alwaysUseMessageFormat) {
		this.alwaysUseMessageFormat = alwaysUseMessageFormat;
	}

	/**
	 * Return whether to always apply the MessageFormat rules, parsing even
	 * messages without arguments.
	 * 
	 * <p> 返回是否始终应用MessageFormat规则，甚至解析不带参数的消息。
	 */
	protected boolean isAlwaysUseMessageFormat() {
		return this.alwaysUseMessageFormat;
	}


	/**
	 * Render the given default message String. The default message is
	 * passed in as specified by the caller and can be rendered into
	 * a fully formatted default message shown to the user.
	 * 
	 * <p> 呈现给定的默认消息String。 默认消息按调用者指定的方式传入，并可呈现为向用户显示的完全格式化的默认消息。
	 * 
	 * <p>The default implementation passes the String to {@code formatMessage},
	 * resolving any argument placeholders found in them. Subclasses may override
	 * this method to plug in custom processing of default messages.
	 * 
	 * <p> 默认实现将String传递给formatMessage，解析在其中找到的任何参数占位符。 子类可以重写此方法以插入默认消息的自定义处理。
	 * 
	 * @param defaultMessage the passed-in default message String - 传入的默认消息String
	 * @param args array of arguments that will be filled in for params within
	 * the message, or {@code null} if none.
	 * 
	 * <p> 将填充消息中的params的参数数组，如果没有则为null。
	 * 
	 * @param locale the Locale used for formatting - 用于格式化的区域设置
	 * @return the rendered default message (with resolved arguments)
	 * 
	 * <p> 呈现的默认消息（带有已解析的参数）
	 * 
	 * @see #formatMessage(String, Object[], java.util.Locale)
	 */
	protected String renderDefaultMessage(String defaultMessage, Object[] args, Locale locale) {
		return formatMessage(defaultMessage, args, locale);
	}

	/**
	 * Format the given message String, using cached MessageFormats.
	 * By default invoked for passed-in default messages, to resolve
	 * any argument placeholders found in them.
	 * 
	 * <p> 使用缓存的MessageFormats格式化给定的消息String。 
	 * 默认情况下，为传入的默认消息调用，以解析在其中找到的任何参数占位符。
	 * 
	 * @param msg the message to format - 要格式化的消息
	 * @param args array of arguments that will be filled in for params within
	 * the message, or {@code null} if none
	 * 
	 * <p> 将填充消息中的params的参数数组，如果没有则为null
	 * 
	 * @param locale the Locale used for formatting - 用于格式化的区域设置
	 * @return the formatted message (with resolved arguments) - 格式化的消息（带有已解析的参数）
	 */
	protected String formatMessage(String msg, Object[] args, Locale locale) {
		if (msg == null || (!this.alwaysUseMessageFormat && ObjectUtils.isEmpty(args))) {
			return msg;
		}
		MessageFormat messageFormat = null;
		synchronized (this.messageFormatsPerMessage) {
			Map<Locale, MessageFormat> messageFormatsPerLocale = this.messageFormatsPerMessage.get(msg);
			if (messageFormatsPerLocale != null) {
				messageFormat = messageFormatsPerLocale.get(locale);
			}
			else {
				messageFormatsPerLocale = new HashMap<Locale, MessageFormat>();
				this.messageFormatsPerMessage.put(msg, messageFormatsPerLocale);
			}
			if (messageFormat == null) {
				try {
					messageFormat = createMessageFormat(msg, locale);
				}
				catch (IllegalArgumentException ex) {
					// invalid message format - probably not intended for formatting,
					// rather using a message structure with no arguments involved
					
					// 无效的消息格式 - 可能不是用于格式化，而是使用不涉及参数的消息结构
					if (this.alwaysUseMessageFormat) {
						throw ex;
					}
					// silently proceed with raw message if format not enforced
					// 如果格式未强制，则以静默方式继续处理原始消息
					messageFormat = INVALID_MESSAGE_FORMAT;
				}
				messageFormatsPerLocale.put(locale, messageFormat);
			}
		}
		if (messageFormat == INVALID_MESSAGE_FORMAT) {
			return msg;
		}
		synchronized (messageFormat) {
			return messageFormat.format(resolveArguments(args, locale));
		}
	}

	/**
	 * Create a MessageFormat for the given message and Locale.
	 * 
	 * <p> 为给定的消息和Locale创建MessageFormat。
	 * 
	 * @param msg the message to create a MessageFormat for - 用于创建MessageFormat的消息
	 * @param locale the Locale to create a MessageFormat for - 用于创建MessageFormat的Locale
	 * @return the MessageFormat instance - MessageFormat实例
	 */
	protected MessageFormat createMessageFormat(String msg, Locale locale) {
		return new MessageFormat((msg != null ? msg : ""), locale);
	}

	/**
	 * Template method for resolving argument objects.
	 * 
	 * <p> 用于解析参数对象的模板方法。
	 * 
	 * <p>The default implementation simply returns the given argument array as-is.
	 * Can be overridden in subclasses in order to resolve special argument types.
	 * 
	 * <p> 默认实现只是按原样返回给定的参数数组。 可以在子类中重写以解析特殊参数类型。
	 * 
	 * @param args the original argument array - 原始参数数组
	 * @param locale the Locale to resolve against - 要解决的区域设置
	 * @return the resolved argument array - 已解析的参数数组
	 */
	protected Object[] resolveArguments(Object[] args, Locale locale) {
		return args;
	}

}
