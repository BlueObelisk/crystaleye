package uk.ac.cam.ch.crystaleye;

import nu.xom.ParsingException;

public class CrystalEyeRuntimeException extends RuntimeException {
    
    /**
     * 
     */
    private static final long serialVersionUID = 6545556008077747195L;

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

    /**
     * parsing exception.
     * 
     * @param msg
     *            additional message
     * @param e
     *            exception
     */
    public CrystalEyeRuntimeException(String msg, ParsingException e) {
        super("PARSE_ERROR [at " + e.getLineNumber() + ":"
                + e.getColumnNumber() + "] " + e.getMessage() + " | " + msg, e);
    }

    /**
     * parsing exception.
     * 
     * @param e
     *            exception
     * @param msg
     *            additional message
     */
    public CrystalEyeRuntimeException(ParsingException e) {
        this("PARSE_ERROR [at " + e.getLineNumber() + ":" + e.getColumnNumber()
                + "] " + e.getMessage());
    }

    public CrystalEyeRuntimeException(String msg, Exception e) {
        super(msg, e);
    }
}
