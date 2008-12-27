package fs.test;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import fs.gui.SwingAppender;
import fs.xml.ConstantResourceReference;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;

/**
 * Tests the SwingAppender
 * @author Simon Hampe
 *
 */
public class SwingAppenderTest extends JFrame{

	public static void main(String[] args) {
		SwingAppenderTest app = new SwingAppenderTest("Test");
		
	}

	public SwingAppenderTest(String title) {
		super(title);
		setBounds(100,100,500,500);
		setLayout(new FlowLayout());
		
		final SwingAppender append = new SwingAppender("testappender");
		final Logger logger = Logger.getLogger("fs.test.SwingAppend");
		logger.addAppender(append.getModel());
		
		add(append);
		JButton change = new JButton("change");
		change.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				append.assignReference(new ResourceReference() {

					@Override
					public String getFullResourcePath(ResourceDependent r,
							String path) {
						return "./stuff/ok.png";
					}
					
				});
				append.reloadResources();
				logger.error("aurgh!");
				append.setErrorIcon(new ImageIcon("graphics/EditCloseTabComponent/close.png"));
				append.reloadIcon();
				
			}
		});
		add(change);
		pack();
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		
		logger.info("test");
		logger.warn("achtung!");
		logger.fatal("owei!");
		logger.debug("nur müll!");
	}
	
}
