package wwmm.crystaleye.nodefactories;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Comment;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ProcessingInstruction;
import nu.xom.Serializer;
import nu.xom.Text;

import org.xmlcml.cml.base.CMLBuilder;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLMolecule;

public class Cml2FooNodeFactory extends NodeFactory implements CMLConstants {

	private Nodes empty = new Nodes();
	boolean insideMol = false;

	public Element startMakingElement(String name, String namespace) {
		if (insideMol ||
				(name.equals(CMLMolecule.TAG))) {
			insideMol = true;
			//System.out.println("START ELEMENT: "+name);
			return new Element(name, namespace);
		} else {
			return new Element(name, namespace);
		}
	}

	public Nodes finishMakingElement(Element element) {
		//System.out.println("END ELEMENT: "+element.getLocalName());
		if (insideMol) {
			if (element.getLocalName().equals(CMLMolecule.TAG)) {
				insideMol = false;
			}
			return new Nodes(element);
		} else if (element.getParent() instanceof Document) {
			return new Nodes(element);
		} else {
			return empty;
		}
	}

	public Nodes makeComment(String data) {	
		if (insideMol) {
			return new Nodes(new Comment(data));
		} else {
			return empty;
		}
	}    

	public Nodes makeText(String data) {
		if (insideMol) {
			return new Nodes(new Text(data));
		} else {
			return empty;
		}
	}    

	public Nodes makeAttribute(String name, String namespace, 
			String value, Attribute.Type type) {
		if (insideMol) {
			return new Nodes(new Attribute(name, namespace, value, type)); 
		} else {
			return empty;
		}
	}

	public Nodes makeProcessingInstruction(
			String target, String data) {
		if (insideMol) {
			return new Nodes(new ProcessingInstruction(target, data));
		} else {
			return empty;
		}
	}

	public static void main(String[] args) {

		String filename = "E:\\data-test\\data\\acta\\e\\2007\\01-00\\oa1001\\oa1001_ErNbAl\\oa1001_ErNbAl.raw.cml.xml";

		try {
			Builder parser = new Builder(new Cml2FooNodeFactory());
			Builder parser2 = new CMLBuilder();

			Document document = null;
			int tries = 1;
			List<Long> list = new ArrayList<Long>();
			for (int i = 0; i < tries; i++) {
				long start2 = System.nanoTime();
				document  = parser2.build(new FileReader(filename));
				long end2 = System.nanoTime();
				list.add(end2-start2);
			}

			long total = 0;
			for (Long l : list) {
				total += l;
			}
			System.out.println(total/list.size());

			// Write it out again
			Serializer serializer = new Serializer(System.out);
			serializer.write(document);

		}
		catch (IOException ex) { 
			System.out.println(
					"Due to an IOException, the parser could not encode " + args[0]
			); 
		}
		catch (ParsingException ex) { 
			System.out.println(ex); 
			ex.printStackTrace(); 
		}

	} 

}