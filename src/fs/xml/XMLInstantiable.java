package fs.xml;

import org.dom4j.Node;

/**
 * Represents a type which can create an object of itself when being given an XML node in the proper format.
 * This is a weaker version of being XMLConfigurable, since it only works at creation. This is useful for example
 * for immutable objects which can nevertheless be represented and created via XML
 * @author Simon Hampe
 *
 */
public interface XMLInstantiable<T> {

	public T getInstanceByXML(Node n);
	
}
