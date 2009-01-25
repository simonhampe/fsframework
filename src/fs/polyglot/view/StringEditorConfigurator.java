package fs.polyglot.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;

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
		JList listExclude = new JList(languages);
		JButton okButton = new JButton(loader.getString("fs.global.ok", languageID));
		JButton cancelButton = new JButton(loader.getString("fs.global.cancel", languageID));
		
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints cIncomplete = GUIToolbox.buildConstraints(0, 0, 3, 1);
		GridBagConstraints cSelected = GUIToolbox.buildConstraints(0, 1, 3, 1);
		GridBagConstraints cOnlyLanguages = GUIToolbox.buildConstraints(0, 2, 3, 1);
		GridBagConstraints cOnlyList = GUIToolbox.buildConstraints(1, 3, 2, 1);
		GridBagConstraints cExcludeLanguages = GUIToolbox.buildConstraints(0, 4, 3, 1);
		GridBagConstraints cExcludeList = GUIToolbox.buildConstraints(1, 5, 2, 1);
		GridBagConstraints cOk = GUIToolbox.buildConstraints(1, 6, 1, 1);
		GridBagConstraints cCancel = GUIToolbox.buildConstraints(2, 6, 1, 1);
		gbl.setConstraints(incomplete, cIncomplete);
		gbl.setConstraints(selected, cSelected);
		gbl.setConstraints(onlylanguages, cOnlyLanguages);
		gbl.setConstraints(listOnly, cOnlyList);
		gbl.setConstraints(excludelanguages, cExcludeLanguages);
		gbl.setConstraints(listExclude, cExcludeList);
		gbl.setConstraints(okButton, cOk);
		gbl.setConstraints(cancelButton, cCancel);
		setLayout(gbl);
		add(incomplete);add(selected);add(onlylanguages);add(listOnly);add(excludelanguages); add(listExclude);
		add(okButton);add(cancelButton);
		pack();
		
	}

}