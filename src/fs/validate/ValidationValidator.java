package fs.validate;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class serves as a summarizing container for several {@link ComponentValidator}s.
 * It listens to each registered validator. On a stateChanged event, it calls a validation
 * on each validator and afterwards the abstract method validationPerformed with the 
 * result as argument. This can for example be used in dialogs, where the dialog reacts
 * according to whether all entries are correct or not.
 * @author Simon Hampe
 *
 */
@SuppressWarnings("unchecked") //Since this Validator is supposed to summarize ComponentValidators of any type, the set cv cannot be parametrized.
public abstract class ValidationValidator implements ChangeListener {

	//List of registered componentvalidators
	private HashSet<ComponentValidator> cv = new HashSet<ComponentValidator>();
	
	/**
	 * Registers a validator and adds this object as a change listener
	 */
	public <T> void addValidator(ComponentValidator<T> v) {
		if(v != null) {
			cv.add(v);
			v.addChangeListener(this);
		}
	}
	
	/**
	 * Unregisters the validator and removes this object as change listener
	 */
	public <T> void removeValidator(ComponentValidator<T> v) {
		if(cv.contains(v)) {
			cv.remove(v);
			v.removeChangeListener(this);
		}
	}
	
	/**
	 * Performs a validation run on all registered {@link ComponentValidator}s and calls the method
	 * validationPerformed with the final result. 
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		//Validate all cv's
		ValidationResult.Result result = ValidationResult.Result.CORRECT;
		HashMap<Object, ValidationResult.Result> idvResults = new HashMap<Object, ValidationResult.Result>();
		for(ComponentValidator v : cv) {
			ValidationResult vresult = v.validate();
			result = ValidationResult.min(result, vresult.getOverallResult());
			idvResults.putAll(vresult.getResults());
		}
		//Call validationPerformed
		validationPerformed(new ValidationResult(result,idvResults));
	}
	
	/**
	 * Calls stateChanged with a null event, i.e. causes a complete validation run.
	 */
	public void validate() {
		stateChanged(null);
	}
	
	/**
	 * This method is called after a change in a component has been noted and a full validation
	 * run over all registered validators has been performed. The validation result
	 * returns the best result of all these runs as overall result.
	 */
	public abstract void validationPerformed(ValidationResult result);
	
	
}
