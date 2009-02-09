package fs.polyglot.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.dom4j.Document;

import fs.gui.FrameworkDialog;
import fs.gui.SwitchIconLabel;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.model.VariantTableModel;
import fs.polyglot.validate.NonEmptyWarner;
import fs.test.XMLDirectoryTest;
import fs.validate.LabelIndicValidator;
import fs.validate.SingleButtonValidator;
import fs.validate.ValidationResult;
import fs.validate.ValidationValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This class represents the most important tool for editing strings in a PolyglotStringTable. 
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
	private JTable tableVariants;
	private JComboBox jumpto;
	private JButton previous;
	private JButton next;
	private JButton config;
	private JButton ok;
	private JButton cancel;
	
	private ImageIcon warnIcon;
	
	//Data
	private PolyglotTableModel table;
	private HashSet<String> selected = new HashSet<String>();
	private StringEditorConfiguration configuration;
	private ArrayList<String> edits = new ArrayList<String>();
	private int currentEdit;
	private String singleEditString = null;
	
	private static String sgroup = "fs.polyglot.StringEditor";
	
	//Validation
	private LabelIndicValidator<JTextField> groupWarner;
	private LabelIndicValidator<JTextField> stringValidator;
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
	 */
	public StringEditor(ResourceReference r, PolyglotStringLoader l, String lid, PolyglotTableModel associatedTable, HashSet<String> selectedStrings, String singleStringID, StringEditorConfiguration configuration) {
		super(r, l, lid);
		
		//Copy data
		if(associatedTable== null) throw new NullPointerException("Can't create editor for null table");
		table = associatedTable;
		singleEditString = singleStringID;
		this.configuration = configuration != null ? configuration : new StringEditorConfiguration(); 
		this.selected = selectedStrings == null? null : new HashSet<String>(selectedStrings); 
		
		//Init all member components
		textID = new JTextField();
		textGroup = new JTextField();
		checkGenerateID = new JCheckBox(loader.getString(sgroup + ".generateid", languageID), true);
		checkQuickNav = new JCheckBox(loader.getString(sgroup + ".quicknav", languageID),singleStringID == null); checkQuickNav.setEnabled(singleStringID == null);
		checkGroup = new JCheckBox();
			checkGroup.setSelected(table.getGroupID(singleStringID) != null);
			checkGroup.addChangeListener(checkGroupListener);
		generatedID = new JLabel("");
			generatedID.setBorder(BorderFactory.createEtchedBorder());
		tableVariants = new JTable();
		jumpto = new JComboBox();
		previous = new JButton("<-");
		next = new JButton("->");
		config = new JButton(loader.getString(sgroup + ".config", languageID));
		ok = new JButton(loader.getString("fs.global.ok", languageID));
		cancel = new JButton(loader.getString("fs.global.cancel", languageID));
		
		//Load data
		updateData();
				
		//Init additional components
		SwitchIconLabel labelID = new SwitchIconLabel(loader.getString(sgroup + ".stringid", languageID));
		labelID.setIconReference(warnIcon);
		SwitchIconLabel labelGroup = new SwitchIconLabel(loader.getString(sgroup + ".groupid", languageID));
		labelGroup.setIconReference(warnIcon);
		JLabel labelTable = new JLabel(loader.getString(sgroup + ".variants", languageID));
		JLabel labelJump = new JLabel(loader.getString(sgroup + ".jumpto", languageID));
		JScrollPane tablePane = new JScrollPane(tableVariants) {
			/**
			 * compiler-generated version id
			 */
			private static final long serialVersionUID = 3430793500371203460L;

			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				return new Dimension(d.width, tableVariants.getPreferredSize().height*2);
			}
		};
		
		
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
			line2.add(checkGenerateID);line2.add(generatedID);
		Box line3 = new Box(BoxLayout.X_AXIS);
		line3.setAlignmentX(LEFT_ALIGNMENT);
			line3.add(labelTable);
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
		lineBox.add(line1);lineBox.add(line2);lineBox.add(line3); 
		lineBox.add(line4); lineBox.add(line5); lineBox.add(line6);
		lineBox.add(hfill2);
		lineBox.add(line7);
		add(lineBox);
		pack();
		
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
		summary.addValidator(groupWarner);
		summary.addValidator(stringValidator);
		summary.validate();
		
		
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
			return;
		}
		//Otherwise calculate edit list
		String currentEditID = (edits == null || edits.size() == 0) ? null : edits.get(currentEdit);
		TreeSet<String> sortedIDs = new TreeSet<String>(table.getIDList());
		//Load strings and remove unused strings
		for(String id : sortedIDs) {
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
			tableVariants.setModel(new VariantTableModel("",table,loader,languageID));
			jumpto.setModel(new DefaultComboBoxModel(new Vector<String>()));
		}
		//Insert strings
		else {
			tableVariants.setModel(new VariantTableModel(edits.get(currentEdit),table,loader,languageID));
			jumpto.setModel(new DefaultComboBoxModel(new Vector<String>(edits)));
			//TODO: Continue...
		}
		
	}
	
	/**
	 * If the entries are valid, effects all changes
	 */
	protected void applyData() {
		//If the entries are not valid, return
		if(summary.validate().getOverallResult() == Result.INCORRECT) return;
		//Change string ID, if necessary
		String newID = textID.getText();
		if(!textID.getText().equals(edits.get(currentEdit))) {
			table.renameString(edits.get(currentEdit), textID.getText());
		}
		//Set Group
		table.setGroupID(newID, checkGroup.isSelected() ? textGroup.getText() : null);
		//Set Variants (we wan't to reduce operations to a mininum)
		//TODO: Finish
		
		
	}
	
	/**
	 * Returns the final string id depending on the state of the checkbox 'Generate ID...'. i.e. either the value of String ID or
	 * Group + '.' + StringID
	 * @return
	 */
	public String getFinalID() {
		return checkGenerateID.isSelected() ? textGroup.getText() + "." + textID.getText() : textID.getText();
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
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/StringEditor/warn.png");
		return tree;
	}
	
	
	
	
}
