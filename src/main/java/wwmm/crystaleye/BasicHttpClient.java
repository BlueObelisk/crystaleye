package wwmm.crystaleye;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Document;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import wwmm.crystaleye.util.Utils;

/**
 * <p>
 * The <code>BasicHttpClient</code> class provides convenience methods for
 * performing common HTTP methods and retrieving the results in various forms.
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 * 
 */
public class BasicHttpClient {

	private HttpClient client;
	private HttpMethod method;

	public BasicHttpClient() {
		client = new HttpClient();
	}

	public BasicHttpClient(HttpClient client) {
		this.client = client;
	}

	/**
	 * <p>
	 * Executes a HTTP GET on the resource at the provided <code>URI</code>.  The 
	 * resource contents are returned in an <code>InputStream</code>.
	 * </p>
	 * 
	 * @param uri - the resource to retrieve.
	 * 
	 * @return InputStream containing the contents of the resource at the
	 * provided <code>URI</code>.
	 * 
	 */
	private InputStream getResourceStream(URI uri) {
		InputStream in = null;
		method = executeGET(uri);
		try {
			in = method.getResponseBodyAsStream();
		} catch (IOException e) {
			throw new RuntimeException("Exception getting response stream for: "+uri);
		}
		return in;
	}
	
	/**
	 * <p>
	 * Executes a HTTP GET on the resource at the provided <code>URI</code>.  The 
	 * resource contents are returned in a String.
	 * </p>
	 * 
	 * @param uri - the resource to retrieve.
	 * 
	 * @return String containing the contents of the resource at the provided 
	 * <code>URI</code>.
	 * 
	 */
	public String getResourceString(URI uri) {
		InputStream in = getResourceStream(uri);
		String html = null;
		try {
			html = IOUtils.toString(in);
		} catch (IOException e) {
			throw new RuntimeException("Exception converting webpage stream to string: "+uri, e);
		} finally {
			IOUtils.closeQuietly(in);
			if (method != null) {
				method.releaseConnection();
			}
		}
		return html;
	}

	/**
	 * <p>
	 * Executes a HTTP GET on the resource at the provided <code>URI</code>.  The 
	 * resource contents are parsed by Tagsoup and returned as a XOM 
	 * <code>Document</code>.
	 * </p>
	 * 
	 * @param uri - the resource to retrieve.
	 * 
	 * @return XML <code>Document</code> containing the contents of the resource 
	 * at the provided <code>URI</code> after they have been parsed using Tagsoup.
	 * 
	 */
	public Document getResourceHTML(URI uri) {
		InputStream in = getResourceStream(uri);
		Document doc = null;
		try {
			Builder builder = getTagsoupBuilder();
			doc = Utils.parseXml(builder, in);
		} finally {
			IOUtils.closeQuietly(in);
			if (method != null) {
				method.releaseConnection();
			}
		}
		return doc;
	}
	
	/**
	 * <p>
	 * Executes a HTTP GET on the resource at the provided <code>URI</code>.  
	 * The resource contents are first obtained as a <code>String</code> so that
	 * a horrible hack can be performed to remove any comments before the HTML is 
	 * passed to Tagsoup.  This is someimtes necessary to remove anything that might
	 * cause Tagsoup or XOM to fail when parsing the HTML (such as including -- in 
	 * middle of a comment).  After the comments have been removed, the HTML is tidied
	 * and parsed by Tagsoup into a XOM <code>Document</code>.
	 * </p>
	 * 
	 * @param uri of the resource for which you want to obtain the HTML.
	 * 
	 * @return XML <code>Document</code> containined the parsed HTML.
	 */
	public Document getResourceHTMLMinusComments(URI uri) {
		String html = getResourceString(uri);

		String patternStr = "<!--(.*)?-->";
		String replacementStr = "";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(html);
		html = matcher.replaceAll(replacementStr);
		patternStr = "<!-->";
		replacementStr = "";
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(html);
		html = matcher.replaceAll(replacementStr);

		StringReader sr = new StringReader(html);
		BufferedReader br = new BufferedReader(sr);
		Document doc = null;
		try {
			Builder builder = getTagsoupBuilder();
			doc = Utils.parseXml(builder, br);
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(sr);
		}
		return doc;
	}
	
	/**
	 * <p>
	 * Executes a HTTP GET on the resource at the provided <code>URI</code>.  The 
	 * resource contents are parsed using the default XOM <code>Builder</code> and 
	 * returned as a XOM <code>Document</code>.
	 * </p>
	 * 
	 * @param uri - the resource to retrieve.
	 * 
	 * @return XML <code>Document</code> containing the contents of the resource at 
	 * the provided <code>URI</code>.
	 * 
	 */
	public Document getResourceXML(URI uri) {
		InputStream in = getResourceStream(uri);
		Document doc = null;
		try {
			doc = Utils.parseXml(in);
		} finally {
			IOUtils.closeQuietly(in);
			if (method != null) {
				method.releaseConnection();
			}
		}
		return doc;
	}	
	
	/**
	 * <p>
	 * Executes a HTTP POST using the provided <code>postMethod</code> (which will 
	 * contain the details of the POST URI).  The POST results retrieved are 
	 * returned in an <code>InputStream</code>.
	 * </p>
	 * 
	 * @param postMethod - POST method to execute.
	 * 
	 * @return InputStream containing the results of the POST method.
	 * 
	 */
	private InputStream getPostResultStream(PostMethod postMethod) {
		method = postMethod;
		executeMethod(method);
		InputStream in = null;
		try {
			in = method.getResponseBodyAsStream();
		} catch (IOException e) {
			throw new RuntimeException("Problem getting POST response stream.", e);
		}
		return in;
	}
	
	/**
	 * <p>
	 * Executes a HTTP POST using the provided <code>postMethod</code> (which will 
	 * contain the details of the POST URI).  The POST results retrieved are 
	 * returned in a <code>String</code>.
	 * </p>
	 * 
	 * @param postMethod - POST method to execute.
	 * 
	 * @return String containing the results of the POST method.
	 * 
	 */
	public String getPostResultString(PostMethod postMethod) {
		InputStream in = getPostResultStream(postMethod);
		String result = null;
		try {
			result = IOUtils.toString(in);
		} catch (IOException e) {
			throw new RuntimeException("Problem converting POST result stream to string.", e);
		} finally {
			IOUtils.closeQuietly(in);
			if (method != null) {
				method.releaseConnection();
			}
		}
		return result;
	}
	
	/**
	 * <p>
	 * Executes a HTTP POST using the provided <code>postMethod</code> (which will 
	 * contain the details of the POST URI).  The POST results retrieved are returned 
	 * in a XOM <code>Document</code>.
	 * </p>
	 * 
	 * @param postMethod - POST method to execute.
	 * 
	 * @return XML <code>Document</code> containing the results of the POST method.
	 * 
	 */
	public Document getPostResultXML(PostMethod postMethod) {
		InputStream in = getPostResultStream(postMethod);
		Document doc = null;
		try {
			Builder builder = getTagsoupBuilder();
			doc = Utils.parseXml(builder, in);
		} finally {
			IOUtils.closeQuietly(in);
			if (method != null) {
				method.releaseConnection();
			}
		}
		return doc;
	}

	/**
	 * <p>
	 * Executes a HTTP HEAD on the resource at the provided URI.  All of the
	 * resource headers are retrieved and returned.
	 * </p>
	 * 
	 * @param uri - the resource for which to retrieve the headers.
	 * 
	 * @return array containing all of the HTTP headers for the resource at
	 * the provided <code>URI</code>.
	 * 
	 */
	public Header[] getHeaders(URI uri) {
		method = executeHEAD(uri);
		try {
			return method.getResponseHeaders();
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
	}

	/**
	 * <p>
	 * Constructs a XOM <code>Builder</code> using the Tagsoup HTML parser.  The 
	 * <code>Builder</code> is used for all parsing and tidying of HTML in this 
	 * class.
	 * </p> 
	 * 
	 * @return XOM <code>Builder</code> created with the Tagsoup HTML parser.
	 * 
	 */
	private Builder getTagsoupBuilder() {
		XMLReader tagsoup = null;
		try {
			tagsoup = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
		} catch (SAXException e) {
			throw new RuntimeException("Exception whilst creating XMLReader from org.ccil.cowan.tagsoup.Parser");
		}
		return new Builder(tagsoup);
	}

	/**
	 * <p>
	 * Simply executes the HTTP method provided as a parameter.
	 * </p>
	 * 
	 * @param method - HTTP method to execute
	 * 
	 */
	private void executeMethod(HttpMethod method) {
		URI uri = null;
		try {
			uri = method.getURI();
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				throw new RuntimeException("Problems executing "+method.getName()
						+" method on "+uri+". Returned status code = "+statusCode);
			}
		} catch (HttpException e) {
			throw new RuntimeException("HttpException executing "+method.getName()
					+" method on "+uri, e);
		} catch (IOException e) {
			throw new RuntimeException("IOException executing "+method.getName()
					+" method on "+uri, e);
		}
	}

	/**
	 * <p>
	 * Executes a HTTP GET on the provided <code>URI</code>.
	 * </p>
	 * 
	 * @param uri - resource to GET
	 * 
	 * @return Apache <code>HTTPClient</code> wrapper containing the GET method 
	 * details and results.
	 * 
	 */
	public GetMethod executeGET(URI uri) {
		method = new GetMethod();
		try {
			method.setURI(uri);
			executeMethod(method);
		} catch (URIException e) {
			throw new RuntimeException("Exception setting the URI for the HTTP " +
					"GET method: "+uri, e);
		}
		return (GetMethod)method;
	}

	/**
	 * <p>
	 * Executes a HTTP HEAD on the provided <code>URI</code>.
	 * </p>
	 * 
	 * @param uri - resource to HEAD
	 * 
	 * @return Apache <code>HTTPClient</code> wrapper containing the HEAD method 
	 * details and results.
	 * 
	 */
	public HeadMethod executeHEAD(URI uri) {
		HeadMethod method = new HeadMethod();
		try {
			method.setURI(uri);
			executeMethod(method);
		} catch (URIException e) {
			throw new RuntimeException("Exception setting the URI for the HTTP " +
					"GET method: "+uri, e);
		}
		return (HeadMethod)method;
	}

	/**
	 * <p>
	 * Performs a HTTP HEAD on the provided <code>URI</code> and returns the value 
	 * of the Content-Type header.
	 * </p>
	 * 
	 * @param uri - resource for which to retrieve the Content-Type
	 * 
	 * @return If the header exists, the String value of the resource Content-Type 
	 * is returned.  If the header does not exist, then <code>null</code> is 
	 * returned.
	 * 
	 */
	public String getContentType(URI uri) {
		Header[] headers = this.getHeaders(uri);
		String contentType = null;
		for (Header header : headers) {
			String name = header.getName();
			if ("Content-Type".equals(name) ||
					"Content-type".equals(name)) {
				contentType = header.getValue();
			}
		}
		return contentType;
	}

	/**
	 * <p>
	 * Main method only for demonstration of class use. Does not require
	 * any arguments.
	 * </p>
	 * 
	 * @param args
	 * 
	 * @throws URIException
	 * @throws NullPointerException
	 * 
	 */
	public static void main(String[] args) throws URIException, NullPointerException {
		BasicHttpClient bhc = new BasicHttpClient();
		Header[] headers = bhc.getHeaders(new URI("http://pubs.rsc.org/suppdata/CC/b8/b811528a/b811528a.pdf", false));
		for (Header h : headers) {
			System.out.println(h.getName()+" = "+h.getValue());
		}
	}

}
