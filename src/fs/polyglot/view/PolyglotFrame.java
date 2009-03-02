package fs.polyglot.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.tree.DefaultDocument;

import fs.polyglot.model.PolyglotOptions;
import fs.polyglot.model.PolyglotTableModel;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;
import fs.xml.XMLReadConfigurationException;
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
	private TableEditPane editPane;
	//The associated file
	private File associatedFile;
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
	
	//Opens a confirm dialog, if necessary and closes the application
	private WindowListener closeListener = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			if(editPane.hasBeenChanged()) {
				int ans = openSaveConfirmDialog();
				switch(ans) {
				case JOptionPane.YES_OPTION: 
					try {
						saveTable();
					} catch (IOException e1) {
						//Abort
						return;
					} break;
				case JOptionPane.CANCEL_OPTION: return;
				}
			}
			logger.info(loader.getString("fs.polyglot.log.closing", languageID));
			System.exit(0);
		}
	};
	
	//Listens to changes in the document and adjusts the frame title
	private ChangeListener tableChangeListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			if(editPane != null) {	
				String tableID = editPane.getTable().getTableID();
				String file = associatedFile == null? "" : ": " + associatedFile.getName();
				String title = "Polyglot - " + 
				(tableID.trim().equals("")? "..." : tableID) + 
				file +
				(editPane.hasBeenChanged()? "*" : "");
				setTitle(title);
			}
		}
	};
	
	//Action listeners for menu actions-------------------------------
	
	private ActionListener newListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(editPane.hasBeenChanged()) {
				switch(openSaveConfirmDialog()) {
				case JOptionPane.CANCEL_OPTION: return;
				case JOptionPane.YES_OPTION: 
					try {
						saveTable();
					} catch (IOException e1) { //Ignored
					}
				}
			}
			setTable(null);
		}
	};
	
	private ActionListener openListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				openFile();
			} catch (DocumentException e1) { //Ignored
			}
		}
	};
	
	private ActionListener saveListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				saveTable();
			} catch (IOException e1) { //Ignored
			}
		}
	};
	
	private ActionListener saveAsListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				saveAs();
			} catch (IOException e1) { //Ignored
			}
		}
	};
	
	private ActionListener quitListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			closeListener.windowClosing(null);
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
		
		try {
			if(file != null) loadTable(file);
			else setTable(null);
		} catch (DocumentException e) {
			//If it doesn't work, open an empty table
			file = null;
			setTable(null);
		}
		associatedFile = file;
		
		//Init GUI --------------------------------------------------
		
		//Menu
		menu = new JMenuBar();		
		fileMenu 	= new JMenu(loader.getString(sgroup + ".filemenu", languageID));
		String filemnemonic = loader.getString(sgroup + ".filemnemonic", languageID);
		if(filemnemonic.length() > 0) fileMenu.setMnemonic(filemnemonic.charAt(0));
			newFile		= new JMenuItem(loader.getString(sgroup + ".newfile", languageID));
			newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
			loadFile 	= new JMenuItem(loader.getString(sgroup + ".loadfile", languageID));
			loadFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,InputEvent.CTRL_DOWN_MASK));
			saveFile	= new JMenuItem(loader.getString(sgroup + ".savefile", languageID));
			saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK));
			saveFileAs	= new JMenuItem(loader.getString(sgroup + ".savefileas", languageID));
			quit 		= new JMenuItem(loader.getString(sgroup + ".quit", languageID));
			quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,InputEvent.CTRL_DOWN_MASK));
			for(JMenuItem i : Arrays.asList(newFile,loadFile,saveFile,saveFileAs,quit)) fileMenu.add(i);
		optionsMenu = new JMenu(loader.getString(sgroup + ".optionsMenu", languageID));
		String optionmnenmonic = loader.getString(sgroup + ".optionsmnemonic", languageID); 
		if(optionmnenmonic.length() > 0) optionsMenu.setMnemonic(optionmnenmonic.charAt(0));
			optionItem	= new JMenuItem(loader.getString(sgroup + ".optionitem", languageID));
			optionsMenu.add(optionItem);
		helpMenu 	= new JMenu(loader.getString(sgroup + ".helpmenu", languageID));
		String helpmnemonic =loader.getString(sgroup + ".helpmnemonic", languageID); 
		if(helpmnemonic.length() > 0) helpMenu.setMnemonic(helpmnemonic.charAt(0));
			help	= new JMenuItem(loader.getString(sgroup + ".help", languageID));
			help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
			info 	= new JMenuItem(loader.getString(sgroup + ".info", languageID));
			for(JMenuItem i : Arrays.asList(help,info)) helpMenu.add(i);
		for(JMenu m : Arrays.asList(fileMenu, optionsMenu, helpMenu)) menu.add(m);
		setJMenuBar(menu);
		
		pack();
			
		// Event handling -------------------------------------------
		newFile.addActionListener(newListener);
		loadFile.addActionListener(openListener);
		saveFile.addActionListener(saveListener);
		saveFileAs.addActionListener(saveAsListener);
		quit.addActionListener(quitListener);
		
		//Make visible
		setVisible(true);
	}
	
	// FILE CONTROL ********************
	// *********************************

	/**
	 * Opens a file chooser and tries to load the selected file
	 * @throws DocumentException - If the selected file cannot be opened
	 */
	protected void openFile() throws DocumentException {
		//Ask if the user is sure, if the document has been modified
		if(editPane.hasBeenChanged()) {
			int ans = openSaveConfirmDialog();
			switch(ans) {
			case JOptionPane.CANCEL_OPTION: return;
			case JOptionPane.YES_OPTION: 
				try {
					saveTable();
				} catch (IOException e) {
					//Abort, if anything goes wrong
					return;
				}
			}
		}
		//Open dialog
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(XMLToolbox.xmlFilter);
		int ret = chooser.showOpenDialog(this);
		if(ret == JFileChooser.APPROVE_OPTION) {
			loadTable(chooser.getSelectedFile());
		}
	}
	
	/**
	 * Tries to load the table represented by the file f 
	 * @throws DocumentException - If the document cannot be loaded
	 */
	protected void loadTable(File f) throws DocumentException{
		try {
			logger.info(loader.getString("fs.polyglot.log.loadingfile", languageID, f.getAbsolutePath()));
			PolyglotTableModel newmodel = new PolyglotTableModel(XMLToolbox.loadXMLFile(f),resource);
			associatedFile = f;
			setTable(newmodel);
			logger.info(loader.getString("fs.polyglot.log.loadedfile", languageID, f.getAbsolutePath()));
		}
		catch(Exception e) {
			String msg = loader.getString("fs.error.openfilefailed", languageID, f!= null? f.getAbsolutePath() : "", e.getMessage());
			logger.error(msg);
			JOptionPane.showMessageDialog(this, msg, loader.getString("fs.global.error", languageID), JOptionPane.ERROR_MESSAGE);
			throw new DocumentException(e);
		}
	}
	
	/**
	 * Constructs a new edit pane for the model m and inserts it into the frame. 
	 */
	protected void setTable(PolyglotTableModel m) {
		if(editPane != null) editPane.removeChangeListener(tableChangeListener);
		editPane= new TableEditPane(m, resource,loader,languageID);
		setContentPane(editPane);
		pack();
		editPane.addChangeListener(tableChangeListener);
		tableChangeListener.stateChanged(null);
		repaint();
	}
	
	/**
	 * Opens a dialog showing 'The document has been modified. Do you want to save it?'
	 * with a YES/NO/CANCEL option
	 */
	protected int openSaveConfirmDialog() {
		return JOptionPane.showConfirmDialog(this, 
				loader.getString("fs.global.saveordiscard",languageID),
				loader.getString("fs.global.question", languageID),
				JOptionPane.YES_NO_CANCEL_OPTION);
	}
	
	/**
	 * Saves the table under the associated file. If the file is null, a save as... 
	 * dialog is opened
	 * @throws IOException - If any I/O-errors occur
	 */
	protected void saveTable() throws IOException {
		if(associatedFile == null)	saveAs();
		else {
			Document tableDoc = new DefaultDocument();
			try {
				logger.info(loader.getString("fs.polyglot.log.savingfile", languageID, associatedFile.getAbsolutePath()));
				tableDoc.setRootElement(editPane.getTable().getConfiguration());
				XMLToolbox.saveXML(tableDoc, associatedFile.getAbsolutePath());
				logger.info(loader.getString("fs.polyglot.log.savedfile", languageID, associatedFile.getAbsolutePath()));
			} catch (XMLReadConfigurationException e) {
				String msg = loader.getString("fs.polyglot.error.readconfig", languageID, e.getMessage());
				JOptionPane.showMessageDialog(this, msg,loader.getString("fs.global.error", languageID),JOptionPane.ERROR_MESSAGE);
				logger.error(msg);
			} catch (IOException e) {
				String msg = loader.getString("fs.error.savefailed", languageID, associatedFile.getAbsolutePath(),e.getMessage());
				JOptionPane.showMessageDialog(this, msg,loader.getString("fs.global.error", languageID),JOptionPane.ERROR_MESSAGE);
				logger.error(msg);
			}
			
		}
	}
	
	/**
	 * Opens a 'Save As...'- Dialog, sets the associatedFile and calls saveTable
	 * @throws IOException - If any I/O-errors occur
	 */
	protected void saveAs() throws IOException {
		JFileChooser chooser = new JFileChooser(associatedFile != null? associatedFile.getPath() : ".");
		chooser.setFileFilter(XMLToolbox.xmlFilter);
		int ans = chooser.showSaveDialog(this);
		if(ans == JFileChooser.APPROVE_OPTION) {
			//Ask for confirmation before overwriting
			if(chooser.getSelectedFile().exists()) {
				int res = JOptionPane.showConfirmDialog(this, loader.getString("fs.global.confirmoverwrite", languageID,chooser.getSelectedFile().getAbsolutePath()),
									loader.getString("fs.global.confirmtitle", languageID),JOptionPane.YES_NO_CANCEL_OPTION);
				switch(res) {
				case JOptionPane.CANCEL_OPTION: 
				case JOptionPane.NO_OPTION:
					return; //Abort
				}
			}
			//Now save
			associatedFile = chooser.getSelectedFile();
			saveTable();
		}
	}
	
	// RESOURCEDEPENDENT METHODS ******************************
	// ********************************************************

	@Override
	public void assignReference(ResourceReference r) {
		throw new UnsupportedOperationException(
			"Can't assign a resource reference to polyglot. The fsframework reference has to be used");
	}

	@Override
	public Document getExpectedResourceStructure() {
		if(editPane != null) 
			return editPane.getExpectedResourceStructure();
		else return new XMLDirectoryTree();
	}
}
