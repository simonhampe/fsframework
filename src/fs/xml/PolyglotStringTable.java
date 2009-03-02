package fs.xml;

import org.dom4j.*;
import org.dom4j.tree.*;

import java.util.*;
import java.io.*;

/**
 * Implements a table of strings, each identified by unique String ID and
 * potentially available in multiple languages. Each PolyglotStringTable is
 * supposed to identify itself by a possibly unique ID and an optional
 * description (i.e. of its purpose and content). It contains as well a table of
 * 'Supported Languages', each with Identifier and description. As this table
 * and the string table is maintained by the user, a priori it can only serve
 * informational purposes. No actual guarantee can be given, that a certain
 * string exists in a certain language. This class also contains a table mapping
 * certain string id's to group id's. These group id's are for example used by
 * the application POLYGLOT to arrange the strings in a clearer way. The group
 * id's impose a hierarchical grouping similar to package hierarchy: A '.' in
 * the group id indicates a subgrouping. Formatted strings with placeholder
 * arguments are supported. <br>
 * The input/output format is XML. The format is strict and described under
 * "schema/PolyglotStringTable.xsd". Internally the tables will be saved in
 * HashMaps, for performance reasons. Input/output operations are performed via
 * the XMLConfigurable interface. The method configure() however, does not
 * implement a simple loading mechanism, but <i>adds</i> content to the existing
 * table.<br>
 * For the class to be fully functional the schema and template files - as
 * specified by the ResourceDependent interface - have to be present.
 * 
 * @author Simon Hampe
 * 
 */

public class PolyglotStringTable implements ResourceDependent, XMLConfigurable {

	// INTERNAL FIELDS **********************************
	// **************************************************

	// Table description and id
	private String description = "";
	private String id = "";

	// HashMap mapping language id to language description
	private HashMap<String, String> languageTable = new HashMap<String, String>();

	// HashMap mapping string id to a mapping of language id to output string
	private HashMap<String, HashMap<String, String>> stringTable = new HashMap<String, HashMap<String, String>>();

	// HashMap mapping string id to a group id
	private HashMap<String, String> grouptable = new HashMap<String, String>();

	// The internal resource reference for locating templates and schemas
	private ResourceReference resourceRef;

	// The default table for all internal fsframework strings
	private static PolyglotStringTable fsfwTable = null;

	// The global language id for fsframework
	private static String globalLanguageID = "en";

	/**
	 * The default language for fsframework is English
	 */
	public final static String defaultLanguageID = "en";

	// CONSTRUCTORS **************************************
	// ***************************************************

	/**
	 * Constructs a String table with empty language list and no string IDs. The
	 * table's id and description are set to the specified values (null will be
	 * interpreted as the empty string). Schema and template files are supposed
	 * to be in the standard subfolder of the current application folder
	 */
	public PolyglotStringTable(String id, String description) {
		this.id = id == null ? "" : id;
		this.description = description == null ? "" : description;
		// Initialize standard reference
		assignReference(null);
	}

	/**
	 * Reads a PolyglotStringTable from an XML document, using the specified
	 * resource reference for validation and saving it internally as resource
	 * reference. If r == null, the default reference is used
	 * 
	 * @throws XMLWriteConfigurationException
	 *             - if any error occurs during validation
	 */
	public PolyglotStringTable(Document doc, ResourceReference r)
			throws XMLWriteConfigurationException {
		assignReference(r);
		// First do a standard configuration. This will
		// automatically validate the document
		configure(doc.getRootElement());
		// If this was succesful, the document is valid
		// and we can safely extract id and description
		id = doc.getRootElement().valueOf("./@id");
		description = doc.getRootElement().selectSingleNode("./description")
				.getText();
	}

	// GETTER METHODS *************************************
	// ****************************************************

	/**
	 * Will return the string associated with the specified ID and language,
	 * with placeholders replaced by appropriate arguments. If the string ID
	 * does not exist or the string does not exist for the given String and
	 * Language ID, null is returned.
	 * 
	 * @throws java.util.MissingFormatArgumentException
	 *             - if the arguments provided do not match the string's format
	 *             (a surplus of arguments will be ignored).
	 */
	public String getString(String stringID, String languageID, Object... args)
			throws MissingFormatArgumentException {
		try {
			String ret = stringTable.get(stringID).get(languageID);
			if (ret == null)
				return null;
			else
				return String.format(ret, args);
		} catch (NullPointerException ne) {
			return null;
		}
	}

	/**
	 * Will return the string associated with the specified ID and language,
	 * with placeholders left unchanged. If the string ID does not exist or the
	 * string does not exist for the given String and Language ID, null is
	 * returned.
	 */
	public String getUnformattedString(String stringID, String languageID) {
		try {
			return stringTable.get(stringID).get(languageID);
		} catch (NullPointerException ne) {
			return null;
		}
	}

	/**
	 * Returns the list of languages this table claims to support
	 */
	public HashSet<String> getLanguageList() {
		return new HashSet<String>(languageTable.keySet());
	}

	/**
	 * Returns a list of language IDs actually used by strings
	 */
	public HashSet<String> getUsedLanguages() {
		HashSet<String> used = new HashSet<String>();
		for (HashMap<String, String> llist : stringTable.values()) {
			used.addAll(llist.keySet());
		}
		return used;
	}

	/**
	 * Returns the set of languages in which a certain polyglotstring is
	 * actually available
	 */
	public HashSet<String> getSupportedLanguages(String stringID) {
		// If the ID doesn't exist, return the empty list
		if (!stringTable.keySet().contains(stringID))
			return new HashSet<String>();
		return new HashSet<String>(stringTable.get(stringID).keySet());
	}

	/**
	 * Returns an integer between 0 and 100 indicating the percentage of strings
	 * for which there exists a variant for this language
	 */
	public int getSupport(String languageID) {
		float numberofstrings = stringTable.keySet().size();
		float numberofsupported = 0;
		for (String sid : stringTable.keySet()) {
			if (getSupportedLanguages(sid).contains(languageID))
				numberofsupported++;
		}
		return (int) ((numberofsupported * 100) / numberofstrings);
	}

	/**
	 * Returns whether languageID exists in the language list (if any
	 * polyglotstring uses this language id, but it has not been added to the
	 * language list, this will still return false)
	 */
	public boolean containsLanguage(String languageID) {
		return languageTable.keySet().contains(languageID);
	}

	/**
	 * Returns the description associated to the specified language ID. If the
	 * ID is not in the language list of this table, null is returned.
	 */
	public String getLanguageDescription(String languageID) {
		return languageTable.get(languageID);
	}

	/**
	 * Returns a list of all String ID's maintained by this table
	 */
	public HashSet<String> getIDList() {
		return new HashSet<String>(stringTable.keySet());
	}

	/**
	 * Returns whether stringID exists in this table
	 */
	public boolean containsStringID(String stringID) {
		return stringTable.keySet().contains(stringID);
	}

	/**
	 * Returns true, if and only if there exists a variant for this string id
	 * for each <i>listed</i> language id.
	 */
	public boolean isCompleteString(String stringID) {
		for (String lid : getLanguageList()) {
			if (getUnformattedString(stringID, lid) == null)
				return false;
		}
		return true;
	}

	/**
	 * Returns the current ID of this table (that is used to identify it as
	 * XMLConfigurable)
	 * 
	 * @see fs.xml.XMLConfigurable
	 */
	public String getTableID() {
		return id;
	}

	/**
	 * Returns the description of this table
	 */
	public String getTableDescription() {
		return description;
	}

	/**
	 * Returns a HashMap containing all tuples (StringID, List of LanguageIDs)
	 * for which the StringID exists in this table and the LanguageIDs are
	 * contained in the language list, but for which no strings exists (i.e.
	 * getString(StringID, LanguageID) == null for each element in the list of
	 * LanguageIDs).
	 */
	public HashMap<String, HashSet<String>> getMissingStrings() {
		HashMap<String, HashSet<String>> ret = new HashMap<String, HashSet<String>>();
		for (String i : getIDList()) {
			for (String l : getLanguageList()) {
				if (getUnformattedString(i, l) == null) {
					// If the stringID is not yet present in the list, add it
					if (!ret.keySet().contains(i)) {
						HashSet<String> llist = new HashSet<String>();
						llist.add(l);
						ret.put(i, llist);
					}
					// If not, just add l to the associated list
					else {
						ret.get(i).add(l);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Returns the group id of the polyglot string with id stringID. Returns
	 * null, if the string has no associated group
	 */
	public String getGroupID(String stringID) {
		if (grouptable.containsKey(stringID))
			return grouptable.get(stringID);
		else
			return null;
	}

	/**
	 * @return A list of all group id's in use
	 */
	public HashSet<String> getGroupList() {
		return new HashSet<String>(grouptable.values());
	}

	/**
	 * Returns all strings in this group (if group == null, this returns all
	 * strings without a group)
	 */
	public HashSet<String> getStringsInGroup(String groupID) {
		HashSet<String> groups = new HashSet<String>();
		// If group id is null, return all strings which have no group
		if (groupID == null) {
			for (String sid : stringTable.keySet()) {
				if (getGroupID(sid) == null)
					groups.add(sid);
			}
			return groups;
		}
		// If this group id doesn't exist, return the empty list
		if (!grouptable.values().contains(groupID))
			return groups;
		for (String sid : grouptable.keySet()) {
			if (getGroupID(sid).equals(groupID))
				groups.add(sid);
		}
		return groups;
	}

	/**
	 * Returns all strings in this group and its subgroups (if group == null,
	 * this returns all strings )
	 */
	public HashSet<String> getStringsInSubgroups(String groupID) {
		HashSet<String> groups = new HashSet<String>();
		// If group id is null, return all strings which have no group
		if (groupID == null) {
			return new HashSet<String>(stringTable.keySet());
		}
		for (String sid : grouptable.keySet()) {
			if (getGroupID(sid).startsWith(groupID))
				groups.add(sid);
		}
		return groups;
	}

	/**
	 * Returns true, if and only if for each string in this group and its
	 * subgroups, there exists a variant for each <i>listed</i> language. (Thus
	 * this also returns true for group id's not in this tree)
	 */
	public boolean isCompleteGroup(String groupID) {
		HashSet<String> ingroup = getStringsInSubgroups(groupID);
		for (String sid : ingroup) {
			for (String lid : getLanguageList()) {
				if (getUnformattedString(sid, lid) == null)
					return false;
			}
		}
		return true;
	}

	// SETTER METHODS *****************************
	// ********************************************

	/**
	 * Sets the description of the specified language. If the languageID is not
	 * contained in the language list of this table, this call is ignored.
	 */
	public void setLanguageDescription(String languageID, String description) {
		if (languageTable.keySet().contains(languageID)) {
			languageTable.put(languageID, description);
		}
	}

	/**
	 * Adds the languageID to the list of languages and sets its description to
	 * the specified value. If the ID is already contained in the list, its
	 * description will simply be set to the new value.
	 */
	public void putLanguage(String languageID, String description) {
		languageTable.put(languageID, description);
	}

	/**
	 * Removes the specified languageID from the list of languages
	 */
	public void removeLanguage(String languageID) {
		languageTable.remove(languageID);
	}

	/**
	 * Adds the specified StringID to the list without inserting any variants.
	 * If the StringID already exists, this call is ignored
	 */
	public void addStringID(String stringID) {
		if (!stringTable.containsKey(stringID)) {
			stringTable.put(stringID, new HashMap<String, String>());
		}
	}

	/**
	 * Removes an ID and all associated variants from this table. If the ID does
	 * not exist, this call is ignored. Possible references in the group table
	 * are removed as well.
	 */
	public void removeID(String stringID) {
		stringTable.remove(stringID);
		grouptable.remove(stringID);
	}

	/**
	 * Sets the string value for the specified stringID and languageID to the
	 * specified value. If the stringID does not exist, it is inserted. If the
	 * languageID does not exist, however, it will not automatically be inserted
	 * into the language list, though an entry in this language will exist under
	 * the stringID passed as first argument. If a variant already exists for
	 * these IDs it will be overwritten. Group references will be preserved. If
	 * value == null, no variant will be created or an existing variant will be
	 * removed. If stringID or languageID == null, this call is ignored
	 */
	public void putString(String stringID, String languageID, String value) {
		if (stringID == null || languageID == null)
			return;
		if (!stringTable.containsKey(stringID)) {
			stringTable.put(stringID, new HashMap<String, String>());
		}
		if (value == null) {
			if (stringTable.containsKey(stringID))
				stringTable.get(stringID).remove(languageID);
		} else
			stringTable.get(stringID).put(languageID, value);
	}

	/**
	 * Sets the string value for the specified stringID and languageID to the
	 * specified value. If the stringID does not exist, it is inserted. If the
	 * languageID does not exist, however, it will not automatically be inserted
	 * into the language list, though an entry in this language will exist under
	 * the stringID passed as first argument. If a variant already exists for
	 * these IDs it will be overwritten. Whatever the case, the group attribute
	 * of this string will be set to groupID afterwards.
	 */
	public void putString(String stringID, String languageID, String groupID,
			String value) {
		putString(stringID, languageID, value);
		setGroupID(stringID, groupID);
	}
	
	/**
	 * This renames a string id, i.e.: All variants that were originally obtained under the old id, are now obtained under the new one and
	 * the old id is removed from its group and the new id added. If the old id doesn't exist or one of the ids is null, this call is ignored.
	 * If the new id already exists, it is overwritten.
	 */
	public void renameString(String oldID, String newID) {
		if(oldID == null || newID == null) return;
		//Move variants
		
		HashMap<String, String> variants = stringTable.get(oldID);
		//If it doesn't exist, stop
		if(variants ==  null) return;
		stringTable.remove(oldID);
		stringTable.put(newID, variants);
		
		//Change group association
		String group = grouptable.get(oldID);
		if(group != null) {
			grouptable.remove(oldID);
			grouptable.put(newID, group);
		}
		
	}

	/**
	 * Sets the String that identified the table in the context of
	 * XMLConfigurables. If the new ID equals null, this call is ignored
	 * 
	 * @see fs.xml.XMLConfigurable
	 */
	public void setTableID(String tableID) {
		if (tableID != null) {
			id = tableID;
		}
	}

	/**
	 * Sets the description of this table to the specified value (null values
	 * are interpreted as empty strings)
	 */
	public void setTableDescription(String desc) {
		description = desc == null ? "" : desc;
	}

	/**
	 * If stringID exists in this table, its group is set to the specified group
	 * id. Otherwise, this call is ignored. 
	 */
	public void setGroupID(String stringID, String groupID) {
		if (containsStringID(stringID)) {
			if(groupID != null) grouptable.put(stringID, groupID);
			else grouptable.remove(stringID);
		}
	}

	// INTERFACE METHODS *************************
	// *******************************************

	// RESOURCEDEPENDENT INTERFACE ********************

	/**
	 * Assigns the ResourceReference that is used to locate templates and schema
	 * files. If r == null, the default reference is used
	 */
	public void assignReference(ResourceReference r) {
		resourceRef = (r == null) ? FsfwDefaultReference.getDefaultReference()
				: r;

	}

	/**
	 * This returns the same for all PolyglotStringTables: A subdirectory
	 * "schema" containing PolyglotStringTable.xsd, a subdirectory "templates"
	 * containing "tmpl_PolyglotStringTable.xml" and a subdirectory "language",
	 * containing "fsfwStringTable.xml"
	 */
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree("basedir");
		tree.addPath("schema/PolyglotStringTable.xsd");
		tree.addPath("templates/tmpl_PolyglotStringTable.xml");
		tree.addPath("language/fsfwStringTable.xml");
		return tree;
	}

	// XMLCONFIGURABLE INTERFACE ********************

	/**
	 * Will try to read the node n as if being the root node of an XML document.
	 * satisfying the PolyglotStringTable schema definition. The table's content
	 * will be changed in the following way: <br>
	 * - Table id and description will not be changed <br>
	 * - All language ID's in n that are not yet in the list of languages of
	 * this table, will be added with their description<br>
	 * - For all language ID's in n that are already present in the list of
	 * languages the description will be overwritten by the description in n<br>
	 * - All String ID's that are not yet in the list of stringIDs of this
	 * table, will be added with all variants<br>
	 * - For all String ID's already present in this table, already existing
	 * variants are overwritten by those in n and additional variants are added.<br>
	 * - All polyglotstrings in n with an existing group attribute will be
	 * associated to this group, no matter if they already existed before. <br>
	 * <br>
	 * Thus this method can be used to 'update' a table, i.e. replace erroneous
	 * strings or add support for another language.
	 */
	@SuppressWarnings("unchecked")
	// since otherwise java won't stop complaining about the casts List foo =
	// ... selectNodes()
	public void configure(Node n) throws XMLWriteConfigurationException {
		// Check for validity of node
		try {
			XMLToolbox.validateXML(n, XMLToolbox.getSchemaObject(new File(
					resourceRef.getFullResourcePath(this,
							"schema/PolyglotStringTable.xsd"))));
		} catch (Exception e) {
			throw new XMLWriteConfigurationException("Can't validate node: "
					+ e.getMessage());
		}
		// Check out language list.
		try { // try block to catch cast errors on .selectNode
			List<Node> l = n.selectNodes("./languagetable/language");
			for (Object o : l) {
				Node lang = (Node) o;
				String lid = lang.selectSingleNode("./id").getText();
				String ldesc = lang.selectSingleNode("./description").getText();
				languageTable.put(lid, ldesc);
			}
			// Check out string table
			List<Node> m = n.selectNodes("./polyglotstring");
			for (Object o : m) {
				Node pstring = (Node) o;
				String pid = pstring.valueOf("./@id");
				// If this ID does not yet exist, add it
				if (!stringTable.containsKey(pid)) {
					stringTable.put(pid, new HashMap<String, String>());
				}
				List<Node> variants = pstring.selectNodes("./variant");
				// Read out each variant
				for (Object v : variants) {
					Node avariant = (Node) v;
					String lid = avariant.valueOf("./@lang");
					String vstring = avariant.getText();
					stringTable.get(pid).put(lid, vstring);
				}
			}
			// Check out groups
			List<Node> glist = n.selectNodes("./polyglotstring[@group]");
			for (Node pstring : glist) {
				String id = pstring.valueOf("./@id");
				String group = pstring.valueOf("./@group");
				setGroupID(id, group);
			}
		} catch (ClassCastException ce) {
			throw new XMLWriteConfigurationException(
					"Can't configure table. Wrong node type: "
							+ ce.getMessage());
		}

	}

	/**
	 * Returns the root element of a tree that forms an XML document conforming
	 * to the PolyglotStringTable schema definition that contains all data of
	 * this string table
	 */
	public Element getConfiguration() throws XMLReadConfigurationException {
		//Root node and basic table attributes
		Element root = new DefaultElement("fsfw:polyglotstringtable");
		Element desc = new DefaultElement("description");
			desc.setText(description);
		root.add(desc);
		root.addAttribute("id", id);
		//Add language table
		Element langtab = new DefaultElement("languagetable");
		for (String lang : languageTable.keySet()) {
			DefaultElement l = new DefaultElement("language");
			DefaultElement lid = new DefaultElement("id");
			DefaultElement ldesc = new DefaultElement("description");
			lid.setText(lang);
			ldesc.setText(languageTable.get(lang));
			l.add(lid);
			l.add(ldesc);
			langtab.add(l);
		}
		root.add(langtab);
		// Add all strings
		for (String sid : stringTable.keySet()) {
			DefaultElement p = new DefaultElement("polyglotstring");
			p.addAttribute("id", sid);
			if (grouptable.containsKey(sid))
				p.addAttribute("group", getGroupID(sid));
			// Add all variants
			for (String lid : stringTable.get(sid).keySet()) {
				DefaultElement v = new DefaultElement("variant");
				v.addAttribute("lang", lid);
				v.setText(stringTable.get(sid).get(lid));
				p.add(v);
			}
			root.add(p);
		}

		return root;
	}

	/**
	 * Returns the tables id
	 */
	public String getIdentifier() {
		return id;
	}

	/**
	 * Returns always true
	 */
	public boolean isConfigured() {
		return true;
	}

	// ELEMENTARY METHODS *************************
	// ********************************************

	/**
	 * Returns a deep copy of the string table. Since the resource reference
	 * cannot be deep-cloned, it is simply copied.
	 */
	@Override
	public PolyglotStringTable clone() {
		PolyglotStringTable clone = new PolyglotStringTable(id, description);
		// Copy language list
		for (String lid : languageTable.keySet()) {
			clone.putLanguage(lid, languageTable.get(lid));
		}
		// Copy string list
		for (String sid : stringTable.keySet()) {
			for (String lang : stringTable.get(sid).keySet()) {
				clone.putString(sid, lang, stringTable.get(sid).get(lang));
			}
		}
		// Copy resource reference
		clone.assignReference(resourceRef);
		return clone;
	}

	// STATIC METHODS *********************************
	// ************************************************

	/**
	 * This extracts the portion of the string after the last occurrence of a
	 * point (including this point) If there is no point, the complete group
	 * name is displayed
	 */
	public static String cutGroupPath(String id) {
		if (id == null)
			return null;
		String[] separate = id.split("[.]");
		if (separate.length == 0)
			return "";
		// If there isn't only one string, add a point as prefix
		return (separate.length > 1 ? "." : "") + separate[separate.length - 1];
	}

	/**
	 * @return true, if and only if: <br>
	 *         - group == null or <br>
	 *         - group, subgroup != null and strict == false and
	 *         subgroup.startswith(group) or <br>
	 *         - group, subgroup != null and strict == false and
	 *         subgroup.startswith(group) and !group.equals(subgroup)
	 */
	public static boolean isSubgroupOf(String group, String subgroup,
			boolean strict) {
		if (group == null)
			return true;
		if (subgroup == null)
			return false;
		if (!subgroup.startsWith(group))
			return false;
		if (strict)
			return !group.equals(subgroup);
		else
			return true;
	}

	/**
	 * Extracts a group name from groupID in the following way:<br>
	 * - If groupID == null, this returns null <br>
	 * - If prefix == null this returns the highest order group name in group ID <br>
	 * - If groupID doesn't start with prefix, this returns null <br>
	 * - If groupID.equals(prefix), this returns null <br>
	 * - Otherwise this returns prefix + "." + the highest order group name in
	 * group ID <i>after</i> prefix
	 */
	public static String extractGroup(String prefix, String groupID) {
		if (groupID == null)
			return null;
		if (prefix == null) {
			return extractHighestGroup(groupID);
		}
		if (!groupID.startsWith(prefix) || groupID.equals(prefix))
			return null;
		return prefix + "."
				+ extractHighestGroup(groupID.substring(prefix.length() + 1));
	}

	/**
	 * @return - null, if groupID == null <br>
	 *         - The prefix of groupID up to and not including the first
	 *         occurence of "."
	 */
	public static String extractHighestGroup(String groupID) {
		if (groupID == null)
			return null;
		String[] separate = groupID.split("[.]", 2);
		String subgroup = separate[0];
		return subgroup;
	}

	/**
	 * Returns a clone of the table created by
	 * (fsframework)/language/fsfwStringTable.xml, which contains strings used
	 * by several framework classes. It wil be loaded using the resource
	 * reference specified. If r is null, the default reference will be used.
	 * This will load a new copy from the XML file each time the mehtod is
	 * called. If you want to load a cached version, call getFsfwTable(), which
	 * returns the last version loaded.
	 * 
	 * @throws XMLWriteConfigurationException
	 *             - if any error occured during document loading
	 */
	public static PolyglotStringTable loadFsfwTable(ResourceReference r)
			throws XMLWriteConfigurationException {
		r = (r == null) ? FsfwDefaultReference.getDefaultReference() : r;
		try {
			fsfwTable = new PolyglotStringTable(XMLToolbox
					.loadXMLFile(new File(r.getFullResourcePath(null,
							"language/fsfwStringTable.xml"))), r);
		} catch (DocumentException de) {
			throw new XMLWriteConfigurationException(
					"Can't initialize fsframework string table. XML file seems to be invalid: "
							+ de.getMessage());
		}
		return fsfwTable;
	}

	/**
	 * Will return the last version of the fsframework table loaded (i.e. the
	 * last version generated by loadFsfwTable(ResourceReference r)). If there
	 * is none (i.e. the internal copy is null), the method will try to load it
	 * using the default resource reference. If this is not succesful, null is
	 * returned.
	 */
	public static PolyglotStringTable getFsfwTable() {
		if (fsfwTable != null)
			return fsfwTable.clone();
		else {
			try {
				return loadFsfwTable(FsfwDefaultReference.getDefaultReference());
			} catch (XMLWriteConfigurationException xe) {
				return null;
			}
		}
	}

	/**
	 * @return The global language ID for fsframework. It can be set via
	 *         setGlobalLanguageID. This has nothing to do with the defaut
	 *         language ID of the fs.xml.PolyglotStringLoader, which is "en" and
	 *         cannot be changed. However, some framework classes require a
	 *         language to be specified upon instance creation. Usually, if null
	 *         is specified here, the global language id is used
	 */
	public static String getGlobalLanguageID() {
		return globalLanguageID;
	}

	/**
	 * This sets the global default language ID for fsframework. All class using
	 * this ID, which are instantiated afterwards, will use it. However,
	 * instances that were created before will not be notified of this change
	 * and might thus show incoherent behavior (certain buttons displayed in
	 * dutch while the tooltips are still in greek, etc...) As a rule of thumb,
	 * this method should be called, before any language-dependent classes are
	 * instantiated.
	 */
	public static void setGlobalLanguageID(String globalLanguageID) {
		if (globalLanguageID != null) {
			PolyglotStringTable.globalLanguageID = globalLanguageID;

		}
	}

}
