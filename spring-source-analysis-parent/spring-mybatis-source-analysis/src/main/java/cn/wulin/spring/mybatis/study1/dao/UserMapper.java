package cn.wulin.spring.mybatis.study1.dao;

import cn.wulin.spring.mybatis.study1.domain.User;

public interface UserMapper {
	
	public User selectOne(Integer id);
	
	public Integer save(User user);

}
