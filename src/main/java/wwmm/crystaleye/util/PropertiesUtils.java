package wwmm.crystaleye.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtils {

	public static Properties loadProperties(String filepath) throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(filepath));
		return props;
	}

}
