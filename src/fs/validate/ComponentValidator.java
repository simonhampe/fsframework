package fs.validate;

import javax.swing.event.ChangeListener;

/**
 * A ComponentValidator is an object that can verify the (usually user-generated) content 
 * of an object (supposedly a graphical component) and associate a boolean value to it, 
 * where true normally indicates a correct value.<br>
 * Each ComponentValidator can be used for several components, registered via addComponent and to which
 * he should add himself as a listener in some way. ChangeListeners can be notified
 * of any data change in registered components so that they can call for a validation run.
 * @author Simon Hampe
 *
 */
public interface ComponentValidator<T> {

	/**
	 * Validates all registered components and returns 'true' as overall result, if the content of 
	 * all components is correct. All validation listeners should be notified of the
	 * result at the end of this method
	 */
	public ValidationResult validate();
	
	/**
	 * Registers a component for validation. Its content should be immediately 
	 * validated and all listeners notified
	 */
	public void addComponent(T component);
	
	/**
	 * Unregisters a component. No more validation occurs on this component. 
	 * The other components should be validated immediately and all listeners 
	 * should be notified.
	 */
	public void removeComponent(T component);
	
	/**
	 * Adds a validation listener that is notified of the result of any validation runs.
	 */
	public void addChangeListener(ChangeListener l);
	
	/**
	 * Removes a change listener
	 */
	public void removeChangeListener(ChangeListener l);
	
}
