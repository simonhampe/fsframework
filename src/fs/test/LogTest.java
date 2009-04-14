package fs.test;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import fs.log.DefaultLogHistoryWindowModel;

/**
 * Tests features of the log4j api
 * 
 * @author Simon Hampe
 * 
 */
public class LogTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		// CHAINSAW connection
		/*
		 * SocketAppender sa = new SocketAppender("localhost",4445);
		 * sa.setLocationInfo(true); sa.setLayout(new XMLLayout());
		 * Logger.getRootLogger().setLevel(Level.DEBUG);
		 * Logger.getRootLogger().addAppender(sa);
		 */
		Logger l = Logger.getLogger("fs.logtest");
		DefaultLogHistoryWindowModel a = new DefaultLogHistoryWindowModel();
		l.addAppender(a);
		l.info("bla");
		l.warn("ik");
		l.fatal("u");
		System.out.println(a.getLogHistory());
	}

}
