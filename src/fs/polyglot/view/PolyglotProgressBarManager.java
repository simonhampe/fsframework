package fs.polyglot.view;

import java.util.HashMap;

import javax.swing.JProgressBar;

import fs.xml.PolyglotStringTable;

/**
 * This class manages a mapping of PolyglotStringTables to ProgressBars, so that there is a unique Progress Bar associated to each table. The progress bar
 * is created on demand.
 * @author Simon Hampe
 *
 */
public class PolyglotProgressBarManager {

	//The mapping
	private static HashMap<PolyglotStringTable, JProgressBar> map = new HashMap<PolyglotStringTable, JProgressBar>();
	
	/**
	 * @return The progress bar associated to the table. If there isn't one yet, it is created. If table == null, null is returned
	 */
	public static JProgressBar getProgressBar(PolyglotStringTable table) {
		if(table == null) return null;
		JProgressBar bar = map.get(table);
		if( bar != null) return bar;
		else {
			bar = new JProgressBar();
			map.put(table, bar);
			return bar;
		}
	}
	
}
