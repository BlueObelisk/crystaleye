package wwmm.crystaleye.tasks;

import static wwmm.crystaleye.CrystalEyeConstants.CIF2CML;
import static wwmm.crystaleye.CrystalEyeConstants.CIF_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.COMPLETE_CML_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.DATE_MIME;
import static wwmm.crystaleye.CrystalEyeConstants.MAX_CIF_SIZE_IN_BYTES;
import static wwmm.crystaleye.CrystalEyeConstants.NED24_NS;
import static wwmm.crystaleye.CrystalEyeConstants.NO_BONDS_OR_CHARGES_FLAG_DICTREF;
import static wwmm.crystaleye.CrystalEyeConstants.POLYMERIC_FLAG_DICTREF;
import static wwmm.crystaleye.CrystalEyeConstants.RAW_CML_MIME;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.Text;
import nu.xom.XPathContext;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.xmlcml.cif.CIF;
import org.xmlcml.cif.CIFDataBlock;
import org.xmlcml.cif.CIFException;
import org.xmlcml.cif.CIFParser;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLBond;
import org.xmlcml.cml.element.CMLBondArray;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;
import org.xmlcml.cml.element.CMLFormula;
import org.xmlcml.cml.element.CMLLength;
import org.xmlcml.cml.element.CMLMetadata;
import org.xmlcml.cml.element.CMLMetadataList;
import org.xmlcml.cml.element.CMLMolecule;
import org.xmlcml.cml.element.CMLMolecule.HydrogenControl;
import org.xmlcml.cml.inchi.InChIGeneratorTool;
import org.xmlcml.cml.legacy2cml.cif.CIFConverter;
import org.xmlcml.cml.tools.ConnectionTableTool;
import org.xmlcml.cml.tools.CrystalTool;
import org.xmlcml.cml.tools.DisorderTool;
import org.xmlcml.cml.tools.DisorderToolControls;
import org.xmlcml.cml.tools.MoleculeTool;
import org.xmlcml.cml.tools.StereochemistryTool;
import org.xmlcml.cml.tools.ValencyTool;
import org.xmlcml.cml.tools.DisorderToolControls.ProcessControl;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.molutil.ChemicalElement.Type;

import wwmm.crystaleye.AbstractManager;
import wwmm.crystaleye.CDKUtils;
import wwmm.crystaleye.CheckCifParser;
import wwmm.crystaleye.CrystalEyeUtils;
import wwmm.crystaleye.Utils;
import wwmm.crystaleye.CrystalEyeUtils.CompoundClass;
import wwmm.crystaleye.properties.ProcessProperties;

public class Cif2CmlManager implements CMLConstants {

	/**
	 * To ensure that the library that processes cif files doesn't complain. A hack!
	 * @param file
	 * @return
	 */
	private boolean fileTooLarge(File file) {
		boolean tooLarge = false;
		int total = Utils.getFileSizeInBytes(file);
		if (total > MAX_CIF_SIZE_IN_BYTES) {
			System.err.println("CIF file too large to parse.  Skipping: "+file.getAbsolutePath());
			tooLarge = true;
		}
		return tooLarge;
	}
	
	/**
	 * Splits the original cif in the one data block per cif format
	 * @param file
	 * @return
	 */
	private List<File> createSplitCifs(File file) {
		String fileName = file.getAbsolutePath();
		List<File> splitCifList = new ArrayList<File>();
		// split the found CIF
		try {
			CIFParser parser = new CIFParser();
			parser.setSkipHeader(true);
			parser.setSkipErrors(true);
			parser.setCheckDuplicates(true);
			parser.setBlockIdsAsIntegers(false);

			CIF cif = (CIF) parser.parse(new BufferedReader(new FileReader(file))).getRootElement();

			List<CIFDataBlock> blockList = cif.getDataBlockList();
			CIFDataBlock global = null;
			String globalBlockId = "";
			for (CIFDataBlock block : blockList) {
				// check whether CIF is an mmCIF or not - we can't process mmCIFs so throw an exception if it is
				Elements loops = block.getChildElements("loop");
				for (int i = 0; i < loops.size(); i++) {
					Element loop = loops.get(i);
					Nodes mmCifNodes = loop.query("./@names[contains(.,'_atom_site.type_symbol') and " +
					"contains(.,'_atom_site.id')]");
					if (mmCifNodes.size() > 0) {
						System.err.println("CIF is an mmCIF, cannot process: "+file.getAbsolutePath());
						throw new CrystalEyeRuntimeException("CIF is an mmCIF, cannot process: "+file.getAbsolutePath());
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
						String cifPathMinusMime = Utils.getPathMinusMimeSet(file);
						String cifId = cifPathMinusMime.substring(cifPathMinusMime.lastIndexOf(File.separator)+1);
						String cifParent = cifPathMinusMime.substring(0,cifPathMinusMime.lastIndexOf(File.separator));
						File splitCifParent = new File(cifParent+File.separator+cifId+"_"+chemBlockId);
						if (!splitCifParent.exists()) {
							splitCifParent.mkdirs();
						}
						File splitCifFile = new File(splitCifParent,File.separator+cifId+"_"+chemBlockId+".cif");
						writer = new FileWriter(splitCifFile);
						cifNew.writeCIF(writer);
						writer.close();
						splitCifList.add(splitCifFile);
					} catch (CIFException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (writer != null)
							writer.close();
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new CrystalEyeRuntimeException("Could not find file "+fileName, e);
		} catch (CIFException e) {
			throw new CrystalEyeRuntimeException("Could not parse CIF in file "+fileName, e);
		} catch (IOException e) {
			throw new CrystalEyeRuntimeException("Could not read file "+fileName, e);
		}
		return splitCifList;
	}
	
	/**
	 * Calculate checkCif and writes it out to file
	 * @param cifPath
	 * @param pathMinusMime
	 */
	private void getCalculatedCheckCif(String cifPath, String pathMinusMime) {
		String calculatedCheckCif = calculateCheckcif(cifPath);
		String ccPath = pathMinusMime+".calculated.checkcif.html";
		Utils.writeText(calculatedCheckCif, ccPath);
	}

	/**
	 * Post CIF off to the checkCif service and grabs the returned HTML
	 * @param cifPath
	 * @return
	 */
	private String calculateCheckcif(String cifPath) {
		PostMethod filePost = null;
		InputStream in = null;
		String checkcif = "";

		int maxTries = 5;
		int count = 0;
		boolean finished = false;
		try {
			while(count < maxTries && !finished) {
				count++;
				File f = new File(cifPath);
				filePost = new PostMethod(
				"http://dynhost1.iucr.org/cgi-bin/checkcif.pl");
				Part[] parts = { new FilePart("file", f),
						new StringPart("runtype", "fullpublication"),
						new StringPart("UPLOAD", "Send CIF for checking") };
				filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
						new DefaultHttpMethodRetryHandler(5, false));
				filePost.setRequestEntity(new MultipartRequestEntity(parts,
						filePost.getParams()));
				HttpClient client = new HttpClient();
				int statusCode = client.executeMethod(filePost);
				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Could not connect to the IUCr Checkcif service.");
					continue;
				}
				in = filePost.getResponseBodyAsStream();
				checkcif = XmlUtils.stream2String(in);
				in.close();
				if (checkcif.length() > 0) {
					finished = true;
				}
			}
		} catch (IOException e) {
			System.err.println("Error calculating checkcif.");
		} finally {
			if (filePost != null) {
				filePost.releaseConnection();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					System.err.println("Error closing InputStream: "+in);
				}
			}
		}
		return checkcif;
	}
	
	/**
	 * Calculate the checkCif for this CML and converts the result of the checkCif
	 * into XML and appends this to the in process CML. Also downloads the ellipsoid plot which
	 * is linked to from the checkCif html 
	 * @param cml
	 * @param pathMinusMime
	 */
	private void handleCheckcifs(CMLCml cml, String pathMinusMime) {
		String depositedCheckcifPath = pathMinusMime.substring(0,pathMinusMime.lastIndexOf(File.separator));
		String depCCParent = new File(depositedCheckcifPath).getParent();
		depositedCheckcifPath = depCCParent+pathMinusMime.substring(pathMinusMime.lastIndexOf(File.separator),pathMinusMime.lastIndexOf("_"))+".deposited.checkcif.html";
		String calculatedCheckcifPath = pathMinusMime+".calculated.checkcif.html";
		File depositedCheckcif = new File(depositedCheckcifPath);
		File calculatedCheckcif = new File(calculatedCheckcifPath);
		if (depositedCheckcif.exists()) {
			String contents = Utils.file2String(depositedCheckcifPath);
			Document deposDoc = new CheckCifParser(contents).parseDeposited();
			cml.appendChild(deposDoc.getRootElement().copy());
		}
		if (calculatedCheckcif.exists()) {
			String contents = Utils.file2String(calculatedCheckcifPath);
			Document calcDoc = new CheckCifParser(contents).parseCalculated();
			cml.appendChild(calcDoc.getRootElement().copy());
			this.getPlatonImage(calcDoc, pathMinusMime);	
		}
	}
	
	/**
	 * Extracts the URL of the ellipsoid plot from the checkCif and downloads it
	 * @param doc
	 * @param pathMinusMime
	 */
	private void getPlatonImage(Document doc, String pathMinusMime) {
		// get platon from parsed checkcif/store
		Nodes platonLinks = doc.query("//x:checkCif/x:calculated/x:dataBlock/x:platon/x:link", new XPathContext("x", "http://journals.iucr.org/services/cif"));
		if (platonLinks.size() > 0) {
			URL url = null;
			try {
				String imageLink = platonLinks.get(0).getValue();
				String prefix = imageLink.substring(0, imageLink.lastIndexOf("/")+1);
				String file = imageLink.substring(imageLink.lastIndexOf("/")+1);
				url = new URL(prefix+URLEncoder.encode(file,"UTF-8")); 
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			BufferedImage image = null;
			try {
				image = ImageIO.read(url);
				image = image.getSubimage(14, 15, 590, 443);
				ImageIO.write(image, "jpeg", new File(pathMinusMime+".platon.jpeg"));
			} catch (IOException e) {
				System.err.println("ERROR: could not read PLATON image");
			}
		}	
	}

}
