package fs.polyglot.model;

import java.util.Comparator;

/**
 * This is an immutable class representing a language in a PolyglotStringTable.
 * Each language has the following properties: id, description (may be null),
 * isOnlyUsed (a boolean flag indicating if this language is actually used in
 * the string table but not listed in the language list) and supported (an
 * integer between 0 and 100 indicating the percentage of strings for which
 * there is a variant for this string).
 * 
 * @author Simon Hampe
 * 
 */
public class Language {
	public final boolean isOnlyUsed;
	public final int supported;
	public final String description;
	public final String id;

	/**
	 * Generates a Language object with properties set to the specified
	 * parameters. Null strings are converted to empty strings. support is set
	 * to 0 if negative and to 100 if greater than 100
	 */
	public Language(String id, String description, boolean isOnlyUsed,
			int supported) {
		super();
		this.isOnlyUsed = isOnlyUsed;
		this.description = description == null ? "" : description;
		this.id = id == null ? "" : id;
		this.supported = supported < 0 ? 0 : supported > 100 ? 100 : supported;
	}

	/**
	 * Returns true, if and only if supported = 100
	 */
	public boolean isFullySupported() {
		return supported == 100;
	}

	/**
	 * Returns true, if all fields are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Language))
			return false;
		return isOnlyUsed == ((Language) obj).isOnlyUsed
				&& description.equals(((Language) obj).description)
				&& id.equals(((Language) obj).id);
	}

	/**
	 * Returns the hashCode generated by all three fields
	 */
	@Override
	public int hashCode() {
		return new Boolean(isOnlyUsed).hashCode() ^ description.hashCode()
				^ id.hashCode();
	}

	/**
	 * Returns id + ": " + description
	 */
	public String toString() {
		return id + ":" + description;
	}

	/**
	 * A comparator for language objects. Languages are ordered by alphabetic
	 * order of their id's.
	 */
	public final static Comparator<Language> languageSorter = new Comparator<Language>() {
		@Override
		public int compare(Language o1, Language o2) {
			return o1.id.compareTo(o2.id);
		}
	};

}
