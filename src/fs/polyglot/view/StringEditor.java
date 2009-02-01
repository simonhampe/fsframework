package fs.polyglot.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.text.JTextComponent;

import org.dom4j.Document;

import fs.gui.FrameworkDialog;
import fs.gui.SwitchIconLabel;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.validate.NonEmptyWarner;
import fs.test.XMLDirectoryTest;
import fs.validate.LabelIndicValidator;
import fs.validate.SingleButtonValidator;
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
	private ListSelectionModel selection;
	private StringEditorConfiguration configuration;
	private static String sgroup = "fs.polyglot.StringEditor";
	private String currentEdit = null;
	
	//Validation
	private NonEmptyWarner groupWarner;
	private LabelIndicValidator<JTextField> stringValidator;
	private ValidationValidator summary = new SingleButtonValidator(ok);
	
	/**
	 * This creates a string editor. If singleStringID is not null, this creates an editor for a single string, with navigational controls
	 * disabled. All configuration parameters are then ignored. If it is null, the editor will create a list of editable strings from the
	 * configuration data and fill all fields with the data of the first one. Navigational controls are activated, as well as Quick Navigation.
	 * @param associatedTable The associated Table from which the data should be retrieved. If it is null, a null pointer exception is thrown
	 * @param selectedStrings A list model from which to retrieve a list of selected strings. If only a single string is to be edited, this parameter is ignored
	 * @param singleStringID Should be null, if the strings to be edited should be retrieved from the configuration data. Otherwise this stringID is edited and its data
	 * retrieved from the table model
	 * @param configuration The Configuration data. This specifies, which strings are to be edited. Usually this data is created by a StringEditorConfigurator.
	 */
	public StringEditor(ResourceReference r, PolyglotStringLoader l, String lid, PolyglotTableModel associatedTable, ListSelectionModel selectedStrings, String singleStringID, StringEditorConfiguration configuration) {
		super(r, l, lid);
		
		//Init all member components
		textID = new JTextField();
		textGroup = new JTextField();
		checkGenerateID = new JCheckBox(loader.getString(sgroup + ".generateid", languageID), true);
		checkQuickNav = new JCheckBox(loader.getString(sgroup + ".quicknav", languageID),singleStringID == null); checkQuickNav.setEnabled(singleStringID == null);
		generatedID = new JLabel("");
			generatedID.setBorder(BorderFactory.createEtchedBorder());
		tableVariants = new JTable(5,5);
		jumpto = new JComboBox();
		previous = new JButton("<-");
		next = new JButton("->");
		config = new JButton(loader.getString(sgroup + ".config", languageID));
		ok = new JButton(loader.getString("fs.global.ok", languageID));
		cancel = new JButton(loader.getString("fs.global.cancel", languageID));
		
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
			line1.add(labelID); line1.add(textID); line1.add(labelGroup); line1.add(textGroup);
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
		
		groupWarner = new NonEmptyWarner(null, warnIcon, null) {
			@Override
			public Result validate(JTextComponent component) {
				Result r = super.validate(component);
				if(r == Result.WARNING) {
					setToolTipText(component, loader.getString(sgroup + ".groupwarn", languageID));
				}
				else setToolTipText(component, null);
				return r;
			}
		};
		groupWarner.addComponent(textGroup, labelGroup);
		groupWarner.validate();
		
		
	}

	/**
	 * If only a single string is edited by this instance, this has no effect. Otherwise, the list of edited strings and languages is reloaded. If 
	 * possible, the current selection is left unchanged and the last edit is performed before the update, if possible.
	 */
	public void updateData() {
		
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
