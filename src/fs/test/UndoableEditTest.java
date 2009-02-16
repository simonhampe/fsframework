package fs.test;

import java.io.File;

import javax.swing.undo.UndoManager;

import org.dom4j.DocumentException;

import fs.polyglot.model.PolyglotString;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.undo.UndoableEditFactory;
import fs.xml.XMLToolbox;
import fs.xml.XMLWriteConfigurationException;

/**
 * Tests all the undoable edits I created for Polyglot
 * @author Simon Hampe
 *
 */
public class UndoableEditTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PolyglotTableModel table = new PolyglotTableModel(XMLToolbox.loadXMLFile(new File("language/fsfwStringTable.xml")),null);
			UndoManager manager = new UndoManager();
			UndoableEditFactory factory = new UndoableEditFactory(table,null,null,manager);
			factory.performUndoablePolyglotStringEdit(new PolyglotString("fs.global","fs.global.cancel",false), 
					new PolyglotString("fs.global","fs.global.canceld",false));
			
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
