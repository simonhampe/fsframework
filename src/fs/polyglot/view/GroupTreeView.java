package fs.polyglot.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import org.dom4j.Document;

import fs.event.DataRetrievalListener;
import fs.gui.GUIToolbox;
import fs.polyglot.model.Group;
import fs.polyglot.model.GroupTreeModel;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.undo.TableUndoManager;
import fs.polyglot.undo.UndoableEditFactory;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This implements a tree view of the group structure of a polyglot string
 * table. In addition to the actual tree view there are buttons for basic
 * operations.
 * 
 * @author Simon Hampe
 * 
 */
public class GroupTreeView extends JPanel implements ResourceDependent {

	//TODO: There are actually two different possible operations: Renaming groups and moving group strings
	
	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = 8224310045071561424L;
	
	//Components
	private JButton editButton = new JButton();
	private JButton deleteButton = new JButton();
	private JTree grouptree = new JTree();

	private GroupTreeModel treemodel = null;
	private GroupTreeCellRenderer treerenderer = null;

	// Icons

	private ImageIcon deleteIcon;
	private ImageIcon editIcon;

	// Resource reference an language id for tooltips
	private ResourceReference reference;
	private PolyglotStringLoader loader;
	private String languageID;
	private static final String sgroup = "fs.polyglot.GroupTreeView";

	// Associated table
	private PolyglotTableModel table;

	// Undo factory
	UndoableEditFactory editFactory = null;

	// LISTENERS ******************************************************
	// ****************************************************************

	// Enables/disables buttons according to current selection
	private TreeSelectionListener selectionListener = new TreeSelectionListener() {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			//Enable button, if selection isn't empty and not the root node
			deleteButton.setEnabled(grouptree.getSelectionCount() >0 && grouptree.getSelectionRows()[0] != 0 );
			//Enable button, if selection isn't empty
			editButton.setEnabled(grouptree.getSelectionCount() > 0 );
		}

	};
	
	//Opens up a group editor
	private Action editListener = new AbstractAction() {
		/**
		 * compiler-generated version id
		 */
		private static final long serialVersionUID = -763939291634307386L;
		@Override
		public void actionPerformed(ActionEvent e) {
			GroupEditor editor = new GroupEditor((((Group)grouptree.getSelectionPath().getLastPathComponent()).path),reference, loader, languageID);
			editor.setModalityType(ModalityType.APPLICATION_MODAL);
			editor.addDataRetrievalListener(editorListener);
			editor.setVisible(true);
		}
	};
	
	//Processes the result of the editor
	private DataRetrievalListener editorListener = new DataRetrievalListener() {
		@Override
		public void dataReady(Object source, Object data) {
			editFactory.performUndoableGroupEdit(((Group)grouptree.getSelectionPath().getLastPathComponent()).path, data.toString());
			System.out.println(table.getGroupList());
			
		}
	};

	// CONSTRUCTOR ****************************************************
	// ****************************************************************

	/**
	 * Creates a group tree view (a tree only displaying the string groups of a
	 * PolyglotStringTable). This will register itself automatically to the
	 * {@link TableUndoManager} associated to the specified table.
	 * 
	 * @param r
	 *            The resource reference for icons and such. If null, the
	 *            default reference is used
	 * @param stringloader
	 *            The string loader for tooltips and such. If null, the default
	 *            loader is used
	 * @param languageID
	 *            The language for tooltips and such. If null, the global
	 *            language id is used
	 * @param associatedTable
	 *            The table whose data should be displayed. If null, an empty
	 *            table is used
	 */
	public GroupTreeView(ResourceReference r,
			PolyglotStringLoader stringloader, String languageID,
			PolyglotTableModel associatedTable) {
		// Copy data
		super();
		assignReference(r);
		this.languageID = languageID != null ? languageID : PolyglotStringTable.getGlobalLanguageID();
		this.loader = stringloader != null ? stringloader : PolyglotStringLoader.getDefaultLoader();
		this.table = associatedTable != null ? associatedTable : new PolyglotTableModel("", "");
		this.editFactory = new UndoableEditFactory(this.table,this.loader, this.languageID,TableUndoManager.getUndoManager(this.table));
		
		//Init Components --------------------------------------------------------------
		
		//Tree
		treemodel = new GroupTreeModel(this.table,false,false);
		treerenderer = new GroupTreeCellRenderer(reference,loader, this.languageID,true, this.table.getTableID());
		grouptree.setModel(treemodel);
		grouptree.setCellRenderer(treerenderer);
		grouptree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		ToolTipManager.sharedInstance().registerComponent(grouptree);
		JScrollPane scrollPane = new JScrollPane(grouptree);

		// Buttons
		deleteButton.setIcon(deleteIcon);
		deleteButton.setToolTipText(loader.getString(sgroup + ".delete",
				this.languageID));
		editButton.setIcon(editIcon);
		editButton.setToolTipText(loader.getString(sgroup + ".edit",
				this.languageID));

		JPanel buttonbar = new JPanel();
		buttonbar.setLayout(new GridLayout(1, 3));
		buttonbar.add(editButton);
		buttonbar.add(deleteButton);

		// Layout
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints treec = GUIToolbox.buildConstraints(0, 1, 1, 1);
		treec.weighty = 100;
		GridBagConstraints barc = GUIToolbox.buildConstraints(0, 0, 1, 1);
		gbl.setConstraints(scrollPane, treec);
		gbl.setConstraints(buttonbar, barc);

		add(scrollPane);
		add(buttonbar);

		// Event handling
		grouptree.getSelectionModel().addTreeSelectionListener(
				selectionListener);
		selectionListener.valueChanged(null);
		editButton.addActionListener(editListener);
	}

	// RESOURCE DEPENDENT METHODS *************************************
	// ****************************************************************

	/**
	 * Assigns a resource reference and reloads icons (does not cause a repaint)
	 */
	@Override
	public void assignReference(ResourceReference r) {
		reference = r != null ? r : FsfwDefaultReference.getDefaultReference();
		deleteIcon = new ImageIcon(reference.getFullResourcePath(this,
				"graphics/GroupTreeView/delete.png"));
		editIcon = new ImageIcon(reference.getFullResourcePath(this,
				"graphics/GroupTreeView/edit.png"));
	}

	/**
	 * Expects two icons and the necessary resource for its renderer
	 */
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = (XMLDirectoryTree) treerenderer
				.getExpectedResourceStructure();
		tree.addPath("graphics/GroupTreeView/new.png");
		tree.addPath("graphics/GroupTreeView/edit.png");
		return tree;
	}

}
