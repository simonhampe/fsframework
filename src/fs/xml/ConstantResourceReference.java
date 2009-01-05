package fs.xml;

/**
 * Convenience class that serves as a constant reference, that is, always
 * returns "base path" + "query path" for its interface method.
 * 
 * @author Simon Hampe
 * 
 */
public class ConstantResourceReference implements ResourceReference {

	// The base directory
	private String baseDirectory = "/";

	/**
	 * Constructs a resource reference that will always return "basepath" +
	 * "queried path" for every ResourceDependent object
	 */
	public ConstantResourceReference(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	/**
	 * Returns baseDirectory + "/" + path
	 */
	public String getFullResourcePath(ResourceDependent r, String path) {
		return baseDirectory + "/" + path;
	}

	/**
	 * Returns the currently used base directory
	 */
	public String getBaseDirectory() {
		return baseDirectory;
	}

	/**
	 * Sets the base directory to the specified value (null is interpreted as
	 * the empty string)
	 */
	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory == null ? "" : baseDirectory;
	}

}
