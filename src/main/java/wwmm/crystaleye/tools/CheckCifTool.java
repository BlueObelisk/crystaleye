package wwmm.crystaleye.tools;

import nu.xom.Builder;
import nu.xom.Document;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import wwmm.pubcrawler.BasicHttpClient;
import wwmm.pubcrawler.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

	private static final Logger LOG = Logger.getLogger(CheckCifTool.class);
    private HttpResponse response;

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
            closeQuietly();
		}
		return checkcif;
	}

    private void closeQuietly() {
        if (response != null) {
            if (response.getEntity() != null) {
                try {
                    response.getEntity().consumeContent();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
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
            closeQuietly();
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
			HttpPost postMethod = new HttpPost(POST_ENDPOINT);

            MultipartEntity entity = new MultipartEntity();
            entity.addPart("file", new FileBody(cifFile));
            entity.addPart("runtype", new StringBody("fullpublication"));
            entity.addPart("UPLOAD", new StringBody("Send CIF for checking"));

//			postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//					new DefaultHttpMethodRetryHandler(5, false));

            postMethod.setEntity(entity);
			HttpClient client = new DefaultHttpClient();
			this.response = client.execute(postMethod);
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                throw new RuntimeException("Non-success status code returned: "+response);
            }
            return response.getEntity().getContent();
		} catch (IOException e) {
			throw new RuntimeException("Error calculating checkcif.", e);
		}
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
