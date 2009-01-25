package fs.polyglot.view;

import fs.gui.FrameworkDialog;
import fs.xml.PolyglotStringLoader;
import fs.xml.ResourceReference;

/**
 * This class represents a simple dialog used for configuring the list of edited strings of a StringEditor. Its result is
 * stored in a StringEditorConfiguration Object
 * @author Simon Hampe
 *
 */
public class StringEditorConfigurator extends FrameworkDialog {

	public StringEditorConfigurator(ResourceReference r,
			PolyglotStringLoader l, String lid) {
		super(r, l, lid);
		// TODO Auto-generated constructor stub
	}

}
