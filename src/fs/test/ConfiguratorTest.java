package fs.test;

import java.io.File;

import javax.swing.JFrame;

import org.dom4j.*;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;

import fs.gui.LogHistoryWindow;
import fs.xml.FsfwConfigurator;
import fs.xml.XMLToolbox;

/**
 * Tests the FsfwConfigurator
 * 
 * @author Simon Hampe
 * 
 */
public class ConfiguratorTest {

	public static void main(String[] args) {
		try {
			FsfwConfigurator config = new FsfwConfigurator("test");

			Document d = XMLToolbox.loadXMLFile(new File(
					"examples/FsfwConfigurator.xml"));
			// For testing purpses validate
			XMLToolbox.validateXML(new File("examples/FsfwConfigurator.xml"),
					new File("schema/FsfwConfigurator.xsd"));
			config.configure(d.getRootElement());
			Document out = new DefaultDocument();
			out.setRootElement(config.getConfiguration());
			System.out.println(XMLToolbox.getDocumentAsPrettyString(out));
			config.applyConfiguration();
			DefaultElement root = new DefaultElement("propertylist");
			DefaultElement dir = new DefaultElement("defaultDirectory");
			dir.setText(".");
			root.add(dir);
			config.configure(root);
			config.applyConfiguration();
			LogHistoryWindow win = new LogHistoryWindow("Test");
			win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			win.setVisible(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
