package cn.wulin.temp1.domain.jdbc.spring;

import java.util.List;

public interface UserService {
	
	public void save(User user);
	
	public List<User> getUsers();
	
	/**
	 * 通过传递参数的形式得到User
	 * @return
	 */
	public List<User> getUsersHasParams();
	
	public User findOne();
	
	public String findName();

}
