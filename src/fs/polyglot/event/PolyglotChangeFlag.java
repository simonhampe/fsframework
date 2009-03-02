package fs.polyglot.event;

import fs.event.DocumentChangeFlag;
import fs.polyglot.model.PolyglotTableModel;

/**
 * Does exactly the same thing as DocumentChangeFlag, except that it can listen directly
 * to a polyglot table model
 * @author Simon Hampe
 *
 */
public class PolyglotChangeFlag extends DocumentChangeFlag implements PolyglotTableModelListener{

	@Override
	public void languageListChanged(PolyglotTableModel source) {
		setChangeFlag(true);
	}

	@Override
	public void stringTableChanged(PolyglotTableModel source) {
		setChangeFlag(true);
	}

	@Override
	public void tableDescriptionChanged(PolyglotTableModel source) {
		setChangeFlag(true);
	}

	@Override
	public void tableIDChanged(PolyglotTableModel source) {
		setChangeFlag(true);
	}

}
