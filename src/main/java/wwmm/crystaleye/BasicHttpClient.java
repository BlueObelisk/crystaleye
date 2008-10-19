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
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import wwmm.crystaleye.util.Utils;

public class BasicHttpClient {

	HttpClient client;
	HttpMethod method;
	
	public BasicHttpClient() {
		client = new HttpClient();
	}
	
	public BasicHttpClient(HttpClient client) {
		this.client = client;
	}
	
	public InputStream getWebpageStream(URI uri) {
		method = new GetMethod();
		InputStream in = null;
		try {
			method.setURI(uri);
			executeMethod(method);
			in = method.getResponseBodyAsStream();
		} catch (URIException e) {
			throw new RuntimeException("Exception setting the URI for the HTTP GET method: "+uri, e);
		} catch (IOException e) {
			throw new RuntimeException("Exception getting response stream for: "+uri);
		}
		return in;
	}
	
	public Document getWebpageDocument(URI uri) {
		InputStream in = getWebpageStream(uri);
		Document doc = null;
		try {
			Builder builder = getTagsoupBuilder();
			doc = Utils.parseXml(builder, in);
		} finally {
			IOUtils.closeQuietly(in);
		}
		return doc;
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
			doc = Utils.parseXml(br);
		} finally {
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(sr);
		}
		return doc;
	}
	
	public Header[] getHeaders(URI uri) {
		method = new HeadMethod();
		try {
			method.setURI(uri);
			executeMethod(method);
			return method.getResponseHeaders();
		} catch (URIException e) {
			throw new RuntimeException("Exception setting the URI for the HTTP HEAD method: "+uri, e);
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
	
	public HttpMethod getMethod() {
		return method;
	}
	
	public static void main(String[] args) throws URIException, NullPointerException {
		BasicHttpClient bhc = new BasicHttpClient();
		Header[] headers = bhc.getHeaders(new URI("http://pubs.rsc.org/suppdata/CC/b8/b811528a/b811528a.pdf", false));
		for (Header h : headers) {
			System.out.println(h.getName()+" = "+h.getValue());
		}
	}
	
}
