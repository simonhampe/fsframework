package fs.test;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.synth.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import fs.gui.*;
import fs.xml.*;

/**
 * Tests the tab component classes
 * 
 * @author Simon Hampe
 * 
 */
public class TabComponentTest extends JFrame {

	public static void main(String[] args) {
		try {
			/*
			 * SynthLookAndFeel laf = new SynthLookAndFeel(); laf.load(new
			 * FileInputStream("stuff/synthtest.xml"), TabComponentTest.class);
			 * UIManager.setLookAndFeel(laf);
			 */
			TabComponentTest app = new TabComponentTest("Testframe");
		} catch (Exception e) {
			System.out.println(e.getClass().toString() + ": " + e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				System.out.println(s);
			}
		}
	}

	public TabComponentTest(String title) throws Exception {
		super(title);
		initializeGUI();
		setVisible(true);

	}

	private void initializeGUI() throws Exception {
		// Set frame bounds
		setBounds(100, 100, 500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Add a tabbed pane
		final JTabbedPane tabs = new JTabbedPane();
		final TabComponent tc = new TabComponent(tabs);
		tc.setSelectTabOnClick(false);
		JButton b = new JButton("Aarrggh");
		tc.addClickTransparent(b);
		tc.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				tc.selectAssociatedTab();
			}
		});
		tabs.addTab("1", new JPanel());
		tabs.addTab("2", new JPanel());
		tabs.addTab("3", new JPanel());

		final EditCloseTabComponent ectc = new EditCloseTabComponent(
				"testtabtiteltext", tabs, true, true, null);
		ectc.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				System.out.println("property change: " + e.getPropertyName()
						+ ": " + e.getOldValue() + "->" + e.getNewValue());
				if ("text".equals(e.getPropertyName())) {
					// if("".equals(e.getNewValue())) {
					// ectc.setToEditMode();
					// }
				}
			}
		});

		JButton b2 = new JButton("x");
		((JPanel) tabs.getComponent(0)).add(b2);
		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ectc.setCloseButton(null);
				ectc.setTextLabel(new JLabel("e"));
				ectc.setTextEditor(new JTextField("b"));
			}
		});

		tabs.setTabComponentAt(1, ectc);
		getContentPane().add(tabs);
		pack();

		System.out.println(XMLToolbox.getDocumentAsPrettyString(ectc
				.getExpectedResourceStructure()));
	}

}
