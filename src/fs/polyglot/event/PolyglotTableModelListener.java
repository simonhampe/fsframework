package fs.polyglot.event;

import fs.polyglot.model.PolyglotTableModel;

/**
 * Listens to all possible changes in a PolyglotTableModel. There is no separate
 * methods to listen for changes in the group structure, since these are too
 * closely related to the string table structure, so any change in the first one
 * will be related to a change in the latter one.
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

	//TODO: Has to be finer grained, since otherwise jtrees will be collapsing all the time
	
}
