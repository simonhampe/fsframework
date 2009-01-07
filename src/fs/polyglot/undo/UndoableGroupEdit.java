package fs.polyglot.undo;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.polyglot.model.PolyglotTableModel;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;

/**
 * This edit represents a group change, i.e. essentially changing a group name. This will automatically change the group attribute of all strings in this group
 * to the new group attribute. null is also allowed as new group attribute, this will 'move' all strings in the concerned group to the root group. Additional
 * options are: <br>
 * - rename all strings which have the group path as prefix to have the new path as prefix<br>
 * - affect subgroups as well. Normally, only strings that are directly in the given group are affected. If this option is activated, subgroups are moved to
 * the new group as well and subgroup relations are preserved.(This is only possible, if the new group is not a subgroup of the old group) <br>
 * This edit is static, i.e. upon creation it will create a list of all strings to be moved which will remain constant regardless of any changes to the table.
 * @author Simon Hampe
 * 
 */
public class UndoableGroupEdit extends AbstractUndoableEdit {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = -1096439921227612760L;
	//Change parameters
	private String oldValue; //old group
	private String newValue; //new group
	
	//These lists should be in 1:1 - correspondance, i.e. each string in one list corresponds to the string at the exact position in one of the
	//other lists
	private ArrayList<String> idsToMove = new ArrayList<String>(); //Strings which should be moved, when redo is called
	private ArrayList<String> idsToMoveBack = new ArrayList<String>(); //Strings which should be moved, when undo is called
	private ArrayList<String> groupsForRedo = new ArrayList<String>(); //The groups to set on redo
	private ArrayList<String> groupsForUndo = new ArrayList<String>(); //The groups to set on undo
	
	
	private boolean renameIDs = false;							//Should strings be renamed, which have their group's path as prefix?
	private PolyglotTableModel table;	

	// Resource
	PolyglotStringLoader loader;
	String languageID;

	/**
	 * 
	 * @throws NullPointerException
	 *             - if table == null
	 */
	public UndoableGroupEdit(String oldValue, String newValue, boolean renameIDs, boolean affectSubGroups, PolyglotTableModel table, PolyglotStringLoader loader, String languageID) {
		if(table == null) throw new NullPointerException("Can't create undoable edit for null table");
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.table = table;		
		this.renameIDs = renameIDs;
		this.loader = (loader != null) ? loader : PolyglotStringLoader.getDefaultLoader();
		this.languageID = (languageID != null) ? languageID : PolyglotStringTable.getGlobalLanguageID();
		//Create static string lists
		idsToMove = affectSubGroups ? new ArrayList<String>(table.getStringsInSubgroups(oldValue)) : new ArrayList<String>(table.getStringsInGroup(oldValue));
		for(String id : idsToMove) {
			//If ids are renamed, all ids which start with oldValue + "." are renamed by replacing oldValue by newValue or, if newValue is null,
			//by replacing oldValue + "." by newValue
			idsToMoveBack.add(renameIDs && id.startsWith(oldValue + ".") && oldValue != null ? id.replaceFirst(newValue != null ? oldValue : oldValue + ".", newValue == null ? "" : newValue) : id);
			String gid = table.getGroupID(id);
			//Save the original group id
			groupsForUndo.add(gid);
			//Create the new group ids
			//If subgroups are not affected or the string's group IS the oldvalue, the new group is just newValue
			if(!affectSubGroups || (oldValue == null? gid == null : oldValue.equals(gid))) groupsForRedo.add(newValue);
			//If newValue == null, replace oldValue + ".", otherwise oldValue
			else groupsForRedo.add((gid != null ? gid : "").replaceFirst((oldValue == null ? "" : oldValue) + (newValue == null? "." : ""),newValue == null? "" : newValue));
		}
	}

	// GETTERS
	// ******************************************************************
	// **************************************************************************

	/**
	 * Returns the old value before the change represented by this edit
	 */
	public String getOldValue() {
		return oldValue;
	}

	/**
	 * Returns the new value after the change represented by this edit
	 */
	public String getNewValue() {
		return newValue;
	}

	// UNDOABLEEDIT METHODS
	// *****************************************************
	// **************************************************************************

	/**
	 * @return True, if and only if all (possibly renamed) string ids that were registered for group change at creation time still exist (under any group path) 
	 */
	@Override
	public boolean canUndo() {
		for(String id : idsToMoveBack) {
			if(!table.containsStringID(id)) return false;
		}
		return true;
	}

	/**
	 * @return True, if and only if all string ids that were registered for group change at creation time still exist (under any group)
	 */
	@Override
	public boolean canRedo() {
		for(String id : idsToMove) {
			if(!table.containsStringID(id)) return false;
		}
		return true;
	}

	/**
	 * Returns the string associated to this edit
	 */
	@Override
	public String getPresentationName() {
		return getGroupEditPresentationName(oldValue, newValue);
	}

	/**
	 * Convenience method returning the representation name of a language change
	 * from oldval to newval (in the language of this edit)
	 */
	public String getGroupEditPresentationName(String oldValue, String newValue) {
		return loader.getString("fs.polyglot.undo.groupgeneral",languageID,oldValue, newValue);
	}

	/**
	 * Returns the appropriate string according to the associated operation
	 */
	@Override
	public String getRedoPresentationName() {
		return getGroupEditPresentationName(oldValue, newValue);
	}

	/**
	 * Returns the appropriate string according to the associated operation
	 */
	@Override
	public String getUndoPresentationName() {
		return getGroupEditPresentationName(newValue, oldValue);
	}

	/**
	 * Tries to perform the edit represented by this object
	 * 
	 * @throws CannotRedoException
	 *             - if the operation cannot be performed
	 */
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		//Just move all registered strings to the new group, renaming them, if necessary
		for(int i = 0; i < idsToMove.size(); i++) {
			//Rename
			if(renameIDs) table.renameString(idsToMove.get(i), idsToMoveBack.get(i));
			//Move to group
			table.setGroupID(idsToMoveBack.get(i), groupsForRedo.get(i));
		}
	}

	/**
	 * Tries to perform the reverse edit of the one represented by this object
	 * 
	 * @throws CannotUndoException
	 *             - If the operation cannot be reversed
	 */
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		//Just move all registered strings to the new group, renaming them, if necessary
		for(int i = 0; i < idsToMoveBack.size(); i++) {
			//Rename
			if(renameIDs) table.renameString(idsToMoveBack.get(i), idsToMove.get(i));
			//Move to group
			table.setGroupID(idsToMove.get(i), groupsForUndo.get(i));
		}
	}

}
