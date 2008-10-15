package ned24.sandbox.crystaleye;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import wwmm.crystaleye.util.Utils;

public class RemoveFilesForSiO {
	
	public static void main(String[] args) {
		String path = "e:/remove.txt";
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(path));
			String line = null;
			while (( line = input.readLine()) != null){
				if (line != null && !"".equals(line)) {
					System.out.println("removing "+line);
					boolean b = Utils.delDir(line.trim());
					System.out.println(b);
				}
			}
			input.close();
		}
		catch (FileNotFoundException ex) {
			throw new RuntimeException("Could not find file: "+path);
		}
		catch (IOException ex){
			throw new RuntimeException("Error reading file: "+path);
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
	}
}
