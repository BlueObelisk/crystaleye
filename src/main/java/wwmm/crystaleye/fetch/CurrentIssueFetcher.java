package wwmm.crystaleye.fetch;


import static wwmm.crystaleye.CrystalEyeConstants.ATOMPUB;
import static wwmm.crystaleye.CrystalEyeConstants.BONDLENGTHS;
import static wwmm.crystaleye.CrystalEyeConstants.CELLPARAMS;
import static wwmm.crystaleye.CrystalEyeConstants.CIF2CML;
import static wwmm.crystaleye.CrystalEyeConstants.CIF_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.CML2FOO;
import static wwmm.crystaleye.CrystalEyeConstants.CML2RDF;
import static wwmm.crystaleye.CrystalEyeConstants.DATE_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.DOILIST;
import static wwmm.crystaleye.CrystalEyeConstants.DOI_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.RSS;
import static wwmm.crystaleye.CrystalEyeConstants.SMILESLIST;
import static wwmm.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.apache.log4j.Logger;

import wwmm.crystaleye.CrystalEyeUtils;
import wwmm.crystaleye.IOUtils;
import wwmm.crystaleye.IssueDate;

public abstract class CurrentIssueFetcher extends Fetcher {
	
	private static final Logger LOG = Logger.getLogger(CurrentIssueFetcher.class);

	protected CurrentIssueFetcher(String publisherAbbreviation, File propertiesFile) {
		super(publisherAbbreviation, propertiesFile);
	}

	protected CurrentIssueFetcher(String publisherAbbreviation, String propertiesFile) {
		super(publisherAbbreviation, propertiesFile);
	}

	protected abstract IssueDate getCurrentIssueId(String journalAbbreviation);

	protected abstract void fetch(String issueWriteDir, String journalAbbreviation, String year, String issueNum);

	public void execute() {
		String[] journalAbbreviations = properties.getPublisherJournalAbbreviations(PUBLISHER_ABBREVIATION);
		for (String journalAbbreviation : journalAbbreviations) {
			LOG.info("Getting TOC of latest issue of "+PUBLISHER_ABBREVIATION.toUpperCase()+" journal "+journalAbbreviation.toUpperCase());
			IssueDate issueDate = getCurrentIssueId(journalAbbreviation);
			String year = issueDate.getYear();
			String issue = issueDate.getIssue();
			boolean alreadyGot = checkDownloads(journalAbbreviation, year, issue);
			String issueWriteDir = properties.getWriteDir()+File.separator+PUBLISHER_ABBREVIATION+File.separator+journalAbbreviation+File.separator+year+File.separator+issue;
			this.fetch(issueWriteDir, journalAbbreviation, year, issue);
			if (!alreadyGot) {
				updateLog(journalAbbreviation, year, issue);
			}
		}
	}

	protected boolean checkDownloads(String journalAbbreviation, String year, String issueNum) {
		String downloadLogPath = properties.getDownloadLogPath();
		boolean alreadyGot = false;
		Document doc = IOUtils.parseXmlFile(downloadLogPath);
		Nodes nodes = doc.query(".//journal[@abbreviation='"+journalAbbreviation+"']/year[@id='"+year+"']/issue[@id='"+issueNum+"']");
		if (nodes.size() > 0) {
			alreadyGot = true;
		}
		return alreadyGot;
	}

	protected void updateLog(String journalAbbreviation, String year, String issueNum) {
		String downloadLogPath = properties.getDownloadLogPath();
		Document doc = IOUtils.parseXmlFile(downloadLogPath);
		Element logEl = doc.getRootElement();
		Nodes publishers = logEl.query("./publisher[@abbreviation='"+PUBLISHER_ABBREVIATION+"']");
		if (publishers.size() == 1) {
			Element publisherEl = (Element)publishers.get(0);
			Nodes journals = publisherEl.query("./journal[@abbreviation='"+journalAbbreviation+"']");
			if (journals.size() == 1) {
				Element journalEl = (Element)journals.get(0);
				Nodes years = journalEl.query("./year[@id='"+year+"']");
				if (years.size() == 1) {
					Element yearEl = (Element)years.get(0);
					yearEl.appendChild(getNewIssueElement(issueNum));
				} else if (years.size() > 1) {
					throw new RuntimeException("Found more than one entry in the log for "+PUBLISHER_ABBREVIATION+"/"+journalAbbreviation+"/"+year+".  Cannot continue.");
				} else if (years.size() == 0) {
					Element yearEl = getNewYearElement(year);
					journalEl.appendChild(yearEl);
					yearEl.appendChild(getNewIssueElement(issueNum));
				}
			} else if (journals.size() > 1) {
				throw new RuntimeException("Found more than one entry in the log for "+PUBLISHER_ABBREVIATION+"/"+journalAbbreviation+".  Cannot continue.");
			} else if (journals.size() == 0) {
				Element journalEl = getNewJournalElement(journalAbbreviation);
				publisherEl.appendChild(journalEl);
				Element yearEl = getNewYearElement(year);
				journalEl.appendChild(yearEl);
				yearEl.appendChild(getNewIssueElement(issueNum));
			}
		} else if (publishers.size() > 1) {
			throw new RuntimeException("Found more than one entry in the log for "+PUBLISHER_ABBREVIATION+".  Cannot continue.");
		} else if (publishers.size() == 0) {
			Element publisherEl = getNewPublisherElement(PUBLISHER_ABBREVIATION);
			logEl.appendChild(publisherEl);
			Element journalEl = getNewJournalElement(journalAbbreviation);
			publisherEl.appendChild(journalEl);
			Element yearEl = getNewYearElement(year);
			journalEl.appendChild(yearEl);
			yearEl.appendChild(getNewIssueElement(issueNum));
		}

		IOUtils.writeXML(doc, downloadLogPath);
		LOG.info("Updated "+downloadLogPath+" by adding "+year+"-"+issueNum);
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

	protected void writeFiles(String issueWriteDir, String cifId, int suppNum, String cif, String doi) {
		String pathPrefix = issueWriteDir+File.separator+cifId+File.separator+cifId;
		LOG.info("Writing cif to: "+pathPrefix+"sup"+suppNum+CIF_MIME);
		IOUtils.writeText(cif, pathPrefix+"sup"+suppNum+CIF_MIME);
		if (doi != null) {
			IOUtils.writeText(doi, pathPrefix+DOI_MIME);
		}
		CrystalEyeUtils.writeDateStamp(pathPrefix+DATE_MIME);
	}
}
