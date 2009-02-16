package fs.polyglot.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fs.event.DataRetrievalListener;
import fs.gui.FrameworkDialog;
import fs.gui.GUIToolbox;
import fs.polyglot.model.Language;
import fs.polyglot.model.PolyglotTableModel;
import fs.xml.PolyglotStringLoader;
import fs.xml.ResourceReference;

/**
 * This class represents a simple dialog used for configuring the list of edited strings of a StringEditor. Its result is
 * stored in a StringEditorConfiguration Object and passed to {@link DataRetrievalListener}s.
 * @author Simon Hampe
 *
 */
public class StringEditorConfigurator extends FrameworkDialog {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = 2195434728552582293L;

	private final static String sgroup = "fs.polyglot.StringEditorConfigurator";
	
	private JButton okButton = new JButton();
	private JButton cancelButton = new JButton();
	private JCheckBox incomplete = 	new JCheckBox();
	private JCheckBox selected = 	new JCheckBox();
	private JCheckBox onlylanguages = 	new JCheckBox();
	private JCheckBox excludelanguages = new JCheckBox();
	private JList listOnly = new JList();
	private JList listExclude = new JList();
	
	//Listens for OK and Cancel
	private Action disposalListener = new AbstractAction() {
		/**
		 * compiler-generated version id
		 */
		private static final long serialVersionUID = 3732305715709762289L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == okButton) {
				//Copy configuration data
				StringEditorConfiguration config = new StringEditorConfiguration();
				config.editOnlyIncomplete = incomplete.isSelected();
				config.editOnlySelected = selected.isSelected();
				//Read out selected ids
				HashSet<String> onlySelected = new HashSet<String>();
				for(Object o : listOnly.getSelectedValues()) onlySelected.add(((Language)o).id);
				HashSet<String> excludeSelected = new HashSet<String>();
				for(Object o : listExclude.getSelectedValues()) excludeSelected.add(((Language)o).id);
				config.excludeTheseLanguages = excludelanguages.isSelected() ? excludeSelected: null;
				config.onlyTheseLanguages = excludelanguages.isSelected() ? null : 
											onlylanguages.isSelected() ? onlySelected : null;
				fireDataReady(config);
			}
			dispose();
		}
	};
	
	//Listens for checkbox changes of exclude and only - languages
	private ChangeListener selectListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			if(e.getSource() == onlylanguages ) {
				if(onlylanguages.isSelected()) {
					excludelanguages.setSelected(false);
					listOnly.setEnabled(true);
				}
				else {
					listOnly.setEnabled(false);
				}
			}
			if(e.getSource() == excludelanguages ) {
				if(excludelanguages.isSelected()) {
					onlylanguages.setSelected(false);
					listExclude.setEnabled(true);
				}
				else {
					listExclude.setEnabled(false);
				}
			}
		}
	};
	
	// CONSTRUCTOR *********************************************
	// *********************************************************
	
	/**
	 * Creates a StringEditorConfigurator dialog.
	 * @param table The table from which to acquire a list of used languages
	 * @param config A configuration to get initial values from. If it is null, default values are used
	 */
	public StringEditorConfigurator(ResourceReference r,
			PolyglotStringLoader l, String lid, PolyglotTableModel table, StringEditorConfiguration config) {
		super(r, l, lid);
		setTitle(loader.getString(sgroup + ".title", languageID));
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		if(table == null) table = new PolyglotTableModel("","");
		
		// Init GUI
		
		incomplete = 	new JCheckBox(loader.getString(sgroup + ".incomplete", languageID));
		selected = 	new JCheckBox(loader.getString(sgroup + ".selected", languageID));
		onlylanguages = 	new JCheckBox(loader.getString(sgroup + ".onlytheselanguages", languageID));
		excludelanguages = new JCheckBox(loader.getString(sgroup + ".excludelanguages",languageID));
		Vector<Language> languages = new Vector<Language>();
		for(String id : table.getUsedLanguages()) {
			String desc = table.getLanguageDescription(id);
			languages.add(new Language(id,desc,desc == null, table.getSupport(id)));
		}
		listOnly = new JList(languages);
		listOnly.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		listOnly.setEnabled(false);
		listExclude = new JList(languages);
		listExclude.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		listExclude.setEnabled(false);
		okButton = new JButton(loader.getString("fs.global.ok", languageID));
		cancelButton = new JButton(loader.getString("fs.global.cancel", languageID));
		
		JPanel fillPanel = new JPanel();
		
		GridBagLayout gbl = new GridBagLayout();
		Insets sideInset = new Insets(0,5,0,10);
		GridBagConstraints cIncomplete = GUIToolbox.buildConstraints(0, 0, 3, 1);cIncomplete.insets = new Insets(10,5,0,10);
		GridBagConstraints cSelected = GUIToolbox.buildConstraints(0, 1, 3, 1); cSelected.insets = sideInset;
		GridBagConstraints cOnlyLanguages = GUIToolbox.buildConstraints(0, 2, 3, 1); cOnlyLanguages.insets = sideInset;
		GridBagConstraints cOnlyList = GUIToolbox.buildConstraints(0, 3, 3, 1); cOnlyList.insets = new Insets(5,10,5,10);
		GridBagConstraints cExcludeLanguages = GUIToolbox.buildConstraints(0, 4, 3, 1);cExcludeLanguages.insets = sideInset;
		GridBagConstraints cExcludeList = GUIToolbox.buildConstraints(0, 5, 3, 1); cExcludeList.insets = new Insets(5,10,5,10);
		GridBagConstraints cFill = GUIToolbox.buildConstraints(0, 6, 1, 1); cFill.weightx = 100;
		GridBagConstraints cOk = GUIToolbox.buildConstraints(1, 6, 1, 1); cOk.insets = new Insets(0,0,5,0);
		GridBagConstraints cCancel = GUIToolbox.buildConstraints(2, 6, 1, 1); cCancel.insets = new Insets(0,0,5,10);
		gbl.setConstraints(incomplete, cIncomplete);
		gbl.setConstraints(selected, cSelected);
		gbl.setConstraints(onlylanguages, cOnlyLanguages);
		gbl.setConstraints(listOnly, cOnlyList);
		gbl.setConstraints(excludelanguages, cExcludeLanguages);
		gbl.setConstraints(listExclude, cExcludeList);
		gbl.setConstraints(fillPanel, cFill);
		gbl.setConstraints(okButton, cOk);
		gbl.setConstraints(cancelButton, cCancel);
		setLayout(gbl);
		add(incomplete);add(selected);add(onlylanguages);add(listOnly);add(excludelanguages); add(listExclude);
		add(fillPanel);add(okButton);add(cancelButton);
		pack();
		setResizable(false);
		
		//Init Event handling
		
		getRootPane().setDefaultButton(okButton);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "dispose");
		getRootPane().getActionMap().put("dispose", disposalListener);
		okButton.addActionListener(disposalListener);
		cancelButton.addActionListener(disposalListener);
		
		excludelanguages.addChangeListener(selectListener);
		onlylanguages.addChangeListener(selectListener);
		
		//Set values from config
		if(config != null) {
			incomplete.setSelected(config.editOnlyIncomplete);
			selected.setSelected(config.editOnlySelected);
			//Find out which list we have to copy
			HashSet<String> listtocopyfrom = null;
			JList listtocopyto = null;
			if(config.excludeTheseLanguages != null) {
				listtocopyfrom = config.excludeTheseLanguages;
				listtocopyto = listExclude;
				excludelanguages.setSelected(true);
			}
			else {
				if(config.onlyTheseLanguages != null) {
					listtocopyfrom = config.onlyTheseLanguages;
					listtocopyto = listOnly;
					onlylanguages.setSelected(true);
				}
			}
			if(listtocopyfrom != null && listtocopyto != null) {
				//Copy
				for(Language lang : languages) {
					if(listtocopyfrom.contains(lang.id)) {
						int index = languages.indexOf(lang);
						listtocopyto.addSelectionInterval(index, index);
					}
				}
			}
		}
		
	}

}