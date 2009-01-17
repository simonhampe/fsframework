package fs.polyglot.model;

import java.util.HashSet;

import org.dom4j.Document;
import org.dom4j.Node;

import fs.polyglot.event.PolyglotTableModelListener;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceReference;
import fs.xml.XMLWriteConfigurationException;

/**
 * An extension of PolyglotStringTable which notifies registered change
 * listeners of all write method calls. For performance reasons the objects passed to listeners do not represent the full status of the table.
 * For example, a polyglotstring object obtained with a call of stringInserted always has an isComplete - attribute of false, since it would mean 
 * too much effort to collect this information each time a string is added. Any listener wishing to obtain this information has to do this by itself. 
 * The information passed on in listener calls is purely structural.
 * 
 * @author Simon Hampe
 * 
 */
public class PolyglotTableModel extends PolyglotStringTable {

	/**
	 * A list of registered listeners
	 */
	private HashSet<PolyglotTableModelListener> listeners = new HashSet<PolyglotTableModelListener>();
	
	/**
	 * This listener notifies all registered listeners
	 */
	private PolyglotTableModelListener wrapperListener = new PolyglotTableModelListener() {

		@Override
		public void groupChanged(PolyglotTableModel source, Group group) {
			for(PolyglotTableModelListener l : listeners) l.groupChanged(source, group);
		}

		@Override
		public void groupInserted(PolyglotTableModel source, Group group) {
			for(PolyglotTableModelListener l : listeners) l.groupInserted(source, group);
		}

		@Override
		public void groupRemoved(PolyglotTableModel source, Group group) {
			for(PolyglotTableModelListener l : listeners) l.groupRemoved(source, group);
		}

		@Override
		public void languageListChanged(PolyglotTableModel source) {
			for(PolyglotTableModelListener l : listeners) l.languageListChanged(source);
		}

		@Override
		public void stringChanged(PolyglotTableModel source,
				PolyglotString string) {
			for(PolyglotTableModelListener l : listeners) l.stringChanged(source, string);
		}

		@Override
		public void stringInserted(PolyglotTableModel source,
				PolyglotString string) {
			for(PolyglotTableModelListener l : listeners) l.stringInserted(source, string);
		}

		@Override
		public void stringRemoved(PolyglotTableModel source,
				PolyglotString string) {
			for(PolyglotTableModelListener l : listeners) l.stringRemoved(source, string);
		}

		@Override
		public void stringTableChanged(PolyglotTableModel source) {
			for(PolyglotTableModelListener l : listeners) l.stringTableChanged(source);
		}

		@Override
		public void tableDescriptionChanged(PolyglotTableModel source) {
			for(PolyglotTableModelListener l : listeners) l.tableDescriptionChanged(source);
		}

		@Override
		public void tableIDChanged(PolyglotTableModel source) {
			for(PolyglotTableModelListener l : listeners) l.tableIDChanged(source);
		}

		@Override
		public void variantChanged(PolyglotTableModel source, Variant variant) {
			for(PolyglotTableModelListener l : listeners) l.variantChanged(source, variant);
		}

		@Override
		public void variantInserted(PolyglotTableModel source, Variant variant) {
			for(PolyglotTableModelListener l : listeners) l.variantInserted(source, variant);
		}

		@Override
		public void variantRemoved(PolyglotTableModel source, Variant variant) {
			for(PolyglotTableModelListener l : listeners) l.variantRemoved(source, variant);
		}
		
	};

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
	public void addListener(PolyglotTableModelListener l) {
		if (l != null)
			listeners.add(l);
	}

	/**
	 * Removes l from the set of registered listeners (if it is in the list)
	 */
	public void removeListener(PolyglotTableModelListener l) {
		listeners.remove(l);
	}
	// OVERWRITTEN WRITE METHODS ******************************
	// ********************************************************

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#addStringID(java.lang.String)
	 */
	@Override
	public void addStringID(String stringID) {
		super.addStringID(stringID);
		wrapperListener.stringInserted(this, new PolyglotString(getGroupID(stringID),stringID, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#configure(org.dom4j.Node)
	 */
	@Override
	public void configure(Node n) throws XMLWriteConfigurationException {
		super.configure(n);
		// All properties may have been changed,
		wrapperListener.tableDescriptionChanged(this);
		wrapperListener.tableIDChanged(this);
		wrapperListener.languageListChanged(this);
		wrapperListener.stringTableChanged(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#putLanguage(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void putLanguage(String languageID, String description) {
		super.putLanguage(languageID, description);
		wrapperListener.languageListChanged(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#putString(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void putString(String stringID, String languageID, String groupID,
			String value) {
		boolean existedBefore = getUnformattedString(stringID, languageID) != null;
		super.putString(stringID, languageID, groupID, value);
		if(existedBefore) wrapperListener.stringChanged(this, new PolyglotString(groupID,stringID,false));
		else wrapperListener.stringInserted(this, new PolyglotString(groupID, stringID,false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#putString(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void putString(String stringID, String languageID, String value) {
		boolean existedBefore = getUnformattedString(stringID, languageID) != null;
		super.putString(stringID, languageID, value);
		if(existedBefore) wrapperListener.stringChanged(this, new PolyglotString(getGroupID(stringID),stringID, false));
		else wrapperListener.stringInserted(this, new PolyglotString(null, stringID,false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#removeID(java.lang.String)
	 */
	@Override
	public void removeID(String stringID) {
		String groupID = getGroupID(stringID);
		super.removeID(stringID);
		wrapperListener.stringRemoved(this, new PolyglotString(groupID, stringID,false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#removeLanguage(java.lang.String)
	 */
	@Override
	public void removeLanguage(String languageID) {
		super.removeLanguage(languageID);
		wrapperListener.languageListChanged(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#setGroupID(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void setGroupID(String stringID, String groupID) {
		String oldGroupID = getGroupID(stringID);
		super.setGroupID(stringID, groupID);
		wrapperListener.stringRemoved(this, new PolyglotString(oldGroupID,stringID,false));
		wrapperListener.stringInserted(this, new PolyglotString(groupID, stringID,false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#setLanguageDescription(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void setLanguageDescription(String languageID, String description) {
		super.setLanguageDescription(languageID, description);
		wrapperListener.languageListChanged(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#setTableDescription(java.lang.String)
	 */
	@Override
	public void setTableDescription(String desc) {
		super.setTableDescription(desc);
		wrapperListener.tableDescriptionChanged(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#setTableID(java.lang.String)
	 */
	@Override
	public void setTableID(String tableID) {
		super.setTableID(tableID);
		wrapperListener.tableIDChanged(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.xml.PolyglotStringTable#renameString(java.lang.String, java.lang.String)
	 */
	@Override
	public void renameString(String oldID, String newID) {
		String groupID = getGroupID(oldID);
		super.renameString(oldID, newID);
		wrapperListener.stringChanged(this, new PolyglotString(groupID,oldID,false));
	}
	
	

}
