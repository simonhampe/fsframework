package fs.polyglot.model;


/**
 * This class represents a general object of a polyglot table group tree. It has one single property:
 * Its group path.
 * @author Simon Hampe
 *
 */
public class TreeObject {

	public final String path;
	
	private final NodeType type = NodeType.NONE; 
	
	public TreeObject(String path) {
		this.path = path;
	}
	
	public NodeType getType() { return type;}
	
	public String toString() {
		return path;
	}
	
	/**
	 * The different types of TreeNodes
	 * @author Simon Hampe
	 *
	 */
	public enum NodeType  {NONE, GROUP, POLYGLOTSTRING, VARIANT};
	
}
