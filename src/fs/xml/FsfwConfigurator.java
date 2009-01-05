package fs.xml;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;

/**
 * Instances of this class can be used to configure global properties of
 * fsframework, such as the default language, a resource directory, etc... It
 * can be initialized / reconfigured via an XML File via its XMLConfigurable
 * interface. The format for this file/node is not strict. The configuration
 * method will look for certain first-level nodes (as specified in
 * FsfwConfigurator.xsd) but will simply not configure any property for which
 * there is no configuring node (i.e. leave it in its current configuration).
 * 
 * @author Simon Hampe
 * 
 */

public class FsfwConfigurator implements XMLConfigurable {

	// The string ID used to identify this object as XMLConfigurable
	private String xmlID = "fsfwconfig";

	// FSFRAMEWORK PROPERTIES *************************************
	// ************************************************************

	private String globalLanguageID = null;
	private String defaultDirectory = null;

	// CONSTRUCTORS ***********************************************
	// ************************************************************

	/**
	 * Constructs an FsfwConfigurator with the specified XMLConfigurable ID
	 * which will set everything to its default configuration
	 */
	public FsfwConfigurator(String xmlID) {
		this.xmlID = xmlID;
		globalLanguageID = PolyglotStringTable.defaultLanguageID;
		defaultDirectory = ".";
	}

	/**
	 * Constructs an FsfwConfigurator with the specified XMLConfigurable ID and
	 * reads its configuration from n
	 */
	public FsfwConfigurator(String xmlID, Node n) {
		this(xmlID);
		try {
			configure(n);
		} catch (XMLWriteConfigurationException e) {
			// will not happen
		}
	}

	// GETTERS AND SETTERS *******************************
	// ***************************************************

	/**
	 * @return the globalLanguageID
	 */
	public String getGlobalLanguageID() {
		return globalLanguageID;
	}

	/**
	 * @param globalLanguageID
	 *            the globalLanguageID to set
	 */
	public void setGlobalLanguageID(String globalLanguageID) {
		this.globalLanguageID = globalLanguageID;
	}

	/**
	 * @return the defaultDirectory
	 */
	public String getDefaultDirectory() {
		return defaultDirectory;
	}

	/**
	 * @param defaultDirectory
	 *            the defaultDirectory to set
	 */
	public void setDefaultDirectory(String defaultDirectory) {
		this.defaultDirectory = defaultDirectory;
	}

	// CONFIGURATION METHODS ************************************
	// **********************************************************

	/**
	 * Configures fsframework according to the properties set in this
	 * configurator. Be aware that any reconfiguration of fsframework after the
	 * application has already started will most probably not be recognized by
	 * and components already initialized. E.g., if you change the global
	 * language, all graphical components that have been initialized before,
	 * will probably remain in the original language.
	 */
	public void applyConfiguration() {
		PolyglotStringTable.setGlobalLanguageID(globalLanguageID);
		FsfwDefaultReference.setFsfwDirectory(defaultDirectory);
	}

	/**
	 * Looks for first level nodes under n that bear certain names specified in
	 * FsfwConfigurator.xsd. For each node that is found, it will configure the
	 * appropriate property.
	 */
	@Override
	public void configure(Node n) throws XMLWriteConfigurationException {
		Node langID = n.selectSingleNode("./globalLanguageID");
		Node defDir = n.selectSingleNode("./defaultDirectory");
		if (langID != null)
			globalLanguageID = langID.getText();
		if (defDir != null)
			defaultDirectory = defDir.getText();
	}

	/**
	 * Returns an XML element containing as subnodes all fsframework property
	 * nodes with the value associated to this configurator.
	 */
	@Override
	public Element getConfiguration() throws XMLReadConfigurationException {
		DefaultElement e = new DefaultElement("propertylist");
		DefaultElement langID = new DefaultElement("globalLanguageID");
		langID.setText(globalLanguageID);
		DefaultElement defDir = new DefaultElement("defaultDirectory");
		defDir.setText(defaultDirectory);
		e.add(langID);
		e.add(defDir);
		return e;
	}

	/**
	 * Returns the string ID assigned at creation
	 */
	@Override
	public String getIdentifier() {
		return xmlID;
	}

	/**
	 * Returns true, since any configurator is always configured.
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

}
