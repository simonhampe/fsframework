package fs.test;

import org.dom4j.*;
import org.dom4j.tree.*;

import java.io.*;
import java.util.*;

import fs.xml.*;

/**
 * Tests all the Resource* classes
 * 
 * @author Simon Hampe
 * 
 */
public class ResourceTest {

	public static void main(String[] args) {
		try {
			// Create artificial resource Dependents
			// (using files as resource structure)
			ResourceDependent rd1 = new ResourceDependent() {
				public void assignReference(ResourceReference r) {
				}

				public Document getExpectedResourceStructure() {
					Document d = null;
					try {
						d = XMLToolbox
								.loadXMLFile(new File("stuff/resdep1.xml"));
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					return d;
				}
			};
			ResourceDependent rd2 = new ResourceDependent() {
				public void assignReference(ResourceReference r) {
				}

				public Document getExpectedResourceStructure() {
					Document d = null;
					try {
						d = XMLToolbox
								.loadXMLFile(new File("stuff/resdep2.xml"));
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					return d;
				}
			};
			ResourceDependent rd3 = new ResourceDependent() {
				public void assignReference(ResourceReference r) {
				}

				public Document getExpectedResourceStructure() {
					Document d = null;
					try {
						d = XMLToolbox
								.loadXMLFile(new File("stuff/resdep3.xml"));
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
					return d;
				}
			};

			// Now create the ResourceDirectory
			ResourceDirectory dir = new ResourceDirectory("./");
			for (ResourceDependent r : Arrays.asList(rd1, rd2, rd3)) {
				dir.addResourceDependent(r);
			}
			dir.verify();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
