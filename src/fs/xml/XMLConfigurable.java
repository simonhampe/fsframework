package fs.xml;

import org.dom4j.*;

/**
 * Defines a type to be XML - configurable, i.e. it is able
 * to store to and read from an org.dom4j.Node its
 * configuration. Different XMLConfigurables should
 * distinguish themselves from each other by declaring
 * unique Type IDs. If a configuration is stored, it usually
 * will be contained in a node bearing this ID as a name.
 * 
 * @author Simon Hampe
 */
public interface XMLConfigurable {
	/**
	 * Returns the (supposedly) unique ID
	 */
	public String getIdentifier();
	
	/**
	 * Configures the object according to the configuration
	 * passed
	 */
	public void configure(Node n);
	
	/**
	 * Returns the current configuration
	 */
	public Node getConfiguration();
	
}
