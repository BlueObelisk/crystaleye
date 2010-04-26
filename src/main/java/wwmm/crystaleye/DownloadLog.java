package wwmm.crystaleye;

import static wwmm.crystaleye.CrystalEyeConstants.ATOMPUB;
import static wwmm.crystaleye.CrystalEyeConstants.BONDLENGTHS;
import static wwmm.crystaleye.CrystalEyeConstants.CELLPARAMS;
import static wwmm.crystaleye.CrystalEyeConstants.CIF2CML;
import static wwmm.crystaleye.CrystalEyeConstants.CML2FOO;
import static wwmm.crystaleye.CrystalEyeConstants.CML2RDF;
import static wwmm.crystaleye.CrystalEyeConstants.DOILIST;
import static wwmm.crystaleye.CrystalEyeConstants.RSS;
import static wwmm.crystaleye.CrystalEyeConstants.SMILESLIST;
import static wwmm.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.log4j.Logger;

import wwmm.crystaleye.util.Utils;

public class DownloadLog {

	private static final Logger LOG = Logger.getLogger(DownloadLog.class);

	private File logFile;
	private Document logContents;

	public DownloadLog(File file) {
		this.logFile = file;
		if (!logFile.exists()) {
			String logInitialContents = "<log></log>";
			Utils.writeText(logFile, logInitialContents);
		}
		this.logContents = Utils.parseXml(file);
	}
	
	public DownloadLog(String filepath) {
		this(new File(filepath));
	}

	public void updateLog(String publisherAbbreviation, String journalAbbreviation, String year, String issueNum) {
		Element logEl = logContents.getRootElement();
		// check issue isn't already in log, if it is then can just stop here
		Nodes issueNodes = logEl.query("./publisher[@abbreviation='"+publisherAbbreviation+"']/journal[@abbreviation='"+journalAbbreviation+"']/year[@id='"+year+"']/issue[@id='"+issueNum+"']");
		if(issueNodes.size() > 0) {
			return;
		}
		
		Nodes publishers = logEl.query("./publisher[@abbreviation='"+publisherAbbreviation+"']");
		if (publishers.size() == 1) {
			Element publisherEl = (Element)publishers.get(0);
			Nodes journals = publisherEl.query("./journal[@abbreviation='"+journalAbbreviation+"']");
			if (journals.size() == 1) {
				Element journalEl = (Element)journals.get(0);
				Nodes years = journalEl.query("./year[@id='"+year+"']");
				if (years.size() == 1) {
					Element yearEl = (Element)years.get(0);
					yearEl.appendChild(getNewIssueElement(issueNum));
				} else if (years.size() == 0) {
					Element yearEl = getNewYearElement(year);
					journalEl.appendChild(yearEl);
					yearEl.appendChild(getNewIssueElement(issueNum));
				}
			} else if (journals.size() == 0) {
				Element journalEl = getNewJournalElement(journalAbbreviation);
				publisherEl.appendChild(journalEl);
				Element yearEl = getNewYearElement(year);
				journalEl.appendChild(yearEl);
				yearEl.appendChild(getNewIssueElement(issueNum));
			}
		} else if (publishers.size() == 0) {
			Element publisherEl = getNewPublisherElement(publisherAbbreviation);
			logEl.appendChild(publisherEl);
			Element journalEl = getNewJournalElement(journalAbbreviation);
			publisherEl.appendChild(journalEl);
			Element yearEl = getNewYearElement(year);
			journalEl.appendChild(yearEl);
			yearEl.appendChild(getNewIssueElement(issueNum));
		}

		Utils.writeXML(logFile, logContents);
		LOG.info("Updated "+logFile+" by adding "+year+"-"+issueNum);
	}

	private Element getNewIssueElement(String issueNum) {
		Element issue = new Element("issue");
		Attribute issueId = new Attribute("id", issueNum);
		issue.addAttribute(issueId);
		Element cif2Cml = new Element(CIF2CML);
		issue.appendChild(cif2Cml);
		cif2Cml.addAttribute(new Attribute("value", "false"));			
		Element cml2Foo = new Element(CML2FOO);
		cml2Foo.addAttribute(new Attribute("value", "false"));
		issue.appendChild(cml2Foo);	
		Element cml2rdfElement = new Element(CML2RDF);
		cml2rdfElement.addAttribute(new Attribute("value", "false"));
		issue.appendChild(cml2rdfElement);	
		Element webpage = new Element(WEBPAGE);
		webpage.addAttribute(new Attribute("value", "false"));
		issue.appendChild(webpage);
		Element doilist = new Element(DOILIST);
		doilist.addAttribute(new Attribute("value", "false"));
		issue.appendChild(doilist);
		Element bondLengths = new Element(BONDLENGTHS);
		bondLengths.addAttribute(new Attribute("value", "false"));
		issue.appendChild(bondLengths);
		Element cellParams = new Element(CELLPARAMS);
		cellParams.addAttribute(new Attribute("value", "false"));
		issue.appendChild(cellParams);
		Element smiles = new Element(SMILESLIST);
		smiles.addAttribute(new Attribute("value", "false"));
		issue.appendChild(smiles);
		Element rss = new Element(RSS);
		rss.addAttribute(new Attribute("value", "false"));
		issue.appendChild(rss);
		Element atompub = new Element(ATOMPUB);
		atompub.addAttribute(new Attribute("value", "false"));
		issue.appendChild(atompub);

		return issue;
	}

	private Element getNewYearElement(String year) {
		Element yearEl = new Element("year");
		yearEl.addAttribute(new Attribute("id", year));	
		return yearEl;
	}

	private Element getNewJournalElement(String journalAbbreviation) {
		Element journalEl = new Element("journal");
		journalEl.addAttribute(new Attribute("abbreviation", journalAbbreviation));
		return journalEl;
	}

	private Element getNewPublisherElement(String publisherAbbreviation) {
		Element publisherEl = new Element("publisher");
		publisherEl.addAttribute(new Attribute("abbreviation", publisherAbbreviation));
		return publisherEl;
	}

}
