package fs.polyglot.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fs.polyglot.model.Language;
import fs.polyglot.model.PolyglotTableModel;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;

/**
 * Implements an UndoableEdit that represents a change in the language list of a 
 * polyglot string table. Usually the user should not create an instance of this class directly, but via an
 * UndoableEditFactory instance.
 * @author Simon Hampe
 *
 */
public class UndoableLanguageEdit extends AbstractUndoableEdit {

	/**
	 * compiler-generated version id 
	 */
	private static final long serialVersionUID = -1627380377727263043L;
	
	//Change parameters
	private Language oldValue;
	private Language newValue;
	private PolyglotTableModel table;
	
	//Resource
	PolyglotStringLoader loader;
	String languageID;
	
	/**
	 * Creates an UndoableLanguageEdit instance
	 * @param oldValue The old value of the language. null indicates that a new language was added. If old and new value are both null, this edit
	 * is considered without effect and it will not post itself automatically
	 * @param newValue The new value of the language. null indicates that a language was removed. If old and new value are both null, this edit
	 * is considered without effect and it will not post itself automatically 
	 * @param table The table in which the change was performed. If this is null, the edit has no effect. 
	 * @param loader The string loader for representation names. If null, the default loader is used. If UndoableEdits are created via an UndoableEditFactory,
	 * the factory already has obtained the string loader and passes it on.
	 * @param languageID The language in which to create the representation string. If null, the global language id is used. If UndoableEdits are created via
	 * an UndoableEditFactory the factory uses a common language ID for all created edits.
	 * @throws NullPointerException - if table == null
	 */
	public UndoableLanguageEdit(Language oldValue, Language newValue, PolyglotTableModel table, PolyglotStringLoader loader, String languageID) {
		if(table == null) throw new NullPointerException("Can't create undoable edit for null table");
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.table = table;
		
		this.loader = (loader != null) ? loader : PolyglotStringLoader.getDefaultLoader();
		this.languageID = (languageID != null) ? languageID : PolyglotStringTable.getGlobalLanguageID();
		
	}
	
	// GETTERS ******************************************************************
	// **************************************************************************
	
	/**
	 * Returns the old value before the change represented by this edit
	 */
	public Language getOldValue() {
		return oldValue;
	}
	
	/**
	 * Returns the new value after the change represented by this edit
	 */
	public Language getNewValue() {
		return newValue;
	}
	
	// UNDOABLEEDIT METHODS *****************************************************
	// **************************************************************************
	
	/**
	 * @return true, if and only if one of the following statements is true: <br>
	 * - This edit represents a language change and the new language id exists in the table <br>
	 * - This edit represents a language addition and the new language id still exists <br>
	 * - This edit represents a language removal and the old language id does not exist in the table <br>
	 * - Both old and new value are null
	 */
	@Override
	public boolean canUndo() {
		if((oldValue == null && newValue == null) || table == null ) return true;
		//Language removal
		if(newValue == null) return !table.containsLanguage(oldValue.id);
		//Language change or addition
		return table.containsLanguage(newValue.id);
	}
	
	/**
	 * @return true, if and only if one of the following statements is true: <br>
	 * - This edit represents a language change and the old language id exists in the table <br>
	 * - This edit represents a language addition and the new language id does not exist in the table <br>
	 * - This edit represents a language removal and the old language id exists in the table <br>
	 * - Both old and new value are null
	 */
	@Override
	public boolean canRedo() {
		if((oldValue == null && newValue == null) || table == null) return true;
		//Language addition
		if(oldValue == null) return !table.containsLanguage(newValue.id);
		//Language removal or change
		return table.containsLanguage(oldValue.id);
	}
	
	/**
	 * Returns the string associated to this edit
	 */
	@Override
	public String getPresentationName() { return getLanguageEditPresentationName(oldValue, newValue); }

	/**
	 * Convenience method returning the representation name of a language change from oldval to newval (in the language of this edit)
	 */
	public String getLanguageEditPresentationName(Language oldval, Language newval) {
		//Null edit
		if(oldval == null && newval == null) return loader.getString("fs.polyglot.undo.languagegeneral",languageID,"null", "null");
		//Addition
		if(oldval== null) return loader.getString("fs.polyglot.undo.languageadd", languageID, newval.description);
		//Removal
		if(newval == null) return loader.getString("fs.polyglot.undo.languageremove", languageID, oldval.description);
		//Change in description
		if(oldValue.id.equals(newValue.id) && !oldValue.description.equals(newValue.description)) 
			return loader.getString("fs.polyglot.undo.languagedescription", languageID, oldval.description, newval.description);
		//Other changes
		return loader.getString("fs.polyglot.undo.languagegeneral",languageID,oldval.toString(), newval.toString());
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
	 * @throws CannotRedoException - if the operation cannot be performed
	 */
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		try {
			performLanguageEdit(table, oldValue, newValue);
		}
		catch(UnsupportedOperationException ue) {
			//Forward exception
			throw new CannotRedoException();
		}
		
		
	}

	/**
	 * Tries to perform the reverse edit of the one represented by this object
	 * @throws CannotUndoException - If the operation cannot be reversed
	 */
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		try {
			performLanguageEdit(table, newValue, oldValue);
		}
		catch(UnsupportedOperationException ue) {
			//Forward exception
			throw new CannotUndoException();
		}
	}
	
	/**
	 * Convenience method which changes a language oldval to newval in the language list of the specified table
	 * @param table The table in which the change should be effected (If null, then this call has no effect)
	 * @param oldval The old value. If null, this is considered an addition of a language
	 * @param newval The new value. If null, this is considered a removal of a language (if both are null, this call has no effect)
	 * @throws UnsupportedOperationException - If this operation cannot be performed
	 */
	public static void performLanguageEdit(PolyglotTableModel table, Language oldval, Language newval) throws UnsupportedOperationException {
		if(table == null || (oldval == null && newval == null)) return;
		//Addition
		if(oldval == null) {
			if(table.containsLanguage(newval.id)) throw new UnsupportedOperationException();
			table.putLanguage(newval.id, newval.description);
			return;
		}
		//Removal
		if(newval == null) {
			if(!table.containsLanguage(oldval.id)) throw new UnsupportedOperationException();
			table.removeLanguage(oldval.id);
			return;
		}
		//Change
		if(!table.containsLanguage(oldval.id)) throw new UnsupportedOperationException();
		table.removeLanguage(oldval.id);
		table.putLanguage(newval.id, newval.description);
	}
	
	
}
