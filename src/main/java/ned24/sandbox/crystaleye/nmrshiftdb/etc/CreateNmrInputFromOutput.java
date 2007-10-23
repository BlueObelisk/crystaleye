package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianTemplate;
import ned24.sandbox.crystaleye.nmrshiftdb.GaussianUtils;
import nu.xom.Nodes;

import org.xmlcml.cml.element.CMLAtom;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLMolecule;

import uk.ac.cam.ch.crystaleye.IOUtils;

public class CreateNmrInputFromOutput implements GaussianConstants {

	List<File> fileList;
	String outFolder;
	File tempFile;
	
	public CreateNmrInputFromOutput(String outFolder, List<File> fileList, File tempFile) {
		this.fileList = fileList;
		this.outFolder = outFolder;
		this.tempFile = tempFile;
	}
	
	public void run() {
		for (File f : fileList) {
			CMLCml cml = GaussianUtils.getFinalCml(f, tempFile);
			cml.debug();
			CMLMolecule molecule = (CMLMolecule)cml.getFirstCMLChild(CMLMolecule.TAG);
			String connTable = GaussianUtils.getConnectionTable(molecule);
			String solvent = getSolvent(cml);
			if (solvent == null) {
				throw new RuntimeException("Could not find solvent: "+f.getAbsolutePath());
			}
			String name = getFileName(f);
			boolean freq = false;
			GaussianTemplate gau = new GaussianTemplate(name, connTable, solvent, freq);
			gau.setExtraBasis(true);

			boolean hasC = false;
			boolean hasO = false;
			for (CMLAtom atom : molecule.getAtoms()) {
				if ("C".equals(atom.getElementType())) {
					hasC = true;
				}
				if ("O".equals(atom.getElementType())) {
					hasO = true;
				}
			}
			gau.setHasC(hasC);
			gau.setHasO(hasO);
			
			String input = gau.getNmrStepInput();
			System.out.println(input);
			
			int numberFileCount = 1;
			File outFile = new File(outFolder);
			String folderName = outFile.getName()+"/"+numberFileCount;
			String outPath = outFolder+File.separator+numberFileCount+File.separator+name+FLOW_MIME;
			IOUtils.writeText(input, outPath);
			writeCondorSubmitFile(folderName, name, numberFileCount);
			writeShFile(folderName, name, numberFileCount);
		}
	}
	
	public void writeCondorSubmitFile(String folderName, String name, int numberFileCount) {
		String submitFile = "universe=vanilla\n"+
		"getenv=True\n"+
		"requirements = Arch == \"X86_64\" && OpSys == \"LINUX\" && Machine != \"gridlock20--ch.grid.private.cam.ac.uk\" && Machine != \"gridlock26--ch.grid.private.cam.ac.uk\" && Machine != \"gridlock27--ch.grid.private.cam.ac.uk\" && HAS_GAUSSIAN == TRUE\n"+
		"executable = /home/ned24/gaussian/"+folderName+"/"+name+".sh\n"+
		"input = /home/ned24/gaussian/"+folderName+"/"+name+FLOW_MIME+"\n"+
		"output = "+name+".out\n"+
		"error = "+name+".err\n"+
		"log = "+name+".log\n"+
		"\n"+
		"should_transfer_files=YES\n"+
		"transfer_executable=True\n"+
		"when_to_transfer_output=ON_EXIT_OR_EVICT\n"+
		"\n"+
		"Queue\n";

		System.out.println("condor: "+outFolder+File.separator+numberFileCount+File.separator+name+SUBMIT_FILE_MIME);
		IOUtils.writeText(submitFile, outFolder+File.separator+numberFileCount+File.separator+name+SUBMIT_FILE_MIME);
	}

	public void writeShFile(String folderName, String name, int numberFileCount) {
		String content = "#!/bin/sh\n"+
		"\n"+
		"# Run g03 job\n"+
		"/usr/local/g03/g03 < "+name+FLOW_MIME+" > "+name+".out \n";
		System.out.println("sh "+outFolder+File.separator+numberFileCount+File.separator+name+".sh");
		IOUtils.writeText(content, outFolder+File.separator+numberFileCount+File.separator+name+".sh");
	}
	
	private String getSolvent(CMLCml cml) {
		Nodes nodes = cml.query(".//cml:scalar[@dictRef='gauss:solvent']", X_CML);
		if (nodes.size() != 1) {
			throw new RuntimeException("Should have found1 solvent, found "+nodes.size());
		}
		return nodes.get(0).getValue();
	}
	
	private static String getFileName(File file) {
		String name = file.getName();
		int idx = name.indexOf(".");
		return name.substring(0,idx);
	}
	
	public static void main(String[] args) {
		String path = "e:/gaussian/outputs/second-protocol/1";
		String outFolder = "e:/gaussian/inputs/second-protocol_mod1/";
		String tmpFolder = "e:/temp";
		
		List<File> fileList = new ArrayList<File>();
		for (File f : new File(path).listFiles()) {
			if (f.getAbsolutePath().endsWith(".out")) {
				fileList.add(f);
			}
		}
		CreateNmrInputFromOutput c = new CreateNmrInputFromOutput(outFolder, fileList, new File(tmpFolder));
		c.run();
	}
}
