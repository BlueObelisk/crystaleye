package wwmm.crystaleye;


public class RetrieveWebpageException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5617734149251656288L;

	protected RetrieveWebpageException() {
        super();
    }
 
    /**
     * creates CMLRuntime with message.
     * 
     * @param msg
     */
    public RetrieveWebpageException(String msg) {
        super(msg);
    }

    /**
     * creates CMLRuntime from CMLException.
     * 
     * @param exception
     */
    public RetrieveWebpageException(Exception exception) {
        super(exception);
    }

    public RetrieveWebpageException(String msg, Exception e) {
        super(msg, e);
    }
}
