package fs.polyglot.model;

/**
 * This class implements a tree object which represents a group. An additional attribute indicates whether this group is complete, i.e. for
 * each string in this group and its subgroups, there is a variant for each listed language
 * @author Simon Hampe
 *
 */
public class Group extends TreeObject {

	private final NodeType type = NodeType.GROUP;
	public final boolean isComplete;
	
	/**
	 * Constructs a group with specified path.
	 */
	public Group(String path, boolean isComplete) {
		super(path);
		this.isComplete = isComplete;
	}

	@Override
	public NodeType getType() {
		return type;
	}
	
	
	
}
