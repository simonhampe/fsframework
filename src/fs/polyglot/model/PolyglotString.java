package fs.polyglot.model;

/**
 * This class implements a tree object which represents a polyglot string. In
 * addtion to its path, which is the path of its group, it has a string id. It
 * also has an attribute indicating whether it is a complete string, i.e. there
 * is a variant for each listed language.<br>
 * Two objects of this class (or subclass) are considered equal, if they have the same path and stringID 
 * 
 * @author Simon Hampe
 * 
 */
public class PolyglotString extends TreeObject {

	private final NodeType type = NodeType.POLYGLOTSTRING;

	public final String stringID;
	public final boolean isComplete;

	/**
	 * Constructs a polyglot string with specified path and id. If id == null,
	 * it is set to the empty string
	 */
	public PolyglotString(String path, String id, boolean isComplete) {
		super(path);
		stringID = id == null ? "" : id;
		this.isComplete = isComplete;
	}

	@Override
	public NodeType getType() {
		return type;
	}

	public String toString() {
		return path + ": " + stringID;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj.getClass() == this.getClass())) return false;
		else return super.equals(obj) && stringID.equals(((PolyglotString)obj).stringID);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ stringID.hashCode();
	}
	
	

}
