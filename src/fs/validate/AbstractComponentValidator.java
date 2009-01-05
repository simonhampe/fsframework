package fs.validate;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This class provides a standard implementation to the class
 * ComponentValidator. Listener mechanism is implemented, the user only has to
 * implement the method {@link AbstractComponentValidator#validate(Object)} ,
 * which is called on every registered component, when validate() is called, and
 * possibly the method
 * {@link AbstractComponentValidator#registerToComponent(Object)}. This
 * ComponentValidator implements some common Swing content listening interfaces,
 * so in the last method it might suffice to add this Validator as a ...listener
 * to the component. On any listener method call, all ChangeListeners are
 * notified
 * 
 * @author Simon Hampe
 */
public abstract class AbstractComponentValidator<T> implements
		ComponentValidator<T>, DocumentListener, ItemListener,
		ListSelectionListener, ChangeListener {

	// List of change listeners
	private HashSet<ChangeListener> listener = new HashSet<ChangeListener>();

	// List of registered components
	private HashSet<T> components = new HashSet<T>();

	/**
	 * Adds a change listener. Doesn't automatically run a validation
	 */
	@Override
	public void addChangeListener(ChangeListener l) {
		listener.add(l);
	}

	/**
	 * Adds the specified component (if it isn't null) and notifies all
	 * listeners. This will also call registerToComponent
	 */
	@Override
	public void addComponent(T component) {
		if (component != null) {
			components.add(component);
			registerToComponent(component);
			fireStateChanged(new ChangeEvent(this));
		}
	}

	/**
	 * Removes a change listener
	 */
	@Override
	public void removeChangeListener(ChangeListener l) {
		listener.remove(l);
	}

	/**
	 * Removes a component from the list of registered components and notifies
	 * all listeners (if this component was actually contained in this list)
	 */
	@Override
	public void removeComponent(T component) {
		if (components.contains(component)) {
			components.remove(component);
			unregisterFromComponent(component);
			fireStateChanged(new ChangeEvent(this));
		}
	}

	/**
	 * Calls {@link AbstractComponentValidator#validate(Object)} on all
	 * registered components and returns CORRECT as overall result, if and only
	 * if all these calls return CORRECT. If one returns a WARNING and none
	 * returns an INCORRECT, WARNING is returned. If one returns an INCORRECT,
	 * this is returned. All validation listeners are notified of the result of
	 * this validation
	 */
	@Override
	public ValidationResult validate() {
		ValidationResult.Result result = ValidationResult.Result.CORRECT;
		HashMap<Object, ValidationResult.Result> idvResults = new HashMap<Object, ValidationResult.Result>();
		for (T c : components) {
			ValidationResult.Result cresult = validate(c);
			result = ValidationResult.min(result, cresult);
			idvResults.put(c, cresult);
		}
		return new ValidationResult(result, idvResults);
	}

	/**
	 * Validates the content of the given component and returns whether it is
	 * 'correct'
	 */
	public abstract ValidationResult.Result validate(T component);

	/**
	 * Registers this ComponentValidator to the component in some way, so that
	 * it is notified of content changes. When a component notifies this
	 * ComponentValidator of a content change,
	 * {@link AbstractComponentValidator#fireStateChanged(ChangeEvent)} should
	 * be called to notify all registered change listeners. For most swing
	 * components it will suffice to add this Validator as ...listener.
	 */
	protected abstract void registerToComponent(T component);

	/**
	 * This method is called, when a component is removed from this validator.
	 * This should remove any listeners added in registerToComponent().
	 * 
	 * @param component
	 */
	protected abstract void unregisterFromComponent(T component);

	/**
	 * Notifies all registered listeners of the specified validation event
	 */
	protected void fireStateChanged(ChangeEvent e) {
		for (ChangeListener l : listener) {
			l.stateChanged(e);
		}
	}

	// DEFAULT LISTENER METHODS ******************************
	// *******************************************************

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
	 * DocumentEvent)
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
		fireStateChanged(new ChangeEvent(e));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.swing.event.DocumentListener#insertUpdate(javax.swing.event.
	 * DocumentEvent)
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
		fireStateChanged(new ChangeEvent(e));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejavax.swing.event.DocumentListener#removeUpdate(javax.swing.event.
	 * DocumentEvent)
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
		fireStateChanged(new ChangeEvent(e));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		fireStateChanged(new ChangeEvent(e));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
	 * .ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		fireStateChanged(new ChangeEvent(e));
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		fireStateChanged(e);
	}
	
	
}
