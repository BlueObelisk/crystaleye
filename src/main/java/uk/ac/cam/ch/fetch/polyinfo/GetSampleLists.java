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
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.IOUtils;

public class GetSampleLists {

	private static final String USER_NAME = "doctornick";
	private static final String PASSWORD = "doctornick";

	HttpClient client;
	int max = 0;
	Document index;

	String ROOT_DIR = "e:/data/polyinfo-new";
	String INDEX_PATH="e:/data/polyinfo-new/polymers/polyinfo-poly-index.xml";

	public GetSampleLists() {
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
										continue;
									}
								}

								String name = polymer.getAttributeValue("name");
								String mainPagePath = ROOT_DIR+File.separator+"polymers"+File.separator+className+File.separator+num+File.separator+name+File.separator+name+".props.html";
								Document doc = null;
								try {
									doc = new Builder().build(new BufferedReader(new FileReader(mainPagePath)));
								} catch (Exception e) {
									throw new CrystalEyeRuntimeException("problem", e);
								}
								int sampleNum = 0;

								Nodes formNodes = doc.query(".//x:form[contains(@action,'ho-id-search')]", X_XHTML);
								if (formNodes.size() > 0) {
									Element el = (Element)formNodes.get(0);
									Nodes inputNodes = el.query("./x:input[@type='hidden']", X_XHTML);
									if (inputNodes.size() >= 6 ) {
										sampleNum++;
										PostMethod postMethod = new PostMethod("https://polymer.nims.go.jp/PoLyInfo/cgi-bin/ho-id-search.cgi");
										postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
												new DefaultHttpMethodRetryHandler(5, false));

										NameValuePair[] data = { new NameValuePair("layout", ((Element)inputNodes.get(0)).getAttributeValue("value")),
												new NameValuePair("PID", ((Element)inputNodes.get(1)).getAttributeValue("value")),
												new NameValuePair("ID", ((Element)inputNodes.get(2)).getAttributeValue("value")),
												new NameValuePair("cond", ((Element)inputNodes.get(3)).getAttributeValue("value")),
												new NameValuePair("dispCond", ""),
												new NameValuePair("props", ""),
												new NameValuePair("block", "1") };
										postMethod.setRequestBody(data);

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
										String propPath = mainPagePath.substring(0,mainPagePath.length()-11)+".samplelist_"+sampleNum+".html";
										Document propDoc = IOUtils.parseHtmlWithTagsoup(html);
										File f = new File(propPath).getParentFile();
										if (!f.exists()) {
											f.mkdirs();
										}

										IOUtils.writeXML(propDoc, propPath);

										Nodes formNds = propDoc.query(".//x:form[contains(@action,'ho-id-search')]", X_XHTML);
										if (formNds.size() > 0) {
											for (int h = 0; h < formNds.size()/2; h++	) {
												Element ele = (Element)formNds.get(h);
												Nodes inputNds = ele.query("./x:input[@type='hidden']", X_XHTML);
												if (inputNds.size() >= 6 ) {
													sampleNum++;
													PostMethod postMethod2 = new PostMethod("https://polymer.nims.go.jp/PoLyInfo/cgi-bin/ho-id-search.cgi");
													postMethod2.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
															new DefaultHttpMethodRetryHandler(5, false));

													NameValuePair[] nvs = { new NameValuePair("layout", ((Element)inputNds.get(0)).getAttributeValue("value")),
															new NameValuePair("PID", ((Element)inputNds.get(1)).getAttributeValue("value")),
															new NameValuePair("ID", ((Element)inputNds.get(2)).getAttributeValue("value")),
															new NameValuePair("cond", ((Element)inputNds.get(3)).getAttributeValue("value")),
															new NameValuePair("dispCond", ""),
															new NameValuePair("props", ""),
															new NameValuePair("block", ((Element)inputNds.get(7)).getAttributeValue("value")) };
													postMethod2.setRequestBody(nvs);

													String newhtml = "";
													try {
														statusCode = client.executeMethod(postMethod2);
														if (statusCode != HttpStatus.SC_OK) {
															System.err.println("Method failed: "+postMethod2.getStatusLine());
														}
														InputStream in = postMethod2.getResponseBodyAsStream();
														newhtml = stream2String(in);
													} catch (HttpException e) {
														e.printStackTrace();
													} catch (IOException e) {
														e.printStackTrace();
													} finally {
														postMethod2.releaseConnection();
													}
													System.out.println(newhtml);
													System.out
													.println("and the block was: "+((Element)inputNds.get(7)).getAttributeValue("value"));
													String newPath = mainPagePath.substring(0,mainPagePath.length()-11)+".samplelist_"+sampleNum+".html";
													Document newDoc = IOUtils.parseHtmlWithTagsoup(newhtml);
													f = new File(propPath).getParentFile();
													if (!f.exists()) {
														f.mkdirs();
													}

													IOUtils.writeXML(newDoc, newPath);
												}
											}
										}
									} else {
										throw new CrystalEyeRuntimeException("Found a form, but cannot find all the input values");
									}
								}
								Element sampleEl = new Element("samples");
								sampleEl.addAttribute(new Attribute("completed", "true"));
								polymer.appendChild(sampleEl);
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
		GetSampleLists gt = new GetSampleLists();
		gt.fetch();
	}
}
