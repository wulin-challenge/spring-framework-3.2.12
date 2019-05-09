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

/**
 * Interface that can be implemented by objects that should be
 * orderable, for example in a Collection.
 * 
 * <p> 可以由可订购的对象实现的接口，例如在Collection中。
 *
 * <p>The actual order can be interpreted as prioritization, with
 * the first object (with the lowest order value) having the highest
 * priority.
 * 
 * <p> 实际订单可以解释为优先级，第一个对象（具有最低订单值）具有最高优先级。
 *
 * <p>Note that there is a 'priority' marker for this interface:
 * {@link PriorityOrdered}. Order values expressed by PriorityOrdered
 * objects always apply before order values of 'plain' Ordered values.
 * 
 * <p> 请注意，此接口有一个“优先级”标记：PriorityOrdered。 PriorityOrdered对象表示的订单值始
 * 终在“普通”有序值的订单值之前应用。
 *
 * @author Juergen Hoeller
 * @since 07.04.2003
 * @see OrderComparator
 * @see org.springframework.core.annotation.Order
 */
public interface Ordered {

	/**
	 * Useful constant for the highest precedence value.
	 * 
	 * <p> 最高优先级值的有用常量。
	 * @see java.lang.Integer#MIN_VALUE
	 */
	int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

	/**
	 * Useful constant for the lowest precedence value.
	 * 
	 * <p> 最低优先级值的有用常量。
	 * @see java.lang.Integer#MAX_VALUE
	 */
	int LOWEST_PRECEDENCE = Integer.MAX_VALUE;


	/**
	 * Return the order value of this object, with a
	 * higher value meaning greater in terms of sorting.
	 * 
	 * <p> 返回此对象的订单值，值越高意味着排序越大。
	 * 
	 * <p>Normally starting with 0, with {@code Integer.MAX_VALUE}
	 * indicating the greatest value. Same order values will result
	 * in arbitrary positions for the affected objects.
	 * 
	 * <p> 通常从0开始，Integer.MAX_VALUE表示最大值。 相同的排序值将导致受影响对象的任意位置。
	 * 
	 * <p>Higher values can be interpreted as lower priority. As a
	 * consequence, the object with the lowest value has highest priority
	 * (somewhat analogous to Servlet "load-on-startup" values).
	 * 
	 * <p> 较高的值可以解释为较低的优先级。 因此，具有最低值的对象具有最高优先级（有点类似于Servlet“load-on-startup”值）。
	 * 
	 * @return the order value - 排序值
	 */
	int getOrder();

}
