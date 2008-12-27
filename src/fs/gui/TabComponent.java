package fs.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*; 

import java.io.*;

/**
 * This class implements a component that can be used as an
 * easier-to-use extensible tab component. It is derived from JPanel (and
 * will by default be non-opaque), so it can be manipulated like a 
 * standard component. It can be configured to automatically select
 * the associated tab in a JTabbedPane after mouse click. However this 
 * can also be realised manually by adding an appropriate listener.
 * Sub-components can be added with a special method, addClickTransparent(Component c) or
 * by registering them as clickTransparents, so
 * that every mouseClicked event on that component will be forwarded to the main JPanel (i.e. potentially interpreted as 
 * tab selection and also forwarded to every MouseListener registered the normal way).
 *
 * @author Simon Hampe
 *
 */
@SuppressWarnings("serial")
public class TabComponent extends JPanel  {

	/**
	 * Indicates whether a click on this panel should 
	 * try to select the associated tab
	 */
	protected boolean selectTabOnClick = false;
	
	/**
	 * The JTabbedPane on which the associated tab
	 * should be selected.
	 */
	protected JTabbedPane paneToNotify = null;
	
	/**
	 * The internal MouseListener that will listen 
	 * to every component added by addClickTransparent and
	 * forward mouseClicked- Events to all registered MouseListeners
	 */
	protected transient MouseListener forwardListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			//Post event to main Panel
			internalListener.mouseClicked(e);
			//Process event regularly
			processMouseEvent(e);
		}
	};
	
	/**
	 * The listener for the component itself
	 */
	protected transient MouseListener internalListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			//Potentially select tab
			if(selectTabOnClick) {
				selectAssociatedTab();
			}
		}
	};
	
	// CONSTRUCTORS *********************************
	// **********************************************
	
	/**
	 * Constructs an empty non-opaque TabComponent (i.e. JPanel) that does
	 * not forward any mouse clicks to an associated
	 * tab
	 */
	public TabComponent() {
		super();
		setOpaque(false);
		//The listener will be added anyway, since
		//the forwarding flag will be checked within it
		addMouseListener(internalListener);
	}
	
	/**
	 * Constructs an empty non-opaque TabComponent that will
	 * on each mouse click invoke associatedPane.selectTabComponent(this);
	 * (if associatedPane is non-null)
	 */
	public TabComponent(JTabbedPane associatedPane) {
		this();
		selectTabOnClick = true;
		paneToNotify = associatedPane;
	}
	
	// GETTER / SETTER METHODS ******************************
	// ******************************************************
	
	/**
	 * @return the JTabbedPane that is notified (if
	 * notification is activated)
	 */
	public JTabbedPane getPaneToNotify() {
		return paneToNotify;
	}

	/**
	 * @return If a possibly associated JTabbedPane is
	 * notified of a mouse click (i.e. the associated tab
	 * is selected)
	 */
	public boolean doesSelectTabOnClick() {
		return selectTabOnClick;
	}

	/**
	 * Sets the JTabbedPane on which the associated tab should
	 * be selected. This should always be the JTabbedPane actually
	 * containing this component, otherwise this won't make
	 * any sense.
	 */
	public void setPaneToNotify(JTabbedPane paneToNotify) {
		this.paneToNotify = paneToNotify;
	}

	/**
	 * Sets whether a possibly associated Tab should be selected
	 * on mouse click
	 */
	public void setSelectTabOnClick(boolean selectTabOnClick) {
		this.selectTabOnClick = selectTabOnClick;
	}

	// SPECIAL METHODS **************************************
	// ******************************************************
		
	
	/**
	 * Tries to select the associated tab, if any JTabbedPane has
	 * been registered. If not, this call is ignored
	 */
	public void selectAssociatedTab() {
		if(paneToNotify != null) {
			int i = paneToNotify.indexOfTabComponent(this);
			if(i != -1) paneToNotify.setSelectedIndex(i);
		}
	}
	
	/**
	 * Convenience method that summarizes adding a component and 
	 * registering it as a click transparent component.
	 * Adds a component in the regular way with the addition 
	 * of registering a MouseListener with it (that will actually
	 * be the same for all components added this way), that will
	 * forward all mouseClicked-Events to the main JPanel (and thus
	 * also potentially select the associated tab). The MouseListener will
	 * only be unregistered on removal, if the component is removed with
	 * removeClickTransparent or by calling unregisterClickTransparent.
	 */
	public void addClickTransparent(Component comp) {
		add(comp);
		registerClickTransparent(comp);
	}
	
	/**
	 * Convenience method that summarizes removing a component and unregistering it
	 * as a click transparent component.
	 * Does the same as {@link java.awt.Component.remove(Component comp) remove(Component comp)}, but 
	 * additionally unregisters the MouseListener that forwarded all
	 * mouseClicked events to the main panel. 
	 */
	public void removeClickTransparent(Component comp) {
		remove(comp);
		unregisterClickTransparent(comp);
	}
	
	/**
	 * Registers a component as a click-transparten component, i.e. all 
	 * mouse clicks on this component will be forwarded to
	 * the TabComponent and all components on it. So registering a component 
	 * not actually added to this TabComponent might result in unpredictable behavior.
	 * Forwarding is realized by adding a MouseListener to 
	 * this component. It can be removed by calling unregisterClickTransparent on
	 * this component
	 */
	public void registerClickTransparent(Component comp) {
		comp.addMouseListener(forwardListener);
	}
	
	/**
	 * Removes the mouse listener that forwards mouse click events
	 * to the TabComponent
	 */
	public void unregisterClickTransparent(Component comp) {
		comp.removeMouseListener(forwardListener);
	}
	
	// SERIALIZATION METHODS *********************
	// *******************************************
	
	/**
	 * This component is not serializable, since by its internal
	 * architecture, its behavior cannot be reconstructed. In particular,
	 * the registered ClickTransparent objects cannot be reconstructed.
	 * @throws NotSerializableException - always
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		throw new NotSerializableException();
	}

	/**
	 * This component is not serializable, since by its internal
	 * architecture, its behavior cannot be reconstructed. In particular,
	 * the registered ClickTransparent objects cannot be reconstructed.
	 * @throws NotSerializableException - always
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		throw new NotSerializableException();
	}
	
}
