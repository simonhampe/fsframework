package fs.polyglot.undo;

import java.util.HashMap;

import javax.swing.undo.UndoManager;

import fs.polyglot.model.PolyglotTableModel;

/**
 * Creates undo managers for PolyglotTableModels. There is one single UndoManager for
 * each PolyglotTableModel, which is obtained by calling getUndoManager().
 * @author Simon Hampe
 *
 */
public class TableUndoManager extends UndoManager {
	
	/**
	 * Compiler-generated version id.
	 */
	private static final long serialVersionUID = 8244740398815898726L;
	/**
	 * Mapping of tables to UndoManagers
	 */
	private static HashMap<PolyglotTableModel,TableUndoManager> managerTable = new HashMap<PolyglotTableModel, TableUndoManager>();
					
	/**
	 * Overwrites constructor to make it protected
	 */
	protected TableUndoManager() {
	}
	
	/**
	 * @return Returns the UndoManager instance associated to the table (or creates one, if it doesn't exist yet).
	 */
	public static TableUndoManager getUndoManager(PolyglotTableModel table) {
		TableUndoManager manager = managerTable.get(table);
		//If it doesn't exist, create it
		if(manager == null) {
			manager = new TableUndoManager();
			managerTable.put(table, manager);
		}
		return manager;
	}
	
	
}
