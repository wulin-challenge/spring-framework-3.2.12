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
@RequestMapping("/")
public class TokenSecurityController {

	@RequestMapping("/tokenLogin")
	public @ResponseBody Map<String,Object> tokenLogin(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		CustomTokenUsernamePasswordAuthenticationToken token = (CustomTokenUsernamePasswordAuthenticationToken)authentication;
		Map<String,Object> tokenInfo = new HashMap<String,Object>();
		tokenInfo.put("token", token.getToken());
		tokenInfo.put("username", token.getPrincipal());
		tokenInfo.put("expireSecond", token.getExpireSecond());
		tokenInfo.put("status", "200");
		tokenInfo.put("info", "登录成功!");
		return tokenInfo;
	}
	
	@RequestMapping("/tokenNoLogin")
	public @ResponseBody Map<String,Object> tokenNoLogin(){
		
		Map<String,Object> tokenInfo = new HashMap<String,Object>();
		tokenInfo.put("token", "");
		tokenInfo.put("username","");
		tokenInfo.put("expireSecond", "");
		tokenInfo.put("status", "403");
		tokenInfo.put("info", "请求被拒绝");
		return tokenInfo;
	}
}
