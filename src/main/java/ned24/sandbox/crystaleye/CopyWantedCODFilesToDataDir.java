package ned24.sandbox.crystaleye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import wwmm.crystaleye.util.Utils;

public class CopyWantedCODFilesToDataDir {

	String codDir;
	String dataPath;
	String dataCellParamPath;
	String codCellParamPath;

	private static final double CELL_PARAM_EPSILON = 0.01;

	private CopyWantedCODFilesToDataDir() {
		;
	}

	public CopyWantedCODFilesToDataDir(String codDir, String dataPath, String dataCellParamPath, String codCellParamPath) {
		this.dataPath = dataPath;
		this.codDir = codDir;
		this.dataCellParamPath = dataCellParamPath;
		this.codCellParamPath = codCellParamPath;
	}

	public void run() {
		Set<String[]> dataCellParamSet = populateDataCellParamSet(dataCellParamPath);
		Set<String[]> codCellParamSet = populateCodCellParamSet(codCellParamPath);
		Set<String> alreadyMovedSet = new HashSet<String>();

		for (String[] da : codCellParamSet) {
			boolean alreadyGot = false;
			for (String[] dc : dataCellParamSet) {
				int counter = 0;
				for (int i = 0; i < 6; i++) {
					if (Math.abs(Double.valueOf(da[i])-Double.valueOf(dc[i])) < CELL_PARAM_EPSILON) {
						counter++;
					} else {
						break;
					}
				}
				if (counter == 6) {
					alreadyGot = true;
					Utils.appendToFile(new File("e:/data-test/already-got-from-cod.txt"), da[6]+","+dc[6]+"\n");
					break;
				}
			}
			if (!alreadyGot) {
				String id = da[6];
				String[] parts = id.split("_");
				if (!alreadyMovedSet.contains(parts[4])) {
					String cifName = parts[4]+".cif";
					String outPath = dataPath+File.separator+parts[0]+File.separator+parts[1]+File.separator+parts[2]+File.separator+parts[3]+
					File.separator+parts[4]+File.separator+cifName;
					System.out.println(outPath);
					String cifPath = codDir+File.separator+cifName;
					File cifFile = new File(cifPath);
					File outFile = new File(outPath);
					File outParent = outFile.getParentFile();

					if (!outParent.exists()) {
						outParent.mkdirs();
					}

					//Utils.copyFile(cifFile.getAbsolutePath(), outFile.getAbsolutePath());	
					System.out.println(outPath);
					boolean success = cifFile.renameTo(outFile);
					if (!success) {
						throw new RuntimeException("Could not copy file: "+cifFile.getAbsolutePath());
					}
					alreadyMovedSet.add(parts[4]);
				}
			}
		}
	}

	private Set<String[]> populateCodCellParamSet(String cellParamPath) {
		Set<String[]> set = new HashSet<String[]>();
		String listPath = cellParamPath;
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(listPath));
			String line = null;
			while (( line = input.readLine()) != null){
				if (line == null || "".equals(line)) continue;
				String[] arr = line.split(",");
				String[] darr = new String[arr.length];
				for (int i = 0; i < 7; i++) {
					darr[i] = arr[i];
				}
				set.add(darr);
			}
			input.close();
		}
		catch (FileNotFoundException ex) {
			throw new CrystalEyeRuntimeException("Could not find file: "+listPath);
		}
		catch (IOException ex){
			throw new CrystalEyeRuntimeException("Error reading file: "+listPath);
		}
		finally {
			try {
				if (input!= null) {
					input.close();
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return set;
	}

	private Set<String[]> populateDataCellParamSet(String cellParamPath) {
		Set<String[]> set = new HashSet<String[]>();
		String listPath = cellParamPath;
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(listPath));
			String line = null;
			while (( line = input.readLine()) != null){
				if (line == null || "".equals(line)) continue;
				String[] arr = line.split(",");
				String[] darr = new String[arr.length];
				for (int i = 0; i < 7; i++) {
					darr[i] = arr[i];
				}
				set.add(darr);
			}
			input.close();
		}
		catch (FileNotFoundException ex) {
			throw new CrystalEyeRuntimeException("Could not find file: "+listPath);
		}
		catch (IOException ex){
			throw new CrystalEyeRuntimeException("Error reading file: "+listPath);
		}
		finally {
			try {
				if (input!= null) {
					input.close();
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return set;
	}

	public static void main(String[] args) {
		String codDir = "e:/COD";
		String dataPath = "e:/crystaleye-test/data";
		String dataCellParamPath = "e:/data-test/publisher-cell-params.txt";
		String codParamPath = "e:/data-test/cod-params.txt";
		CopyWantedCODFilesToDataDir c = new CopyWantedCODFilesToDataDir(codDir, dataPath, dataCellParamPath, codParamPath);
		c.run();
	}
}
