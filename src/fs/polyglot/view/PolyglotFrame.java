package fs.polyglot.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;

import org.dom4j.Document;

import fs.polyglot.model.PolyglotOptions;
import fs.polyglot.model.PolyglotTableModel;
import fs.xml.PolyglotStringLoader;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;

/**
 * This is the main window of Polyglot. It contains a menu and the main views
 * 
 * @author Simon Hampe
 * 
 */
public class PolyglotFrame extends JFrame implements ResourceDependent {

	// DATA ****************************
	// *********************************
	
	//The table currently edited
	private PolyglotTableModel currentTable;
	//The associated file
	private File tableFile;
	
	// VIEW ****************************
	// *********************************
	private StringTreeView stringView;
	private GroupTreeView treeView;
	private LanguageListView languageView;
	private JMenu menu;
	
	// EVENT HANDLING *****************************
	// ********************************************
	
	private WindowListener closeListener = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			super.windowClosing(e);
			System.exit(0);
		}
	};
	
	// CONSTRUCTOR *********************
	// *********************************

	/**
	 * Constructs a main frame for POLYGLOT
	 * @param r The resource reference to be used. If null, the default reference is used
	 * @param loader The string loader for accessing the string table. If null, the default loader is used
	 * @param languageID The languageID of the language in which everythings should be displayed. If null, the global language id is used
	 * @param options The application options.
	 */
	public PolyglotFrame(ResourceReference r, PolyglotStringLoader loader, String languageID, PolyglotOptions options) {
		super();
		setSize(getMaximumSize());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(closeListener);
		
		//Load file
		
		
		
		setVisible(true);
	}
	
	// FILE CONTROL ********************
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
