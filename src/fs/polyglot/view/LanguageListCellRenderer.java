package fs.polyglot.view;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ListCellRenderer;

import org.dom4j.Document;

import fs.gui.GUIToolbox;
import fs.polyglot.model.Language;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This implements a renderer for cells of language lists. 
 * @author Simon Hampe
 *
 */
public class LanguageListCellRenderer implements ListCellRenderer , ResourceDependent {

	//Resource reference
	private ResourceReference reference = null;
	private PolyglotStringLoader loader;
	private String languageID = null;
	
	//The string group of all strings for this class
	private final static String sgroup = "fs.polyglot.LanguageListCellRenderer";
	
	//Colors codes
	/**
	 * HTML code for the color of a language id which is fully supported
	 */
	public final static String fullySupportedHTML = "'#33BB44'";
	/**
	 *HTML code for the color of a language id which is not fully supported 
	 */
	public final static String notfullySupportedHTML = "'#FF5500'";
	
	//Icons
	private ImageIcon okIcon;
	private ImageIcon undescribedIcon;
	private ImageIcon warnIcon;
	
	/**
	 * Constructs a cell renderer using the specified resource reference and languageID.
	 * Null values indicate that the fsframework default values should be used
	 */
	public LanguageListCellRenderer(ResourceReference r, PolyglotStringLoader loader, String languageID) {
		assignReference(r);
		this.languageID = (languageID != null)? languageID : PolyglotStringTable.getGlobalLanguageID();
		this.loader = loader != null? loader :PolyglotStringLoader.getDefaultLoader();
	}
	
	@Override
	public Component getListCellRendererComponent(final JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		//Create components
		final JLabel label = new RendererLabel(isSelected, cellHasFocus);
		final JProgressBar bar = new JProgressBar();
		JPanel sumPanel = new JPanel();
		sumPanel.setBackground(label.getBackground());
		sumPanel.setBorder(label.getBorder());
		label.setBorder(null);
		
		
		
		//If the object is not of type Language, return the empty label,
		//otherwise copy values
		if(value instanceof Language) {
			Language lang = (Language)value;
			label.setIcon(lang.isOnlyUsed? undescribedIcon : (lang.isFullySupported()? okIcon : warnIcon));
			String color = lang.isFullySupported()? fullySupportedHTML: notfullySupportedHTML;
			String desc = lang.isOnlyUsed? "<i>" + loader.getString(sgroup + ".nodescription", languageID) + "</i>"
											: lang.description;
			sumPanel.setToolTipText((lang.isOnlyUsed || !lang.isFullySupported()? 
					("<html>" 
					+ (lang.isOnlyUsed? "- " + loader.getString(sgroup + ".onlyused", languageID) : "") 
					+ (lang.isOnlyUsed && !lang.isFullySupported()? "<br>" : "") 
					+ (!lang.isFullySupported()? "- " + loader.getString(sgroup + ".notfullysupported", languageID): "")
					+ "</html>" ) : null));
			String text = "<html><font color=" + color + ">" + lang.id + "</font>: " + 
							desc + "</html>";
			label.setText(text);
			
			bar.setStringPainted(true);
			bar.setValue(lang.supported);
			//bar.setToolTipText(loader.getString(sgroup + ".bartooltip", languageID));
		}
		
		//Layout
		GridBagLayout gbl = new GridBagLayout();
		sumPanel.setLayout(gbl);
		GridBagConstraints clabel = GUIToolbox.buildConstraints(0, 0, 1, 1);
		clabel.weightx = 100;
		clabel.insets = new Insets(0,0,0,5);
		gbl.setConstraints(label, clabel);
		GridBagConstraints cbar = GUIToolbox.buildConstraints(1, 0, 1, 1);
		cbar.insets = new Insets(2,0,2,0);
		gbl.setConstraints(bar, cbar);
		sumPanel.add(label);
		sumPanel.add(bar);
		return sumPanel;
	}

	/**
	 * Assigns a resource reference (if null, the FsfwDefaultReference is used) and reloads
	 * the icons. This doesn't cause any repaints
	 */
	@Override
	public void assignReference(ResourceReference r) {
		reference = (r != null) ? r : FsfwDefaultReference.getDefaultReference();
		okIcon = new ImageIcon(reference.getFullResourcePath(this, "graphics/LanguageListCellRenderer/ok.png"));
		undescribedIcon = new ImageIcon(reference.getFullResourcePath(this, "graphics/LanguageListCellRenderer/undescribed.png"));
		warnIcon = new ImageIcon(reference.getFullResourcePath(this, "graphics/LanguageListCellRenderer/warn.png"));
	}

	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/LanguageListCellRenderer/ok.png");
		tree.addPath("graphics/LanguageListCellRenderer/undescribed.png");
		tree.addPath("graphics/LanguageListCellRenderer/warn.png");
		return tree;
	}

}
