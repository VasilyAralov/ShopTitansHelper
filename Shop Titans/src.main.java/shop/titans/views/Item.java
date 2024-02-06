package shop.titans.views;

import java.util.TreeSet;

import shop.titans.controllers.Game;
import shop.titans.interfaces.ICraftReagent;
import shop.titans.interfaces.ADBObject;

public class Item extends ADBObject implements Comparable<Item> {
	
	private String name;
	
	public String getName() {
		return name;
	}

	public int getPrice() {
		return price;
	}

	public int getMulticraft() {
		return Math.round((multicraft + itemType.getMulticraft()) * 100) + Game.getMulticraftChance();
	}

	public Type getItemType() {
		return itemType;
	}

	private int price;
	private float multicraft;
	private Type itemType;
	private TreeSet<ICraftReagent> reagents = new TreeSet<>();
	
	public Item(Integer id) {
		super(id, false);
	}
	
	public Item(int id, String name, int price, float multicraft, int typeId) {
		super(id, true);
		this.name = name;
		this.price = price;
		this.multicraft = multicraft;
		this.itemType = Game.getItemType(typeId);
	}

	@Override
	protected <T extends ADBObject> void join(T that) {
		if (!(that instanceof Item item)) {
			throw new IllegalArgumentException("Can't join " + that.getClass().getCanonicalName() + " to " + Item.class.getCanonicalName());
		}
		this.name = item.name;
		this.price = item.price;
		this.multicraft = item.multicraft;
		this.itemType = item.itemType;
		this.reagents.addAll(item.reagents);
	}
	
	@Override
	public String toString() {
		return itemType.getName() + " " + getName() + ". Price " + getPrice() + ". Multicraft chance " + getMulticraft() + "%. Components: " + reagents.toString();
	}

	public void addCraft(ICraftReagent reagent) {
		reagents.add(reagent);
	}
	
	public TreeSet<ICraftReagent> getReagents() {
		return reagents;
	}
	
	public int getCost() {
		return getReagents().stream().mapToInt(ICraftReagent::getPrice).sum();
	}
	
	public int getProfit() {
		return getPrice() - getCost();
	}

	@Override
	public int compareTo(Item o) {
		var profitDiff = o.getProfit() - this.getProfit();
		if (Math.abs(profitDiff) > 2) {
			return profitDiff;
		}
		var itemReagentsDiff = this.getItemReagentsCount() - o.getItemReagentsCount(); 
		if (itemReagentsDiff != 0) {
			return itemReagentsDiff;
		}
		var reagentsCountDiff = o.getReagentsCount() - this.getReagentsCount();
		if (reagentsCountDiff != 0) {
			return reagentsCountDiff;
		}
		return o.getId() - this.getId();
	}
	
	private int getReagentsCount() {
		return this.reagents.stream().map(e -> {
			if (e instanceof CraftReagentComponent reagent) {
				return reagent;
			}
			return null;
		}).filter(e -> {
			return e != null;
		}).mapToInt(CraftReagentComponent::getCount).sum();
	}
	
	private int getItemReagentsCount() {
		return (int) reagents.stream().filter(e -> {
			return e instanceof CraftReagentItem;
		}).count();
	}

}