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
		try {
			XMLToolbox.validateXML("examples/LocalizedStringTable.xml", 
					"schema/LocalizedStringTable.xsd");
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
