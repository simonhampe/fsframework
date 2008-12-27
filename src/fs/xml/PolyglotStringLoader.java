package fs.xml;

import java.util.*;

/**
 * A kind of read-only facade for a PolyglotStringTable. 
 * A string loader is associated to a single 
 * PolyglotStringTable, but can be given the reference 
 * to another table for each query. Queries without any table reference will
 * be directed to the associated table. However the string loader
 * can be configured to handle a query in a way a bit
 * more advanced than a standard string table. 
 * This class is essentially immutable (at least as far as String can 
 * be considered immutable). The associated table is fully cloned at 
 * creation time.<br>
 * - A default language id can be specified, so that each time a
 * queried string does not exist in a certain language, it is queried
 * in the default language. <br>
 * - The string loader can be configured to return a preconfigured string instead of null,
 * if a queried string does not exist (that is, neither for the original query, nor for any default
 * language)
 * @author Simon Hampe
 *
 */
public class PolyglotStringLoader {
	
	/**
	 * The default language id to query for, if a string does not
	 * exist in a queried language
	 */
	private String defaultLanguageID;
	/**
	 * The string to be returned instead of null, if no string is returned neither for
	 * the original query nor for the default ID
	 */
	private String failReturnValue;
	/**
	 * The associated PolyglotStringTable
	 */
	private PolyglotStringTable internalTable = null;
	/**
	 * The default string loader used by all fsframework classes
	 * internally. The default language is english and the fail return
	 * value is the empty string (not null for reasons of stability).
	 * It is accessed via the loadDefaultLoader() and getDefaultLoader() methods
	 */
	private static PolyglotStringLoader defaultLoader = null;
	
	
	// CONSTRUCTOR **************************************
	// **************************************************

	/**
	 * Constructs a string loader with the specified default language id (which will be queried, if
	 * no string is returned for a query) and the failure return value (which will be returned instead of
	 * null), associated to the specified table. If table == null, all
	 * operations directed to the associated table will return a 
	 * OperationNotSupportedException  
	 */
	public PolyglotStringLoader(PolyglotStringTable table,String defaultLanguageID, String failReturnValue) {
		this.defaultLanguageID = defaultLanguageID;
		this.failReturnValue = failReturnValue;
		if(table != null) internalTable = table.clone();
	}
	
	// GENERAL QUERY METHODS *****************************
	// ***************************************************
	
	/**
	 * Queries a string from the specified table in exact the same way that 
	 * PolyglotStringTable.getString(..) does. If the string does not exist, another query
	 * is made for the default language id (if it isn't null) and in case of success the result 
	 * is returned. If the string still does not exist, the failReturnValue is returned. Automatically 
	 * returns this value, if table == null
	 * @throws MissingFormatArgumentException - if the arguments provided do not 
	 * match the string's format (a surplus of arguments will be ignored).
	 */
	public String queryString(PolyglotStringTable table,
								String stringID, String languageID, Object... args )
								throws MissingFormatArgumentException{
		if(table == null) return failReturnValue;
		String ret = table.getString(stringID, languageID, args);
		if(ret != null) return ret;
		else  {
			if(defaultLanguageID != null) {
				ret = table.getString(stringID, defaultLanguageID, args);
				if(ret != null) return ret;
				else return failReturnValue;
			}
			else return failReturnValue;
		}
	}
	
	/**
	 * Queries a string from the specified table in exact the same way that 
	 * PolyglotStringTable.getString(..) does. If the string does not exist, another query
	 * is made for the default language id (if it isn't null) and in case of success the result 
	 * is returned. If the string still does not exist, the failReturnValue is returned. Automatically 
	 * returns this value, if table == null.<br>
	 * Any format specifiers are treated and returned as normal characters.
	 */
	public String queryStringUnformatted(PolyglotStringTable table,
								String stringID, String languageID) {
		if(table == null) return failReturnValue;
		String ret = table.getString(stringID, languageID);
		if(ret != null) return ret;
		else  {
			if(defaultLanguageID != null) {
				ret = table.getString(stringID, defaultLanguageID);
				if(ret != null) return ret;
				else return failReturnValue;
			}
			else return failReturnValue;
		}
	}


	// GETTER METHODS ************************
	// ***************************************
	
	/**
	 * @return the language id to query for, when the original 
	 * query fails.
	 */
	public String getDefaultLanguageID() {
		return defaultLanguageID;
	}

	/**
	 * @return The value to return if no query is succesful.
	 */
	public String getFailReturnValue() {
		return failReturnValue;
	}
	
	/**
	 * @return True, if the associated table is non-null, false
	 * otherwise
	 */
	public boolean hasAssociatedTable() {
		return internalTable != null;
	}
	
	/**
	 * Convenience method used to abort any delegate call, if
	 * internalTable == null
	 * @throws UnsupportedOperationException - if internalTable == null
	 */
	private void precheckDelegate() throws UnsupportedOperationException{
		if(internalTable == null) 
			throw new UnsupportedOperationException("Can't delegate call. No table is associated to this loader.");
	}
	
	// DELEGATE METHODS FOR THE TABLE *********************
	// ***************************************************

	/**
	 * @return Delegates the call to the internal table
	 * @throws UnsupportedOperationException - if no table is associated
	 * @see fs.xml.PolyglotStringTable#getIdentifier()
	 */
	public String getIdentifier() {
		precheckDelegate();	
		return internalTable.getIdentifier();
	}

	/**
	 * @return Delegates the call to the internal table
	 * @throws UnsupportedOperationException - if no table is associated
	 * @see fs.xml.PolyglotStringTable#getIDList()
	 */
	public HashSet<String> getIDList() {
		precheckDelegate();
		return internalTable.getIDList();
	}

	/**
	 * @return Delegates the call to the internal table
	 * @throws UnsupportedOperationException - if no table is associated
	 * @see fs.xml.PolyglotStringTable#getLanguageDescription(java.lang.String)
	 */
	public String getLanguageDescription(String languageID) {
		precheckDelegate();
		return internalTable.getLanguageDescription(languageID);
	}

	/**
	 * @return Delegates the call to the internal table
	 * @throws UnsupportedOperationException - if no table is associated
	 * @see fs.xml.PolyglotStringTable#getLanguageList()
	 */
	public HashSet<String> getLanguageList() {
		precheckDelegate(); 
		return internalTable.getLanguageList();
	}

	/**
	 * @return Delegates the call to the internal table
	 * @throws UnsupportedOperationException - if no table is associated
	 * @see fs.xml.PolyglotStringTable#getMissingStrings()
	 */
	public HashMap<String, HashSet<String>> getMissingStrings() {
		precheckDelegate(); 
		return internalTable.getMissingStrings();
	}

	/**
	 * Does the same as queryString, but for the associated table.
	 * @throws UnsupportedOperationException - if no table is associated
	 * @throws MissingFormatArgumentException
	 * @see fs.xml.PolyglotStringTable#getString(java.lang.String, java.lang.String, java.lang.Object[])
	 */
	public String getString(String stringID, String languageID, Object... args) throws MissingFormatArgumentException {
		precheckDelegate(); 
		return queryString(internalTable, stringID, languageID, args);
	}

	/**
	 * @return Delegates the call to the internal table
	 * @throws UnsupportedOperationException - if no table is associated
	 * @see fs.xml.PolyglotStringTable#getTableDescription()
	 */
	public String getTableDescription() {
		precheckDelegate(); 
		return internalTable.getTableDescription();
	}

	/**
	 * @return Delegates the call to the internal table
	 * @throws UnsupportedOperationException - if no table is associated
	 * @see fs.xml.PolyglotStringTable#getTableID()
	 */
	public String getTableID() {
		precheckDelegate(); 
		return internalTable.getTableID();
	}

	/**
	 * Does the same as queryStringUnformatted, but for the associated table
	 * @throws UnsupportedOperationException - if no table is associated
	 * @see fs.xml.PolyglotStringTable#getUnformattedString(java.lang.String, java.lang.String)
	 */
	public String getUnformattedString(String stringID, String languageID) {
		precheckDelegate(); 
		return queryStringUnformatted(internalTable, stringID, languageID);
	}
	
	// THE DEFAULT FSFRAMEWORK STRING LOADER ********
	// **********************************************
	
	/**
	 * This will load a new copy of the fsframework string table and
	 * generate a string loader associated to it. Since this is done by
	 * calling PolyglotStringTable.getFsfwTable(), the table should have
	 * been loaded before or accessible under the default reference. This method
	 * will be a loader without associated table, if the table cannot be loaded. The fsframework default loader
	 * has as default language the fsframework default language and fail return value "" (i.e. the empty string). 
	 */
	public static PolyglotStringLoader loadDefaultLoader() {
		PolyglotStringTable table = PolyglotStringTable.getFsfwTable();
		if(table == null) defaultLoader = new PolyglotStringLoader(null,PolyglotStringTable.defaultLanguageID,"");
		else defaultLoader = new PolyglotStringLoader(table,PolyglotStringTable.defaultLanguageID, "");
		return defaultLoader;
	}

	/**
	 * Returns the last version of the default string loader, that was 
	 * created by loadDefaultLoader(). If this is null, 
	 * loadDefaultLoader() will be called.
	 */
	public static PolyglotStringLoader getDefaultLoader() {
		return defaultLoader == null? loadDefaultLoader() : defaultLoader;
	}
	
		
}
