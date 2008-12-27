package fs.polyglot.view;

import javax.swing.JFrame;

import org.dom4j.Document;

import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;

/**
 * This is the main window of Polyglot. It contains a menu and 
 * handles all tab views.
 * @author Simon Hampe
 *
 */
public class PolyglotFrame extends JFrame implements ResourceDependent {

	
	// VIEW ****************************
	// *********************************
	
	
	// CONSTRUCTOR *********************
	// *********************************
	
	// CONTROL *************************
	// *********************************

	// RESOURCEDEPENDENT METHODS ******************************
	// ********************************************************

	@Override
	public void assignReference(ResourceReference r) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Document getExpectedResourceStructure() {
		// TODO Auto-generated method stub
		return null;
	}
}
