package fs.log;

import org.apache.log4j.*;

import java.util.*;

/**
 * This class represents the basic functionality of the model of a log history
 * window. It is basically an org.apache.log4j.Appender and implements as well a
 * basic change listener mechanism that can be set to action via
 * notifyChangeListeners(), for example, when a new message has been appended.
 * Each class extending this class must implement the method getLogHistory(),
 * which provides an ordered list of all formatted messages that have been
 * appended together with their respective logging level, and several filter
 * control methods.
 * 
 * @author Simon Hampe
 * 
 */
public abstract class AbstractLogHistoryModel extends AppenderSkeleton
		implements LogHistoryModel {

	/**
	 * The set of registered change listeners
	 */
	private HashSet<LogHistoryListener> listeners = new HashSet<LogHistoryListener>();

	// LOG HISTORY AND FILTER METHODS ***************
	// **********************************************

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.log.LogHistoryModel#getLogHistory()
	 */
	public abstract ArrayList<String> getLogHistory();

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.log.LogHistoryModel#isLevelDisplayed(org.apache.log4j.Level)
	 */
	public abstract boolean isLevelDisplayed(Level l);

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.log.LogHistoryModel#putFilter(org.apache.log4j.Level, boolean)
	 */
	public abstract void putFilter(Level l, boolean isDisplayed);

	/*
	 * (non-Javadoc)
	 * 
	 * @see fs.log.LogHistoryModel#getFilterTable()
	 */
	public abstract HashMap<Level, Boolean> getFilterTable();

	// CHANGE LISTENER METHODS *****************
	// *****************************************

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fs.log.LogHistoryModel#addChangeListener(javax.swing.event.ChangeListener
	 * )
	 */
	public void addLogHistoryListener(LogHistoryListener l) {
		if (l != null)
			listeners.add(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fs.log.LogHistoryModel#removeChangeListener(javax.swing.event.ChangeListener
	 * )
	 */
	public void removeLogHistoryListener(LogHistoryListener l) {
		listeners.remove(l);
	}

	/**
	 * Notifies all registered listeners of an appended message
	 */
	protected void notifyMessageAppended(String formattedMessage) {
		for (LogHistoryListener l : listeners) {
			l.messageAppended(formattedMessage);
		}
	}

	/**
	 * Notifies all registered listeners of a changed filter criterion
	 */
	protected void notifyFilterChanged(Level level, boolean flag) {
		for (LogHistoryListener l : listeners) {
			l.filterChanged(level, flag);
		}
	}
}
