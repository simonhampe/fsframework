package fs.test;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.undo.UndoManager;

import fs.polyglot.model.Group;
import fs.polyglot.model.GroupTreeModel;
import fs.polyglot.model.LanguageListModel;
import fs.polyglot.model.PolyglotTableModel;
import fs.polyglot.undo.TableUndoManager;
import fs.polyglot.view.GroupTreeCellRenderer;
import fs.polyglot.view.GroupTreeView;
import fs.polyglot.view.LanguageListCellRenderer;
import fs.polyglot.view.LanguageListView;
import fs.xml.PolyglotStringTable;
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
			JFrame main = new JFrame("polyglot model test");
			main.setBounds(100, 100, 300, 300);
			main.setLayout(new FlowLayout());

			String filename = "language/fsfwStringTable.xml";
			// String filename = "examples/PolyglotStringTable.xml";
			final PolyglotTableModel table = new PolyglotTableModel(XMLToolbox
					.loadXMLFile(new File(filename)), null);

			final UndoManager manager = TableUndoManager.getUndoManager(table);

			LanguageListView view = new LanguageListView(null, null, null,
					table);
			GroupTreeView tview = new GroupTreeView(null, null, null, table);

			 JTree tree = new JTree();
			 tree.setModel(new GroupTreeModel(table, true, true));
			 GroupTreeCellRenderer render =new	 GroupTreeCellRenderer(null,null,null,true, "bla");
			 render.setNullString(table.getTableID());
			 tree.setCellRenderer(render);
			// tree.repaint();
			// ToolTipManager.sharedInstance().registerComponent(tree);
			//			
			// JTree gtree = new JTree();
			// gtree.setModel(new GroupTreeModel(table,false,false));
			// gtree.setCellRenderer(new
			// GroupTreeCellRenderer(null,null,null,false));
			// ToolTipManager.sharedInstance().registerComponent(gtree);

			JButton undo = new JButton("undo");
			undo.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println(manager.getUndoPresentationName());
					if(manager.canUndo()) manager.undo();
				}

			});

			 main.getContentPane().add(tree);
			// main.getContentPane().add(gtree);
			// main.getContentPane().add(stree);
			main.getContentPane().add(view);
			main.getContentPane().add(tview);
			main.getContentPane().add(undo);
			main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			main.pack();
			main.setVisible(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
