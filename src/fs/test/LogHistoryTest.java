package fs.test;

import java.awt.event.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fs.gui.*;
import fs.xml.PolyglotStringTable;

public class LogHistoryTest {

	public static void main(String[] args) {
		PolyglotStringTable.setGlobalLanguageID("en");
		LogHistoryWindow win = new LogHistoryWindow(null);
		win.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		win.setVisible(true);

		Logger logger = Logger.getLogger("fs.logtest");
		logger.setLevel(Level.ALL);
		logger.addAppender(win.getModel());

		logger.info("info");
		logger.fatal("argh!");
		logger.warn("achtung!");
		logger.trace("www.google.de");
		logger.debug("debug");

		win.getModel().putFilter(Level.ALL, false);
		win.getModel().putFilter(Level.OFF, false);
		win.getModel().putFilter(new MoreLevels("BLA", 17), false);

	}

	private static class MoreLevels extends Level {
		public MoreLevels(String name, int value) {
			super(value, name, value);
		}
	}

}
