package fs.polyglot.event;

import fs.polyglot.model.Group;
import fs.polyglot.model.PolyglotString;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.model.Variant;

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

	@Override
	public void groupChanged(PolyglotTableModel source, Group group) {
	}

	@Override
	public void groupInserted(PolyglotTableModel source, Group group) {
	}

	@Override
	public void groupRemoved(PolyglotTableModel source, Group group) {
	}

	@Override
	public void stringChanged(PolyglotTableModel source, PolyglotString string) {
	}

	@Override
	public void stringInserted(PolyglotTableModel source, PolyglotString string) {
	}

	@Override
	public void stringRemoved(PolyglotTableModel source, PolyglotString string) {
	}

	@Override
	public void variantChanged(PolyglotTableModel source, Variant variant) {
	}

	@Override
	public void variantInserted(PolyglotTableModel source, Variant variant) {
	}

	@Override
	public void variantRemoved(PolyglotTableModel source, Variant variant) {
	}

}
