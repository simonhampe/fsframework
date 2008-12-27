package fs.log;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;


/**
 * The basic interface that should be available by every data model
 * representing a LogHistoryWindow.
 * @author Simon Hampe
 *
 */
public interface LogHistoryModel extends Appender {

	/**
	 * @return A list of all appended messages (a formatted version) , which has possibly been filtered before. 
	 * The order should represent the order in which they were
	 * appended.
	 */
	public ArrayList<String> getLogHistory();

	/**
	 * Indicates whether messages of a certain level
	 * will be contained in the list returned by getLogHistory()
	 */
	public boolean isLevelDisplayed(Level l);

	/**
	 * Sets, whether messages of a given level should be contained
	 * in the list returned by getLogHistory() or not.
	 */
	public void putFilter(Level l, boolean isDisplayed);

	/**
	 * Returns a table containing all Levels for which a flag
	 * has been specified, with their respective flags.
	 */
	public abstract HashMap<Level, Boolean> getFilterTable();

	/**
	 * Registers a listener that will be notified of 
	 * any changes in the model. (null values will be
	 * ignored)
	 */
	public void addLogHistoryListener(LogHistoryListener l);

	/**
	 * Removes a listener from the list of registered listeners
	 */
	public void removeLogHistoryListener(LogHistoryListener l);

	
}