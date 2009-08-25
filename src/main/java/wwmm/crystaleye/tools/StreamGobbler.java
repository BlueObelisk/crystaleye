package wwmm.crystaleye.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class StreamGobbler extends Thread {
	
	InputStream is;
	String type;
	OutputStream os;

	StreamGobbler(InputStream is, String type) {
		this(is, type, null);
	}

	StreamGobbler(InputStream is, String type, OutputStream os) {
		this.is = is;
		this.type = type;
		this.os = os;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (os != null) {
					//System.out.println(type + ">" + line);    
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();  
		}
	}
}