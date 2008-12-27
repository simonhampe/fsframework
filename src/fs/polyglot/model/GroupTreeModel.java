package fs.polyglot.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import fs.polyglot.event.PolyglotTableModelListener;
import fs.xml.PolyglotStringTable;

/**
 * This represents a GroupTree, which represents the group structure of a PolyglotTableModel. It serves as a model 
 * for JTree and can be associated to a PolyglotTableModel, the group structure of which it will
 * represent and to which it listens for changes. This model notifies potential TreeModelListeners only
 * of structureChanged events from root down, since it would be far too complicated and slow to deduce
 * specific changes from the PolyglotTableModelListener events. On constructing this model, one can
 * specify, if strings and variants should also be included.
 * @author Simon Hampe
 *
 */
public class GroupTreeModel implements TreeModel, PolyglotTableModelListener {

	/**
	 * The associated table
	 */
	private PolyglotTableModel table = null;
	/**
	 * Are polyglotstrings included?
	 */
	private boolean includeStrings = false;
	/**
	 * Are variants of polyglotstrings included?
	 */
	private boolean includeVariants = false;
	/**
	 * A set of TreeModelListeners
	 */
	private HashSet<TreeModelListener> listeners = new HashSet<TreeModelListener>();
	
	
	/**
	 * Compares two TreeObjects primarily by alphabetical order of their paths. Afterwards they are
	 * potentially compared by string id and language id. The tree object with path/stringid/languageid null comes before 
	 * all other tree objects
	 */
	public final static Comparator<TreeObject> treeObjectComparator = new Comparator<TreeObject>() {

		@Override
		public int compare(TreeObject o1, TreeObject o2) {
			if(o1.path == null && o2.path == null) return 0;
			if(o1.path == null) return -1;
			if(o2.path == null) return 1;
			int r1 = o1.path.compareTo(o2.path);
			if(r1 != 0) return r1;
			//If both have a stringID attribute, compare that
			if(o1 instanceof PolyglotString && o2 instanceof PolyglotString) {
				if(((PolyglotString)o1).stringID == null && ((PolyglotString)o2).path == null) return 0;
				if(((PolyglotString)o1).stringID == null) return -1;
				if(((PolyglotString)o2).stringID == null) return 1;
				int r2 = ((PolyglotString)o1).stringID.compareTo(((PolyglotString)o2).stringID);
				if(r2 != 0) return r2;
				//If both have a language attribute, compare that
				if(o1.getType() == TreeObject.NodeType.VARIANT && o2.getType() == TreeObject.NodeType.VARIANT) {
					//If one variant hasn't got a language attribute, there are not comparable
					if(((Variant)o1).language == null || ((Variant)o2).language == null) return r2;
					if(((Variant)o1).language.id == null && ((Variant)o2).language.id == null) return 0;
					if(((Variant)o1).language.id == null) return -1;
					if(((Variant)o2).language.id == null) return 1;
					return ((Variant)o1).language.id.compareTo(((Variant)o2).language.id);
				}
				else return r2;
			}
			else return r1;
		}
		
	};
	
	
	// CONSTRUCTOR *******************************************
	// *******************************************************
	
	/**
	 * Constructs a tree model which gets its data from the specified table. 
	 * If table == null, this model will represent the empty tree.
	 * The table can be configured to show either only group nodes, or also
	 * polyglotstring nodes and even variants.
	 */
	public GroupTreeModel(PolyglotTableModel table, boolean includeStrings, boolean includeVariants) {
		this.table = table;
		this.includeStrings = includeStrings;
		this.includeVariants = includeVariants;
	}
	
	// HELPER METHODS ***************************************
	// ******************************************************
	
	/**
	 * If this Object is a tree object and has a valid path for this tree, this 
	 * returns a list of all visible children. If not, this returns
	 * the empty list 
	 */
	public ArrayList<TreeObject> getChildren(Object ob) {
		if(!isOfThisTree(ob)) return new ArrayList<TreeObject>();
		TreeObject obj = (TreeObject) ob;
		//Now the list of children depends on the type of this object
		//No children for variants
		if(obj.getType() == TreeObject.NodeType.VARIANT) return new ArrayList<TreeObject>();
				
		TreeSet<TreeObject> children = new TreeSet<TreeObject>(treeObjectComparator);
		//If it's a group, add subgroups and possibly strings
		if(obj.getType() == TreeObject.NodeType.GROUP) {
			for(String gid : table.getGroupList()) {
				if(PolyglotStringTable.isSubgroupOf(obj.path, gid, true)) {
					String cid = PolyglotStringTable.extractGroup(obj.path, gid);
					//This creates no doublets, since the treeObjectComparator recognizes identical paths:
					children.add(new Group(cid,table.isCompleteGroup(cid)));
				}
			}
			if(includeStrings) {
				for(String sid : table.getStringsInGroup(obj.path)) {
					children.add(new PolyglotString(obj.path,sid,table.isCompleteString(sid)));
				}
			}
		}
		//If it's a string, add variants
		if(obj.getType() == TreeObject.NodeType.POLYGLOTSTRING && includeVariants) {
			for(String l : table.getSupportedLanguages(((PolyglotString)obj).stringID)) {
				children.add(new Variant(obj.path,((PolyglotString)obj).stringID,
						new Language(l,table.getLanguageDescription(l),!table.getLanguageList().contains(l),table.getSupport(l)),
						table.getUnformattedString(((PolyglotString)obj).stringID, l)));
			}
		}
		
		
		return new ArrayList<TreeObject>(children);
	}
	
	
	
	/**
	 * Returns true if there is a group in the associated table which has
	 * this path or is a subgroup of this path (i.e. path must be a substring of
	 * a group id). As null is considered the root node, it is always a valid path
	 */
	public boolean isValidTreePath(String path) {
		if(table == null) return false;
		if(path == null) return true;
		for(String gid : table.getGroupList()) {
			if(gid.startsWith(path)) return true;
		}
		return false;
	}
	
	/**
	 * Returns true, if this object is an object of this tree model, i.e. it
	 * is of type TreeObject and has a valid tree path.
	 */
	public boolean isOfThisTree(Object o) {
		return (o != null && o instanceof TreeObject && isValidTreePath(((TreeObject)o).path));
	}
	
	
	// TREE MODEL INTERFACE **********************************
	// *******************************************************
	
	@Override
	public void addTreeModelListener(TreeModelListener l) {
		if(l != null) listeners.add(l);
	}

	/**
	 * Returns the index-th child of parent or null, 
	 * if parent is not an object of this tree or the index
	 * is out of bounds
	 */
	@Override
	public Object getChild(Object parent, int index) {
		try {
			return getChildren(parent).get(index);
		}
		catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Returns the number of children (-1 if parent is not an object of this tree)
	 */
	@Override
	public int getChildCount(Object parent) {
		return isOfThisTree(parent)? getChildren(parent).size() : -1;
	}

	/**
	 * Returns the index of child (or -1 if parent or child are not nodes of this tree)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return getChildren(parent).indexOf(child);
	}

	/**
	 * Returns a Group object with null path
	 */
	@Override
	public Object getRoot() {
		return new Group(null,table.isCompleteGroup(null));
	}

	/**
	 * Returns true, if node is an object of this tree with empty children list
	 */
	@Override
	public boolean isLeaf(Object node) {
		return isOfThisTree(node)? getChildren(node).size() == 0 : false;
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	
	/**
	 * Actually, a node value shouldn't be changed but via the associated table model, but in any case
	 * this only notifies all potential TreeModelListeners of a structure change
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		fireTreeStructureChanged();
	}

	/**
	 * Notifies all TreeModelListeners of a structure change from the root node down
	 */
	protected void fireTreeStructureChanged() {
		Object[] o = {getRoot()};
		for(TreeModelListener l : listeners) l.treeStructureChanged(new TreeModelEvent(this,o));
	}
	
	// TABLEMODELLISTENER INTERFACE****************************
	// ********************************************************
	
	/**
	 * Notifies, if variants are included
	 */
	@Override
	public void languageListChanged(PolyglotTableModel source) {
		if(includeVariants) fireTreeStructureChanged();
	}

	/**
	 * Notifies always
	 */
	@Override
	public void stringTableChanged(PolyglotTableModel source) {
		fireTreeStructureChanged();
	}

	/**
	 * Ignored
	 */
	@Override
	public void tableDescriptionChanged(PolyglotTableModel source) {
		//Ignored
	}

	/**
	 * Ignored
	 */
	@Override
	public void tableIDChanged(PolyglotTableModel source) {
		//Ignored
	}

}
