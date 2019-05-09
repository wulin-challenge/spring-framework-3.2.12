package cn.wulin.temp1.test.jdbc.tx;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.Constants;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import cn.wulin.temp1.domain.jdbc.tx.User;
import cn.wulin.temp1.domain.jdbc.tx.UserService;
import cn.wulin.temp1.domain.jdbc.tx.UserServiceImpl;


@SuppressWarnings("resource")
public class TestJdbcTx {
	
	@Test
	public void testJdbctx(){
		
		/**
		 * class:AbstractPlatformTransactionManager
		 * code:TransactionSynchronizationManager.initSynchronization();
		 * lineNumber:699
		 */
		ApplicationContext context = new ClassPathXmlApplicationContext("jdbc/tx/jdbc_tx.xml");
		
		//案例中不同包下有多个 UserService,要注意...
		UserService userService = (UserService)context.getBean("userService");
		
		User user = new User();
		user.setName("王敏");
		user.setAge(21);
		user.setSex("女");
		
		//保存一条记录
		userService.save(user);
		//上述源代码分析已完成.......................
		List<User> users = userService.getUsers();
		System.out.println(users);
	}
	
	@Test
	public void testFindOne(){
		
		ApplicationContext context = new ClassPathXmlApplicationContext("jdbc/spring/jdbc_spring.xml");
		//案例中不同包下有多个 UserService,要注意...
		UserService userService = (UserService)context.getBean("userService");
		User findOne = userService.findOne();
		System.out.println(findOne);
	}
	
	@Test
	public void testFindName(){
		
		ApplicationContext context = new ClassPathXmlApplicationContext("jdbc/spring/jdbc_spring.xml");
		//案例中不同包下有多个 UserService,要注意...
		UserService userService = (UserService)context.getBean("userService");
		String findOne = userService.findName();
		System.out.println(findOne);
	}
	
	@Test
	public void testSpringContents(){
		 Constants constants = new Constants(TransactionDefinition.class);
		 System.out.println(constants);
	}
	
	@Test
	public void testMethodCacheKey() throws NoSuchMethodException, SecurityException{
		UserService userService1 = new UserServiceImpl();
		UserService userService2 = new UserServiceImpl();
		
		Method method1 = userService1.getClass().getMethod("getUsers");
		Method method2 = userService1.getClass().getMethod("getUsers");
		
		MethodCacheKey methodCacheKey1 = new MethodCacheKey(method1);
		MethodCacheKey methodCacheKey2 = new MethodCacheKey(method2);
		
		if(methodCacheKey1 == methodCacheKey2){
			System.out.println("1111111111");
		}
		
		if(methodCacheKey1.equals(methodCacheKey2)){
			
			System.out.println("33333333333333333");
		}
		System.out.println("12222222222222222");
		
	}
	
	/**
	 * Simple wrapper class around a Method. Used as the key when
	 * caching methods, for efficient equals and hashCode comparisons.
	 * 
	 * <p> 围绕Method的简单包装类。 用作缓存方法时的键，用于有效的equals和hashCode比较。
	 */
	private static class MethodCacheKey {

		private final Method method;

		private final int hashCode;

		public MethodCacheKey(Method method) {
			this.method = method;
			this.hashCode = method.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			MethodCacheKey otherKey = (MethodCacheKey) other;
			return (this.method == otherKey.method);
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}
	
//	

}
