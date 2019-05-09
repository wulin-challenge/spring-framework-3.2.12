package cn.wulin.temp1.domain.jdbc.programming_transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserServiceImplPt implements UserServicePt{
	
	private JdbcTemplate jdbcTemplate;
	
	private DataSourceTransactionManager transactionManager;
	
	private ExecutorService service = Executors.newFixedThreadPool(2);
	
	public void setTransactionManager(DataSourceTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	//设置数据源
	public void setDataSource(DataSource dataSource){
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	

	@Override
	public void save(UserPt user) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status = transactionManager.getTransaction(def);
		
		try {

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
//			jdbcTemplate.batchUpdate
			System.out.println();
			List<UserPt> users = getUsers();
			int i=1/0;
			transactionManager.commit(status);
		} catch (Exception e) {
			transactionManager.rollback(status);
		}
//		
	}
	
	@Override
	public void multiThreadSave(UserPt user) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status = transactionManager.getTransaction(def);
		
		try {
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
//				int i=1/0;
			transactionManager.commit(status);
		} catch (DataAccessException e) {
			transactionManager.rollback(status);
		}
}

	@Override
	public List<UserPt> getUsers() {
		String querySql = "select * from user";
		RowMapper<UserPt> mapper = new UserRowMapperPt();
		return jdbcTemplate.query(querySql,mapper);
	}

	@Override
	public List<UserPt> getUsersHasParams() {
		RowMapper<UserPt> mapper = new UserRowMapperPt();
		String querySql2 = "select * from user where name=?";
		Object[] params = new Object[1];
		params[0] = "张三";
		List<UserPt> query = jdbcTemplate.query(querySql2, params, mapper);
		System.out.println("带参数查询: "+query);
		return query;
	}

	@Override
	public UserPt findOne() {
		String querySql2 = "select * from user where id='2'";
		RowMapper<UserPt> mapper = new UserRowMapperPt();
		return jdbcTemplate.queryForObject(querySql2, mapper);
	}

	@Override
	public String findName() {
		String querySql2 = "select name from user where id='2'";
		return jdbcTemplate.queryForObject(querySql2, String.class);
	}
}
