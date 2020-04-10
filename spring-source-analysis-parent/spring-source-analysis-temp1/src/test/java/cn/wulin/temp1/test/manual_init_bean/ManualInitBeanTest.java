package cn.wulin.temp1.test.manual_init_bean;


import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.manual_init_bean.ManualInitBeanProcessor;
import cn.wulin.temp1.domain.manual_init_bean.Role;

/**
 * 手动初始化bean处理器,即可以自动组装值和回调 beanPostProcesser 和 InitializingBean的回调
 */
public class ManualInitBeanTest {
	
	@Test
	public void manualInitBeanTest(){
		ApplicationContext  context = new ClassPathXmlApplicationContext("manual_init_bean/spring-manual_init_bean.xml");
		
		
		ManualInitBeanProcessor manualInitBeanProcessor = (ManualInitBeanProcessor)context.getBean("manualInitBeanProcessor");
		
		Role role = new Role(1L, "firstRole");
		manualInitBeanProcessor.manualInitBean(role, "xx");
		
		System.out.println(role.getMenu().getName());
	}

}
