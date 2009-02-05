package fs.polyglot.model;

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
	private TreeSet<String> languages = new TreeSet<String>();
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
			languages = new TreeSet<String>(table.getSupportedLanguages(stringID));
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
	 * There are always two columns
	 */
	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * The first column is named 'Language', the second 'Variant'
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
	 * Except for the last row returns the language ID in the first column, the variant in the second row. The last row is empty
	 */
	@Override
	public Object getValueAt(int arg0, int arg1) {
		if(arg0 == languages.size())
			//TODO: Argharghargh this doesn't work...
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setValueAt(Object arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
