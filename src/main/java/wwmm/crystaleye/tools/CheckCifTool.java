package wwmm.crystaleye.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import wwmm.pubcrawler.BasicHttpClient;
import wwmm.pubcrawler.Utils;

/**
 * <p>
 * The <code>CheckCifTool</code> class provides a wrapper for
 * the International Union of Crystallography's CheckCIF service so 
 * that it can be called programmatically rather than having to use
 * the webpage manually. 
 * </p>
 * 
 * @author Nick Day
 * @version 1.1
 */
public class CheckCifTool {
	
	// The endpoint to which a CIF is posted to calculate the CheckCIF. 
	private final String POST_ENDPOINT = "http://dynhost1.iucr.org/cgi-bin/checkcif.pl";
	private PostMethod postMethod;
	
	private static final Logger LOG = Logger.getLogger(CheckCifParser.class);
	
	/**
	 * <p>
	 * Sends the provided CIF file off to the CheckCIF service.  The 
	 * resulting CheckCIF HTML is returned as a <code>String</code>.
	 * </p>
	 * 
	 * @param cifFile - a file containing a CIF.
	 * 
	 * @return String containing the CheckCIF HTML.
	 */
	public String getCheckcifString(File cifFile) {
		InputStream in = getCheckcifStream(cifFile);
		String checkcif = null;
		try {
			checkcif = IOUtils.toString(in);
		} catch (IOException e) {
			throw new RuntimeException("Error converting CheckCIF stream to string.", e);
		} finally {
			IOUtils.closeQuietly(in);
			if (postMethod != null) {
				postMethod.releaseConnection();
			}
		}
		return checkcif;
	}
	
	/**
	 * <p>
	 * Sends the provided CIF file off to the CheckCIF service.  The 
	 * resulting CheckCIF HTML is tidied and converted into an XML
	 * Document (using Tagsoup) before being returned.
	 * </p>
	 * 
	 * @param cifFile - a file containing a CIF.
	 * 
	 * @return XML Document containing tidied CheckCIF HTML.
	 */
	public Document getCheckcifHtml(File cifFile) {
		InputStream in = getCheckcifStream(cifFile);
		Document xml = null;
		try {
			Builder builder = BasicHttpClient.getTagsoupBuilder();
			xml = Utils.parseXml(builder, in);
		} finally {
			IOUtils.closeQuietly(in);
			if (postMethod != null) {
				postMethod.releaseConnection();
			}
		}
		return xml;
	}
	
	/**
	 * <p>
	 * Sends the provided CIF file off to the CheckCIF service.  The
	 * resulting CheckCIF HTML is returned in an <code>InputStream</code>.
	 * Any class methods calling this should remember to close the
	 * returned InputStream AND release the connection for postMethod. See
	 * getCheckcifHtml for an example. 
	 * </p>
	 * 
	 * @param cifFile - a file containing a CIF.
	 * 
	 * @return InputStream containing the CheckCIF HTML.
	 */
	private InputStream getCheckcifStream(File cifFile) {
		InputStream in = null;
		try {
			postMethod = new PostMethod(POST_ENDPOINT);
			Part[] parts = { new FilePart("file", cifFile),
					new StringPart("runtype", "fullpublication"),
					new StringPart("UPLOAD", "Send CIF for checking") };
			postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
					new DefaultHttpMethodRetryHandler(5, false));
			postMethod.setRequestEntity(new MultipartRequestEntity(parts,
					postMethod.getParams()));
			HttpClient client = new HttpClient();
			int statusCode = client.executeMethod(postMethod);
			if (statusCode != HttpStatus.SC_OK) {
				throw new RuntimeException("Non-success status code returned: "+statusCode);
			}
			in = postMethod.getResponseBodyAsStream();
		} catch (IOException e) {
			throw new RuntimeException("Error calculating checkcif.", e);
		}
		return in;
	}
	
	/**
	 * <p>
	 * Main method is meant for demonstration purposes only. Does not
	 * require any arguments.
	 * </p>
	 * 
	 */
	public static void main(String[] args) {
		CheckCifTool tool = new CheckCifTool();
		File cifFile = new File("c:/Users/ned24/Desktop/2.cif");
		Document doc = tool.getCheckcifHtml(cifFile);
		System.out.println(doc.toXML());
	}

}
