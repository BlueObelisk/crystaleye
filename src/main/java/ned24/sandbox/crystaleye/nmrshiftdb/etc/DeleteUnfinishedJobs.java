package ned24.sandbox.crystaleye.nmrshiftdb.etc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteUnfinishedJobs {

	public static void main(String[] args) {
		String folder = "e:/gaussian/outputs/second-protocol/1/";
		List<File> list = new ArrayList<File>();
		Pattern p = Pattern
		.compile("\\s*\\d+\\s+\\w+\\s+Isotropic\\s+=\\s+([^\\s]*)\\s+Anisotropy\\s+=\\s+[^\\s]*\\s*");
		for (File file : new File(folder).listFiles()) {
			String path = file.getAbsolutePath();
			if (path.endsWith(".out")) {
				System.out.println(path);
				boolean found = false;
				BufferedReader input = null;
				try {
					input = new BufferedReader(new FileReader(path));
					String line = null;
					while ((line = input.readLine()) != null) {
						if (line != null && !"".equals(line)) {
							Matcher m = p.matcher(line);
							if (m.find()) {
								found = true;
								break;
							}
						}
					}
					input.close();
				} catch (FileNotFoundException ex) {
					throw new RuntimeException("Could not find file: " + path);
				} catch (IOException ex) {
					throw new RuntimeException("Error reading file: " + path);
				} finally {
					try {
						if (input != null) {
							input.close();
						}
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				if (!found) {
					list.add(file);
				}
			}
		}
		
		for (File file : list){
			String name = file.getName();
			name = name.substring(0,name.length()-4);
			
			File parent = file.getParentFile();
			File gjf = new File(parent+File.separator+name+".gjf");

			file.delete();
			gjf.delete();
		}
	}
}

