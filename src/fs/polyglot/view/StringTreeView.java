package fs.polyglot.view;

import java.awt.Image;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTree;

import org.apache.log4j.Logger;
import org.dom4j.Document;

import fs.polyglot.model.GroupTreeModel;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.undo.UndoableEditFactory;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This implements the main tree view for the application POLYGLOT. The complete string table is displayed as tree, Strings and Variants 
 * can be toggled to be invisible, controls for several operations are included.
 * @author hampe
 *
 */
public class StringTreeView extends JPanel implements ResourceDependent {

	//Components
	private JTree stringtree = new JTree();
	private JButton add = new JButton();
	private JButton delete = new JButton();
	private JButton editsingle = new JButton();
	private JButton editmultiple = new JButton();
	private JButton viewString = new JButton();
	private JButton viewVariant = new JButton();
	private JButton toggleCut = new JButton();
	private JButton showOnlyIncomplete = new JButton();
	
	//Associated models
	private GroupTreeModel treemodel;
	private GroupTreeCellRenderer treerenderer;
	
	//Icons
	private ImageIcon addStringIcon;
	private ImageIcon addVariantIcon;
	private ImageIcon deleteIcon;
	private ImageIcon editSingleIcon;
	private ImageIcon editMultipleIcon;
	private ImageIcon viewStringIcon;
	private ImageIcon viewNoStringIcon;
	private ImageIcon viewVariantIcon;
	private ImageIcon viewNoVariantIcon;
	private ImageIcon toggleCutIcon;
	private ImageIcon toggleNoCutIcon;
	private ImageIcon showOnlyIncompleteIcon;
	private ImageIcon showNotOnlyIncompleteIcon;
	
	// Resource reference an language id for tooltips
	private ResourceReference reference;
	private PolyglotStringLoader loader;
	private String languageID;
	private static final String sgroup = "fs.polyglot.GroupTreeView";

	// Associated table
	private PolyglotTableModel table;

	// Undo factory
	UndoableEditFactory editFactory = null;

	//Log
	private org.apache.log4j.Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
	
	// RESOURCEDEPENDENT METHODS ***************************************************************
	// *****************************************************************************************
	
	/**
	 * Assigns a resource reference and reloads all icons. Does not cause a repaint. If r == null, the default reference is used
	 */
	@Override
	public void assignReference(ResourceReference r) {
		reference = (r != null) ? r : FsfwDefaultReference.getDefaultReference();
		//Assign icons
		String path = "/graphics/";
		addStringIcon = new ImageIcon(reference.getFullResourcePath(this, path + "newstring.png"));
		addVariantIcon = new ImageIcon(reference.getFullResourcePath(this, path + "newvariant.png"));
		deleteIcon = new ImageIcon(reference.getFullResourcePath(this, path + "delete.png"));
		editSingleIcon = new ImageIcon(reference.getFullResourcePath(this, path + "edit.png"));
		editMultipleIcon = new ImageIcon(reference.getFullResourcePath(this, path + "editmultiple.png"));
		//TODO: ...
		//viewStringIcon = new ImageIcon(reference.getFullResourcePath(this, path + "string"));
		
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		
		for(String s : Arrays.asList("delete","edit","editmultiple","newstring","newvariant","nostring","novariant","nowarn","string","variant", "toggleCut", "toggleNoCut", "variant","warn")) {
			tree.addPath("graphics/" + s + ".png");
		}
		
		return tree;
	}

}
