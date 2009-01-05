package fs.xml;

/**
 * Acts as a wrapper to all exceptions that might occur while trying to
 * configure an object via its XMLConfigurable interface.
 * 
 * @author Simon Hampe
 * 
 */

public class XMLWriteConfigurationException extends Exception {

	/**
	 * Compiler-generated version id
	 */
	private static final long serialVersionUID = -6210661774717910140L;

	/**
	 * Forwards to Exception(String)
	 */
	public XMLWriteConfigurationException(String message) {
		super(message);
	}

}
