package fs.polyglot.undo;

import java.util.HashSet;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.polyglot.model.PolyglotTableModel;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;

/**
 * This edit represents a group change, i.e. changing a group name. This will
 * automatically change the group attribute of all strings in this group to the
 * new group attribute. null is also allowed as new group attribute, this will
 * 'move' all strings in the concerned group to the root group
 * 
 * @author Simon Hampe
 * 
 */
public class UndoableGroupEdit extends AbstractUndoableEdit {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = -1096439921227612760L;
	// Change parameters
	private String oldValue;
	private String newValue;
	private PolyglotTableModel table;

	// Resource
	PolyglotStringLoader loader;
	String languageID;

	/**
	 * 
	 * @throws NullPointerException
	 *             - if table == null
	 */
	public UndoableGroupEdit(String oldValue, String newValue,
			PolyglotTableModel table, PolyglotStringLoader loader,
			String languageID) {
		if (table == null)
			throw new NullPointerException(
					"Can't create undoable edit for null table");
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.table = table;
		this.loader = (loader != null) ? loader : PolyglotStringLoader
				.getDefaultLoader();
		this.languageID = (languageID != null) ? languageID
				: PolyglotStringTable.getGlobalLanguageID();

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
	 * @return Always true (though the actual edit might have no effect, if
	 *         there is no string in the group concerned (hence it doesn't
	 *         exist)
	 */
	@Override
	public boolean canUndo() {
		return true;
	}

	/**
	 * @return Always true (though the actual edit might have no effect, if
	 *         there is no string in the group concerned (hence it doesn't
	 *         exist)
	 */
	@Override
	public boolean canRedo() {
		return true;
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
	public String getLanguageEditPresentationName(String oldValue2,
			String newValue2) {
		return loader.getString("fs.polyglot.undo.groupgeneral", languageID,
				oldValue, newValue);
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
		try {
			performLanguageEdit(table, oldValue, newValue);
		} catch (UnsupportedOperationException ue) {
			// Forward exception
			throw new CannotRedoException();
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
