package cn.wulin.temp1.test.properties.bean_post_processor;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.properties.bean_post_processor.Apple;
import cn.wulin.temp1.domain.properties.bean_post_processor.MyInstantiationAwareBeanPostProcessor;

@SuppressWarnings("resource")
public class TestBeanPostProcessor {
	
	@Test
	public void testBeanPostProcessor(){
		ApplicationContext  context = new ClassPathXmlApplicationContext("properties/bean_post_processor/bean_post_processor.xml");
		
//		MyInstantiationAwareBeanPostProcessor post = (MyInstantiationAwareBeanPostProcessor)context.getBean("myBeanPostProcessor");
		Apple apple = (Apple)context.getBean("apple");
//		System.out.println(post);
		System.out.println(apple);
	}
	
	@Test
	public void testBigNumber(){
		long start = System.currentTimeMillis();
		long js = js();
		long end = System.currentTimeMillis();
		System.out.println(js+"-->"+(end-start));
	}
	
	private long js(){
		Long total = 100000000l;
		
		long j = 0;
		for (int i = 0; i < total; i++) {
			for (int k = 1; k < 301; k++) {
				j +=1;
			}
		}
		return j;
	}

}
