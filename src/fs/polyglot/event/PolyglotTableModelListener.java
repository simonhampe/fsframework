package fs.polyglot.event;

import fs.polyglot.model.Group;
import fs.polyglot.model.PolyglotString;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.model.Variant;

/**
 * Listens to all possible changes in a PolyglotTableModel. Most of the listener methods correspond to the methods of 
 * TreeModelListener.
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

	
	public void groupChanged(PolyglotTableModel source, Group group);
	
	public void groupInserted(PolyglotTableModel source, Group group);
	
	public void groupRemoved(PolyglotTableModel source, Group group);
	
	public void stringChanged(PolyglotTableModel source, PolyglotString string);
	
	public void stringInserted(PolyglotTableModel source, PolyglotString string);
	
	public void stringRemoved(PolyglotTableModel source, PolyglotString string);
	
	public void variantChanged(PolyglotTableModel source, Variant variant);
	
	public void variantInserted(PolyglotTableModel source, Variant variant);
	
	public void variantRemoved(PolyglotTableModel source, Variant variant);
	
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
