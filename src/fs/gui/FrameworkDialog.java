package fs.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.util.HashSet;

import javax.swing.JDialog;

import fs.event.DataRetrievalListener;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceReference;

/**
 * Most dialogs in fsframework make as well of the ResourceReference mechanism
 * as well as the internal string table. Therefore they have to be initialized
 * with a ResourceReference, a StringLoader and a language ID, which, if null,
 * are initialized to default values. This class takes care of that. All
 * constructors of JDialog have been copied and behave just as the original
 * constructors. <br>
 * This dialog also implements a listener mechanism to inform eventual listeners
 * that a certain set of data is ready for retrieval (usually, when OK has been
 * clicked)
 * 
 * @author Simon Hampe
 * 
 */
public class FrameworkDialog extends JDialog {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = 3358137658609505541L;

	// Resource
	protected ResourceReference resource;
	protected PolyglotStringLoader loader;
	protected String languageID;

	// Listeners
	protected HashSet<DataRetrievalListener> listeners = new HashSet<DataRetrievalListener>();

	// LISTENER MECHANISM

	/**
	 * Adds a data retrieval listener
	 */
	public void addDataRetrievalListener(DataRetrievalListener l) {
		if (l != null)
			listeners.add(l);
	}

	/**
	 * Removes a listener
	 */
	public void removeDataRetrievalListener(DataRetrievalListener l) {
		listeners.remove(l);
	}

	/**
	 * This informs all listeners that the data of this dialog is ready for
	 * retrieval and passes it on
	 */
	protected void fireDataReady(Object data) {
		for (DataRetrievalListener l : listeners)
			l.dataReady(data);
	}

	// FRAMEWORK VALUE SETTING

	/**
	 * Sets the internal resource reference, string loader and language id
	 * 
	 * @param r
	 *            The resource reference. If null, the default reference is used
	 * @param l
	 *            The String Loader, if null, the default loader is used
	 * @param lid
	 *            The language ID. Id null, the global language ID is used
	 */
	protected void setFrameworkValues(ResourceReference r,
			PolyglotStringLoader l, String lid) {
		resource = r != null ? r : FsfwDefaultReference.getDefaultReference();
		loader = l != null ? l : PolyglotStringLoader.getDefaultLoader();
		languageID = lid != null ? lid : PolyglotStringTable
				.getGlobalLanguageID();
	}

	// CONSTRUCTORS

	public FrameworkDialog(ResourceReference r, PolyglotStringLoader l,
			String lid) {
		super();
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Dialog owner, boolean modal, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner, modal);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Dialog owner, String title, boolean modal,
			GraphicsConfiguration gc, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner, title, modal, gc);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Dialog owner, String title, boolean modal,
			ResourceReference r, PolyglotStringLoader l, String lid) {
		super(owner, title, modal);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Dialog owner, String title, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner, title);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Dialog owner, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Frame owner, boolean modal, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner, modal);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Frame owner, String title, boolean modal,
			GraphicsConfiguration gc, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner, title, modal, gc);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Frame owner, String title, boolean modal,
			ResourceReference r, PolyglotStringLoader l, String lid) {
		super(owner, title, modal);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Frame owner, String title, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner, title);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Frame owner, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Window owner, ModalityType modalityType,
			ResourceReference r, PolyglotStringLoader l, String lid) {
		super(owner, modalityType);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Window owner, String title,
			ModalityType modalityType, GraphicsConfiguration gc,
			ResourceReference r, PolyglotStringLoader l, String lid) {
		super(owner, title, modalityType, gc);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Window owner, String title,
			ModalityType modalityType, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner, title, modalityType);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Window owner, String title, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner, title);
		setFrameworkValues(r, l, lid);
	}

	public FrameworkDialog(Window owner, ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(owner);
		setFrameworkValues(r, l, lid);
	}

}
