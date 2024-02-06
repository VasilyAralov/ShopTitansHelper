package shop.titans.interfaces;

public abstract class ADBObject {
	
	private final int id;
	private boolean isInitialized;
	
	protected ADBObject(int id, boolean isInitialized) {
		this.id = id;
		this.isInitialized = isInitialized;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ADBObject that) {
			return this.getId() == that.getId();
		}
		return false;
	}
	
	public boolean isInitialized() {
		return isInitialized;
	}

	@SuppressWarnings("unchecked")
	public <T extends ADBObject> T merge(T v) {
		if (!this.getClass().equals(v.getClass())) {
			throw new UnsupportedOperationException("Can't merge " + this.getClass() + " and " + v.getClass());
		}
		if (!isInitialized()) {
			join(v);
		}
		return (T) this;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	protected abstract <T extends ADBObject> void join(T that);
	
}
