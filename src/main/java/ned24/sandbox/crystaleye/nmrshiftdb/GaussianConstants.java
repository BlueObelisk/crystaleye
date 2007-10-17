package ned24.sandbox.crystaleye.nmrshiftdb;

import org.xmlcml.cml.base.CMLConstants;

public interface GaussianConstants extends CMLConstants {
	
	public static final String SOLVENT_ROLE = "subst:solvent";
	public static final String FIELD_DICTREF = "cml:field";
	
	public static final String GAUSSIAN_NMR = "nmr:GAUSSIAN";
	public static final String OBSERVED_NMR = "nmr:OBSERVENUCLEUS";
	
	public static final String NMR_TYPE= "NMR";
	public static final String UNITS_PPM = "units:ppm";
	
	public static final double TMS_SHIFT = 200;	
	
	// MIMES
	public static final String FLOW_MIME = ".gjf";
	public static final String SUBMIT_FILE_MIME = ".condor.sh";
	
	// constants for scatter plots
	public static final String X_LAB = "Observed shift (ppm)";
	public static final String Y_LAB = "Gaussian calculated shift (ppm)";
	public static final String TITLE = "Observed versus Gaussian calculated chemical shift for 13C spectra";
	public static final int PAGE_WIDTH = 675;
	public static final int PAGE_HEIGHT = 675;
	public static final int TICK_MARKS = 10;
	public static final int X_MIN = 0;
	public static final int X_MAX = 200;
	public static final int Y_MIN = 0;
	public static final int Y_MAX = 200;
	
	// jmol
	public static final String FILE_PREFIX = "file:///";
	public static final String JMOL_ROOT_DIR = "e:/test/";
	public static final String JMOL_APPLET_FOLDER	= FILE_PREFIX+JMOL_ROOT_DIR;
	public static final String JMOL_JS_PATH = FILE_PREFIX+JMOL_ROOT_DIR+"/Jmol.js";
	public static final String SUMMARY_JS_PATH = FILE_PREFIX+JMOL_ROOT_DIR+"/summary.js";
	public static final String CML_DIR_NAME = "cml";
}
