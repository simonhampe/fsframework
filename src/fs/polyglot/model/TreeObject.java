package fs.polyglot.model;

/**
 * This class represents a general object of a polyglot table group tree. It has
 * one single property: Its group path.
 * Two objects of this class are considered equal (also by hashCode), if they have equal paths.
 * @author Simon Hampe
 * 
 */
public class TreeObject {

	public final String path;

	private final NodeType type = NodeType.NONE;

	public TreeObject(String path) {
		this.path = path;
	}

	public NodeType getType() {
		return type;
	}

	public String toString() {
		return path;
	}

	/**
	 * The different types of TreeNodes
	 * 
	 * @author Simon Hampe
	 * 
	 */
	public enum NodeType {
		NONE, GROUP, POLYGLOTSTRING, VARIANT
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj.getClass() == this.getClass())) return false;
		else return this.path == null? ((TreeObject)obj).path == null : this.path.equals(((TreeObject)obj).path);
	}

	@Override
	public int hashCode() {
		return path == null? 0 : path.hashCode();
	};

	
	
}
