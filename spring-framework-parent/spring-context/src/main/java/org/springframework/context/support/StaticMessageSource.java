/*
 * Copyright 2002-2010 the original author or authors.
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

import org.springframework.util.Assert;

/**
 * Simple implementation of {@link org.springframework.context.MessageSource}
 * which allows messages to be registered programmatically.
 * This MessageSource supports basic internationalization.
 * 
 * <p> 简单实现org.springframework.context.MessageSource，它允许以编程方式注册消息。 此MessageSource支持基本国际化。
 *
 * <p>Intended for testing rather than for use in production systems.
 * 
 * <p> 用于测试而不是用于生产系统。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class StaticMessageSource extends AbstractMessageSource {

	/** Map from 'code + locale' keys to message Strings */
	/** 从'code + locale'键映射到消息字符串 */
	private final Map<String, String> messages = new HashMap<String, String>();

	private final Map<String, MessageFormat> cachedMessageFormats = new HashMap<String, MessageFormat>();


	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		return this.messages.get(code + "_" + locale.toString());
	}

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		String key = code + "_" + locale.toString();
		String msg = this.messages.get(key);
		if (msg == null) {
			return null;
		}
		synchronized (this.cachedMessageFormats) {
			MessageFormat messageFormat = this.cachedMessageFormats.get(key);
			if (messageFormat == null) {
				messageFormat = createMessageFormat(msg, locale);
				this.cachedMessageFormats.put(key, messageFormat);
			}
			return messageFormat;
		}
	}

	/**
	 * Associate the given message with the given code.
	 * 
	 * <p> 从'code + locale'键映射到消息字符串
	 * 
	 * @param code the lookup code - 查找代码
	 * @param locale the locale that the message should be found within
	 * 
	 * <p> 应该在其中找到消息的语言环境
	 * 
	 * @param msg the message associated with this lookup code
	 * 
	 * <p> 与此查找代码关联的消息
	 * 
	 */
	public void addMessage(String code, Locale locale, String msg) {
		Assert.notNull(code, "Code must not be null");
		Assert.notNull(locale, "Locale must not be null");
		Assert.notNull(msg, "Message must not be null");
		this.messages.put(code + "_" + locale.toString(), msg);
		if (logger.isDebugEnabled()) {
			logger.debug("Added message [" + msg + "] for code [" + code + "] and Locale [" + locale + "]");
		}
	}

	/**
	 * Associate the given message values with the given keys as codes.
	 * 
	 * <p> 将给定的消息值与给定的密钥关联为代码。
	 * 
	 * @param messages the messages to register, with messages codes
	 * as keys and message texts as values
	 * 
	 * <p> 要注册的消息，消息代码为键，消息文本为值
	 * 
	 * @param locale the locale that the messages should be found within
	 * 
	 * <p> 应该在其中找到消息的语言环境
	 * 
	 */
	public void addMessages(Map<String, String> messages, Locale locale) {
		Assert.notNull(messages, "Messages Map must not be null");
		for (Map.Entry<String, String> entry : messages.entrySet()) {
			addMessage(entry.getKey(), locale, entry.getValue());
		}
	}


	@Override
	public String toString() {
		return getClass().getName() + ": " + this.messages;
	}

}
