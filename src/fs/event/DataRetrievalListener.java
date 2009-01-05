package fs.event;

/**
 * This listener listens for processes preparing data , for example a dialog. It
 * is notified with the result, when the process is finished
 * 
 * @author Simon Hampe
 * 
 */
public interface DataRetrievalListener {

	/**
	 * The data is ready for retrieval
	 * @param source The object processing the data
	 * @param data The data  prepared
	 */
	public void dataReady(Object source, Object data);

}
