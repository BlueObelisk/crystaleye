package uk.ac.cam.ch.fetch.polyinfo;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class PolyInfoPolymerFetcher {

	private static final String SITE_URL = "https://polymer.nims.go.jp";
	private static final String START_PAGE = SITE_URL+"/PoLyInfo/cgi-bin/p-easy-ptable.cgi?vtype=range&H=ALL&V=ptype";

	private static final String USER_NAME = "doctornick";
	private static final String PASSWORD = "doctornick";

	HttpClient client;
	int max = 3000;
	Document indexDoc;

	String ROOT_DIR = "e:/data/polyinfo";
	String INDEX_PATH="e:/data/polyinfo/polyinfo-index.xml";

	public PolyInfoPolymerFetcher() {
		setHttpClient();
		readIndex();
	}

	public static void main(String[] args) {
		PolyInfoPolymerFetcher poly = new PolyInfoPolymerFetcher();
		poly.fetch();
	}

	public void readIndex() {
		try {
			this.indexDoc = new Builder().build(new BufferedReader(new FileReader(INDEX_PATH)));
		} catch (Exception e) {
			throw new CrystalEyeRuntimeException("Could not read index: "+INDEX_PATH, e);
		}
	}

	public void fetch()	{
		Element root = indexDoc.getRootElement();

		String html = getWebPage(START_PAGE);
		Document doc = IOUtils.parseHtmlWithTagsoup(html);
		Nodes classNameNds = doc.query(".//x:a[contains(@href,'ALL')]", X_XHTML);
		for (int i = 0; i < classNameNds.size(); i++) {
			Element el = ((Element)classNameNds.get(i));
			String href = el.getAttributeValue("href");
			String className = el.getValue().trim();

			// start update index
			Element classEl = null;
			Nodes resultNdsClass = indexDoc.query(".//class[@name=\""+className+"\"]");
			if (resultNdsClass.size() > 0) {
				classEl = ((Element)resultNdsClass.get(0));
				String value = classEl.getAttributeValue("completed");
				if ("true".equalsIgnoreCase(value)) {
					continue;
				}
			} else {
				classEl = new Element("class");
				classEl.addAttribute(new Attribute("name", className));
				classEl.addAttribute(new Attribute("completed", "false"));
				root.appendChild(classEl);
				writeIndex();
			}
			// end update index

			String classUrl = SITE_URL+href;
			html = getWebPage(classUrl);
			sleep(max);
			doc = IOUtils.parseHtmlWithTagsoup(html);
			System.out.println(doc.toXML());
			Nodes carbonNumNds = doc.query(".//x:a[contains(@href,'ALL') and contains(@href,'V=pi')]", X_XHTML);
			for (int j = 0; j < carbonNumNds.size(); j++) {
				el = ((Element)carbonNumNds.get(j));
				href = el.getAttributeValue("href");
				String carbonNum = el.getValue().trim();

				// start update index
				Element carbonEl = null;
				Nodes resultNdsCarbon = indexDoc.query(".//class[@name=\""+className+"\"]/carbons[@number=\""+carbonNum+"\"]");
				if (resultNdsCarbon.size() > 0) {
					carbonEl = ((Element)resultNdsCarbon.get(0));
					String value = carbonEl.getAttributeValue("completed");
					if ("true".equalsIgnoreCase(value)) {
						continue;
					}
				} else {
					carbonEl = new Element("carbons");
					carbonEl.addAttribute(new Attribute("number", carbonNum));
					carbonEl.addAttribute(new Attribute("completed", "false"));
					classEl.appendChild(carbonEl);
					writeIndex();
				}
				// end update index

				String url = SITE_URL+href;
				System.out.println(url);
				html = getWebPage(url);
				sleep(max);
				doc = IOUtils.parseHtmlWithTagsoup(html);

				Nodes polymerNds = doc.query(".//x:a[contains(@href,'PID')]", X_XHTML);
				for (int k = 0; k < polymerNds.size(); k++) {
					el = ((Element)polymerNds.get(k));
					href = el.getAttributeValue("href");
					String pid = href.substring(href.length()-7);

					// check haven't already got before downloading
					String xpath = ".//class[@name=\""+className+"\"]/carbons[@number=\""+carbonNum+"\"]/polymer[@name=\""+pid+"\"]";
					Nodes resultNdsPoly = indexDoc.query(xpath);
					if (resultNdsPoly.size() > 0) {
						System.out.println("Already got polymer: "+pid);
						continue;
					}

					url = SITE_URL+href;
					html = getWebPage(url);
					sleep(max);
					doc = IOUtils.parseHtmlWithTagsoup(html);

					String filename = ROOT_DIR+File.separator+className+File.separator+carbonNum+File.separator+pid+File.separator+pid+".html";
					IOUtils.writeXML(doc, filename);

					// update index
					Element polymerEl = new Element("polymer");
					polymerEl.addAttribute(new Attribute("name", pid));
					carbonEl.appendChild(polymerEl);
					writeIndex();


					// set this carbon elements 'completed' attribute to 
					// true if this is the last node in the set
					if (k+1 == polymerNds.size()) {
						Nodes nodes = indexDoc.query(".//class[@name=\""+className+"\"]/carbons[@number=\""+carbonNum+"\"]");
						((Element)nodes.get(0)).getAttribute("completed").setValue("true");
						writeIndex();
					}
				}
				if (j+1 == carbonNumNds.size()) {
					Nodes nodes = indexDoc.query(".//class[@name=\""+className+"\"]");
					((Element)nodes.get(0)).getAttribute("completed").setValue("true");
					writeIndex();
				}
			}
		}
	}

	private void writeIndex() {
		IOUtils.writePrettyXML(indexDoc, this.INDEX_PATH);
	}

	public HttpClient setHttpClient() {
		client = new HttpClient();
		Credentials credentials = new UsernamePasswordCredentials(USER_NAME , PASSWORD);
		client.getState().setCredentials(AuthScope.ANY, credentials);
		return client;
	}

	public String getWebPage(String url) {
		GetMethod method = new GetMethod(url);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
				new DefaultHttpMethodRetryHandler(5, false));
		int statusCode;
		String html = "";
		try {
			statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: "+method.getStatusLine());
			}
			InputStream in = method.getResponseBodyAsStream();
			html = stream2String(in);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
		return html;
	}

	public static String stream2String (InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	private void sleep(int maxTime) {
		try {
			Thread.sleep(((int)(maxTime*Math.random())));
		} catch (InterruptedException e) {
			System.err.println("Sleep interrupted.");
		}
	}
}
