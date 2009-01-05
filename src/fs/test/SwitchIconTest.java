package fs.test;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import fs.gui.SwitchIconLabel;

public class SwitchIconTest extends JFrame {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwitchIconTest app = new SwitchIconTest("test");
	}

	public SwitchIconTest(String title) {
		super(title);
		setBounds(100, 100, 500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(2, 4));

		for (int i = 1; i <= 4; i++) {
			final SwitchIconLabel l = new SwitchIconLabel("Label " + i);
			l.setIconReference(new ImageIcon(
					"./graphics/SwingAppender/warn.png"));
			l.setHorizontalAlignment(JLabel.RIGHT);
			l.setHorizontalTextPosition(JLabel.LEADING);
			JButton switchb = new JButton("switch me!");
			add(l);
			add(switchb);
			switchb.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					l.switchIcon();
				}

			});
		}
		pack();
		setVisible(true);

	}

}
