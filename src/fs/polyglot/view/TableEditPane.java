package fs.polyglot.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import fs.gui.GUIToolbox;
import fs.gui.SwingAppender;
import fs.polyglot.event.PolyglotChangeFlag;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.undo.TableUndoManager;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

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
	
	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = 6397871914197118728L;
	
	private StringTreeView stringtree;
	private GroupTreeView  grouptree;
	private LanguageListView languagelist;
	private JTextField tableid;
	private JTextArea tabledesc;
	private SwingAppender logAppender;
	private JProgressBar progressBar;
	private JButton undo;
	private JButton redo;
	
	// DATA ***************************************
	// ********************************************
	
	//Table data
	private PolyglotTableModel table;
	
	//Resource
	private ResourceReference resource;
	private PolyglotStringLoader loader;
	private String languageID;
	private final static String sgroup = "fs.polyglot.TableEditPane";
	
	//Log
	private Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
	
	// EVENT HANDLING *****************************
	// ********************************************
	
	//Registers changes in the table
	private PolyglotChangeFlag flag;
	
	//Adjusts the 'undo' and 'redo' buttons
	private UndoableEditListener undoListener = new UndoableEditListener() {
		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			UndoManager mgr =TableUndoManager.getUndoManager(table); 
			undo.setEnabled(mgr.canUndo());
			undo.setToolTipText(mgr.canUndo()? mgr.getUndoPresentationName() : null);
			redo.setEnabled(mgr.canRedo());
			redo.setToolTipText(mgr.canRedo()? mgr.getRedoPresentationName() : null);
		}
	};
	
	//Undoes the last operation
	private ActionListener undoButtonListener = new ActionListener()  {
		@Override
		public void actionPerformed(ActionEvent e) {
			TableUndoManager.getUndoManager(table).undo();			
		}
	};
	
	//Redoes the last operation undone
	private ActionListener redoButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			TableUndoManager.getUndoManager(table).redo();
		}
	};
	
	//Registers chanegs to id and description
	private DocumentListener tableAttributeListener = new DocumentListener() {
		@Override
		public void changedUpdate(DocumentEvent e) {
			updateTable(e.getDocument());	
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			updateTable(e.getDocument());			
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			updateTable(e.getDocument());			
		}
		public void updateTable(Object source) {
			table.setTableID(tableid.getText());
			table.setTableDescription(tabledesc.getText());
		}
	};
	
	// CONSTRUCTOR ********************************
	// ********************************************
	
	public TableEditPane(PolyglotTableModel model, ResourceReference r, PolyglotStringLoader l, String langID) {
		//Copy data
		assignReference(r);
		languageID = langID != null? langID : PolyglotStringTable.getGlobalLanguageID();
		loader = l!= null? l : PolyglotStringLoader.getDefaultLoader();
		table = model != null? model : new PolyglotTableModel("","");
		flag = new PolyglotChangeFlag();
		table.addListener(flag);
		
		//Init GUI ----------------------------------------
		stringtree = new StringTreeView(resource, loader, languageID,table);
		grouptree = new GroupTreeView(resource, loader, languageID, table);
		languagelist = new LanguageListView(resource,loader,languageID, table);
		tableid	= new JTextField(table.getTableID());
		tabledesc = new JTextArea(table.getTableDescription());
			tabledesc.setBorder(BorderFactory.createEtchedBorder());
			tabledesc.setLineWrap(true);
			tabledesc.setRows(5);
		logAppender = new SwingAppender(null,resource,loader, languageID);
			Logger.getLogger("fs.polyglot").addAppender(logAppender.getModel());
		progressBar = PolyglotProgressBarManager.getProgressBar(table);
		undo = new JButton(loader.getString("fs.global.undo", languageID));
		redo = new JButton(loader.getString("fs.global.redo", languageID));
		
		JPanel headerPanel = new JPanel();
		JPanel editPanel = new JPanel();
		JPanel statusBar = new JPanel();
		JSplitPane horizontalPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			horizontalPane.setOneTouchExpandable(true);
		
		JPanel toolBar = new JPanel();
		
		JLabel tableidlabel = new JLabel(loader.getString(sgroup + ".idlabel", languageID));
		JLabel tabledesclabel = new JLabel(loader.getString(sgroup + ".desclabel", languageID));
				
		//Layout ------------------------------------------
		
		//Header Panel
		Box headerBox = new Box(BoxLayout.Y_AXIS);
		headerBox.setAlignmentX(Box.LEFT_ALIGNMENT);
		Box idbox = new Box(BoxLayout.X_AXIS);
			idbox.setAlignmentX(Box.LEFT_ALIGNMENT);
			idbox.add(tableidlabel);idbox.add(tableid);
			idbox.add(Box.createHorizontalGlue());
		Box descbox = new Box(BoxLayout.X_AXIS);
			descbox.setAlignmentX(Box.LEFT_ALIGNMENT);
			descbox.add(tabledesclabel);
		Box descareabox = new Box(BoxLayout.X_AXIS);
			descareabox.setAlignmentX(Box.LEFT_ALIGNMENT);
			descareabox.add(tabledesc);
		Box fillBox = new Box(BoxLayout.X_AXIS);
			fillBox.add(Box.createRigidArea(new Dimension(5,5)));
		headerBox.add(fillBox);
		headerBox.add(idbox); 
		headerBox.add(descbox);
		headerBox.add(descareabox);
		headerBox.add(Box.createVerticalGlue());
		headerPanel.setLayout(new GridLayout(1,1));
		headerPanel.add(headerBox);
		
		//Toolbar
		Box toolbarbox = new Box(BoxLayout.X_AXIS);
		toolbarbox.setAlignmentX(Box.LEFT_ALIGNMENT);
			toolbarbox.add(Box.createHorizontalGlue());
			toolbarbox.add(undo);
			toolbarbox.add(redo);
		toolBar.setLayout(new GridLayout(1,1));
		toolBar.add(toolbarbox);
		
		//Edit panel
		GridBagLayout gbl = new GridBagLayout();
		editPanel.setLayout(gbl);
		GridBagConstraints gcString = GUIToolbox.buildConstraints(0, 0, 1, 2);
			gcString.weightx = 100;gcString.weighty = 100;
		GridBagConstraints gcLang = GUIToolbox.buildConstraints(1, 0, 1, 1);
			gcLang.weighty = 50;
		GridBagConstraints gcGroup = GUIToolbox.buildConstraints(1, 1, 1, 1);
			gcGroup.weighty = 50;
		gbl.setConstraints(stringtree, gcString);
		gbl.setConstraints(languagelist, gcLang);
		gbl.setConstraints(grouptree, gcGroup);
		editPanel.add(stringtree);
		editPanel.add(languagelist);
		editPanel.add(grouptree);
		
		//Split panel
		horizontalPane.setLeftComponent(headerBox);
		horizontalPane.setRightComponent(editPanel);
		
		//Status bar
		statusBar.setLayout(new BorderLayout());
		statusBar.add(logAppender,BorderLayout.WEST);
		statusBar.add(progressBar,BorderLayout.EAST);
		
		//Content pane
		GridBagLayout gbl2 = new GridBagLayout();
		setLayout(gbl2);
		GridBagConstraints gcTool = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints gcSplit = GUIToolbox.buildConstraints(0, 1, 1, 1);
			gcSplit.weightx = 100;gcSplit.weighty = 100;
		GridBagConstraints gcStatus = GUIToolbox.buildConstraints(0, 2, 1, 1);
			gcStatus.weightx = 100;
		gbl2.setConstraints(toolBar, gcTool);
		gbl2.setConstraints(horizontalPane, gcSplit);
		gbl2.setConstraints(statusBar, gcStatus);
		add(toolBar);
		add(horizontalPane);
		add(statusBar);
		
		//Init Eventhandling ------------------------------
		
		//Register for undo/redo changes
		TableUndoManager.getUndoManager(table).addUndoableEditListener(undoListener);
		undoListener.undoableEditHappened(null);
		undo.addActionListener(undoButtonListener);
		redo.addActionListener(redoButtonListener);
		tableid.getDocument().addDocumentListener(tableAttributeListener);
		tabledesc.getDocument().addDocumentListener(tableAttributeListener);
		
		//Reset change flag
		flag.setChangeFlag(false);
		
		logger.info(loader.getString("fs.polyglot.log.interfaceinit", languageID));
		logger.info(model == null? loader.getString("fs.polyglot.log.newtable", languageID) : 
									loader.getString("fs.polyglot.log.opentable", languageID, table.getTableID()));
	}
	
	// GETTERS AND SETTERS ************************
	// ********************************************
	
	/**
	 * Returns the table edited by this pane
	 */
	public PolyglotTableModel getTable() {
		return table;
	}
	
	// EVENT HANDLING METHODS *********************
	// ********************************************
	
	/**
	 * Listeners are notified of changes in the document and the document change flag
	 * is passed on as source
	 */
	public void addChangeListener(ChangeListener c) {
		if(c != null) flag.addChangeListener(c);
	}
	
	/**
	 * Removes listener from the document change flag
	 */
	public void removeChangeListener(ChangeListener c) {
		flag.removeChangeListener(c);
	}
	
	/**
	 * Returns whether the document has been changed
	 */
	public boolean hasBeenChanged() {
		return flag.hasBeenChanged();
	}
	
	/**
	 * Manually sets the change flag of this document
	 */
	public void setChangeFlag(boolean f) {
		flag.setChangeFlag(f);
	}
	
	// RESOURCEDEPENDENT **************************
	// ********************************************
	
	/**
	 * Assigns a new resource reference. If r == null, the default reference is used.
	 * Since the resource references for contained components are not updated, it doesn't
	 * make much sense to call this method after initialization.
	 */
	@Override
	public void assignReference(ResourceReference r) {
		resource = r!= null? r : FsfwDefaultReference.getDefaultReference();
	}

	/**
	 * Returns a document containing the resource needs of all contained components.
	 */
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addTree((XMLDirectoryTree) stringtree.getExpectedResourceStructure());
		tree.addTree((XMLDirectoryTree) grouptree.getExpectedResourceStructure());
		tree.addTree((XMLDirectoryTree) languagelist.getExpectedResourceStructure());		
		return tree;
	}

}
