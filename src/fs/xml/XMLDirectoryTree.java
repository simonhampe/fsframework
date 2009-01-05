package fs.xml;

import java.io.*;
import java.util.*;

import org.dom4j.*;
import org.dom4j.tree.*;

/**
 * Implements an extension of DefaultDocument which contains some convenient
 * methods to create an XML document representing a file/directory hierarchy,
 * such as accessing nodes via filepaths.
 * 
 * @author Simon Hampe
 * 
 */

public class XMLDirectoryTree extends DefaultDocument {

	// CONSTRUCTORS ********************************
	// *********************************************

	/**
	 * A compiler generated version ID for serialization purposes
	 */
	private static final long serialVersionUID = -2466025571666294518L;

	/**
	 * Constructs an empty DefaultDocument
	 */
	public XMLDirectoryTree() {
		super();
	}

	/**
	 * Constructs a DefaultDocument which contains a root node with the
	 * specified name
	 */
	public XMLDirectoryTree(String rootname) {
		super();
		DefaultElement root = new DefaultElement(rootname);
		setRootElement(root);
	}

	// PATH ACCESS METHODS *********************
	// *****************************************

	/**
	 * Returns a list containing elements corresponding in name to the
	 * successive directory names in the path argument. The first element
	 * corresponds to the first part, the last to the last part. Will return
	 * null for a null string.
	 */
	public static ArrayList<DefaultElement> getSuccessiveElementList(String path) {
		if (path == null)
			return null;
		// Using File to profit from parsing capabilities
		File f = new File(path);
		// Create list of elements from bottom to top
		ArrayList<DefaultElement> branch = new ArrayList<DefaultElement>();
		while (f != null) {
			// Insert at front of list
			branch.add(0, new DefaultElement(f.getName()));
			f = f.getParentFile();
		}
		return branch;
	}

	/**
	 * The argument path is interpreted as being a file path relative to the
	 * root element (if no root element is present, it will be created as being
	 * the root directory (i.e. ""). No double entries will be created, i.e. if
	 * a part of this path already exists, it will be created within that path.
	 */
	public void addPath(String path) {
		if (path == null)
			return;
		ArrayList<DefaultElement> branch = getSuccessiveElementList(path);
		// Create them in tree
		Element parent = getRootElement();
		if (parent == null) {
			DefaultElement root = new DefaultElement("");
			setRootElement(root);
			parent = root;
		}
		for (DefaultElement e : branch) {
			// check for existing nodes and
			// add correspondingly
			// If the node does not exist, create it
			Element c = parent.element(e.getName());
			if (c == null) {
				parent.add(e);
				parent = e;
			}
			// If it exists, skip it
			else {
				parent = c;
			}
		}
	}

	/**
	 * This will remove the node corresponding to the last part of the filename
	 * path and all its subnodes. If the node does not exist, this call is
	 * ignored.
	 */
	public void removePath(String path) {
		if (path == null)
			return;
		ArrayList<DefaultElement> branch = getSuccessiveElementList(path);
		// Step through the hierarchy and abort, if any node does not
		// exist
		Node n = getRootElement();
		for (DefaultElement e : branch) {
			n = n.selectSingleNode("./" + e.getName());
			if (n == null)
				return;
		}
		// Now n is the node to be removed
		n.detach();
	}

}
