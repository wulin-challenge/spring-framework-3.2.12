package cn.wulin.temp1.domain.cache.service;

import java.util.List;
import cn.wulin.temp1.domain.cache.domain.Account;

public interface AccountService{

	public List<Account> findAll();

	public Account findByUsername(String username);
	
	public Account save(Account account);
	
	public Account update(Account account);
	
	public void delete(String username);
}
