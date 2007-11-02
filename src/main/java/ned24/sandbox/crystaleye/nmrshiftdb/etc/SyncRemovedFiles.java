package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ned24.sandbox.crystaleye.nmrshiftdb.GaussianConstants;

public class SyncRemovedFiles implements GaussianConstants {

	public static void main(String[] args) {
		String removedPath = CML_DIR+"removed-files/";
		String spPath = removedPath+HSR0_NAME+"/";
		
		List<String> cmlNames = new ArrayList<String>();
		for (File folder : new File(CML_DIR).listFiles()) {
			if (folder.isDirectory() && !folder.getName().equals("removed-files")
					&& !folder.getName().equals("second-protocol")
					&& !folder.getName().equals(".svn")) {
				cmlNames.add(folder.getName());
			}
		}
		
		for (File folder : new File(spPath).listFiles()) {
			if (!folder.isDirectory() || folder.getName().equals(".svn")) {
				continue;
			}
			
			for (File file : folder.listFiles()) {
				if (!file.getAbsolutePath().endsWith(".cml.xml")) {
					continue;
				}
				String filename = file.getName();
				String parentname = file.getParentFile().getName();
				
				moveFiles(cmlNames, filename, parentname);
			}
			
		}
		
	}
	
	public static void moveFiles(List<String> cmlnames, String filename, String parentname) {
		String removedPath = CML_DIR+"removed-files/";
		for (String cmlname : cmlnames) {
			String cmlDir = CML_DIR+cmlname;
			for (File cmlFile : new File(cmlDir).listFiles()) {
				if (cmlFile.getName().equals(filename)) {
					String newPath = removedPath+cmlname+File.separator+parentname;
					File newFile = new File(newPath);
					newFile.mkdirs();
					String name = newPath+File.separator+filename;
					System.out.println(name);
					boolean s = cmlFile.renameTo(new File(name));
					if (!s) {
						throw new RuntimeException("Could not rename from "+cmlFile.getAbsolutePath()+" to "+name);
					}
				}
			}
		}
	}
	
}