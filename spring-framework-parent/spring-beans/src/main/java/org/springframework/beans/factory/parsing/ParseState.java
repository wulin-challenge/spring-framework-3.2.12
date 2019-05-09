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

import java.util.Stack;

/**
 * Simple {@link Stack}-based structure for tracking the logical position during
 * a parsing process. {@link Entry entries} are added to the stack at
 * each point during the parse phase in a reader-specific manner.
 * 
 * <p>简单的基于堆栈的结构，用于在解析过程中跟踪逻辑位置。 在解析阶段期间，以读者特定的方式将条目添加到堆栈中。
 *
 * <p>Calling {@link #toString()} will render a tree-style view of the current logical
 * position in the parse phase. This representation is intended for use in
 * error messages.
 * 
 * <p>调用toString（）将呈现解析阶段中当前逻辑位置的树形视图。 此表示旨在用于错误消息。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public final class ParseState {

	/**
	 * Tab character used when rendering the tree-style representation.
	 * 
	 * <p>渲染树形表示时使用的制表符。
	 */
	private static final char TAB = '\t';

	/**
	 * Internal {@link Stack} storage.
	 * 
	 * <p>内部堆栈存储。
	 */
	private final Stack state;


	/**
	 * Create a new {@code ParseState} with an empty {@link Stack}.
	 * 
	 * <p>使用空Stack创建一个新的ParseState。
	 */
	public ParseState() {
		this.state = new Stack();
	}

	/**
	 * Create a new {@code ParseState} whose {@link Stack} is a {@link Object#clone clone}
	 * of that of the passed in {@code ParseState}.
	 * 
	 * <p>创建一个新的ParseState，其Stack是传递的ParseState的克隆。
	 */
	private ParseState(ParseState other) {
		this.state = (Stack) other.state.clone();
	}


	/**
	 * Add a new {@link Entry} to the {@link Stack}.
	 * 
	 * <p>向堆栈添加新条目。
	 */
	public void push(Entry entry) {
		this.state.push(entry);
	}

	/**
	 * Remove an {@link Entry} from the {@link Stack}.
	 * 
	 * <p>从堆栈中删除条目。
	 */
	public void pop() {
		this.state.pop();
	}

	/**
	 * Return the {@link Entry} currently at the top of the {@link Stack} or
	 * {@code null} if the {@link Stack} is empty.
	 * 
	 * <p>返回当前位于堆栈顶部的Entry，如果Stack为空，则返回null。
	 */
	public Entry peek() {
		return (Entry) (this.state.empty() ? null : this.state.peek());
	}

	/**
	 * Create a new instance of {@link ParseState} which is an independent snapshot
	 * of this instance.
	 * 
	 * <p>创建ParseState的新实例，该实例是此实例的独立快照。
	 */
	public ParseState snapshot() {
		return new ParseState(this);
	}


	/**
	 * Returns a tree-style representation of the current {@code ParseState}.
	 * 
	 * <p>返回当前ParseState的树型表示形式。
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < this.state.size(); x++) {
			if (x > 0) {
				sb.append('\n');
				for (int y = 0; y < x; y++) {
					sb.append(TAB);
				}
				sb.append("-> ");
			}
			sb.append(this.state.get(x));
		}
		return sb.toString();
	}


	/**
	 * Marker interface for entries into the {@link ParseState}.
	 * 
	 * <p>用于进入ParseState的条目的标记接口。
	 */
	public interface Entry {

	}

}
