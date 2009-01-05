package fs.xml;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import org.xml.sax.*;

import org.dom4j.*;
import org.dom4j.io.*;

import java.io.*;

/**
 * This class contains a variety of useful static functions that implement
 * certain frequently used XML operations in a standard way. It can not be
 * instantiated.
 * 
 * @author Simon Hampe
 * 
 */

public class XMLToolbox {

	// VALIDATION **************************************
	// *************************************************

	/**
	 * This validates an xml file according to a scheme file with the standard
	 * java SAX parser and validation implementations
	 * 
	 * @throws SAXException
	 *             - if either the schema file can't be parsed or a SAX Error
	 *             occured during parsing of the actual document
	 * @throws IOException
	 *             - if an I/O-error occured while reading the xml file
	 */
	public static void validateXML(File xmlFile, File schemaFile)
			throws SAXException, IOException {
		SchemaFactory fac = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema scheme = fac.newSchema(schemaFile);
		StreamSource document = new StreamSource(xmlFile);
		validateXML(document, scheme);
	}

	/**
	 * This validates an xml source according to a scheme using the standard
	 * java SAX parser and validation implementations
	 * 
	 * @throws SAXException
	 *             - if a SAX Error occured during parsing of the actual
	 *             document
	 * @throws IOException
	 *             - if an I/O-error occured while reading the xml file
	 */
	public static void validateXML(Source xmlSource, Schema scheme)
			throws SAXException, IOException {
		Validator val = scheme.newValidator();
		val.validate(xmlSource);
	}

	/**
	 * This validates an already existent XML node according to a scheme using
	 * the standard java SAX parser and validation implementations. This is
	 * rather inefficient since the document has to be converted in a character
	 * stream of XML code that will be read by the validator.
	 * 
	 * @throws SAXException
	 *             - if a SAX Error occured during parsing of the actual
	 *             document
	 * @throws IOException
	 *             - if an I/O-error occured while reading the xml source
	 */
	public static void validateXML(Node n, Schema scheme) throws SAXException,
			IOException {
		// Write document to string
		StringWriter out = new StringWriter();
		XMLWriter writer = new XMLWriter(out);
		writer.write(n);
		// Read document from string
		StringReader in = new StringReader(out.toString());
		StreamSource s = new StreamSource(in);
		// Validate
		Validator val = scheme.newValidator();
		val.validate(s);
	}

	/**
	 * Constructs a Schema object from a given XML file
	 * 
	 * @throws IOException
	 *             - if an I/O-Error occured while reading the file
	 * @throws SAXException
	 *             - if the file is not a valid schema file
	 */
	public static Schema getSchemaObject(File schemaFile) throws SAXException,
			IOException {
		SchemaFactory fac = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		Schema scheme = fac.newSchema(schemaFile);
		return scheme;
	}

	// XML OUTPUT / INPUT ********************************
	// ***************************************************

	/**
	 * Tries to save an org.dom4j.Document object in the specified file in the
	 * standard "pretty format".
	 * 
	 * @throws IOException
	 *             - if an I/O-error occured during saving
	 */
	public static void saveXML(Document doc, String filename)
			throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(filename), format);
		writer.write(doc);
		writer.close();
	}

	/**
	 * Tries to open the specified XML file. If succesful, it will be stored in
	 * a Document object and returned
	 * 
	 * @throws DocumentException
	 *             - If the XML file can not be opened or is not a valid XML
	 *             file
	 */
	public static Document loadXMLFile(File templateFile)
			throws DocumentException {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(templateFile);
		return doc;
	}

	/**
	 * Returns a document written in the standard 'pretty format' of dom4j
	 */
	public static String getDocumentAsPrettyString(Document doc)
			throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		StringWriter output = new StringWriter();
		XMLWriter writer = new XMLWriter(output, format);
		writer.write(doc);
		return output.toString();
	}

}
