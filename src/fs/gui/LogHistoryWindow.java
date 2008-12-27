package fs.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;

import java.util.*;
import org.apache.log4j.*;
import org.dom4j.Document;

import fs.xml.*;
import fs.log.*;

/**
 * Implements a window that contains a text area for displaying 
 * log messages, some basic filter options and a button for saving
 * the current log text. Appending of log messages is done via its data
 * model which must implement LogHistoryModel (which in turn
 * extends AppenderSkeleton and can thus be added as Appender to a Logger). 
 * The messages will be appended in html format. Since line breaks are not added automatically,
 * the appender should take care of that in its layout (the default appender does of course do so).
 * @author Simon Hampe
 *
 */
public class LogHistoryWindow extends JFrame implements ResourceDependent{
	
	/**
	 * compiler-generated version ID 
	 */
	private static final long serialVersionUID = -6565101753640174396L;
	
	// GUI COMPONENTS ******************************
	// *********************************************
	private JButton saveButton = new JButton();
	//private ArrayList<JCheckBox> filterchecks = new ArrayList<JCheckBox>();
	private JPanel filterpanel = new JPanel();
	private JEditorPane display = new JEditorPane();
	
	// DATA MODEL **********************************
	// *********************************************
	
	//The internal data model
	private LogHistoryModel model;
	//An internal copy of the filter set of the model. It is kept for graphical performance
	//purposes.
	private HashSet<Level> filteroptions = new HashSet<Level>();
	//The change listener for the model which will update
	//the display and the filter checkboxes
	private LogHistoryListener updateListener = new LogHistoryListener() {
		public void messageAppended(String formattedMessage) {
			//Insert text and repaint
			//TODO: This is inefficient. Messages should be appended. Perhaps, an HTMLDocument with external stylesheet should be used?
			reloadMessages();
		}
		public void filterChanged(Level l, boolean flag) {
			//If the filter has been added reload filter. If not, just reload messages
			if(!filteroptions.contains(l)) {
				reloadFilter();
			}
			reloadMessages();
		}
	};
	//The change listener listening to check box clicks
	private ActionListener checkListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JCheckBox c = ((JCheckBox)e.getSource());
			model.putFilter(Level.toLevel(c.getText()),c.isSelected()); 
		}
	};
	//The save action for the button
	private ActionListener saveListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				saveLogWithDialog();
			} catch (IOException e1) {
				//Display an error message
				JOptionPane.showMessageDialog(null, loader.getString("fs.global.savefailed", languageID,"", e1.getMessage()),
						loader.getString("fs.global.error", languageID), JOptionPane.ERROR_MESSAGE);
			}
			
		}
		
	};
	
	
	//
	
	// RESOURCE MANAGEMENT *************************
	// *********************************************
	
	//The resource reference
	private ResourceReference resource = null;
	
	//An internal copy of the string loader
	private PolyglotStringLoader loader = null;
	
	//The language id used to query strings
	private String languageID = null;
	
	// CONSTRUCTOR *********************************
	// *********************************************
	
	/**
	 * Constructs a log history window with default data model fs.log.DefaultLogHistoryWindowModel
	 * @param title The window title. If null, the default title, as specified by fs.gui.LogHistoryWindow.defaulttitle 
	 * in the fsframework string table is used with the appropriate languageid
	 * @param r The resource reference for locating graphical resources. If null, the fsframework default
	 * reference is used.
	 * @param loader The string loader used to load the different texts in this component. If null,
	 * the fsframework default loader is used.
	 * @param languageID The id of the language in which this component should be displayed. If null, the
	 * fsframework global language is used.
	 */
	public LogHistoryWindow(String title, ResourceReference r, PolyglotStringLoader loader, String languageID) {
		//Basic initialization -----------------------------------------------
		super(title);
		setBounds(0,0,300,500);
		
		//Data initialization ------------------------------------------------
		model = new DefaultLogHistoryWindowModel();
		model.addLogHistoryListener(updateListener);
		filteroptions = new HashSet<Level>(model.getFilterTable().keySet());
		resource = (r != null) ? r : FsfwDefaultReference.getDefaultReference();
		this.loader = (loader != null) ? loader : PolyglotStringLoader.getDefaultLoader();
		this.languageID = (languageID != null) ? languageID : PolyglotStringTable.getGlobalLanguageID();
		if(title == null) setTitle(this.loader.getString("fs.gui.LogHistoryWindow.defaulttitle", this.languageID));
		
		//GUI initialization -------------------------------------------------
		
		//Initialize components and additional components, such as dummy components
		display.setEditable(false);
		display.setContentType("text/html");
		display.setEditorKit(new HTMLEditorKit());
		saveButton.setIcon(new ImageIcon(resource.getFullResourcePath(this, "graphics/LogHistoryWindow/save.png")));
		saveButton.setToolTipText(this.loader.getString("fs.gui.LogHistoryWindow.savetooltip", this.languageID));
		saveButton.addActionListener(saveListener);
		
		JPanel topPanel = new JPanel(); //will contain all components at the top
		JPanel fillPanel = new JPanel(); //will prevent the top components from wandering on scaling
		JLabel filterTip = new JLabel(this.loader.getString("fs.gui.LogHistoryWindow.filtertip", this.languageID )); //A label explaining the filter panel
		JScrollPane scrollPane = new JScrollPane(display);
		
		//Initialize layout
		GridBagLayout gbl = new GridBagLayout();
		GridBagLayout gbltop = new GridBagLayout();
		setLayout(gbl);
		topPanel.setLayout(gbltop);
		GridBagConstraints gbtop = GUIToolbox.buildConstraints(0, 0, 1, 1);
		GridBagConstraints gbdisplay = GUIToolbox.buildConstraints(0, 1, 1, 1);
		gbdisplay.weightx = gbdisplay.weighty = 100;
		gbdisplay.ipady = 100;
		GridBagConstraints gbsave = GUIToolbox.buildConstraints(0, 1, 1, 1);
		GridBagConstraints gbtip = GUIToolbox.buildConstraints(1, 0, 1, 1);
		GridBagConstraints gbfilter = GUIToolbox.buildConstraints(1, 1, 1, 1);
		GridBagConstraints gbfill = GUIToolbox.buildConstraints(2, 0, 1, 1);
		gbfill.weightx = 100;
		
		
		//Assign layouts
		gbl.setConstraints(topPanel, gbtop);
		gbl.setConstraints(scrollPane, gbdisplay);
		gbltop.setConstraints(saveButton, gbsave);
		gbltop.setConstraints(filterTip, gbtip);
		gbltop.setConstraints(filterpanel, gbfilter);
		gbltop.setConstraints(fillPanel, gbfill);
		
		//Add components
		topPanel.add(saveButton);
		topPanel.add(filterTip);
		topPanel.add(filterpanel);
		topPanel.add(fillPanel);
		add(topPanel);
		add(scrollPane);
		
		//finalize
		reloadFilter();
		reloadMessages();
		pack();
		
	}
	
	/**
	 * Constructs a log history window with the specified title, using the default resource reference and string loader
	 * and the global languageID
	 * @param title
	 */
	public LogHistoryWindow(String title) {
		this(title,null,null,null);
	}
	
	// GETTERS AND SETTERS ****************************
	// ************************************************
	
	/**
	 * @return The data model used by this component
	 */
	public LogHistoryModel getModel() {
		return model;
	}

	/**
	 * Sets the data model and reloads all message history / filter options. This call
	 * is ignored, if the argument is null
	 */
	public void setModel(LogHistoryModel model) {
		if(model != null) {
			this.model.removeLogHistoryListener(updateListener);
			this.model = model;
			this.model.addLogHistoryListener(updateListener);
			reloadFilter();
			reloadMessages();
		}
	}
	
	/**
	 * @return The language id of the language in which this component should be displayed
	 */
	public String getLanguageID() {
		return languageID;
	}

	/**
	 * @return the button used for saving
	 */
	public JButton getSaveButton() {
		return saveButton;
	}

	/**
	 * @return The Editor Pane used to display
	 */
	public JEditorPane getDisplay() {
		return display;
	}
	
	// BEHAVIOR CONTROL METHODS *********************
	// **********************************************
	
	/**
	 * Reloads the complete message history from the associated appender (filtered according 
	 * to it's filter properties) and repaints 
	 * the displaying component afterwards. 
	 */
	public void reloadMessages() {
		StringBuilder history = new StringBuilder();
		for(String m : model.getLogHistory()) {
			history.append(m);
		}
		display.setText(history.toString());
		repaint();
	}
	
	/**
	 * Reloads all filter options currently configured in the associated appender, re-constructs 
	 * the filter checkboxes and repaints the component
	 *
	 */
	public void reloadFilter() {
		//Remove old checkboxes
		//filterchecks = new ArrayList<JCheckBox>();
		filterpanel.removeAll();
		//Create a comparator and a set that will sort the levels by priority
		Comparator<Level> levelComp = new Comparator<Level>() {
			public int compare(Level o1, Level o2) {
				if(o1.isGreaterOrEqual(o2)) {
					if(o2.isGreaterOrEqual(o1)) return 0;
					else return 1;
				}
				else return -1;
			}
		};
		TreeSet<Level> levelset= new TreeSet<Level>(levelComp);
		//Load filter options
		HashMap<Level,Boolean> foptions = model.getFilterTable();
		filteroptions = new HashSet<Level>(foptions.keySet());
		levelset.addAll(foptions.keySet());
		//Set layout and add checkboxes, use (No. of Filter / 2) rows.
		int columns = foptions.keySet().size();
		if(2*columns < foptions.keySet().size()) columns++; //avoid rounding 'errors'
		filterpanel.setLayout(new GridLayout(2,columns));
		for(Level l : levelset) {
			JCheckBox c = new JCheckBox(l.toString(),foptions.get(l));
			c.addActionListener(checkListener);
			//filterchecks.add(c);
			filterpanel.add(c);
		}
		pack();
		repaint();
	}
	
	// OUTPUT METHODS *******************************
	// **********************************************
	
	/**
	 * Tries to save the currently visible log to the specified file in HTML format.
	 * The resulting HTML file is a valid html file (up to the format of the log text, which 
	 * is formatted by the appender) with empty header.
	 * @throws IOException - if any I/O-error occurs
	 */
	public void saveLog(File f) throws IOException{
		HTMLDocument doc = (HTMLDocument)display.getDocument();
		FileWriter output = new FileWriter(f);
		HTMLWriter writer = new HTMLWriter(output, doc);
		try {
			writer.write();
			output.close();
		}
		catch(BadLocationException ble) {
			//Won't happen
		}
	}
	
	/**
	 * Calls the standard fsframework file save dialog as created by {@link GUIToolbox#saveFileAndConfirm(File, javax.swing.filechooser.FileNameExtensionFilter, JComponent)}
	 * and saves the currently visible log to this file in HTML format. 
	 * The resulting HTML file is a valid html file (up to the format of the log text, which 
	 * is formatted by the appender) with empty header.
	 * @throws IOException - if any I/O-error occurs
	 */
	public void saveLogWithDialog() throws IOException{
		File f = GUIToolbox.saveFileAndConfirm(null, new FileNameExtensionFilter("HTML files","html"), this);
		if(f != null) saveLog(f);
	}
	
	
	
	// RESOURCEDEPENDENT METHODS ********************
	// **********************************************
	
	/**
	 * Assigns the specified resource reference. If (r == null), 
	 * the default reference is used.
	 */
	public void assignReference(ResourceReference r) {
		resource = (r == null) ? FsfwDefaultReference.getDefaultReference() : r;
	}
	
	/**
	 * This class expects one image file, (basedir)/graphics/LogHistoryWindow/save.png and
	 * one xml file, (basedir)/language/fsfwStringTable.xml
	 */
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree("basedir");
		tree.addPath("graphics/LogHistoryWindow/save.png");
		tree.addPath("language/fsfwStringTable.xml");
		return tree;
	}
}
