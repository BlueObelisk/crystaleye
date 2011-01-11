package wwmm.crystaleye.tools;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFDataBlock;
import org.xmlcml.cif.CIFException;
import wwmm.crystaleye.util.CifIO;
import wwmm.crystaleye.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CifFileSplitter {

    private static final Logger LOG = Logger.getLogger(CifFileSplitter.class);

    public List<File> split(File cifFile) {
        List<File> splitCifFiles = new ArrayList<File>();
        try {
            CIF cif = CifIO.readCif(cifFile, "UTF-8");
            List<CIF> splitCifs = CifSplitter.split(cif);

            for (CIF splitCif : splitCifs) {
                List<CIFDataBlock> dataBlocks = splitCif.getDataBlockList();
                CIFDataBlock mainBlock = dataBlocks.get(dataBlocks.size()-1);

                File splitCifFile = getSplitCifFile(cifFile, mainBlock);
                FileUtils.forceMkdir(splitCifFile.getParentFile());

                CifIO.writeCif(splitCif, splitCifFile, "UTF-8");
                splitCifFiles.add(splitCifFile);
            }

        } catch (Exception e) {
            throw new RuntimeException("Problem parsing CIF ("+cifFile+")", e);
        }

        return splitCifFiles;
    }

    private static File getSplitCifFile(File cifFile, CIFDataBlock block) {
        String chemBlockId = normaliseBlockId(block.getId());
        String cifPathMinusMime = Utils.getPathMinusMimeSet(cifFile);
        String cifId = cifPathMinusMime.substring(cifPathMinusMime.lastIndexOf(File.separator)+1);
        String cifParent = cifPathMinusMime.substring(0,cifPathMinusMime.lastIndexOf(File.separator));
        File splitCifParent = new File(cifParent+"/"+cifId+"_"+chemBlockId);
        File splitCifFile = new File(splitCifParent,"/"+cifId+"_"+chemBlockId+".cif");
        return splitCifFile;
    }

    private static String normaliseBlockId(String chemBlockId) {
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

}
