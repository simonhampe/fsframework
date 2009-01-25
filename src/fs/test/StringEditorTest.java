package fs.test;

import java.io.File;

import javax.swing.JDialog;

import org.dom4j.DocumentException;

import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.view.StringEditorConfigurator;
import fs.xml.XMLToolbox;
import fs.xml.XMLWriteConfigurationException;

/**
 * Tests the string editor and associated classes
 * @author Simon Hampe
 *
 */
public class StringEditorTest {

	public static void main(String[] args) {
		PolyglotTableModel model;
		try {
			model = new PolyglotTableModel(XMLToolbox.loadXMLFile(new File("language/fsfwStringTable.xml")), null);
			StringEditorConfigurator configurator = new StringEditorConfigurator(null,null,null, model);
			configurator.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			configurator.setVisible(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
