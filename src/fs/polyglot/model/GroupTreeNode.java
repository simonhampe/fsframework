package fs.polyglot.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

import fs.xml.PolyglotStringTable;


/**
 * This class implements a tree structure for polyglot group IDs. Group IDs are ordered 
 * hierarchically in a way similar to java package naming: Each "." in the ID signifies a 
 * step downward in the hierarchy. For example "fs.example.polyglot.mainframe" is the group "mainframe",
 * which is a subgroup of "polyglot", etc...<br>
 * Subgroups are added as strings, which are automatically parsed, so that for each "." a subnode 
 * is created. <br>
 * Each tree object contains a reference to its parent and its children (possibly null). To preserve
 * the group hierarchy structure and uniqueness of group id's, node names cannot be edited and 
 * children can only be added via the addBranch method.
 * @author Simon Hampe
 *
 */
public class GroupTreeNode {

	// TREE DATA STRUCTURE *********************************
	
	/**
	 * This object's name
	 */
	private String name = null;
	
	/**
	 * This nodes direct parent. If null, this node is considered a 'root' node
	 */
	private GroupTreeNode parent = null;
	
	/**
	 * A list of children
	 */
	private HashSet<GroupTreeNode> children = new HashSet<GroupTreeNode>();
	
	
	// A COMPARATOR FOR NODES *******************************
	// ******************************************************
	
	/**
	 * A comparator that compares to nodes according to the alphabetical order of their names
	 */
	public final static Comparator<GroupTreeNode> defaultComparator = new Comparator<GroupTreeNode>() {

		@Override
		public int compare(GroupTreeNode o1, GroupTreeNode o2) {
			return o1.getName().compareTo(o2.getName());
		}
		
	};
	
	
	// CONSTRUCTORS ******************************************
	// *******************************************************
	
	/**
	 * Creates a group tree node of the given name and parent without children. This
	 * constructor is protected to guarantee that a node cannot be 'attached' to a parent without
	 * being added as its child.
	 */
	protected GroupTreeNode(String name, GroupTreeNode parent) {
		this.name = name;
		this.parent = parent;
	}
	
	/**
	 * Creates a group tree with root node the null string, whose structure reflects the
	 * group hierarchy of the given table. This object will then represent the root node of
	 * the tree (named as rootName).
	 */
	public GroupTreeNode(PolyglotStringTable table, String rootName) {
		name = rootName;
		if(table == null) return;
		HashSet<String> grouplist = table.getGroupList();
		for(String g : grouplist) addBranch(g);
		
	}
	
	// GETTERS AND SETTERS ***********************************
	// *******************************************************
	
	/**
	 * Returns the name of this node (possibly null)
	 */
	public String getName()  {
		return name;
	}

	/**
	 * @return The parent of this node (possibly null)
	 */
	public GroupTreeNode getParent() {
		return parent;
	}
	
	/**
	 * This will call getParent() recursively, until a null value is obtained. The last node != null
	 * will be returned (if this node is a root node it will return itself)
	 */
	public GroupTreeNode getRoot() {
		GroupTreeNode root = this;
		while(root.getParent() != null) root = root.getParent();
		return root;
	}
	
	/**
	 * Returns the set of children of this node
	 */
	public HashSet<GroupTreeNode> getChildren() {
		return new HashSet<GroupTreeNode>(children);
	}
	
	/**
	 * Returns a list of all children of this node, using c to sort them.
	 * If c == null, the alphabetical order of their names is used.
	 */
	public ArrayList<GroupTreeNode> getChildrenSorted(Comparator<GroupTreeNode> c) {
		if(c == null) c = defaultComparator;
		TreeSet<GroupTreeNode> ret = new TreeSet<GroupTreeNode>(c);
		ret.addAll(children);
		return new ArrayList<GroupTreeNode>(ret);
	}
	
	/**
	 * Returns the branch associated to the given group tree path (or null, if 
	 * it doesn't exist).  The null string refers to this node
	 */
	public GroupTreeNode getBranch(String branch) {
		if(branch == null) return this;
		//Separate first subgroup
		String[] separate = branch.split("[.]",2);
		String subgroup = separate[0];
		String restgroup = separate.length > 1 ? separate[1] : null;
		for(GroupTreeNode c : children) {
			if(c.getName().equals(subgroup)) {
				return c.getBranch(restgroup);
			}
		}
		//If there is no node of this name, return null
		return null;
	}
	
	
	/**
	 * Adds a subgroup branch to this tree node. The first part of the string up to the 
	 * first occurence of "." will be treated as the name of a direct subnode of this node, which
	 * will be created if it doesn't already exist and whose addBranch method will then be called recursively with the truncated branch.
	 *  A null branch is ignored. All newly created nodes will be furnished with a 
	 */
	public void addBranch(String branch) {
		if(branch == null) return;
		//Separate first subgroup
		String[] separate = branch.split("[.]",2);
		String subgroup = separate[0];
		String restgroup = separate.length > 1 ? separate[1] : null;
		for(GroupTreeNode t : children) {
			//If a node of that name already exists, call addBranch recursively 
			if(t.getName().equals(subgroup)) {
				t.addBranch(restgroup);
				return;
			}
		}
		//If it doesn't exist, create it.
		GroupTreeNode child = new GroupTreeNode(subgroup, this);
		children.add(child);
		child.addBranch(restgroup);
	}
	
	/**
	 * Adds a child to this branch. This is protected to preserve the group structure
	 */
	protected void addChild(GroupTreeNode child) {
		children.add(child);
	}
	
	// GENERAL OVERWRITTEN METHODS ****************************
	// ********************************************************
	
	/**
	 * @return The name of this node
	 */
	public String toString() {
		return name;
	}

}
