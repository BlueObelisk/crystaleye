package uk.ac.cam.ch.crystaleye.fetch;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.ATOMPUB;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.BONDLENGTHS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CELLPARAMS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CIF2CML;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CIF_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CML2FOO;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.DATE_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.DOILIST;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.DOI_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.RSS;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.SMILESLIST;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.WEBPAGE;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.CrystalEyeUtils;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.IssueDate;

public abstract class CurrentIssueFetcher extends Fetcher {

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
			System.out.println("Getting TOC of latest issue of "+PUBLISHER_ABBREVIATION.toUpperCase()+" journal "+journalAbbreviation.toUpperCase());
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
					throw new CrystalEyeRuntimeException("Found more than one entry in the log for "+PUBLISHER_ABBREVIATION+"/"+journalAbbreviation+"/"+year+".  Cannot continue.");
				} else if (years.size() == 0) {
					Element yearEl = getNewYearElement(year);
					journalEl.appendChild(yearEl);
					yearEl.appendChild(getNewIssueElement(issueNum));
				}
			} else if (journals.size() > 1) {
				throw new CrystalEyeRuntimeException("Found more than one entry in the log for "+PUBLISHER_ABBREVIATION+"/"+journalAbbreviation+".  Cannot continue.");
			} else if (journals.size() == 0) {
				Element journalEl = getNewJournalElement(journalAbbreviation);
				publisherEl.appendChild(journalEl);
				Element yearEl = getNewYearElement(year);
				journalEl.appendChild(yearEl);
				yearEl.appendChild(getNewIssueElement(issueNum));
			}
		} else if (publishers.size() > 1) {
			throw new CrystalEyeRuntimeException("Found more than one entry in the log for "+PUBLISHER_ABBREVIATION+".  Cannot continue.");
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
		System.out.println("Updated "+downloadLogPath+" by adding "+year+"-"+issueNum);
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
		System.out.println("Writing cif to: "+pathPrefix+"sup"+suppNum+CIF_MIME);
		IOUtils.writeText(cif, pathPrefix+"sup"+suppNum+CIF_MIME);
		if (doi != null) {
			IOUtils.writeText(doi, pathPrefix+DOI_MIME);
		}
		CrystalEyeUtils.writeDateStamp(pathPrefix+DATE_MIME);
	}
}
