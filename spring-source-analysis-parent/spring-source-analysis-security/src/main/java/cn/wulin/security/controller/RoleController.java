package cn.wulin.security.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.wulin.security.token.authentication.CustomTokenUsernamePasswordAuthenticationToken;

@Controller
@RequestMapping("/role")
public class RoleController {

	@RequestMapping("/getRoleUser")
	public @ResponseBody Map<String,Object> getRoleUser(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Map<String,Object> tokenInfo = new HashMap<String,Object>();
		tokenInfo.put("username", authentication.getPrincipal()); 
		return tokenInfo;
	}
}
