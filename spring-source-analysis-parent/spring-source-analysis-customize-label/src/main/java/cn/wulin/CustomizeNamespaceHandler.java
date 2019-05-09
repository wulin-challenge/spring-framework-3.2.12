package cn.wulin;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class CustomizeNamespaceHandler extends NamespaceHandlerSupport{

	@Override
	public void init() {
		/**
    	 * 子类可以调用它来注册提供的BeanDefinitionParser来处理指定的元素。元素名称是本地（非名称空间限定）名称
    	 */
        registerBeanDefinitionParser("person", new PersonBeanDefinitionParser());
	}

}
