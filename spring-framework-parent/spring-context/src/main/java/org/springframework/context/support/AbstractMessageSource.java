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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.ObjectUtils;

/**
 * Abstract implementation of the {@link HierarchicalMessageSource} interface,
 * implementing common handling of message variants, making it easy
 * to implement a specific strategy for a concrete MessageSource.
 * 
 * <p> HierarchicalMessageSource接口的抽象实现，实现消息变体的常见处理，
 * 使得为具体的MessageSource实现特定策略变得容易。
 *
 * <p>Subclasses must implement the abstract {@link #resolveCode}
 * method. For efficient resolution of messages without arguments, the
 * {@link #resolveCodeWithoutArguments} method should be overridden
 * as well, resolving messages without a MessageFormat being involved.
 * 
 * <p> 子类必须实现抽象的resolveCode方法。为了有效地解决没有参数的消息，还应该重写
 * resolveCodeWithoutArguments方法，解决消息而不涉及MessageFormat。
 *
 * <p><b>Note:</b> By default, message texts are only parsed through
 * MessageFormat if arguments have been passed in for the message. In case
 * of no arguments, message texts will be returned as-is. As a consequence,
 * you should only use MessageFormat escaping for messages with actual
 * arguments, and keep all other messages unescaped. If you prefer to
 * escape all messages, set the "alwaysUseMessageFormat" flag to "true".
 * 
 * <p> 注意：默认情况下，如果已为消息传入参数，则仅通过MessageFormat解析消息文本。如果没有参数，
 * 将按原样返回消息文本。因此，您应该仅对具有实际参数的消息使用MessageFormat转义，并保持所有其他消息不转义。
 * 如果您希望转义所有消息，请将“alwaysUseMessageFormat”标志设置为“true”。
 *
 * <p>Supports not only MessageSourceResolvables as primary messages
 * but also resolution of message arguments that are in turn
 * MessageSourceResolvables themselves.
 * 
 * <p> 不仅支持MessageSourceResolvables作为主要消息，还支持消息参数的解析，
 * 而消息参数又是MessageSourceResolvables本身。
 *
 * <p>This class does not implement caching of messages per code, thus
 * subclasses can dynamically change messages over time. Subclasses are
 * encouraged to cache their messages in a modification-aware fashion,
 * allowing for hot deployment of updated messages.
 * 
 * <p> 此类不实现每个代码的消息缓存，因此子类可以随时间动态更改消息。鼓励子类以修改感知的方式缓存其消息，允许热更新消息的部署。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @see #resolveCode(String, java.util.Locale)
 * @see #resolveCodeWithoutArguments(String, java.util.Locale)
 * @see #setAlwaysUseMessageFormat
 * @see java.text.MessageFormat
 */
public abstract class AbstractMessageSource extends MessageSourceSupport implements HierarchicalMessageSource {

	private MessageSource parentMessageSource;

	private Properties commonMessages;

	private boolean useCodeAsDefaultMessage = false;


	public void setParentMessageSource(MessageSource parent) {
		this.parentMessageSource = parent;
	}

	public MessageSource getParentMessageSource() {
		return this.parentMessageSource;
	}

	/**
	 * Specify locale-independent common messages, with the message code as key
	 * and the full message String (may contain argument placeholders) as value.
	 * 
	 * <p> 指定与语言环境无关的公共消息，消息代码为键，完整消息String（可能包含参数占位符）作为值。
	 * 
	 * <p>May also link to an externally defined Properties object, e.g. defined
	 * through a {@link org.springframework.beans.factory.config.PropertiesFactoryBean}.
	 * 
	 * <p> 也可以链接到外部定义的Properties对象，例如 通过
	 * org.springframework.beans.factory.config.PropertiesFactoryBean定义。
	 * 
	 */
	public void setCommonMessages(Properties commonMessages) {
		this.commonMessages = commonMessages;
	}

	/**
	 * Return a Properties object defining locale-independent common messages, if any.
	 * 
	 * <p> 返回定义与语言环境无关的公共消息的Properties对象（如果有）。
	 * 
	 */
	protected Properties getCommonMessages() {
		return this.commonMessages;
	}

	/**
	 * Set whether to use the message code as default message instead of
	 * throwing a NoSuchMessageException. Useful for development and debugging.
	 * Default is "false".
	 * 
	 * <p> 设置是否将消息代码用作默认消息，而不是抛出NoSuchMessageException。 对开发和调试很有用。 默认为“false”。
	 * 
	 * <p>Note: In case of a MessageSourceResolvable with multiple codes
	 * (like a FieldError) and a MessageSource that has a parent MessageSource,
	 * do <i>not</i> activate "useCodeAsDefaultMessage" in the <i>parent</i>:
	 * Else, you'll get the first code returned as message by the parent,
	 * without attempts to check further codes.
	 * 
	 * <p> 注意：如果MessageSourceResolvable具有多个代码（如FieldError）和MessageSource具有父MessageSource，
	 * 请不要在父级中激活“useCodeAsDefaultMessage”：否则，您将获得父级返回的第一个代码， 没有尝试检查进一步的代码。
	 * 
	 * <p>To be able to work with "useCodeAsDefaultMessage" turned on in the parent,
	 * AbstractMessageSource and AbstractApplicationContext contain special checks
	 * to delegate to the internal {@link #getMessageInternal} method if available.
	 * In general, it is recommended to just use "useCodeAsDefaultMessage" during
	 * development and not rely on it in production in the first place, though.
	 * 
	 * <p> 为了能够在父级中启用“useCodeAsDefaultMessage”，AbstractMessageSource和
	 * AbstractApplicationContext包含特殊检查以委托给内部getMessageInternal方法（如果可用）。 
	 * 一般来说，建议在开发过程中使用“useCodeAsDefaultMessage”，而不是首先在生产中依赖它。
	 * 
	 * @see #getMessage(String, Object[], Locale)
	 * @see org.springframework.validation.FieldError
	 */
	public void setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
		this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
	}

	/**
	 * Return whether to use the message code as default message instead of
	 * throwing a NoSuchMessageException. Useful for development and debugging.
	 * Default is "false".
	 * 
	 * <p> 返回是否使用消息代码作为默认消息而不是抛出NoSuchMessageException。 对开发和调试很有用。 默认为“false”。
	 * 
	 * <p>Alternatively, consider overriding the {@link #getDefaultMessage}
	 * method to return a custom fallback message for an unresolvable code.
	 * 
	 * <p> 或者，考虑重写getDefaultMessage方法以返回无法解析的代码的自定义回退消息。
	 * 
	 * @see #getDefaultMessage(String)
	 */
	protected boolean isUseCodeAsDefaultMessage() {
		return this.useCodeAsDefaultMessage;
	}


	public final String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		String msg = getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}
		if (defaultMessage == null) {
			String fallback = getDefaultMessage(code);
			if (fallback != null) {
				return fallback;
			}
		}
		return renderDefaultMessage(defaultMessage, args, locale);
	}

	public final String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		String msg = getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}
		String fallback = getDefaultMessage(code);
		if (fallback != null) {
			return fallback;
		}
		throw new NoSuchMessageException(code, locale);
	}

	public final String getMessage(MessageSourceResolvable resolvable, Locale locale)
			throws NoSuchMessageException {

		String[] codes = resolvable.getCodes();
		if (codes == null) {
			codes = new String[0];
		}
		for (String code : codes) {
			String msg = getMessageInternal(code, resolvable.getArguments(), locale);
			if (msg != null) {
				return msg;
			}
		}
		String defaultMessage = resolvable.getDefaultMessage();
		if (defaultMessage != null) {
			return renderDefaultMessage(defaultMessage, resolvable.getArguments(), locale);
		}
		if (codes.length > 0) {
			String fallback = getDefaultMessage(codes[0]);
			if (fallback != null) {
				return fallback;
			}
		}
		throw new NoSuchMessageException(codes.length > 0 ? codes[codes.length - 1] : null, locale);
	}


	/**
	 * Resolve the given code and arguments as message in the given Locale,
	 * returning {@code null} if not found. Does <i>not</i> fall back to
	 * the code as default message. Invoked by {@code getMessage} methods.
	 * 
	 * <p> 将给定的代码和参数解析为给定Locale中的消息，如果未找到则返回null。 不作为默认消息回退到代码。 
	 * 由getMessage方法调用。
	 * 
	 * @param code the code to lookup up, such as 'calculator.noRateSet'
	 * 
	 * <p> 要查找的代码，例如'calculator.noRateSet'
	 * 
	 * @param args array of arguments that will be filled in for params
	 * within the message
	 * 
	 * <p> 将填充消息中的参数的参数数组
	 * 
	 * @param locale the Locale in which to do the lookup - 要在其中执行查找的区域设置
	 * @return the resolved message, or {@code null} if not found - 已解决的消息，如果未找到则为null
	 * @see #getMessage(String, Object[], String, Locale)
	 * @see #getMessage(String, Object[], Locale)
	 * @see #getMessage(MessageSourceResolvable, Locale)
	 * @see #setUseCodeAsDefaultMessage
	 */
	protected String getMessageInternal(String code, Object[] args, Locale locale) {
		if (code == null) {
			return null;
		}
		if (locale == null) {
			locale = Locale.getDefault();
		}
		Object[] argsToUse = args;

		if (!isAlwaysUseMessageFormat() && ObjectUtils.isEmpty(args)) {
			// Optimized resolution: no arguments to apply,
			// therefore no MessageFormat needs to be involved.
			// Note that the default implementation still uses MessageFormat;
			// this can be overridden in specific subclasses.
			String message = resolveCodeWithoutArguments(code, locale);
			if (message != null) {
				return message;
			}
		}

		else {
			// Resolve arguments eagerly, for the case where the message
			// is defined in a parent MessageSource but resolvable arguments
			// are defined in the child MessageSource.
			argsToUse = resolveArguments(args, locale);

			MessageFormat messageFormat = resolveCode(code, locale);
			if (messageFormat != null) {
				synchronized (messageFormat) {
					return messageFormat.format(argsToUse);
				}
			}
		}

		// Check locale-independent common messages for the given message code.
		Properties commonMessages = getCommonMessages();
		if (commonMessages != null) {
			String commonMessage = commonMessages.getProperty(code);
			if (commonMessage != null) {
				return formatMessage(commonMessage, args, locale);
			}
		}

		// Not found -> check parent, if any.
		return getMessageFromParent(code, argsToUse, locale);
	}

	/**
	 * Try to retrieve the given message from the parent MessageSource, if any.
	 * 
	 * <p> 尝试从父MessageSource检索给定的消息（如果有）。
	 * 
	 * @param code the code to lookup up, such as 'calculator.noRateSet' - 要查找的代码，例如'calculator.noRateSet'
	 * @param args array of arguments that will be filled in for params
	 * within the message
	 * 
	 * <p> 将填充消息中的参数的参数数组
	 * 
	 * @param locale the Locale in which to do the lookup - 要在其中执行查找的区域设置
	 * @return the resolved message, or {@code null} if not found - 已解决的消息，如果未找到则为null
	 * @see #getParentMessageSource()
	 */
	protected String getMessageFromParent(String code, Object[] args, Locale locale) {
		MessageSource parent = getParentMessageSource();
		if (parent != null) {
			if (parent instanceof AbstractMessageSource) {
				// Call internal method to avoid getting the default code back
				// in case of "useCodeAsDefaultMessage" being activated.
				return ((AbstractMessageSource) parent).getMessageInternal(code, args, locale);
			}
			else {
				// Check parent MessageSource, returning null if not found there.
				return parent.getMessage(code, args, null, locale);
			}
		}
		// Not found in parent either.
		return null;
	}

	/**
	 * Return a fallback default message for the given code, if any.
	 * 
	 * <p> 返回给定代码的后备默认消息（如果有）。
	 * 
	 * <p>Default is to return the code itself if "useCodeAsDefaultMessage" is activated,
	 * or return no fallback else. In case of no fallback, the caller will usually
	 * receive a NoSuchMessageException from {@code getMessage}.
	 * 
	 * <p> 如果激活“useCodeAsDefaultMessage”，则默认为返回代码本身，或者不返回其他后退。 
	 * 如果没有回退，调用者通常会从getMessage收到NoSuchMessageException。
	 * 
	 * @param code the message code that we couldn't resolve
	 * and that we didn't receive an explicit default message for
	 * 
	 * <p> 我们无法解决的消息代码，以及我们没有收到明确的默认消息
	 * 
	 * @return the default message to use, or {@code null} if none
	 * 
	 * <p> 要使用的默认消息，如果没有则为null
	 * 
	 * @see #setUseCodeAsDefaultMessage
	 */
	protected String getDefaultMessage(String code) {
		if (isUseCodeAsDefaultMessage()) {
			return code;
		}
		return null;
	}


	/**
	 * Searches through the given array of objects, finds any MessageSourceResolvable
	 * objects and resolves them.
	 * 
	 * <p> 搜索给定的对象数组，查找任何MessageSourceResolvable对象并解析它们。
	 * 
	 * <p>Allows for messages to have MessageSourceResolvables as arguments.
	 * 
	 * <p> 允许消息将MessageSourceResolvables作为参数。
	 * 
	 * @param args array of arguments for a message - 消息的参数数组
	 * @param locale the locale to resolve through - 要解决的语言环境
	 * @return an array of arguments with any MessageSourceResolvables resolved 
	 * 
	 * <p> 解析了任何MessageSourceResolvables的参数数组
	 * 
	 */
	@Override
	protected Object[] resolveArguments(Object[] args, Locale locale) {
		if (args == null) {
			return new Object[0];
		}
		List<Object> resolvedArgs = new ArrayList<Object>(args.length);
		for (Object arg : args) {
			if (arg instanceof MessageSourceResolvable) {
				resolvedArgs.add(getMessage((MessageSourceResolvable) arg, locale));
			}
			else {
				resolvedArgs.add(arg);
			}
		}
		return resolvedArgs.toArray(new Object[resolvedArgs.size()]);
	}

	/**
	 * Subclasses can override this method to resolve a message without arguments
	 * in an optimized fashion, i.e. to resolve without involving a MessageFormat.
	 * 
	 * <p> 子类可以重写此方法以优化方式解析没有参数的消息，即在不涉及MessageFormat的情况下解析。
	 * 
	 * <p>The default implementation <i>does</i> use MessageFormat, through
	 * delegating to the {@link #resolveCode} method. Subclasses are encouraged
	 * to replace this with optimized resolution.
	 * 
	 * <p> 默认实现使用MessageFormat，通过委托给resolveCode方法。 鼓励子类用优化的分辨率替换它。
	 * 
	 * <p>Unfortunately, {@code java.text.MessageFormat} is not implemented
	 * in an efficient fashion. In particular, it does not detect that a message
	 * pattern doesn't contain argument placeholders in the first place. Therefore,
	 * it is advisable to circumvent MessageFormat for messages without arguments.
	 * 
	 * <p> 遗憾的是，java.text.MessageFormat没有以有效的方式实现。 特别是，它不会检测到消息模式首先不包含参数占位符。 
	 * 因此，建议对没有参数的消息规避MessageFormat。
	 * 
	 * @param code the code of the message to resolve - 要解决的消息的代码
	 * @param locale the Locale to resolve the code for
	 * (subclasses are encouraged to support internationalization)
	 * 
	 * <p> 用于解析代码的Locale（鼓励子类支持国际化）
	 * 
	 * @return the message String, or {@code null} if not found - 消息String，如果未找到则为null
	 * @see #resolveCode
	 * @see java.text.MessageFormat
	 */
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		MessageFormat messageFormat = resolveCode(code, locale);
		if (messageFormat != null) {
			synchronized (messageFormat) {
				return messageFormat.format(new Object[0]);
			}
		}
		return null;
	}

	/**
	 * Subclasses must implement this method to resolve a message.
	 * 
	 * <p> 子类必须实现此方法来解析消息。
	 * 
	 * <p>Returns a MessageFormat instance rather than a message String,
	 * to allow for appropriate caching of MessageFormats in subclasses.
	 * 
	 * <p> 返回MessageFormat实例而不是消息String，以允许在子类中适当缓存MessageFormats。
	 * 
	 * <p><b>Subclasses are encouraged to provide optimized resolution
	 * for messages without arguments, not involving MessageFormat.</b>
	 * See the {@link #resolveCodeWithoutArguments} javadoc for details.
	 * 
	 * <p> 鼓励子类为没有参数的消息提供优化的解析，不涉及MessageFormat。 
	 * 有关详细信息，请参阅resolveCodeWithoutArguments javadoc。
	 * 
	 * @param code the code of the message to resolve - 要解决的消息的代码
	 * @param locale the Locale to resolve the code for
	 * (subclasses are encouraged to support internationalization)
	 * 
	 * <p> 用于解析代码的Locale（鼓励子类支持国际化）
	 * 
	 * @return the MessageFormat for the message, or {@code null} if not found
	 * 
	 * <p> 消息的MessageFormat，如果未找到则为null
	 * 
	 * @see #resolveCodeWithoutArguments(String, java.util.Locale)
	 */
	protected abstract MessageFormat resolveCode(String code, Locale locale);

}
