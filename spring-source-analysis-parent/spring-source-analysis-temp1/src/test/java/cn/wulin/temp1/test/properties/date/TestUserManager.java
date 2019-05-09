package cn.wulin.temp1.test.properties.date;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.properties.date.UserManager;

@SuppressWarnings("resource")
public class TestUserManager {
	
	@Test
	public void testUserManager(){
		ApplicationContext bf=new ClassPathXmlApplicationContext("properties/date/spring-properties-date.xml");
		
		UserManager userManager = (UserManager) bf.getBean("userManager");
		System.out.println(userManager);
	}
}
