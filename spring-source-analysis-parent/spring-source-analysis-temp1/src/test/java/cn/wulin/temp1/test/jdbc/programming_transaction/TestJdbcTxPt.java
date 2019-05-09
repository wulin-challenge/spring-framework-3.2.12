package cn.wulin.temp1.test.jdbc.programming_transaction;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.Constants;
import org.springframework.transaction.TransactionDefinition;

import cn.wulin.temp1.domain.jdbc.programming_transaction.UserPt;
import cn.wulin.temp1.domain.jdbc.programming_transaction.UserServicePt;
import cn.wulin.temp1.domain.jdbc.tx.User;
import cn.wulin.temp1.domain.jdbc.tx.UserService;
import cn.wulin.temp1.domain.jdbc.tx.UserServiceImpl;


@SuppressWarnings("resource")
public class TestJdbcTxPt {
	
	@Test
	public void testJdbctxPt(){
		ApplicationContext context = new ClassPathXmlApplicationContext("jdbc/programming_transaction/jdbc_tx_pt.xml");
		
		//案例中不同包下有多个 UserService,要注意...
		UserServicePt userService = (UserServicePt)context.getBean("userService");
		
		UserPt user = new UserPt();
		user.setName("王敏");
		user.setAge(21);
		user.setSex("女");
		
		//保存一条记录
		userService.save(user);
		//上述源代码分析已完成.......................
		List<UserPt> users = userService.getUsers();
		System.out.println(users);
	}
	
	@Test
	public void testJdbctxPt_multi_thread(){
		ApplicationContext context = new ClassPathXmlApplicationContext("jdbc/programming_transaction/jdbc_tx_pt.xml");
		
		//案例中不同包下有多个 UserService,要注意...
		UserServicePt userService = (UserServicePt)context.getBean("userService");
		
		UserPt user = new UserPt();
		user.setName("王敏");
		user.setAge(21);
		user.setSex("女");
		
		//保存一条记录
		userService.save(user);
		//上述源代码分析已完成.......................
		List<UserPt> users = userService.getUsers();
		System.out.println(users);
	}
	
	@Test
	public void testFindOnePt(){
		
		ApplicationContext context = new ClassPathXmlApplicationContext("jdbc/programming_transaction/jdbc_tx_pt.xml");
		//案例中不同包下有多个 UserService,要注意...
		UserServicePt userService = (UserServicePt)context.getBean("userService");
		UserPt findOne = userService.findOne();
		System.out.println(findOne);
	}
	


}
