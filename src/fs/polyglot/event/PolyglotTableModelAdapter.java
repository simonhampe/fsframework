package fs.polyglot.event;

import fs.polyglot.model.PolyglotTableModel;

/**
 * This is a convenience class for creating PolyglotTableModelListeners. All
 * methods of this interface are implemented as empty stubs.
 * 
 * @author Simon Hampe
 * 
 */
public class PolyglotTableModelAdapter implements PolyglotTableModelListener {

	@Override
	public void languageListChanged(PolyglotTableModel source) {
	}

	@Override
	public void stringTableChanged(PolyglotTableModel source) {
	}

	@Override
	public void tableDescriptionChanged(PolyglotTableModel source) {
	}

	@Override
	public void tableIDChanged(PolyglotTableModel source) {
	}

}
