package wwmm.crystaleye.tools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

public class Execute {

	public static void run(String[] cmd) {
		try {        
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(cmd);
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());            
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());
			errorGobbler.start();
			outputGobbler.start();
			int exitVal = proc.waitFor();       
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private static class StreamGobbler extends Thread {
		
		private InputStream is;

		StreamGobbler(InputStream is) {
			this.is = is;
		}

		public void run() {
			InputStreamReader isr = null;
			BufferedReader br = null;
			try {
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
			} catch (Exception e) {
				throw new RuntimeException("Exception whilst running command, due to: "+e.getMessage(), e);  
			} finally {
				IOUtils.closeQuietly(br);
				IOUtils.closeQuietly(isr);
			}
		}
	}
}