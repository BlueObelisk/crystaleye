package ned24.sandbox;

import wwmm.crystaleye.Update;

public class RunCrystaleye {
	
	public static void main(String[] args) {
		String[] as = {"-p", "c:/workspace/my-crystaleye/conf/crystaleye.properties", 
				"-bondlengths"};
		Update.main(as);
	}

}
