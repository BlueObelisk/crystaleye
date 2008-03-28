package ned24.sandbox.crystaleye;

import static crystaleye.CrystalEyeConstants.XHTML_NS;
import static crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import crystaleye.IOUtils;
import crystaleye.Utils;

public class AddGolemRdfLinks {

	public static void main(String[] args) {
		//String startPath = "e:/crystaleye-test2/summary";
		String startPath = "/var/crystaleye/www/crystaleye/summary/";

		File startFile = new File(startPath);

		for (File pubFile : startFile.listFiles()) {
			for (File journalFile : pubFile.listFiles()) {
				for (File yearFile : journalFile.listFiles()) {
					for (File issueFile : yearFile.listFiles()) {
						for (File dataFile : issueFile.listFiles()) {
							if (dataFile.getName().equals("data")) {
								for (File articleFile : dataFile.listFiles()) {
									for (File structureFile : articleFile.listFiles()) {
										if (structureFile.isDirectory()) {
											for (File summaryFile : structureFile.listFiles()) {
												if (summaryFile.getName().endsWith(".cif.summary.html")) {
													try {
														String summaryPath = summaryFile.getAbsolutePath();
														String html = Utils.file2String(summaryPath);

														Pattern p = Pattern.compile("<< Table");
														Matcher m = p.matcher(html);
														html = m.replaceAll("&lt;&lt; Table");
														
														Pattern p2 = Pattern.compile("<wbr>");
														Matcher m2 = p2.matcher(html);
														html = m2.replaceAll("<wbr />");
														
														IOUtils.writeText(html, summaryPath);

														System.out.println(summaryFile.getAbsolutePath());
														Document doc = IOUtils.parseXmlFile(summaryPath);

														Nodes headProfiles = doc.query("./x:head/@profile", X_XHTML);
														if (headProfiles.size() > 0) {
															for (int i = 0; i < headProfiles.size(); i++) {
																headProfiles.get(i).detach();
															}
														}
														Nodes links = doc.query(".//x:link[@rel='meta']", X_XHTML);
														if (links.size() > 0) {
															for (int i = 0; i < links.size(); i++) {
																links.get(i).detach();
															}
														}													

														Nodes headNodes = doc.query("./x:html/x:head", X_XHTML);
														Element head = (Element)headNodes.get(0);
														head.addAttribute(new Attribute("profile", "http://purl.org/net/uriprofile/"));
														Element link = new Element("link", XHTML_NS);
														head.appendChild(link);
														link.addAttribute(new Attribute("rel", "meta"));

														String cmlPath = summaryPath.replaceAll("\\.cif\\.summary\\.html", ".complete.cml.xml");
														int idx = cmlPath.indexOf(File.separator+"summary"+File.separator);
														String ss = cmlPath.substring(idx+9);
														ss = ss.replaceAll("\\\\", "/");
														String urlStr = "http%3A//wwmm.ch.cam.ac.uk/crystaleye/summary/"+ss;

														String golemUrl = "http://www.cmlcomp.org/golem/demo/cedescribe/"+urlStr;
														System.out
														.println(golemUrl);
														link.addAttribute(new Attribute("href", golemUrl));
														IOUtils.writePrettyXML(doc, summaryPath);	
														
													} catch (Exception e) {
														System.out
														.println("Problem with: "+summaryFile.getAbsolutePath());
														e.printStackTrace();
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
