package wwmm.crystaleye.harvester;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import uk.ac.cam.ch.atomxom.model.AtomEntry;
import uk.ac.cam.ch.atomxom.model.AtomLink;
import wwmm.crystaleye.util.Utils;

public class ECrystalsFeedEntryHandler implements EntryHandler {

	private static final Logger LOG = Logger.getLogger(ECrystalsFeedEntryHandler.class);

	private File dataDirectory;

	public ECrystalsFeedEntryHandler(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public boolean handle(AtomEntry entry) {
		String entryId = entry.getId();
		int idx = entryId.lastIndexOf("/");
		String structureId = entryId.substring(idx+1);
		File entryDir = new File(dataDirectory, structureId);
		List<AtomLink> atomLinks = entry.getLinks();
		String cifUrl = "";
		for (AtomLink link : atomLinks) {
			String href = link.getHref();
			if (href.endsWith(".cif")) {
				cifUrl = href;
			}
		}
		if ("".equals(cifUrl)) {
			return true;
		}
		DefaultHttpClient client = new DefaultHttpClient();
		String cifString = "";
		try {
			HttpUriRequest request = new HttpGet(cifUrl);
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			HttpEntity entity = response.getEntity();
			if (status.getStatusCode() != HttpStatus.SC_OK) {
				LOG.warn("Moiety resource not found ("+cifUrl+"). "+status.getStatusCode()+": "+status.getReasonPhrase());
				return false;
			}
			InputStream in = entity.getContent();
			cifString = IOUtils.toString(in);
		} catch (Exception e) {
			LOG.warn("Problem trying to get moiety CML from: "+cifUrl+" - "+e.getMessage());
			return false;
		}
		entryDir.mkdir();
		try {
			FileUtils.writeStringToFile(new File(entryDir, structureId+".cif"), cifString);
			Utils.writeXML(new File(entryDir, "entry.xml"), entry);
			Utils.writeText(new File(entryDir, structureId+".doi"), "10.3737/ecrystals.chem.soton.ac.uk/"+structureId);
			Utils.writeDateStamp(entryDir+"/"+structureId+".date");
		} catch (Exception e) {
			LOG.warn("Problem writing entry data: "+e.getMessage());
			return false;
		}

		return true;
	}

}
