package fs.event;

/**
 * This listener listens for processes preparing data , for example a dialog. It
 * is notified with the result, when the process is finished
 * 
 * @author Simon Hampe
 * 
 */
public interface DataRetrievalListener {

	public void dataReady(Object data);

}
