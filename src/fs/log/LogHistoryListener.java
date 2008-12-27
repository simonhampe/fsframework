package fs.log;

import org.apache.log4j.Level;

/**
 * This interface defines a listener which listens for
 * changes in a LogHistoryModel.
 * @author Simon Hampe
 *
 */
public interface LogHistoryListener {
	/**
	 * Indicates that a message has been appended to the
	 * history. The actual message is passed as argument
	 */
	public void messageAppended(String formattedMessage);
	
	/**
	 * Indicates that a filter criterion has been added/changed. 
	 * The level for which a flag has been set and the actual flag
	 * are passed as arguments
	 */
	public void filterChanged(Level l, boolean b);	
}
