package fs.polyglot.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.dom4j.Document;

import fs.event.DocumentChangeFlag;
import fs.gui.GUIToolbox;
import fs.gui.SwingAppender;
import fs.polyglot.model.PolyglotTableModel;
import fs.test.XMLDirectoryTest;
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
	
	private StringTreeView stringtree;
	private GroupTreeView  grouptree;
	private LanguageListView languagelist;
	private JTextField tableid;
	private JTextArea tabledesc;
	private SwingAppender logAppender;
	private JProgressBar progressBar;
	
	// DATA ***************************************
	// ********************************************
	
	//Table data
	private PolyglotTableModel table;
	private DocumentChangeFlag flag;
	
	//Resource
	private ResourceReference resource;
	private PolyglotStringLoader loader;
	private String languageID;
	private final static String sgroup = "fs.polyglot.TableEditPane";
	
	// CONSTRUCTOR ********************************
	// ********************************************
	
	public TableEditPane(PolyglotTableModel model, ResourceReference r, PolyglotStringLoader l, String langID) {
		//Copy data
		assignReference(r);
		languageID = langID != null? langID : PolyglotStringTable.getGlobalLanguageID();
		loader = l!= null? l : PolyglotStringLoader.getDefaultLoader();
		table = model != null? model : new PolyglotTableModel("","");
		flag = new DocumentChangeFlag();
		
		//Init GUI ----------------------------------------
		stringtree = new StringTreeView(resource, loader, languageID,table);
		grouptree = new GroupTreeView(resource, loader, languageID, table);
		languagelist = new LanguageListView(resource,loader,languageID, table);
		tableid	= new JTextField(table.getTableID());
		tabledesc = new JTextArea(table.getTableDescription());
		logAppender = new SwingAppender(null,resource,loader, languageID);
		progressBar = new JProgressBar();
		
		JPanel headerPanel = new JPanel();
		JPanel editPanel = new JPanel();
		JPanel statusBar = new JPanel();
		JSplitPane horizontalPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,headerPanel,editPanel);
		
		JLabel tableidlabel = new JLabel(loader.getString(sgroup + ".idlabel", languageID));
		JLabel tabledesclabel = new JLabel(loader.getString(sgroup + ".desclabel", languageID));
				
		//Layout ------------------------------------------
		
		//Header Panel
		Box headerBox = new Box(BoxLayout.Y_AXIS);
		Box idbox = new Box(BoxLayout.X_AXIS);
			idbox.setAlignmentX(Box.LEFT_ALIGNMENT);
			idbox.add(tableidlabel);idbox.add(tableid);
		Box descbox = new Box(BoxLayout.X_AXIS);
			descbox.setAlignmentX(Box.LEFT_ALIGNMENT);
			descbox.add(tabledesclabel);descbox.add(tabledesc);
		headerPanel.add(headerBox);
		
		//Edit panel
		GridBagLayout gbl = new GridBagLayout();
		editPanel.setLayout(gbl);
		GridBagConstraints gcString = GUIToolbox.buildConstraints(0, 0, 1, 2);
			gcString.weightx = 100;
		GridBagConstraints gcLang = GUIToolbox.buildConstraints(1, 0, 1, 1);
		GridBagConstraints gcGroup = GUIToolbox.buildConstraints(1, 1, 1, 1);
		gbl.setConstraints(stringtree, gcString);
		gbl.setConstraints(languagelist, gcLang);
		gbl.setConstraints(grouptree, gcGroup);
		editPanel.add(stringtree);
		editPanel.add(languagelist);
		editPanel.add(grouptree);
		
		//Init Eventhandling ------------------------------
		
				
		//Reset change flag
		flag.setChangeFlag(false);
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
