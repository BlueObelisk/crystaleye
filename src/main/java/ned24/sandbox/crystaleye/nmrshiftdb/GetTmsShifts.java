package ned24.sandbox.crystaleye.nmrshiftdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.cam.ch.crystaleye.Utils;

public class GetTmsShifts {

	public static void main(String[] args) {
		Pattern p = Pattern
		.compile("\\s*\\d+\\s+\\w+\\s+Isotropic\\s+=\\s+([^\\s]*)\\s+Anisotropy\\s+=\\s+[^\\s]*\\s*");
		String folder = "e:/gaussian/TMS/inputs/second-protocol/1";
		for (File file : new File(folder).listFiles()) {
			String path = file.getAbsolutePath();
			if (path.endsWith(".out")) {
				List<String> values = new ArrayList<String>();
				BufferedReader input = null;
				try {
					input = new BufferedReader(new FileReader(path));
					String line = null;
					while ((line = input.readLine()) != null) {
						if (line != null && !"".equals(line)) {
							Matcher m = p.matcher(line);
							if (m.find()) {
								values.add(m.group(1));
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
				if (values.size() != 17) {
					throw new RuntimeException("Wrong number of atoms.");
				}
				double total = 0; 
				for (int i = 1; i < 5; i++) {
					total += Double.valueOf(values.get(i));
				}
				double average = total/4;
				System.out.println(file.getName()+" - "+Utils.round(average, 4));
			}
		}
	}
}
