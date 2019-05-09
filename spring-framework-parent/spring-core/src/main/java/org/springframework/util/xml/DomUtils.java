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

package org.springframework.util.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;

import org.springframework.util.Assert;

/**
 * Convenience methods for working with the DOM API,
 * in particular for working with DOM Nodes and DOM Elements.
 * 
 * <p> 使用DOM API的便捷方法，特别是用于处理DOM节点和DOM元素。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Costin Leau
 * @author Arjen Poutsma
 * @author Luke Taylor
 * @since 1.2
 * @see org.w3c.dom.Node
 * @see org.w3c.dom.Element
 */
public abstract class DomUtils {

	/**
	 * Retrieves all child elements of the given DOM element that match any of the given element names.
	 * Only looks at the direct child level of the given element; do not go into further depth
	 * (in contrast to the DOM API's {@code getElementsByTagName} method).
	 * 
	 * <p> 检索给定DOM元素中与任何给定元素名称匹配的所有子元素。 仅查看给定元素的直接子级别; 
	 * 不要深入研究（与DOM API的getElementsByTagName方法相反）。
	 * 
	 * @param ele the DOM element to analyze - 要分析的DOM元素
	 * @param childEleNames the child element names to look for - 要查找的子元素名称
	 * @return a List of child {@code org.w3c.dom.Element} instances - 子org.w3c.dom.Element实例的列表
	 * @see org.w3c.dom.Element
	 * @see org.w3c.dom.Element#getElementsByTagName
	 */
	public static List<Element> getChildElementsByTagName(Element ele, String... childEleNames) {
		Assert.notNull(ele, "Element must not be null");
		Assert.notNull(childEleNames, "Element names collection must not be null");
		List<String> childEleNameList = Arrays.asList(childEleNames);
		NodeList nl = ele.getChildNodes();
		List<Element> childEles = new ArrayList<Element>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && nodeNameMatch(node, childEleNameList)) {
				childEles.add((Element) node);
			}
		}
		return childEles;
	}

	/**
	 * Retrieves all child elements of the given DOM element that match the given element name.
	 * Only look at the direct child level of the given element; do not go into further depth
	 * (in contrast to the DOM API's {@code getElementsByTagName} method).
	 * 
	 * <p> 检索给定DOM元素的与给定元素名称匹配的所有子元素。 只查看给定元素的直接子级别;
	 *  不要深入研究（与DOM API的getElementsByTagName方法相反）。
	 * 
	 * @param ele the DOM element to analyze - 要分析的DOM元素
	 * @param childEleName the child element name to look for - 要查找的子元素名称
	 * @return a List of child {@code org.w3c.dom.Element} instances - 子org.w3c.dom.Element实例的列表
	 * @see org.w3c.dom.Element
	 * @see org.w3c.dom.Element#getElementsByTagName
	 */
	public static List<Element> getChildElementsByTagName(Element ele, String childEleName) {
		return getChildElementsByTagName(ele, new String[] {childEleName});
	}

	/**
	 * Utility method that returns the first child element identified by its name.
	 * 
	 * <p> 返回由其名称标识的第一个子元素的实用程序方法。
	 * 
	 * @param ele the DOM element to analyze - 要分析的DOM元素
	 * @param childEleName the child element name to look for - 要查找的子元素名称
	 * @return the {@code org.w3c.dom.Element} instance, or {@code null} if none found
	 * 
	 * <p> org.w3c.dom.Element实例，如果没有找到则为null
	 * 
	 */
	public static Element getChildElementByTagName(Element ele, String childEleName) {
		Assert.notNull(ele, "Element must not be null");
		Assert.notNull(childEleName, "Element name must not be null");
		NodeList nl = ele.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && nodeNameMatch(node, childEleName)) {
				return (Element) node;
			}
		}
		return null;
	}

	/**
	 * Utility method that returns the first child element value identified by its name.
	 * 
	 * <p> 返回由其名称标识的第一个子元素值的实用程序方法。
	 * 
	 * @param ele the DOM element to analyze - 要分析的DOM元素
	 * @param childEleName the child element name to look for - 要查找的子元素名称
	 * @return the extracted text value, or {@code null} if no child element found - 提取的文本值，如果未找到子元素，则返回null
	 */
	public static String getChildElementValueByTagName(Element ele, String childEleName) {
		Element child = getChildElementByTagName(ele, childEleName);
		return (child != null ? getTextValue(child) : null);
	}

	/**
	 * Retrieves all child elements of the given DOM element
	 * 
	 * <p> 检索给定DOM元素的所有子元素
	 * 
	 * @param ele the DOM element to analyze - 要分析的DOM元素
	 * @return a List of child {@code org.w3c.dom.Element} instances - 子org.w3c.dom.Element实例的列表
	 */
	public static List<Element> getChildElements(Element ele) {
		Assert.notNull(ele, "Element must not be null");
		NodeList nl = ele.getChildNodes();
		List<Element> childEles = new ArrayList<Element>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				childEles.add((Element) node);
			}
		}
		return childEles;
	}

	/**
	 * Extracts the text value from the given DOM element, ignoring XML comments.
	 * 
	 * <p> 从给定的DOM元素中提取文本值，忽略XML注释。
	 * 
	 * <p>Appends all CharacterData nodes and EntityReference nodes into a single
	 * String value, excluding Comment nodes. Only exposes actual user-specified
	 * text, no default values of any kind.
	 * 
	 * <p> 将所有CharacterData节点和EntityReference节点追加到单个String值中，
	 * 不包括Comment节点。 仅公开实际的用户指定文本，没有任何类型的默认值。
	 * @see CharacterData
	 * @see EntityReference
	 * @see Comment
	 */
	public static String getTextValue(Element valueEle) {
		Assert.notNull(valueEle, "Element must not be null");
		StringBuilder sb = new StringBuilder();
		NodeList nl = valueEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if ((item instanceof CharacterData && !(item instanceof Comment)) || item instanceof EntityReference) {
				sb.append(item.getNodeValue());
			}
		}
		return sb.toString();
	}

	/**
	 * Namespace-aware equals comparison. Returns {@code true} if either
	 * {@link Node#getLocalName} or {@link Node#getNodeName} equals
	 * {@code desiredName}, otherwise returns {@code false}.
	 * 
	 * <p> 命名空间感知等于比较。 如果Node.getLocalName或Node.getNodeName等于desiredName，则返回true，否则返回false。
	 * 
	 */
	public static boolean nodeNameEquals(Node node, String desiredName) {
		Assert.notNull(node, "Node must not be null");
		Assert.notNull(desiredName, "Desired name must not be null");
		return nodeNameMatch(node, desiredName);
	}

	/**
	 * Returns a SAX {@code ContentHandler} that transforms callback calls to DOM {@code Node}s.
	 * 
	 * <p> 返回一个SAX ContentHandler，它将回调调用转换为DOM节点。
	 * 
	 * @param node the node to publish events to - 要将事件发布到的节点
	 * @return the content handler - 内容处理程序
	 */
	public static ContentHandler createContentHandler(Node node) {
		return new DomContentHandler(node);
	}

	/**
	 * Matches the given node's name and local name against the given desired name.
	 * 
	 * <p> 根据给定的所需名称匹配给定节点的名称和本地名称。
	 * 
	 */
	private static boolean nodeNameMatch(Node node, String desiredName) {
		return (desiredName.equals(node.getNodeName()) || desiredName.equals(node.getLocalName()));
	}

	/**
	 * Matches the given node's name and local name against the given desired names.
	 * 
	 * <p> 根据给定的所需名称匹配给定节点的名称和本地名称。
	 * 
	 */
	private static boolean nodeNameMatch(Node node, Collection<?> desiredNames) {
		return (desiredNames.contains(node.getNodeName()) || desiredNames.contains(node.getLocalName()));
	}

}
