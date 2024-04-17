package shop.titans.views;

import java.util.Collections;
import java.util.Map;

import shop.titans.controllers.Game;
import shop.titans.interfaces.ICraftReagent;
import shop.titans.interfaces.ADBObject;
import shop.titans.interfaces.EDIFFICULTIES;

public class Reagent extends ADBObject implements ICraftReagent {

	public String getName() {
		return name;
	}

	private String name;
	private Boss boss;
	private Map<EDIFFICULTIES, Integer> bossDrop;
	private Map<EDIFFICULTIES, Integer> areaDrop;
	private int price;
	private boolean isFinalPrice;

	public Reagent(int id, String name, int bossId, Map<EDIFFICULTIES, Integer> questDrop, Map<EDIFFICULTIES, Integer> bossDrop, int marketPrice) {
		super(id, true);
		this.name = name;
		this.boss = Game.getBoss(bossId);
		this.bossDrop = Collections.unmodifiableMap(bossDrop);
		this.areaDrop = Collections.unmodifiableMap(questDrop);
		price = marketPrice * 10;
		isFinalPrice = false;
	}
	
	public Reagent(Integer id) {
		super(id, false);
	}
	
	public void setCalculated() {
		this.isFinalPrice = true;
	}
	
	public boolean isPriceCalculated() {
		return isFinalPrice;
	}
	
	public void updatePrice(int newPrice) {
		if (this.isFinalPrice) {
			throw new IllegalArgumentException("Can't update component price because final price is already calculated");
		}
		if (this.price < newPrice) {
			price = newPrice;
		}
	}

	@Override
	public String toString() {
		return "Components " + getName() + " drop " + getBossDrop(EDIFFICULTIES.extreme) + " from the " + getBoss().getName() + " boss and "
				+ getAreaDrop(EDIFFICULTIES.extreme) + " from the quest";
	}

	public Boss getBoss() {
		return boss;
	}

	protected int getAreaDrop(EDIFFICULTIES difficulty) {
		return areaDrop.get(difficulty);
	}

	public int getBossDrop(EDIFFICULTIES difficulty) {
		return bossDrop.get(difficulty);
	}

	@Override
	public int getPrice() {
		return price;
	}

	@Override
	protected <T extends ADBObject> void join(T that) {
		if (!(that instanceof Reagent reagent)) {
			throw new IllegalArgumentException("Can't join " + that.getClass().getCanonicalName() + " to " + Item.class.getCanonicalName());
		}
		this.name = reagent.name;
		this.areaDrop = reagent.areaDrop;
		this.bossDrop = reagent.bossDrop;
		this.boss = reagent.boss;
		this.price = reagent.price;
	}

	public int getMaxBossDrop() {
		if (bossDrop.isEmpty()) {
			return 0;
		}
		return bossDrop.values().stream().max((o1, o2) -> Integer.compare(o1, o2)).get();
	}

}
