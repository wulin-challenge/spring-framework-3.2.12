package cn.wulin.temp1.test.aop;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import cn.wulin.temp1.domain.aop.TestAopBean;
@SuppressWarnings({"resource","deprecation"})
public class TestAop {
	
	/**
	 * ProxyCreatorSupport --> 
	 * return getAopProxyFactory().createAopProxy(this);
	 * --> 131
	 */
	
	@Test
	public void testAop() {
		ApplicationContext  context = new ClassPathXmlApplicationContext("aop/spring-aop.xml");
		
		TestAopBean testAop = (TestAopBean)context.getBean("test_aop");
		testAop.test2();
		System.out.println("88888888888888888888888888");
	}

}
