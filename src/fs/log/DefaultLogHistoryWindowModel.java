package fs.log;

import java.util.*;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Provides the standard model implementation for LogHistoryWindow. Appended
 * messages are stored internally in a sorted list as a string conforming to the
 * appendes layout together with the log events level. This class also supports
 * the basic level-oriented filtering mechanism defined by AbstractLogHistory:
 * For each Level a boolean value can be configured that specifies if messages
 * of this level should be returned by getLogHistory(). By default, all
 * predefined levels from INFO upwards are displayed. Since this class is
 * derived from AppenderSkeleton, threshold filtering and general filters are
 * supported.
 * 
 * @author Simon Hampe
 * 
 */
public class DefaultLogHistoryWindowModel extends AbstractLogHistoryModel {

	/**
	 * The default pattern layout for log messages. The current value is
	 * "%-7p [%d{HH:mm:ss}] %m"
	 */
	public final static String DLHWM_PATTERN_LAYOUT = "%-7p [%d{HH:mm:ss}] %m";

	/**
	 * The internal list of messages
	 */
	private ArrayList<LevelAnnotatedMessage> loghistory = new ArrayList<LevelAnnotatedMessage>();

	/**
	 * Indicates for each Level, if messages of this level should be returned by
	 * getLogHistory. If a message has a level not included in the key set, it
	 * will be displayed.
	 */
	private HashMap<Level, Boolean> isDisplayed = new HashMap<Level, Boolean>();

	// CONSTRUCTOR ******************************
	// ******************************************

	public DefaultLogHistoryWindowModel() {
		// Initialize Level table default values
		// Everything from INFO upwards is displayed.
		for (Level l : Arrays.asList(Level.TRACE, Level.DEBUG)) {
			isDisplayed.put(l, false);
		}
		for (Level l : Arrays.asList(Level.INFO, Level.WARN, Level.ERROR,
				Level.FATAL)) {
			isDisplayed.put(l, true);
		}
		// Initialize Layout
		setLayout(new DefaultHTMLLayout());
	}

	// FILTER CONTROL METHODS *******************
	// ******************************************

	/**
	 * Controls whether messages of the specified level should be displayed. If
	 * a value has already been specified, it is replaced by the new one.
	 */
	public void putFilter(Level l, boolean isDisplayed) {
		this.isDisplayed.put(l, isDisplayed);
		notifyFilterChanged(l, isDisplayed);
	}

	/**
	 * Returns whether messages of a certain level are displayed. If no value
	 * has been specified (e.g. if l is a user-defined level that has not yet
	 * been configured), true is returned, since by default, all unconfigured
	 * levels are displayed.
	 */
	public boolean isLevelDisplayed(Level l) {
		Boolean b = isDisplayed.get(l);
		if (b == null)
			return true;
		else
			return b;
	}

	/**
	 * Returns a table containing all Levels for which a flag has been
	 * specified, with their respective flags.
	 */
	public HashMap<Level, Boolean> getFilterTable() {
		return new HashMap<Level, Boolean>(isDisplayed);
	}

	// LHWMODEL METHODS *************************
	// ******************************************

	@Override
	public ArrayList<String> getLogHistory() {
		ArrayList<String> ret = new ArrayList<String>();
		for (LevelAnnotatedMessage lam : loghistory) {
			if (isLevelDisplayed(lam.getLevel())) {
				ret.add(lam.getMessage());
			}
		}
		return ret;
	}

	// ABSTRACT APPENDER METHODS ****************
	// ******************************************

	/**
	 * @see org.apache.log4j.AppenderSkeleton#append(LoggingEvent arg0)
	 */
	@Override
	protected void append(LoggingEvent arg0) {
		String msg = getLayout().format(arg0);
		loghistory.add(new LevelAnnotatedMessage(msg, arg0.getLevel()));
		notifyMessageAppended(msg);
	}

	/**
	 * Closes the appender
	 */
	public void close() {
		closed = true;
	}

	/**
	 * This appender does not require a layout.
	 */
	public boolean requiresLayout() {
		return false;
	}

	private class DefaultHTMLLayout extends Layout {

		/**
		 * The internal pattern layouter
		 */
		private final PatternLayout internalPattern = new PatternLayout(
				DLHWM_PATTERN_LAYOUT);

		/**
		 * Formats the event to an html
		 * 
		 * < pre >
		 * - string containing the log message.
		 * Color options are added according to the Level priority. Everything up to
		 * and including INFO is black, WARN is orange and everything above is red.
		 */
		@Override
		public String format(LoggingEvent arg0) {
			String colorcode = "#000000";// BLACK
			// If > INFO (that is >= and INFO is not <=)
			if (arg0.getLevel().isGreaterOrEqual(Level.INFO)
					&& !Level.INFO.isGreaterOrEqual(arg0.getLevel())) {
				// If > WARN (that is: >= and WARN is not <=)
				if (arg0.getLevel().isGreaterOrEqual(Level.WARN)
						&& !Level.WARN.isGreaterOrEqual(arg0.getLevel())) {
					colorcode = "#FF0000"; // RED
				} else
					colorcode = "#FFCC11"; // ORANGE
			}
			return String.format("<pre> <font color=\'%s\'>%s</font></pre>",
					colorcode, internalPattern.format(arg0));
		}

		/**
		 * Returns true
		 */
		@Override
		public boolean ignoresThrowable() {
			return true;
		}

		/**
		 * Ignored
		 */
		public void activateOptions() {
		}

	}

}
