package wwmm.crystaleye.tasks;

import static wwmm.crystaleye.CrystalEyeConstants.CIF2CML;
import static wwmm.crystaleye.CrystalEyeConstants.CML2FOO;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.NED24_NS;
import static wwmm.crystaleye.CrystalEyeConstants.POLYMERIC_FLAG_DICTREF;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.base.CMLException;
import org.xmlcml.cml.base.RuntimeException;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.cml.element.CMLAngle;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLAtomParity;
import org.xmlcml.cml.element.CMLAtomSet;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLBondSet;
import org.xmlcml.cml.element.CMLBondStereo;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLLength;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLTorsion;
import org.xmlcml.cml.inchi.InChIGenerator;
import org.xmlcml.cml.inchi.InChIGeneratorFactory;
import org.xmlcml.cml.tools.ConnectionTableTool;
import org.xmlcml.cml.tools.DisorderTool;
import org.xmlcml.cml.tools.GeometryTool;
import org.xmlcml.cml.tools.MoleculeTool;
import org.xmlcml.cml.tools.ValencyTool;
import org.xmlcml.molutil.ChemicalElement;
import org.xmlcml.molutil.ChemicalElement.Type;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CDKUtils;
import wwmm.crystaleye.CrystalEyeUtils;
import wwmm.crystaleye.Utils;
import wwmm.crystaleye.CrystalEyeUtils.CompoundClass;
import wwmm.crystaleye.properties.ProcessProperties;
import wwmm.crystaleye.site.Cml2Png;

public class CML2FooManager extends AbstractManager implements CMLConstants {

	/**
	 * 
	 * @param issueWriteDir
	 * @param publisherAbbreviation
	 * @param journalAbbreviation
	 * @param year
	 * @param issueNum
	 */
	public void process() {


		CMLCml cml = (CMLCml)(Utils.parseCml(structureFile)).getRootElement();

		Nodes classNodes = cml.query(".//cml:scalar[@dictRef='iucr:compoundClass']", CML_XPATH);
		String compClass = "";
		if (classNodes.size() > 0) {
			compClass = ((Element)classNodes.get(0)).getValue();
		} else {
			throw new RuntimeException("Molecule should have a compoundClass scalar set.");
		}
		boolean isPolymeric = false;
		Nodes polymericNodes = cml.query(".//"+CMLMetadata.NS+"[@dictRef='"+
				POLYMERIC_FLAG_DICTREF+"']", CML_XPATH);
		if (polymericNodes.size() > 0) {
			isPolymeric = true;
		}

		if (!compClass.equals(CompoundClass.INORGANIC.toString()) && !isPolymeric) {
			CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);

			if (molecule.getAtomCount() < 1000) {
				List<Node> bondLengthNodes = CMLUtil.getQueryNodes(molecule, ".//cml:length", CML_XPATH);
				for (Node bondLengthNode : bondLengthNodes) {
					bondLengthNode.detach();
				}

				//don't generate image if the molecule is disordered
				List<CMLMolecule> uniqueMolList = CrystalEyeUtils.getUniqueSubMolecules(molecule);	
				int count = 1;
				for (CMLMolecule subMol : uniqueMolList) {	
					Nodes nonUnitOccNodes = subMol.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", CML_XPATH);
					if (!DisorderTool.isDisordered(subMol) && !subMol.hasCloseContacts() && nonUnitOccNodes.size() == 0
							&& Cif2CmlManager.hasBondOrdersAndCharges(subMol)) {
						if (CrystalEyeUtils.isBoringMolecule(subMol)) {
							continue;
						}
						write2dImages(subMol, pathMinusMime+"_"+count);
						count++;
					}
				}

				// set doi
				Nodes doiNodes = cml.query("//cml:scalar[@dictRef='idf:doi']", CML_XPATH);
				this.doi = "";
				if (doiNodes.size() > 0) {
					this.doi = ((Element)doiNodes.get(0)).getValue();
				}

				File structureParent = structureFile.getParentFile();
				String structureId = structureParent.getName();					
				File moietyDir = new File(structureParent, "moieties");
				outputMoieties(moietyDir, structureId, molecule, compClass);
			}
		}
	}

	private void write2dImages(CMLMolecule molecule, String pathMinusMime) {
		write2dImage(pathMinusMime+".small.png", molecule, 358, 278, false);
		write2dImage(pathMinusMime+".png", molecule, 600, 600, false);
	}

	private void write2dImage(String path, CMLMolecule molecule, int width, int height, boolean showH) {
		try {
			if (molecule.isMoleculeContainer()) {
				throw new RuntimeException("Molecule should not contain molecule children.");
			}
			File file = new File(path).getParentFile();
			if (!file.exists()) {
				file.mkdirs();
			}
			CDKUtils.add2DCoords(molecule);
			Cml2Png cp = new Cml2Png(molecule);
			cp.setWidthAndHeight(width, height);
			cp.renderMolecule(path);
		} catch (Exception e) {
			System.err.println("Could not produce 2D image for molecule.");
		}
	}

	private void outputMoieties(File dir, String id, CMLMolecule mergedMolecule, String compoundClass) {
		int moiCount = 0;
		for (CMLMolecule mol : mergedMolecule.getDescendantsOrMolecule()) {
			Nodes nonUnitOccNodes = mol.query(".//"+CMLAtom.NS+"[@occupancy[. < 1]]", CML_XPATH);
			if (!DisorderTool.isDisordered(mol) && !mol.hasCloseContacts() && nonUnitOccNodes.size() == 0
					&& Cif2CmlManager.hasBondOrdersAndCharges(mol)) {
				moiCount++;
				if (CrystalEyeUtils.isBoringMolecule(mol)) continue;
				// remove crystal nodes from molecule if they exist
				Nodes crystNodes = mol.query(".//"+CMLCrystal.NS, CML_XPATH);
				for (int i = 0; i < crystNodes.size(); i++) {
					crystNodes.get(i).detach();
				}

				String moiName = id+"_moiety_"+moiCount;
				String moiDir = dir+File.separator+moiName;
				String outPath = moiDir+File.separator+moiName+COMPLETE_CML_MIME;
				String pathMinusMime = outPath.substring(0,outPath.indexOf(COMPLETE_CML_MIME));
				addDoi(mol);
				writeXML(outPath, mol, "moiety");
				String smallPngPath = pathMinusMime+".small.png";
				String pngPath = pathMinusMime+".png";
				write2dImage(pngPath, mol, 600, 600, true);
				write2dImage(smallPngPath, mol, 358, 278, true);
				addAtomSequenceNumbers(mol);
				writeGeometryHtmlFiles(mol, pathMinusMime, 5);	
				// remove atom sequence numbers from mol now it has been written so they
				// don't interfere with the fragments that are written
				for (CMLAtom atom : mol.getAtoms()) {
					Attribute att = atom.getAttribute("sequenceNumber", NED24_NS);
					if (att != null) {
						att.detach();
					}
				}
			}
		}
	}

}
