package fs.test;

import java.util.HashSet;

import fs.polyglot.model.Group;
import fs.polyglot.model.Language;
import fs.polyglot.model.TreeObject;
import fs.polyglot.model.Variant;

/**
 * Tests the properties of TreeObject and descendants
 * @author Simon Hampe
 *
 */
public class GroupTreeObjectTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TreeObject to1 = new TreeObject("a");
		TreeObject to2 = new TreeObject("a");
		System.out.println(to1.equals(to2));
		HashSet<TreeObject> toset = new HashSet<TreeObject>();
		toset.add(to1);toset.add(to2);
		System.out.println(toset.size());

		Group g1 = new Group("a",false);
		Variant v1 = new Variant("a","id",new Language("l","agh",false,0),"bla");
		System.out.println(g1.equals(v1));
		System.out.println(v1.equals(g1));
		Variant v2 = new Variant("a","id",new Language("l","brgh",true,100),"argh");
		System.out.println(v1.equals(v2));
		toset.clear();
		toset.add(v1);toset.add(v2);
		System.out.println(toset.size());
		
	}

}
