package fs.polyglot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Paper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;

import org.dom4j.Document;

import fs.event.DataRetrievalListener;
import fs.event.DocumentChangeFlag;
import fs.gui.FrameworkDialog;
import fs.gui.SwitchIconLabel;
import fs.polyglot.model.Language;
import fs.polyglot.model.PolyglotString;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.model.Variant;
import fs.polyglot.model.VariantTableModel;
import fs.polyglot.undo.UndoableEditFactory;
import fs.polyglot.undo.UndoablePolyglotStringEdit;
import fs.polyglot.undo.UndoableVariantEdit;
import fs.polyglot.validate.NonEmptyWarner;
import fs.test.XMLDirectoryTest;
import fs.validate.LabelIndicValidator;
import fs.validate.SingleButtonValidator;
import fs.validate.ValidationResult;
import fs.validate.ValidationValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This class represents the most important tool for editing strings in a PolyglotStringTable. It assumes that table data is only changed within this editor
 * while it is open, so modality should not be changed (by default it is application modal).
 * @author Simon Hampe
 *
 */
public class StringEditor extends FrameworkDialog implements ResourceDependent{

	//Components
	private JTextField textID;
	private JTextField textGroup;
	private JCheckBox checkGenerateID;
	private JCheckBox checkQuickNav;
	private JCheckBox checkGroup;
	private JLabel generatedID;
	private JScrollPane tablePane;
	private JTable tableVariants;
	private JComboBox jumpto;
	private JButton previous;
	private JButton next;
	private JButton config;
	private JButton ok;
	private JButton cancel;
	
	
	private ImageIcon warnIcon;
	private ImageIcon deleteIcon;
	
	//Data
	private PolyglotTableModel table;
	private VariantTableModel model;
	private HashSet<String> selected = new HashSet<String>();
	private StringEditorConfiguration configuration;
	private ArrayList<String> edits = new ArrayList<String>();
	private int currentEdit;
	private String singleEditString = null;
	
	private static String sgroup = "fs.polyglot.StringEditor";
	
	private UndoableEditFactory editFactory;
	
	//Validation
	private LabelIndicValidator<JTextField> groupWarner;
	private LabelIndicValidator<JTextField> stringValidator;
	private LabelIndicValidator<JTable> tableValidator;
	private ValidationValidator summary;
	
	// EVENT HANDLING ********************************************
	// ***********************************************************
	
	//Changes the enables-status of textGroup according to the check box
	private ChangeListener checkGroupListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			textGroup.setEnabled(checkGroup.isSelected());
		}
	};
	
	//Updates the 'final ID' field, if the check box for generating the id is changed
	private ChangeListener checkGenerateListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			setGeneratedID(getFinalID());
			repaint();
		}
	};
	
	//Registers any changes to the checkbox 'generate id'
	private DocumentListener checkGenerateEditListener = new DocumentListener() {
		@Override
		public void changedUpdate(DocumentEvent e) { update(); }
		@Override
		public void insertUpdate(DocumentEvent e) { update(); }
		@Override
		public void removeUpdate(DocumentEvent e) { update(); }
		private void update() {
			setGeneratedID(getFinalID());
			repaint();
		}
	};
	
	//Takes care of clicks on 'next' and 'previous' and 'jumpto'
	private ActionListener jumpListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			//We only do this, if there are any edits to jump to
			if(edits.size() > 0) {
				if(e.getSource() == previous) {
					selectID(currentEdit == 0? edits.get(edits.size()-1) : edits.get(currentEdit-1));
				}
				if(e.getSource() == next) {
					selectID((currentEdit == edits.size() -1) ? edits.get(0) : edits.get(currentEdit+1));
				}
				if(e.getSource() == jumpto) {
					selectID(jumpto.getSelectedItem().toString());
				}
			}
		}
	};
	
	//Takes care of ok and cancel
	private Action disposalListener = new AbstractAction() {
		/**
		 * Compiler-generated version id
		 */
		private static final long serialVersionUID = -7756862192859052931L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == ok) {
				//Apply changes, if necessary
				if(flag.hasBeenChanged()) {
					applyData();
				}
			}
			dispose();
		}
	};
	
	//Listens to the config button
	private ActionListener configListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			configure();
		}
	};
	
	//Registers any changes to the current edit
	private DocumentChangeFlag flag = new DocumentChangeFlag();
	
	// CONSTRUCTOR ***********************************************
	// ***********************************************************
	
	/**
	 * This creates a string editor. If singleStringID is not null, this creates an editor for a single string, with navigational controls
	 * disabled. All configuration parameters are then ignored. If it is null, the editor will create a list of editable strings from the
	 * configuration data and fill all fields with the data of the first one. Navigational controls are activated, as well as Quick Navigation.
	 * @param associatedTable The associated Table from which the data should be retrieved. If it is null, a null pointer exception is thrown
	 * @param selectedStrings A list of selected strings. If only a single string is to be edited, this parameter is ignored
	 * @param singleStringID Should be null, if the strings to be edited should be retrieved from the configuration data. Otherwise this stringID is edited and its data
	 * retrieved from the table model
	 * @param configuration The Configuration data. This specifies, which strings are to be edited. Usually this data is created by a StringEditorConfigurator. If null, default values are used
	 * @param manager The UndoManager used to register the edits performed by this editor.
	 */
	public StringEditor(ResourceReference r, PolyglotStringLoader l, String lid, PolyglotTableModel associatedTable, HashSet<String> selectedStrings, String singleStringID, StringEditorConfiguration configuration, UndoManager manager) {
		super(r, l, lid);
		
		//Copy data
		if(associatedTable== null) throw new NullPointerException("Can't create editor for null table");
		table = associatedTable;
		singleEditString = singleStringID;
		this.configuration = configuration != null ? configuration : new StringEditorConfiguration(); 
		this.selected = selectedStrings == null? null : new HashSet<String>(selectedStrings); 
		editFactory = new UndoableEditFactory(table,loader,languageID,manager);
		
		//Init all membercomponents
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle(loader.getString(sgroup + ".title",languageID));
		textID = new JTextField();
		textGroup = new JTextField();
			textID.getDocument().addDocumentListener(checkGenerateEditListener);
			textID.getDocument().addDocumentListener(flag);
			textGroup.getDocument().addDocumentListener(checkGenerateEditListener);
			textGroup.getDocument().addDocumentListener(flag);
		checkGenerateID = new JCheckBox(loader.getString(sgroup + ".generateid", languageID), true);
			checkGenerateID.addChangeListener(checkGenerateListener);
			checkGenerateID.addChangeListener(flag);
		checkQuickNav = new JCheckBox(loader.getString(sgroup + ".quicknav", languageID),singleStringID == null); 
			checkQuickNav.setEnabled(singleStringID == null);
		checkGroup = new JCheckBox();
			checkGroup.addChangeListener(checkGroupListener);
				checkGroupListener.stateChanged(null);
			checkGroup.addChangeListener(checkGenerateListener);
			checkGroup.addChangeListener(flag);
		generatedID = new JLabel("");
			generatedID.setForeground(new Color(0,0,255));
			checkGenerateEditListener.changedUpdate(null);
		tableVariants = new JTable();
			tableVariants.getTableHeader().setReorderingAllowed(false);
		jumpto = new JComboBox();
		previous = new JButton("<-");
			previous.addActionListener(jumpListener);
		next = new JButton("->");
			next.addActionListener(jumpListener);
		config = new JButton(loader.getString(sgroup + ".config", languageID));
			config.addActionListener(configListener);
		ok = new JButton(loader.getString("fs.global.ok", languageID));
		cancel = new JButton(loader.getString("fs.global.cancel", languageID));
		
		getRootPane().setDefaultButton(ok);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "dispose");
		getRootPane().getActionMap().put("dispose", disposalListener);
		ok.addActionListener(disposalListener);
		cancel.addActionListener(disposalListener);
		
		tablePane = new JScrollPane(tableVariants) {
			/**
			 * compiler-generated version id
			 */
			private static final long serialVersionUID = 3430793500371203460L;

			private JTable mesureTable = new JTable(6,3);
			
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				return new Dimension(d.width, mesureTable.getPreferredSize().height);
			}
		};		
		
		//Load data
		updateData();
		
		jumpto.addActionListener(jumpListener);
		previous.setEnabled(singleEditString == null);
		next.setEnabled(singleEditString == null);
		jumpto.setEnabled(singleEditString == null);
		
		//Init additional components
		SwitchIconLabel labelID = new SwitchIconLabel(loader.getString(sgroup + ".stringid", languageID));
		labelID.setIconReference(warnIcon);
		SwitchIconLabel labelGroup = new SwitchIconLabel(loader.getString(sgroup + ".groupid", languageID));
		labelGroup.setIconReference(warnIcon);
		SwitchIconLabel labelValidTable = new SwitchIconLabel("");
			labelValidTable.setIconReference(warnIcon);
		JLabel labelTable = new JLabel(loader.getString(sgroup + ".variants", languageID));
		JLabel labelJump = new JLabel(loader.getString(sgroup + ".jumpto", languageID));
				
		
		//Layout
		Box lineBox = new Box(BoxLayout.Y_AXIS); //The box for all lines
		Box hfill = new Box(BoxLayout.X_AXIS);
		hfill.add(Box.createRigidArea(new Dimension(5,5)));
		Box hfill2 = new Box(BoxLayout.X_AXIS);
		hfill2.add(Box.createRigidArea(new Dimension(5,5)));
		Box line1 = new Box(BoxLayout.X_AXIS);
		line1.setAlignmentX(LEFT_ALIGNMENT);
			line1.add(labelID); line1.add(textID); line1.add(labelGroup); line1.add(textGroup);line1.add(checkGroup);
		Box line2 = new Box(BoxLayout.X_AXIS);
		line2.setAlignmentX(LEFT_ALIGNMENT);
			line2.add(checkGenerateID);
		Box line2b = new Box(BoxLayout.X_AXIS);
		line2b.setAlignmentX(LEFT_ALIGNMENT);
			line2b.add(Box.createHorizontalGlue());line2b.add(generatedID);line2b.add(Box.createHorizontalGlue());
		Box line3 = new Box(BoxLayout.X_AXIS);
		line3.setAlignmentX(LEFT_ALIGNMENT);
			line3.add(labelTable);line3.add(labelValidTable);
		Box line4 = new Box(BoxLayout.X_AXIS);
		line4.setAlignmentX(LEFT_ALIGNMENT);
			line4.add(tablePane);
		Box line5 = new Box(BoxLayout.X_AXIS);
		line5.setAlignmentX(LEFT_ALIGNMENT);
			line5.add(checkQuickNav);
		Box line6 = new Box(BoxLayout.X_AXIS);
		line6.setAlignmentX(LEFT_ALIGNMENT);
			line6.add(Box.createHorizontalGlue());line6.add(previous); line6.add(config); line6.add(ok); line6.add(cancel); line6.add(next);
			line6.add(Box.createHorizontalGlue());
		Box line7 = new Box(BoxLayout.X_AXIS);
		line7.setAlignmentX(LEFT_ALIGNMENT);
			line7.add(labelJump); line7.add(jumpto);line7.add(Box.createHorizontalGlue());
			
		lineBox.add(hfill);	
		lineBox.add(line1);lineBox.add(line2);lineBox.add(line2b);
		lineBox.add(line3); 
		lineBox.add(line4); lineBox.add(line5); lineBox.add(line6);
		lineBox.add(hfill2);
		lineBox.add(line7);
		add(lineBox);
		
		pack();
		setResizable(false);
		
		//Init Validation
		
		groupWarner = new LabelIndicValidator<JTextField>(null, warnIcon, null) {
			@Override
			public Result validate(JTextField component) {
				if(textGroup.getText().trim().equals("") && checkGroup.isSelected()) {
					setToolTipText(component, loader.getString(sgroup + ".groupwarn", languageID));
					return Result.WARNING;
				}
				else {
					setToolTipText(component, null);
					return Result.CORRECT;
				}
			}
			@Override
			protected void registerToComponent(JTextField component) {
				textGroup.getDocument().addDocumentListener(this);
				checkGroup.addChangeListener(this);
				
			}
			@Override
			protected void unregisterFromComponent(JTextField component) {
				textGroup.getDocument().removeDocumentListener(this);
				checkGroup.removeChangeListener(this);
			}
		};
		stringValidator = new LabelIndicValidator<JTextField>(null, warnIcon, warnIcon) {
			@Override
			protected void registerToComponent(JTextField component) {
				if(component != null) {
					component.getDocument().addDocumentListener(this);
					checkGenerateID.addChangeListener(this);
				}
			}
			@Override
			protected void unregisterFromComponent(JTextField component) {
				if(component != null) {
					component.getDocument().removeDocumentListener(this);
					checkGenerateID.removeChangeListener(this);
				}
			}
			@Override
			public Result validate(JTextField component) {
				if(component == null) return Result.CORRECT;
				//The empty ID is not allowed
				if(component.getText().trim().equals("")) {
					setToolTipText(component, loader.getString(sgroup + ".stringerror", languageID));
					return Result.INCORRECT;
				}
				//If it exists already, issue a warning
				String finalid = getFinalID();
				if(!finalid.equals(edits.get(currentEdit)) && table.containsStringID(finalid)) {
					setToolTipText(component, loader.getString(sgroup + ".stringwarn", languageID));
					return Result.WARNING;
				}
				setToolTipText(component, null);
				return Result.CORRECT;
			}
		};
		tableValidator = new LabelIndicValidator<JTable>(null, warnIcon, warnIcon) {
			private TableModelListener listener = new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					fireStateChanged(new ChangeEvent(e.getSource()));
				}
			};
			@Override
			protected void registerToComponent(JTable component) {
				component.getModel().addTableModelListener(listener);
			}
			@Override
			protected void unregisterFromComponent(JTable component) {
				component.getModel().removeTableModelListener(listener);
			}
			@Override
			public Result validate(JTable component) {
				ArrayList<String> languageList = new ArrayList<String>();
				HashSet<String> originalList = new HashSet<String>();
				try {
					languageList = ((VariantTableModel)tableVariants.getModel()).getLanguageList();
					originalList = ((VariantTableModel)tableVariants.getModel()).getOriginalLanguageList();
				}
				catch(ClassCastException ce) {
					throw new UnsupportedOperationException("Can't load language id list from variant table. It seems this listener was used for a wrong table model.");
				}
				Result r = Result.CORRECT;
				String tooltip = null;
				//Check for variants that were not originally displayed, but exist already
				for(String l : languageList) {
					if(table.getSupportedLanguages(edits.get(currentEdit)).contains(l) && !originalList.contains(l)) {
						r = Result.WARNING;
						if(tooltip == null) tooltip = "<html>- ";
						else tooltip += "<br>- ";
						tooltip += loader.getString(sgroup + ".overwritevariant", languageID, l);
					}
				}
				//Check for doublets
				if((new HashSet<String>(languageList).size() < languageList.size())) {
					if(tooltip == null) tooltip = "<html>- ";
					else tooltip += "<br>- ";
					tooltip += loader.getString(sgroup + ".doublevariant", languageID);
					r = Result.INCORRECT;
				}
				//Check for empty language ids
				for(String l : languageList) {
					if(l.trim().equals("")) {
						r = Result.INCORRECT;
						if(tooltip == null) tooltip = "<html>- ";
						else tooltip += "<br>- ";
						tooltip += loader.getString(sgroup + ".emptylanguage", languageID);
						break;
					}
				}
				if(tooltip != null) tooltip += "</html>";
				//Set tooltips
				setToolTipText(component, tooltip);
				return r;
			}
		};
		//Dis/En-ables Ok-Button and changes tooltips
		summary = new ValidationValidator() {
			@Override
			public void validationPerformed(ValidationResult result) {
				boolean enable = true;
				switch(result.getOverallResult()) {
				case INCORRECT: enable = false; break;
				case CORRECT:
				case WARNING: enable = true;break;
				}
				ok.setEnabled(enable);
				String nexttooltip = loader.getString(sgroup + (enable? ".switchvalid" : ".switchcancel"), languageID, "next");
				String prevtooltip = loader.getString(sgroup + (enable? ".switchvalid" : ".switchcancel"), languageID, "previous");
				String jumptooltip = loader.getString(sgroup + (enable? ".switchvalid" : ".switchcancel"), languageID, "selected");
				next.setToolTipText(nexttooltip);
				previous.setToolTipText(prevtooltip);
				jumpto.setToolTipText(jumptooltip);
			}
		};
		groupWarner.addComponent(textGroup, labelGroup);
		stringValidator.addComponent(textID, labelID);
		tableValidator.addComponent(tableVariants, labelValidTable);
		summary.addValidator(groupWarner);
		summary.addValidator(stringValidator);
		summary.addValidator(tableValidator);
		summary.validate();
		
		flag.setChangeFlag(false);		
	}

	// CONTROL METHODS ************************************************************************************
	// ****************************************************************************************************
	
	/**
	 * Shows a configuration dialog (after a confirmation dialog, if any changes were made) and configures the editor accordingly.
	 */
	protected void configure() {
		//Ask for confirmation if valid changes have been made
		if(flag.hasBeenChanged() && summary.validate().getOverallResult() != Result.INCORRECT) {	
			int answer = JOptionPane.showConfirmDialog(this, loader.getString(sgroup + ".confirmconfig", languageID), 
					loader.getString(sgroup + ".titleconfirm", languageID), JOptionPane.YES_NO_CANCEL_OPTION);
			switch(answer) {
			case JOptionPane.YES_OPTION:
				applyData(); break;
			case JOptionPane.CANCEL_OPTION: return;
			}
		}
		StringEditorConfigurator configdialog = new StringEditorConfigurator(resource,loader,languageID,table,configuration);
		configdialog.addDataRetrievalListener(new DataRetrievalListener() {
			@Override
			public void dataReady(Object source, Object data) {
				StringEditorConfiguration newconfig = (StringEditorConfiguration) data;
				configuration = newconfig;
				updateData();
				insertData();
			}
		});	
		configdialog.setModalityType(ModalityType.APPLICATION_MODAL);
		configdialog.setVisible(true);
	}
	
	/**
	 * Selects the specified ID (if it doesn't exist, won't change the currently selected id). In any case applies the current changes, if they are valid. 
	 */
	protected void selectID(String id) {
		if(flag.hasBeenChanged()) applyData();
		updateData();
		if(edits.contains(id)) {
			currentEdit = edits.indexOf(id);
			insertData();
		}
	}

	/**
	 * If only a single string is edited by this instance, this has no effect. Otherwise, the list of edited strings and languages is loaded. If 
	 * possible, the current selection is left unchanged and the last edit is performed before the update, if possible.
	 */
	public void updateData() {
		//If we only edit a single string, we don't change anything
		if(singleEditString != null) {
			edits = new ArrayList<String>(Arrays.asList(singleEditString));
			currentEdit = 0;
			insertData();
			return;
		}
		//Otherwise calculate edit list
		String currentEditID = (edits == null || edits.size() == 0) ? null : edits.get(currentEdit);
		TreeSet<String> sortedIDs = new TreeSet<String>(table.getIDList());
		TreeSet<String> sortedIDsCopy = new TreeSet<String>(sortedIDs);
		//Load strings and remove unused strings
		for(String id : sortedIDsCopy) {
			//only incomplete?
			if(configuration.editOnlyIncomplete && table.isCompleteString(id)) sortedIDs.remove(id);
			//only selected?
			if(configuration.editOnlySelected && !selected.contains(id)) sortedIDs.remove(id);
		}
		//We don't have to change anything, if the current edit is still contained
		edits = new ArrayList<String>(sortedIDs);
		if(edits.contains(currentEditID)) currentEdit = edits.indexOf(currentEditID);
		else {
			currentEdit = 0;
			insertData();
		}
	}
	
	/**
	 * Inserts the data of the current edit in the dialog components
	 */
	protected void insertData() {
		//Clear everything
		textID.setText(""); textGroup.setText("");
		//If there are no strings, there is nothing to insert
		if(edits.size() == 0) {
			model = new VariantTableModel("",configuration,table,loader,languageID);
			jumpto.setModel(new DefaultComboBoxModel(new Vector<String>()));
		}
		//Insert strings
		else {
			model = new VariantTableModel(edits.get(currentEdit),configuration,table,loader,languageID);
			jumpto.setModel(new DefaultComboBoxModel(new Vector<String>(edits)));
			jumpto.setSelectedIndex(currentEdit);
			//Extract string name
			String cutString = PolyglotStringTable.cutGroupPath(edits.get(currentEdit));
			if(cutString.startsWith(".")) cutString = cutString.substring(1);
			textID.setText(checkGenerateID.isSelected()? cutString: edits.get(currentEdit));
			textGroup.setText(table.getGroupID(edits.get(currentEdit)));
		}
		tableVariants.setModel(model);
		model.addTableModelListener(flag);
		tableVariants.getColumnModel().getColumn(2).setCellRenderer(new ButtonEditor());
		tableVariants.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor());
		
		//Set check boxes and so on
		checkGroup.setSelected(edits.size() == 0? true : table.getGroupID(edits.get(currentEdit)) != null);
		
		//Now resize table columns
		TableColumnModel cm = tableVariants.getColumnModel();
		tableVariants.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		cm.getColumn(2).setPreferredWidth(10);
		cm.getColumn(1).setPreferredWidth(
				tablePane.getPreferredSize().width-
					cm.getColumn(2).getPreferredWidth() - cm.getColumn(0).getPreferredWidth());
		cm.getColumn(1).setResizable(false);
		cm.getColumn(2).setResizable(false); 
		repaint();
		//Reset change log
		flag.setChangeFlag(false);
	}
	
	/**
	 * If the entries are valid, applies all changes and resets the change flag
	 */
	protected void applyData() {
		String finalid = getFinalID();
		String grouppath = checkGroup.isSelected()? textGroup.getText(): null;
		//If the entries are not valid, return
		if(summary.validate().getOverallResult() == Result.INCORRECT) return;
		CompoundEdit edit = new CompoundEdit();
		//If there are no edits, a new entry is created
		if(edits.size() == 0) {
			//Create String
			UndoablePolyglotStringEdit stringadd = editFactory.createUndoablePolyglotStringEdit(null, new PolyglotString(grouppath,finalid,false));
			stringadd.redo();
			edit.addEdit(stringadd);
			//Add variants
			for(int i = 0; i < model.getLanguageList().size(); i++) {
				UndoableVariantEdit variantadd = editFactory.createUndoableVariantEdit(null, new Variant(grouppath, finalid, 
						new Language(model.getValueAt(i,0).toString(), "", false, 0), model.getValueAt(i, 1).toString()));
				variantadd.redo();
				edit.addEdit(variantadd);
			}
		}
		//Otherwise...
		else {
			//Compare edited id to new id and potentially rename and set group
			UndoablePolyglotStringEdit stringchange = editFactory.createUndoablePolyglotStringEdit(new PolyglotString(grouppath,edits.get(currentEdit),false), 
						new PolyglotString(grouppath,finalid,false));
			stringchange.redo();
			edit.addEdit(stringchange);
//			//Set group anyway
			//Change variants:
			HashSet<String> languages = new HashSet<String>(model.getLanguageList());
			HashSet<String> oldlanguages = model.getOriginalLanguageList();
			//Remove variants that are no longer present:
			for(String old : oldlanguages) {
				if(!languages.contains(old)){
					UndoableVariantEdit variantremove = editFactory.createUndoableVariantEdit(new Variant(grouppath,finalid,new Language(old,"",false,0),""),null);
					variantremove.redo();
					edit.addEdit(variantremove);
				}
			}
			//Put all variants
			for(int i = 0; i < languages.size(); i++) {
				UndoableVariantEdit variantchange = editFactory.createUndoableVariantEdit(null, new Variant(grouppath, finalid, 
						new Language(model.getValueAt(i,0).toString(), "", false, 0), model.getValueAt(i, 1).toString()));
				variantchange.redo();
				edit.addEdit(variantchange);
			}
		}
		edit.end();
		editFactory.postEdit(edit);
		flag.setChangeFlag(false);
	}
	
	/**
	 * Returns the final string id depending on the state of the checkbox 'Generate ID...'. i.e. either the value of String ID or
	 * Group + '.' + StringID
	 */
	public String getFinalID() {
		return checkGenerateID.isSelected() ? 
			(checkGroup.isSelected()? textGroup.getText() + "." + textID.getText() : textID.getText() ): 
			textID.getText();
	}

	/**
	 * Sets the content of the label 'generated id'. If id is empty or the null string, a space will be inserted
	 */
	protected void setGeneratedID(String id) {
		generatedID.setText((id == null | id.equals(""))? " " : id);
	}
	
	// RESOURCEDEPENDENT METHODS ********************************************************
	// **********************************************************************************
	
	/**
	 * Assigns a resource reference (default, if r == null)
	 */
	@Override
	public void assignReference(ResourceReference r) {
		super.assignReference(r);
		warnIcon = new ImageIcon(resource.getFullResourcePath(this, "graphics/StringEditor/warn.png"));
		deleteIcon = new ImageIcon(resource.getFullResourcePath(this, "graphics/StringEditor/delete.png"));
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/StringEditor/warn.png");
		tree.addPath("graphics/StringEditor/delete.png");
		return tree;
	}	
	
	// LOCAL CLASSES ********************************************************************
	// **********************************************************************************
	
	//A class for rendering the delete buttons in the table
	private class ButtonEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

		/**
		 * compiler-generated version id
		 */
		private static final long serialVersionUID = 2908728669582358571L;

		//The button which is returned as renderer / editor
		private JButton deleteButton = new JButton();

		//A renderer for the last cell
		private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
	
		public ButtonEditor() {
			deleteButton.setIcon(deleteIcon);
			deleteButton.setToolTipText(loader.getString(sgroup + ".deletevariant", languageID));
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			final int r = row;
			deleteButton.setAction(new AbstractAction() {
				/**
				 * compiler-generated version id
				 */
				private static final long serialVersionUID = 247392917500595587L;

				@Override
				public void actionPerformed(ActionEvent e) {
					model.removeRow(r);
					fireEditingStopped();
				}
			});
			return deleteButton;
		}

		@Override
		public Object getCellEditorValue() {
			return deleteButton;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return row != model.getRowCount() - 1 ? deleteButton : renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}		
		
	}
	
}
