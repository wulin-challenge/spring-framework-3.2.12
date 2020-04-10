package cn.wulin.temp1.test.cache;

import java.util.List;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.wulin.temp1.domain.cache.domain.Account;
import cn.wulin.temp1.domain.cache.service.AccountService;
import cn.wulin.temp1.domain.cache.service.AccountServiceImpl;

public class TestCache {
	
	@Test
	@SuppressWarnings("resource")
	public void springCacheTest() {
		
		ApplicationContext  context = new ClassPathXmlApplicationContext("cache/spring-cache.xml");
		
		AccountService accountService = context.getBean(AccountService.class);
		
		for (int i = 0; i < 5; i++) {
			Account findByUsername = accountService.findByUsername("zs");
			System.out.println(findByUsername);
			if(i==0) {
				accountService.save(new Account("ww",20l));
			}
			accountService.update(new Account("ls",15l));
			accountService.delete("zl");
			
			List<Account> findAll = accountService.findAll();
			System.out.println(findAll);
		}
	}
}
