package fs.polyglot.view;

import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dom4j.Document;

import fs.event.DocumentChangeFlag;
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
	private JTextField tabledesc;
	
	// DATA ***************************************
	// ********************************************
	
	//Table data
	private PolyglotTableModel table;
	private DocumentChangeFlag flag;
	
	//Resource
	private ResourceReference resource;
	private PolyglotStringLoader loader;
	private String languageID;
	
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
