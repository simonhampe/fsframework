package fs.test;

import org.dom4j.*;
import org.dom4j.tree.*;

import java.io.*;
import java.util.*;

import fs.xml.*;

/**
 * Tests the XMLConfigurable and ConfigureFileWriter
 * @author Simon Hampe
 *
 */
public class ConfigTest {

	public static void main(String[] args) {
		try {
			//Test write
			Config c1 = new Config("eins",1);
			Config c2 = new Config("zwei",2);
			XMLConfigureFile config = new XMLConfigureFile("stuff/configtest.xml");
			config.registerConfigurable(c1);
			config.registerConfigurable(c2);
			PolyglotStringTable ex
				= new PolyglotStringTable("FSFW_PST_EXAMPLE","Config test table");
			config.registerConfigurable(ex);
			config.configure();
			System.out.println(c1.toString());
			System.out.println(c2.toString());
			//Test read
			ex.removeID("EX01");
			config.readConfigurations();
			config.setAssociatedFile("stuff/configtest2.xml");
			config.store();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
		
}
