package fs.xml;

/**
 * A singleton class used to retrieve file paths to resources of fsframework.
 * The user can set the fsframework base path.
 * 
 * @author Simon Hampe
 * 
 */
public class FsfwDefaultReference implements ResourceReference {

	/**
	 * The base directory of the fsframework used by the application. By default
	 * it is assumed to be the working directory
	 */
	private static String basedir = ".";

	/**
	 * The only FsfwDefaultReference
	 */
	private static FsfwDefaultReference globalReference = null;

	/**
	 * Constructs the reference with the given base directory
	 */
	private FsfwDefaultReference(String newbasedir) {
		basedir = newbasedir;
	}

	/**
	 * Returns the FsfwDefaultReference
	 */
	public static FsfwDefaultReference getDefaultReference() {
		// If it doesn't exist, it is created with the current base dir
		if (globalReference == null) {
			globalReference = new FsfwDefaultReference(basedir);
		}
		return globalReference;
	}

	/**
	 * Sets the base path of the <i>global</i> reference for fsframework. A null
	 * string is interpreted as the empty string.
	 */
	public static void setFsfwDirectory(String newDirectory) {
		basedir = newDirectory == null ? "" : newDirectory;
	}
	
	/**
	 * @return The current base directory of fsframework
	 */
	public static String getFsfwDirectory() {
		return basedir;
	}

	/**
	 * Always returns (base path) + "/" + path
	 */
	public String getFullResourcePath(ResourceDependent r, String path) {
		return basedir + "/" + path;
	}

}
