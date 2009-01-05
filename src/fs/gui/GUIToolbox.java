package fs.gui;

import java.awt.*;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;

/**
 * Implements frequently used GUI procedures in static methods
 * 
 * @author Simon Hampe
 * 
 */
public class GUIToolbox {

	/**
	 * @return A GridBagConstraints object with x,y, gridwidth, gridheight set
	 *         to the respective parameter values. All other values are
	 *         initialized as follows:<br>
	 *         - weightx = weighty = 0 <br>
	 *         - anchor = CENTER <br>
	 *         - fill = BOTH <br>
	 *         - Insets = (0,0,0,0) <br>
	 *         - ipadx = ipady = 0
	 */
	public static GridBagConstraints buildConstraints(int x, int y, int width,
			int height) {
		return new GridBagConstraints(x, y, width, height, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0);
	}

	/**
	 * Opens a file saving dialog (in the system's language) in the specified
	 * directory with the specified filter. Upon terminating, it checks for
	 * existence of this file. If it already exists, the user is prompted to
	 * confirm, if he wants to overwrite the file (in the global fsframework
	 * language).
	 * 
	 * @param directory
	 * @param filter
	 *            The file name extension filter for the dialog
	 * @param parent
	 *            The parent window of the dialog
	 * @return The chosen file, if the dialog is terminated by approving (and
	 *         the confirmation dialog is terminated by pressing 'Yes') and null
	 *         otherwise
	 */
	public static File saveFileAndConfirm(File directory,
			FileNameExtensionFilter filter, Component parent) {
		JFileChooser chooser = new JFileChooser(directory);
		chooser.setFileFilter(filter);
		int answer = chooser.showSaveDialog(parent);
		if (answer == JFileChooser.APPROVE_OPTION) {
			// Check for existence
			File f = chooser.getSelectedFile();
			if (f.exists()) {
				int confirm = JOptionPane
						.showConfirmDialog(parent, PolyglotStringLoader
								.getDefaultLoader().getString(
										"fs.global.confirmoverwrite",
										PolyglotStringTable
												.getGlobalLanguageID(),
										f.getName()), PolyglotStringLoader
								.getDefaultLoader().getString(
										"fs.global.confirmtitle",
										PolyglotStringTable
												.getGlobalLanguageID()),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (confirm == JOptionPane.YES_OPTION)
					return f;
				else
					return null;
			} else
				return f;
		} else
			return null;
	}

}
