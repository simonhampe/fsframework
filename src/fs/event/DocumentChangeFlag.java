package fs.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;

/**
 * This class can be used to register general changes in a document (for example to provide 
 * confirmation dialogs when the user tries to close an application, etc..). It implements a few basic listeners which all do the same:
 * Set an internal flag which indicates that something has been changed. It can be reset via reset() and notified manually via notifyOfChange().
 * ChangeListeners can be registered to it to react to changes in the document.
 * @author Simon Hampe
 *
 */
public class DocumentChangeFlag implements ActionListener, ChangeListener,
		DocumentListener, ListDataListener, javax.swing.event.TableModelListener {

	//A list of listeners
	private HashSet<ChangeListener> listeners = new HashSet<ChangeListener>();
	
	//The internal flag indicating whether something has changed.
	private boolean changed = false;
	
	// CONTROL METHODS ***************************************************
	// *******************************************************************
	
	/**
	 * @return Whether any changes were registered
	 */
	public boolean hasBeenChanged() { return changed;}
	
	/**
	 * Sets the internal change flag to the specified value and notifies all listeners
	 */
	public void setChangeFlag(boolean flag) {
		changed = flag;
		fireStatusChanged();
	}
	
	/**
	 * Notifies all registered listeners of a change in the status of this object.
	 */
	protected void fireStatusChanged() {
		for(ChangeListener l : listeners) l.stateChanged(new ChangeEvent(this));
	}
	
	/**
	 * Adds the given listener
	 */
	public void addChangeListener(ChangeListener l) {
		if(l != null) listeners.add(l);
	}
	
	/**
	 * Removes the given listener
	 */
	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}
	
	
	// LISTENER METHODS *****************************************************
	// **********************************************************************
	
	@Override
	public void actionPerformed(ActionEvent e) {
		setChangeFlag(true);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		setChangeFlag(true);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		setChangeFlag(true);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		setChangeFlag(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		setChangeFlag(true);
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		setChangeFlag(true);
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		setChangeFlag(true);
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		setChangeFlag(true);
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		setChangeFlag(true);
	}

}
