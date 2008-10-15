package wwmm.crystaleye.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Document;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class HttpUtils {

	private static HttpClient client;
	private static GetMethod getMethod;
	


	static {
		client = new HttpClient();
		getMethod = new GetMethod();
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(10, false));
		getMethod.getParams().setSoTimeout(60000);
	}

	public static Document getWebpageAsXML(URI uri) throws Exception  {
		String response = null;
		response = HttpUtils.getWebpageAsString(uri);
		return parseHtmlWithTagsoup(response);
	}

	public static Document getWebpageMinusCommentsAsXML(URI uri) throws Exception {
		String response;
		response = HttpUtils.getWebpageAsString(uri);
		String patternStr = "<!--(.*)?-->";
		String replacementStr = "";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(response);
		String html = matcher.replaceAll(replacementStr);

		// I found the horror below on the Chemistry Letters site and it broke Tagsoup/XOM!
		patternStr = "<!-->";
		replacementStr = "";
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(html);
		html = matcher.replaceAll(replacementStr);

		Document doc = parseHtmlWithTagsoup(html);
		return doc;
	}

	public static Document parseHtmlWithTagsoup(String htmlString) throws Exception {
		Document doc = null;
		StringReader sr = null;
		BufferedReader br = null;
		try {
			XMLReader tagsoup = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
			Builder builder = new Builder(tagsoup);
			sr = new StringReader(htmlString);
			br = new BufferedReader(sr);
			doc = Utils.parseXmlFile(builder, br);
		} catch (SAXException e) {
			throw new Exception("Exception while creating XMLReader from org.ccil.cowan.tagsoup.Parser", e);
		} finally {
			IOUtils.closeQuietly(sr);
			IOUtils.closeQuietly(br);
		}
		return doc;
	}

	public static String getWebpageAsString(URI uri) throws Exception {
		InputStream in = null;
		try {
			getMethod.setURI(uri);
		} catch (URIException e) {
			throw new Exception("Exception setting URI ("+uri.toString()+") for GET method.", e);
		}
		int statusCode;
		String html = "";
		try {
			statusCode = client.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				throw new Exception("Could not get webpage from "+uri.toString()+". Returned status code = "+statusCode);
			}
			in = getMethod.getResponseBodyAsStream();
			html = IOUtils.toString(in);
		} catch (HttpException e) {
			throw new Exception("HttpException getting webpage from: "+uri.toString()+", e");
		} catch (IOException e) {
			throw new Exception("IOException getting webpage from: "+uri.toString()+", e");
		} finally {
			if (getMethod != null) {
				getMethod.releaseConnection();
			}
			IOUtils.closeQuietly(in);
		}
		return html;
	}

}
