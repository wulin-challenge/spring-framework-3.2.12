package cn.wulin.temp1.domain.jdbc.tx;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation=Propagation.REQUIRED)
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
