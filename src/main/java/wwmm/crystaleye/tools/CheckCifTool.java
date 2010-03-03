package wwmm.crystaleye.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Document;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

import wwmm.pubcrawler.BasicHttpClient;
import wwmm.pubcrawler.Utils;
import wwmm.pubcrawler.core.CrawlerHttpClient;

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
	private HttpPost httpPost;

	private static final Logger LOG = Logger.getLogger(CheckCifTool.class);

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
		String checkcifString = getCheckcifString(cifFile);
		Document xml = null;
		Builder builder = BasicHttpClient.getTagsoupBuilder();
		xml = Utils.parseXml(builder, new StringReader(checkcifString));
		return xml;
	}

	/**
	 * <p>
	 * Sends the provided CIF file off to the CheckCIF service.  The
	 * resulting CheckCIF HTML is returned in <code>String</code>.
	 * </p>
	 * 
	 * @param cifFile - a file containing a CIF.
	 * 
	 * @return InputStream containing the CheckCIF HTML.
	 */
	private String getCheckcifString(File cifFile) {
		InputStream in = null;
		try {
			httpPost = new HttpPost(POST_ENDPOINT);
			FileEntity cifEntity = new FileEntity(cifFile, "chemical/x-cif");
			StringEntity runtypeEntity = new StringEntity("runtype", "fullpublication");
			StringEntity uploadEntity = new StringEntity("UPLOAD", "Send CIF for checking");
			httpPost.setEntity(multipartEntity);
			return new CrawlerHttpClient().getPostResultString(httpPost);
		} catch (IOException e) {
			throw new RuntimeException("Error calculating checkcif.", e);
		} finally {
			IOUtils.closeQuietly(in);
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
