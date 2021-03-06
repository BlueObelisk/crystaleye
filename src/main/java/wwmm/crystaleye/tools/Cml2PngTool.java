package wwmm.crystaleye.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.renderer.Renderer2D;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.tools.MoleculeTool;

import wwmm.crystaleye.util.CDKUtils;
import wwmm.crystaleye.util.Utils;

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
    /* Gif apparently doesn't work, for strange legal reasons. */
    public String format = "png";
   
    public double occupationFactor = 0.8; /* 1.0 = no border */
    public double scaleFactor = 20.0;
    public int borderWidth = 20; /* Pixels. This is *after* a sensible margin for lettering */
   
    public int width = 500;
    public int height = 500;
   
    public Cml2PngTool(CMLMolecule molecule) {
        this.cmlMol = new CMLMolecule(molecule);
        MoleculeTool mt = MoleculeTool.getOrCreateTool(this.cmlMol);
        mt.contractExplicitHydrogens(CMLMolecule.HydrogenControl.REPLACE_HYDROGEN_COUNT, false);
        if (molecule.getDescendantsOrMolecule().size() > 1) {
            throw new RuntimeException("CMLMolecule must not have any child molecules");
        }
    }
   
    public void renderMolecule(String path) {
        try {
            renderMolecule(new FileOutputStream(new File(path)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error writing 2D image to: "+path);
        }
    }

    public void renderMolecule(File file) {
        try {
            renderMolecule(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error writing 2D image to: "+file.getAbsolutePath());
        }
    }

    public void renderMolecule(OutputStream out) {           
        Renderer2DModel r2dm = new Renderer2DModel();
        Renderer2D r2d = new Renderer2D(r2dm);
       
        IMolecule cdkMol = CDKUtils.getCdkMol(cmlMol);
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
               
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = img.getGraphics();
        g.setColor(backgroundColour);       
        g.fillRect(0, 0, width, height);
       
        GeometryTools.translateAllPositive(cdkMol,r2dm.getRenderingCoordinates());
        GeometryTools.scaleMolecule(cdkMol, new Dimension(width, height), 0.8,r2dm.getRenderingCoordinates());
        GeometryTools.center(cdkMol, new Dimension(width, height), r2dm.getRenderingCoordinates());
        r2dm.setBackgroundDimension(new Dimension(width, height));
        r2dm.setBackColor(backgroundColour);
        r2dm.setFont(new Font(fontName, fontStyle, fontSize));
        r2dm.setShowImplicitHydrogens(true);
        r2dm.setShowEndCarbons(true);
       
        if(cdkMol != null) r2d.paintMolecule(cdkMol, img.createGraphics(), true, true);
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
   
    public static void main(String[] args) {
        File cmlFile = new File("c:/workspace/test.complete.cml");
        CMLCml cmlCml = (CMLCml)Utils.parseCml(cmlFile).getRootElement();
        CMLMolecule mol = (CMLMolecule) cmlCml.getFirstCMLChild(CMLMolecule.TAG);
        int count = 1;
        for (CMLMolecule subMol : mol.getDescendantsOrMolecule()) {
            Cml2PngTool cp = new Cml2PngTool(subMol);
            try {
                cp.renderMolecule(new FileOutputStream(cmlFile.getAbsolutePath()+"_"+count+".png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            count++;
        }
    }
}