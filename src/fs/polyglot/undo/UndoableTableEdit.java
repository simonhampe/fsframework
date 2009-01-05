package fs.polyglot.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.polyglot.model.PolyglotTableModel;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;

/**
 * This class represents a change in one of a table's basic attributes id or
 * description. Usually an instance of this class should not be created directly
 * but via an UndoableEditFactory instance.
 * 
 * @author Simon Hampe
 * 
 */
public class UndoableTableEdit extends AbstractUndoableEdit {

	/**
	 * Compiler-generated version id
	 */
	private static final long serialVersionUID = 7754904722700372797L;

	// Change parameters
	String oldValue;
	String newValue;
	boolean isIdEdit;
	PolyglotTableModel table;

	// Resource
	PolyglotStringLoader loader;
	String languageID;

	/**
	 * Creates an edit representing a change to one of a table's basic
	 * attributes ID or Description.
	 * 
	 * @param oldValue
	 *            The old value. If null, this is an invalid edit, that cannot
	 *            be performed
	 * @param newValue
	 *            The new value. If null, this is an invalid edit, that cannot
	 *            be performed
	 * @param isIdEdit
	 *            If this is true, this edit represents a change to the table's
	 *            id. Otherwise it represents a change in the table's
	 *            description
	 * @param table
	 *            The table in which these changes are performed. If null, this
	 *            edit has no effect
	 * @param loader
	 *            The string loader for loading representation names. If null,
	 *            the default loader is used
	 * @param languageID
	 *            The language id for the representation names. If null, the
	 *            global language id is used.
	 */
	public UndoableTableEdit(String oldValue, String newValue,
			boolean isIdEdit, PolyglotTableModel table,
			PolyglotStringLoader loader, String languageID) {
		super();
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.isIdEdit = isIdEdit;
		this.table = table;
		this.loader = (loader != null) ? loader : PolyglotStringLoader
				.getDefaultLoader();
		this.languageID = (languageID != null) ? languageID
				: PolyglotStringTable.getGlobalLanguageID();
	}

	/**
	 * @return true if and only if both old and new value are non-null and the
	 *         attribute concerned still has the old value
	 */
	@Override
	public boolean canRedo() {
		if (table == null)
			return true;
		return (oldValue != null && newValue != null && (isIdEdit ? table
				.getIdentifier().equals(oldValue) : table.getTableDescription()
				.equals(oldValue)));
	}

	/**
	 * @return true if and only if both old and new value are non-null and the
	 *         attribute concerned still has the new value
	 */
	@Override
	public boolean canUndo() {
		if (table == null)
			return true;
		return (oldValue != null && newValue != null && (isIdEdit ? table
				.getIdentifier().equals(newValue) : table.getTableDescription()
				.equals(newValue)));
	}

	/**
	 * Returns the same as getRedoPresentationName
	 */
	@Override
	public String getPresentationName() {
		return super.getRedoPresentationName();
	}

	/**
	 * Returns the appropriate description
	 */
	@Override
	public String getRedoPresentationName() {
		return loader.getString(isIdEdit ? "fs.polyglot.undo.tableid"
				: "fs.polyglot.undo.tabledescription", languageID, oldValue,
				newValue);
	}

	/**
	 * Returns the appropriate description
	 */
	@Override
	public String getUndoPresentationName() {
		return loader.getString(isIdEdit ? "fs.polyglot.undo.tableid"
				: "fs.polyglot.undo.tabledescription", languageID, newValue,
				oldValue);
	}

	/**
	 * Tries to perform the edit represented by this edit
	 * 
	 * @throws CannotRedoException
	 *             - if the operation cannot be performed
	 */
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (table == null)
			return;
		if (isIdEdit)
			table.setTableID(newValue);
		else
			table.setTableDescription(newValue);
	}

	/**
	 * Tries to reverse this edit
	 * 
	 * @throws CannotUndoException
	 *             - If the operation cannot be reversed
	 */
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (table == null)
			return;
		if (isIdEdit)
			table.setTableID(oldValue);
		else
			table.setTableDescription(oldValue);
	}

}
