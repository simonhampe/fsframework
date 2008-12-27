package fs.polyglot.model;

import java.util.HashSet;

import org.dom4j.Document;
import org.dom4j.Node;

import fs.polyglot.event.PolyglotTableModelListener;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceReference;
import fs.xml.XMLWriteConfigurationException;

/**
 * An extension of PolyglotStringTable which notifies registered change listeners of all
 * write method calls.
 * @author Simon Hampe
 *
 */
public class PolyglotTableModel extends PolyglotStringTable {
	
	/**
	 * A list of registered listeners 
	 */
	private HashSet<PolyglotTableModelListener> listeners = new HashSet<PolyglotTableModelListener>();
	
	// CONSTRUCTORS *******************************************
	// ********************************************************
	
	public PolyglotTableModel(String id, String description) {
		super(id, description);
	}

	public PolyglotTableModel(Document doc, ResourceReference r)
			throws XMLWriteConfigurationException {
		super(doc, r);
	}
	
	// NOTIFY METHODS *****************************************
	// ********************************************************
	
	/**
	 * Registers l as a listener (if l != null)
	 */
	public void addChangeListener(PolyglotTableModelListener l)  {
		if(l!= null) listeners.add(l);
	}
	
	/**
	 * Removes l from the set of registered listeners (if it is in the list)
	 */
	public void removeListener(PolyglotTableModelListener l) {
		listeners.remove(l);
	}
	
	/**
	 * Notifies all listeners of a table id change
	 */
	private void notifyIDChanged() {
		if(listeners == null) return;
		for(PolyglotTableModelListener l : listeners) {
			l.tableIDChanged(this);
		}
	}
	
	/**
	 * Notifies all listeners of a table description change
	 */
	private void notifyDescChanged() {
		if(listeners == null) return;
		for(PolyglotTableModelListener l : listeners) {
			l.tableDescriptionChanged(this);
		}
	}
	
	/**
	 * Notifies all listeners of a change in the language list 
	 */
	private void notifyLanguageListChanged() {
		if(listeners == null) return;
		for(PolyglotTableModelListener l : listeners ) {
			l.languageListChanged(this);
		}
	}
	
	/**
	 * Notifies all listeners of a change in the string table
	 */
	private void notifyStringTableChanged() {
		if(listeners == null) return;
		for(PolyglotTableModelListener l : listeners) {
			l.stringTableChanged(this);
		}
	}
	
	/**
	 * Notifies all listeners, that everything might have changed. In fact, all interface methods
	 * are called on after the other
	 */
	private void notifyChanged() {
		notifyIDChanged();
		notifyDescChanged();
		notifyLanguageListChanged();
		notifyStringTableChanged();
	}
	
	// OVERWRITTEN WRITE METHODS ******************************
	// ********************************************************

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#addStringID(java.lang.String)
	 */
	@Override
	public void addStringID(String stringID) {
		super.addStringID(stringID);
		notifyStringTableChanged();
	}

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#configure(org.dom4j.Node)
	 */
	@Override
	public void configure(Node n) throws XMLWriteConfigurationException {
		super.configure(n);
		//All properties may have been changed,
		notifyChanged();
	}

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#putLanguage(java.lang.String, java.lang.String)
	 */
	@Override
	public void putLanguage(String languageID, String description) {
		super.putLanguage(languageID, description);
		notifyLanguageListChanged();
	}

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#putString(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void putString(String stringID, String languageID, String groupID,
			String value) {
		super.putString(stringID, languageID, groupID, value);
		notifyStringTableChanged();
	}

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#putString(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void putString(String stringID, String languageID, String value) {
		super.putString(stringID, languageID, value);
		notifyStringTableChanged();
	}

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#removeID(java.lang.String)
	 */
	@Override
	public void removeID(String stringID) {
		super.removeID(stringID);
		notifyStringTableChanged();
	}

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#removeLanguage(java.lang.String)
	 */
	@Override
	public void removeLanguage(String languageID) {
		super.removeLanguage(languageID);
		notifyStringTableChanged();
	}

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#setGroupID(java.lang.String, java.lang.String)
	 */
	@Override
	public void setGroupID(String stringID, String groupID) {
		super.setGroupID(stringID, groupID);
		notifyStringTableChanged();
	}

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#setLanguageDescription(java.lang.String, java.lang.String)
	 */
	@Override
	public void setLanguageDescription(String languageID, String description) {
		super.setLanguageDescription(languageID, description);
		notifyLanguageListChanged();
	}

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#setTableDescription(java.lang.String)
	 */
	@Override
	public void setTableDescription(String desc) {
		super.setTableDescription(desc);
		notifyDescChanged();
	}

	/* (non-Javadoc)
	 * @see fs.xml.PolyglotStringTable#setTableID(java.lang.String)
	 */
	@Override
	public void setTableID(String tableID) {
		super.setTableID(tableID);
		notifyIDChanged();
	}
	
}
