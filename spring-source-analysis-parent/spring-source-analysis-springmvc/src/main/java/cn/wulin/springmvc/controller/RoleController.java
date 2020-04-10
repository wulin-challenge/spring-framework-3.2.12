package cn.wulin.springmvc.controller;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("roleController")
public class RoleController {
	
	@RequestMapping("index.html")
	public String index() {
		return "role_index";
	}

}
