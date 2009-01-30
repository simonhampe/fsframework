package fs.gui;

import javax.swing.*;

import org.dom4j.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import fs.xml.*;

/**
 * This is a more concrete implementation of TabComponent. It builds a
 * TabComponent that (optionally) already contains: <br>
 * - A JLabel that can change into an editable text field on double click for
 * changing the label's text.<br>
 * - An iconified JButton (The appearance can be changed), that takes the form
 * of a close button. Automatic removal of the associated tab can be
 * (de)activated. <br>
 * Both components can be accessed and messed around with freely (i.e.: they can be obtained
 * via public methods), all internal methods are written in a robust way, so no exceptions
 * will be caused by possible changes (e.g. one component is set to null).
 * However, unexpected behavior is of course possible. Without any external
 * changes, the standard behavior is the following: The label changes into a
 * text field, when double-clicked. It contains the original label text,
 * completely selected and has requested keyboard focus. When the user presses
 * ENTER, the text field changes back into the label, which contains the new
 * text. When the user presses ESCAPE, it changes back to the label with the
 * original text. Focus loss or arbitrary mouse clicks do not cause the label to
 * reappear. Any of these have to implemented manually. <br>
 * <br>
 * The component can notify potential listeners about changes by firing
 * PropertyChangeEvents for the following events:<br>
 * - Mode has changed (Property name: 'mode', value: 'edit' or 'display')<br>
 * - Text editing has been terminated, by confirmation or abortion (Property
 * name: 'text', value: original and new text. If both values are identical,
 * null and new text are sent)
 * 
 * @author Simon Hampe
 * 
 */

@SuppressWarnings("serial")
public class EditCloseTabComponent extends TabComponent implements
		ResourceDependent {

	// RESOURCE REFERENCE ***********************
	// ******************************************
	/**
	 * The resource reference (Not serialized, since it might be any object)
	 */
	private ResourceReference resource = null;

	// COMPORTMENT FLAGS ************************
	// ******************************************

	/**
	 * Indicates whether the label becomes a text field on double click
	 */
	private boolean replaceLabelOnDblClick = true;

	/**
	 * Indicates whether the component should try to remove the associated tab
	 * from its TabbedPane when the close button is clicked
	 */
	private boolean closeButtonActive = true;

	// COMPONENTS AND GRAPHICS ******************
	// ******************************************

	/**
	 * The button used as 'close'-Button
	 */
	private JButton closeButton = new JButton();
	/**
	 * The label that is by default added to this component
	 */
	private JLabel textLabel = new JLabel();
	/**
	 * The text field that appears instead of the label on double-click
	 */
	private JTextField textEditor = new JTextField();

	/**
	 * The default icon for the closing button
	 */
	private ImageIcon defaultIcon;

	// EVENT HANDLERS ****************************
	// *******************************************

	/**
	 * The mouse event handler for the label (in fact, it is listening to the
	 * whole panel, not to the label)
	 */
	protected MouseListener labelListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			// On double click, activate text field
			if (e.getClickCount() >= 2 && replaceLabelOnDblClick) {
				setToEditMode();
			}
		}
	};

	/**
	 * The keyboard listener for the text field
	 */
	protected KeyListener editorListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			int k = e.getKeyCode();
			if (k == KeyEvent.VK_ENTER) {
				// On ENTER copy changes and leave edit mode
				displayChanges();
			}
			if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_ESCAPE) {
				// On ESCAPE leave edit mode
				setToDisplayMode();
			}
		}
	};

	/**
	 * The action listener for the close button
	 */
	protected ActionListener closeListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (closeButtonActive) {
				closeTab();
			}
		}
	};

	// CONSTRUCTORS ******************************
	// *******************************************

	/**
	 * Creates a standard tab component that contains a label bearing the
	 * specified title and an activated close button represented by an icon.<br>
	 * A component created by this constructor will by default NOT forward any
	 * mouse clicks to the main panel or to any JTabbedPane<br>
	 * All necessary graphical resources will be loaded from the default
	 * resource path. If this is not the correct path, you should use a
	 * different constructor
	 */
	public EditCloseTabComponent(String title) {
		this(title, null, true, true, null);
	}

	/**
	 * Initializes an EditCloseTabComponent with the specified properties:
	 * 
	 * @param title
	 *            The title displayed by the label
	 * @param associatedPane
	 *            The JTabbedPane in which this component will serve as tab
	 *            component
	 * @param replaceLabelOnDblClick
	 *            Indicates whether the label should change into a text field on
	 *            double click for title editing
	 * @param displayCloseButton
	 *            Indicates whether the close button should be displayed
	 * @param r
	 *            The resource reference for locating the icon for the button
	 *            (if this null, the default location will be used).
	 */
	public EditCloseTabComponent(String title, JTabbedPane associatedPane,
			boolean replaceLabelOnDblClick, boolean displayCloseButton,
			ResourceReference r) {
		// Basis initialization without mouse click forwarding
		super(associatedPane);
		setReplaceLabelOnDblClick(replaceLabelOnDblClick);
		resource = (r == null) ? FsfwDefaultReference.getDefaultReference() : r;

		// Component initialization
		textLabel = new JLabel(title);
		// The text field will show the minimum size of
		// a text field containing a standard length text, so
		// that editing is still comfortable if the current text is short
		final JTextField measureField = new JTextField("standard text");
		textEditor = new JTextField(title) {
			/**
			 * compiler-generated version id
			 */
			private static final long serialVersionUID = -2840723449738000832L;

			public Dimension getPreferredSize() {
				Dimension dim = super.getPreferredSize();
				Dimension min = measureField.getPreferredSize();
				dim.width = dim.width >= min.width ? dim.width : min.width;
				dim.height = dim.height >= min.height ? dim.height : min.height;
				return dim;
			}
		};
		// Load icon
		loadIcon();
		// The button will resize itself to fit the icon
		closeButton = new JButton() {
			/**
			 * compiler-generated version id
			 */
			private static final long serialVersionUID = -1746250662485230999L;

			public Dimension getPreferredSize() {
				if (defaultIcon == null)
					return super.getPreferredSize();
				return new Dimension(defaultIcon.getIconWidth(), defaultIcon
						.getIconHeight());
			}
		};
		if (defaultIcon == null)
			closeButton.setText("x");
		else
			closeButton.setIcon(defaultIcon);
		closeButton.setOpaque(false);

		// Event handling
		addMouseListener(labelListener);
		textEditor.addKeyListener(editorListener);
		closeButton.addActionListener(closeListener);

		// Add components and hide text field
		addClickTransparent(textLabel);
		addClickTransparent(textEditor);
		textEditor.setVisible(false);
		addClickTransparent(closeButton);
		closeButton.setVisible(displayCloseButton);
	}

	// BEHAVIOR CONTROL METHODS ****************
	// *****************************************

	/**
	 * @return If the label is replaced by a text field for editing on double
	 *         click
	 */
	public boolean isReplaceLabelOnDblClick() {
		return replaceLabelOnDblClick;
	}

	/**
	 * @return Whether the close button is active, i.e. closes the tab
	 */
	public boolean isCloseButtonActive() {
		return closeButtonActive;
	}

	/**
	 * Sets if the label should be replaced by a text field for editing on
	 * double click
	 */
	public void setReplaceLabelOnDblClick(boolean replaceLabelOnDblClick) {
		this.replaceLabelOnDblClick = replaceLabelOnDblClick;
	}

	/**
	 * Hides the text field (if visible) and displays the label. Call is ignored
	 * if text field is already invisible and label already visible
	 */
	public void setToDisplayMode() {
		if (textEditor != null)
			textEditor.setVisible(false);
		if (textLabel != null)
			textLabel.setVisible(true);
		repaint();
		// Notify of mode change
		firePropertyChange("mode", "edit", "display");
	}

	/**
	 * If the component is in edit mode, it changes the label's text into the
	 * current text of the text field and calls
	 * EditCloseTabComponent.setToDisplayMode(). If the component is in display
	 * mode, this call is ignored
	 */
	public void displayChanges() {
		if (textEditor != null && textLabel != null) {
			if (textEditor.isVisible()) {
				String oldtext = textLabel.getText();
				String newtext = textEditor.getText();
				textLabel.setText(newtext);
				setToDisplayMode();
				firePropertyChange("text", oldtext.equals(newtext) ? null
						: oldtext, newtext);
			}
		}
	}

	/**
	 * Hides the label (if visible) and activates the text field. The text field
	 * will react as usual, i.e. disappear when the user presses ESCAPE/ENTER,
	 * etc. If the TextField is already visible when this method is called, the
	 * call is ignored
	 */
	public void setToEditMode() {
		if (textEditor != null) {
			if (textEditor.isVisible())
				return;
			textEditor.setVisible(true);
			if (textLabel != null) {
				textLabel.setVisible(false);
				textEditor.setText(textLabel.getText());
			}
			textEditor.requestFocus();
			textEditor.selectAll();
			repaint();
			// Notify of mode change
			firePropertyChange("mode", "display", "edit");
		}
	}

	/**
	 * Sets the visibility of the close button and repaints the component
	 */
	public void setCloseButtonVisibility(boolean visible) {
		if (closeButton != null)
			closeButton.setVisible(visible);
		repaint();
	}

	/**
	 * Activates or deactivates the close button. If it is active then on click
	 * the application will try to remove the associated tab from its
	 * JTabbedPane. This will not change visibility
	 */
	public void activateCloseButton(boolean active) {
		closeButtonActive = active;
	}

	/**
	 * Activates or deactivates the text field. If the text field is visible
	 * while this method deactivates it, this will have the same effect, as if
	 * editing was aborted
	 */
	public void activateEditor(boolean active) {
		if (textEditor != null) {
			if (!active && textEditor.isVisible())
				setToDisplayMode();
		}
		replaceLabelOnDblClick = active;
		repaint();
	}

	/**
	 * Tries to remove the associated tab (if any) from its JTabbedPane
	 */
	public void closeTab() {
		if (paneToNotify != null) {
			int i = paneToNotify.indexOfTabComponent(this);
			if (i != -1) {
				paneToNotify.remove(i);
			}
		}
	}

	// GETTERS/SETTERS FOR COMPONENT PARTS *****
	// *****************************************

	/**
	 * @return the close button
	 */
	public JButton getCloseButton() {
		return closeButton;
	}

	/**
	 * @return the text field for editing the tab's name
	 */
	public JTextField getTextEditor() {
		return textEditor;
	}

	/**
	 * @return the text label for displaying the tab's name
	 */
	public JLabel getTextLabel() {
		return textLabel;
	}

	/**
	 * Replaces the close button. The new button will be placed at the last
	 * index of the component.
	 */
	public void setCloseButton(JButton closeButton) {
		// Replace component part
		if (this.closeButton != null)
			remove(this.closeButton);
		if (closeButton != null) {
			closeButton.addActionListener(closeListener);
			add(closeButton, getComponentCount());
			registerClickTransparent(closeButton);
		}
		// Replace pointer
		this.closeButton = closeButton;
		// Repaint
		validate();
	}

	/**
	 * Replaces the text field used to edit the tab's title. The new text field
	 * will be placed at the first index.
	 * 
	 */
	public void setTextEditor(JTextField textEditor) {
		// Replace component
		if (this.textEditor != null)
			remove(this.textEditor);
		if (textEditor != null) {
			textEditor.addKeyListener(editorListener);
			add(textEditor, 0);
			registerClickTransparent(textEditor);
			if (this.textEditor != null)
				textEditor.setVisible(this.textEditor.isVisible());
		}
		// Replace pointer
		this.textEditor = textEditor;
		// Repaint
		validate();
	}

	/**
	 * Replaces the label used to display the tab's title. The label will be set
	 * at the second index
	 */
	public void setTextLabel(JLabel textLabel) {
		// Replace component
		if (this.textLabel != null)
			remove(this.textLabel);
		if (textLabel != null) {
			textLabel.addMouseListener(labelListener);
			add(textLabel, 1);
			registerClickTransparent(textLabel);
			if (this.textLabel != null) {
				textLabel.setVisible(this.textLabel.isVisible());
			}
		}
		// Replace pointer
		this.textLabel = textLabel;
		// Repaint
		validate();
	}

	// RESOURCEDEPENDENT METHODS ***************
	// *****************************************

	/**
	 * Tries to load the ImageIcon for the close button from the current
	 * resource reference (from the default path, if the reference is 0) and
	 * repaints the component
	 */
	protected void loadIcon() {
		defaultIcon = new ImageIcon(resource.getFullResourcePath(this,
				"graphics/EditCloseTabComponent/close.png"));
		repaint();
	}

	/**
	 * Assigns a resource reference for locating the image file necessary for
	 * the close icon. If null, the default path is used. The icon will be
	 * reloaded afterwards and the component repainted
	 * 
	 * @see fs.xml.ResourceDependent#assignReference(fs.xml.ResourceReference)
	 */
	public void assignReference(ResourceReference r) {
		resource = (r == null) ? FsfwDefaultReference.getDefaultReference() : r;
		loadIcon();
	}

	/**
	 * The only resource needed is
	 * "(base directory)/graphics/EditCloseTabComponent/close.png"
	 * 
	 * @see fs.xml.ResourceDependent#getExpectedResourceStructure()
	 */
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree("basedir");
		tree.addPath("graphics/EditCloseTabComponent/close.png");
		return tree;
	}

	// SERIALIZATION METHODS *********************
	// *******************************************

	/**
	 * This component is not serializable, since by its internal architecture,
	 * its behavior cannot be reconstructed. In particular, the registered
	 * ClickTransparent objects cannot be reconstructed.
	 * 
	 * @throws NotSerializableException
	 *             - always
	 */
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		throw new NotSerializableException();
	}

	/**
	 * This component is not serializable, since by its internal architecture,
	 * its behavior cannot be reconstructed. In particular, the registered
	 * ClickTransparent objects cannot be reconstructed.
	 * 
	 * @throws NotSerializableException
	 *             - always
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		throw new NotSerializableException();
	}

}
