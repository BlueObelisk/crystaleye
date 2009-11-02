package wwmm.crystaleye.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.Java2DRenderer;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.tools.MoleculeTool;

import wwmm.crystaleye.CDKUtils;
import wwmm.crystaleye.IOUtils;

/** 
 * Produces png images for CML molecules using CDK.
 * 
 * @author ptc24, Nick Day
 * @version 1.1
 *
 */
public class Cml2PngTool {

	private CMLMolecule cmlMol;

	public Color backgroundColour = Color.WHITE;
	public String fontName = "Sans Serif";
	public int fontStyle = Font.PLAIN;
	public int fontSize = 16;
	public String format = "png";

	public double occupationFactor = 0.8; /* 1.0 = no border */
	public double scaleFactor = 20.0;
	public int borderWidth = 20; /* Pixels. This is *after* a sensible margin for lettering */

	public int width = 500;
	public int height = 500;

	private Cml2PngTool() {
		;
	}

	public Cml2PngTool(CMLMolecule molecule) {
		this.cmlMol = molecule;
		MoleculeTool mt = MoleculeTool.getOrCreateTool(this.cmlMol);
		mt.contractExplicitHydrogens(CMLMolecule.HydrogenControl.REPLACE_HYDROGEN_COUNT, false);
		if (molecule.getDescendantsOrMolecule().size() > 1) {
			throw new RuntimeException("CMLMolecule must not have any child molecules");
		}
	}

	public void renderMolecule(String path) {
		renderMolecule(new File(path));
	}

	public void renderMolecule(File file) {
		try {
			renderMolecule(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error writing 2D image to: "+file.getAbsolutePath());
		}
	}

	public void renderMolecule(OutputStream out) {		
		Renderer2DModel model = new Renderer2DModel();
		Java2DRenderer renderer = new Java2DRenderer(model);

		IAtomContainer cdkMol = CDKUtils.createMolecule(cmlMol);
		int atomCount = cdkMol.getAtomCount();
		if (atomCount > 1 && atomCount < 20) {
			fontSize = 14;
		} else if (atomCount >= 20 && atomCount < 30) {
			fontSize = 13;
		} else if (atomCount >= 30 && atomCount < 40) {
			fontSize = 12;
		} else if (atomCount >= 40 && atomCount < 50) {
			fontSize = 11;
		} else if (atomCount >= 50) {
			fontSize = 10;
		}

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);
		Graphics g = img.createGraphics();
		g.setColor(backgroundColour);		
		g.fillRect(0, 0, width, height);

		model.setBackgroundDimension(new Dimension(width, height));
		model.setBackColor(backgroundColour);
		model.setFont(new Font(fontName, fontStyle, fontSize));
		model.setShowImplicitHydrogens(true);
		model.setShowEndCarbons(true);

		renderer.paintMolecule(cdkMol, (Graphics2D) g, new Rectangle(width, height));
		try {
			ImageIO.write(img, format, out);
		} catch (IOException e) {
			throw new RuntimeException("Error writing image: "+e.getMessage());
		}
	}

	public void setWidthAndHeight(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public static void main(String[] args) throws FileNotFoundException {
		File cmlFile = new File("E:\\data-test\\cif\\acta\\e\\2006\\13-00\\ac2051\\ac2051sup1_I\\ac2051sup1_I.cml.xml");
		CMLCml cmlCml = (CMLCml)IOUtils.parseCml(cmlFile).getRootElement();
		CMLMolecule mol = (CMLMolecule) cmlCml.getFirstCMLChild(CMLMolecule.TAG);
		mol = CDKUtils.add2DCoords(mol);
		int count = 1;
		for (CMLMolecule subMol : mol.getDescendantsOrMolecule()) {
			Cml2PngTool cp = new Cml2PngTool(subMol);
			cp.renderMolecule(new FileOutputStream(cmlFile.getAbsolutePath()+"_"+count+".png"));
			count++;
		}
	}
}
