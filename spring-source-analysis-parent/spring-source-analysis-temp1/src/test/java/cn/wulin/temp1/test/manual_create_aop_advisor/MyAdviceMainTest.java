package cn.wulin.temp1.test.manual_create_aop_advisor;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 我的通知测试,并实现拦截注解具有继承性
 * @author ThinkPad
 *
 */
public class MyAdviceMainTest {

	@Test
	public void testCustomAop() {
		ApplicationContext  context = new ClassPathXmlApplicationContext("manual_create_aop_advisor/spring-manual_create_aop_advisor.xml");

		MyAdviceUser user = context.getBean("myAdviceUser", MyAdviceUser.class);
		String say = user.say("wulin");
		
		System.out.println(say);
	}
}
