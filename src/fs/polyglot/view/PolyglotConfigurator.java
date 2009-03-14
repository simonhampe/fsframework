package fs.polyglot.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import fs.gui.FrameworkDialog;
import fs.polyglot.model.Language;
import fs.polyglot.model.PolyglotOptions;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceReference;

/**
 * Implements a dialog for configurating Polyglot. DataRetrievalListeners are notified with the resulting PolyglotOptions object as data. 
 * @author Simon Hampe
 *
 */
public class PolyglotConfigurator extends FrameworkDialog {
	
	/**
	 *compiler-generated version id 
	 */
	private static final long serialVersionUID = 3574119439450051287L;
	
	//Components
	private JComboBox languageBox;
	private JSpinner lastfileSpinner;
	private JButton ok;
	private JButton cancel;
	
	
	private PolyglotOptions originalOptions;
	
	//Notifies all listeners that the configuration is done
	private ActionListener okListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			//Extract options
			PolyglotOptions options = new PolyglotOptions();
				options.setDefaultDirectory(originalOptions.getDefaultDirectory());
				options.setGlobalLanguageID(((Language)languageBox.getSelectedItem()).id);
				options.setMaxfilenumber((Integer)lastfileSpinner.getModel().getValue());
				options.setLastfiles(originalOptions.getLastfiles());
			//Pass on
			fireDataReady(options);
			//Close this dialog
			dispose();
		}
	};
	
	private ActionListener cancelListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	};
	
	// CONSTRUCTOR **************************************
	// **************************************************
	
	/**
	 * Constructs a dialog for configuring Polyglot
	 * @param options The options currently used by polyglot. The fields will be filled according to this data. If null, a default configuration is used
	 * @param frameworkTable The table from which the languages available for polyglot should be retrieved. 
	 */
	public PolyglotConfigurator(PolyglotOptions options,PolyglotStringTable frameworkTable, ResourceReference r, PolyglotStringLoader l, String lid) {
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
		ok = new JButton(loader.getString("fs.global.ok", languageID));
		cancel = new JButton(loader.getString("fs.global.cancel", languageID));
		
		JLabel languageLabel = new JLabel(loader.getString("fs.polyglot.PolyglotConfigurator.language", languageID));
		JLabel spinnerLabel = new JLabel(loader.getString("fs.polyglot.PolyglotConfigurator.lastfiles", languageID));
				
		//Layout
		Box mainBox = new Box(BoxLayout.Y_AXIS);
		Box langBox = new Box(BoxLayout.X_AXIS);
			langBox.setAlignmentX(LEFT_ALIGNMENT);
			langBox.add(languageLabel); 
		Box langBox2 = new Box(BoxLayout.X_AXIS);
			langBox2.setAlignmentX(LEFT_ALIGNMENT);
			langBox2.add(Box.createHorizontalGlue());langBox2.add(languageBox);
		Box spinnerBox = new Box(BoxLayout.X_AXIS);
			spinnerBox.setAlignmentX(LEFT_ALIGNMENT);
			spinnerBox.add(spinnerLabel); spinnerBox.add(lastfileSpinner);
		Box finalBox = new Box(BoxLayout.X_AXIS);
			finalBox.setAlignmentX(LEFT_ALIGNMENT);
			finalBox.add(Box.createHorizontalGlue());finalBox.add(ok);finalBox.add(cancel);
		mainBox.add(Box.createRigidArea(new Dimension(5,5)));
		mainBox.add(langBox);
		mainBox.add(Box.createRigidArea(new Dimension(5,5)));
		mainBox.add(langBox2);
		mainBox.add(Box.createRigidArea(new Dimension(5,5)));
		mainBox.add(spinnerBox);
		mainBox.add(Box.createRigidArea(new Dimension(5,5)));
		mainBox.add(finalBox);
		
		add(mainBox);
		pack();
		setResizable(false);
		
		ok.addActionListener(okListener);
		cancel.addActionListener(cancelListener);
		
	}	
}
