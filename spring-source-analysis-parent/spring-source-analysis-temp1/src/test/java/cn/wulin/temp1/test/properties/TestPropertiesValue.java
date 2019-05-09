package cn.wulin.temp1.test.properties;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import cn.wulin.temp1.domain.properties.TestProperies;

@SuppressWarnings(value={"deprecation","resource"})
public class TestPropertiesValue {
	
	/**
	 * AbstractApplicationContext _>
	 * 
	 * if (this.allowBeanDefinitionOverriding != null) {
	 * 
	 * 268
	 */
	
	@Test
	public void testProperties() {
		
		ApplicationContext bf=new ClassPathXmlApplicationContext("properties/spring-properties.xml");
//		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("properties/spring-properties.xml"));
		
		TestProperies testProperies = (TestProperies) bf.getBean("testProperies");
		System.out.println(testProperies);
	}

}
