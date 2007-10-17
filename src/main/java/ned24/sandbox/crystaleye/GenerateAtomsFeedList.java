package ned24.sandbox.crystaleye;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmlcml.molutil.ChemicalElement;

public class GenerateAtomsFeedList {
	public static void main(String[] args) {
		String str = "";
		List<String> set = new ArrayList<String>();
		for (int i = 1; i < 105; i++) {
			ChemicalElement ce = ChemicalElement.getElement(i);
			String symbol = ce.getSymbol();
			set.add(symbol);
		}
		Collections.sort(set);
		for (String s : set) {
			str += "<li style=\"font-weight: bold;\">"+s+"</li>\n" +
					"<ul class=\"normal\">\n" +
					"<li>RSS (<a href='./"+s+"/rss/rss_10/feed.xml'>rss1</a> | " +
							"<a href='./"+s+"/rss/rss_20/feed.xml'>rss2</a> | " +
							"<a href='./"+s+"/rss/atom_10/feed.xml'>atom1</a>)" +
							"</li>\n" +
					"<li>CMLRSS (<a href='./"+s+"/cmlrss/rss_10/feed.xml'>rss1</a> | " +
					"<a href='./"+s+"/cmlrss/rss_20/feed.xml'>rss2</a> | " +
					"<a href='./"+s+"/cmlrss/atom_10/feed.xml'>atom1</a>)" +
					"</li>\n" +
					"</ul>\n";
		}
		System.out.println(str);
	}
}
