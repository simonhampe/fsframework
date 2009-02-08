package fs.polyglot.view;

import java.awt.Component;
import java.io.File;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.CellEditor;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import org.dom4j.Document;

import fs.gui.SwitchIconLabel;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

public class VariantTableCellEditor extends AbstractCellEditor implements TableCellEditor, ResourceDependent {

	//Resources
	private ResourceReference resource;
	private PolyglotStringLoader loader;
	private String languageID;
	
	//Icon
	private ImageIcon warn;
	
	// CONSTRUCTOR *********************************************
	// *********************************************************
	
	/**
	 * Constructs an editor, using the specified resource reference and string loader and language ID. (null = default value)
	 */
	public VariantTableCellEditor(ResourceReference r, PolyglotStringLoader l, String lang) {
		assignReference(r);
		loader = l != null ? l : PolyglotStringLoader.getDefaultLoader();
		languageID = lang != null? lang : PolyglotStringTable.getGlobalLanguageID();
	}
	
	// INTERFACE METHODS ***************************************
	// *********************************************************
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		JPanel p = new JPanel();
		SwitchIconLabel label = new SwitchIconLabel(warn);
		
		return p;
	}
	
	@Override
	public void cancelCellEditing() {
	}

	@Override
	public Object getCellEditorValue() {
		return null;
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		return false;
	}

	@Override
	public void removeCellEditorListener(CellEditorListener l) {
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return false;
	}

	@Override
	public boolean stopCellEditing() {
		return false;
	}

	// RESOURCEDEPENDENT METHODS ****************************************
	// ******************************************************************
	
	/**
	 * Assigns a reference (if null, uses the default reference) and reloads the icon
	 */
	@Override
	public void assignReference(ResourceReference r) {
		resource = r != null? r : FsfwDefaultReference.getDefaultReference();
		warn = new ImageIcon(resource.getFullResourcePath(this, "graphics/VariantTableCellEditor/warn.png"));
	}

	/**
	 * Expects the icon warn.png in its graphics directory
	 */
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/VariantTableCellEditor/warn.png");
		return tree;
	}

	

}
