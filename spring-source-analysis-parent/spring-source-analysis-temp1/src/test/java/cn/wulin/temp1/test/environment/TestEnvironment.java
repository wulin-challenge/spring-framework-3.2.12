package cn.wulin.temp1.test.environment;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.environment.UserProperties;

@SuppressWarnings("resource")
public class TestEnvironment {
	
	public static void main(String[] args) {
		
		ApplicationContext en=new ClassPathXmlApplicationContext("environment/environment.xml");
		
		UserProperties bean = en.getBean("userProperties", UserProperties.class);
		
		System.out.println(bean);
	}

}
