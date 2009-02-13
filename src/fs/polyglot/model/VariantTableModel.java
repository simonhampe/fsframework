package fs.polyglot.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import fs.polyglot.view.StringEditorConfiguration;
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
	//A list of languages before any user edit occurred
	private ArrayList<String> originallist = new ArrayList<String>();
	//A list of variants
	private ArrayList<String> variants = new ArrayList<String>();
	
	private HashSet<TableModelListener> listeners = new HashSet<TableModelListener>();
	
	private PolyglotStringLoader loader = null;
	private String languageID;
	private StringEditorConfiguration config;
	
	// CONSTRUCTOR **********************************************************
	// **********************************************************************
	
	public VariantTableModel(String stringID,StringEditorConfiguration config, PolyglotTableModel table, PolyglotStringLoader loader, String languageID) {
		this.loader = loader != null ? loader : PolyglotStringLoader.getDefaultLoader();
		this.languageID = languageID != null ? languageID : PolyglotStringTable.getGlobalLanguageID();
		this.config = config != null ? config : new StringEditorConfiguration();
		if(!(table == null || stringID == null)) {
			TreeSet<String> lang = new TreeSet<String>(table.getSupportedLanguages(stringID));
			languages = new ArrayList<String>();
			for(String l : lang) {
				if(this.config.excludeTheseLanguages == null || !this.config.excludeTheseLanguages.contains(l)) {
					if(this.config.excludeTheseLanguages != null || this.config.onlyTheseLanguages == null || this.config.onlyTheseLanguages.contains(l)) 
						languages.add(l);
						originallist.add(l);
						variants.add(table.getUnformattedString(stringID, l));
				}
			}
			
		}
	}
	
	// GETTER AND SETTER METHODS *********************************************
	// ***********************************************************************
	
	/**
	 * Returns a list of all language IDs in the model
	 */
	public ArrayList<String> getLanguageList() {
		return new ArrayList<String>(languages);
	}
	
	/**
	 * Returns a list of language IDs that were displayed before any edit occurred.
	 */
	public HashSet<String> getOriginalLanguageList() {
		return new HashSet<String>(originallist);
	}
	
	/**
	 * Removes the entry in the specified row, if 0 <= row < getRowCount-1 (the last row cannot be deleted)
	 */
	public void removeRow(int row) {
		if(0 <= row && row < languages.size()) {
			languages.remove(row);
			variants.remove(row);
			fireTableChanged(new TableModelEvent(this,row,row,TableModelEvent.ALL_COLUMNS,TableModelEvent.DELETE));
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
		return loader.getString("fs.polyglot.VariantTableModel" + ((arg0 == 0) ? ".language" : (arg0 == 1 ? ".variant" : "") ), languageID); 
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
		case 1: return variants.get(arg0);
		case 2: return "";
		default: return "";
		}
	}

	/**
	 * Returns true for all cells which are not in the last row and only for the language cell in the last row
	 */
	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return (arg0 < languages.size() || arg1 == 0);
	}

	@Override
	public void removeTableModelListener(TableModelListener arg0) {
		listeners.remove(arg0);

	}

	/**
	 * If the cell adressed is a cell in the first or second column, the following happens:<br>
	 * - If an existing language or variant is changed, this change will be adopted and all listeners notified<br>
	 * - If a language is added in the last row, it is added with an empty variant and all listeners are notified
	 * @throws ArrayIndexOutOfBoundsException - If the indices are out of bounds.
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		if(col > 2 || col < 0 || row < 0 || row > languages.size()) 
			throw new ArrayIndexOutOfBoundsException("Can't edit cell at (" + row + ", " + col + "). Is not editable or does not exist.");
		if(col == 2) return;
		//Language added
		if(row == languages.size() && col == 0) {
			languages.add(value.toString());
			variants.add("");
			fireTableChanged(new TableModelEvent(this, row,row,TableModelEvent.ALL_COLUMNS,TableModelEvent.UPDATE));
			fireTableChanged(new TableModelEvent(this, row+1,row+1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
		}
		//Change
		else {
			if(col == 0) {
				languages.set(row, value.toString());
			}
			else {
				variants.set(row, value.toString());
			}
			fireTableChanged(new TableModelEvent(this, row,row,col));
		}
	}
	
	
	
	/**
	 * Notifies all listeners
	 */
	protected void fireTableChanged(TableModelEvent e) {
		for(TableModelListener l : listeners) l.tableChanged(e);
	}

}
