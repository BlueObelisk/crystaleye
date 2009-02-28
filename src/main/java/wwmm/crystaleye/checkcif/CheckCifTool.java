package wwmm.crystaleye.checkcif;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
		String checkcif;
		try {
			checkcif = IOUtils.toString(in);
		} catch (IOException e) {
			throw new RuntimeException("Error converting CheckCIF stream to string.", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
		return checkcif;
	}
	
	/**
	 * <p>
	 * Sends the provided CIF file off to the CheckCIF service.  The
	 * resulting CheckCIF HTML is returned in an <code>InputStream</code>.
	 * </p>
	 * 
	 * @param cifFile - a file containing a CIF.
	 * 
	 * @return InputStream containing the CheckCIF HTML.
	 */
	public InputStream getCheckcifStream(File cifFile) {
		PostMethod filePost = null;
		InputStream in = null;
		try {
			filePost = new PostMethod(POST_ENDPOINT);
			Part[] parts = { new FilePart("file", cifFile),
					new StringPart("runtype", "fullpublication"),
					new StringPart("UPLOAD", "Send CIF for checking") };
			filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
					new DefaultHttpMethodRetryHandler(5, false));
			filePost.setRequestEntity(new MultipartRequestEntity(parts,
					filePost.getParams()));
			HttpClient client = new HttpClient();
			int statusCode = client.executeMethod(filePost);
			if (statusCode != HttpStatus.SC_OK) {
				throw new RuntimeException("Non-success status code returned: "+statusCode);
			}
			in = filePost.getResponseBodyAsStream();
		} catch (IOException e) {
			throw new RuntimeException("Error calculating checkcif.", e);
		} finally {
			if (filePost != null) {
				filePost.releaseConnection();
			}
			IOUtils.closeQuietly(in);
		}
		return in;
	}

}
