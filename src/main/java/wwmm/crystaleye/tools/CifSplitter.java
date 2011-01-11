package wwmm.crystaleye.tools;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFDataBlock;
import org.xmlcml.cif.CIFException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sam Adams
 */
public class CifSplitter {

    /**
     * Creates a list of CIFs, each containing a single datablock, along
     * with any global block if present.
     * @param cif
     * @return
     * @throws CIFException
     */
    public static List<CIF> split(CIF cif) throws CIFException {
        // check whether CIF is an mmCIF or not - we can't process mmCIFs
        if (isMmcif(cif)) {
            throw new IllegalArgumentException("MMCIF files not supported");
        }

        List<CIF> splitCifs = new ArrayList<CIF>();
        CIFDataBlock globalBlock = findGlobalBlock(cif);
        for (CIFDataBlock block : cif.getDataBlockList()) {
            if (block == globalBlock) {
                continue;
            }
            CIF newCif = createCIF(globalBlock, block);
            splitCifs.add(newCif);
        }
        return splitCifs;
    }

    private static boolean isMmcif(CIF cif) {
        for (CIFDataBlock block : cif.getDataBlockList()) {
            Elements loops = block.getChildElements("loop");
            for (int i = 0; i < loops.size(); i++) {
                Element loop = loops.get(i);
                Nodes mmCifNodes = loop.query("./@names[contains(.,'_atom_site.type_symbol') and " +
                        "contains(.,'_atom_site.id')]");
                if (mmCifNodes.size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static CIFDataBlock findGlobalBlock(CIF cif) {
        CIFDataBlock global = null;
        for (CIFDataBlock block : cif.getDataBlockList()) {
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

    private static CIF createCIF(CIFDataBlock global, CIFDataBlock block) throws CIFException {
        CIF cifNew = new CIF();
        if (global != null) {
            CIFDataBlock globalCopy = (CIFDataBlock) global.copy();
            cifNew.add(globalCopy);
        }

        CIFDataBlock blockCopy = (CIFDataBlock) block.copy();
        cifNew.add(blockCopy);
        return cifNew;
    }

}
