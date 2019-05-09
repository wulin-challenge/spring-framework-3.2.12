package cn.wulin.temp1.test.event_multicaster;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.event_multicaster.TestEvent;

@SuppressWarnings("resource")
public class TestEventMulticaster {
	
	@Test
	public void testEventMulticaster(){
		ApplicationContext context = new ClassPathXmlApplicationContext("event_multicaster/event_multicaster.xml");
		
		TestEvent event = new TestEvent("hello","world");
		context.publishEvent(event);
	}

}
