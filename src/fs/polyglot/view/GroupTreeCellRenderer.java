package fs.polyglot.view;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.dom4j.Document;

import fs.polyglot.model.Group;
import fs.polyglot.model.PolyglotString;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.model.TreeObject;
import fs.polyglot.model.Variant;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * Implements a renderer for a polyglot group tree. 
 * @author Simon Hampe
 *
 */
public class GroupTreeCellRenderer implements TreeCellRenderer,
		ResourceDependent {

	//Resource reference & language id
	private ResourceReference reference;
	private PolyglotStringLoader loader;
	private String languageID;
	
	//Group path for all strings
	private final static String sgroup = "fs.polyglot.GroupTreeCellRenderer";
	
	//Does this renderer cut group paths?
	private boolean cutGroupPath;
	
	//Which table for the name of the root node (= table id)?
	private PolyglotTableModel table;
	
	//Icons
	private ImageIcon group;
	private ImageIcon groupOpen;
	private ImageIcon groupWarn;
	private ImageIcon groupWarnOpen;
	private ImageIcon string;
	private ImageIcon stringWarn;
	private ImageIcon variant;
	
	/**
	 * Constructs a tree cell renderer. 
	 * @param r The resource reference to be used. If null, the default reference is used
	 * @param loader The string loader to be used. On null, the default loader is used
	 * @param languageID The language ID for tooltips and similar. If null, the global fsfw language id is used
	 * @param cutGroupPath Normally, string id's don't have to begin with the path of their group. But since it is quite practical to do so and
	 * it is not very sensible to display their complete name, this option gives the possibility to only display the last portion of every group and
	 * string name (more precisely: '.' + portion of path after last occurrence of a point).
	 * @param nullString The string to be displayed instead of a null string (i.e. for the root node, for example)
	 */
	public GroupTreeCellRenderer(ResourceReference r,PolyglotStringLoader loader, String languageID, boolean cutGroupPath, PolyglotTableModel model) {
		assignReference(r);
		this.languageID = languageID != null? languageID : PolyglotStringTable.getGlobalLanguageID();
		this.loader = loader != null? loader : PolyglotStringLoader.getDefaultLoader();
		this.cutGroupPath = cutGroupPath;
		this.table = model;
	}
	
	
	// CELL RENDERER ********************************************
	// **********************************************************
	
	/**
	 * Returns a group tree component. Groups are displayed as folders, PolyglotStrings as Sheets and Variants as paragraph symbols.
	 * A warning symbol will be attached to group and string icons, if the group / string is not complete, i.e. there is not a variant for each string 
	 * and listed language.  
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		JLabel label = new RendererLabel(selected,hasFocus);
		//If value is not of type TreeObject, there is nothing to do
		if(value instanceof TreeObject) {
			switch(((TreeObject) value).getType()) {
			case NONE: break;//Nothing to do.
			case GROUP: label.setIcon(expanded? (((Group)value).isComplete? groupOpen : groupWarnOpen): (((Group)value).isComplete? group: groupWarn));
						
						if(((Group)value).path == null) { label.setText("<html><i>" + (table!= null? table.getTableID() : "") + "</i></html>"); }
						else { label.setText(cutGroupPath? PolyglotStringTable.cutGroupPath(((Group)value).path) : ((Group)value).path); }
						
						if(!((Group)value).isComplete) label.setToolTipText(loader.getString(sgroup + ".groupincomplete", languageID));
						break;
			case POLYGLOTSTRING: 	label.setIcon(((PolyglotString)value).isComplete? string : stringWarn);
									label.setText(cutGroupPath? PolyglotStringTable.cutGroupPath(((PolyglotString)value).stringID) : ((PolyglotString)value).stringID);
									if(!((PolyglotString)value).isComplete) label.setToolTipText(loader.getString(sgroup + ".stringincomplete", languageID));
									break;
			case VARIANT:	label.setIcon(variant);
							label.setText(((Variant)value).language.id + ": " + ((Variant)value).value);
							break;
						
			}
		}
		return label;		
	}
	
	// BEHAVIOR CONTROL *****************************************
	// **********************************************************
	

	
	// RESOURCE DEPENDENT ***************************************
	// **********************************************************
	
	/**
	 * Assigns a resource reference (if null, the default reference is used) and reloads all graphic. Causes no
	 * repaints
	 */
	@Override
	public void assignReference(ResourceReference r) {
		reference = (r != null) ? r : FsfwDefaultReference.getDefaultReference();
		//Reload icons
		String path = "graphics/GroupTreeCellRenderer/";
		group = new ImageIcon(reference.getFullResourcePath(this, path + "group.png"));
		groupOpen = new ImageIcon(reference.getFullResourcePath(this, path + "groupOpen.png"));
		groupWarn = new ImageIcon(reference.getFullResourcePath(this, path + "groupWarn.png"));
		groupWarnOpen = new ImageIcon(reference.getFullResourcePath(this, path + "groupWarnOpen.png"));
		string = new ImageIcon(reference.getFullResourcePath(this, path + "string.png"));
		stringWarn = new ImageIcon(reference.getFullResourcePath(this, path + "stringWarn.png"));
		variant = new ImageIcon(reference.getFullResourcePath(this, path + "variant.png"));
	}

	/**
	 * This expects 5 png images of file names "group","groupOpen", "groupWarn", "groupWarnOpen", "string", "stringWarn" and "variant" in folder
	 * (fsframework)/graphics/GroupTreeCellRenderer/
	 */
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		for(String iname : Arrays.asList("group","groupOpen","groupWarn","groupWarnOpen","string","stringWarn","variant")) {
			tree.addPath("graphics/GroupTreeCellRenderer/" + iname + ".png");
		}
		return tree;
	}

	/**
	 * Indicates whether group and string paths are trimmed to the last path component
	 */
	public boolean doesCutGroupPath() {
		return cutGroupPath;
	}

	/**
	 * Sets whether group and string paths should be trimmed to the last path component
	 * @param cutGroupPath
	 */
	public void setCutGroupPath(boolean cutGroupPath) {
		this.cutGroupPath = cutGroupPath;
	}

}
