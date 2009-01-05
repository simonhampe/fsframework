package fs.polyglot.view;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * Constructs a label which can be used by CellRenderer classes. This class is
 * designed to unify color schemes of different renderers
 * 
 * @author Simon Hampe
 * 
 */
public class RendererLabel extends JLabel {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = 7380789103751070995L;

	// Colors
	/**
	 * The background color of a standard label, not selected (white) and of the
	 * border of a label which is not Selected and doesn't have focus
	 */
	public final static Color normalBkg = new Color(255, 255, 255);
	/**
	 * The border color of a selected label
	 */
	public final static Color selectedBorder = new Color(0, 0, 230);
	/**
	 * The background and border color of a selected label (which doesn't have
	 * focus)
	 */
	public final static Color selectedBkg = new Color(211, 220, 229);

	public RendererLabel(boolean isSelected, boolean cellHasFocus) {
		super();
		setOpaque(true);
		setBackground(isSelected ? selectedBkg : normalBkg);
		setBorder(BorderFactory
				.createLineBorder(isSelected ? (cellHasFocus ? selectedBorder
						: selectedBkg) : normalBkg));
	}

}
