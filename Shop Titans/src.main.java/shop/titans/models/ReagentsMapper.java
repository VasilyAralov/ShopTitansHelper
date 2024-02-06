package shop.titans.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.TreeMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import shop.titans.interfaces.EDIFFICULTIES;
import shop.titans.views.Reagent;

@Component
public class ReagentsMapper implements RowMapper<Reagent> {
	
	public static List<Reagent> getAllReagents(JdbcTemplate jdbc) {
		return jdbc.query("SELECT * FROM components ORDER BY component_market_price", new ReagentsMapper());
	}
	
	@Override
	public Reagent mapRow(ResultSet rs, int rowNum) throws SQLException {
		var id = rs.getInt("component_id");
		var name = rs.getString("component_name");
		var bossId = rs.getInt("component_boss_id");
		var questDrop = new TreeMap<EDIFFICULTIES, Integer>();
		var bossDrop = new TreeMap<EDIFFICULTIES, Integer>();
		var questDropPrefix = "component_drop_";
		var bossDropPrefix = "component_boss_drop_";
		for (var difficulty : EDIFFICULTIES.values()) {
			var diffAreaValue = rs.getInt(questDropPrefix + difficulty.name());
			if (diffAreaValue > 0) {
				questDrop.put(difficulty, diffAreaValue);
			}
			var diffBossValue = rs.getInt(bossDropPrefix + difficulty.name());
			if (diffBossValue > 0) {
				bossDrop.put(difficulty, diffBossValue);
			}
		}
		var marketPrice = rs.getInt("component_market_price"); 
		return new Reagent(id, name, bossId, questDrop, bossDrop, marketPrice);
	}

}
