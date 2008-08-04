package fs.test;

import org.dom4j.*;
import org.dom4j.tree.*;

import java.io.*;

import fs.xml.*;

/**
 * Creates a few XMLConfigurables and tests several 
 * features of the XML package
 * @author Simon Hampe
 *
 */
public class XMLTest {

	public static void main(String[] args) {
		Config c1 = new Config("test",1);
		Config c2 = new Config("test",2);
		try {
			XMLConfigureFile f = new XMLConfigureFile("xmltest.xml");
			f.registerConfigurable(c1);
			f.registerConfigurable(c2);
			try {
				f.readConfigurations();
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
			}
			f.store();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}	
		System.out.println(c1 + "\n" + c2);
	}
}
