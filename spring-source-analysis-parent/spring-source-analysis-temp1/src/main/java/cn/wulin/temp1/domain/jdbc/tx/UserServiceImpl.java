package cn.wulin.temp1.domain.jdbc.tx;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserServiceImpl implements UserService{
	
	private JdbcTemplate jdbcTemplate;
	
	//设置数据源
	public void setDataSource(DataSource dataSource){
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void save(User user) {
		String insertSql = "insert into user(name,age,sex) values(?,?,?)";
		
		Object[] params = new Object[3];
		params[0] = user.getName();
		params[1] = user.getAge();
		params[2] = user.getSex();
		
		int[] paramTypes = new int[3];
		paramTypes[0] = java.sql.Types.VARCHAR;
		paramTypes[1] = java.sql.Types.INTEGER;
		paramTypes[2] = java.sql.Types.VARCHAR;
		
		jdbcTemplate.update(insertSql,params,paramTypes);
		
//		int i=1/0;
	}

	@Override
	public List<User> getUsers() {
		String querySql = "select * from user";
		RowMapper<User> mapper = new UserRowMapper();
		return jdbcTemplate.query(querySql,mapper);
	}

	@Override
	public List<User> getUsersHasParams() {
		RowMapper<User> mapper = new UserRowMapper();
		String querySql2 = "select * from user where name=?";
		Object[] params = new Object[1];
		params[0] = "张三";
		List<User> query = jdbcTemplate.query(querySql2, params, mapper);
		System.out.println("带参数查询: "+query);
		return query;
	}

	@Override
	public User findOne() {
		String querySql2 = "select * from user where id='2'";
		RowMapper<User> mapper = new UserRowMapper();
		return jdbcTemplate.queryForObject(querySql2, mapper);
	}

	@Override
	public String findName() {
		String querySql2 = "select name from user where id='2'";
		return jdbcTemplate.queryForObject(querySql2, String.class);
	}
}
