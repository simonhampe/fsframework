package fs.polyglot.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;

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

	private final static String sgroup = "fs.polyglot.StringEditorConfigurator";
	
	private JButton okButton = new JButton();
	private JButton cancelButton = new JButton();
	
	private Action disposalListener = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == )
		}
	};
	
	/**
	 * Creates a StringEditorConfigurator dialog.
	 * @param table The table from which to acquire a list of used languages
	 */
	public StringEditorConfigurator(ResourceReference r,
			PolyglotStringLoader l, String lid, PolyglotTableModel table) {
		super(r, l, lid);
		setTitle(loader.getString(sgroup + ".title", languageID));
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		if(table == null) table = new PolyglotTableModel("","");
		
		// Init GUI
		
		JCheckBox incomplete = 	new JCheckBox(loader.getString(sgroup + ".incomplete", languageID));
		JCheckBox selected = 	new JCheckBox(loader.getString(sgroup + ".selected", languageID));
		JCheckBox onlylanguages = 	new JCheckBox(loader.getString(sgroup + ".onlytheselanguages", languageID));
		JCheckBox excludelanguages = new JCheckBox(loader.getString(sgroup + ".excludelanguages",languageID));
		Vector<Language> languages = new Vector<Language>();
		for(String id : table.getUsedLanguages()) {
			String desc = table.getLanguageDescription(id);
			languages.add(new Language(id,desc,desc == null, table.getSupport(id)));
		}
		JList listOnly = new JList(languages);
		listOnly.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		JList listExclude = new JList(languages);
		listExclude.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
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
		
	}

}