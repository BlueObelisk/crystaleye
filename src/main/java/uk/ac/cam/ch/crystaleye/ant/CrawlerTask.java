package uk.ac.cam.ch.crystaleye.ant;

import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import uk.ac.cam.ch.crystaleye.fetch.Fetcher;

public class CrawlerTask extends Task {

	String fetcherClass;
	String properties;
	String propertyPrefix;
	String outputDir;
	private Fetcher fetcher;

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	public String getPropertyPrefix() {
		return propertyPrefix;
	}

	public void setPropertyPrefix(String propertyPrefix) {
		this.propertyPrefix = propertyPrefix;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() throws BuildException {
	}

	@Override
	public void execute() throws BuildException {
		if(fetcherClass==null) {
			throw new BuildException("crawlerClass attribute is not optional");
		}
		try {
			Class fetcherClazz = Class.forName(fetcherClass);
			fetcher = (Fetcher) fetcherClazz.newInstance();
			fetcher.fetchAll();
		} catch (ClassNotFoundException e) {
			throw new BuildException(e);
		} catch (InstantiationException e) {
			throw new BuildException(e);
		} catch (IllegalAccessException e) {
			throw new BuildException(e);
		} catch (IOException e) {
			throw new BuildException(e);
		}
	}

	public String getFetcherClass() {
		return fetcherClass;
	}

	public void setFetcherClass(String fetcherClass) {
		this.fetcherClass = fetcherClass;
	}


}
