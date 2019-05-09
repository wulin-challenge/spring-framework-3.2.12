package cn.wulin.spring.mybatis.study1.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;

import cn.wulin.spring.mybatis.study1.dao.UserMapper;
import cn.wulin.spring.mybatis.study1.domain.User;

public class UserService {
	
	private SqlSessionFactoryBean sqlSessionFactory;
	
	public User findOne(Integer id){
		SqlSession openSession = getSqlSessionFactory().openSession();
		try {
			UserMapper mapper = openSession.getMapper(UserMapper.class);
			
			User selectOne = mapper.selectOne(id);
			return selectOne;
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			openSession.close();
		}
		return null;
	}
	
	public Integer save(User user){
		SqlSession openSession = getSqlSessionFactory().openSession();
		try {
			UserMapper mapper = openSession.getMapper(UserMapper.class);
			Integer id = mapper.save(user);
			openSession.commit();
			return id;
		} finally{
			openSession.close();
		}
	}
	
	private SqlSessionFactory getSqlSessionFactory(){
		try {
			return sqlSessionFactory.getObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setSqlSessionFactory(SqlSessionFactoryBean sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}
	
}
