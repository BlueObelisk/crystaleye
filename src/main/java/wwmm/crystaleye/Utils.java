package wwmm.crystaleye;

import java.io.File;


/**
 * The class is used to define many usual routines
 *
 * @author Nick Day <ned24@cam.ac.uk>
 */
public class Utils {
	
	public static double round(double val, int places) {
		long factor = (long)Math.pow(10,places);
		val = val * factor;
		long tmp = Math.round(val);
		return (double)tmp / factor;
	}

	public static String getFileNameWithMimes(String filePath) {
		String fileSep = "/";
		if (filePath.contains("\\")) {
			fileSep = "\\";
		}
		int idx = filePath.lastIndexOf(fileSep);
		String nameWithMime = filePath.substring(idx+1, filePath.length());
		return nameWithMime;
	}

	public static String getPathMinusMimeSet(File file) {
		String path = file.getAbsolutePath();
		String parent = file.getParent();
		String fileName = path.substring(path.lastIndexOf(File.separator)+1);
		String fileId = fileName.substring(0,fileName.indexOf("."));
		return parent+File.separator+fileId;
	}

	public static String getPathMinusMimeSet(String path) {		
		return getPathMinusMimeSet(new File(path));
	}

}