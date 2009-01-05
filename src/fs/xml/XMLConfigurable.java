package fs.xml;

import org.dom4j.*;

/**
 * Defines a type to be XML - configurable, i.e. it is able to store to and read
 * from an org.dom4j.Node its configuration. Different XMLConfigurables should
 * distinguish themselves from each other by declaring unique Type IDs. If a
 * configuration is stored, it usually will be contained in a node bearing this
 * ID as a name.
 * 
 * @author Simon Hampe
 */
public interface XMLConfigurable {
	/**
	 * @return the (supposedly) unique ID
	 */
	public String getIdentifier();

	/**
	 * Indicates whether this Object has been configured yet (usually this means
	 * that either an appropriate constructor has been used or conigure(Node n)
	 * has been called successfully at least once)
	 */
	public boolean isConfigured();

	/**
	 * Configures the object according to the configuration passed
	 * 
	 * @throws XMLWriteConfigurationException
	 *             , if the configuration passed on ist not a valid
	 *             configuration or null
	 */
	public void configure(Node n) throws XMLWriteConfigurationException;

	/**
	 * @return The current configuration
	 * @throws XMLReadConfigurationException
	 *             if isConfigured() == false or the configuration can not be
	 *             returned for some other reason
	 */
	public Element getConfiguration() throws XMLReadConfigurationException;

}
