package fs.polyglot.view;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventObject;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;

import org.dom4j.Document;

import fs.event.DataRetrievalListener;
import fs.gui.SwitchIconLabel;
import fs.polyglot.validate.TabooValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * Implements the component which is used for editing cells in a variant table. 
 * @author hampe
 *
 */
public class VariantTableEditor extends JPanel implements TableCellEditor, ResourceDependent {

	/**
	 * compiler-generated version id 
	 */
	private static final long serialVersionUID = 4549677866383774748L;
	
	//Resources
	private ResourceReference resource;
	private PolyglotStringLoader loader;
	private String languageID;
	private static String sgroup = "fs.polyglot.VariantTableEditor";
	
	//Components
	private SwitchIconLabel label;
	private JTextField entryField;
	
	//Icon
	private ImageIcon warn;
	
	//Data
	private HashSet<String> tabooList;
	
	//Listeners
	//private HashSet<DataRetrievalListener> listeners = new HashSet<DataRetrievalListener>();
	private HashSet<CellEditorListener> editorlisteners = new HashSet<CellEditorListener>();
	
	private TabooValidator languageValidator;
	
	private KeyListener entryListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			super.keyPressed(e);
			//If ENTER is pressed and the content is valid, notify of an edit stop
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				stopCellEditing();
			}
			//Id ESCAPE is pressed, 
			if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				cancelCellEditing();
			}
		}	
	};
	
	// CONSTRUCTOR ******************************************************
	// ******************************************************************
	
	/**
	 * Constructs an editor, that is a panel containing a SwitchIconLabel and a text field.  Edit stops are allowed /disallowed according to the validation status. 
	 * @param taboos If this is null, every possible content is valid content. Otherwise the content may not be empty and not be contained in this list
	 */
	public VariantTableEditor(HashSet<String> taboos, ResourceReference r, PolyglotStringLoader l, String lang) {
		super();
		assignReference(r);
		loader = l != null ? l : PolyglotStringLoader.getDefaultLoader();
		languageID = lang != null? lang : PolyglotStringTable.getGlobalLanguageID();
		tabooList = taboos == null? taboos : new HashSet<String>(taboos);
		
		//Init components
		add(label); add(entryField);
		languageValidator = new TabooValidator(null,warn,warn,tabooList,false) {
			@Override
			public Result validate(JTextComponent component) {
				Result r = super.validate(component);
				//Set Tooltips
				switch(r) {
				case INCORRECT: if(component.getText().trim().equals("")) setToolTipText(component,loader.getString(sgroup + ".nonempty", languageID));
								else setToolTipText(component, loader.getString(loader + ".existant", languageID)); break;
				default: setToolTipText(component, null);
				}
				return r;
			}			
		};
		entryField.addKeyListener(entryListener);
		
	}
	
	// RESOURCEDEPENDENT METHODS ****************************************
	// ******************************************************************
	
	/**
	 * Assigns a reference (if null, uses the default reference) and reloads the icon
	 */
	@Override
	public void assignReference(ResourceReference r) {
		resource = r != null? r : FsfwDefaultReference.getDefaultReference();
		warn = new ImageIcon(resource.getFullResourcePath(this, "graphics/VariantTableEditor/warn.png"));
	}

	/**
	 * Expects the icon warn.png in its graphics directory
	 */
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/VariantTableEditor/warn.png");
		return tree;
	}

	// LISTENER METHODS ********************************************
	// *************************************************************
	
//	public void addDataRetrievalListener(DataRetrievalListener l) {
//		if(l != null) listeners.add(l);
//	}
//	
//	public void removeDataRetrievalListener(DataRetrievalListener l) {
//		listeners.remove(l);
//	}
//	
//	protected void fireDataReady(Object source, Object data) {
//		for(DataRetrievalListener l : listeners) l.dataReady(source, data);
//	}
	
	protected void fireEditingCanceled(ChangeEvent e) {
		for(CellEditorListener l: editorlisteners) l.editingCanceled(e);
	}
	
	protected void fireEditingStopped(ChangeEvent e) {
		for(CellEditorListener l : editorlisteners) l.editingStopped(e);
	}

	// INTERFACE METHODS *******************************************
	// *************************************************************
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		//Reset values
		entryField.setText("");
		entryField.requestFocusInWindow();
		label.setIconVisible(false);
		//If necessary, activate validation
		if(row == 0) {
			languageValidator.addComponent(entryField, label);
			languageValidator.validate();
		}
		else languageValidator.removeComponent(entryField);
		return this;
	}

	@Override
	public void addCellEditorListener(CellEditorListener l) {
		if(l!= null) editorlisteners.add(l);		
	}

	@Override
	public void cancelCellEditing() {
		fireEditingCanceled(new ChangeEvent(this));
	}

	@Override
	public Object getCellEditorValue() {
		return entryField.getText();
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}

	@Override
	public void removeCellEditorListener(CellEditorListener l) {
		editorlisteners.remove(l);		
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}

	@Override
	public boolean stopCellEditing() {
		if(languageValidator.validate().getOverallResult() != Result.INCORRECT) {
			fireEditingStopped(new ChangeEvent(this));
			return true;
		}
		return false;
	}
	
	
	
}
