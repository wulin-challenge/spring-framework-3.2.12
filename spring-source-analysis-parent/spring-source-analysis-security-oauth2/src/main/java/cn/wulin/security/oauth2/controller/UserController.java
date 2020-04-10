package cn.wulin.security.oauth2.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import cn.wulin.security.oauth2.domain.User;

public class UserController extends AbstractController implements InitializingBean{

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		List<User> userList = new ArrayList<User>();
		
		userList.add(new User(1L,"张三", 27));
		userList.add(new User(2L,"李四", 37));
		
		return new ModelAndView("userlist","users",userList);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("22222222222222222");
		System.out.println("22222222222222222");
		System.out.println("22222222222222222");
		System.out.println("22222222222222222");
		System.out.println("注解handlerMapping: RequestMappingHandlerMapping");
	}

}
