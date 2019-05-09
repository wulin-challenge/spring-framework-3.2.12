package cn.wulin.temp1.test.properties.bean_factory_post_processor;

import org.junit.Test;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import cn.wulin.temp1.domain.properties.bean_factory_post_processor.SimplePostProcessor;

@SuppressWarnings("deprecation")
public class TestBeanFactoryPostProcessor {
	
	@Test
	public void testBeanFactoryPostProcessor(){
		
		ConfigurableListableBeanFactory bf = new XmlBeanFactory(new ClassPathResource("properties/bean_factory_post_processor/bean_factory_post_processor.xml"));
		
		BeanFactoryPostProcessor bfpp = (BeanFactoryPostProcessor) bf.getBean("bfpp");
		bfpp.postProcessBeanFactory(bf);
		SimplePostProcessor bean = (SimplePostProcessor) bf.getBean("simpleBean");
		System.out.println(bean);
	}

}
