package shop.titans.views;

import shop.titans.interfaces.ADBObject;

public class Type extends ADBObject {

	public String getName() {
		return name;
	}

	private String name;
	private float multicraft;

	public Type(int id, String name, float multicraft) {
		super(id, true);
		this.name = name;
		this.multicraft = multicraft;
		if (multicraft < 0) {
			throw new IllegalArgumentException("Multicraft chance can't be negative");
		}
	}

	public Type(Integer id) {
		super(id, false);
	}

	protected float getMulticraft() {
		return multicraft;
	}
	
	@Override
	public String toString() {
		String text = "Type " + name + " ";
		if (multicraft == 0) {
			return text + "doesn't have any multicraft bonuses";
		}
		return text + "has additional +" + Math.round(multicraft * 100) + "% multicraft bonus";
	}

	@Override
	protected <T extends ADBObject> void join(T that) {
		if (!(that instanceof Type type)) {
			throw new IllegalArgumentException("Can't join " + that.getClass().getCanonicalName() + " to " + Item.class.getCanonicalName());
		}
		this.name = type.name;
		this.multicraft = type.multicraft;
	}
}
