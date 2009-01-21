package fs.polyglot.event;

import fs.polyglot.model.PolyglotTableModel;

/**
 * Listens to all possible changes in a PolyglotTableModel. This is very coarse-grained, since it would be a serious performance impact to calculate 
 * the exact changes
 */
public interface PolyglotTableModelListener {

	/**
	 * This method is called, when the table id has been changed.
	 */
	public void tableIDChanged(PolyglotTableModel source);

	/**
	 * This method is called when the table description has been changed
	 */
	public void tableDescriptionChanged(PolyglotTableModel source);
	
	/**
	 * This method is called, when any changes were committed to the language
	 * list
	 */
	public void languageListChanged(PolyglotTableModel source);

	/**
	 * This method is called when any heavy structural changes were committed to the actual
	 * string table that cannot be reflected by one of the other methods
	 */
	public void stringTableChanged(PolyglotTableModel source);
	
}
