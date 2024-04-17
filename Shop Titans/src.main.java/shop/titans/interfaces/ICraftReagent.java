package shop.titans.interfaces;

public interface ICraftReagent extends Comparable<ICraftReagent> {
	
	public int getPrice();
	public int getId();
	
	@Override
	default int compareTo(ICraftReagent o) {
		int diff = getPrice() - o.getPrice();
		if (diff == 0) {
			return this.getId() - o.getId();
		}
		return diff;
	}
	
}
