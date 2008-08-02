package fs.xml;

/**
 * Acts as a wrapper to all exceptions that might occur while
 * trying to configure an object via its XMLConfigurable interface.
 * 
 * @author Simon Hampe
 *
 */

public class XMLWriteConfigurationException extends Exception {

	/**
	 * Forwards to Exception(String)
	 */
	public XMLWriteConfigurationException(String message) {
		super(message);
	}
	
}
