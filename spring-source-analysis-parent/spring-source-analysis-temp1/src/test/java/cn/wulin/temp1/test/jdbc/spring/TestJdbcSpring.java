package cn.wulin.temp1.test.jdbc.spring;

import java.util.List;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.jdbc.spring.User;
import cn.wulin.temp1.domain.jdbc.spring.UserService;

@SuppressWarnings("resource")
public class TestJdbcSpring {
	
	@Test
	public void testJdbcSpring(){
		
		ApplicationContext context = new ClassPathXmlApplicationContext("jdbc/spring/jdbc_spring.xml");
		
		//案例中不同包下有多个 UserService,要注意...
		UserService userService = (UserService)context.getBean("userService");
		
		User user = new User();
		user.setName("张三");
		user.setAge(20);
		user.setSex("男");
		
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

}
