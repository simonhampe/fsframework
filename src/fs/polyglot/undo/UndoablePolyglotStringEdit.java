package fs.polyglot.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.polyglot.model.PolyglotString;
import fs.polyglot.model.PolyglotTableModel;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;

/**
 * This class represents a change in a polyglot string, i.e. an addition, a
 * removal or a change of its group or name (Changes to variants are represented by
 * UndoableVariantEdit). Usually an instance of this class is not created
 * directly but via an UndoableEditFactory instance.
 * 
 * @author Simon Hampe
 * 
 */
public class UndoablePolyglotStringEdit extends AbstractUndoableEdit {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = -3300274457025002070L;

	// Change parameters
	PolyglotString oldValue;
	PolyglotString newValue;
	PolyglotTableModel table;

	// Resource
	PolyglotStringLoader loader;
	String languageID;

	/**
	 * Creates an edit representing a change to a polyglot string
	 * 
	 * @param oldValue
	 *            The old value. If null, this is considered a removal
	 * @param newValue
	 *            The new value. If null, this is considered an addition. If
	 *            both are null, this operation has no effect and if both are
	 *            non-null, this operation is either a group change (if both id's are equal) or 
	 *            a renaming (if they are not)
	 * @param table
	 *            The table in which to perform the changes. If this is null,
	 *            the edit has no effect.
	 * @param loader
	 *            The string loader for loading representation names. If null,
	 *            the default loader is used
	 * @param languageID
	 *            The language id for the representation names. If null, the
	 *            global language id is used.
	 */
	public UndoablePolyglotStringEdit(PolyglotString oldValue,
			PolyglotString newValue, PolyglotTableModel table,
			PolyglotStringLoader loader, String languageID) {
		super();
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.table = table;
		this.loader = loader != null ? loader : PolyglotStringLoader
				.getDefaultLoader();
		this.languageID = languageID != null ? languageID : PolyglotStringTable
				.getGlobalLanguageID();
	}

	/**
	 * @return true, if and only if one of the following is true: <br>
	 *         - Both values or the table are null <br>
	 *         - This represents a removal and the old string id exists in the
	 *         table <br>
	 *         - This represents an addition and the new string id does not
	 *         exist in the table <br>
	 *         - This represents a group change or rename and the id of oldValue exists in the table
	 */
	@Override
	public boolean canRedo() {
		if ((table == null) || (oldValue == null && newValue == null))
			return true;
		// Addition
		if (oldValue == null)
			return !table.containsStringID(newValue.stringID);
		// Removal
		if (newValue == null)
			return table.containsStringID(oldValue.stringID);
		// Group change or rename
		if (oldValue.stringID == null || newValue.stringID == null)
			return false;
		return (table.containsStringID(oldValue.stringID));
	}

	/**
	 * @return true, if and only if one of the following is true: <br>
	 *         - Both values or the table are null <br>
	 *         - This represents a removal and the old string id doesn't exist
	 *         in the table <br>
	 *         - This represents an addition and the new string id exists in the
	 *         table <br>
	 *         - This represents a group change or rename and the id of oldvalue exists in the table.
	 */
	@Override
	public boolean canUndo() {
		if ((table == null) || (oldValue == null && newValue == null))
			return true;
		// Addition
		if (oldValue == null)
			return table.containsStringID(newValue.stringID);
		// Removal
		if (newValue == null)
			return !table.containsStringID(oldValue.stringID);
		// Group change or rename
		if(oldValue.stringID == null || newValue.stringID == null)
			return false;
		return (table.containsStringID(newValue.stringID));
	}

	/**
	 * Convenience method that returns a representation name for an edit from
	 * oldval to newval in the language of this edit
	 */
	public String getStringEditPresentationName(PolyglotString oldval,
			PolyglotString newval) {
		if (oldval == null && newval == null)
			loader.getString("fs.polyglot.undo.stringgeneral", languageID,
					"null", "null");
		// Addition
		if (oldval == null)
			return loader.getString("fs.polyglot.undo.stringadd", languageID,
					newval.stringID);
		// Removal
		if (newval == null)
			return loader.getString("fs.polyglot.undo.stringremove",
					languageID, oldval.stringID);
		// Group change
		if (newval.stringID.equals(oldval.stringID))
			return loader.getString("fs.polyglot.undo.stringgroup", languageID,
					newval.stringID, oldval.path, newval.path);
		// Any other change
		return loader.getString("fs.polyglot.undo.stringgeneral", languageID,
				oldval.toString(), newval.toString());
	}

	/**
	 * Returns a general description
	 */
	@Override
	public String getPresentationName() {
		return getStringEditPresentationName(oldValue, newValue);
	}

	/**
	 * Returns the appropriate representation name
	 */
	@Override
	public String getRedoPresentationName() {
		return getStringEditPresentationName(oldValue, newValue);
	}

	/**
	 * Returns the appropriate representation name
	 */
	@Override
	public String getUndoPresentationName() {
		return getStringEditPresentationName(newValue, oldValue);
	}

	/**
	 * Tries to perform the change represented by this edit
	 * 
	 * @throws CannotRedoException
	 *             - If the operation cannot be performed
	 */
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		try {
			performStringEdit(table, oldValue, newValue);
		} catch (UnsupportedOperationException ue) {
			throw new CannotRedoException();
		}
	}

	/**
	 * Tries to reverse the change represented by this edit
	 * 
	 * @throws CannotUndoException
	 *             - If the operation cannot be reversed
	 */
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		try {
			performStringEdit(table, newValue, oldValue);
		} catch (UnsupportedOperationException ue) {
			throw new CannotUndoException();
		}
	}

	/**
	 * This method tries to perform the change on the specified table as
	 * indicated by the old and new values
	 * 
	 * @throws UnsupportedOperationException
	 *             - If the operation cannot be performed. See the documentation
	 *             of the canUndo() and canRedo() methods for a detailed
	 *             explanation.
	 */
	public static void performStringEdit(PolyglotTableModel table,
			PolyglotString oldval, PolyglotString newval)
			throws UnsupportedOperationException {
		if (table == null || (oldval == null && newval == null))
			return;
		// Addition
		if (oldval == null) {
			if (table.containsStringID(newval.stringID))
				throw new UnsupportedOperationException();
			table.addStringID(newval.stringID);
			table.setGroupID(newval.stringID, newval.path);
			return;
		}
		// Removal
		if (newval == null) {
			if (!table.containsStringID(oldval.stringID))
				throw new UnsupportedOperationException();
			table.removeID(oldval.stringID);
			return;
		}
		// Group change or rename
		if (!table.containsStringID(oldval.stringID) || newval.stringID == null)
				throw new UnsupportedOperationException();
		if(!oldval.stringID.equals(newval.stringID)) {
			table.renameString(oldval.stringID, newval.stringID);
			table.setGroupID(newval.stringID, newval.path);
		}
		else table.setGroupID(oldval.stringID, newval.path);
	}
}
