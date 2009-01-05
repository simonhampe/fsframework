package fs.polyglot.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import fs.polyglot.model.Language;
import fs.polyglot.model.PolyglotString;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.model.Variant;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;

/**
 * This is a factory class for UndoableEdits on a PolyglotTableModel. More
 * precisely, for the following classes: <br>
 * - UndoableLanguageEdit <br>
 * - UndoablePolyglotStringEdit <br>
 * - UndoableVariantEdit <br>
 * - UndoableTableEdit <br>
 * A factory instance is created with a link to a table on which this edits
 * occur, a string loader and a language id, as well as an UndoManager for
 * receiving created edits. For each class, there are two methods for creating
 * instances:<br>
 * - create... - which creates an undoable edit - perform... - which creates an
 * undoable edit and calls redo() on it after creation. If an undo manager has
 * been specified upon factory creation, the edit is posted to it.
 * 
 * @author Simon Hampe
 * 
 */
public class UndoableEditFactory {

	// The edited table
	PolyglotTableModel table;

	// String resources
	PolyglotStringLoader loader;
	String languageID;

	// An UndoManager for edit posting
	UndoManager manager;

	/**
	 * Creates a factory that will produce edits on the specified table
	 * 
	 * @param table
	 *            The edited table. May be null, but then all edits have no
	 *            effect
	 * @param loader
	 *            A string loader for loading representation names. If null, the
	 *            default loader is used
	 * @param languageID
	 *            The language ID for the representation names. If null, the
	 *            global language ID is used.
	 * @param manager
	 *            An UndoManager for posting created edits to. If not null, all
	 *            edits that are created via a perform... - method are posted
	 *            automatically to this manager.
	 */
	public UndoableEditFactory(PolyglotTableModel table,
			PolyglotStringLoader loader, String languageID, UndoManager manager) {
		super();
		this.table = table;
		this.loader = loader != null ? loader : PolyglotStringLoader
				.getDefaultLoader();
		this.languageID = languageID != null ? languageID : PolyglotStringTable
				.getGlobalLanguageID();
		this.manager = manager;
	}

	private void postEdit(UndoableEdit edit) {
		if (manager != null)
			manager.addEdit(edit);
	}

	public UndoableLanguageEdit createUndoableLanguageEdit(Language oldValue,
			Language newValue) {
		return new UndoableLanguageEdit(oldValue, newValue, table, loader,
				languageID);
	}

	public UndoableLanguageEdit performUndoableLanguageEdit(Language oldValue,
			Language newValue) throws CannotRedoException {
		UndoableLanguageEdit edit = createUndoableLanguageEdit(oldValue,
				newValue);
		edit.redo();
		postEdit(edit);
		return edit;
	}

	public UndoablePolyglotStringEdit createUndoablePolyglotStringEdit(
			PolyglotString oldValue, PolyglotString newValue) {
		return new UndoablePolyglotStringEdit(oldValue, newValue, table,
				loader, languageID);
	}

	public UndoablePolyglotStringEdit performUndoablePolyglotStringEdit(
			PolyglotString oldValue, PolyglotString newValue)
			throws CannotRedoException {
		UndoablePolyglotStringEdit edit = createUndoablePolyglotStringEdit(
				oldValue, newValue);
		edit.redo();
		postEdit(edit);
		return edit;
	}

	public UndoableVariantEdit createUndoableVariantEdit(Variant oldValue,
			Variant newValue) {
		return new UndoableVariantEdit(oldValue, newValue, table, loader,
				languageID);
	}

	public UndoableVariantEdit performUndoableVariantEdit(Variant oldValue,
			Variant newValue) throws CannotRedoException {
		UndoableVariantEdit edit = createUndoableVariantEdit(oldValue, newValue);
		edit.redo();
		postEdit(edit);
		return edit;
	}
	
	public UndoableGroupEdit createUndoableGroupEdit(String oldValue, String newValue, boolean renameIDs, boolean affectSubgroups) {
		return new UndoableGroupEdit(oldValue, newValue, renameIDs, affectSubgroups, table, loader, languageID);
	}
	
	public UndoableGroupEdit performUndoableGroupEdit(String oldValue, String newValue, boolean renameIDs, boolean affectSubgroups) throws CannotRedoException {
		UndoableGroupEdit edit = createUndoableGroupEdit(oldValue, newValue, renameIDs, affectSubgroups);
		edit.redo();
		postEdit(edit);
		return edit;
	}
	
	public UndoableTableEdit createUndoableTableEdit(String oldValue, String newValue, boolean isIdEdit) {
		return new UndoableTableEdit(oldValue, newValue, isIdEdit, table, loader, languageID);
	}

	public UndoableTableEdit performUndoableTableEdit(String oldValue,
			String newValue, boolean isIdEdit) throws CannotRedoException {
		UndoableTableEdit edit = createUndoableTableEdit(oldValue, newValue,
				isIdEdit);
		edit.redo();
		postEdit(edit);
		return edit;
	}

}
