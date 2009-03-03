package fs.polyglot.undo;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import fs.polyglot.model.PolyglotTableModel;

/**
 * Creates undo managers for PolyglotTableModels. There is one single
 * UndoManager for each PolyglotTableModel, which is obtained by calling
 * getUndoManager().
 * 
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
	private static HashMap<PolyglotTableModel, TableUndoManager> managerTable = new HashMap<PolyglotTableModel, TableUndoManager>();
	
	private HashSet<UndoableEditListener> listeners = new HashSet<UndoableEditListener>();
	
	
	/**
	 * Overwrites constructor to make it protected
	 */
	protected TableUndoManager() {
	}

	/**
	 * @return Returns the UndoManager instance associated to the table (or
	 *         creates one, if it doesn't exist yet).
	 */
	public static TableUndoManager getUndoManager(PolyglotTableModel table) {
		TableUndoManager manager = managerTable.get(table);
		// If it doesn't exist, create it
		if (manager == null) {
			manager = new TableUndoManager();
			managerTable.put(table, manager);
		}
		return manager;
	}

	
	
	@Override
	public synchronized boolean addEdit(UndoableEdit anEdit) {
		boolean r = super.addEdit(anEdit);
		fireUndoableEditHappened(new UndoableEditEvent(this,anEdit));
		return r;
	}

	@Override
	public synchronized void discardAllEdits() {
		super.discardAllEdits();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	public synchronized void end() {
		super.end();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	public synchronized void redo() throws CannotRedoException {
		super.redo();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	protected void redoTo(UndoableEdit edit) throws CannotRedoException {
		super.redoTo(edit);
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	public synchronized void setLimit(int l) {
		super.setLimit(l);
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	protected void trimEdits(int from, int to) {
		super.trimEdits(from, to);
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	protected void trimForLimit() {
		super.trimForLimit();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	public synchronized void undo() throws CannotUndoException {
		super.undo();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	public synchronized void undoOrRedo() throws CannotRedoException,
			CannotUndoException {
		super.undoOrRedo();
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	@Override
	protected void undoTo(UndoableEdit edit) throws CannotUndoException {
		super.undoTo(edit);
		fireUndoableEditHappened(new UndoableEditEvent(this,null));
	}

	// LISTENER METHODS *****************************************************
	
	protected void fireUndoableEditHappened(UndoableEditEvent e) {
		for(UndoableEditListener l : listeners) l.undoableEditHappened(e);
	}
	
	public void addUndoableEditListener(UndoableEditListener l) {
		if(l != null) listeners.add(l);
	}
	
	public void removeUndoableEditListener(UndoableEditListener l) {
		listeners.remove(l);
	}
	
	

}
