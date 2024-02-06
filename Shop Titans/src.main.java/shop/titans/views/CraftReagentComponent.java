package shop.titans.views;

import shop.titans.controllers.Game;
import shop.titans.interfaces.ICraftReagent;
import shop.titans.interfaces.ADBObject;

public class CraftReagentComponent extends ADBObject implements ICraftReagent {
	
	private Reagent reagent;
	
	public Reagent getReagent() {
		return reagent;
	}

	public int getCount() {
		return count;
	}

	private int count;

	public CraftReagentComponent(int id, int itemId, int reagentId, int count) {
		super(id, true);
		this.reagent = Game.getReagent(reagentId);
		this.count = count;
		Game.getItem(itemId).addCraft(this);
	}

	@Override
	protected <T extends ADBObject> void join(T that) {
		if (!(that instanceof CraftReagentComponent reagent)) {
			throw new IllegalArgumentException("Can't join " + that.getClass().getCanonicalName() + " to " + Item.class.getCanonicalName());
		}
		this.count = reagent.count;
		this.reagent = reagent.reagent;
	}

	@Override
	public int getPrice() {
		return reagent.getPrice() * count;
	}
	
	@Override
	public String toString() {
		return count + "x" + reagent.getName();
	}

}
