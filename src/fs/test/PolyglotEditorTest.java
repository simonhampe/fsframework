package fs.test;

import java.util.Arrays;

import javax.swing.JDialog;

import fs.event.DataRetrievalListener;
import fs.polyglot.view.LanguageEditor;

/**
 * Tests the POLYGLOT editors
 * @author Simon Hampe
 *
 */
public class PolyglotEditorTest {

	
	public static void main(String[] args) {
		LanguageEditor editor = new LanguageEditor(Arrays.asList("de","en"), null, null, null, null);
		editor.addDataRetrievalListener(new DataRetrievalListener(){

			@Override
			public void dataReady(Object data) {
				System.out.println(data);
			}
			
		});
		editor.setVisible(true);
		editor.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

}
