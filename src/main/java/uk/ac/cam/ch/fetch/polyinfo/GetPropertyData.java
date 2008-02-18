package uk.ac.cam.ch.fetch.polyinfo;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class GetPropertyData {

	private static final String USER_NAME = "doctornick";
	private static final String PASSWORD = "doctornick";


	HttpClient client;
	int max = 0;
	Document index;

	String ROOT_DIR = "e:/data/polyinfo-new";
	String INDEX_PATH="e:/data/polyinfo-new/polymers/polyinfo-poly-index.xml";

	public GetPropertyData() {
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

								Nodes propNds = polymer.query("./properties");
								Element propNode = null;
								if (propNds.size() > 0) {
									propNode = ((Element)propNds.get(0));
									String completed = propNode.getAttributeValue("completed");
									if ("true".equalsIgnoreCase(completed)) {
										continue;
									}
								} else {
									throw new CrystalEyeRuntimeException("no propNds");
								}

								String name = polymer.getAttributeValue("name");
								String mainPagePath = ROOT_DIR+File.separator+"polymers"+File.separator+className+File.separator+num+File.separator+name+File.separator+name+".props.html";
								Document doc = null;
								try {
									doc = new Builder().build(new BufferedReader(new FileReader(mainPagePath)));
								} catch (Exception e) {
									throw new CrystalEyeRuntimeException("problem", e);
								}
								int propNum = 0;

								Nodes propLinkNds = doc.query(".//x:a[contains(@href,'layout=property') or contains(@href,'layout=sample')] ", X_XHTML);
								if (propLinkNds.size() > 0) {
									for (int l = 0; l < propLinkNds.size(); l++) {
										propNum++;
										Element el = (Element)propLinkNds.get(l);
										String href = el.getAttributeValue("href");

										Pattern pattern = Pattern.compile("[=]([^\\&?]*)[\\&?]");
										Matcher matcher = pattern.matcher(href);

										String[] results = new String[5];
										int a = 0;
										while (matcher.find()) {
											results[a] = matcher.group(1);
											a++;
										}

										for (int s = 0; s < results.length; s++) {
											if (results[s] == null) {
												throw new CrystalEyeRuntimeException("did not find all POST variables from URL: "+href);
											}
										}

										PostMethod postMethod = new PostMethod("https://polymer.nims.go.jp/PoLyInfo/cgi-bin/ho-id-search.cgi");
										postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
												new DefaultHttpMethodRetryHandler(5, false));

										URLCodec uc = new URLCodec();
										try {
											NameValuePair[] data = { new NameValuePair("layout", uc.decode(results[0])),
													new NameValuePair("PID", uc.decode(results[1])),
													new NameValuePair("ID", uc.decode(results[2])),
													new NameValuePair("cond", uc.decode(results[3])),
													new NameValuePair("dispCond", ""),
													new NameValuePair("prop", uc.decode(results[4])),
													new NameValuePair("props", ""),
													new NameValuePair("block", uc.decode("1")) };
											postMethod.setRequestBody(data);
										} catch (DecoderException e) {
											throw new CrystalEyeRuntimeException("could not decode url.", e);
										}

										int statusCode;
										String html = "";
										try {
											statusCode = client.executeMethod(postMethod);
											if (statusCode != HttpStatus.SC_OK) {
												System.err.println("Method failed: "+postMethod.getStatusLine());
											}
											InputStream in = postMethod.getResponseBodyAsStream();
											html = stream2String(in);
										} catch (HttpException e) {
											e.printStackTrace();
										} catch (IOException e) {
											e.printStackTrace();
										} finally {
											postMethod.releaseConnection();
										}
										System.out.println(html);
										String propPath = mainPagePath.substring(0,mainPagePath.length()-11)+".prop_"+propNum+"_1.html";
										Document propDoc = IOUtils.parseHtmlWithTagsoup(html);
										File f = new File(propPath).getParentFile();
										if (!f.exists()) {
											f.mkdirs();
										}

										IOUtils.writeXML(propDoc, propPath);

										if (l == propLinkNds.size()-1) {
											propNode.getAttribute("completed").setValue("true");
										}
									}
								}
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
		GetPropertyData gt = new GetPropertyData();
		gt.fetch();
	}
}
