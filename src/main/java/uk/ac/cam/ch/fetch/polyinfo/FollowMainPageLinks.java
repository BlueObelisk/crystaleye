package uk.ac.cam.ch.fetch.polyinfo;

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

public class FollowMainPageLinks {

	private static final String USER_NAME = "doctornick";
	private static final String PASSWORD = "doctornick";

	HttpClient client;
	int max = 50;
	Document index;

	String ROOT_DIR = "e:/data/polyinfo-new";
	String INDEX_PATH="e:/data/polyinfo-new/polyinfo-index.xml";

	public FollowMainPageLinks() {
		setHttpClient();
		readIndex();
	}

	public void readIndex() {
		try {
			index = new Builder().build(new BufferedReader(new FileReader(INDEX_PATH)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fetch() {
		Nodes classes = index.query(".//class");
		if (classes.size() > 0) {
			for (int i = 0; i < classes.size(); i++) {
				Element classe = ((Element)classes.get(i));
				String className = classe.getAttributeValue("name");
				Nodes carbonNums = classe.query("./carbons");
				if (carbonNums.size() > 0) {
					for (int j = 0; j < carbonNums.size(); j++) {
						Element carbonNum = ((Element)carbonNums.get(j));
						String num = carbonNum.getAttributeValue("number");
						Nodes polymers = carbonNum.query("./polymer");
						if (polymers.size() > 0) {
							for (int k = 0; k < polymers.size(); k++) {
								Element polymer = ((Element)polymers.get(k));

								Nodes mons = polymer.query("./candidateMonomers[@got='true']");
								Nodes props = polymer.query("./properties[@got='true']");
								if (mons.size() > 0 && props.size() > 0) {
									continue;
								}

								String pid = polymer.getAttributeValue("name");

								String candidateMonomersUrl = "https://polymer.nims.go.jp/PoLyInfo/cgi-bin/zpath-search.cgi?PID="+pid;
								String html = getWebPage(candidateMonomersUrl);
								Document doc = IOUtils.parseHtmlWithTagsoup(html);
								String filename = ROOT_DIR+File.separator+className+File.separator+num+File.separator+pid+File.separator+pid+".mons.html";
								File f = new File(filename).getParentFile();
								if (!f.exists()) {
									f.mkdirs();
								}
								System.out.println(filename);
								IOUtils.writeXML(doc, filename);

								String propertiesUrl = "https://polymer.nims.go.jp/PoLyInfo/cgi-bin/p-advanced-search.cgi?p-cu-name="+pid+"&p-cu-name-string=EXACT&from=15";
								String propHtml = getWebPage(propertiesUrl);
								Document propDoc = IOUtils.parseHtmlWithTagsoup(propHtml);
								String propFilename = ROOT_DIR+File.separator+className+File.separator+num+File.separator+pid+File.separator+pid+".props.html";
								f = new File(propFilename).getParentFile();
								if (!f.exists()) {
									f.mkdirs();
								}
								System.out.println(propFilename);
								IOUtils.writeXML(propDoc, propFilename);

								Element mon = new Element("candidateMonomers");
								mon.addAttribute(new Attribute("got", "true"));
								polymer.appendChild(mon);

								Element prop = new Element("properties");
								prop.addAttribute(new Attribute("got", "true"));
								polymer.appendChild(prop);
							}
							writeIndex();
						}
					}
				} else {
					throw new CrystalEyeRuntimeException("could not find any classes");
				}
			}
		} else {
			throw new CrystalEyeRuntimeException("could not find any classes");
		}

	}

	private void writeIndex() {
		IOUtils.writePrettyXML(index, this.INDEX_PATH);
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

	public static void main(String[] args) {
		FollowMainPageLinks gt = new FollowMainPageLinks();
		gt.fetch();
	}
}
