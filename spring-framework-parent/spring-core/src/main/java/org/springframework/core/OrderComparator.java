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

package org.springframework.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * {@link Comparator} implementation for {@link Ordered} objects,
 * sorting by order value ascending (resp. by priority descending).
 * 
 * <p> Ordered对象的比较器实现，按顺序值升序排序（按优先级降序排序）。
 *
 * <p>Non-{@code Ordered} objects are treated as greatest order
 * values, thus ending up at the end of the list, in arbitrary order
 * (just like same order values of {@code Ordered} objects).
 * 
 * <p> 非有序对象被视为最大的顺序值，因此以任意顺序结束于列表的末尾（就像有序对象的相同顺序值一样）。
 *
 * @author Juergen Hoeller
 * @since 07.04.2003
 * @see Ordered
 * @see java.util.Collections#sort(java.util.List, java.util.Comparator)
 * @see java.util.Arrays#sort(Object[], java.util.Comparator)
 */
public class OrderComparator implements Comparator<Object> {

	/**
	 * Shared default instance of OrderComparator.
	 * 
	 * <p> OrderComparator的共享默认实例。
	 */
	public static final OrderComparator INSTANCE = new OrderComparator();


	public int compare(Object o1, Object o2) {
		boolean p1 = (o1 instanceof PriorityOrdered);
		boolean p2 = (o2 instanceof PriorityOrdered);
		if (p1 && !p2) {
			return -1;
		}
		else if (p2 && !p1) {
			return 1;
		}

		// Direct evaluation instead of Integer.compareTo to avoid unnecessary object creation.
		// 直接评估而不是Integer.compareTo以避免不必要的对象创建。
		int i1 = getOrder(o1);
		int i2 = getOrder(o2);
		return (i1 < i2) ? -1 : (i1 > i2) ? 1 : 0;
	}

	/**
	 * Determine the order value for the given object.
	 * 
	 * <p> 确定给定对象的顺序值。
	 * 
	 * <p>The default implementation checks against the {@link Ordered}
	 * interface. Can be overridden in subclasses.
	 * 
	 * <p> 默认实现检查Ordered接口。 可以在子类中重写。
	 * 
	 * @param obj the object to check - 要检查的对象
	 * @return the order value, or {@code Ordered.LOWEST_PRECEDENCE} as fallback
	 * 
	 * <p> 订单值，或Ordered.LOWEST_PRECEDENCE作为后备
	 */
	protected int getOrder(Object obj) {
		return (obj instanceof Ordered ? ((Ordered) obj).getOrder() : Ordered.LOWEST_PRECEDENCE);
	}


	/**
	 * Sort the given List with a default OrderComparator.
	 * 
	 * <p> 使用默认的OrderComparator对给定的List进行排序。
	 * 
	 * <p>Optimized to skip sorting for lists with size 0 or 1,
	 * in order to avoid unnecessary array extraction.
	 * 
	 * <p> 优化为跳过排序大小为0或1的列表，以避免不必要的数组提取。
	 * 
	 * @param list the List to sort
	 * @see java.util.Collections#sort(java.util.List, java.util.Comparator)
	 */
	public static void sort(List<?> list) {
		if (list.size() > 1) {
			Collections.sort(list, INSTANCE);
		}
	}

	/**
	 * Sort the given array with a default OrderComparator.
	 * 
	 * <p> 使用默认的OrderComparator对给定数组进行排序。
	 * 
	 * <p>Optimized to skip sorting for lists with size 0 or 1,
	 * in order to avoid unnecessary array extraction.
	 * 
	 * <p> 优化为跳过排序大小为0或1的列表，以避免不必要的数组提取。
	 * 
	 * @param array the array to sort - 要排序的数组
	 * @see java.util.Arrays#sort(Object[], java.util.Comparator)
	 */
	public static void sort(Object[] array) {
		if (array.length > 1) {
			Arrays.sort(array, INSTANCE);
		}
	}

}
