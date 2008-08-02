package fs.xml;

import java.io.*;
import java.util.*;

import org.dom4j.*;
import org.dom4j.io.*;
import org.dom4j.tree.*;

/**
 * Represents a file that is used to store, read and apply
 *  configurations in XML formats. Several XMLConfigurable objects
 *  can be registered at one instance of a XMLConfigureFile. The XML file will
 *  be interpreted as follows: For each registered XML-Configurable a first-level node
 *  that bears the name of the corresponding ID will be selected and passed on for
 *  configuration. If there are several of this name, only the first one will be passed on.
 *  If there is none, the configure()-Method will simply not be called.
 * <br> As for writing, if a node for an ID does not exist yet, it is created. If it exists, the
 *  first occurrence will be overwritten and any further ones ignored. Note, 
 *  that XMLConfigureFileWrite gives no further guarantees as to the order of the
 *  nodes.
 *  
 * @author Simon Hampe
 *
 */
public class XMLConfigureFile {
	/**
	 * The file to which the instance is bound, i.e. stored to
	 * and read from
	 */
	private File internalFile;
	
	/**
	 * The internal XML representation of the current configuration
	 */
	private Document internalXML;
	
	/**
	 * A list of XMLConfigurable objects which are to be configured and/or 
	 * stored via this instance
	 */
	private HashSet<XMLConfigurable> configurables = new HashSet<XMLConfigurable>();
	
	//Constructors ----------------------------------
	
	/**
	 * Initializes a configuration file bound to the given
	 * path. If the file associated with this pathname exists,
	 * it is read and XML parsing is attempted. Otherwise it is
	 * created and a standard XML file with only a root node is
	 *  stored within
	 * @throws IOException - If an I/O-error occured
	 * @throws DocumentException - If an error occured during parsing
	 */
	public XMLConfigureFile(String pathname) throws IOException, DocumentException {
		internalFile = new File(pathname);
		if(internalFile.exists()) {
			SAXReader reader = new SAXReader();
			internalXML = reader.read(internalFile);
		}
		else {
			//Create default document
			internalXML = new DefaultDocument("configuration");
			//Attempt write
			store();				
		}
	}
	
	
	//Read-Write operations -----------------------------
	
	/**
	 * Attempts to store the current configuration in the file
	 * this instance is bound to
	 * @throws IOException - If an I/O-error occured
	 */
	public void store() throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(internalFile),format);
		writer.write(internalXML);
		writer.close();
	}

	//Register operations
	
	/**
	 * Registers an XMLConfigurable to be configured and read by
	 * this file
	 */
	public void registerConfigurable(XMLConfigurable c) {
		if(c != null) {
			configurables.add(c);
		}
	}
	
	/**
	 * Unregisters an XMLConfigurable, so it is no longer updated and read
	 * by this file
	 * @return true - if c had been registered before and false otherwise
	 */
	public boolean unregisterConfigurable(XMLConfigurable c) {
		return configurables.remove(c);
	}
	
	//Sync operations

	public void configure() {
		for(XMLConfigurable c : configurables) {
			String id = c.getIdentifier();
			
		}
	}
}
