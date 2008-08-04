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
 *  configuration. If there are several of this name, only the first one will be used.
 *  If there is none, the configure()-Method will simply not be called.
 * <br> As for writing, if a node for an ID does not exist yet, it is created. If it exists, the
 *  first occurrence will be overwritten and any further ones ignored. Note, 
 *  that XMLConfigureFile gives no further guarantees as to the order of the
 *  nodes. This implies further that non-unique IDs might result in 
 *  unpredictable behavior.
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
			internalXML = new DefaultDocument(new DefaultElement("configuration"));
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

	/**
	 * Tries to read a first-level node with the appropriate name (i.e. equal to the ID) for each
	 * XMLConfigurable and pass it on for configuration.
	 * @throws XMLWriteConfigurationException - If any XMLConfigurable.configure(Node n) throws this exception. However,
	 * all correctly written configurations will still be read regardless of any errors occuring. The exception thrown at the
	 * end contains a list of all errors reported as message. 
	 */
	public void configure() throws XMLWriteConfigurationException {
		//All possible configuration errors are stored in 
		//one large string and passed on afterwards all at once.
		//This is supposed to enable correctly written configurations
		//to be read regardless of incorrectly written ones, while still
		//giving a full feedback about all errors occuring
		StringBuilder error = new StringBuilder("Configuration Write error report:");
		boolean errorOccured = false;
		for(XMLConfigurable c : configurables) {
			String id = c.getIdentifier();
			Node n = internalXML.selectSingleNode("/*/" + id);
			if(n!=null) {
				try {
					c.configure(n);
				}
				catch(XMLWriteConfigurationException e) {
					errorOccured = true;
					//Append the message to the general error string
					error.append("\n * " + e.getMessage());
				}
			}
		}
		if(errorOccured) {
			throw new XMLWriteConfigurationException(error.toString());
		}
	}
	
	/**
	 * Tries to read all configurations from the registered XMLConfigurables and 
	 * stores them in appropriate nodes.
	 * @throws XMLReadConfigurationException - if any of the XMLConfigurables threw this
	 * exception. However, all correctly returned configurations will still be stored, regardless
	 * of any exceptions and all errors will be returned as one large error report in the returned
	 * exception's message
	 */
	public void readConfigurations() throws XMLReadConfigurationException{
		//All possible configuration errors are stored in 
		//one large string and passed on afterwards all at once.
		//This is supposed to enable correctly returned configurations
		//to be read regardless of any exceptions from other XMLConfigurables, while still
		//giving a full feedback about all errors occuring
		StringBuilder error = new StringBuilder("Configuration Read error report:");
		boolean errorOccured = false;
		for(XMLConfigurable c : configurables) {
			try {
				Node n = c.getConfiguration();
				//Make sure, the configuration is not empty
				if(n == null) {
					throw new XMLReadConfigurationException("The XMLConfigurable " + c + " returned a null configuration.");
				}
				else {
					Node store = internalXML.selectSingleNode("/*/" + c.getIdentifier());
					//If the node already exists, overwrite it
					if(store != null) {
						store.detach();
						internalXML.getRootElement().add(n);
					}
					else {	//Otherwise create it
						internalXML.getRootElement().add(n);
					}
				}
			}
			catch(XMLReadConfigurationException e) {
				errorOccured = true;
				//Append the error message to the general error string
				error.append("\n * " + e.getMessage());
			}
		}
		if(errorOccured) {
			throw new XMLReadConfigurationException(error.toString());
		}
	}
	
}
