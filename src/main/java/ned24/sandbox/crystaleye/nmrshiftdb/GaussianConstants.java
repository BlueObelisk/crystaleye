package ned24.sandbox.crystaleye.nmrshiftdb;

import org.xmlcml.cml.base.CMLConstants;

public interface GaussianConstants extends CMLConstants {
	
	public static final String GAUSSIAN_DICT = "e:/legacy2cml/dict/gaussianArchiveDict.xml";
	
	public static final String SOLVENT_ROLE = "subst:solvent";
	public static final String FIELD_DICTREF = "cml:field";
	public static final String GAUSSIAN_NMR = "nmr:GAUSSIAN";
	public static final String OBSERVED_NMR = "nmr:OBSERVENUCLEUS";
	public static final String NMR_TYPE= "NMR";
	public static final String UNITS_PPM = "units:ppm";
	
	// MIMES
	public static final String FLOW_MIME = ".gjf";
	public static final String SUBMIT_FILE_MIME = ".condor.sh";
	public static final String GAUSSIAN_CONVERTER_OUT_MIME = ".xml";
	
	// constants for scatter plots
	public static final String X_LAB = "Observed shift (ppm)";
	public static final String Y_LAB = "Gaussian calculated shift (ppm)";
	public static final String TITLE = "Observed versus Gaussian calculated chemical shift for 13C spectra";
	public static final int PAGE_WIDTH = 715;
	public static final int PAGE_HEIGHT = 675;
	public static final int TICK_MARKS = 10;
	public static final int X_MIN = 0;
	public static final int X_MAX = 200;
	public static final int Y_MIN = 0;
	public static final int Y_MAX = 200;
	
	// jmol
	public static final String FILE_PREFIX = "file:///";
	public static final String GAUSSIAN_DIR = "e:/gaussian/";
	public static final String HTML_DIR = GAUSSIAN_DIR+"html/";
	public static final String CML_DIR = GAUSSIAN_DIR+"cml/";
	
	public static final String SECOND_PROTOCOL_NAME = "second-protocol";
	public static final String SECOND_PROTOCOL_MOD1_NAME = "second-protocol_mod1";
	public static final String SECOND_PROTOCOL_MANUALMOD_NAME = "second-protocol_manualmod";
	public static final String SECOND_PROTOCOL_MANUAL_AND_MORGAN_NAME = "second-protocol_manualAndMorgan";
	// first diff
	public static final String FIRST_DIFF_FOLDER	= HTML_DIR+"first-diff";
}
