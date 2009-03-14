package fs.polyglot.view;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.dom4j.Document;
import org.dom4j.tree.DefaultDocument;

import fs.gui.FrameworkDialog;
import fs.gui.SwitchIconLabel;
import fs.polyglot.model.Language;
import fs.polyglot.model.PolyglotOptions;
import fs.polyglot.model.PolyglotTableModel;
import fs.validate.LabelIndicValidator;
import fs.validate.SingleButtonValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.PolyglotStringLoader;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLToolbox;

/**
 * Implements a dialog for configurating Polyglot. 
 * @author Simon Hampe
 *
 */
public class PolyglotConfigurator extends FrameworkDialog {
	
	//Components
	private JComboBox languageBox;
	private JSpinner lastfileSpinner;
	private JTextField configFile;
	private JButton ok;
	private JButton cancel;
	
	
	private PolyglotOptions originalOptions;
	private ImageIcon warnIcon;
	
	public PolyglotConfigurator(PolyglotOptions options,PolyglotTableModel frameworkTable, File config, ResourceReference r, PolyglotStringLoader l, String lid) {
		super(r,l,lid);
		originalOptions = options == null? new PolyglotOptions() : options;
		
		setTitle(loader.getString("fs.polyglot.PolyglotConfigurator.title", languageID));
		
		//Init GUI
		Vector<Language> llist = new Vector<Language>();
		Language selectedValue = null;
		for(String lang : frameworkTable.getLanguageList()) {
			Language ltoadd =new Language(lang,frameworkTable.getLanguageDescription(lang),false,frameworkTable.getSupport(lang)); 
			llist.add(ltoadd);
			if(lang.equals(originalOptions.getGlobalLanguageID())) selectedValue = ltoadd;
		}
		languageBox = new JComboBox(llist);
		languageBox.setRenderer(new LanguageListCellRenderer(resource,loader,languageID));
		if(selectedValue != null) languageBox.setSelectedItem(selectedValue);
		
		lastfileSpinner = new JSpinner(new SpinnerNumberModel(originalOptions.getMaxfilenumber(),0,
				10 >= originalOptions.getMaxfilenumber()? 10 : originalOptions.getMaxfilenumber()	,1));
		configFile = new JTextField(config != null? config.getAbsolutePath() : "");
		ok = new JButton(loader.getString("fs.global.ok", languageID));
		cancel = new JButton(loader.getString("fs.global.cancel", languageID));
		
		JLabel languageLabel = new JLabel(loader.getString("fs.polyglot.PolyglotConfigurator.language", languageID));
		JLabel spinnerLabel = new JLabel(loader.getString("fs.polyglot.PolyglotConfigurator.lastfiles", languageID));
		SwitchIconLabel fileLabel = new SwitchIconLabel(warnIcon);
				fileLabel.setText(loader.getString("fs.polyglot.PolyglotConfigurator.configfile", languageID));
				
		//Layout
		Box mainBox = new Box(BoxLayout.Y_AXIS);
		Box langBox = new Box(BoxLayout.X_AXIS);
			langBox.setAlignmentX(LEFT_ALIGNMENT);
			langBox.add(languageLabel); langBox.add(languageBox);
		Box spinnerBox = new Box(BoxLayout.X_AXIS);
			spinnerBox.setAlignmentX(LEFT_ALIGNMENT);
			spinnerBox.add(spinnerLabel); spinnerBox.add(lastfileSpinner);
		Box fileBox = new Box(BoxLayout.X_AXIS);
			fileBox.setAlignmentX(LEFT_ALIGNMENT);
			fileBox.add(fileLabel); fileBox.add(configFile);
		Box finalBox = new Box(BoxLayout.X_AXIS);
			finalBox.setAlignmentX(RIGHT_ALIGNMENT);
			finalBox.add(ok);finalBox.add(cancel);
		mainBox.add(langBox);mainBox.add(spinnerBox);mainBox.add(fileBox);mainBox.add(finalBox);
		
		add(mainBox);
		
		//Validation
		
		LabelIndicValidator<JTextField> warner = new LabelIndicValidator<JTextField>(null,warnIcon,null) {
			@Override
			protected void registerToComponent(JTextField component) {
				configFile.getDocument().addDocumentListener(this);
			}
			@Override
			protected void unregisterFromComponent(JTextField component) {
				configFile.getDocument().removeDocumentListener(this);
			}
			@Override
			public Result validate(JTextField component) {
				if(component.getText().trim().equals("")) {
					setToolTipText(component, loader.getString("fs.polyglot.PolyglotConfigurator.emptyconfigfile", languageID));
					return Result.INCORRECT;
				}
				else {
					setToolTipText(component, null);
					return Result.CORRECT;
				}
			}
			
		};
		
		SingleButtonValidator summary = new SingleButtonValidator(ok);
		summary.addValidator(warner);
		summary.validate();
	}
	
	/**
	 * Saves the current options to the configuration file, if possible.
	 * @throws IOException - If any I/O-Errors occur
	 */
	protected void saveConfiguration() throws IOException{
		//Create Data
		PolyglotOptions options = new PolyglotOptions();
			options.setDefaultDirectory(originalOptions.getDefaultDirectory());
			options.setGlobalLanguageID(((Language)languageBox.getSelectedItem()).id);
			options.setMaxfilenumber((Integer)lastfileSpinner.getModel().getValue());
			options.setLastfiles(originalOptions.getLastfiles());
		//Save
		Document d = new DefaultDocument();
		try {
			d.setRootElement(options.getConfiguration());
			XMLToolbox.saveXML(d, configFile.getText());
		} catch (XMLReadConfigurationException e) {
			throw new IOException(e);
		}
		
	}
	
	//RESOURCEDEPENDENT METHODS *****************************
	// ******************************************************
	
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/PolyglotConfigurator/warn.png");
		return tree;
	}

	@Override
	public void assignReference(ResourceReference r) {
		super.assignReference(r);
		//Reload icon
		warnIcon = new ImageIcon(resource.getFullResourcePath(this, "graphics/PolyglotConfigurator/warn.png"));
	}
	
	
}
