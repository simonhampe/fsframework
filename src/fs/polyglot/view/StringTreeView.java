package fs.polyglot.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.CompoundEdit;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import fs.gui.GUIToolbox;
import fs.polyglot.model.GroupTreeModel;
import fs.polyglot.model.LanguageListModel;
import fs.polyglot.model.PolyglotString;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.model.TreeObject;
import fs.polyglot.model.Variant;
import fs.polyglot.undo.TableUndoManager;
import fs.polyglot.undo.UndoableEditFactory;
import fs.polyglot.undo.UndoablePolyglotStringEdit;
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

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = 3073891130815953048L;
	//Components
	private JTree stringtree = new JTree();
	private JButton add = new JButton();
	private JButton delete = new JButton();
	private JButton editsingle = new JButton();
	private JButton editmultiple = new JButton();
	private JToggleButton editSelected = new JToggleButton();
	private JToggleButton editIncomplete = new JToggleButton();
	private JToggleButton onlyLanguages = new JToggleButton();
	private JToggleButton excludeLanguages = new JToggleButton();
	private JList languageList = new JList();
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
	private ImageIcon editSelectedIcon;
	private ImageIcon editIncompleteIcon;
	private ImageIcon onlyLanguagesIcon;
	private ImageIcon excludeLanguagesIcon;
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
			TreePath[] selected = stringtree.getSelectionPaths();
			if(selected == null || selected.length > 1) {
				if(selected == null) {
					delete.setEnabled(false);
					editsingle.setEnabled(false);
				}
				add.setIcon(addStringIcon);
				add.setToolTipText(loader.getString(sgroup + ".addstring", languageID));
			}
			else {
				delete.setEnabled(true);
				editsingle.setEnabled(true);
				switch(((TreeObject)selected[0].getLastPathComponent()).getType()) {
				case GROUP: add.setIcon(addStringIcon); add.setToolTipText(loader.getString(sgroup + ".addstring",languageID));break;
				case POLYGLOTSTRING:  
				case VARIANT: add.setIcon(addVariantIcon); add.setToolTipText(loader.getString(sgroup + ".addvariant",languageID));break; 
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
	
	//Listens for the two exclusive edit restriction toggle buttonss
	private ChangeListener toggleListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			if(e.getSource() == onlyLanguages && onlyLanguages.isSelected()) excludeLanguages.setSelected(false);
			if(e.getSource() == excludeLanguages && excludeLanguages.isSelected()) onlyLanguages.setSelected(false);
		}
	};
	
	
	//Edit listeners --------------------------------------
	
	private ActionListener newListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			//Create configuration
			StringEditorConfiguration config = new StringEditorConfiguration();
			//Create string ID
			int selcount = stringtree.getSelectionCount();
			TreePath selpath = stringtree.getSelectionPath();
			String stringID = selcount == 0 ? "" : (((TreeObject)selpath.getLastPathComponent()).path) + ".";
			//Open editor
			StringEditor editor = new StringEditor(reference, loader, languageID, table ,null, stringID, config,TableUndoManager.getUndoManager(table));
			editor.setVisible(true);
		}
	};
	
	
	private ActionListener deleteListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath[] selectedPaths = stringtree.getSelectionPaths();
			for(TreePath p : selectedPaths) {
				TreeObject ob = (TreeObject)p.getLastPathComponent();
				switch(ob.getType()) {
				case GROUP:
					//Create compound edit to remove all strings in this group
					HashSet<String> idsToRemove = table.getStringsInSubgroups(ob.path);
					CompoundEdit removeEdit = new CompoundEdit();
					for(String id : idsToRemove) {
						UndoablePolyglotStringEdit rem = editFactory.createUndoablePolyglotStringEdit(new PolyglotString(ob.path,id,false), null);
						rem.redo();
						removeEdit.addEdit(rem);
					}
					removeEdit.end();
					editFactory.postEdit(removeEdit);
					break;
				case POLYGLOTSTRING:
					editFactory.performUndoablePolyglotStringEdit((PolyglotString)ob, null);
					break;
				case VARIANT:
					editFactory.performUndoableVariantEdit((Variant)ob, null);
					break;
				}
			}
		}
	};
	
	private ActionListener simpleEditListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			//Create config
			StringEditorConfiguration config = new StringEditorConfiguration();
				config.editOnlyIncomplete = treemodel.doesShowOnlyIncomplete();
				config.editOnlySelected = true;
			//Create Editor
			StringEditor editor = new StringEditor(reference,loader,languageID,table,getSelectedStrings(),null,config,TableUndoManager.getUndoManager(table));
			//Show editor
			editor.setVisible(true);
		}
	};
	private ActionListener multipledEditListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			//Create config
			StringEditorConfiguration config = new StringEditorConfiguration();
				config.editOnlyIncomplete = editIncomplete.isSelected();
				config.editOnlySelected = editSelected.isSelected();
				HashSet<String> langlist = new HashSet<String>();
					for(Object o : languageList.getSelectedValues()) langlist.add(o.toString());
				config.excludeTheseLanguages = excludeLanguages.isSelected()? langlist : null;
				config.onlyTheseLanguages = onlyLanguages.isSelected()? langlist : null;
			//Create editor
			StringEditor editor = new StringEditor(reference,loader,languageID,table,getSelectedStrings(),null,config,TableUndoManager.getUndoManager(table));
			//Show editor
			editor.setVisible(true);
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
		editFactory = new UndoableEditFactory(table,loader,this.languageID,TableUndoManager.getUndoManager(table));
		
		//Init GUI and layout ---------------------------------------------------------------
		treemodel = new GroupTreeModel(associatedTable, true, true, false);
		treerenderer = new GroupTreeCellRenderer(reference,loader,this.languageID,true,table);
		stringtree.setModel(treemodel);
		stringtree.setCellRenderer(treerenderer);
		ToolTipManager.sharedInstance().registerComponent(stringtree);
		stringtree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		stringtree.getSelectionModel().addTreeSelectionListener(selectionListener);
		selectionListener.valueChanged(null);
		languageList.setModel(new LanguageListModel(table));
		
		//Init Buttons  (those which do not depend on selection) 
		delete.setIcon(deleteIcon);
		delete.setToolTipText(loader.getString(sgroup + ".delete", languageID));
		editsingle.setIcon(editSingleIcon);
		editsingle.setToolTipText(loader.getString(sgroup + ".editsingle", languageID));
		editmultiple.setIcon(editMultipleIcon);
		editmultiple.setToolTipText(loader.getString(sgroup + ".editmultiple", languageID));
		editIncomplete.setIcon(editIncompleteIcon);
		editIncomplete.setToolTipText(loader.getString(sgroup + ".editincomplete", languageID));
		editSelected.setIcon(editSelectedIcon);
		editSelected.setToolTipText(loader.getString(sgroup + ".editselected", languageID));
		onlyLanguages.setIcon(onlyLanguagesIcon);
		onlyLanguages.setToolTipText(loader.getString(sgroup + ".editonly", languageID ));
		excludeLanguages.setIcon(excludeLanguagesIcon);
		excludeLanguages.setToolTipText(loader.getString(sgroup + ".editnot",languageID));
		viewString.setIcon(viewStringIcon);
		viewString.setToolTipText(loader.getString(sgroup + ".togglestring", languageID));
		viewVariant.setIcon(viewVariantIcon);
		viewVariant.setToolTipText(loader.getString(sgroup + ".togglevariant", languageID));
		toggleCut.setIcon(toggleCutIcon);
		toggleCut.setToolTipText(loader.getString(sgroup + ".togglecut", languageID));
		showOnlyIncomplete.setIcon(showNotOnlyIncompleteIcon);
		showOnlyIncomplete.setToolTipText(loader.getString(sgroup + ".toggleincomplete", languageID));
		
		//Create Panels to contain everything
		
		JPanel operationbar = new JPanel();
		JPanel viewbar = new JPanel();
		JPanel advedit = new JPanel();
		advedit.setBorder(BorderFactory.createEtchedBorder());
		JScrollPane scrollpane = new JScrollPane(stringtree);
		//I have to mess around with the size to make it pretty
		JScrollPane listpane = new JScrollPane(languageList) {
			/**
			 * compiler-generated version id
			 */
			private static final long serialVersionUID = -5008388045112489067L;

			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				return new Dimension(d.width + 50, editmultiple.getPreferredSize().height);
			}
		};
		operationbar.setLayout(new FlowLayout(FlowLayout.LEFT));
		viewbar.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		
		
		//Layout
		
		GridBagLayout ael = new GridBagLayout(); // Layout for the advanced edit panel
		advedit.setLayout(ael);
		GridBagConstraints cEdit = GUIToolbox.buildConstraints(0, 0, 1, 2); cEdit.insets = new Insets(5,5,5,5);
		GridBagConstraints cInc = GUIToolbox.buildConstraints(1, 0, 1, 1); cInc.insets = new Insets(5,0,0,0);
		GridBagConstraints cSel = GUIToolbox.buildConstraints(1, 1, 1, 1); cSel.insets = new Insets(0,0,5,0);
		GridBagConstraints cOnl = GUIToolbox.buildConstraints(2, 0, 1, 1); cOnl.insets = new Insets(5,0,0,0);
		GridBagConstraints cExc = GUIToolbox.buildConstraints(2, 1, 1, 1); cExc.insets = new Insets(0,0,5,0);
		GridBagConstraints cLis = GUIToolbox.buildConstraints(3, 0, 1, 2); cLis.insets = new Insets(5,5,5,5);
		ael.setConstraints(editmultiple, cEdit); ael.setConstraints(editIncomplete, cInc);
		ael.setConstraints(editSelected, cSel); ael.setConstraints(onlyLanguages, cOnl);
		ael.setConstraints(excludeLanguages, cExc); ael.setConstraints(listpane, cLis);
		advedit.add(editmultiple); advedit.add(editIncomplete); advedit.add(editSelected);
		advedit.add(onlyLanguages); advedit.add(excludeLanguages);advedit.add(listpane);
		
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints cOps = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints cScroll = GUIToolbox.buildConstraints(0, 1, 1, 1); cScroll.weighty = 100; cScroll.weightx = 100;
		GridBagConstraints cView = GUIToolbox.buildConstraints(0, 2, 1, 1);
		gbl.setConstraints(operationbar, cOps);
		gbl.setConstraints(viewbar, cView);
		gbl.setConstraints(scrollpane, cScroll);
		
		//Add everything
		operationbar.add(add);
		operationbar.add(delete);
		operationbar.add(editsingle);
		operationbar.add(advedit);
		viewbar.add(viewString);
		viewbar.add(viewVariant);
		viewbar.add(toggleCut);
		viewbar.add(showOnlyIncomplete);
		
		add(operationbar);
		add(scrollpane); 
		add(viewbar);
		
		//Assign listeners ------------------------------------------------------------------
		
		viewString.addActionListener(viewStringListener);
		viewVariant.addActionListener(viewVariantListener);
		toggleCut.addActionListener(toggleCutListener);
		showOnlyIncomplete.addActionListener(showOnlyIncompleteListener);
		onlyLanguages.addChangeListener(toggleListener);
		excludeLanguages.addChangeListener(toggleListener);
		
		add.addActionListener(newListener);
		delete.addActionListener(deleteListener);
		editsingle.addActionListener(simpleEditListener);
		editmultiple.addActionListener(multipledEditListener );
	}
	
	// GETTERS AND SETTERS *********************************************************************
	// *****************************************************************************************
	
	/**
	 * Returns a list of selected strings, i.e. a list of all strings which are either selected directly or whose group is selected.
	 */
	public HashSet<String> getSelectedStrings() {
		TreePath[] paths = stringtree.getSelectionPaths();
		if(paths == null || paths.length == 0) return new HashSet<String>();
		else {
			HashSet<String> selstrings = new HashSet<String>();
			for(TreePath p : paths) {
				TreeObject ob = (TreeObject)p.getLastPathComponent();
				switch(ob.getType()) {
				case GROUP: selstrings.addAll(table.getStringsInSubgroups(ob.path)); break;
				case POLYGLOTSTRING: selstrings.add(((PolyglotString)ob).stringID);
				}
			}
			return selstrings;
		}
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
		editIncompleteIcon = new ImageIcon(reference.getFullResourcePath(this, path + "editincomplete.png"));
		editSelectedIcon = new ImageIcon(reference.getFullResourcePath(this, path + "editselected.png"));
		onlyLanguagesIcon = new ImageIcon(reference.getFullResourcePath(this, path + "editonly.png"));
		excludeLanguagesIcon = new ImageIcon(reference.getFullResourcePath(this, path + "editnot.png"));
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
		
		for(String s : Arrays.asList("delete","edit","editmultiple","editselected", "editincomplete", "editonly", "editnot","newstring","newvariant","nostring","novariant","nowarn","string","variant", "toggleCut", "toggleNoCut", "variant","warn")) {
			tree.addPath("graphics/StringTreeView/" + s + ".png");
		}
		
		return tree;
	}

}
