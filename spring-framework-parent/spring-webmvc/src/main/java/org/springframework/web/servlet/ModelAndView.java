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

package org.springframework.web.servlet;

import java.util.Map;

import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;

/**
 * Holder for both Model and View in the web MVC framework.
 * Note that these are entirely distinct. This class merely holds
 * both to make it possible for a controller to return both model
 * and view in a single return value.
 * 
 * <p> Web MVC框架中的Model和View的持有者。 请注意，这些完全不同。 
 * 这个类只是为了使控制器能够在单个返回值中返回模型和视图。
 *
 * <p>Represents a model and view returned by a handler, to be resolved
 * by a DispatcherServlet. The view can take the form of a String
 * view name which will need to be resolved by a ViewResolver object;
 * alternatively a View object can be specified directly. The model
 * is a Map, allowing the use of multiple objects keyed by name.
 * 
 * <p> 表示由处理程序返回的模型和视图，由DispatcherServlet解析。 视图可以采用String视图名称的形式，
 * 需要由ViewResolver对象解析; 或者，可以直接指定View对象。 该模型是一个Map，允许使用按名称键入的多个对象。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see DispatcherServlet
 * @see ViewResolver
 * @see HandlerAdapter#handle
 * @see org.springframework.web.servlet.mvc.Controller#handleRequest
 */
public class ModelAndView {

	/** View instance or view name String */
	/** 查看实例或视图名称String */
	private Object view;

	/** Model Map */
	private ModelMap model;

	/** Indicates whether or not this instance has been cleared with a call to {@link #clear()} */
	/** 指示是否已通过调用clear（）清除此实例 */
	private boolean cleared = false;


	/**
	 * Default constructor for bean-style usage: populating bean
	 * properties instead of passing in constructor arguments.
	 * 
	 * <p> bean样式用法的默认构造函数：填充bean属性而不是传入构造函数参数。
	 * 
	 * @see #setView(View)
	 * @see #setViewName(String)
	 */
	public ModelAndView() {
	}

	/**
	 * Convenient constructor when there is no model data to expose.
	 * Can also be used in conjunction with {@code addObject}.
	 * 
	 * <p> 没有要公开的模型数据时方便的构造函数。 也可以与addObject一起使用。
	 * 
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet's ViewResolver
	 * 
	 * <p> 要呈现的视图的名称，由DispatcherServlet的ViewResolver解析
	 * 
	 * @see #addObject
	 */
	public ModelAndView(String viewName) {
		this.view = viewName;
	}

	/**
	 * Convenient constructor when there is no model data to expose.
	 * Can also be used in conjunction with {@code addObject}.
	 * 
	 * <p> 没有要公开的模型数据时方便的构造函数。 也可以与addObject一起使用。
	 * 
	 * @param view View object to render - 查看要渲染的对象
	 * @see #addObject
	 */
	public ModelAndView(View view) {
		this.view = view;
	}

	/**
	 * Creates new ModelAndView given a view name and a model.
	 * 
	 * <p> 给定视图名称和模型，创建新的ModelAndView。
	 * 
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet's ViewResolver
	 * 
	 * <p> 要呈现的视图的名称，由DispatcherServlet的ViewResolver解析
	 * 
	 * @param model Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be {@code null}, but the
	 * model Map may be {@code null} if there is no model data.
	 * 
	 * <p> 模型名称（字符串）到模型对象（对象）的映射。 模型条目可能不为null，但如果没有模型数据，模型Map可能为null。
	 * 
	 */
	public ModelAndView(String viewName, Map<String, ?> model) {
		this.view = viewName;
		if (model != null) {
			getModelMap().addAllAttributes(model);
		}
	}

	/**
	 * Creates new ModelAndView given a View object and a model.
	 * <emphasis>Note: the supplied model data is copied into the internal
	 * storage of this class. You should not consider to modify the supplied
	 * Map after supplying it to this class</emphasis>
	 * 
	 * <p> 给定View对象和模型，创建新的ModelAndView。 注意：提供的模型数据将复制到此类的内部存储中。 在将其提供给此类后，您不应该考虑修改提供的Map
	 * 
	 * @param view View object to render - 查看要渲染的对象
	 * @param model Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be {@code null}, but the
	 * model Map may be {@code null} if there is no model data.
	 * 
	 * <p> 模型名称（字符串）到模型对象（对象）的映射。 模型条目可能不为null，但如果没有模型数据，模型Map可能为null。
	 */
	public ModelAndView(View view, Map<String, ?> model) {
		this.view = view;
		if (model != null) {
			getModelMap().addAllAttributes(model);
		}
	}

	/**
	 * Convenient constructor to take a single model object.
	 * 
	 * <p> 方便的构造函数来获取单个模型对象。
	 * 
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet's ViewResolver
	 * 
	 * <p> 要呈现的视图的名称，由DispatcherServlet的ViewResolver解析
	 * 
	 * @param modelName name of the single entry in the model
	 * 
	 * <p> 模型中单个条目的名称
	 * 
	 * @param modelObject the single model object - 单个模型对象
	 */
	public ModelAndView(String viewName, String modelName, Object modelObject) {
		this.view = viewName;
		addObject(modelName, modelObject);
	}

	/**
	 * Convenient constructor to take a single model object.
	 * 
	 * <p> 方便的构造函数来获取单个模型对象。
	 * 
	 * @param view View object to render - 查看要渲染的对象
	 * @param modelName name of the single entry in the model - 模型中单个条目的名称
	 * @param modelObject the single model object - 单个模型对象
	 */
	public ModelAndView(View view, String modelName, Object modelObject) {
		this.view = view;
		addObject(modelName, modelObject);
	}


	/**
	 * Set a view name for this ModelAndView, to be resolved by the
	 * DispatcherServlet via a ViewResolver. Will override any
	 * pre-existing view name or View.
	 * 
	 * <p> 设置此ModelAndView的视图名称，由DispatcherServlet通过ViewResolver解析。 
	 * 将覆盖任何预先存在的视图名称或视图。
	 * 
	 */
	public void setViewName(String viewName) {
		this.view = viewName;
	}

	/**
	 * Return the view name to be resolved by the DispatcherServlet
	 * via a ViewResolver, or {@code null} if we are using a View object.
	 * 
	 * <p> 返回由DispatcherServlet通过ViewResolver解析的视图名称，如果我们使用View对象，则返回null。
	 */
	public String getViewName() {
		return (this.view instanceof String ? (String) this.view : null);
	}

	/**
	 * Set a View object for this ModelAndView. Will override any
	 * pre-existing view name or View.
	 * 
	 * <p> 为此ModelAndView设置View对象。 将覆盖任何预先存在的视图名称或视图。
	 */
	public void setView(View view) {
		this.view = view;
	}

	/**
	 * Return the View object, or {@code null} if we are using a view name
	 * to be resolved by the DispatcherServlet via a ViewResolver.
	 * 
	 * <p> 返回View对象，如果我们使用视图名称由DispatcherServlet通过ViewResolver解析，则返回null。
	 */
	public View getView() {
		return (this.view instanceof View ? (View) this.view : null);
	}

	/**
	 * Indicate whether or not this {@code ModelAndView} has a view, either
	 * as a view name or as a direct {@link View} instance.
	 * 
	 * <p> 指示此ModelAndView是否具有视图，可以是视图名称，也可以是直接View实例。
	 */
	public boolean hasView() {
		return (this.view != null);
	}

	/**
	 * Return whether we use a view reference, i.e. {@code true}
	 * if the view has been specified via a name to be resolved by the
	 * DispatcherServlet via a ViewResolver.
	 * 
	 * <p> 返回我们是否使用视图引用，即如果通过名称指定视图以由DispatcherServlet通过ViewResolver解析，则返回true。
	 * 
	 */
	public boolean isReference() {
		return (this.view instanceof String);
	}

	/**
	 * Return the model map. May return {@code null}.
	 * Called by DispatcherServlet for evaluation of the model.
	 * 
	 * <p> 返回模型图。 可能会返回null。 由DispatcherServlet调用以评估模型。
	 */
	protected Map<String, Object> getModelInternal() {
		return this.model;
	}

	/**
	 * Return the underlying {@code ModelMap} instance (never {@code null}).
	 * 
	 * <p> 返回底层的ModelMap实例（永远不为null）。
	 * 
	 */
	public ModelMap getModelMap() {
		if (this.model == null) {
			this.model = new ModelMap();
		}
		return this.model;
	}

	/**
	 * Return the model map. Never returns {@code null}.
	 * To be called by application code for modifying the model.
	 * 
	 * <p> 返回模型图。 永远不会返回null。 由应用程序代码调用以修改模型。
	 */
	public Map<String, Object> getModel() {
		return getModelMap();
	}


	/**
	 * Add an attribute to the model.
	 * 
	 * <p> 向模型添加属性。
	 * 
	 * @param attributeName name of the object to add to the model
	 * 
	 * <p> 要添加到模型的对象的名称
	 * 
	 * @param attributeValue object to add to the model (never {@code null})
	 * 
	 * <p> 要添加到模型的对象（永远不为null）
	 * 
	 * @see ModelMap#addAttribute(String, Object)
	 * @see #getModelMap()
	 */
	public ModelAndView addObject(String attributeName, Object attributeValue) {
		getModelMap().addAttribute(attributeName, attributeValue);
		return this;
	}

	/**
	 * Add an attribute to the model using parameter name generation.
	 * 
	 * <p> 使用参数名称生成向模型添加属性。
	 * 
	 * @param attributeValue the object to add to the model (never {@code null})
	 * 
	 * <p> 要添加到模型的对象（从不为null）
	 * 
	 * @see ModelMap#addAttribute(Object)
	 * @see #getModelMap()
	 */
	public ModelAndView addObject(Object attributeValue) {
		getModelMap().addAttribute(attributeValue);
		return this;
	}

	/**
	 * Add all attributes contained in the provided Map to the model.
	 * 
	 * <p> 将提供的Map中包含的所有属性添加到模型中。
	 * 
	 * @param modelMap a Map of attributeName -> attributeValue pairs
	 * 
	 * <p> attributeName  - > attributeValue对的Map
	 * 
	 * @see ModelMap#addAllAttributes(Map)
	 * @see #getModelMap()
	 */
	public ModelAndView addAllObjects(Map<String, ?> modelMap) {
		getModelMap().addAllAttributes(modelMap);
		return this;
	}


	/**
	 * Clear the state of this ModelAndView object.
	 * The object will be empty afterwards.
	 * 
	 * <p> 清除此ModelAndView对象的状态。 之后该对象将为空。
	 * 
	 * <p>Can be used to suppress rendering of a given ModelAndView object
	 * in the {@code postHandle} method of a HandlerInterceptor.
	 * 
	 * <p> 可用于在HandlerInterceptor的postHandle方法中禁止呈现给定的ModelAndView对象。
	 * 
	 * @see #isEmpty()
	 * @see HandlerInterceptor#postHandle
	 */
	public void clear() {
		this.view = null;
		this.model = null;
		this.cleared = true;
	}

	/**
	 * Return whether this ModelAndView object is empty,
	 * i.e. whether it does not hold any view and does not contain a model.
	 * 
	 * <p> 返回此ModelAndView对象是否为空，即它是否不包含任何视图且不包含模型。
	 */
	public boolean isEmpty() {
		return (this.view == null && CollectionUtils.isEmpty(this.model));
	}

	/**
	 * Return whether this ModelAndView object is empty as a result of a call to {@link #clear}
	 * i.e. whether it does not hold any view and does not contain a model.
	 * 
	 * <p> 返回此ModelAndView对象是否由于调用清除而为空，即它是否不包含任何视图且不包含模型。
	 * 
	 * <p>Returns {@code false} if any additional state was added to the instance
	 * <strong>after</strong> the call to {@link #clear}.
	 * 
	 * <p> 如果在调用clear之后将任何其他状态添加到实例，则返回false。
	 * 
	 * @see #clear()
	 */
	public boolean wasCleared() {
		return (this.cleared && isEmpty());
	}


	/**
	 * Return diagnostic information about this model and view.
	 * 
	 * <p> 返回有关此模型和视图的诊断信息。
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ModelAndView: ");
		if (isReference()) {
			sb.append("reference to view with name '").append(this.view).append("'");
		}
		else {
			sb.append("materialized View is [").append(this.view).append(']');
		}
		sb.append("; model is ").append(this.model);
		return sb.toString();
	}

}
