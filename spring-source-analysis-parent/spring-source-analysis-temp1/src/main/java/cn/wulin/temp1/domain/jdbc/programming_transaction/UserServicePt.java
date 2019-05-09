package cn.wulin.temp1.domain.jdbc.programming_transaction;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface UserServicePt {
	
	public void save(UserPt user);
	
	public void multiThreadSave(UserPt user);
	
	public List<UserPt> getUsers();
	
	/**
	 * 通过传递参数的形式得到User
	 * @return
	 */
	public List<UserPt> getUsersHasParams();
	
	public UserPt findOne();
	
	public String findName();

}
