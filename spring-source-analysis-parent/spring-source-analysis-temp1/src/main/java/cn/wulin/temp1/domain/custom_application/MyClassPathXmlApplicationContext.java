package cn.wulin.temp1.domain.custom_application;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MyClassPathXmlApplicationContext extends ClassPathXmlApplicationContext{

	public MyClassPathXmlApplicationContext(String... configLocations) throws BeansException {
		super(configLocations);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initPropertySources() {
		//添加验证要求
//		getEnvironment().setRequiredProperties("var");
	}

	
}
