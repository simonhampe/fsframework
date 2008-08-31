package fs.xml;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.xml.sax.*;

import org.dom4j.*;
import org.dom4j.io.*;

import java.io.*;



/**
 * This class contains a variety of useful static functions that
 * implement certain frequently used XML operations in a standard
 * way. It can not be instantiated.
 * @author Simon Hampe
 *
 */

public class XMLToolbox {
	
	/**
	 * This validates an xml file according to a scheme file with 
	 * the standard java SAX parser and validation implementations
	 * @throws SAXException - if either the schema file can't be parsed or a SAX Error 
	 * occured during parsing of the actual document
	 * @throws IOException - if an I/O-error occured while reading the xml file
	 */
	public static void validateXML(String xmlFile, String schemaFile) 
						throws SAXException, IOException {
		SchemaFactory fac = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema scheme = fac.newSchema(new File(schemaFile));
		StreamSource document = new StreamSource(new File(xmlFile));
		validateXML(document,scheme);
	}
	
	/**
	 * This validates an xml stream source according to a scheme using  
	 * the standard java SAX parser and validation implementations
	 * @throws SAXException - if a SAX Error 
	 * occured during parsing of the actual document
	 * @throws IOException - if an I/O-error occured while reading the xml file
	 */
	public static void validateXML(StreamSource xmlSource, Schema scheme)
						throws SAXException, IOException
	{
		Validator val = scheme.newValidator();
		val.validate(xmlSource);
	}
	
	/**
	 * Tries to save an org.dom4j.Document object in the specified file in the
	 * standard "pretty format".
	 * @throws IOException - if an I/O-error occured during saving
	 */
	public static void saveXML(Document doc, String filename) throws IOException{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(filename),format);
		writer.write(doc);
		writer.close();		
	}
	
}
