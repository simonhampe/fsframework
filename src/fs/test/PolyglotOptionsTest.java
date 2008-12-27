package fs.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.dom4j.Element;
import org.dom4j.tree.DefaultDocument;

import fs.polyglot.model.PolyglotOptions;
import fs.xml.XMLToolbox;

/**
 * Tests the class PolyglotOptions
 * @author Simon Hampe
 *
 */
public class PolyglotOptionsTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PolyglotOptions options = new PolyglotOptions();
		options.setMaxfilenumber(2);
		options.setLastfiles(new ArrayList<File>(Arrays.<File>asList(new File("a.xml"),new File("b.xml"), new File("c.xml"))));
		Element e;
		try {
			e = options.getConfiguration();
			DefaultDocument d = new DefaultDocument();
			d.setRootElement(e);
			System.out.println(XMLToolbox.getDocumentAsPrettyString(d));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

}
