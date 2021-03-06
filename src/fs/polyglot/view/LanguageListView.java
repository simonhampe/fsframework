package fs.polyglot.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.CannotRedoException;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import fs.event.DataRetrievalListener;
import fs.gui.GUIToolbox;
import fs.polyglot.model.Language;
import fs.polyglot.model.LanguageListModel;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.undo.TableUndoManager;
import fs.polyglot.undo.UndoableEditFactory;
import fs.polyglot.undo.UndoableLanguageEdit;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This class implements a panel containing a list of PolyglotStringTable
 * languages as rendered by LanguageListCellRenderer, together with some buttons
 * to manipulate it
 * 
 * @author Simon Hampe
 * 
 */
public class LanguageListView extends JPanel implements ResourceDependent {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = -1724195179235556531L;
	// Components, Models and Renderers
	private JButton newButton = new JButton();
	private JButton deleteButton = new JButton();
	private JButton editButton = new JButton();

	private JList languageList = new JList();
	private LanguageListModel listmodel;
	private LanguageListCellRenderer listrenderer;

	// Icons
	private ImageIcon newIcon;
	private ImageIcon deleteIcon;
	private ImageIcon editIcon;

	// Resource reference an language id for tooltips
	private ResourceReference reference;
	private PolyglotStringLoader loader;
	private String languageID;
	private static final String sgroup = "fs.polyglot.LanguageListView";

	// Associated table
	private PolyglotTableModel table;
	
	//Logger
	private Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

	// Event handlers and actions **************
	// *****************************************

	// Edit factory
	private UndoableEditFactory editFactory;

	/**
	 * Mouse listener for double clicks
	 */
	private MouseListener doubleClickListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			super.mouseClicked(e);
			if (e.getClickCount() >= 2) {
				if (languageList.getSelectedIndex() != -1) {
					editorListener.actionPerformed(new ActionEvent(editButton,
							0, null));
				}
			}
		}
	};

	/**
	 * Toggles the activation state of the buttons depending on the current
	 * selection
	 */
	private ListSelectionListener selectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// Toggle add / edit / addud button
			boolean enable = true;
			boolean udenable = true;
			if (languageList.getSelectedValue() == null) {
				enable = false;
				udenable = false;
			} else {
				// /For deleteButton also check, if the selected language is an
				// undescribed language
				udenable = ((Language) languageList.getSelectedValue()).isOnlyUsed;

			}
			editButton.setEnabled(enable);
			deleteButton.setEnabled(!udenable && enable);
		}
	};

	/**
	 * Toggles button states on data changes
	 */
	private ListDataListener dataListener = new ListDataListener() {

		@Override
		public void contentsChanged(ListDataEvent e) {
			selectionListener.valueChanged(null);
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			contentsChanged(e);
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			contentsChanged(e);
		}
	};

	/**
	 * Delete selected language from list
	 */
	private ActionListener deleteListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			UndoableLanguageEdit edit = editFactory.createUndoableLanguageEdit((Language) languageList.getSelectedValue(), null);
			try {
				logger.info(loader.getString("fs.global.perform", languageID, edit.getRedoPresentationName()));
				editFactory.performUndoableLanguageEdit((Language) languageList
						.getSelectedValue(), null);
			}
			catch(CannotRedoException ce) {
				logger.error(loader.getString("fs.global.cantperform",languageID,edit.getRedoPresentationName()));
			}
			
		}
	};

	/**
	 * Opens a LanguageEditor appropriate for the button
	 */
	private ActionListener editorListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Create dialog
			LanguageEditor editor = null;
			if (e.getSource() == newButton) {
				editor = createEditor(null);
			}
			if (e.getSource() == editButton) {
				editor = createEditor((Language) languageList
						.getSelectedValue());
			}
			if (editor != null) {
				editor.setVisible(true);
			}
		}
	};

	/**
	 * Adds a new language generated by the editor
	 */
	private DataRetrievalListener editListener = new DataRetrievalListener() {
		@Override
		public void dataReady(Object source,  Object data) {
			try {
				logger.info(loader.getString("fs.global.perform", languageID, ((LanguageEditor)source).getUndoableLanguageEdit(editFactory).getRedoPresentationName()));
				((LanguageEditor)source).performUndoableLanguageEdit(editFactory);
			}
			catch(CannotRedoException ce) {
				logger.error(loader.getString("fs.global.cantperform", languageID, ((LanguageEditor)source).getUndoableLanguageEdit(editFactory)));
			}
		}
	};

	// CONSTRUCTOR *************************************
	// *************************************************

	/**
	 * Constructs a language list view, containing a scrollable list view of the
	 * language list of a polyglot string table together with buttons for
	 * operations. This list view will automatically link itself to the
	 * TableUndoManager associated to the table
	 * 
	 * @param r
	 *            The resource reference for this component and created dialogs.
	 *            If null, default reference is used
	 * @param stringloader
	 *            The string loader for the tooltip strings and all created
	 *            dialogs. If null, the default loader is used.
	 * @param languageID
	 *            The language id for this component and created dialogs. If
	 *            null, the global language id is used
	 * @param associatedTable
	 *            The associated PolyglotTableModel, the data of which is
	 *            displayed. If null, an empty table is created.
	 */
	public LanguageListView(ResourceReference r,
			PolyglotStringLoader stringloader, String languageID,
			PolyglotTableModel associatedTable) {
		super();
		// Copy data
		assignReference(r);
		this.languageID = languageID != null ? languageID : PolyglotStringTable
				.getGlobalLanguageID();
		this.loader = stringloader != null ? stringloader
				: PolyglotStringLoader.getDefaultLoader();
		this.table = associatedTable != null ? associatedTable
				: new PolyglotTableModel("", "");
		this.editFactory = new UndoableEditFactory(this.table, this.loader,
				this.languageID, TableUndoManager
						.getUndoManager(associatedTable));

		// Create GUI

		// Create List
		listmodel = new LanguageListModel(table);
		listmodel.addListDataListener(dataListener);
		listrenderer = new LanguageListCellRenderer(r, loader, languageID);
		languageList.setModel(listmodel);
		languageList.setCellRenderer(listrenderer);
		languageList.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		languageList.getSelectionModel().addListSelectionListener(
				selectionListener);
		languageList.addMouseListener(doubleClickListener);
		selectionListener.valueChanged(null);
		JScrollPane listPane = new JScrollPane(languageList);

		// Create Button-bar
		JPanel buttonbar = new JPanel();
		buttonbar.setLayout(new GridLayout(1, 4));
		newButton.setIcon(newIcon);
		newButton.setToolTipText(loader.getString(sgroup + ".newtooltip",
				this.languageID));
		buttonbar.add(newButton);
		deleteButton.setIcon(deleteIcon);
		deleteButton.setToolTipText(loader.getString(sgroup + ".deletetooltip",
				this.languageID));
		buttonbar.add(deleteButton);
		editButton.setIcon(editIcon);
		editButton.setToolTipText(loader.getString(sgroup + ".edittooltip",
				this.languageID));
		buttonbar.add(editButton);
		// buttonbar.add(addudButton);

		// Now put them together
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints listc = GUIToolbox.buildConstraints(0, 1, 1, 1);
		listc.weighty = 100; listc.weightx = 100;
		GridBagConstraints barc = GUIToolbox.buildConstraints(0, 0, 1, 1);
		gbl.setConstraints(listPane, listc);
		gbl.setConstraints(buttonbar, barc);

		add(listPane);
		add(buttonbar);

		// Add event handling
		deleteButton.addActionListener(deleteListener);
		newButton.addActionListener(editorListener);
		editButton.addActionListener(editorListener);

	}

	// EDITOR METHODS **********************************************
	// *************************************************************

	/**
	 * Creates a language editor for editing the specified language. If l ==
	 * null, this creates an empty editor and registers a listener for adding a
	 * language upon confirmation. Otherwise it registers a listener for
	 * changing the currently selected language to the resulting language. The
	 * editor is placed outside this list view just beside the currently
	 * selected language (if any is selected) or at the top of the list.
	 */
	protected LanguageEditor createEditor(Language l) {
		// Create language id taboo list (and remove l's id, if it is edited)
		HashSet<String> tabooList = new HashSet<String>(table.getLanguageList());
		if (l != null)
			tabooList.remove(l.id);
		// Create dialog
		LanguageEditor editor = new LanguageEditor(tabooList, l, reference,
				loader, languageID);
		// If l is null, this is a language addition, but also if l!= null but
		// not contained in the official table language list
		// (in this case, an only-used language is added)
		editor.addDataRetrievalListener(editListener);
		// Set Bounds
		int index = languageList.getSelectedIndex();
		int y = 0;
		if (index != -1 && index < languageList.getModel().getSize()) //If a language has just been deleted, it might be, that index is still set to this row
			y = languageList.getCellBounds(index, index).y;
		int realx = languageList.getLocationOnScreen().x;
		int realy = languageList.getLocationOnScreen().y;
		editor.setBounds(realx - editor.getWidth(), realy + y, editor
				.getWidth(), editor.getHeight());
		// Return
		return editor;
	}

	// RESOURCEDEPENDENT *******************************************
	// *************************************************************

	/**
	 * Sets the resource reference and reloads the icons. If r == null, the
	 * default reference is used.
	 */
	@Override
	public void assignReference(ResourceReference r) {
		reference = r != null ? r : FsfwDefaultReference.getDefaultReference();
		// Reload icons
		String path = "graphics/LanguageListView/";
		newIcon = new ImageIcon(reference.getFullResourcePath(this, path
				+ "new.png"));
		deleteIcon = new ImageIcon(reference.getFullResourcePath(this, path
				+ "delete.png"));
		editIcon = new ImageIcon(reference.getFullResourcePath(this, path
				+ "edit.png"));
	}

	/**
	 * Expects a png in the appropriate folder for each button
	 */
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		// Add Renderer resources
		Document d = listrenderer.getExpectedResourceStructure();
		tree.setRootElement(d.getRootElement());
		// Add own resources
		for (String file : Arrays.asList("new", "delete", "edit")) {
			tree.addPath("graphics/LanguageListView/" + file + ".png");
		}
		return tree;
	}

}
