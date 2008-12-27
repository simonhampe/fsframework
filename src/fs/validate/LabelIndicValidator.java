package fs.validate;

import java.util.HashMap;

import javax.swing.ImageIcon;
import fs.gui.SwitchIconLabel;

/**
 * This class implements a {@link ComponentValidator}, where each registered component may be linked 
 * to a {@link SwitchIconLabel}, which is modified according to the validation status of the component. The Validator is
 * created with three icons, one for 'correct' state , one for 'warning' and one for 'incorrect' state. After each validation run, the 
 * icon of the label for each Component is set accordingly. A tooltip text can also be set. 
 * @author Simon Hampe
 */
public abstract class LabelIndicValidator<T> extends AbstractComponentValidator<T> {

	//Icons
	private ImageIcon correct;
	private ImageIcon warning;
	private ImageIcon incorrect;
	
	//Map of labels
	private HashMap<T, SwitchIconLabel> labels = new HashMap<T, SwitchIconLabel>();
	
	/**
	 * This creates a validator which will set the icons of associated labels according to the
	 * content status of the component. It is recommended, that all three icons are of the same size (if they aren't null), so
	 * that the label will not change its size.
	 * @param correct The icon for correct content (If null, no icon is displayed)
	 * @param warning The icon for content that produces a warning ( If null, no icon is displayed)
	 * @param incorrect The icon for incorrect content (If null, no icon is displayed)
	 */
	public LabelIndicValidator(ImageIcon correct, ImageIcon warning, ImageIcon incorrect) {
		this.correct = correct;
		this.warning = warning;
		this.incorrect = incorrect;
	}
	
	/**
	 * Runs a validation on all registered components and will set the icons of the associated labels
	 * accordingly. If an icon for a certain state has been set to null, the icon is simply disabled
	 * (i.e. setIconVisible(false) is called).
	 */
	@Override
	public ValidationResult validate() {
		ValidationResult result =  super.validate();
		for(Object c : result.getResults().keySet()) { 
			SwitchIconLabel l = labels.get(c);
			ValidationResult.Result cr = result.getResults().get(c); 
			if(l != null) {
				ImageIcon iconToSet = (cr == ValidationResult.Result.CORRECT? correct : 
					(cr == ValidationResult.Result.WARNING? warning : incorrect));
				if(iconToSet != null) {
					l.setIconReference(iconToSet);
					l.setIconVisible(true);
				}
				else l.setIconVisible(false);								
			}
			
		}
		return result;
	}

	/**
	 * Adds a component for validation and associates it to the specified label
	 */
	public void addComponent(T component, SwitchIconLabel associatedLabel) {
		labels.put(component, associatedLabel);
		addComponent(component);
	}

	/**
	 * Sets the tooltip text of the label associated to the component. If no label is 
	 * associated, this call has no effect
	 */
	public void setToolTipText(T component, String text) {
		SwitchIconLabel l = labels.get(component);
		if(l != null) l.setToolTipText(text);
	}
	


}
