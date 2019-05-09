package cn.wulin.temp1.domain.component;

import org.springframework.stereotype.Component;

@Component
public class CUserService {
	
	public CUser getCUser(){
		
		CUser user = new CUser();
		user.setUsername("张三");
		user.setPassword("123456");
		return user;
	}

}
