package cn.wulin.temp1.test.manual_create_aop_advisor;

import cn.wulin.temp1.domain.manual_create_aop_advisor.MyAdvice;

public class MyAdviceUser extends BaseController{
	
	@MyAdvice("userSay")
	public String say(String name) {
		System.out.println(name);
		return name +" 你好,这是真实的实现!";
	}

}
