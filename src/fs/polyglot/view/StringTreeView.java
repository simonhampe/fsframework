package fs.polyglot.view;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import fs.gui.GUIToolbox;
import fs.polyglot.model.GroupTreeModel;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.model.TreeObject;
import fs.polyglot.model.TreeObject.NodeType;
import fs.polyglot.undo.UndoableEditFactory;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This implements the main tree view for the application POLYGLOT. The complete string table is displayed as tree, Strings and Variants 
 * can be toggled to be invisible, controls for several operations are included.
 * @author hampe
 *
 */
public class StringTreeView extends JPanel implements ResourceDependent {

	//Components
	private JTree stringtree = new JTree();
	private JButton add = new JButton();
	private JButton delete = new JButton();
	private JButton editsingle = new JButton();
	private JButton editmultiple = new JButton();
	private JButton viewString = new JButton();
	private JButton viewVariant = new JButton();
	private JButton toggleCut = new JButton();
	private JButton showOnlyIncomplete = new JButton();
	
	//Associated models
	private GroupTreeModel treemodel;
	private GroupTreeCellRenderer treerenderer;
	
	//Icons
	private ImageIcon addStringIcon;
	private ImageIcon addVariantIcon;
	private ImageIcon deleteIcon;
	private ImageIcon editSingleIcon;
	private ImageIcon editMultipleIcon;
	private ImageIcon viewStringIcon;
	private ImageIcon viewNoStringIcon;
	private ImageIcon viewVariantIcon;
	private ImageIcon viewNoVariantIcon;
	private ImageIcon toggleCutIcon;
	private ImageIcon toggleNoCutIcon;
	private ImageIcon showOnlyIncompleteIcon;
	private ImageIcon showNotOnlyIncompleteIcon;
	
	// Resource reference an language id for tooltips
	private ResourceReference reference;
	private PolyglotStringLoader loader;
	private String languageID;
	private static final String sgroup = "fs.polyglot.StringTreeView";

	// Associated table
	private PolyglotTableModel table;

	// Undo factory
	UndoableEditFactory editFactory = null;

	//Log
	private org.apache.log4j.Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
	
	// LISTENERS *******************************************************************************
	// *****************************************************************************************
	
	//Controls buttons according to current selection
	private TreeSelectionListener selectionListener = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath selected = (TreePath) stringtree.getSelectionPath();
			if(selected == null) {
				delete.setEnabled(false);
				editsingle.setEnabled(false);
				add.setIcon(addStringIcon);
			}
			else {
				delete.setEnabled(true);
				editsingle.setEnabled(true);
				switch(((TreeObject)selected.getLastPathComponent()).getType()) {
				case GROUP: add.setIcon(addStringIcon); break;
				case POLYGLOTSTRING:  
				case VARIANT: add.setIcon(addVariantIcon); break; 
				}
			}
		}
	};
	
	//Listeners for view controls
	
	private ActionListener viewStringListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			treemodel.setIncludeStrings(!treemodel.doesIncludeStrings());
			viewString.setIcon(treemodel.doesIncludeStrings()? viewStringIcon : viewNoStringIcon);
			//If no strings are included, set variant icon to negative, regardless of actual value for variants
			viewVariant.setIcon(treemodel.doesIncludeStrings()? (treemodel.doesIncludeVariants()? viewVariantIcon : viewNoVariantIcon) : viewNoVariantIcon);
		}
	};
	private ActionListener viewVariantListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			//If no strings are visible, set value to true
			treemodel.setIncludeVariants(treemodel.doesIncludeStrings()? !treemodel.doesIncludeVariants() : true);
			if(treemodel.doesIncludeVariants()) {
				treemodel.setIncludeStrings(true);
				viewString.setIcon(viewStringIcon);
			}
			viewVariant.setIcon(treemodel.doesIncludeVariants() ? viewVariantIcon : viewNoVariantIcon);
		}
	};
	private ActionListener toggleCutListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			treerenderer.setCutGroupPath(!treerenderer.doesCutGroupPath());
			toggleCut.setIcon(treerenderer.doesCutGroupPath()? toggleCutIcon : toggleNoCutIcon);
			repaint();
		}
	};
	private ActionListener showOnlyIncompleteListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			treemodel.setShowOnlyIncomplete(!treemodel.doesShowOnlyIncomplete());
			showOnlyIncomplete.setIcon(treemodel.doesShowOnlyIncomplete() ? showOnlyIncompleteIcon : showNotOnlyIncompleteIcon);
		}
	};
	
	// CONSTRUCTOR *****************************************************************************
	// *****************************************************************************************
	
	public StringTreeView(ResourceReference r,
			PolyglotStringLoader stringloader, String languageID,
			PolyglotTableModel associatedTable) {
		//Copy data
		assignReference(r);
		loader = stringloader != null ? stringloader : PolyglotStringLoader.getDefaultLoader();
		this.languageID = languageID != null? languageID : PolyglotStringTable.getGlobalLanguageID();
		table = associatedTable == null ? new PolyglotTableModel("", "") : associatedTable;
		
		//Init GUI and layout ---------------------------------------------------------------
		treemodel = new GroupTreeModel(associatedTable, true, true, false);
		treerenderer = new GroupTreeCellRenderer(reference,loader,this.languageID,true,table.getTableID());
		stringtree.setModel(treemodel);
		stringtree.setCellRenderer(treerenderer);
		ToolTipManager.sharedInstance().registerComponent(stringtree);
		stringtree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		stringtree.getSelectionModel().addTreeSelectionListener(selectionListener);
		selectionListener.valueChanged(null);
		
		//Init Buttons icons (those which do not depend on selection) and tooltips
		delete.setIcon(deleteIcon);
		editsingle.setIcon(editSingleIcon);
		editmultiple.setIcon(editMultipleIcon);
		viewString.setIcon(viewStringIcon);
		viewVariant.setIcon(viewVariantIcon);
		toggleCut.setIcon(toggleCutIcon);
		showOnlyIncomplete.setIcon(showNotOnlyIncompleteIcon);
		
		//Create Panels to contain everything
		
		JPanel operationbar = new JPanel();
		JPanel viewbar = new JPanel();
		JScrollPane scrollpane = new JScrollPane(stringtree);
		operationbar.setLayout(new FlowLayout(FlowLayout.LEFT));
		viewbar.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		//Add everything
		
		operationbar.add(add);
		operationbar.add(delete);
		operationbar.add(editsingle);
		operationbar.add(editmultiple);
		viewbar.add(viewString);
		viewbar.add(viewVariant);
		viewbar.add(toggleCut);
		viewbar.add(showOnlyIncomplete);
		
		//Layout
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints cOps = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints cScroll = GUIToolbox.buildConstraints(0, 1, 1, 1); cScroll.weighty = 100; cScroll.weightx = 100;
		GridBagConstraints cView = GUIToolbox.buildConstraints(0, 2, 1, 1);
		gbl.setConstraints(operationbar, cOps);
		gbl.setConstraints(viewbar, cView);
		gbl.setConstraints(scrollpane, cScroll);
		add(operationbar);
		add(scrollpane); 
		add(viewbar);
		
		//Assign listeners ------------------------------------------------------------------
		
		viewString.addActionListener(viewStringListener);
		viewVariant.addActionListener(viewVariantListener);
		toggleCut.addActionListener(toggleCutListener);
		showOnlyIncomplete.addActionListener(showOnlyIncompleteListener);
		
	}
	
	// RESOURCEDEPENDENT METHODS ***************************************************************
	// *****************************************************************************************
	
	/**
	 * Assigns a resource reference and reloads all icons. Does not cause a repaint. If r == null, the default reference is used
	 */
	@Override
	public void assignReference(ResourceReference r) {
		reference = (r != null) ? r : FsfwDefaultReference.getDefaultReference();
		//Assign icons
		String path = "graphics/StringTreeView/";
		addStringIcon = new ImageIcon(reference.getFullResourcePath(this, path + "newstring.png"));
		addVariantIcon = new ImageIcon(reference.getFullResourcePath(this, path + "newvariant.png"));
		deleteIcon = new ImageIcon(reference.getFullResourcePath(this, path + "delete.png"));
		editSingleIcon = new ImageIcon(reference.getFullResourcePath(this, path + "edit.png"));
		editMultipleIcon = new ImageIcon(reference.getFullResourcePath(this, path + "editmultiple.png"));
		viewStringIcon = new ImageIcon(reference.getFullResourcePath(this, path + "string.png"));
		viewNoStringIcon = new ImageIcon(reference.getFullResourcePath(this, path + "nostring.png"));
		viewVariantIcon = new ImageIcon(reference.getFullResourcePath(this, path + "variant.png"));
		viewNoVariantIcon = new ImageIcon(reference.getFullResourcePath(this, path + "novariant.png"));
		toggleCutIcon = new ImageIcon(reference.getFullResourcePath(this, path + "toggleCut.png"));
		toggleNoCutIcon = new ImageIcon(reference.getFullResourcePath(this, path + "toggleNoCut.png"));
		showOnlyIncompleteIcon = new ImageIcon(reference.getFullResourcePath(this, path + "warn.png"));
		showNotOnlyIncompleteIcon = new ImageIcon(reference.getFullResourcePath(this, path + "nowarn.png"));
		
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		
		for(String s : Arrays.asList("delete","edit","editmultiple","newstring","newvariant","nostring","novariant","nowarn","string","variant", "toggleCut", "toggleNoCut", "variant","warn")) {
			tree.addPath("graphics/StringTreeView/" + s + ".png");
		}
		
		return tree;
	}

}
