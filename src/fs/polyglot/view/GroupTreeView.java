package fs.polyglot.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Dialog.ModalityType;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.CannotRedoException;

import org.apache.log4j.Logger;
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

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = 8224310045071561424L;
	
	//Components
	private JButton editButton = new JButton();
	private JButton deleteButton = new JButton();
	private JButton toggleViewButton = new JButton();
	private JTree grouptree = new JTree();

	private GroupTreeModel treemodel = null;
	private GroupTreeCellRenderer treerenderer = null;

	// Icons

	private ImageIcon deleteIcon;
	private ImageIcon editIcon;
	private ImageIcon toggleOnIcon;
	private ImageIcon toggleOffIcon;

	// Resource reference an language id for tooltips
	private ResourceReference reference;
	private PolyglotStringLoader loader;
	private String languageID;
	private static final String sgroup = "fs.polyglot.GroupTreeView";

	// Associated table
	private PolyglotTableModel table;

	// Undo factory
	UndoableEditFactory editFactory = null;

	//Log
	private org.apache.log4j.Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
	
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
			openEditor((((Group)grouptree.getSelectionPath().getLastPathComponent()).path),e.getSource() == deleteButton ? null : "");
		}
	};
	
	//Processes the result of the editor
	private DataRetrievalListener editorListener = new DataRetrievalListener() {
		@Override
		public void dataReady(Object source, Object data) {
			try {
				logger.info(loader.getString("fs.global.perform", languageID, ((GroupEditor)source).getUndoableGroupEdit(editFactory).getRedoPresentationName()));
				((GroupEditor) source).performUndoableGroupEdit(editFactory);
			}
			catch(CannotRedoException ce) {
				logger.error(loader.getString("fs.global.cantperform", languageID, ((GroupEditor)source).getUndoableGroupEdit(editFactory).getRedoPresentationName()));
			}
		}
	};
	
	//Toggles the cutGroupPath property
	private ActionListener toggleListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			setCutGroupPath(!doesCutGroupPath());
			toggleViewButton.setIcon(doesCutGroupPath()? toggleOnIcon : toggleOffIcon);
		}
	};

	// DRAG'N'DROP SUPPORT ********************************************
	// ****************************************************************
	
	//Allows to drag a node onto any DIFFERENT node and opens up a group editor accordingly
	private TransferHandler dndHandler = new TransferHandler() {
		/**
		 * compiler-generated version id
		 */
		private static final long serialVersionUID = 3443048457094840001L;

		/**
		 * Import is possible, if the new path is not equal to the old path
		 */
		@Override
		public boolean canImport(TransferSupport support) {
			JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
			if(dl == null || dl.getPath() == null) return false;
			Group g = (Group)dl.getPath().getLastPathComponent();
			try {
				String old = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
				return old == null? g.path != null : !old.equals(g.path);	
			} catch (UnsupportedFlavorException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		}

		/**
		 * Returns MOVE
		 */
		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.MOVE;
		}

		/**
		 * Returns the path of the components text 
		 */
		@Override
		protected Transferable createTransferable(JComponent c) {
			return new StringSelection(((Group)grouptree.getSelectionPath().getLastPathComponent()).path);
		}

		/**
		 * Does nothing
		 */
		@Override
		protected void exportDone(JComponent source, Transferable data,
				int action) {
			super.exportDone(source, data, action);
		}

		/**
		 * Opens up an editor with original path the dragged group and new path the drop node 
		 */
		@Override
		public boolean importData(TransferSupport support) {
			JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
			Group g = (Group)dl.getPath().getLastPathComponent();
			try {
				String old = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
				openEditor(old, g.path);
				return true;
			} catch (UnsupportedFlavorException e) {
				return true;
			} catch (IOException e) {
				return true;
			}
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
		treemodel = new GroupTreeModel(this.table,false,false,false);
		treerenderer = new GroupTreeCellRenderer(reference,loader, this.languageID,true, this.table);
		grouptree.setModel(treemodel);
		grouptree.setCellRenderer(treerenderer);
		grouptree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		ToolTipManager.sharedInstance().registerComponent(grouptree);
		JScrollPane scrollPane = new JScrollPane(grouptree);
		//Drag'n'Drop
		grouptree.setDragEnabled(true);
		grouptree.setDropMode(DropMode.ON);
		grouptree.setTransferHandler(dndHandler);
		
		// Main Buttons
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

		//View Control
		toggleViewButton.setIcon(toggleOnIcon);
		toggleViewButton.addActionListener(toggleListener);
		toggleViewButton.setToolTipText(loader.getString(sgroup + ".toggletip", this.languageID));
		JPanel togglePanel = new JPanel();
		togglePanel.setBorder(BorderFactory.createEtchedBorder());
		togglePanel.setLayout(new BorderLayout());
		togglePanel.add(toggleViewButton, BorderLayout.WEST);
		
		// Layout
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints treec = GUIToolbox.buildConstraints(0, 1, 1, 1);
		treec.weighty = 100; treec.weightx = 100;
		GridBagConstraints barc = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints togglec = GUIToolbox.buildConstraints(0, 2, 1, 1);
		gbl.setConstraints(scrollPane, treec);
		gbl.setConstraints(buttonbar, barc);
		gbl.setConstraints(togglePanel, togglec);

		add(scrollPane);
		add(buttonbar);
		add(togglePanel);

		// Event handling
		grouptree.getSelectionModel().addTreeSelectionListener(
				selectionListener);
		selectionListener.valueChanged(null);
		editButton.addActionListener(editListener);
		deleteButton.addActionListener(editListener);
	}

	// EDITOR METHODS *************************************************
	// ****************************************************************
	
	/**
	 * Opens up an editor filled with the specified data and already registered to the internal listener
	 */
	protected void openEditor(String oldValue, String newValue) {
		GroupEditor editor = new GroupEditor(oldValue,newValue,reference, loader, languageID);
		editor.setModalityType(ModalityType.APPLICATION_MODAL);
		editor.addDataRetrievalListener(editorListener);
		editor.setVisible(true);
	}
	
	// VIEW CONTROL ********************************************************
	// *********************************************************************
	
	/**
	 * Returns whether group paths are trimmed to the last path component
	 */
	public boolean doesCutGroupPath() {
		return treerenderer.doesCutGroupPath();
	}

	/**
	 * Specifies whether group paths should be trimmed to the last path component and 
	 * causes a repaint
	 */
	public void setCutGroupPath(boolean cutGroupPath) {
		treerenderer.setCutGroupPath(cutGroupPath);
		repaint();
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
		toggleOnIcon = new ImageIcon(reference.getFullResourcePath(this, "graphics/GroupTreeView/toggleCut.png"));
		toggleOffIcon = new ImageIcon(reference.getFullResourcePath(this, "graphics/GroupTreeView/toggleNoCut.png"));
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
		tree.addPath("graphics/GroupTreeView/toggleCut.png");
		tree.addPath("graphics/GroupTreeView/toggleNoCut.png");
		return tree;
	}

}
