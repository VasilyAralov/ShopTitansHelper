package shop.titans.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import shop.titans.views.CraftReagentItem;

public class CraftItemMapper implements RowMapper<CraftReagentItem> {

	@Override
	public CraftReagentItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		var id = rs.getInt("ii_id");
		var itemId = rs.getInt("ii_item_id");
		var price = (int) rs.getFloat("ii_price");
		return new CraftReagentItem(id, itemId, price);
	}
	
	public static Collection<CraftReagentItem> getAllRecords(JdbcTemplate jdbc) {
		return query(jdbc, "");
	}
	
	public static CraftReagentItem getComponentRecord(JdbcTemplate jdbc, int id) {
		return query(jdbc, " WHERE ic_id = " + id).get(0);
	}
	
	private static List<CraftReagentItem> query(JdbcTemplate jdbc, String where) {
		return jdbc.query("SELECT ii_id, ii_item_id, ((item_price * 10) * (iq_price_multiplier + 1) * ii_item_craft_quantity) as ii_price FROM item_items INNER JOIN items ON item_items.ii_item_craft_id = item_id INNER JOIN item_quality ON iq_id = ii_item_craft_quality" + where, new CraftItemMapper());
	}
}
