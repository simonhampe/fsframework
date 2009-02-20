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
			XMLDirectoryTree tree2 = new XMLDirectoryTree();
			tree.addPath("eins/zwei/drei");
			tree.addPath("eins/zwei/drei1");
			tree.addPath("eins/zwo/drei");
			tree.removePath("eins/zwei/drei");
			tree2.addPath("a/b/c");
			tree2.addTree(tree);
			System.out.println(XMLToolbox.getDocumentAsPrettyString(tree));
			System.out.println(XMLToolbox.getDocumentAsPrettyString(tree2));
			System.out.println(tree.getListOfPaths());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
