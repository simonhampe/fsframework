package fs.xml;

/**
 * This interface can be understood as the dual interface to
 * ResourceDependent. While the ResourceDependent publishes its need
 * for certain resources, the ResourceReference can be used to provide
 * a (potentially dynamic) path reference to these resources.
 * @see fs.xml.ResourceDependent
 * @author Simon Hampe
 *
 */
public interface ResourceReference {
	
	/**
	 * Provides the full path to a resource specified by a relative path
	 * for a given ResourceDependent object (leading "/"s or "./"s should
	 * be omitted)
	 */
	public String getFullResourcePath(ResourceDependent r, String path);
}
