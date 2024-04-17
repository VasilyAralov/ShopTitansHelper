package shop.titans.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import shop.titans.views.CraftReagentComponent;

public class CraftComponentMapper implements RowMapper<CraftReagentComponent> {

	@Override
	public CraftReagentComponent mapRow(ResultSet rs, int rowNum) throws SQLException {
		var id = rs.getInt("ic_id");
		var itemId = rs.getInt("ic_item_id");
		var componentId = rs.getInt("ic_component_id");
		var componentCount = rs.getInt("ic_component_count");
		return new CraftReagentComponent(id, itemId, componentId, componentCount);
	}
	
	public static Collection<CraftReagentComponent> getAllRecords(JdbcTemplate jdbc) {
		return jdbc.query("SELECT * FROM item_components", new CraftComponentMapper());
	}
	
}
