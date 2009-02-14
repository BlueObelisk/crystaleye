package wwmm.crystaleye.crawlers;

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
