package uk.ac.cam.ch.crystaleye.fetch;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.CIF_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.DATE_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.DOI_MIME;
import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.TITLE_MIME;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import uk.ac.cam.ch.crystaleye.CrystalEyeUtils;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.IssueDate;

public abstract class CurrentIssueFetcher extends JournalFetcher {

	File logfile;

	private final static Logger LOG = Logger
			.getLogger(CurrentIssueFetcher.class);

	protected abstract IssueDate getCurrentIssueId();

	protected abstract void fetch(File issueWriteDir, String year,
			String issueNum) throws IOException;

	public void fetchAll() throws IOException {
		LOG.info("Getting TOC of latest issue of " + publisherAbbr
				+ " journal " + journalAbbr);
		IssueDate issueDate = getCurrentIssueId();
		String year = issueDate.getYear();
		String issue = issueDate.getIssue();
		String issueCode = createIssueCode(year, issue);
		boolean alreadyGot = checkDownloads(issueCode);
		if (alreadyGot) {
			LOG.info("Already got this issue <" + issueCode
					+ ">");
		} else {
			File issueWriteDir = new File(downloadDir + File.separator
					+ publisherAbbr + File.separator + journalAbbr
					+ File.separator + year + File.separator + issue);

			this.fetch(issueWriteDir, year, issue);
			updateLog(issueCode);
		}
	}

	private String createIssueCode(String year, String issue) {
		return publisherAbbr + "_" + journalAbbr + "_" + year + "_" + issue;
	}

	protected boolean checkDownloads(String issueCode) throws IOException {
		for (LineIterator li = FileUtils.lineIterator(logfile); li.hasNext();) {
			if (issueCode.equals(li.nextLine())) {
				return true;
			}
		}
		return false;
	}

	protected void updateLog(String issueCode) {
		IOUtils.appendToFile(logfile, issueCode);
		LOG.info("Updated " + logfile + " with " + issueCode);
	}

	protected void writeFiles(File issueWriteDir, String cifId, int suppNum,
			URL cif, String doi, String title) throws IOException {
		File cifDir = new File(issueWriteDir + File.separator + cifId);
		String pathPrefix = cifId + "sup" + suppNum;
		File cifFile = new File(cifDir, pathPrefix + CIF_MIME);
		File tmpcif = null;
		File doiFile = new File(cifDir, pathPrefix + DOI_MIME);
		try {
			tmpcif = File.createTempFile("crystaleye", ".cif");
			LOG.debug("Downloading cif to: " + tmpcif);
			FileUtils.copyURLToFile(cif, tmpcif);
			LOG.debug("Moving cif to " + cifFile);
			if (!tmpcif.renameTo(cifFile)) {
				FileUtils.copyFile(tmpcif, cifFile);
				tmpcif.delete();
			}
			if (doi != null) {
				IOUtils.writeText(doi, doiFile.getCanonicalPath());
			}
			if (title != null) {
				IOUtils.writeText(title, pathPrefix+TITLE_MIME);
			}
			CrystalEyeUtils.writeDateStamp(pathPrefix + DATE_MIME);
		} finally {
			if (tmpcif != null && tmpcif.exists()) {
				tmpcif.delete();
			}
		}
	}

	public File getLogfile() {
		return logfile;
	}

	public void setLogfile(File logfile) {
		this.logfile = logfile;
	}
}