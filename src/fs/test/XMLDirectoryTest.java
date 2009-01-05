package fs.test;

import fs.xml.*;

/**
 * Tests features of the XMLDirectoryTree class
 * 
 * @author Simon Hampe
 * 
 */
public class XMLDirectoryTest {

	public static void main(String[] args) {
		try {
			XMLDirectoryTree tree = new XMLDirectoryTree();
			tree.addPath("eins/zwei/drei");
			tree.addPath("eins/zwei/drei1");
			tree.removePath("eins/zwei/drei");
			System.out.println(XMLToolbox.getDocumentAsPrettyString(tree));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
