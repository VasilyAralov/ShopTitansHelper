package shop.titans.interfaces;

public interface ICraftReagent extends Comparable<ICraftReagent> {
	
	public int getPrice();
	
	@Override
	default int compareTo(ICraftReagent o) {
		return getPrice() - o.getPrice();
	}
	
}
