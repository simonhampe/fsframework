package fs.gui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * Defines a label that is associated to an ImageIcon reference. The difference to a normal label is, that 
 * this label always returns a preferredSize the size of the label as if was displaying also this icon.
 * The idea behind this is that the icon can be 'switched on and off' without any change in the layout.
 * Of course this label can be assigned an icon different from its reference, although this would produce 
 * unpredictable behavior. The invariant layout is created by assigning an empty dummy icon each time the
 * label actually doesn't have an icon. To use this functionality, each icon assignment should take place
 * via get- and setIconReference.
 * @author Simon Hampe
 *
 */

public class SwitchIconLabel extends JLabel {

	/**
	 * compiler-generated version ID
	 */
	private static final long serialVersionUID = -9039099975063053702L;
	
	/**
	 * The icon reference
	 */
	private Icon iconReference = null;
	
	/**
	 * The dummy icon used to simulate a null icon
	 */
	private final DummyIcon dummy = new DummyIcon(null);
	
	/**
	 * Generates a JLabel with icon reference null.
	 */
	public SwitchIconLabel() {
		super();
		setIcon(dummy);
	}

	/**
	 * Generates a JLabel with the specified Icon as icon reference
	 */
	public SwitchIconLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		iconReference = image;
		dummy.adaptBounds(image);
		if(image == null) setIcon(dummy);
	}

	/**
	 * Generates a JLabel with the specified Icon as icon reference
	 */
	public SwitchIconLabel(Icon image) {
		super(image);
		iconReference = image;
		dummy.adaptBounds(image);
		if(image == null) setIcon(dummy);
	}

	/**
	 * Generates a JLabel with the specified Icon as icon reference
	 */
	public SwitchIconLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		iconReference = icon;
		dummy.adaptBounds(icon);
		if(icon == null) setIcon(dummy);
	}

	/**
	 * Generates a JLabel with icon reference null
	 */
	public SwitchIconLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		setIcon(dummy);
	}

	/**
	 * Generates a JLabel with icon reference null
	 */
	public SwitchIconLabel(String text) {
		super(text);
		setIcon(dummy);
	}

	// BEHAVIORAL CONTROL ********************************
	// ***************************************************
	
	/**
	 * Switches visibility of the icon, i.e.: If the icon of this label is not null (or the dummy) at the moment, it is set to the dummy.
	 * If the icon of this label is null(or the dummy icon), it is set to the iconReference. Note that this method is oblivious of 
	 * any call of setIcon(..) with an icon different from the reference, so the icon will always be set to 
	 * the <i> reference </i> icon. This method does not repaint automatically.
	 */
	public void switchIcon() {
		if(getIcon() != dummy) setIcon(dummy);
		else setIcon(iconReference);
	}
	
	/**
	 * Explicitly sets the visibility of the label's icon. Note that this method is oblivious of 
	 * any call of setIcon(..) with an icon different from the reference, so the icon will always be set to 
	 * the <i> reference </i> icon. This method does not repaint automatically.
	 */
	public void setIconVisible(boolean isVisible) {
		if(isVisible) setIcon(iconReference);
		else setIcon(dummy);
	}

	/**
	 * @return the iconReference
	 */
	public Icon getIconReference() {
		return iconReference;
	}

	/**
	 * @param iconReference the iconReference to set. If the label currently has a visible icon, setIcon(..)
	 * will also be called with iconReference as argument.
	 */
	public void setIconReference(Icon iconReference) {
		this.iconReference = iconReference;
		dummy.adaptBounds(iconReference);
		if(getIcon() != dummy )setIcon(iconReference);
	}

	/**
	 * An icon that never paints itself, but returns the height and width of an associated icon
	 * @author Simon Hampe
	 *
	 */
	private class DummyIcon implements Icon {

		private int width = 0;
		private int height = 0;
		
		/**
		 * Creates a dummy icon of width and height 0
		 */
		public DummyIcon() {
			width = height = 0;
		}
		
		/**
		 * Creates a dummy icon that will return the width and height of ref
		 */
		public DummyIcon(Icon ref) {
			this();
			if(ref != null) {
				width = ref.getIconWidth();
				height = ref.getIconHeight();
			}
		}
		
		@Override
		public int getIconHeight() {
			return height;
		}

		@Override
		public int getIconWidth() {
			return width;
		}
		
		/**
		 * Sets the width of the dummy icon to max(0,w)
		 */
		public void setWidth(int w) {
			width = 0 >= w? 0 : w;
		}

		/**
		 * Sets the height of the dummy icon to max(0,h)
		 */
		public void setHeight(int h) {
			height = 0 >= h? 0 : h;
		}
		
		/**
		 * Sets the dummy icon's width and height to those of i
		 */
		public void adaptBounds(Icon i) {
			if(i != null) {
				width = i.getIconWidth();
				height = i.getIconHeight();
			}
			else width = height = 0;
		}
		
		/**
		 * Paints absolutely nothing
		 */
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			//Does nothing
		}
		
	}
	
}
