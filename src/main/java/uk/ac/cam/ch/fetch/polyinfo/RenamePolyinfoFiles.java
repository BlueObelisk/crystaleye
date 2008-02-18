package uk.ac.cam.ch.fetch.polyinfo;

import static uk.ac.cam.ch.crystaleye.CrystalEyeConstants.X_XHTML;

import java.io.File;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import uk.ac.cam.ch.crystaleye.CrystalEyeRuntimeException;
import uk.ac.cam.ch.crystaleye.IOUtils;
import uk.ac.cam.ch.crystaleye.Utils;

public class RenamePolyinfoFiles {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = "e:\\data\\polyinfo";
		String newPath = "e:\\data\\polyinfo-new";
		File dir = new File(path);
		
		File[] classes = dir.listFiles();
		for (File classe : classes) {
			String className = classe.getName();
			if (classe.getAbsolutePath().endsWith(".xml")) {
				continue;
			}
			File[] carbons = classe.listFiles();
			for (File carbon : carbons) {
				String carbonNum = carbon.getName();
				File[] polys = carbon.listFiles();
				for (File poly : polys) {							    
					File[] polymers = poly.listFiles();
					for (File polymer : polymers) {
						if (!polymer.getAbsolutePath().endsWith(".html")) {
							continue;
						}
						if (polymer.getAbsolutePath().contains("poly{oxydecane-1,10-diyloxy(2,6-dimethoxy-1,4-phenylene)[(E)-ethene-1,2-diyl]-1,4-phenylene[(E)-ethene-1,2-diyl](3,5-dimetho")) {
							continue;
						}
						// get polyinfo ID
						Document doc = IOUtils.parseHtmlWithTagsoup(Utils.file2String(polymer.getAbsolutePath()));
						Nodes idNodes = doc.query(".//x:dd[x:strong[contains(text(),'PID')]]", X_XHTML);
						String pid = "";
						if (idNodes.size() > 0) {
							pid = ((Element)idNodes.get(0)).getValue().substring(5).trim();
						} else {
							throw new CrystalEyeRuntimeException("couldn't find the PID in: "+polymer.getAbsolutePath());
						}
						
						// Destination directory
						String destPath = newPath+File.separator+className+File.separator+carbonNum+File.separator+pid+File.separator+pid+".html";
						System.out.println(destPath);
					    File destFile = new File(destPath);
					    
					    if (!destFile.exists()) {
					    	destFile.getParentFile().mkdirs();
					    }
					    // Move file to new directory
					    boolean success = polymer.renameTo(destFile);
					    if (!success) {
					        throw new CrystalEyeRuntimeException("could not move file: "+polymer.getAbsolutePath());
					    }
					}
				    
				}
			}
		}
	}

}
