package ned24.sandbox.crystaleye.nmrshiftdb;

import java.util.List;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Text;

import org.graph.GraphException;
import org.graph.Point;
import org.graph.SVGElement;
import org.layout.GraphLayout;
import org.layout.PageLayout;
import org.scatter.Scatter;

public class GaussianScatter implements GaussianConstants {

	List<Point> pointList;
	int xmin = X_MIN;
	int xmax = X_MAX;
	int ymin = Y_MIN;
	int ymax = Y_MAX;
	int pageWidth = PAGE_WIDTH;
	int pageHeight = PAGE_HEIGHT;
	int xtickMarks = TICK_MARKS;
	int ytickMarks = TICK_MARKS;

	public void setXmax(int xmax) {
		this.xmax = xmax;
	}

	public void setXmin(int xmin) {
		this.xmin = xmin;
	}

	public void setYmax(int ymax) {
		this.ymax = ymax;
	}

	public void setYmin(int ymin) {
		this.ymin = ymin;
	}

	public GaussianScatter(List<Point> pointList) {
		this.pointList = pointList;
	}
	
	public Document getPlot() {
		GraphLayout gLayout = new GraphLayout();
		gLayout.setXmin(xmin);
		gLayout.setXmax(xmax);
		gLayout.setYmin(ymin);
		gLayout.setYmax(ymax);
		gLayout.setPlotXGridLines(true);
		gLayout.setPlotYGridLines(true);
		Document doc = null;
		try {
			PageLayout pLayout = new PageLayout();
			pLayout.setPageWidth(pageWidth);
			pLayout.setPageHeight(pageHeight);
			gLayout.setNXTickMarks(xtickMarks);
			gLayout.setNYTickMarks(ytickMarks);
			Scatter scatter = new Scatter(pLayout, gLayout);
			scatter.addDataToPlot(pointList);
			scatter.setXlab(X_LAB);
			scatter.setYlab(Y_LAB);
			scatter.setGraphTitle("");
			scatter.plot();
			doc = new Document(scatter.getSVG());
		} catch (GraphException e) {
			e.printStackTrace();
		}

		appendScriptElement(doc.getRootElement());
		return doc;
	}
	
	private void appendScriptElement(Element element) {
		SVGElement script = new SVGElement("script");
		element.appendChild(script);
		script.addAttribute(new Attribute("type", "text/ecmascript"));
		Text content = new Text("function changeAtom(path, num) {parent.changeAtomInJmol(path, num);}");
		script.appendChild(content);
	}

	public void setPageHeight(int pageHeight) {
		this.pageHeight = pageHeight;
	}

	public void setPageWidth(int pageWidth) {
		this.pageWidth = pageWidth;
	}

	public void setPointList(List<Point> pointList) {
		this.pointList = pointList;
	}

	public void setXTickMarks(int xtickMarks) {
		this.xtickMarks = xtickMarks;
	}
	
	public void setYTickMarks(int ytickMarks) {
		this.ytickMarks = ytickMarks;
	}
}
