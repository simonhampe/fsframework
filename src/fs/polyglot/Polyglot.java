package fs.polyglot;

import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import fs.polyglot.model.*;
import fs.polyglot.view.*;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;
import fs.xml.XMLToolbox;
import fs.xml.XMLWriteConfigurationException;

/**
 * This is an application for editing PolyglotStringTables. A full documentation
 * is found under (fsframework)/doc/Polyglot/manual.pdf. This class does nothing
 * more than to initialize fsframework and itself, possibly by reading
 * configurations from xml files and to construct the main frame, which in turn
 * will take care of the view
 * 
 * @author Simon Hampe
 * 
 */
// TODO: Write this manual!
public class Polyglot implements ResourceDependent {

	/**
	 * The associated main application frame
	 */
	private PolyglotFrame mainFrame = null;

	/**
	 * The application options
	 */
	private PolyglotOptions options = null;

	/**
	 * The application root logger
	 */
	private Logger polyglotLogger = Logger.getLogger("fs.polyglot");

	/**
	 * The polyglot options file
	 */
	private File configFile = null;

	/**
	 * Starts the application.
	 * 
	 * @param args
	 *            Command line arguments. A list of supported arguments is given
	 *            in the documentation.
	 */
	public static void main(String[] args) {
		// Initialize application
		@SuppressWarnings("unused")
		Polyglot App = new Polyglot(args);

	}

	// CONSTRUCTOR **********************************************
	// **********************************************************

	/**
	 * Constructs a polyglot application using the specified command line
	 * arguments
	 * 
	 * @param args
	 *            Command line arguments. A list of supported arguments is given
	 *            in the documentation. Invalid arguments will be ignored,
	 *            though the error will be logged
	 */
	protected Polyglot(String[] args) {
		// Initialize
		BasicConfigurator.configure();
		// Try to load logging configuration
		// TODO: Load logging config
		polyglotLogger.info("Initializing Polyglot...");

		// Parse command line arguments
		parseCommandLine(args);

		// Load configuration
		options = new PolyglotOptions();
		if (configFile == null || !configFile.exists()) {
			if(configFile != null) polyglotLogger
					.warn("Configuration file "
							+ configFile.getAbsolutePath()
							+ " does not exist. Using default configuration but saving configuration to this file afterwards.");
			else polyglotLogger.warn("No configuration file specified. No configuration will be saved.");
			//Use default
			options.setDefaultDirectory(".");
			options.setGlobalLanguageID("en");
			options.setLastfiles(new ArrayList<File>());
			options.setMaxfilenumber(5);
		} else {
			try {
				Document optionsDocument = XMLToolbox.loadXMLFile(configFile);
				options.configure(optionsDocument.getRootElement());
			} catch (DocumentException e) {
				polyglotLogger.warn("Can't load configuration file "
						+ configFile.getAbsolutePath() + ": " + e.getMessage());
				polyglotLogger
						.warn("Using default configuration. Will try to save configuration to this file afterwards");
			} catch (XMLWriteConfigurationException e) {
				// This will not happen
			}
		}
		options.applyConfiguration();

		// Now load the main frame
		mainFrame = new PolyglotFrame(null, options);

	}

	// INITIALIZATION METHODS **************************************
	// *************************************************************

	/**
	 * This will parse any valid options and apply them and log any parsing
	 * errors.
	 */
	protected void parseCommandLine(String[] args) {
		if (args == null)
			return;
		int i = 0;
		while (args.length > i) {
			// Check for configuration file option
			if (args[i].equals("-c")) {
				if (args.length > i + 1) {
					configFile = new File(args[i + 1]);
					i = i + 2;
				} else {
					polyglotLogger
							.warn("Missing command line argument for '-c': No configuration file given");
					i++;
				}
				continue;
			}
			// Log any invalid option
			polyglotLogger.warn("Invalid command line argument: " + args[i]);
			i++;
		}
	}

	// GETTERS AND SETTERS ***************************************
	// ***********************************************************

	/**
	 * @return The application options of this polyglot instance
	 */
	public PolyglotOptions getOptions() {
		return options;
	}

	// RESOURCEDEPENDENT METHODS ***********************************
	// *************************************************************

	/**
	 * Polyglot uses the fsframework resource reference, so a call of this
	 * method throws an exception
	 * 
	 * @throws UnsupportedOperationException
	 *             - always
	 */
	@Override
	public void assignReference(ResourceReference r) {
		throw new UnsupportedOperationException(
				"Can't assign a resource reference to polyglot. The fsframework reference has to be used");
	}

	/**
	 * Summarizes the resource needs of the complete application
	 */
	@Override
	public Document getExpectedResourceStructure() {
		// So far, only mainframe needs any resources
		if (mainFrame != null)
			return mainFrame.getExpectedResourceStructure();
		else
			return new XMLDirectoryTree();
	}

}
