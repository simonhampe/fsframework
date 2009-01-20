package fs.polyglot.model;

/**
 * This class implements a tree object representing a variant. It has as
 * properties its path, its string UID, a Language object and the actual string.
 * As subtype of PolyglotString, it is always considered complete, since there
 * are no subnodes.
 * Two variants are considered equal, if they have the same path, stringID and languageID. The value is irrelevant, since only the abstract position 
 * in the string tree is considered.
 * @author Simon Hampe
 * 
 */
public class Variant extends PolyglotString {

	private final NodeType type = NodeType.VARIANT;

	public final Language language;
	public final String value;

	/**
	 * Creates a variant with the specified group path, string and language id
	 * and value. If either of string id, language id or value are null, they
	 * are set to the empty string (or the language with empty id and
	 * description, respectively)
	 */
	public Variant(String path, String id, Language language, String value) {
		super(path, id, true);
		this.language = language == null ? new Language("", "", true, 0)
				: language;
		this.value = value == null ? "" : value;
	}

	@Override
	public NodeType getType() {
		return type;
	}

	public String toString() {
		return super.toString() + ": " + language.id;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj.getClass() == this.getClass())) return false;
		return super.equals(obj) && this.language.id.equals(((Variant)obj).language.id);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ language.id.hashCode();
	}

	
	
}
