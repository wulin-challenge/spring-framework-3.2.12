package cn.wulin.temp1.domain.cache.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import cn.wulin.temp1.domain.cache.domain.Account;

public class AccountServiceImpl implements AccountService,InitializingBean{

	private static final ConcurrentHashMap<String/*username*/,Account> accounts = new ConcurrentHashMap<String/*username*/,Account>();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		accounts.put("zs",new Account("zs",5L));
		accounts.put("ls",new Account("ls",10L));
		accounts.put("zl",new Account("zl",12L));
	}
	
	public List<Account> findAll(){
		System.out.println("执行数据库: findAll");
		return accounts.values().stream().collect(Collectors.toList());
	}

	@Cacheable(value = "account_cache",key = "#username")
	public Account findByUsername(String username) {
		System.out.println("执行数据库: findByUsername");
		Account account = accounts.get(username);
		return account;
	}
	
	@Cacheable(value = "account_cache", key = "#account.getUsername()")
	public Account save(Account account) {
		
		System.out.println("执行数据库: save");
		accounts.put(account.getUsername(),account);
		return account;
	}
	
	@CachePut(value = "account_cache",key = "#account.getUsername()")
	public Account update(Account account) {
		
		System.out.println("执行数据库: update");
		accounts.put(account.getUsername(),account);
		return account;
	}
	
	@CacheEvict(value="account_cache",key = "#username")
	public void delete(String username) {
		
		System.out.println("执行数据库: delete");
		accounts.remove(username);
	}
}
