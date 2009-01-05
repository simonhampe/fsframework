package fs.test;

import fs.xml.*;

import org.dom4j.*;
import org.dom4j.io.*;

import java.io.*;
import java.util.*;

/**
 * Tests the LocalizedStringTable class
 * 
 * @author Simon Hampe
 * 
 */
public class StringTableTest {

	public static void main(String[] args) {
		try {
			// Load the example table
			Document in = XMLToolbox.loadXMLFile(new File(
					"examples/PolyglotStringTable.xml"));
			PolyglotStringTable ex = new PolyglotStringTable(in,
					new ConstantResourceReference("."));
			// Load some strings
			showfullReport(ex);
			System.out.println("Formatted String: "
					+ ex.getString("EX01", "de", 42.0, "tadaa!"));
			// Show output version
			Element e = ex.getConfiguration();
			XMLWriter writer = new XMLWriter(System.out, OutputFormat
					.createPrettyPrint());
			writer.write(e);
			System.out.println();
			// Add another document for update
			Document up = XMLToolbox.loadXMLFile(new File(
					"examples/PolyglotStringTableUpdate.xml"));
			ex.configure(up.getRootElement());
			showfullReport(ex);
			// Check out resource structure node
			writer.write(ex.getExpectedResourceStructure());
			// Change some data
			ex.setTableID("FS_MINGLED");
			ex.setTableDescription(null);
			ex.setLanguageDescription("wu", "Altwuselsch");
			ex.setLanguageDescription("bx", "BXisch");
			ex.putLanguage("bw", "BWisch");
			ex.putLanguage("bz", "BZisch");
			ex.putString("EX01", "de", "Dies ist immer noch ein Beispiel");
			ex.putString("EX04", "de", "noch eins");
			ex.removeID("EX02");
			ex.addStringID("EX05");
			ex.removeLanguage("bz");
			ex.renameString("EX01", "NEWNAME");
			showfullReport(ex);
			HashMap<String, HashSet<String>> ms = ex.getMissingStrings();
			System.out.println("Missing strings");
			for (String sid : ms.keySet()) {
				System.out.println(sid + ": ");
				for (String lid : ms.get(sid)) {
					System.out.println("- " + ex.getLanguageDescription(lid));
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void showfullReport(PolyglotStringTable ex) {
		System.out.println("Table ID: " + ex.getTableID());
		System.out.println("Table Description: " + ex.getTableDescription());
		System.out.println("Supported Languages:");
		for (String lid : ex.getUsedLanguages()) {
			System.out.println(" - " + ex.getLanguageDescription(lid) + " ("
					+ lid + ")" + ", isOnlyUsed: "
					+ !ex.getLanguageList().contains(lid) + ", supported: "
					+ ex.getSupport(lid));
		}
		System.out.println("Group Ids:");
		for (String g : new TreeSet<String>(ex.getGroupList())) {
			System.out.println(" - " + g);
		}
		System.out.println("String Table:");
		for (String sid : ex.getIDList()) {
			System.out.println("- " + sid + " (group: " + ex.getGroupID(sid)
					+ ")");
			for (String lid : ex.getLanguageList()) {
				String s = ex.getUnformattedString(sid, lid);
				if (s == null)
					s = "[missing]";
				System.out.println("- - " + ex.getLanguageDescription(lid)
						+ ": " + s);
			}
		}
	}
}
