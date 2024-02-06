package shop.titans.views;

import shop.titans.controllers.Game;
import shop.titans.interfaces.ICraftReagent;
import shop.titans.interfaces.ADBObject;

public class CraftReagentItem extends ADBObject implements ICraftReagent {
	
	private int price;
	
	public CraftReagentItem(int id, int itemId, int price) {
		super(id, true);
		Game.getItem(itemId).addCraft(this);
		this.price = price;
	}

	@Override
	protected <T extends ADBObject> void join(T that) {
		if (!(that instanceof CraftReagentItem item)) {
			throw new IllegalArgumentException("Can't join " + that.getClass().getCanonicalName() + " to " + Item.class.getCanonicalName());
		}
		this.price = item.price;
	}

	@Override
	public int getPrice() {
		return price;
	}
	
	@Override
	public String toString() {
		return "Price: " + price;
	}

}
