package fs.test;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.undo.UndoManager;

import org.apache.log4j.BasicConfigurator;

import fs.gui.GUIToolbox;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.undo.TableUndoManager;
import fs.polyglot.view.GroupTreeView;
import fs.polyglot.view.LanguageListView;
import fs.polyglot.view.StringTreeView;
import fs.xml.XMLToolbox;

/**
 * This tests all the model and renderer classes of Polyglot
 * 
 * @author Simon Hampe
 * 
 */
public class PolyglotModelTest {

	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();
			JFrame main = new JFrame("polyglot model test");
			main.setSize(main.getMaximumSize());
			main.setLayout(new FlowLayout());

			String filename = "language/fsfwStringTable.xml";
			// String filename = "examples/PolyglotStringTable.xml";
			final PolyglotTableModel table = new PolyglotTableModel(XMLToolbox
					.loadXMLFile(new File(filename)), null);

			final UndoManager manager = TableUndoManager.getUndoManager(table);

			LanguageListView view = new LanguageListView(null, null, null,
					table);
			GroupTreeView tview = new GroupTreeView(null, null, null, table);

			StringTreeView sview = new StringTreeView(null,null,null,table);

			JButton undo = new JButton("undo");
			undo.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println(manager.getUndoPresentationName());
					if(manager.canUndo()) manager.undo();
				}

			});

			sview.setBorder(BorderFactory.createEtchedBorder());
			GridBagLayout gbl = new GridBagLayout();
			main.getContentPane().setLayout(gbl);
			GridBagConstraints cs = GUIToolbox.buildConstraints(0, 0, 1, 2);
			cs.weightx = 100;cs.weighty = 100; cs.fill = GridBagConstraints.BOTH;
			GridBagConstraints cv = GUIToolbox.buildConstraints(1, 0, 1, 1);
			GridBagConstraints ct = GUIToolbox.buildConstraints(1, 1, 1, 1);
			gbl.setConstraints(sview, cs);
			gbl.setConstraints(tview, ct);
			gbl.setConstraints(view, cv);
			
			main.getContentPane().add(sview);
			main.getContentPane().add(view);
			main.getContentPane().add(tview);
			//main.getContentPane().add(undo);
			main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//main.pack();
			main.setVisible(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
