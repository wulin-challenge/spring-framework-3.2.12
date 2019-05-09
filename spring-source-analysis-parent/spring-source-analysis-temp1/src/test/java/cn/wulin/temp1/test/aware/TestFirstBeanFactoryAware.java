package cn.wulin.temp1.test.aware;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import cn.wulin.temp1.domain.aware.FirstBeanFactoryAware;

@SuppressWarnings("deprecation")
public class TestFirstBeanFactoryAware {
	
	@Test
	public void testFirstBeanFactoryAware1(){
		
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("aware/spring-first-factory-aware.xml"));
		FirstBeanFactoryAware firstBeanFactoryAware = (FirstBeanFactoryAware) bf.getBean("firstBeanFactoryAware");
		firstBeanFactoryAware.printHello();
		System.out.println(firstBeanFactoryAware);
	}

}
