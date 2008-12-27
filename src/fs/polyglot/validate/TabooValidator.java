package fs.polyglot.validate;

import java.util.Collection;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.text.JTextComponent;

import fs.validate.LabelIndicValidator;
import fs.validate.ValidationResult.Result;

/**
 * This validator makes sure, a text fields content is not in a given list of 
 * strings (this might be used to ensure uniqueness of ID's, etc...). It's also
 * possible to ensure that the content is not empty (after trimming)
 * @author Simon Hampe
 *
 */
public class TabooValidator extends LabelIndicValidator<JTextComponent> {

	//The list of prohibited strings
	private HashSet<String> tabooList = new HashSet<String>();
	//Is empty correct?
	private boolean canBeEmpty;
	
	/**
	 * Constructs a {@link LabelIndicValidator}, which regards a JTextField's content as
	 * correct, if it is not empty and not in the tabooList, and otherwise as incorrect. 
	 * @param correct The Icon for the associated label, if the content is correct
	 * @param warning never used
	 * @param incorrect The icon for the associated label, if the content is incorrect
	 * @param tabooList A list of strings which are not valid content
	 * @param Indicates whether an empty text field should be regared as correct or not
	 */
	public TabooValidator(ImageIcon correct, ImageIcon warning,
			ImageIcon incorrect, Collection<String> tabooList, boolean canBeEmpty) {
		super(correct, warning, incorrect);
		if(tabooList != null) this.tabooList.addAll(tabooList);
		this.canBeEmpty = canBeEmpty;
	}
	
	/**
	 * This sets the list of incorrect values and notifies all change listeners. If 
	 * it is null, it is interpreted as the empty list.
	 */
	public void setTabooList(Collection<String> tabooList) {
		this.tabooList = new HashSet<String>();
		if(tabooList != null) this.tabooList.addAll(tabooList);
		fireStateChanged(new ChangeEvent(this));
	}
	
	/**
	 * Sets whether the empty string is allowed as content and notifies all change listeners if the new value
	 * is different.
	 */
	public void setCanBeEmpty(boolean canBeEmpty) {
		if(this.canBeEmpty != canBeEmpty) {
			this.canBeEmpty = canBeEmpty;
			fireStateChanged(new ChangeEvent(this));
		}
	}

	/**
	 * Adds this validator as DocumentListener to the TextField
	 */
	@Override
	protected void registerToComponent(JTextComponent component) {
		if(component != null) component.getDocument().addDocumentListener(this);
	}

	/**
	 * Removes this validator as DocumentListener from the TextField
	 */
	@Override
	protected void unregisterFromComponent(JTextComponent component) {
		if(component != null) component.getDocument().removeDocumentListener(this);
	}
	
	/**
	 * Returns CORRECT, if the TextFields content is not in the taboo list (after trimming of whitespaces) and, if canBeEmpty has been set to false, if it isn't empty (after trimming).
	 * Otherwise returns INCORRECT. Returns INCORRECT for a null component
	 */
	@Override
	public Result validate(JTextComponent component) {
		if(component == null) return Result.INCORRECT;
		//Empty text?
		if(!canBeEmpty && component.getText().trim().equals("")) return Result.INCORRECT;
		//TabooList
		for(String taboo : tabooList) {
			if(component.getText().trim().equals(taboo.trim())) return Result.INCORRECT;
		}
		return Result.CORRECT;
	}

}
