package fs.test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import fs.gui.EditCloseTabComponent;
import fs.gui.TabComponent;
import fs.xml.XMLToolbox;

/**
 * Tests the tab component classes
 * 
 * @author Simon Hampe
 * 
 */
@SuppressWarnings("serial")
public class TabComponentTest extends JFrame {

	public static void main(String[] args) {
		try {
			/*
			 * SynthLookAndFeel laf = new SynthLookAndFeel(); laf.load(new
			 * FileInputStream("stuff/synthtest.xml"), TabComponentTest.class);
			 * UIManager.setLookAndFeel(laf);
			 */
			@SuppressWarnings("unused")
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
