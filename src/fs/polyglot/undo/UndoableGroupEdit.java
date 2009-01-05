package fs.polyglot.undo;

import java.util.HashSet;

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
 * - affect subgroups as well. Normally, only strings that are directly in the given group are affected. If this option is activated, subgroup are moved to
 * the new group as well. <br>
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
	private String oldValue; 
	private String newValue;
	private HashSet<String> idsToMove = new HashSet<String>();
	private boolean renameIDs = false;
	private boolean affectSubGroups = false;
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
		this.affectSubGroups = affectSubGroups;
		this.loader = (loader != null) ? loader : PolyglotStringLoader.getDefaultLoader();
		this.languageID = (languageID != null) ? languageID : PolyglotStringTable.getGlobalLanguageID();
		//Create static string list
		idsToMove = affectSubGroups ? table.getStringsInSubgroups(oldValue) : table.getStringsInGroup(oldValue);
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
	 * @return True, if all strings that were registered for group change at creation time are still under the new group path
	 */
	@Override
	public boolean canUndo() {
		return true;
	}

	/**
	 * @return false, if and only if newValue is a subgroup of oldValue and subgroups are affected (as this would cause recursion)
	 */
	@Override
	public boolean canRedo() {
		return !(PolyglotTableModel.isSubgroupOf(oldValue, newValue, false) && affectSubGroups);
	}

	/**
	 * Returns the string associated to this edit
	 */
	@Override
	public String getPresentationName() {
		return getLanguageEditPresentationName(oldValue, newValue);
	}

	/**
	 * Convenience method returning the representation name of a language change
	 * from oldval to newval (in the language of this edit)
	 */
	public String getLanguageEditPresentationName(String oldValue, String newValue) {
		return loader.getString("fs.polyglot.undo.groupgeneral",languageID,oldValue, newValue);
	}

	/**
	 * Returns the appropriate string according to the associated operation
	 */
	@Override
	public String getRedoPresentationName() {
		return getLanguageEditPresentationName(oldValue, newValue);
	}

	/**
	 * Returns the appropriate string according to the associated operation
	 */
	@Override
	public String getUndoPresentationName() {
		return getLanguageEditPresentationName(newValue, oldValue);
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
		for(String s : idsToMove) {
			//TODO: Argh
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
		try {
			performLanguageEdit(table, newValue, oldValue);
		} catch (UnsupportedOperationException ue) {
			// Forward exception
			throw new CannotUndoException();
		}
	}

	/**
	 * Convenience method which changes a group oldval to newval in the
	 * specified table, i.e. does the following: All strings that are in group
	 * oldval are moved to group newval.
	 * 
	 * @param table
	 *            The table in which the change should be effected (If null,
	 *            then this call has no effect)
	 * @param oldValue
	 *            The old value.
	 * @param newValue
	 *            The new value.
	 * @throws UnsupportedOperationException
	 *             - If this operation cannot be performed (never thrown)
	 */
	public static void performLanguageEdit(PolyglotTableModel table,
			String oldValue, String newValue)
			throws UnsupportedOperationException {
		if (table == null || (oldValue == null && newValue == null))
			return;
		HashSet<String> groupstrings = table.getStringsInGroup(oldValue);
		for (String s : groupstrings) {
			table.setGroupID(s, newValue);
		}
	}

}
