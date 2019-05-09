package cn.wulin.temp1.domain.jdbc.programming_transaction;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class UserRowMapperPt implements RowMapper<UserPt>{

	@Override
	public UserPt mapRow(ResultSet rs, int rowNum) throws SQLException {
		UserPt user = new UserPt(rs.getInt("id"), rs.getString("name"), rs.getInt("age"), rs.getString("sex"));
		return user;
	}


}
