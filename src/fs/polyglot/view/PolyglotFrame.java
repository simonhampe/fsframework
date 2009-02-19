package fs.polyglot.view;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import fs.polyglot.model.PolyglotOptions;
import fs.polyglot.model.PolyglotTableModel;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLToolbox;
import fs.xml.XMLWriteConfigurationException;

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
	//The application options
	private PolyglotOptions options;
	
	//Resource
	private ResourceReference resource;
	private PolyglotStringLoader loader;
	private String languageID;
	private static final String sgroup = "fs.polyglot.PolyglotFrame";
	
	//Log
	private Logger logger = Logger.getLogger(PolyglotFrame.class);
	
	// VIEW ****************************
	// *********************************
	private StringTreeView stringView;
	private GroupTreeView treeView;
	private LanguageListView languageView;
	private JMenuBar menu;
	private JMenu fileMenu;
		private JMenuItem newFile;
		private JMenuItem loadFile;
		private JMenuItem saveFile;
		private JMenuItem saveFileAs;
		private JMenuItem quit;
	private JMenu optionsMenu;
		private JMenuItem optionItem;
	private JMenu helpMenu;
		private JMenuItem help;
		private JMenuItem info;
	
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
	 * @param file The table to be opened. If null, an empty table is opened
	 * @param options The application options.
	 */
	public PolyglotFrame(File file, PolyglotOptions options) {
		//Basic initialization
		super();
		setSize(getMaximumSize());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(closeListener);
		
		//Copy data-------------------------------------------------
		
		this.options = options == null? new PolyglotOptions() : options;
		resource = FsfwDefaultReference.getDefaultReference();
		loader = PolyglotStringLoader.getDefaultLoader();
		languageID = PolyglotStringTable.getGlobalLanguageID();
		
		//Load table-------------------------------------------------
		
		if(file != null) {
			try {
				currentTable = new PolyglotTableModel(XMLToolbox.loadXMLFile(file),resource);
			}
			catch(Exception xe) {
				String msg = "Couldn't open file " + file.getAbsolutePath() + ": " + 
								xe.getMessage();
				logger.error(msg);
				JOptionPane.showMessageDialog(this, msg, "Error", ERROR);
				//Instead, open an empty table:
				file = null;
			}
		}
		if(file == null) {
			currentTable = new PolyglotTableModel("","");
		}
		
		//Init GUI --------------------------------------------------
		
		//Menu
		fileMenu 	= new JMenu(loader.getString(sgroup + ".filemenu", languageID));
		fileMenu.setMnemonic(loader.getString(sgroup + ".filemnemonic", languageID).charAt(0));
			newFile		= new JMenuItem(loader.getString(sgroup + ".newfile", languageID));
			newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
			loadFile 	= new JMenuItem(loader.getString(sgroup + ".loadfile", languageID));
			loadFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_DOWN_MASK));
			saveFile	= new JMenuItem(loader.getString(sgroup + ".savefile", languageID));
			saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK));
			saveFileAs	= new JMenuItem(loader.getString(sgroup + ".savefileas", languageID));
			quit 		= new JMenuItem(loader.getString(sgroup + ".quit", languageID));
			quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,InputEvent.CTRL_DOWN_MASK));
		optionsMenu = new JMenu(loader.getString(sgroup + ".optionsMenu", languageID));
			optionItem	= new JMenuItem(loader.getString(sgroup + ".optionitem", languageID));
		helpMenu 	= new JMenu(loader.getString(sgroup + ".helpmenu", languageID));
			help	= new JMenuItem(loader.getString(sgroup + ".help", languageID));
			help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
			info 	= new JMenuItem(loader.getString(sgroup + ".info", languageID));
		
		//Views
			
			
		//Make visible
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
