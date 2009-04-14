package fs.test;

import java.io.File;

import javax.swing.JDialog;
import javax.swing.undo.UndoManager;

import fs.event.DataRetrievalListener;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.view.StringEditor;
import fs.polyglot.view.StringEditorConfiguration;
import fs.polyglot.view.StringEditorConfigurator;
import fs.xml.XMLToolbox;

/**
 * Tests the string editor and associated classes
 * @author Simon Hampe
 *
 */
public class StringEditorTest {

	public static void main(String[] args) {
		final PolyglotTableModel model;
		try {
			model = new PolyglotTableModel(XMLToolbox.loadXMLFile(new File("language/fsfwStringTable.xml")), null);
			StringEditorConfigurator configurator = new StringEditorConfigurator(null,null,null, model,null);
			configurator.addDataRetrievalListener(new DataRetrievalListener() {
			UndoManager manager = new UndoManager();
				@Override
				public void dataReady(Object source, Object data) {
					StringEditorConfiguration config = (StringEditorConfiguration) data;
					StringEditor editor = new StringEditor(null, null, null, model, null, null,config,manager);
					editor.setVisible(true);
					editor.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				}
				
			});
			configurator.setVisible(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
