package fs.polyglot.view;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.EventObject;
import java.util.HashSet;

import javax.swing.AbstractCellEditor;
import javax.swing.CellEditor;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import org.dom4j.Document;

import fs.gui.SwitchIconLabel;
import fs.validate.LabelIndicValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.FsfwDefaultReference;
import fs.xml.PolyglotStringLoader;
import fs.xml.PolyglotStringTable;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

public class VariantTableCellEditor extends JPanel implements TableCellEditor, ResourceDependent {

	//Resources
	private ResourceReference resource;
	private PolyglotStringLoader loader;
	private String languageID;
	
	//Data
	private HashSet<String> tabooList = new HashSet<String>(); //List of forbidden language ids

	// CONSTRUCTOR *********************************************
	// *********************************************************
	
	/**
	 * Constructs an editor, using the specified resource reference and string loader and language ID. (null = default value)
	 */
	public VariantTableCellEditor(ResourceReference r, PolyglotStringLoader l, String lang, HashSet<String> taboos) {
		super();
		assignReference(r);
		loader = l != null ? l : PolyglotStringLoader.getDefaultLoader();
		languageID = lang != null? lang : PolyglotStringTable.getGlobalLanguageID();
		if(taboos != null) this.tabooList = new HashSet<String>(taboos);
		
		
		//Init component
		add(label);
		add(entryField);
		
		//Init listeners
		nonEmptyValidator = new LabelIndicValidator<JTextField>(null, warn,warn) {
			@Override
			protected void registerToComponent(JTextField component) {
				component.getDocument().addDocumentListener(this);				
			}
			@Override
			protected void unregisterFromComponent(JTextField component) {
				component.getDocument().addDocumentListener(this);				
			}
			@Override
			public Result validate(JTextField component) {
				//non empty
				if(component.getText().trim().equals("")) {
					setToolTipText(component, loader.getString(sgroup + ".nonempty", languageID));
					return Result.INCORRECT;
				}
				//Not already existing
				if(tabooList.contains(component.getText()) && !component.getText().equals(oldid)) {
					setToolTipText(component, loader.getString(sgroup + ".existant", languageID));
					return Result.INCORRECT;
				}
				//Else correct
				setToolTipText(component, null);
				return Result.CORRECT;
			}
		};
	}
	
	// INTERFACE METHODS ***************************************
	// *********************************************************
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		JPanel editorPanel = new JPanel();
		SwitchIconLabel label = new SwitchIconLabel(warn);
		JTextField entryField = new JTextField();
		//If it is a language id, we have to validate the entry
		if(row == 0) {
			//Extract the current value, which will be of course be allowed as value
			final String oldid = table.getValueAt(row, column).toString();
			LabelIndicValidator<JTextField> langValidator = new LabelIndicValidator<JTextField>(null, warn,warn) {
				@Override
				protected void registerToComponent(JTextField component) {
					component.getDocument().addDocumentListener(this);
				}
				@Override
				protected void unregisterFromComponent(JTextField component) {
					component.getDocument().removeDocumentListener(this);
				}
				@Override
				public Result validate(JTextField component) {
					//non empty
					if(component.getText().trim().equals("")) {
						setToolTipText(component, loader.getString(sgroup + ".nonempty", languageID));
						return Result.INCORRECT;
					}
					//Not already existing
					if(tabooList.contains(component.getText()) && !component.getText().equals(oldid)) {
						setToolTipText(component, loader.getString(sgroup + ".existant", languageID));
						return Result.INCORRECT;
					}
					//Else correct
					setToolTipText(component, null);
					return Result.CORRECT;
				}
			};
		}
		editorPanel.add(label);
		editorPanel.add(entryField);
		return editorPanel;
	}
	
	@Override
	public Object getCellEditorValue() {
		return null;
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
