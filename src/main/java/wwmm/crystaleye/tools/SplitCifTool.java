package wwmm.crystaleye.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFDataBlock;
import org.xmlcml.cif.CIFParser;
import wwmm.crystaleye.util.Utils;

public class SplitCifTool {
	
	private static final Logger LOG = Logger.getLogger(SplitCifTool.class);
	
	public List<File> split(File cifFile) {
		List<File> splitCifList = new ArrayList<File>();
		
		try {
			CIFParser parser = new CIFParser();
			parser.setSkipHeader(true);
			parser.setSkipErrors(true);
			parser.setCheckDuplicates(true);
			parser.setBlockIdsAsIntegers(false);

			CIF cif = (CIF) parser.parse(new BufferedReader(new FileReader(cifFile))).getRootElement();

			List<CIFDataBlock> blockList = cif.getDataBlockList();
			CIFDataBlock global = null;
			String globalBlockId = "";
			for (CIFDataBlock block : blockList) {
				// check whether CIF is an mmCIF or not - we can't process mmCIFs
				Elements loops = block.getChildElements("loop");
				for (int i = 0; i < loops.size(); i++) {
					Element loop = loops.get(i);
					Nodes mmCifNodes = loop.query("./@names[contains(.,'_atom_site.type_symbol') and " +
					"contains(.,'_atom_site.id')]");
					if (mmCifNodes.size() > 0) {
						LOG.warn("CIF is an mmCIF, cannot process: "+cifFile.getAbsolutePath());
					}
				}
			}
			for (CIFDataBlock block : blockList) {		
				Nodes crystalNodes = block.query(".//item[@name='_cell_length_a']");
				Nodes moleculeNodes = block.query(".//loop[contains(@names,'_atom_site_label')]");
				Nodes symmetryNodes = block.query(".//loop[contains(@names,'_symmetry_equiv_pos_as_xyz')]");
				if (crystalNodes.size() == 0 && moleculeNodes.size() == 0 && symmetryNodes.size() == 0) {
					global = block;
					globalBlockId = block.getId();
					break;
				}
			}
			for (CIFDataBlock block : blockList) {
				if (block.getId().equalsIgnoreCase(globalBlockId)) {
					continue;
				} else {
					CIF cifNew = new CIF();
					block.detach();
					if (global != null) {
						global.detach();
					}
					Writer writer = null; 
					try {
						if (global != null) {
							cifNew.add(global);
						}
						cifNew.add(block);
						String chemBlockId = block.getId();
						chemBlockId = chemBlockId.replaceAll("\\.", "-");
						chemBlockId = chemBlockId.replaceAll(":", "-");
						chemBlockId = chemBlockId.replaceAll("/", "-");
						chemBlockId = chemBlockId.replaceAll("\\\\", "-");
						chemBlockId = chemBlockId.replaceAll("_", "-");
						chemBlockId = chemBlockId.replaceAll("%", "-");
						chemBlockId = chemBlockId.replaceAll("\\*", "-");
						chemBlockId = chemBlockId.replaceAll("\\?", "-");
						chemBlockId = chemBlockId.replaceAll(">", "-");
						chemBlockId = chemBlockId.replaceAll("<", "-");
						chemBlockId = chemBlockId.replaceAll("'", "-");
						chemBlockId = chemBlockId.replaceAll("\"", "-");
						chemBlockId = chemBlockId.replaceAll(",", "-");
						String cifPathMinusMime = Utils.getPathMinusMimeSet(cifFile);
						String cifId = cifPathMinusMime.substring(cifPathMinusMime.lastIndexOf(File.separator)+1);
						String cifParent = cifPathMinusMime.substring(0,cifPathMinusMime.lastIndexOf(File.separator));
						File splitCifParent = new File(cifParent+"/"+cifId+"_"+chemBlockId);
						if (!splitCifParent.exists()) {
							if (!splitCifParent.mkdirs()) {
								LOG.warn("Could not create folder at: "+splitCifParent);
							}
						}
						File splitCifFile = new File(splitCifParent,"/"+cifId+"_"+chemBlockId+".cif");
						writer = new FileWriter(splitCifFile);
						cifNew.writeCIF(writer);
						writer.close();
						splitCifList.add(splitCifFile);
					} catch (Exception e) {
						LOG.warn("Exception whilst splitting CIF file ("+cifFile+"), due to: "+e.getMessage());
					} finally {
						IOUtils.closeQuietly(writer);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Problem parsing CIF ("+cifFile+"), due to: "+e.getMessage(), e);
		}
		return splitCifList;
	}

}
