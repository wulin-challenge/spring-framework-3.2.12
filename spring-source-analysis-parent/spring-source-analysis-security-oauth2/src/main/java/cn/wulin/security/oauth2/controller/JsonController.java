package cn.wulin.security.oauth2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.wulin.security.oauth2.domain.User;

@Controller
@RequestMapping("json")
public class JsonController {
	
	@RequestMapping("getUser")
	public @ResponseBody User getUser() {
		return new User(1L, "测试", 27);
	}

}
