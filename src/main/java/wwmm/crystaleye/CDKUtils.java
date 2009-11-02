package wwmm.crystaleye;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

import javax.vecmath.Point2d;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLMolecule;

import wwmm.crystaleye.tools.MinimalCmlTool;

public class CDKUtils {
	
	public static IMolecule getCdkMol(CMLMolecule cmlMol) {
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new ByteArrayInputStream(cmlMol.toXML().getBytes()));
			IChemFile cf = (IChemFile) new CMLReader(bis).read(new ChemFile());
			bis.close();
			IMoleculeSet mols = cf.getChemSequence(0).getChemModel(0).getMoleculeSet();
			if (mols.getMoleculeCount() > 1) {
				throw new RuntimeException("CDK found more than one molecule in molecule.");
			}
			return mols.getMolecule(0);
		} catch (Exception e) {
			throw new RuntimeException("Exception while creating CDK molecule: "+e.getMessage());
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(bis);
		}
	}

	public static CMLMolecule add2DCoords(CMLMolecule molecule) {
		CMLMolecule minimalMol = new MinimalCmlTool(molecule).getMinimalMol();
		IMolecule mol = getCdkMol(minimalMol);
		for (int i = 0; i < mol.getAtomCount(); i++) {
			IAtom atom = mol.getAtom(i);
			atom.setPoint2d(null);
			atom.setPoint3d(null);
		}
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		sdg.setMolecule(mol);
		try {
			sdg.generateCoordinates();
		} catch (Exception e) {
			throw new RuntimeException("Error generating molecule coordinates: "+e.getMessage());
		}
		mol = sdg.getMolecule();
		for (int i = 0; i < mol.getAtomCount(); i++) {
			IAtom atom = mol.getAtom(i);
			Point2d p = atom.getPoint2d();
			CMLAtom cmlAtom = molecule.getAtomById(atom.getID());
			cmlAtom.setX2(p.x);
			cmlAtom.setY2(p.y);
		}
		return molecule;
	}
	
}
