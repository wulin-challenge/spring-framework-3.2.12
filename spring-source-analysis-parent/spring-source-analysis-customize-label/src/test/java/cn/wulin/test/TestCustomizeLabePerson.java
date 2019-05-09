package cn.wulin.test;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import cn.wulin.Person;
@SuppressWarnings("deprecation")
public class TestCustomizeLabePerson {
	
	
	@Test
	public void testPerson(){
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("spring-customize.xml"));
		Person person = (Person) bf.getBean("person");
		System.out.println(person);
	}

}
