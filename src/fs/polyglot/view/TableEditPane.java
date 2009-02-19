package fs.polyglot.view;

import javax.swing.JPanel;

import org.dom4j.Document;

import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;

/**
 * Implements a three-view panel for editing polyglot string tables. The three views
 * are a string tree view, a group tree view, a language list view and in addition
 * text fields for editing table description and ID.   
 * @author Simon Hampe
 *
 */
public class TableEditPane extends JPanel implements ResourceDependent {

	// VIEW ***************************************
	// ********************************************
	
	// CONSTRUCTOR ********************************
	// ********************************************
	
	@Override
	public void assignReference(ResourceReference r) {
	}

	@Override
	public Document getExpectedResourceStructure() {
		return null;
	}

}
