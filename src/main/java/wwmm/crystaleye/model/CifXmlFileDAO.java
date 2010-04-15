package wwmm.crystaleye.model;

import static wwmm.crystaleye.CrystaleyeConstants.CIFXML_EXTENSION;

import java.io.File;

import ned24.fsdb.PrimaryFileDAO;

import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFException;

import wwmm.pubcrawler.Utils;

public class CifXmlFileDAO extends PrimaryFileDAO {
	
	private static final int LEVELS_DOWN = 1;
	
	public CifXmlFileDAO(File dbRoot) {
		super(dbRoot, CIFXML_EXTENSION, LEVELS_DOWN);
	}
	
	public CIF getCifXml(int... keys) {
		File file = getAndEnsureFileFromKeys(keys);
		try {
			return new CIF(Utils.parseXml(file), true);
		} catch (CIFException e) {
			throw new RuntimeException("Problem getting CIFXML from "+file+", "+e.getMessage());
		}
	}
	
}
