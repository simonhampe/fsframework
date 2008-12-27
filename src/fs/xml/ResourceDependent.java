package fs.xml;

import org.dom4j.*;

/**
 * This interface indicates that a class, especially the implementation,
 * depends on certain file resources (usually Schema files, templates, etc..). A corruption, removal or change of
 * these resources might render that class's behavior unpredictable.<br>
 * The class is supposed to be able to give a full account of these files that
 * are all supposed to be located relative to a common base directory that might be determined dynamically, e.g. 
 * if a certain library is integrated in a personal project in some exotic 
 * subdirectory that would hence not be found under the standard library path.    
 * @author Simon Hampe
 *
 */
public interface ResourceDependent {
		
	/**
	 * Returns an XML document, whose structure reflects the file/folder dependencies this 
	 * class expects to be satisfied. The format of this document is completely arbitrary, it should be
	 * interpreted as follows:<br>
	 * - The root node corresponds to the base directory (but does not have to carry the same name)<br>
	 * - Every non-leaf node corresponds to a directory with the nodes name as directory name <br>
	 * - Every leaf node corresponds to a directory or file with the nodes name as directory/file name<br>
	 * - Text contents of nodes are generally ignored
	 * 
	 */
	public Document getExpectedResourceStructure();
	
	/**
	 * Assigns a ResourceReference to this object, that can 
	 * be used to retrieve the actual file paths for its
	 * resources
	 */
	public void assignReference(ResourceReference r);
	
	
}
