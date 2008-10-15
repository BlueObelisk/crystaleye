package wwmm.crystaleye;


public class CrystalEyeRuntimeException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -707359438428531649L;

	protected CrystalEyeRuntimeException() {
        super();
    }
 
    /**
     * creates CMLRuntime with message.
     * 
     * @param msg
     */
    public CrystalEyeRuntimeException(String msg) {
        super(msg);
    }

    /**
     * creates CMLRuntime from CMLException.
     * 
     * @param exception
     */
    public CrystalEyeRuntimeException(Exception exception) {
        super(exception);
    }

    public CrystalEyeRuntimeException(String msg, Exception e) {
        super(msg, e);
    }
}
