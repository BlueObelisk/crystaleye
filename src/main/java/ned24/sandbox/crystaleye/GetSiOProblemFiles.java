package ned24.sandbox.crystaleye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Nodes;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLCml;
import org.xmlcml.cml.element.CMLCrystal;

import wwmm.crystaleye.util.XmlIOUtils;

public class GetSiOProblemFiles implements CMLConstants {

	public static void main(String[] args ) {
		String path = "e:/O-Si.csv";
		String dataPath = "e:/crystaleye-test2/data/";
		
		BufferedReader input = null;
		List<String> list = new ArrayList<String>();
		try {
			input = new BufferedReader(new FileReader(path));
			String line = null;
			while (( line = input.readLine()) != null){
				if (line != null && !"".equals(line)) {
					String[] a = line.split(",");
					if (a.length != 10) {
						continue;
					}
					if (a[7].equals("<span class='formula'>MgO<sub>3</sub>Si</span>")) {
						double d = Double.valueOf(a[0]);
						if (d > 1.22 && d <= 1.23) {
							list.add(a[1]);
						}
					}
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
		
		Set<String> set = new HashSet<String>();
		for (String s : list) {
			int idx = s.lastIndexOf("_");
			s = s.substring(0,idx);
			set.add(s);
		}
		
		Map<String, String> pMap = new HashMap<String, String>();
		for (String s : set) {
			String[] a = s.split("_");
			String p = dataPath+a[0]+File.separator+a[1]+File.separator+a[2]+File.separator
			+a[3]+File.separator+a[4]+File.separator+a[4]+"_"+a[5]+File.separator+a[4]+"_"+a[5]+".complete.cml.xml";
			CMLCml cml = (CMLCml)XmlIOUtils.parseCmlFile(p).getRootElement();
			CMLCrystal crystal = (CMLCrystal)cml.query(".//cml:crystal", CML_XPATH).get(0);
			Nodes scalars = crystal.query(".//cml:scalar", CML_XPATH);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < scalars.size(); i++){
				sb.append(scalars.get(i).getValue()+"_");
			}
			String hg = sb.toString();
			hg = hg.substring(0,hg.length()-1);
			pMap.put(s, hg);
		}
		
		set = new HashSet<String>();
		List<String> removeList = new ArrayList<String>();
		for (Iterator it = pMap.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry)it.next();
	        String key = (String)entry.getKey();
	        String value = (String)entry.getValue();
	        if (set.contains(value)) {
				removeList.add(key);
			} else {
				set.add(value);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for (String s : removeList) {
			String[] a = s.split("_");
			String p = "e:\\crystaleye-test2\\data\\"+a[0]+File.separator+a[1]+File.separator+a[2]+File.separator
			+a[3]+File.separator+a[4];

			sb.append(p+"\n");
		}
		
		XmlIOUtils.writeText(sb.toString(), "e:/remove.txt");
		
		/*
		StringBuilder sb = new StringBuilder();
		File outFile = new File("e:/new-csv");
		int count = 0;
		input = null;
		try {
			input = new BufferedReader(new FileReader(path));
			String line = null;
			while (( line = input.readLine()) != null){
				if (line != null && !"".equals(line)) {
					boolean add = true;
					for (String s : removeList) {
						if (line.contains(s)) {
							add = false;
							break;
						}
					}
					if (add) {
						sb.append(line+"\n");
					} else {
						System.err.println("FOUND DUPLICATE");
					}
					if (count == 500) {
						if (outFile.exists()) {
							IOUtils.appendToFile(outFile, sb.toString());
						} else {
							IOUtils.writeText(sb.toString(), outFile.getAbsolutePath());
						}
						count = 0;
						sb = new StringBuilder();
					}
					count++;
				}
			}
			if (outFile.exists()) {
				IOUtils.appendToFile(outFile, sb.toString());
			} else {
				IOUtils.writeText(sb.toString(), outFile.getAbsolutePath());
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
		*/
	}
}
