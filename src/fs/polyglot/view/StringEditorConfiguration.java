package fs.polyglot.view;

import java.util.HashSet;

/**
 * This class represents a configuration for the string editor, i.e.: 
 * Which strings are selected for editing?
 * @author Simon Hampe
 *
 */
public class StringEditorConfiguration {
	
	/**
	 * Indicates, whether only strings are selected which have missing variants. default = true
	 */
	public boolean editOnlyIncomplete = true;
	
	/**
	 * Indicates, whether only strings are selected, for which the corresponding group or string has been selected in the 
	 * string view. default = false
	 */
	public boolean editOnlySelected = false;

	/**
	 * Indicates the set of language ids, which are displayed for edit. If null, all ids are displayed. This is usually ignored, if
	 * exludeThesLanguages is not null.
	 * default = null
	 */
	public HashSet<String> onlyTheseLanguages = null;
	
	/**
	 * Indicates the set of language ids, which should NOT be displayed for edit. If null, no languages are excluded. default = null; 
	 */
	public HashSet<String> excludeTheseLanguages = null;
	
}
