package wwmm.crystaleye;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Document;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Utility class for handling Web-related methods.
 * 
 * @author Nick Day
 * @version 0.2
 *
 */
public class WebUtils {
	
	private static final Logger LOG = Logger.getLogger(WebUtils.class);

	public static String fetchWebPage(String url) {
		HttpClient client = new HttpClient();
		InputStream in = null;
		GetMethod method = new GetMethod(url);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
				new DefaultHttpMethodRetryHandler(10, false));
		method.getParams().setSoTimeout(60000);
		int statusCode;
		try {
			statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				LOG.warn("Method failed: "+method.getStatusLine());
			}
			in = method.getResponseBodyAsStream();
			return org.apache.commons.io.IOUtils.toString(in);
		} catch (Exception e) {
			throw new RuntimeException("Exception while trying to fetch web page ("+url+"), due to: "+e.getMessage());
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
			org.apache.commons.io.IOUtils.closeQuietly(in);
		}
	}

	public static void saveFileFromUrl(String url, String outPath) {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(url);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
				new DefaultHttpMethodRetryHandler(10, false));
		method.getParams().setSoTimeout(20000);
		int statusCode;
		InputStream in = null;
		FileOutputStream fos = null;
		try {
			statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				LOG.warn("Method failed: "+method.getStatusLine());
			}
			in = method.getResponseBodyAsStream();
			fos = new FileOutputStream(outPath);
			byte[] buf = new byte[256];
			int read = 0;
			while ((read = in.read(buf)) > 0) {
				fos.write(buf, 0, read);
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception while trying to save file from URL ("+url+"), due to: "+e.getMessage());
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
			org.apache.commons.io.IOUtils.closeQuietly(in);
			org.apache.commons.io.IOUtils.closeQuietly(fos);
		}
	}

	public static Document parseWebPage(String url) {
		return parseAndTidyHtml(fetchWebPage(url));
	}

	public static Document parseWebPageAndRemoveComments(String url) {
		String response = fetchWebPage(url);

		String patternStr = "<!--(.*)?-->";
		String replacementStr = "";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(response);
		String html = matcher.replaceAll(replacementStr);

		// done specifically because I found this horror on the 
		// Chemistry Letters site and it broke Tagsoup/XOM!
		patternStr = "<!-->";
		replacementStr = "";
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(html);
		html = matcher.replaceAll(replacementStr);

		return parseAndTidyHtml(html);
	}

	public static Document parseAndTidyHtml(String html) {
		StringReader sr = new StringReader(html);
		BufferedReader br = new BufferedReader(sr);
		try {
			XMLReader tagsoup = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
			return new Builder(tagsoup).build(br);
		} catch (Exception e) {
			throw new RuntimeException("Exception parsing HTML due to: "+e.getMessage(), e);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(br);
			org.apache.commons.io.IOUtils.closeQuietly(sr);
		}
	}

}
