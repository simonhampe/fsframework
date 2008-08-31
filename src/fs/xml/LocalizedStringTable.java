package fs.xml;

import org.dom4j.*;
import org.dom4j.tree.*;

/**
 * Implements an XML-based String table that can be used to store
 * e.g. application-related constant strings in different locales.
 * Strings are accessed via a unique String identifier and a locale 
 * identifier. The locale identifier can be retrieved from a list
 * of "supported" locales. Since this is a list maintained solely by
 * the user, no actual guarantee will be made, that a locale contained in
 * this list is actually completely supported. Its only purpose is to 
 * inform other users of potentially available locales.
 * @author Simon Hampe
 *
 */
public class LocalizedStringTable implements XMLConfigurable {

	/**
	 * The XML-Document representing the string table
	 */
	private Document xmltable;
	
	/**
	 * The name of the default locale
	 */
	private String default_locale;
	
	/* *********************************************
	 * Constructors
	 */
	
	/**
	 * This constructor will initialize an empty string table
	 * with no locales
	 */
	public LocalizedStringTable() {
		xmltable = new DefaultDocument(new DefaultElement("locstringtable"));
	}
	
	
	
	public void configure(Node n) throws XMLWriteConfigurationException {
		// TODO Auto-generated method stub

	}

	public Node getConfiguration() throws XMLReadConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isConfigured() {
		// TODO Auto-generated method stub
		return false;
	}

}
