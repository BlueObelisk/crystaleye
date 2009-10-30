package wwmm.crystaleye;

import java.io.IOException;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Utility class for generic methods for using the Freemarker
 * templating library.
 * 
 * @author Nick Day
 * @version 0.2
 *
 */
public class FreemarkerUtils {

	public static Configuration getConfiguration() {
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(cfg.getClass(), "/freemarker");
		return cfg;
	}
	
	public static Template getHtmlTemplate(String filename) {
		try {
			return getConfiguration().getTemplate("/html/"+filename);
		} catch (IOException e) {
			throw new IllegalStateException("BUG: could not load Freemarker HTML template: "+filename, e);
		}
	}

}
