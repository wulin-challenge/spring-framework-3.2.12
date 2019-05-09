package cn.wulin.temp1.test.component;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.component.CUser;
import cn.wulin.temp1.domain.component.CUserService;

public class TestComponent {

	@SuppressWarnings("resource")
	@Test
	public void testComponent() {
		ApplicationContext  context = new ClassPathXmlApplicationContext("component/spring-component.xml");
		CUserService userService = context.getBean(CUserService.class);
		CUser cUser = userService.getCUser();
		System.out.println(cUser.getUsername()+":"+cUser.getPassword());
		System.out.println();
	}
}
