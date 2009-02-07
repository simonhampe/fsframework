package fs.polyglot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;

/**
 * Implements a table model for diplaying / editing variants of a given string id in a given table. Variants
 * are statically loaded, i.e. not updated, if the table is changed
 * @author hampe
 *
 */
public class VariantTableModel implements TableModel {

	//A list of languages
	private ArrayList<String> languages = new ArrayList<String>();
	//A map mapping language ids to variants
	private HashMap<String, String> variants = new HashMap<String, String>();	
	
	private HashSet<TableModelListener> listeners = new HashSet<TableModelListener>();
	
	private PolyglotStringLoader loader = null;
	private String languageID;
	
	// CONSTRUCTOR **********************************************************
	// **********************************************************************
	
	public VariantTableModel(String stringID, PolyglotTableModel table, PolyglotStringLoader loader, String languageID) {
		this.loader = loader != null ? loader : PolyglotStringLoader.getDefaultLoader();
		this.languageID = languageID != null ? languageID : PolyglotStringTable.getGlobalLanguageID();
		if(!(table == null || stringID == null)) {
			languages = new ArrayList<String>(new TreeSet<String>(table.getSupportedLanguages(stringID)));
			for(String l : languages) {
				variants.put(l, table.getUnformattedString(stringID, l));
			}
			
		}
	}
	
	// INTERFACE METHODS ****************************************************
	// **********************************************************************
	
	/**
	 * Adds a listener
	 */
	@Override
	public void addTableModelListener(TableModelListener arg0) {
		listeners.add(arg0);
	}

	/**
	 * Returns String.class
	 */
	@Override
	public Class<?> getColumnClass(int arg0) {
		return String.class;
	}

	/**
	 * There are always three columns
	 */
	@Override
	public int getColumnCount() {
		return 3;
	}

	/**
	 * The first column is named 'Language', the second 'Variant', the third has no name
	 */
	@Override
	public String getColumnName(int arg0) {
		return loader.getString("fs.polyglot.VariantTableModel" + ((arg0 == 0) ? ".language" : "variant" ), languageID); 
	}

	/**
	 * There is one row more than there are languages
	 */
	@Override
	public int getRowCount() {
		return languages.size()+1;
	}

	/**
	 * Except for the last row, returns the language ID in the first column, the variant in the second row and nothing in the third column. 
	 * The last row is empty
	 */
	@Override
	public Object getValueAt(int arg0, int arg1) {
		if(arg0 == languages.size()) {
			return "";
		}
		else switch(arg1) {
		case 0: return languages.get(arg0);
		case 1: return variants.get(languages.get(arg0));
		case 2: return "";
		default: return "";
		}
	}

	/**
	 * Returns true for all language/variant cells which are not in the last row and only for the language cell in the last row
	 */
	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return (arg0 < languages.size() || arg1 == 0) && arg1 <= 1;
	}

	@Override
	public void removeTableModelListener(TableModelListener arg0) {
		listeners.remove(arg0);

	}

	/**
	 * Will set the value in any language/variant cell, but won't change any third row cells. 
	 */
	@Override
	public void setValueAt(Object arg0, int arg1, int arg2) {
		
	}

}
