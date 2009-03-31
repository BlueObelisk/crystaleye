package wwmm.crystaleye.crawler.core;

import nu.xom.Document;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import wwmm.crystaleye.BasicHttpClient;
import wwmm.crystaleye.Utils;

/**
 * <p>
 * The <code>CrawlerHttpClient</code> class is a wrapper of BasicHttpClient.  Here, 
 * all HttpClient calls also include a period of sleep.  This is useful, as if a
 * number of resources are being retrieved from one provider, no Denial Of Service 
 * (DOS) will be caused.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class CrawlerHttpClient extends BasicHttpClient {
	
	/**
	 * <p>
	 * The maximum time (in milliseconds) that each 
	 * process will be forced to sleep for.
	 * </p>
	 * 
	 */
	int maxSleep = 1500;
	
	private static final Logger LOG = Logger.getLogger(CrawlerHttpClient.class);
	
	public CrawlerHttpClient() {
		super();
	}
	
	/**
	 * <p>
	 * Executes a HTTP GET on the resource at the provided <code>URI</code>.  The 
	 * resource contents are returned in a String.  Includes a period of sleep.
	 * </p>
	 * 
	 * @param uri - the resource to retrieve.
	 * 
	 * @return String containing the contents of the resource at the provided 
	 * <code>URI</code>.
	 * 
	 * @see BasicHttpClient.getResourceString(URI)
	 * 
	 */
	@Override
	public String getResourceString(URI uri) {
		Utils.sleep(maxSleep);
		return super.getResourceString(uri);
	}

	/**
	 * <p>
	 * Executes a HTTP GET on the resource at the provided <code>URI</code>.  The 
	 * resource contents are parsed by Tagsoup and returned as a XOM 
	 * <code>Document</code>.  Includes a period of sleep.
	 * </p>
	 * 
	 * @param uri - the resource to retrieve.
	 * 
	 * @return XML <code>Document</code> containing the contents of the resource 
	 * at the provided <code>URI</code> after they have been parsed using Tagsoup.
	 * 
	 * @see BasicHttpClient.getResourceHTML(URI)
	 * 
	 */
	@Override
	public Document getResourceHTML(URI uri) {
		Utils.sleep(maxSleep);
		return super.getResourceHTML(uri);
	}
	
	/**
	 * <p>
	 * Executes a HTTP GET on the resource at the provided <code>URI</code>.  
	 * The resource contents are first obtained as a <code>String</code> so that
	 * a horrible hack can be performed to remove any comments before the HTML is 
	 * passed to Tagsoup.  This is someimtes necessary to remove anything that might
	 * cause Tagsoup or XOM to fail when parsing the HTML (such as including -- in 
	 * middle of a comment).  After the comments have been removed, the HTML is tidied
	 * and parsed by Tagsoup into a XOM <code>Document</code>.  Includes
	 * a period of sleep.
	 * </p>
	 * 
	 * @param uri of the resource for which you want to obtain the HTML.
	 * 
	 * @return XML <code>Document</code> containined the parsed HTML.
	 */
	@Override
	public Document getResourceHTMLMinusComments(URI uri) {
		Utils.sleep(maxSleep);
		return super.getResourceHTMLMinusComments(uri);
	}
	
	/**
	 * <p>
	 * Executes a HTTP GET on the resource at the provided <code>URI</code>.  The 
	 * resource contents are parsed using the default XOM <code>Builder</code> and 
	 * returned as a XOM <code>Document</code>.  Includes a period of sleep.
	 * </p>
	 * 
	 * @param uri - the resource to retrieve.
	 * 
	 * @return XML <code>Document</code> containing the contents of the resource at 
	 * the provided <code>URI</code>.
	 * 
	 * @see BasicHttpClient.getResourceXML
	 * 
	 */
	@Override
	public Document getResourceXML(URI uri) {
		Utils.sleep(maxSleep);
		return super.getResourceXML(uri);
	}
	
	/**
	 * <p>
	 * Executes a HTTP POST using the provided <code>postMethod</code> (which will 
	 * contain the details of the POST URI).  The POST results retrieved are 
	 * returned in a <code>String</code>.  Includes a period of sleep.
	 * </p>
	 * 
	 * @param postMethod - POST method to execute.
	 * 
	 * @return String containing the results of the POST method.
	 * 
	 * @see BasicHttpClient.getPostResultString(PostMethod)
	 * 
	 */
	@Override
	public String getPostResultString(PostMethod postMethod) {
		Utils.sleep(maxSleep);
		return super.getPostResultString(postMethod);
	}
	
	/**
	 * <p>
	 * Executes a HTTP POST using the provided <code>postMethod</code> (which will 
	 * contain the details of the POST URI).  The POST results retrieved are returned 
	 * in a XOM <code>Document</code>.  Includes a period of sleep.
	 * </p>
	 * 
	 * @param postMethod - POST method to execute.
	 * 
	 * @return XML <code>Document</code> containing the results of the POST method.
	 * 
	 * @see BasicHttpClient.getPostResultXML(PostMethod)
	 * 
	 */
	@Override
	public Document getPostResultXML(PostMethod postMethod) {
		Utils.sleep(maxSleep);
		return super.getPostResultXML(postMethod);
	}

	/**
	 * <p>
	 * Executes a HTTP HEAD on the resource at the provided URI.  All of the
	 * resource headers are retrieved and returned.  Includes a period of 
	 * sleep.
	 * </p>
	 * 
	 * @param uri - the resource for which to retrieve the headers.
	 * 
	 * @return array containing all of the HTTP headers for the resource at
	 * the provided <code>URI</code>.
	 * 
	 * @see BasicHttpClient.getHeader(URI)
	 * 
	 */
	@Override
	public Header[] getHeaders(URI uri) {
		Utils.sleep(maxSleep);
		return super.getHeaders(uri);
	}

	/**
	 * <p>
	 * Executes a HTTP GET on the provided <code>URI</code>.  Includes a period of
	 * sleep.
	 * </p>
	 * 
	 * @param uri - resource to GET
	 * 
	 * @return Apache <code>HTTPClient</code> wrapper containing the GET method 
	 * details and results.
	 * 
	 * @see BasicHttpClient.executeGET(URI)
	 * 
	 */
	@Override
	public GetMethod executeGET(URI uri) {
		Utils.sleep(maxSleep);
		return super.executeGET(uri);
	}

	/**
	 * <p>
	 * Executes a HTTP HEAD on the provided <code>URI</code>.  Includes a period of
	 * sleep.
	 * </p>
	 * 
	 * @param uri - resource to HEAD
	 * 
	 * @return Apache <code>HTTPClient</code> wrapper containing the HEAD method 
	 * details and results.
	 * 
	 * @see BasicHttpClient.executeHEAD(URI)
	 * 
	 */
	@Override
	public HeadMethod executeHEAD(URI uri) {
		Utils.sleep(maxSleep);
		return super.executeHEAD(uri);
	}

	/**
	 * <p>
	 * Performs a HTTP HEAD on the provided <code>URI</code> and returns the value 
	 * of the Content-Type header.  Includes a period of sleep.
	 * </p>
	 * 
	 * @param uri - resource for which to retrieve the Content-Type
	 * 
	 * @return If the header exists, the String value of the resource Content-Type 
	 * is returned.  If the header does not exist, then <code>null</code> is 
	 * returned.
	 * 
	 * @see BasicHttpClient.getContentType(URI)
	 * 
	 */
	@Override
	public String getContentType(URI uri) {
		Utils.sleep(maxSleep);
		return super.getContentType(uri);
	}

}
