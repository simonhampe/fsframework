package fs.polyglot.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import fs.polyglot.event.PolyglotTableModelListener;
import fs.xml.PolyglotStringTable;

/**
 * This represents a GroupTree, which represents the group structure of a
 * PolyglotTableModel. It serves as a model for JTree and can be associated to a
 * PolyglotTableModel, the group structure of which it will represent and to
 * which it listens for changes. This model notifies potential
 * TreeModelListeners only of structureChanged events from root down, since it
 * would be far too complicated and slow to deduce specific changes from the
 * PolyglotTableModelListener events. On constructing this model, one can
 * specify, if strings and variants should also be included.
 * 
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
	 * Should only incomplete groups be displayed?
	 */
	private boolean showOnlyIncomplete = false;
	/**
	 * A set of TreeModelListeners
	 */
	private HashSet<TreeModelListener> listeners = new HashSet<TreeModelListener>();

	//The actual data
	private Group root = null; //The root node
	private HashMap<TreeObject, ArrayList<TreeObject>> children = new HashMap<TreeObject, ArrayList<TreeObject>>(); //The tree data
	
	/**
	 * Compares two TreeObjects primarily by alphabetical order of their paths.
	 * Afterwards they are potentially compared by string id and language id.
	 * The tree object with path/stringid/languageid null comes before all other
	 * tree objects
	 */
	public final static Comparator<TreeObject> treeObjectComparator = new Comparator<TreeObject>() {

		@Override
		public int compare(TreeObject o1, TreeObject o2) {
			if (o1.path == null)
				return -1;
			if (o2.path == null)
				return 1;
			if(o1 != null && o2 != null) {
				int r1 = o1.path.compareTo(o2.path);
				if (r1 != 0)
					return r1;
			}
			// If both have a stringID attribute, compare that
			if (o1 instanceof PolyglotString && o2 instanceof PolyglotString) {
				if (((PolyglotString) o1).stringID == null
						&& ((PolyglotString) o2).path == null)
					return 0;
				if (((PolyglotString) o1).stringID == null)
					return -1;
				if (((PolyglotString) o2).stringID == null)
					return 1;
				int r2 = ((PolyglotString) o1).stringID
						.compareTo(((PolyglotString) o2).stringID);
				if (r2 != 0)
					return r2;
				// If both have a language attribute, compare that
				if (o1.getType() == TreeObject.NodeType.VARIANT
						&& o2.getType() == TreeObject.NodeType.VARIANT) {
					// If one variant hasn't got a language attribute, there are
					// not comparable
					if (((Variant) o1).language == null
							|| ((Variant) o2).language == null)
						return r2;
					if (((Variant) o1).language.id == null
							&& ((Variant) o2).language.id == null)
						return 0;
					if (((Variant) o1).language.id == null)
						return -1;
					if (((Variant) o2).language.id == null)
						return 1;
					return ((Variant) o1).language.id
							.compareTo(((Variant) o2).language.id);
				} else
					return r2;
			} 
			else return 0;
		}

	};

	// CONSTRUCTOR *******************************************
	// *******************************************************

	/**
	 * Constructs a tree model which gets its data from the specified table. If
	 * table == null, this model will represent the empty tree. The table can be
	 * configured to show either only group nodes, or also polyglotstring nodes
	 * and even variants.
	 */
	public GroupTreeModel(PolyglotTableModel table, boolean includeStrings,
			boolean includeVariants, boolean showOnlyIncomplete) {
		this.table = table;
		this.includeStrings = includeStrings;
		this.includeVariants = includeVariants;
		this.showOnlyIncomplete = showOnlyIncomplete;
		if(table != null) {
			table.addListener(this);
			syncData();
		}
	}
	
	// SYNC METHOS ******************************************
	// ******************************************************
	
	/**
	 * This causes all data to be reloaded from the associated table. The difference to the status before is computed and 
	 * appropriate listener calls are made
	 */
	public void syncData() {
		HashMap<TreeObject,ArrayList<TreeObject>> newchildren = new HashMap<TreeObject, ArrayList<TreeObject>>();
		//Generate root
		root = new Group(null,table.isCompleteGroup(null));
		//Generate tree
		addChildrenRecursively(root, newchildren);
		
		//If this is the first time that this method is called, just call for a structure change
		if(children.size() == 0) {
			children = newchildren;
			fireTreeStructureChanged(new TreeModelEvent(this,new TreePath(root)));
		}
		
		//Create parent map 
		HashMap<TreeObject,TreeObject> newparents = new HashMap<TreeObject, TreeObject>();
		for(TreeObject p : newchildren.keySet()) for(TreeObject c : newchildren.get(p)) newparents.put(c,p);
		
		
		//Compute difference and notify listeners
		
		HashSet<TreeModelEvent> inserts = new HashSet<TreeModelEvent>();
		HashSet<TreeModelEvent> removals = new HashSet<TreeModelEvent>();
		HashSet<TreeModelEvent> changes = new HashSet<TreeModelEvent>();
		
		//We compute for each element in newchildren which of its children has been removed, added or changed
		//This works, since every 'old' child is always a child (or descendant) of a still existing node (since root always exists).
		for(TreeObject p : newchildren.keySet()) {
			//We only have to compute this, if the node p still exists, since otherwise a corresponding removal event has already
			//been generated
			if(children.keySet().contains(p)) {
				ArrayList<TreeObject> removed = new ArrayList<TreeObject>(); ArrayList<Integer> remIndex = new ArrayList<Integer>();
				ArrayList<TreeObject> inserted = new ArrayList<TreeObject>(); ArrayList<Integer> insIndex = new ArrayList<Integer>();
				ArrayList<TreeObject> changed = new ArrayList<TreeObject>(); ArrayList<Integer> chaIndex = new ArrayList<Integer>();
				//Calculate removed
				for(TreeObject oc : children.get(p)) {
					if(!newchildren.containsKey(oc)) {
						removed.add(oc); remIndex.add(children.get(p).indexOf(oc));
					}
				}
				//Calculate added and changed
				for(TreeObject nc : newchildren.get(p)) {
					//Inserted
					if(!children.containsKey(nc)) {
						inserted.add(nc); insIndex.add(newchildren.get(p).indexOf(nc));
					}
					//Changed
					else {
						TreeObject older = children.get(p).get(children.get(p).indexOf(nc));
						switch(nc.getType()) {
						case VARIANT: 
							if(!((Variant)nc).value.equals(((Variant)older).value)) {
									changed.add(older); chaIndex.add(newchildren.get(p).indexOf(nc));
									break;
							} 
							//We don't break, if the value is equal since we still have to check isComplete
						case POLYGLOTSTRING: 
							if(((PolyglotString)nc).isComplete != ((PolyglotString)older).isComplete) { 
								changed.add(older);chaIndex.add(newchildren.get(p).indexOf(nc));
							}
							break;
						case GROUP: 
							if(((Group)nc).isComplete != ((Group)older).isComplete) {
								changed.add(older); chaIndex.add(newchildren.get(p).indexOf(nc));
							}
							break;
						default: //Will be considered changed 
							changed.add(older);chaIndex.add(newchildren.get(p).indexOf(nc));
						}
					}
				}
				//Add events
				int[] iIndex = new int[insIndex.size()];
				Object[] iObject = new Object[inserted.size()];
				for(int i = 0; i < iIndex.length; i++) {
					iIndex[i] = insIndex.get(i); iObject[i] = inserted.get(i);
				}
				int[] rIndex = new int[remIndex.size()];
				Object[] rObject = new Object[removed.size()];
				for(int i = 0; i < rIndex.length; i++) {
					rIndex[i] = remIndex.get(i); rObject[i] = removed.get(i);
				}
				int[] cIndex = new int[chaIndex.size()];
				Object[] cObject = new Object[changed.size()];
				for(int i = 0; i < cIndex.length; i++) {
					cIndex[i] = chaIndex.get(i); cObject[i] = changed.get(i);
				}
				TreePath newpath = getNodePath(p, newparents);
				if(inserted.size() > 0) inserts.add(new TreeModelEvent(this,getNodePath(p, newparents),iIndex, iObject));
				if(removed.size() > 0) removals.add(new TreeModelEvent(this,newpath,rIndex,rObject));
				if(changed.size() > 0) changes.add(new TreeModelEvent(this,newpath,cIndex,cObject));
			}
		}
		
		//Copy data
		children = newchildren;
		
		//Now fire events
		for(TreeModelEvent e : removals) {
			fireTreeNodesRemoved(e);
		}
		for(TreeModelEvent e : inserts) {
			fireTreeNodesInserted(e);
		}
		for(TreeModelEvent e : changes) {
			fireTreeNodesChanged(e);
		}
	}
	
	/**
	 * Adds all children of o to the associated list newchildren.get(o) and calls itself for all children of 
	 * o.
	 */
	protected void addChildrenRecursively(TreeObject o, HashMap<TreeObject, ArrayList<TreeObject>> newchildren) {
		ArrayList<TreeObject> clist = getChildren(o);
		newchildren.put(o, clist);
		for(TreeObject c : clist) addChildrenRecursively(c, newchildren);
	}
	
	

	// HELPER METHODS ***************************************
	// ******************************************************

	/**
	 * Returns, whether polyglot strings are included
	 */
	public boolean doesIncludeStrings() {
		return includeStrings;
	}


	/**
	 * Sets whether polyglot strings should be included and potentially resyncs
	 */
	public void setIncludeStrings(boolean includeStrings) {
		if(includeStrings == this.includeStrings) return;
		this.includeStrings = includeStrings;
		syncData();
	}


	/**
	 * Returns, whether variants are included (regardless of whether strings are included, this only the abstract value set by
	 * the user)
	 */
	public boolean doesIncludeVariants() {
		return includeVariants;
	}

	/**
	 * Sets whether variants should be included and potentially resyncs
	 */
	public void setIncludeVariants(boolean includeVariants) {
		if(includeVariants == this.includeVariants) return;
		this.includeVariants = includeVariants;
		syncData();
	}

	/**
	 * Returns, whether only incomplete groups are included
	 */
	public boolean doesShowOnlyIncomplete() {
		return showOnlyIncomplete;
	}

	/**
	 * Sets whether only incomplete groups are included and notifies all listeners
	 */
	public void setShowOnlyIncomplete(boolean showOnlyIncomplete) {
		if(this.showOnlyIncomplete == showOnlyIncomplete) return;
		this.showOnlyIncomplete = showOnlyIncomplete;
		syncData();
	}



	/**
	 * If this Object is a tree object and has a valid path for this tree, this
	 * returns a list of all visible children. If not, this returns the empty
	 * list
	 */
	protected ArrayList<TreeObject> getChildren(TreeObject obj) {
		// Now the list of children depends on the type of this object
		// No children for variants
		if (obj.getType() == TreeObject.NodeType.VARIANT)
			return new ArrayList<TreeObject>();

		TreeSet<TreeObject> children = new TreeSet<TreeObject>(
				treeObjectComparator);
		// If it's a group, add subgroups and possibly strings
		if (obj.getType() == TreeObject.NodeType.GROUP) {
			for (String gid : table.getGroupList()) {
				if (PolyglotStringTable.isSubgroupOf(obj.path, gid, true)) {
					String cid = PolyglotStringTable
							.extractGroup(obj.path, gid);
					// This creates no doublets, since the treeObjectComparator
					// recognizes identical paths:
					boolean isComplete = table.isCompleteGroup(cid);
					if(!showOnlyIncomplete || !isComplete) 
						children.add(new Group(cid, isComplete));
				}
			}
			if (includeStrings) {
				for (String sid : table.getStringsInGroup(obj.path)) {
					children.add(new PolyglotString(obj.path, sid, table
							.isCompleteString(sid)));
				}
			}
		}
		// If it's a string, add variants
		if (obj.getType() == TreeObject.NodeType.POLYGLOTSTRING
				&& includeVariants) {
			for (String l : table
					.getSupportedLanguages(((PolyglotString) obj).stringID)) {
				children.add(new Variant(obj.path,
						((PolyglotString) obj).stringID, new Language(l, table
								.getLanguageDescription(l), !table
								.getLanguageList().contains(l), table
								.getSupport(l)), table.getUnformattedString(
								((PolyglotString) obj).stringID, l)));
			}
		}

		return new ArrayList<TreeObject>(children);
	}
	
	/**
	 * Returns the tree path to the specified node (or null, if the node is
	 * not part of this tree) according to the given parent map
	 * @param node The node for which the path should be obtained
	 * @param parents A map which maps children to a parent
	 */
	protected TreePath getNodePath(TreeObject node, HashMap<TreeObject, TreeObject> parents) {
		ArrayList<TreeObject> path = new ArrayList<TreeObject>();
		
		if(node == null) return null;
		if(root.equals(node)) return new TreePath(root);
		if(!parents.keySet().contains(node)) return null;
				
		TreeObject p = node;
		do {
			path.add(0, p);
			p = parents.get(p);
		} while( p != null);
		return new TreePath(path.toArray());
	}

//	/**
//	 * Returns true if there is a group in the associated table which has this
//	 * path or is a subgroup of this path (i.e. path must be a substring of a
//	 * group id). As null is considered the root node, it is always a valid path
//	 */
//	public boolean isValidTreePath(String path) {
//		if (table == null)
//			return false;
//		if (path == null)
//			return true;
//		for (String gid : table.getGroupList()) {
//			if (gid.startsWith(path))
//				return true;
//		}
//		return false;
//	}
//
//	/**
//	 * Returns true, if this object is an object of this tree model, i.e. it is
//	 * of type TreeObject and has a valid tree path.
//	 */
//	public boolean isOfThisTree(Object o) {
//		return (o != null && o instanceof TreeObject && isValidTreePath(((TreeObject) o).path));
//	}

	// TREE MODEL INTERFACE **********************************
	// *******************************************************

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		if (l != null)
			listeners.add(l);
	}

	/**
	 * Returns the index-th child of parent or null, if parent is not an object
	 * of this tree or the index is out of bounds
	 */
	@Override
	public Object getChild(Object parent, int index) {
		try {
			return children.get(parent).get(index);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the number of children (-1 if parent is not an object of this
	 * tree)
	 */
	@Override
	public int getChildCount(Object parent) {
		try {
			return children.get(parent).size();
		}
		catch(Exception e) {
			return -1;
		}
	}

	/**
	 * Returns the index of child (or -1 if parent or child are not nodes of
	 * this tree)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		try {
			return children.get(parent).indexOf(child);
		}
		catch(Exception e) {
			return -1;
		}
	}

	/**
	 * Returns a Group object with null path
	 */
	@Override
	public Object getRoot() {
		return root;
	}

	/**
	 * Returns true, if node is an object of this tree with empty children list
	 */
	@Override
	public boolean isLeaf(Object node) {
		try {
			return children.get(node).size() == 0;
		} 
		catch(Exception e) {
			return false;
		}
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	/**
	 * Ignored, since node values are only changed via the associated table model
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		//Ignored
	}


	// TABLEMODELLISTENER INTERFACE****************************
	// ********************************************************

	public void fireTreeNodesChanged(TreeModelEvent e) {
		for(TreeModelListener l : listeners) l.treeNodesChanged(e);
	}
	public void fireTreeNodesInserted(TreeModelEvent e) {
		for(TreeModelListener l : listeners) l.treeNodesInserted(e);
	}
	public void fireTreeNodesRemoved(TreeModelEvent e) {
		for(TreeModelListener l : listeners) l.treeNodesRemoved(e);
	}
	public void fireTreeStructureChanged(TreeModelEvent e) {
		for(TreeModelListener l : listeners) l.treeStructureChanged(e);
	}
	
	/**
	 * Notifies, if variants are included
	 */
	@Override
	public void languageListChanged(PolyglotTableModel source) {
		if (includeVariants)
			syncData();
	}

	/**
	 * Notifies always
	 */
	@Override
	public void stringTableChanged(PolyglotTableModel source) {
		syncData();
	}

	/**
	 * Ignored
	 */
	@Override
	public void tableDescriptionChanged(PolyglotTableModel source) {
		// Ignored
	}

	/**
	 * Ignored
	 */
	@Override
	public void tableIDChanged(PolyglotTableModel source) {
		// Ignored
	}

}
