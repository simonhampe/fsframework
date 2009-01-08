package fs.polyglot.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.undo.CannotRedoException;

import org.dom4j.Document;

import fs.event.DataRetrievalListener;
import fs.gui.FrameworkDialog;
import fs.gui.GUIToolbox;
import fs.gui.SwitchIconLabel;
import fs.polyglot.undo.UndoableEditFactory;
import fs.polyglot.undo.UndoableGroupEdit;
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
 * An editor for a group change. The old group path is indicated at the top and the new group path is given by the user. Furthermore the user can specify
 * if string ids bearing the old group path as prefix should be renamed and if subgroups should be moved as well. An invalid entry is detected, if a group
 * is supposed to be moved to a subgroup and subgroups are affected as well. <br>
 * Potential {@link DataRetrievalListener}s are notified with the new path as data and can retrieve the other values via getter methods.
 * @author Simon Hampe
 *
 */
public class GroupEditor extends FrameworkDialog implements ResourceDependent {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = -5005794553298843056L;
	//Components
	private SwitchIconLabel originalLabel = new SwitchIconLabel();
	private JLabel originalPath = new JLabel();
	private String originalPathAsString;
	private SwitchIconLabel newLabel = new SwitchIconLabel();
	private JRadioButton changeGroup = new JRadioButton();
	private JRadioButton noGroup = new JRadioButton();
	private JTextField newPath = new JTextField();
	private JLabel newTooltip = new JLabel();
	private JCheckBox renameIDs = new JCheckBox();
	private JCheckBox affectSubGroups = new JCheckBox();
	
	private JButton okButton = new JButton();
	private JButton cancelButton = new JButton();
	
	//Resources
	private String sgroup = "fs.polyglot.GroupEditor";
	private ImageIcon warnIcon;
	
	
	// LISTENERS ************************************************
	// **********************************************************
	
	private Action disposalListener = new AbstractAction() {
		/**
		 * compiler-generated version id
		 */
		private static final long serialVersionUID = -7671850910897169668L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == okButton) {
				fireDataReady(getNewPath());
			}
			dispose();
		}
	};
	
	// VALIDATION ***********************************************
	// **********************************************************
	
	private LabelIndicValidator<JTextField> newpathValidator;
	private LabelIndicValidator<JRadioButton> radioValidator;
	private ValidationValidator summary = new SingleButtonValidator(okButton);
	
	// CONSTRUCTOR **********************************************
	// **********************************************************
	
	/**
	 * Constructs a group editor
	 * @param path The path of the original group to be changed
	 * @param newpath The path of the new group (if null, nothing will be inserted)
	 * @param r Never used
	 * @param l The string loader for label texts and similar
	 * @param lid The language id for label texts and similar
	 */
	public GroupEditor(String path, String newpath, ResourceReference r, PolyglotStringLoader l, String lid) {
		super(r, l, lid);
		setTitle(loader.getString(sgroup + ".title", languageID));
		
		warnIcon = new ImageIcon(resource.getFullResourcePath(this, "graphics/GroupEditor/warn.png"));
		originalPathAsString = path;
		
		//Create GUI
		originalLabel.setText(loader.getString(sgroup + ".originalpath", languageID));
		originalPath.setText(path != null? path : "<html><i>" + loader.getString(sgroup + ".nullpath", languageID) + "</i></html>");
		newLabel.setText(loader.getString(sgroup + ".newpath", languageID));
		newPath.setText(newpath);
		changeGroup.setText(loader.getString(sgroup + ".changegroup", languageID));
		noGroup.setText(loader.getString(sgroup + ".nogroup", languageID));
		newTooltip.setText("<html>" + loader.getString(sgroup  + ".newtooltip1", languageID) + "<br>" + 
									  loader.getString(sgroup + ".newtooltip2", languageID) + "<br>" + 
									  loader.getString(sgroup + ".newtooltip3", languageID) + 
									  "</html>");
		newTooltip.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		renameIDs.setSelected(true);
		renameIDs.setText(loader.getString(sgroup + ".rename", languageID));
		renameIDs.setToolTipText(loader.getString(sgroup + ".renametip", languageID));
		affectSubGroups.setSelected(true);
		affectSubGroups.setText(loader.getString(sgroup + ".affectsubgroups", languageID));
		affectSubGroups.setToolTipText(loader.getString(sgroup + ".affectsubgroupstip", languageID));
		okButton.setText(loader.getString("fs.global.ok", languageID));
		cancelButton.setText(loader.getString("fs.global.cancel", languageID));
		ButtonGroup group = new ButtonGroup();
		group.add(changeGroup);
		group.add(noGroup);
		if(newpath != null) changeGroup.setSelected(true);
		else noGroup.setSelected(true);
		setResizable(false);
		
		//Layout
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gcOriginalLabel = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints gcOriginalPath = GUIToolbox.buildConstraints(1, 0, 2, 1);
		GridBagConstraints gcChangeGroup = GUIToolbox.buildConstraints(0, 1, 1, 1);
		GridBagConstraints gcNewLabel = GUIToolbox.buildConstraints(0, 2, 1, 1);
		GridBagConstraints gcNewPath = GUIToolbox.buildConstraints(1, 2, 2, 1);
		GridBagConstraints gcNoGroup = GUIToolbox.buildConstraints(0, 3, 1, 1);
		GridBagConstraints gcNewTooltip = GUIToolbox.buildConstraints(0, 4, 3, 1);
		GridBagConstraints gcRename = GUIToolbox.buildConstraints(0, 5, 1, 1);
		GridBagConstraints gcAffect = GUIToolbox.buildConstraints(0, 6, 1, 1);
		GridBagConstraints gcOk = GUIToolbox.buildConstraints(1, 7, 1, 1);
		gcOk.weightx = 50;
		GridBagConstraints gcCancel = GUIToolbox.buildConstraints(2, 7, 1, 1);
		gcCancel.weightx = 50;
		gbl.setConstraints(originalLabel, gcOriginalLabel);
		gbl.setConstraints(originalPath, gcOriginalPath);
		gbl.setConstraints(changeGroup, gcChangeGroup);
		gbl.setConstraints(newLabel, gcNewLabel);
		gbl.setConstraints(newPath, gcNewPath);
		gbl.setConstraints(noGroup, gcNoGroup);
		gbl.setConstraints(newTooltip, gcNewTooltip);
		gbl.setConstraints(renameIDs, gcRename);
		gbl.setConstraints(affectSubGroups, gcAffect);
		gbl.setConstraints(okButton, gcOk);
		gbl.setConstraints(cancelButton, gcCancel);
		
		//Add
		for(JLabel label : Arrays.asList(originalLabel,originalPath, newLabel, newTooltip)) add(label);
		add(changeGroup); add(newPath); add(noGroup);
		add(renameIDs);add(affectSubGroups);
		add(okButton);add(cancelButton);
		
		pack();
		newPath.requestFocusInWindow();
		
		//Event handling
		getRootPane().setDefaultButton(okButton);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "dispose");
		getRootPane().getActionMap().put("dispose", disposalListener);
		okButton.addActionListener(disposalListener);
		cancelButton.addActionListener(disposalListener);
		
		//Validation
		
		newpathValidator = new LabelIndicValidator<JTextField>(null, warnIcon, warnIcon) {
			@Override
			protected void registerToComponent(JTextField component) {
				component.getDocument().addDocumentListener(this);
				changeGroup.addChangeListener(this);
				affectSubGroups.addChangeListener(this);
			}
			@Override
			protected void unregisterFromComponent(JTextField component) {
				component.getDocument().removeDocumentListener(this);
				changeGroup.removeChangeListener(this);
				affectSubGroups.addChangeListener(this);
			}
			@Override
			public Result validate(JTextField component) {
				Result r = Result.CORRECT;
				String tooltip = null;
				//If 'no group' is selected, then the content of this text field is irrelevant
				if(!changeGroup.isSelected()) {
					r = Result.CORRECT;
					tooltip = null;
				}
				else {
					//Else check, if the new group path equals the original group path
					if(getNewPath().equals(getOriginalPath())) {
						tooltip = loader.getString(sgroup + ".errorequal", languageID);
						r = Result.INCORRECT;
					}
				}
				setToolTipText(component,tooltip);
				return r;
			}
		};
		radioValidator = new LabelIndicValidator<JRadioButton>(null, warnIcon, warnIcon) {
			@Override
			protected void registerToComponent(JRadioButton component) {
				changeGroup.addChangeListener(this);
				affectSubGroups.addChangeListener(this);
			}
			@Override
			protected void unregisterFromComponent(JRadioButton component) {
				changeGroup.removeChangeListener(this);
				affectSubGroups.addChangeListener(this);
			}
			@Override
			public Result validate(JRadioButton component) {
				Result r = Result.CORRECT;
				String tooltip = null;
				//If 'no group' is selected, check,the original path is the root path as well
				if(!changeGroup.isSelected()) {
					if(getOriginalPath() == null) {
							r = Result.INCORRECT;
							tooltip = loader.getString(sgroup + ".errorequal", languageID);
					}
				}
				setToolTipText(component, tooltip);
				return r;
			}
		};
		
		newpathValidator.addComponent(newPath, newLabel);
		radioValidator.addComponent(changeGroup, originalLabel);
		summary.addValidator(newpathValidator);
		summary.addValidator(radioValidator);
		summary.validate();
	}

	/**
	 * @return the original group path
	 */
	public String getOriginalPath() {
		return originalPathAsString;
	}

	/**
	 * @return the new group path
	 */
	public String getNewPath() {
		return changeGroup.isSelected() ? newPath.getText() : null;
	}

	/**
	 * @return If ids should be renamed
	 */
	public boolean getRenameIDs() {
		return renameIDs.isSelected();
	}

	/**
	 * @return If subgroups should be affected
	 */
	public boolean getAffectSubGroups() {
		return affectSubGroups.isSelected();
	}

	/**
	 * Assigns a resource reference. If r == null, uses the default fsfw reference
	 */
	@Override
	public void assignReference(ResourceReference r) {
		resource = r != null ? r : FsfwDefaultReference.getDefaultReference();
	}

	/**
	 * Expects one icon "warn.png"
	 */
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		
		tree.addPath("graphics/GroupEditor/warn.png");
		
		
		return tree;
	}

	/** 
	 * Performs the group edit associated to the data in this editor. This method should only be called by a dataRetrievalListener, when
	 * dataReady is called, since this ensures validity of the operation
	 */
	public UndoableGroupEdit performUndoableGroupEdit(UndoableEditFactory f) throws CannotRedoException {
		if(f == null) throw new CannotRedoException();
		//If both paths are equal throw exception
		if(getNewPath() == null ? getOriginalPath() == null : getNewPath().equals(getOriginalPath())) throw new CannotRedoException();
		return f.performUndoableGroupEdit(getOriginalPath(), getNewPath(), getRenameIDs(), getAffectSubGroups());
	}
	
	/**
	 * Returns the group edit associated to the data in this editor. Does not guarantee validity.
	 * @param f
	 */
	public UndoableGroupEdit getUndoableGroupEdit(UndoableEditFactory f) {
		if(f == null) return null;
		else return f.createUndoableGroupEdit(getOriginalPath(), getNewPath(), getRenameIDs(), getAffectSubGroups());
	}
	
}
