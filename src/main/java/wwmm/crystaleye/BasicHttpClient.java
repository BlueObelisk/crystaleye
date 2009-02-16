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

public class BasicHttpClient {

	private HttpClient client;
	private HttpMethod method;

	public BasicHttpClient() {
		client = new HttpClient();
	}

	public BasicHttpClient(HttpClient client) {
		this.client = client;
	}

	private InputStream getWebpageStream(URI uri) {
		InputStream in = null;
		method = executeGET(uri);
		try {
			in = method.getResponseBodyAsStream();
		} catch (IOException e) {
			throw new RuntimeException("Exception getting response stream for: "+uri);
		}
		return in;
	}
	
	public String getWebpageString(URI uri) {
		InputStream in = getWebpageStream(uri);
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

	public Document getWebpageHTML(URI uri) {
		InputStream in = getWebpageStream(uri);
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
	
	public Document getWebpageXML(URI uri) {
		InputStream in = getWebpageStream(uri);
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

	public Document getWebpageDocumentMinusComments(URI uri) {
		String html = getWebpageString(uri);

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
	
	public Document getPostResultDocument(PostMethod postMethod) {
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

	private Builder getTagsoupBuilder() {
		XMLReader tagsoup = null;
		try {
			tagsoup = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
		} catch (SAXException e) {
			throw new RuntimeException("Exception whilst creating XMLReader from org.ccil.cowan.tagsoup.Parser");
		}
		return new Builder(tagsoup);
	}

	private void executeMethod(HttpMethod method) {
		URI uri = null;
		try {
			uri = method.getURI();
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				throw new RuntimeException("Problems executing "+method.getName()+" method on "+uri+". Returned status code = "+statusCode);
			}
		} catch (HttpException e) {
			throw new RuntimeException("HttpException executing "+method.getName()+" method on "+uri, e);
		} catch (IOException e) {
			throw new RuntimeException("IOException executing "+method.getName()+" method on "+uri, e);
		}
	}

	public GetMethod executeGET(URI uri) {
		method = new GetMethod();
		try {
			method.setURI(uri);
			executeMethod(method);
		} catch (URIException e) {
			throw new RuntimeException("Exception setting the URI for the HTTP GET method: "+uri, e);
		}
		return (GetMethod)method;
	}

	public HeadMethod executeHEAD(URI uri) {
		HeadMethod method = new HeadMethod();
		try {
			method.setURI(uri);
			executeMethod(method);
		} catch (URIException e) {
			throw new RuntimeException("Exception setting the URI for the HTTP GET method: "+uri, e);
		}
		return (HeadMethod)method;
	}

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

	public static void main(String[] args) throws URIException, NullPointerException {
		BasicHttpClient bhc = new BasicHttpClient();
		Header[] headers = bhc.getHeaders(new URI("http://pubs.rsc.org/suppdata/CC/b8/b811528a/b811528a.pdf", false));
		for (Header h : headers) {
			System.out.println(h.getName()+" = "+h.getValue());
		}
	}

}
