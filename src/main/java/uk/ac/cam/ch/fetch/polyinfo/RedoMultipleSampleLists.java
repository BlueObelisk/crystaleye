package uk.ac.cam.ch.fetch.polyinfo;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

public class RedoMultipleSampleLists {

	private static final String USER_NAME = "doctornick";
	private static final String PASSWORD = "doctornick";

	private static final String SITE_URL = "https://polymer.nims.go.jp";

	HttpClient client;
	int max = 0;
	Document index;

	String ROOT_DIR = "e:/data/polyinfo-new";
	String INDEX_PATH="e:/data/polyinfo-new/polymers/polyinfo-poly-index.xml";

	public RedoMultipleSampleLists() {
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

								Nodes propNds = polymer.query("./samples");
								Element propNode = null;
								if (propNds.size() > 0) {
									propNode = ((Element)propNds.get(0));
									String completed = propNode.getAttributeValue("completed");
									if ("true".equalsIgnoreCase(completed)) {
										String name = polymer.getAttributeValue("name");
										String folderPath = ROOT_DIR+File.separator+"polymers"+File.separator+className+File.separator+num+File.separator+name;
										File[] fileList = new File(folderPath).listFiles();
										List<String> pathList = new ArrayList<String>();
										for (File file : fileList) {
											String path = file.getAbsolutePath();
											if (path.contains("samplelist")) {
												pathList.add(path);
											}
										}
										int listNum = 0;
										if (pathList.size() > 1) {
											System.out.println("---------------MORE THAN ONE SAMPLE LIST--------------");
											for (String str : pathList) {
												listNum++;
												Document doc = null;
												try {
													doc = new Builder().build(new BufferedReader(new FileReader(str)));
													Nodes sampleNodes = doc.query(".//x:a[contains(text(),'SampleID')]", X_XHTML);
													if (sampleNodes.size() > 0) {
														for (int l = 0; l < sampleNodes.size(); l++) {
															int sampNum = l+1;
															Element el = (Element)sampleNodes.get(l);
															String href = el.getAttributeValue("href");
															String sampleUrl = SITE_URL+href;
															String html = getWebPage(sampleUrl);
															Document newDoc = IOUtils.parseHtmlWithTagsoup(html);

															String filename = ROOT_DIR+File.separator+"polymers"+File.separator+className+File.separator+num+File.separator+name+File.separator+name+".sample_"+listNum+"_"+sampNum+".html";
															System.out.println(filename);
															IOUtils.writeXML(newDoc, filename);
														}
													}
												} catch (Exception e) {
													throw new CrystalEyeRuntimeException("problem", e);
												}
											}
										} else {
											System.out.println("only 1 sample list.");
										}
									}
								}
							}
							//writeIndex();
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
		RedoMultipleSampleLists gt = new RedoMultipleSampleLists();
		gt.fetch();
	}
}
