package wwmm.crystaleye.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;

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
		try {
			// TODO - move these out to a properties file somewhere.
			cfg.setSharedVariable("crystaleyeSiteUrl", "http://wwmm.ch.cam.ac.uk/crystaleye");
			cfg.setSharedVariable("wwmmSandboxSiteUrl", "http://wwmm-sandbox.ch.cam.ac.uk");
		} catch (TemplateModelException e) {
			throw new IllegalStateException("This shouldn't happen!.");
		}
		return cfg;
	}
	
	public static Template getHtmlTemplate(String filename) {
		try {
			return getConfiguration().getTemplate("/html/"+filename);
		} catch (IOException e) {
			throw new IllegalStateException("BUG: could not load Freemarker HTML template: "+filename, e);
		}
	}

	public static void writeHtmlTemplate(String templatePath, File outfile, TemplateHashModel model) {
		File parent = outfile.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		Template tpl = FreemarkerUtils.getHtmlTemplate(templatePath);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outfile));
			tpl.process(model, bw);
		} catch (Exception e) {
			throw new RuntimeException("Exception writing file ("+outfile+"), due to: "+e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(bw);
		}
	}
	
}
