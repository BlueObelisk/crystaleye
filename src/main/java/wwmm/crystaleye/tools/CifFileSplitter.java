package wwmm.crystaleye.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFDataBlock;
import org.xmlcml.cif.CIFException;
import org.xmlcml.cif.CIFParser;
import wwmm.crystaleye.util.Utils;

public class CifFileSplitter {

    private static final Logger LOG = Logger.getLogger(CifFileSplitter.class);

    public List<File> split(File cifFile) {
        List<File> splitCifList = new ArrayList<File>();
        try {
            CIF cif = readCif(cifFile);

            List<CIFDataBlock> blockList = cif.getDataBlockList();

            for (CIFDataBlock block : blockList) {
                // check whether CIF is an mmCIF or not - we can't process mmCIFs
                Elements loops = block.getChildElements("loop");
                for (int i = 0; i < loops.size(); i++) {
                    Element loop = loops.get(i);
                    Nodes mmCifNodes = loop.query("./@names[contains(.,'_atom_site.type_symbol') and " +
                            "contains(.,'_atom_site.id')]");
                    if (mmCifNodes.size() > 0) {
                        LOG.warn("CIF is an mmCIF, cannot process: "+cifFile.getAbsolutePath());
                        // TODO should this return?
                    }
                }
            }

            CIFDataBlock global = findGlobalBlock(blockList);
            for (CIFDataBlock block : blockList) {
                if (block == global) {
                    continue;
                }

                CIF cifNew = createCIF(global, block);
                try {
                    String chemBlockId = normaliseBlockId(block.getId());
                    String cifPathMinusMime = Utils.getPathMinusMimeSet(cifFile);
                    String cifId = cifPathMinusMime.substring(cifPathMinusMime.lastIndexOf(File.separator)+1);
                    String cifParent = cifPathMinusMime.substring(0,cifPathMinusMime.lastIndexOf(File.separator));
                    File splitCifParent = new File(cifParent+"/"+cifId+"_"+chemBlockId);
                    if (!splitCifParent.isDirectory()) {
                        FileUtils.forceMkdir(splitCifParent);
                    }
                    File splitCifFile = new File(splitCifParent,"/"+cifId+"_"+chemBlockId+".cif");
                    writeCifFile(cifNew, splitCifFile);
                    splitCifList.add(splitCifFile);
                } catch (Exception e) {
                    LOG.warn("Exception whilst splitting CIF file ("+cifFile+")", e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Problem parsing CIF ("+cifFile+")", e);
        }
        return splitCifList;
    }

    private CIF createCIF(CIFDataBlock global, CIFDataBlock block) throws CIFException {
        CIF cifNew = new CIF();
        if (global != null) {
            global.detach();
            cifNew.add(global);
        }
        block.detach();
        cifNew.add(block);
        return cifNew;
    }

    private void writeCifFile(CIF cifNew, File splitCifFile) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(splitCifFile), "UTF-8"));
        try {
            cifNew.writeCIF(out);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private String normaliseBlockId(String chemBlockId) {
        chemBlockId = chemBlockId.replace('.', '-');
        chemBlockId = chemBlockId.replace(':', '-');
        chemBlockId = chemBlockId.replace('/', '-');
        chemBlockId = chemBlockId.replace('\\', '-');
        chemBlockId = chemBlockId.replace('_', '-');
        chemBlockId = chemBlockId.replace('%', '-');
        chemBlockId = chemBlockId.replace('*', '-');
        chemBlockId = chemBlockId.replace('?', '-');
        chemBlockId = chemBlockId.replace('>', '-');
        chemBlockId = chemBlockId.replace('<', '-');
        chemBlockId = chemBlockId.replace('\'','-');
        chemBlockId = chemBlockId.replace('"', '-');
        chemBlockId = chemBlockId.replace(',', '-');
        return chemBlockId;
    }

    private CIFDataBlock findGlobalBlock(List<CIFDataBlock> blockList) {
        CIFDataBlock global = null;
        for (CIFDataBlock block : blockList) {
            Nodes crystalNodes = block.query(".//item[@name='_cell_length_a']");
            Nodes moleculeNodes = block.query(".//loop[contains(@names,'_atom_site_label')]");
            Nodes symmetryNodes = block.query(".//loop[contains(@names,'_symmetry_equiv_pos_as_xyz')]");
            if (crystalNodes.size() == 0 && moleculeNodes.size() == 0 && symmetryNodes.size() == 0) {
                global = block;
                break;
            }
        }
        return global;
    }

    private CIF readCif(File cifFile) throws CIFException, IOException {
        CIF cif;

        CIFParser parser = createCifParser();
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(cifFile), "UTF-8"));
        try {
            cif = (CIF) parser.parse(in).getRootElement();
        } finally {
            IOUtils.closeQuietly(in);
        }
        return cif;
    }

    protected CIFParser createCifParser() {
        CIFParser parser = new CIFParser();
        parser.setSkipHeader(true);
        parser.setSkipErrors(true);
        parser.setCheckDuplicates(true);
        parser.setBlockIdsAsIntegers(false);
        return parser;
    }

}
