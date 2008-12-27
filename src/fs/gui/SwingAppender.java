package fs.gui;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.*;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.dom4j.Document;

import fs.log.LogHistoryModel;
import fs.xml.*;

/**
 * This class implements a component, that can display 
 * log4j logger messages. It consists of a JLabel (from which it is derived) for displaying 
 * messages with an icon. It can activate a larger 
 * log history window on double click. The icon itself represents the current
 * status of the application: All OK, warning, or erroneous.
 * This status is, in fact derived from the level of the last logging message. The status to 
 * be derived from a logging level can be configured. By default everything up to INFO is OK, WARNING yields
 * a warning and everything above is an error. <br>
 * A user can append his own application messages, though it would make more
 * sense to wrap these messages in a logger. <br>
 * The component's output is controlled via an internal LogHistoryModel, by default with a slightly 
 * different layout than the default one.
 * 
 * @author Simon Hampe
 *
 */
public class SwingAppender extends JLabel
						   implements ResourceDependent {

	// MODEL *******************************************
	// *************************************************
	
	/**
	 * compiler-generated serial version ID
	 */
	private static final long serialVersionUID = -5803635436768075824L;

	/**
	 * The forwarding appender
	 */
	private ForwardAppender forwardAppender = new ForwardAppender();
	
	/**
	 * The Appender for the label
	 */
	private LabelAppender labelAppender = new LabelAppender();
	
	// VIEW ********************************************
	// *************************************************
	
	/**
	 * The window used to display logging history
	 */
	private LogHistoryWindow window = null;
	
	// RESOURCE ****************************************
	// *************************************************
	
	/**
	 * The icon indicating everything's fine
	 */
	private ImageIcon okIcon = null;
	/**
	 * The icon indicating a warning
	 */
	private ImageIcon warnIcon = null;
	/**
	 * The icon indicating an error
	 */
	private ImageIcon errorIcon = null;
	
	/**
	 * The resource reference used to locate the
	 * icons
	 */
	private ResourceReference resource = null;
	
	/**
	 * The string loader used by this component
	 */
	private PolyglotStringLoader loader = null;
	
	/**
	 * The language ID used for this component
	 */
	private String languageID = null;
	
	// CONTROLLER ********************************
	// *******************************************
	
	/**
	 * This mouse listener switches the visibility state of the associated LogHistoryWindow
	 */
	private MouseListener clickListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			window.setVisible(!window.isVisible());
		}
	};
	
	// CONSTRUCTORS ******************************
	// *******************************************
	
	/**
	 * Constructs a SwingAppender. The label text is empty, the icon indicates ALL OK.
	 * The parameters are also used to initialize the associated LogHistoryWindow
	 * @param logHistoryTitle The title of the associated LogHistoryWindow
	 * @param r The resource reference used to obtain the icons. If null, the default reference is used
	 * @param l The string loader used to obtain the tooltips and similar
	 * @param langID The ID of the language in which this component should display its native texts
	 */
	public SwingAppender(String logHistoryTitle, ResourceReference r, PolyglotStringLoader l, String langID) {
		//Copy parameters		
		resource = (r != null) ? r : FsfwDefaultReference.getDefaultReference();
		loader = (l != null) ? l : PolyglotStringLoader.getDefaultLoader();
		languageID = ( langID != null) ? langID : PolyglotStringTable.getGlobalLanguageID();
		
		//Initialize LogWindow
		window = new LogHistoryWindow(logHistoryTitle, resource,loader,languageID);
		window.setVisible(false);
		
		//Initialize icons 
		reloadResources();
		
		//Init state
		setText("");
		setIcon(okIcon);
		setToolTipText(loader.getString("fs.gui.SwingAppender.tooltip", languageID));
		addMouseListener(clickListener);
	}
	
	/**
	 * Constructs a SwingAppender using the default resource reference and string loader and the global 
	 * language id.
	 * @param logHistoryTitle The title of the associated LogHistoryWindow
	 */
	public SwingAppender(String logHistoryTitle) {
		this(logHistoryTitle, null, null, null);
	}
	
	// BEHAVIOR CONTROL METHODS ******************
	// *******************************************
	
	
	/**
	 * @return The LogHistoryWindow which saves the logging history of this component. 
	 */
	public LogHistoryWindow getWindow() {
		return window;
	}

	/**
	 * Sets the window which saves the logging history of this component. If the parameter is null,
	 * this call is ignored.
	 */
	public void setWindow(LogHistoryWindow window) {
		if(window != null) this.window = window;
	}

	/**
	 * @return the appender which forwards logs to the label and
	 * the LogHistoryWindow. This is the appender that should be added to
	 * any Loggers the messages of which should be displayed in this component.
	 * For manipulating output format however, the LogHistoryWindow or the labelAppender
	 * have to be obtained.
	 */
	public ForwardAppender getModel() {
		return forwardAppender;
	}

	/**
	 * @return the appender used to print log messages to the label. This appender should not be
	 * added directly to any logger. Instead, the forwardAppender should be used, since it forwards logs 
	 * to the label appender and the log history window
	 */
	public Appender getLabelAppender() {
		return labelAppender;
	}
	
	/**
	 * @return The Level of the last message appended
	 */
	public Level getLastLevel() {
		return labelAppender.getLastLevel();
	}

	/**
	 * @return The data model of the associated LogHistoryWindow 
	 * @see fs.gui.LogHistoryWindow#getModel()
	 */
	public LogHistoryModel getLogHistoryModel() {
		return window.getModel();
	}
	
	/**
	 * Sets the icon for ALL OK to the specified parameter. You should call reloadIcon()
	 * afterwards to make the change immediately visible
	 * @param okIcon the okIcon to set
	 */
	public void setOkIcon(ImageIcon okIcon) {
		this.okIcon = okIcon;
	}

	/**
	 * Sets the icon for WARNING to the specified parameter. You should call reloadIcon()
	 * afterwards to make the change immediately visible
	 * @param warnIcon the warnIcon to set
	 */
	public void setWarnIcon(ImageIcon warnIcon) {
		this.warnIcon = warnIcon;
	}

	/**
	 * Sets the icon for ERROR to the specified parameter. You should call reloadIcon()
	 * afterwards to make the change immediately visible
	 * @param errorIcon the errorIcon to set
	 */
	public void setErrorIcon(ImageIcon errorIcon) {
		this.errorIcon = errorIcon;
	}

	/**
	 * Delegates the call to LogHistoryWindow.saveLog(java.io.File). Tries to
	 * save the log history to the specified File.
	 * @throws IOException
	 * @see fs.gui.LogHistoryWindow#saveLog(java.io.File)
	 */
	public void saveLog(File f) throws IOException {
		window.saveLog(f);
	}

	/**
	 * Sets the label' icon according to the level of the last message and repaints. This message should for 
	 * example be used after a change of icons 
	 */
	public void reloadIcon() {
		try {
			Level l = getLastLevel();
			if(l == null) {
				setIcon(okIcon); return;
			}
			if(l.isGreaterOrEqual(Level.ERROR)) {
				setIcon(errorIcon);
				return;
			}
			if(l.isGreaterOrEqual(Level.WARN)) {
				setIcon(warnIcon);
				return;
			}
			setIcon(okIcon);
			}
		finally {
			repaint();
		}
	}
	
	// RESOURCEDEPENDENT METHODS *****************
	// *******************************************
	
	/**
	 * Reloads the icons and sets the label's icon to the appropriate one. 
	 */
	public void reloadResources() {
		okIcon = new ImageIcon(resource.getFullResourcePath(this, "graphics/SwingAppender/ok.png"));
		warnIcon = new ImageIcon(resource.getFullResourcePath(this, "graphics/SwingAppender/warn.png"));
		errorIcon = new ImageIcon(resource.getFullResourcePath(this, "graphics/SwingAppender/error.png"));
		reloadIcon();
	}
	
	/**
	 * Assigns a resource reference (but does not reload anything) If r == null, the default
	 * reference is used. The resource reference of the associated LogHistoryWindow remains untouched.
	 */
	public void assignReference(ResourceReference r) {
		resource = (r == null)? FsfwDefaultReference.getDefaultReference() : r;		
	}

	/**
	 * This class expects to find three icon graphics in a folder
	 * (basedir)/graphics/SwingAppender/, that is ok.png, warning.png
	 * and error.png and the fsframework string table language/fsfwStringTable.xml
	 * @see fs.xml.ResourceDependent#getExpectedResourceStructure()
	 */
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree("basedir");
		for(String imgName : Arrays.asList("ok.png","warn.png","error.png")) {
			tree.addPath("graphics/SwingAppender/" + imgName);
		}
		tree.addPath("language/fsfwStringTable.xml");
		return tree;
		
	}
	
	/**
	 * This class implements an appender that does nothing
	 * but forward any appended logging events to the Label's
	 * logging model and the LogHistoryWindow's logging model
	 * @author Simon Hampe
	 *
	 */
	private class ForwardAppender extends AppenderSkeleton {

		/**
		 * Forward the logging event to the JLabel's and the LogHistoryWindow's appender
		 */
		@Override
		protected void append(LoggingEvent arg0) {
			labelAppender.doAppend(arg0);
			window.getModel().doAppend(arg0);
		}

		/**
		 * Closes the appender. 
		 */
		@Override
		public void close() {
			closed = true;
		}

		/**
		 * Returns false, since this appender only forwards
		 */
		@Override
		public boolean requiresLayout() {
			return false;
		}
		
	}
		
	/**
	 * This class implements the appender for the label displaying the log message.
	 * It sets the label's text to the new log message and adapts the icon 
	 * according to the LoggingEvents level.
	 * @author Simon Hampe
	 *
	 */
	private class LabelAppender extends AppenderSkeleton {

		/**
		 * The level of the last log appended
		 */
		private Level lastLevel = null;
		
		/**
		 * @return The level of the last log appended (or null, if no message was appended)
		 */
		public Level getLastLevel() {
			return lastLevel;
		}
		
		public LabelAppender() {
			setLayout(new PatternLayout("%m"));
		}
		
		/**
		 * Sets the label's text to the formatted logging message and adapts the icon appropriately
		 */
		@Override
		protected void append(LoggingEvent arg0) {
			lastLevel = arg0.getLevel();
			setText(getLayout().format(arg0));
			if(arg0.getLevel().isGreaterOrEqual(Level.ERROR)) {
				setIcon(errorIcon);
				return;
			}
			if(arg0.getLevel().isGreaterOrEqual(Level.WARN)) {
				setIcon(warnIcon);
				return;
			}
			setIcon(okIcon);
			repaint();
		}

		/**
		 * Sets closed to true
		 */
		@Override
		public void close() {
			closed = true;
		}

		/**
		 * This appender has its own layout, thus this returns false
		 */
		@Override
		public boolean requiresLayout() {
			return false;
		}
		
	}
	
}
