package fs.xml;

/**
 * Acts as a wrapper for all exceptions that might occur while
 * trying to read a configuration from an object via its
 * XMLConfigurable interface
 */

public class XMLReadConfigurationException extends Exception {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = 3847071888558216833L;

	/**
	 * Forwards to Exception(String)
	 */
	public XMLReadConfigurationException(String message) {
		super(message);
	}

}
