package fs.validate;

import java.util.HashMap;

/**
 * Represents the result of a validation run of a ComponentValidator. It
 * contains an enum value indicating the overall result and a table mapping registered
 * components to individual results.
 * @author Simon Hampe
 *
 */
public class ValidationResult {

	/**
	 * compiler-generated version id
	 */
	private static final long serialVersionUID = -8981134469788743294L;
	
	public enum Result {CORRECT, WARNING, INCORRECT};
	
	/**
	 * Returns the 'worse' value of two Results
	 */
	public static Result min(Result a, Result b) { return a == Result.INCORRECT || b == Result.INCORRECT ? 
												   Result.INCORRECT : 
												   a == Result.WARNING || b == Result.WARNING ? 
												   Result.WARNING : Result.CORRECT;}
	
	//The final result
	private Result overallResult;
	//List of all individual results
	private HashMap<Object,Result> results;

	/**
	 * Creates a ValidationResult representing a validation run on a ComponentValidator.
	 * @param overallResult The overall result
	 * @param results A table with individual results of all registered components. May be null. In that case
	 * it will internally be set to the empty table.
	 */
	public ValidationResult(Result overallResult, HashMap<Object,Result> results) {
		this.overallResult = overallResult;
		this.results = results != null ? results : new HashMap<Object,Result>();		
	}

	/**
	 * @return the overall result of the validation run
	 */
	public Result getOverallResult() {
		return overallResult;
	}

	/**
	 * @return A table of individual results for each registered component
	 */
	public HashMap<Object, Result> getResults() {
		return results;
	}

	
	
}
