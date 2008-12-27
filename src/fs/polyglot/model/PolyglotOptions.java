package fs.polyglot.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.*;
import org.dom4j.tree.DefaultElement;

import fs.xml.FsfwConfigurator;
import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * This class summarizes all options that can be set for the application Polyglot. It can 
 * be configured via XML. It also takes care of all fsframework options
 * @author Simon Hampe
 *
 */
public class PolyglotOptions extends FsfwConfigurator implements XMLConfigurable {

	// OPTIONS *********************************
	// *****************************************
	
	/**
	 * The maximal size of the lastfiles list. By default 10.
	 */
	private int maxfilenumber = 10;
	
	/**
	 * A list of all recently opened files. By default the empty list.
	 * The size of this list will alway be less than or equal to maxfilenumber.
	 */
	private ArrayList<File> lastfiles = new ArrayList<File>();
	
	
	
	// CONSTRUCTORS ****************************
	// *****************************************
	
	/**
	 * This will construct a PolyglotOptions object with XMLConfigurable ID "polyglotoptions"
	 * and a default configuration
	 */
	public PolyglotOptions() {
		super("polyglotoptions");
	}
	
	/**
	 * This will construct a PolyglotOptions object with the specified string (null will be replaced by 
	 * the default value "polyglotoptions"), which will be configured by n.
	 */
	public PolyglotOptions(String xmlID, Node n) {
		super((xmlID == null? "polyglotoptions" : xmlID), n);
	}
	
	// GETTERS AND SETTERS *********************
	// *****************************************
	
	
	
	/**
	 * @return the maximal number of recently opened files that will be recorded
	 */
	public int getMaxfilenumber() {
		return maxfilenumber;
	}

	/**
	 * Sets the maximal number of recently opened files that will be recorded. If the 
	 * parameter is < 0, it will be replaced by 0. If the list of recently opened files
	 * is longer than this number, it will be truncated.
	 */
	public void setMaxfilenumber(int maxfilenumber) {
		this.maxfilenumber = Math.max(0, maxfilenumber);
		while(lastfiles.size() > this.maxfilenumber) {
			lastfiles.remove(this.maxfilenumber);
		}
	}

	/**
	 * @return A copy of the list of recently opened files.
	 */
	public ArrayList<File> getLastfiles() {
		return new ArrayList<File>(lastfiles);
	}

	/**
	 * Sets the list of files which have been opened last. The maximal size of this list 
	 * will be determined by maxfilenumber. If the list size is greater than this number, the list
	 * will be truncated to the first maxfilenumber elements.
	 * @param lastfiles A list of files. Null will be replaced by the empty list
	 */
	public void setLastfiles(ArrayList<File> lastfiles) {
		this.lastfiles = new ArrayList<File>();
		if(lastfiles != null){
			for(int i = 0; i < Math.min(maxfilenumber, lastfiles.size()); i++) {
				this.lastfiles.add(lastfiles.get(i));
			}
		}
	}
	
	// CONFIGURATION ***************************
	// *****************************************

	/**
	 * For every possible option, this will look for the appropriate first-level node (as specified either in
	 * FsfwConfigurator.xsd or PolyglotOptions.xsd) and configure the corresponding option. If a node does not
	 * exist or contains invalid content, the corresponding option remains unchanged.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void configure(Node n) throws XMLWriteConfigurationException {
		//Configure fsframework options
		super.configure(n);
		//Add other options
		
		Node mfn = n.selectSingleNode("./maxfilenumber");
		if(mfn != null) {
			try {
				int mfnumber = Integer.parseInt(mfn.getText());
				setMaxfilenumber(mfnumber);
			}
			catch(NumberFormatException nfe) {
				//Ignore
			}
		}
	
		try  {
			List<Node> lfo = n.selectNodes("./lastfile");
			ArrayList<File> newlist = new ArrayList<File>();
			for(Node fo : lfo) {
				newlist.add(new File(fo.getText()));
			}
			setLastfiles(newlist);
		}
		catch(ClassCastException ce) {
			//Ignore
		}
	}

	/**
	 * Returns a root node of name "polyglotoptions" with a first level node for each option, whose text
	 * specifies the chosen value.
	 */
	@Override
	public Element getConfiguration() throws XMLReadConfigurationException {
		//Get fsframework options
		Element e = super.getConfiguration();
		//Add other options
		DefaultElement mfn = new DefaultElement("maxfilenumber");
		mfn.setText(new Integer(maxfilenumber).toString());
		e.add(mfn);
		
		for(File f : lastfiles) {
			DefaultElement lf = new DefaultElement("lastfile");
			lf.setText(f.getAbsolutePath());
			e.add(lf);
		}
		
		return e;
	}

	/**
	 * Returns true
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

}
