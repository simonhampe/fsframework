package fs.polyglot.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.model.Variant;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;

/**
 * This class represents a change to polyglotstring variant, i.e. an addition, a
 * removal or a change of value.
 * 
 * @author Simon Hampe
 * 
 */
public class UndoableVariantEdit extends AbstractUndoableEdit {
	/**
	 *compiler-generated version id
	 */
	private static final long serialVersionUID = -1150137045757683628L;

	// Change parameters
	Variant oldValue;
	Variant newValue;
	PolyglotTableModel table;

	// Resource
	PolyglotStringLoader loader;
	String languageID;

	/**
	 * Creates an edit representing a change to a variant
	 * 
	 * @param oldValue
	 *            The old value. If null, this is considered a removal
	 * @param newValue
	 *            The new value. If null, this is considered an addition. If
	 *            both are null, this operation has no effect and if both are
	 *            non-null, this operation is only valid, if both have the same
	 *            string id and language id. In this case this edit represents a
	 *            value change
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
	public UndoableVariantEdit(Variant oldValue, Variant newValue,
			PolyglotTableModel table, PolyglotStringLoader loader,
			String languageID) {
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
	 *         - This represents a removal or addition <br>
	 *         - This represents a value change, both variants have the same
	 *         string and language id, and the old variant exists in the table
	 */
	@Override
	public boolean canRedo() {
		if ((table == null) || (oldValue == null && newValue == null))
			return true;
		// Addition or removal
		if (oldValue == null || newValue == null) return true;
		// Value change
		return (newValue.stringID.equals(oldValue.stringID)
				&& newValue.language.id.equals(oldValue.language.id) && table
				.getUnformattedString(oldValue.stringID, oldValue.language.id) != null);
	}

	/**
	 * @return true, if and only if one of the following is true: <br>
	 *         - Both values or the table are null <br>
	 *         - This represents a removal or addition <br>
	 *         - This represents a value change, both variants have the same
	 *         string and language id, and the old variant exists in the table
	 */
	@Override
	public boolean canUndo() {
		if ((table == null) || (oldValue == null && newValue == null))
			return true;
		// Addition
		if (oldValue == null || newValue == null) return true;
		// Value change
		return (newValue.stringID.equals(oldValue.stringID)
				&& newValue.language.id.equals(oldValue.language.id) && table
				.getUnformattedString(oldValue.stringID, oldValue.language.id) != null);
	}

	/**
	 * Convenience method that returns a representation name for an edit from
	 * oldval to newval in the language of this edit
	 */
	public String getStringEditPresentationName(Variant oldval, Variant newval) {
		if (oldval == null && newval == null)
			loader.getString("fs.polyglot.undo.variantgeneral", languageID,
					"null", "null");
		// Addition
		if (oldval == null)
			return loader.getString("fs.polyglot.undo.variantadd", languageID,
					newval.stringID, newval.language.id);
		// Removal
		if (newval == null)
			return loader.getString("fs.polyglot.undo.variantremove",
					languageID, oldval.stringID, oldval.language.id);
		// value change
		if (newval.stringID.equals(oldval.stringID)
				&& newval.language.id.equals(oldval.language.id))
			return loader.getString("fs.polyglot.undo.variantvalue",
					languageID, newval.stringID, newval.language.id,
					oldval.value, newval.value);
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
			performVariantEdit(table, oldValue, newValue);
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
			performVariantEdit(table, newValue, oldValue);
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
	public static void performVariantEdit(PolyglotTableModel table,
			Variant oldval, Variant newval)
			throws UnsupportedOperationException {
		if (table == null || (oldval == null && newval == null))
			return;
		// Addition
		if (oldval == null) {
			table.putString(newval.stringID, newval.language.id, newval.value);
			return;
		}
		// Removal
		if (newval == null) {
			table.putString(oldval.stringID, oldval.language.id, null);
			return;
		}
		// value change
		if (newval.stringID.equals(oldval.stringID)
				&& newval.language.id.equals(oldval.language.id)) {
			if (table.getUnformattedString(oldval.stringID, oldval.language.id) == null)
				throw new UnsupportedOperationException();
			table.putString(oldval.stringID, oldval.language.id, newval.value);
			return;
		}
		// otherwise this is not a valid change
		throw new UnsupportedOperationException();
	}
}
