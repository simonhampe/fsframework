package fs.validate;

import javax.swing.JButton;

/**
 * This validation validator is linked to a single JButton, which is disabled when an INCORRECT validation occurs and enabled otherwise
 * @author Simon Hampe
 *
 */
public class SingleButtonValidator extends ValidationValidator {

	private JButton button;
	
	/**
	 * Creates a validator which enabled / disabled the specified button according to the result of the validation
	 * (INCORRECT = disabled, everything else = enabled)
	 */
	public SingleButtonValidator(JButton button) {
		this.button = button;
	}
	
	@Override
	public void validationPerformed(ValidationResult result) {
		if(button != null) {
			if(result.getOverallResult() == ValidationResult.Result.INCORRECT) button.setEnabled(false);
			else button.setEnabled(true);
		}
	}

}
