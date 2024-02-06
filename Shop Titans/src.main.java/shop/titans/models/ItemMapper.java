package shop.titans.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import shop.titans.views.Item;

@Component
public class ItemMapper implements RowMapper<Item> {

	public static Collection<Item> getAllItems(JdbcTemplate jdbc) {
		return query(jdbc, "");
	}
	
	@Override
	public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
		var id = rs.getInt("item_id");
		var name = rs.getString("item_name");
		var price = rs.getInt("item_price");
		var multicraft = rs.getFloat("item_multicraft_bonus");
		var item_type = rs.getInt("item_type");
		return new Item(id, name, price, multicraft, item_type);
	}
	
	public static Item getItem(JdbcTemplate jdbc, int id) {
		return query(jdbc, " WHERE item_id = " + id).get(0);
	}
	
	private static List<Item> query(JdbcTemplate jdbc, String where) {
		return jdbc.query("SELECT * FROM items" + where, new ItemMapper());
	}
	
}
