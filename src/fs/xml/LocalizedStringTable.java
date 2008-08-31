package fs.xml;

import org.dom4j.*;
import org.dom4j.tree.*;

import java.io.*;

import java.util.*;

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
	 * with no locales and with the specified description string
	 */
	public LocalizedStringTable(String name) {
		/*
		 * Initializdoce standard document
		 */
		//First try loading the template
		try {
			xmltable = XMLToolbox.createXMLFromTemplate("templates/tmpl_LocalizedStringTable.xml");
		}
		catch(DocumentException de) {
			//Generate it manually (this might not be up to date with  the
			//current format specification)
			DefaultElement root = new DefaultElement("fsframework:locstringtable");
			root.addAttribute("xmlns:fsframework", "fsframework");
			root.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
			root.addAttribute("xsi:schemaLocation","fsframework ../schema/LocalizedStringTable.xsd " );
			
			root.addElement("description");
			root.addElement("localelist");
			root.addElement("stringtables");
			xmltable = new DefaultDocument();
			xmltable.setRootElement(root);
		}
		
		//Set Name
		xmltable.selectSingleNode("/*/description").setText(name);
	}
	
	/* ***********************************************
	 * 
	 */
	
	/**
	 * This method expects a valid LocalizedStringTable XML Document, conforming to the
	 *  Schema definition of the same name. <br>
	 * The current string table will NOT be replaced by the one passed as argument, but
	 * extended. Additional locales and strings will simply be added, in case of duplicates the 
	 * old one will be overwritten by the new one. The old description will be preserved.
	 * This allows an existing string table to be extended e.g. by an additional locale or to be 
	 * updated 
	 */
	public void configure(Node n) throws XMLWriteConfigurationException {
		//Check out base listing nodes
		Node loclist = n.selectSingleNode("/*/localelist");
		Node srglist = n.selectSingleNode("/*/stringtables");
		if(loclist == null) throw new XMLWriteConfigurationException("Update failed: Invalid LocalizedStringTable. Locale list missing");
		if(srglist == null) throw new XMLWriteConfigurationException("Update failed: Invalid LocalizedStringTable. String list missing");
		//Check out locale descriptions
		List loc = loclist.selectNodes("/locale");
		for(Object o : loc) {
			String
		}
		
	}

	public Node getConfiguration() throws XMLReadConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getIdentifier() {
		return "";
	}

	public boolean isConfigured() {
		// TODO Auto-generated method stub
		return false;
	}

}
