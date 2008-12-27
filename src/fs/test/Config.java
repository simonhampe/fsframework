package fs.test;

import org.dom4j.*;
import org.dom4j.tree.*;

import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * This is a test class for XMlConfigurable
 * @author Simon Hampe
 *
 */

public class Config implements XMLConfigurable {
	private String text;
	private int id;
	
	public Config(String t, int id) {
		text = new String(t);
		this.id = id;
	}
	
	public void setText(String t) {
		text = new String(t);
	}
	
	public boolean isConfigured() {
		return true;
	}

	public void configure(Node n) throws XMLWriteConfigurationException {
		Node k = n.selectSingleNode("./meintext");
		if(k == null) throw new XMLWriteConfigurationException(
				"No <meintext> tag defined. Illegal configuration format");
		else {
			setText(k.getText());
		}
	}

	public Element getConfiguration() throws XMLReadConfigurationException {
		DefaultElement n = new DefaultElement("config" + id);
		DefaultElement tn = new DefaultElement("meintext");
		tn.setText(text);
		n.add(tn);
		return n;
	}

	public String getIdentifier() {
		return "config"+id;
	}
	
	public String toString() {
		return id + ": " + text;
	}
	

}
