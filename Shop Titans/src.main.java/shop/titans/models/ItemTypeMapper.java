package shop.titans.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import shop.titans.views.Type;

public class ItemTypeMapper implements RowMapper<Type> {

	@Override
	public Type mapRow(ResultSet rs, int rowNum) throws SQLException {
		var id = rs.getInt("type_id");
		var name = rs.getString("type_name");
		var multicraftChance = rs.getFloat("type_multicraft_chance");
		return new Type(id, name, multicraftChance);
	}

	public static Collection<Type> getAllTypes(JdbcTemplate jdbc) {
		return jdbc.query("SELECT * FROM types", new ItemTypeMapper());
	}

}
