package cn.wulin.temp1.test.aop.jdk;

import org.junit.Test;

public class ProxyTest {
	
	@Test
	public void testProxy(){
		//实例化目标对象
		UserService userService = new UserServiceImpl();
		//实例化InvocationHandler
		MyInvocationHandler handler = new MyInvocationHandler(userService);
		
		//根据目标对象生产代理对象
		UserService proxy = (UserService)handler.getProxy();
		
		//调用代理对象的方法
		proxy.add();
	}

}
