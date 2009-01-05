package fs.polyglot.validate;

import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;

import fs.validate.LabelIndicValidator;
import fs.validate.ValidationResult.Result;

/**
 * Implements a validator that gives off a warning, if content is empty after
 * trimming, but otherwise regards it always as correct
 * 
 * @author Simon Hampe
 * 
 */
public class NonEmptyWarner extends LabelIndicValidator<JTextComponent> {

	/**
	 * Constructs a validator
	 * 
	 * @param correct
	 *            The icon for correct state
	 * @param warning
	 *            The icon for warning state
	 * @param incorrect
	 *            never used
	 */
	public NonEmptyWarner(ImageIcon correct, ImageIcon warning,
			ImageIcon incorrect) {
		super(correct, warning, incorrect);
	}

	/**
	 * Adds this validator as DocumentListener
	 */
	@Override
	protected void registerToComponent(JTextComponent component) {
		if (component != null)
			component.getDocument().addDocumentListener(this);
	}

	/**
	 * Removes this validator as DocumentListener
	 */
	@Override
	protected void unregisterFromComponent(JTextComponent component) {
		if (component != null)
			component.getDocument().removeDocumentListener(this);
	}

	/**
	 * Returns WARNING, if the content is the empty string (after trimming) (or
	 * the component is null) and CORRECT otherwise
	 */
	@Override
	public Result validate(JTextComponent component) {
		if (component == null)
			return Result.WARNING;
		if (component.getText().trim().equals(""))
			return Result.WARNING;
		else
			return Result.CORRECT;
	}

}
