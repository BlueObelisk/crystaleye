package wwmm.crystaleye.commandline;


public class Execute {

	public static void run(String cmd) {
		try {        
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(cmd);
			// any error message?
			StreamGobbler errorGobbler = new 
			StreamGobbler(proc.getErrorStream(), "ERROR");            

			// any output?
			StreamGobbler outputGobbler = new 
			StreamGobbler(proc.getInputStream(), "OUTPUT");

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			int exitVal = proc.waitFor();       
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}