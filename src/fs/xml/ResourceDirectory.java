package fs.xml;

import org.dom4j.*;
import org.dom4j.tree.*;

import java.io.*;
import java.util.*;

/**
 * A ResourceDirectory is used to summarize several ResourceDependent objects
 * that use a common resource reference (e.g. the same directory, like
 * '/myproject/resources'). Since runtime changes in the file system would not
 * be noticed anyhow, no guarantee as to fulfillment of all requirements is made
 * by a ResourceDirectory on long term basis. However, verifications of
 * momentary status can be queried for each registered object separately and for
 * all objects at once. Write/Read permissions are not verified. <br>
 * Since ResourceDirectory implements ResourceDependent, itself can be
 * registered at a ResourceDirectory or a similar object to realise multi-level
 * grouping of resources.
 * 
 * @author Simon Hampe
 * 
 */
public class ResourceDirectory implements ResourceDependent, ResourceReference {

	/**
	 * The resource reference
	 */
	private ResourceReference pathref = null;

	/**
	 * The set of all registered ResourceDependent objects
	 */
	private HashSet<ResourceDependent> registered = new HashSet<ResourceDependent>();

	// CONSTRUCTORS *********************************

	/**
	 * Creates a resource directory with the specified base directory
	 */
	public ResourceDirectory(final String path) {
		pathref = new ResourceReference() {
			public String getFullResourcePath(ResourceDependent r, String p) {
				return path + "/" + p;
			}
		};
	}

	/**
	 * Creates a resource directory using the specified ResourceReference . A
	 * null value indicates, that the default fsframework resource reference
	 * should be used.
	 */
	public ResourceDirectory(ResourceReference r) {
		pathref = (r == null) ? FsfwDefaultReference.getDefaultReference() : r;
	}

	// REGISTRATION FUNCTIONS **************************

	/**
	 * Adds a new ResourceDependent object. Null objects and already registered
	 * objects are ignored, as well as a recursive addition
	 * (addResourceDependent(this)) The internal base directory of this object
	 * will remain unaltered.
	 */
	public void addResourceDependent(ResourceDependent o) {
		if (o != null && o != this) {
			registered.add(o);
		}
	}

	/**
	 * Removes a ResourceDependent object from the list of registered objects,
	 * if included in this list (If not, this call is ignored).
	 */
	public void removeResourceDependent(ResourceDependent o) {
		registered.remove(o);
	}

	// VERIFICATION ****************************

	/**
	 * Verifies that the requirements of all registered objects are fulfilled
	 * 
	 * @throws FileNotFoundException
	 *             - if any of the requirements are not fulfilled. A detailed
	 *             list of all missing files/directories is appended to the
	 *             exception message
	 */
	public void verify() throws FileNotFoundException {
		StringBuilder errorreport = new StringBuilder(
				"Verification error report: \n");
		boolean erroroccured = false;
		// Verification is done object-wise instead of for the global
		// resource structure, so error messages can be given in a
		// detailed manner
		for (ResourceDependent rd : registered) {
			try {
				Document structure = rd.getExpectedResourceStructure();
				verifyNode(structure.getRootElement(), null);
			} catch (FileNotFoundException fe) {
				erroroccured = true;
				errorreport.append("Verification error for object " + rd
						+ ":\n" + fe.getMessage() + "\n");
			}
		}
		if (erroroccured) {
			throw new FileNotFoundException(errorreport.toString());
		}
	}

	// INTERFACE METHODS ********************************

	/**
	 * Forwards the resource query to the Directory's resource reference, IF the
	 * object is registered. If not, null is returned.
	 */
	public String getFullResourcePath(ResourceDependent r, String path) {
		return registered.contains(r) ? pathref.getFullResourcePath(this, path)
				: null;
	}

	/**
	 * Assigns a ResourceReference to the Directory. The Directory will forward
	 * its path requests to this reference. A direct recursice assignment
	 * (assignReference(this)) is ignored and a null assignment assigns the
	 * fsframework default reference
	 */
	public void assignReference(ResourceReference r) {
		if (r != this) {
			pathref = (r == null) ? FsfwDefaultReference.getDefaultReference()
					: r;
		}
	}

	/**
	 * Will return the ResourceReference used at the moment
	 */
	public ResourceReference getResourceReference() {
		return pathref;
	}

	/**
	 * Will return the resource structure generated by the set of all registered
	 * ResourceDependent objects (nodes of the same name - i.e.: equality by
	 * case-sensitive comparison - are merged into one node). The root element
	 * will bear the generic name "CommonBaseDirectory" instead of any
	 * indication as to the base directory.
	 * 
	 * @see fs.xml.ResourceDirectory#mergeNodes(Node, Node) which is the method
	 *      being used to merge the different resource structures.
	 */
	public Document getExpectedResourceStructure() {
		DefaultDocument ret = new DefaultDocument();

		DefaultElement root = new DefaultElement("CommonBaseDirectory");
		for (ResourceDependent rd : registered) {
			root = mergeNodes(root, rd.getExpectedResourceStructure()
					.getRootElement());
		}
		ret.setRootElement(root);

		return ret;
	}

	// STATIC METHODS *********************************

	/**
	 * A static method for verification of the file structure represented by a
	 * single XML Node of this Directory relative to a given parent directory
	 * and finally obtained through a given resource reference. If the second
	 * parameter is null, the node will be assumed to be the common base
	 * directory.
	 * 
	 * @see fs.xml.ResourceDependent#getExpectedResourceStructure() for a
	 *      detailed account of how XML nodes represent a file structure
	 * @throws FileNotFoundException
	 *             - If any requirement is not fulfilled. A list of all missing
	 *             directories and files is appended.
	 */
	@SuppressWarnings("unchecked")
	// To stop java complaining about List l = node.selectNodes
	private void verifyNode(Node rd, String parentDirectory)
			throws FileNotFoundException {
		// In any case add a "/", since doubles will be ignored
		parentDirectory = parentDirectory == null ? null : parentDirectory
				+ "/";
		// If this node is not a leaf, check for existence of the
		// directory and proceed recursively, otherwise only check for existence
		if (parentDirectory != null) {
			File dir = new File(pathref.getFullResourcePath(this,
					parentDirectory + rd.getName()));
			if (!dir.exists()) {
				throw new FileNotFoundException("Directory/File "
						+ dir.getAbsolutePath() + " not found");
			}
		}
		// Set parentDirectory to the new recursive value
		parentDirectory = parentDirectory == null ? "/" : parentDirectory + "/"
				+ rd.getName() + "/";
		// Get a list of all children
		List<Node> l = null;
		try {
			l = rd.selectNodes("./*");
		} catch (ClassCastException ce) {
			throw new FileNotFoundException("Directory/File " + parentDirectory
					+ "/" + rd.getName() + " not found. Wrong XML node type.");
		}
		if (l != null && !l.isEmpty()) {
			StringBuilder errorreport = new StringBuilder("");
			boolean erroroccured = false;
			for (Object n : l) {
				try {
					verifyNode((Node) n, parentDirectory);
				} catch (FileNotFoundException fe) {
					errorreport.append(fe.getMessage() + "\n");
					erroroccured = true;
				}
			}
			if (erroroccured)
				throw new FileNotFoundException(errorreport.toString());
		}
	}

	/**
	 * Merges two Nodes into one in the following way: Both nodes are regarded
	 * as nodes of the same level. The top level node of the first one will
	 * serve as root node of the resulting node. The top-level node of the
	 * second one will be ignored. For each different sub-level node
	 * ('different' by case-sensitive name comparison) in one of the two nodes
	 * there will be a node at the same level in the resulting node tree. If
	 * there are two nodes of the same name at the same level in both input
	 * nodes, the content of the first one will prevail.<br>
	 * This procedure assumes that no two sub-nodes of the same level in one of
	 * the nodes bear the same name. It will also work however, if this is not
	 * fulfilled. In this case, all nodes of primary will be preserved, while
	 * only the last one of several equally named nodes of secondary will
	 * remain. Merging will replace only one node of several equally named nodes
	 * in primary.<br>
	 * Node order might not be preserved and node content will be ignored.
	 */
	private static DefaultElement mergeNodes(Node primary, Node secondary) {
		// If the primary node is null, use the secondary.
		// If both are null, return null
		if (primary == null) {
			primary = secondary;
			if (secondary == null)
				return null;
		}
		// Create root node
		DefaultElement result = new DefaultElement(primary.getName());
		// Copy primary nodes
		for (Object o : primary.selectNodes("./*")) {
			result.add(mergeNodes((Node) o, null));
		}
		// If the secondary is null, we're done
		if (secondary == null)
			return result;
		// If not, merge the secondary nodes
		for (Object l : secondary.selectNodes("./*")) {
			Node s = (Node) l;
			Node tomerge = result.selectSingleNode("./" + s.getName());
			// If the node does not yet exist, just add it.
			if (tomerge == null) {
				result.add(mergeNodes(s, null));
			}
			// Otherwise delete the old one and insert the
			// merged version
			else {
				result.remove(tomerge);
				result.add(mergeNodes(tomerge, s));
			}
		}
		return result;
	}

}
