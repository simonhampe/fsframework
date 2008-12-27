package fs.log;

import org.apache.log4j.Level;

/**
 * This is class wraps a tupel of a string (that represents a
 * formatted log message) and an org.apache.log4j.Level. An
 * instance of this class is immutable.
 * @author Simon Hampe
 *
 */
public class LevelAnnotatedMessage {
	private final String message;
	private final Level level;
	
	public LevelAnnotatedMessage(String message, Level level) {
		this.message = message;
		this.level = level;
	}

	/**
	 * @return the level
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
		
}
