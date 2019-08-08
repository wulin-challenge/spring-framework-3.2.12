package cn.wulin.springmvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import cn.wulin.springmvc.domain.User;

public class UserController extends AbstractController implements InitializingBean{

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		List<User> userList = new ArrayList<User>();
		
		userList.add(new User("张三", 27));
		userList.add(new User("李四", 37));
		
		return new ModelAndView("userlist","users",userList);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("1111111111111111111111111111111111111");
		System.out.println("1111111111111111111111111111111111111");
		System.out.println("1111111111111111111111111111111111111");
		System.out.println("1111111111111111111111111111111111111");
	}

}
