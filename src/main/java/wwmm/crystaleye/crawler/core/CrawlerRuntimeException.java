package wwmm.crystaleye.crawler.core;

/**
 * <p>
 * The <code>CrawlerRuntimeException</code> is meant to be used for
 * generic runtime exceptions that occur during crawler execution.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class CrawlerRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -8993316955991175892L;

	public CrawlerRuntimeException() {
		super();
	}

	public CrawlerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CrawlerRuntimeException(String message) {
		super(message);
	}

	public CrawlerRuntimeException(Throwable cause) {
		super(cause);
	}
	
}
