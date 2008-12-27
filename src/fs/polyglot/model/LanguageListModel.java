package fs.polyglot.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

import fs.polyglot.event.PolyglotTableModelListener;

/**
 * Implements the data model for the language list view. Languages are saved as Language objects.
 * Each LanguageListModel is associated to a PolyglotTableModel, to which it listens for changes.
 * For performance reasons, this ListModel will notify potential ListDataListeners only of content 
 * change events for the complete list range, since it would usually be too complex to figure out,
 * which subset was actually changed.
 * @author Simon Hampe
 *
 */
public class LanguageListModel extends AbstractListModel implements PolyglotTableModelListener {

	/**
	 * Compiler-generated version ID
	 */
	private static final long serialVersionUID = 4495638832621988192L;

	/**
	 * The associated table
	 */
	private PolyglotTableModel table = null;
	
	/**
	 * The data representation. Languages are saved in the alphabetical oder of their id.
	 */
	private ArrayList<Language> vector = new ArrayList<Language>();
	
	
	// CONSTRUCTOR ***********************
	// ***********************************
	
	/**
	 * Constructs a LanguageListModel which represents a list of languages 
	 * of the specified table. It will contain not only all languages of the
	 * language list, but also all languages actually used but not listed. 
	 * table may also be null, then the list will be constantly empty
	 */
	public LanguageListModel(PolyglotTableModel table) {
		this.table = table;
		if(table != null) {
			syncToTable();
			table.addChangeListener(this);
		}
	}
	
	// GETTERS AND SETTERS ***************
	// ***********************************
	
	/**
	 * Returns the associated table
	 */
	public PolyglotTableModel getTable() {
		return table;
	}
	
	/**
	 * Sets the associated table of this model. The list is
	 * completely reloaded afterwards
	 */
	public void setTable(PolyglotTableModel table) {
		//Detach from old table
		this.table.removeListener(this);
		//Attach to new one
		this.table = table;
		if(this.table != null) this.table.addChangeListener(this);
	}
	
	/**
	 * Returns the complete list as ArrayList
	 */
	public ArrayList<Language> getValues() {
		return new ArrayList<Language>(vector);
	}
	
	// LIST MODEL ************************
	// ***********************************
	
	/**
	 * Returns the language object at the specified index (or null, if the index is
	 * out of bounds)
	 */
	@Override
	public Object getElementAt(int index) {
		if(!(0 <= index && index < vector.size())) return null;
		else return vector.get(index);
	}

	@Override
	public int getSize() {
		return vector.size();
	}

	// TABLE CHANGE LISTENING *************
	// ************************************
	
	/**
	 * Synchronizes this model with its associated table. Usually there should be no need to call
	 * this method explicitly, since this will already be done by the change listening mechanism.
	 */
	public void syncToTable() {
		if(table == null) return;
		//This will store the final language list
		TreeSet<Language> llist = new TreeSet<Language>(Language.languageSorter);
		//The list of all id's in the language list
		HashSet<String> idlist = table.getLanguageList();
		//The list of all id's actually used 
		HashSet<String> usedlist = new HashSet<String>(table.getUsedLanguages());
		//Now create sorted list
		for(String lid : idlist) {
			llist.add(new Language(lid, table.getLanguageDescription(lid),false, table.getSupport(lid)));
		}
		for(String uid : usedlist) {
			//Only add languages not already added to preserve the isOnlyUsed attribute
			if(!idlist.contains(uid)) {
				llist.add(new Language(uid, null, true, table.getSupport(uid)));
			}
		}
		vector = new ArrayList<Language>(llist);
		fireContentsChanged(this, 0, vector.size()-1);
	}
	
	@Override
	public void languageListChanged(PolyglotTableModel source) {
		syncToTable();
	}

	@Override
	public void stringTableChanged(PolyglotTableModel source) {
		syncToTable();
	}

	@Override
	public void tableDescriptionChanged(PolyglotTableModel source) {
		//Ignored
	}

	@Override
	public void tableIDChanged(PolyglotTableModel source) {
		//Ignored
	}

}
