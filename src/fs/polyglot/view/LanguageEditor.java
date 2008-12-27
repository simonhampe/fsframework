package fs.polyglot.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.dom4j.Document;

import fs.gui.FrameworkDialog;
import fs.gui.GUIToolbox;
import fs.gui.SwitchIconLabel;
import fs.polyglot.model.Language;
import fs.polyglot.validate.NonEmptyWarner;
import fs.polyglot.validate.TabooValidator;
import fs.validate.ValidationResult;
import fs.validate.ValidationValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * Implements a dialog for editing a Language object. Contains two text fields, one for the id, one for the description.
 * The ID field is validated against a list of existing Language IDs which are not allowed as values. The description field 
 * will produce a warning when it's empty. dataRetrievalListeners will obtain a language 
 * object when OK is clicked, where isOnlyUsed is set to false and supported is set to 0. <br>
 * This dialog disappears, when losing focus or when ESCAPE is pressed. OK is set to be the default button. Initial focus is set to the description field, 
 * if it is empty and the id field is not, and to the id field otherwise
 * @author Simon Hampe
 *
 */
public class LanguageEditor extends FrameworkDialog implements ResourceDependent {

	/**
	 * Compiler-generated version id 
	 */
	private static final long serialVersionUID = 637698584717152783L;
	
	//Components
	private JTextField idText = new JTextField();
	private SwitchIconLabel idLabel  = new SwitchIconLabel();
	private JTextField descText = new JTextField();
	private SwitchIconLabel descLabel = new SwitchIconLabel();
	private JButton okButton = new JButton();
	private JButton cancelButton = new JButton();
	
	private ImageIcon warnIcon;
	
	//Data
	private HashSet<String> tabooList = new HashSet<String>();
	
	//The string group
	private final static String sgroup = "fs.polyglot.LanguageEditor";
	
	//Validators
	private TabooValidator idValidator;
	private NonEmptyWarner descValidator;
	private ValidationValidator summary = new ValidationValidator() { //Enables or disables the okButton according to the content
		@Override
		public void validationPerformed(ValidationResult result) {
			if(result.getOverallResult() == Result.INCORRECT) okButton.setEnabled(false);
			else okButton.setEnabled(true);
		}
	};
	
	//Disposal listener for the cancel button and ESCAPE key
	private Action disposalListener = new AbstractAction() {
		/**
		 * Compiler-generated version id
		 */
		private static final long serialVersionUID = -590544310931020969L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == okButton) fireDataReady(new Language(idText.getText(), descText.getText(), false, 0));
			dispose();
		}
	};
	
	//Focus-loss listener which discards the dialog
	private WindowFocusListener focusListener = new WindowFocusListener() {
		@Override
		public void windowGainedFocus(WindowEvent e) {
			//Ignored
		}
		@Override
		public void windowLostFocus(WindowEvent e) {
			dispose();
		}
	};
	
	/**
	 * Creates a dialog for editing a language
	 * @param tabooList A list of language ids that are not allowed. If you want to edit an existing language, its ID should not be contained in the list.
	 * @param language If null, all fields are initialized as empty. If not null, id and description are copied from this language
	 */
	public LanguageEditor(Collection<String> tabooList, Language language, ResourceReference r, PolyglotStringLoader l,String lid) {
		super(r, l, lid);
		//Copy data
		if(tabooList != null) this.tabooList.addAll(tabooList);
		
		//Load ImageIcon
		warnIcon = new ImageIcon(resource.getFullResourcePath(this, "graphics/LanguageEditor/warn.png"));
		
		//Initialize dialog
		setTitle(loader.getString(sgroup + ".title", languageID));
		setUndecorated(true);
		getRootPane().setBorder(BorderFactory.createEtchedBorder());
		addWindowFocusListener(focusListener);
		
		//Initialize components
		okButton.setText(loader.getString("fs.global.ok", languageID));
		cancelButton.setText(loader.getString("fs.global.cancel", languageID));
		if(language != null) {
			idText.setText(language.id);
			descText.setText(language.description);
		}
		idLabel.setText(loader.getString(sgroup + ".id", languageID));
		idLabel.setIconReference(warnIcon);
		descLabel.setText(loader.getString(sgroup + ".description", languageID));
		descLabel.setIconReference(warnIcon);
		
		okButton.addActionListener(disposalListener);
		cancelButton.addActionListener(disposalListener);
		
		
		
		//Create layout
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints idLabelC = GUIToolbox.buildConstraints(0, 0, 1, 1);
		gbl.setConstraints(idLabel, idLabelC);
		GridBagConstraints idTextC = GUIToolbox.buildConstraints(1, 0, 1, 1);
		gbl.setConstraints(idText, idTextC);
		GridBagConstraints descLabelC = GUIToolbox.buildConstraints(0, 1, 1, 1);
		gbl.setConstraints(descLabel, descLabelC);
		GridBagConstraints descTextC = GUIToolbox.buildConstraints(1, 1, 2, 1);
		gbl.setConstraints(descText, descTextC);
		GridBagConstraints okC = GUIToolbox.buildConstraints(1, 2, 1, 1);
		gbl.setConstraints(okButton, okC);
		GridBagConstraints cancelC = GUIToolbox.buildConstraints(2, 2, 1, 1);
		gbl.setConstraints(cancelButton, cancelC);
		
		add(idLabel); add(idText);
		add(descLabel); add(descText);
		add(okButton);add(cancelButton);
		pack();
		
		//Set the keyboard focus according to the data
		if(language != null && (language.description.equals(""))) descText.requestFocusInWindow();
		else idText.requestFocusInWindow();
		
		//Register key stroke for cancel and ok button
		getRootPane().setDefaultButton(okButton);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "dispose");
		getRootPane().getActionMap().put("dispose", disposalListener);
		
		
		
		//Initialize validators to set correct tooltips and link them to components
		
		idValidator = new TabooValidator(null, null, warnIcon, this.tabooList, false) {

			/* (non-Javadoc)
			 * @see fs.polyglot.validate.TabooValidator#validate(javax.swing.text.JTextComponent)
			 */
			@Override
			public Result validate(JTextComponent component) {
				Result result = super.validate(component);
				if(result == Result.CORRECT) component.setToolTipText(null);
				if(result == Result.INCORRECT) {
					if(component.getText().trim().equals("")) setToolTipText(component,loader.getString(sgroup + ".idempty", languageID));
					else setToolTipText(component,loader.getString(sgroup + ".idnotunique", languageID));
				}
				return result;
			}
		};
		
		descValidator = new NonEmptyWarner(null, warnIcon, null) {
			@Override
			public Result validate(JTextComponent component) {
				Result result = super.validate(component);
				if(result == Result.WARNING) setToolTipText(component,loader.getString(sgroup + ".nodescription", languageID));
				else setToolTipText(component,null);
				return result;
			}
		};
		
		idValidator.addComponent(idText, idLabel);
		descValidator.addComponent(descText, descLabel);
		summary.addValidator(idValidator);
		summary.addValidator(descValidator);
		summary.validate();
	}

	/**
	 * Assigns a resource reference. If r == null, the default reference is used.
	 */
	@Override
	public void assignReference(ResourceReference r) {
		resource = (r != null)? r : FsfwDefaultReference.getDefaultReference();
	}

	/**
	 * Only needs the image file graphics/LanguageEditor/warn.png
	 */
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		
		tree.addPath("graphics/LanguageEditor/warn.png");
		
		return tree;
	}
	
}
