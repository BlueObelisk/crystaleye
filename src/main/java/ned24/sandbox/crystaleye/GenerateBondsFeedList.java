package ned24.sandbox.crystaleye;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmlcml.molutil.ChemicalElement;

import wwmm.crystaleye.IOUtils;

public class GenerateBondsFeedList {
	public static void main(String[] args) {
		String str = "";
		List<String> set = new ArrayList<String>();
		for (int i = 1; i < 105; i++) {
			ChemicalElement ce = ChemicalElement.getElement(i);
			String symbol1 = ce.getSymbol();
			for (int j = 0; j < 105; j++) {
				IOUtils.writeText("", "C:\\Documents and Settings\\Nick Day\\Desktop\\homepage\\feed\\bonds\\"+symbol1+"-index.html");
			}
		}
		
		Collections.sort(set);
		for (String s : set) {
			str += "<li style='font-weight: bold;'><a href=\""+s+"-index.html\">"+s+"</a></li>\n";
		}
		System.out.println(str);
	}
}
