package shop.titans.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import shop.titans.views.Boss;

@Component
public class BossMapper implements RowMapper<Boss> {
	
	public static Collection<Boss> getAllRecords(JdbcTemplate jdbc) {
		return jdbc.query("SELECT * FROM bosses", new BossMapper());
	}
	
	@Override
	public Boss mapRow(ResultSet rs, int rowNum) throws SQLException {
		var id = rs.getInt("boss_id");
		var name = rs.getString("boss_name");
		var cooldown = rs.getInt("boss_cooldown");
		var respawn = rs.getTimestamp("boss_respawn");
		return new Boss(id, name, cooldown, respawn);
	}

	public static Boss getBoss(JdbcTemplate jdbc, int id) {
		return query(jdbc, " WHERE boss_id = " + id).get(0);
	}
	
	private static List<Boss> query(JdbcTemplate jdbc, String where) {
		return jdbc.query("SELECT * FROM bosses" + where, new BossMapper());
	}

}
