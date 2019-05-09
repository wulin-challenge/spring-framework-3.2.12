package cn.wulin.spring.mybatis.study1.test;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.spring.mybatis.study1.dao.UserMapper;
import cn.wulin.spring.mybatis.study1.domain.User;
import cn.wulin.spring.mybatis.study1.service.UserService;

@SuppressWarnings("unused")
public class TestSpringMybatis {
	
	private ApplicationContext  context = null;
	
	
	@Test
	public void testfindOne(){
		UserMapper userMapper = (UserMapper) getContext().getBean("userMapper");
		
		UserMapper userMapper2 = (UserMapper) getContext().getBean(UserMapper.class);
		
		UserService userService = (UserService)getContext().getBean("userService");
		UserService bean = getContext().getBean(UserService.class);
		User findOne = userService.findOne(18);
		
		System.out.println(findOne);
		
	}
	
	@Test
	public void testUserMapper(){
		UserMapper userMapper = (UserMapper)getContext().getBean("userMapper");
		User selectOne = userMapper.selectOne(3);
		System.out.println(selectOne);
	}
	
	private ApplicationContext getContext(){
		if(context == null){
			context = new ClassPathXmlApplicationContext("study1/study1-spring-mybatis.xml");
		}
		return context;
	}

}
