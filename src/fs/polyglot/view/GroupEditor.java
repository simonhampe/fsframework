package fs.polyglot.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;

import fs.gui.FrameworkDialog;
import fs.gui.GUIToolbox;
import fs.xml.PolyglotStringLoader;
import fs.xml.ResourceReference;

public class GroupEditor extends FrameworkDialog {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = -5005794553298843056L;
	//Components
	private JLabel originalLabel = new JLabel();
	private JLabel originalPath = new JLabel();
	private JLabel newLabel = new JLabel();
	private JRadioButton changeGroup = new JRadioButton();
	private JRadioButton noGroup = new JRadioButton();
	private JTextField newPath = new JTextField();
	private JLabel newTooltip = new JLabel();
	
	private JButton okButton = new JButton();
	private JButton cancelButton = new JButton();
	
	//Resources
	private String sgroup = "fs.polyglot.GroupEditor";
	
	
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
				fireDataReady(newPath.getText());
			}
			dispose();
		}
	};
	
	// CONSTRUCTOR **********************************************
	// **********************************************************
	
	/**
	 * Constructs a group editor
	 * @param path The path of the original group to be changed
	 * @param r Never used
	 * @param l The string loader for label texts and similar
	 * @param lid The language id for label texts and similar
	 */
	public GroupEditor(String path, ResourceReference r, PolyglotStringLoader l, String lid) {
		super(r, l, lid);
		
		//Create GUI
		originalLabel.setText(loader.getString(sgroup + ".originalpath", languageID));
		originalPath.setText(path != null? path : "<html><i>" + loader.getString(sgroup + ".nullpath", languageID) + "</i></html>");
		newLabel.setText(loader.getString(sgroup + ".newpath", languageID));
		newPath.setText(path);
		changeGroup.setText(loader.getString(sgroup + ".changegroup", languageID));
		noGroup.setText(loader.getString(sgroup + ".nogroup", languageID));
		newTooltip.setText("<html>" + loader.getString(sgroup  + ".newtooltip1", languageID) + "<br>" + 
									  loader.getString(sgroup + ".newtooltip2", languageID) + "<br>" + 
									  loader.getString(sgroup + ".newtooltip3", languageID) + 
									  "</html>");
		newTooltip.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		okButton.setText(loader.getString("fs.global.ok", languageID));
		cancelButton.setText(loader.getString("fs.global.cancel", languageID));
		ButtonGroup group = new ButtonGroup();
		group.add(changeGroup);
		group.add(noGroup);
		changeGroup.setSelected(true);
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
		GridBagConstraints gcOk = GUIToolbox.buildConstraints(1, 5, 1, 1);
		gcOk.weightx = 50;
		GridBagConstraints gcCancel = GUIToolbox.buildConstraints(2, 5, 1, 1);
		gcCancel.weightx = 50;
		gbl.setConstraints(originalLabel, gcOriginalLabel);
		gbl.setConstraints(originalPath, gcOriginalPath);
		gbl.setConstraints(changeGroup, gcChangeGroup);
		gbl.setConstraints(newLabel, gcNewLabel);
		gbl.setConstraints(newPath, gcNewPath);
		gbl.setConstraints(noGroup, gcNoGroup);
		gbl.setConstraints(newTooltip, gcNewTooltip);
		gbl.setConstraints(okButton, gcOk);
		gbl.setConstraints(cancelButton, gcCancel);
		
		//Add
		for(JLabel label : Arrays.asList(originalLabel,originalPath, newLabel, newTooltip)) add(label);
		add(changeGroup); add(newPath); add(noGroup);
		add(okButton);add(cancelButton);
		
		pack();
		newPath.requestFocusInWindow();
		
		//Event handling
		getRootPane().setDefaultButton(okButton);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "dispose");
		getRootPane().getActionMap().put("dispose", disposalListener);
		okButton.addActionListener(disposalListener);
		cancelButton.addActionListener(disposalListener);
	}

}
