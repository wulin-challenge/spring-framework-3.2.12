package cn.wulin.temp1.test.constructor;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.constructor.ConstructorField;

@SuppressWarnings("resource")
public class TestConstructorField {
	
	@Test
	public void testConstructorField() {
		ApplicationContext  context = new ClassPathXmlApplicationContext("constructor/constructor-field.xml");
		
		ConstructorField constructorFieldBean = (ConstructorField)context.getBean("constructorFieldBean");
		String clazzName = constructorFieldBean.getClazzName();
		System.out.println(clazzName);
	}

}
