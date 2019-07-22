package cn.wulin.temp1.domain.manual_create_bean_and_aop.service;

import cn.wulin.temp1.domain.manual_create_bean_and_aop.manual_aop.ManualAop;

/**
 * 用于测试手动创建bean和aop
 * @author wulin
 *
 */
public class ManualAopUserService {
	
	@ManualAop
	public String saveUser(String user) {
		return user+":service";
	}
}
